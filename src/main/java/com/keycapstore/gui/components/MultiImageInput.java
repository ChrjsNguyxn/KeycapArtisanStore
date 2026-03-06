package com.keycapstore.gui.components;

import com.keycapstore.utils.ImageHelper;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MultiImageInput extends JPanel {

    private JLabel lblLargeImage;
    private JPanel thumbnailContainer;
    private ArrayList<String> imagePaths; // Danh sách tên file ảnh
    private JButton btnAdd;
    private final int THUMB_SIZE = 60;
    private final int LARGE_IMG_SIZE = 250;

    public MultiImageInput() {
        imagePaths = new ArrayList<>();
        setLayout(new BorderLayout(0, 5));
        setBackground(Color.WHITE);
        setBorder(null);

        // 1. Vùng hiển thị ảnh lớn (CENTER) - Bọc trong 1 panel để căn giữa
        JPanel largeImageWrapper = new JPanel(new GridBagLayout());
        largeImageWrapper.setOpaque(false);
        lblLargeImage = new JLabel("Click [+] để thêm ảnh", SwingConstants.CENTER);
        lblLargeImage.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblLargeImage.setForeground(Color.GRAY);
        lblLargeImage.setPreferredSize(new Dimension(LARGE_IMG_SIZE, LARGE_IMG_SIZE));
        lblLargeImage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblLargeImage.setOpaque(true);
        lblLargeImage.setBackground(new Color(248, 248, 248));
        largeImageWrapper.add(lblLargeImage);
        add(largeImageWrapper, BorderLayout.CENTER);

        // 2. Dải ảnh thumbnail (SOUTH)
        thumbnailContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        thumbnailContainer.setBackground(Color.WHITE);

        // Nút thêm ảnh (Dấu +)
        btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnAdd.setPreferredSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
        btnAdd.setBackground(new Color(240, 240, 240));
        btnAdd.setForeground(Color.GRAY);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> chooseImages());

        JScrollPane scroll = new JScrollPane(thumbnailContainer);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, THUMB_SIZE + 10)); // Chiều cao cho thumbnail + padding

        add(scroll, BorderLayout.SOUTH);

        refreshView();
    }

    private void chooseImages() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true); // Cho phép chọn nhiều ảnh
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));

        // THÊM TÍNH NĂNG: Xem trước ảnh bên phải cửa sổ chọn file
        fc.setAccessory(new ImagePreview(fc));

        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            for (File f : files) {
                try {
                    // Lưu và resize ảnh
                    String savedName = ImageHelper.saveImage(f);
                    imagePaths.add(savedName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            refreshView();
        }
    }

    private void refreshView() {
        // 1. Cập nhật ảnh lớn
        if (!imagePaths.isEmpty()) {
            ImageIcon largeIcon = ImageHelper.loadResizedIcon(imagePaths.get(0), LARGE_IMG_SIZE, LARGE_IMG_SIZE);
            lblLargeImage.setIcon(largeIcon);
            lblLargeImage.setText(null);
        } else {
            lblLargeImage.setIcon(null);
            lblLargeImage.setText("Click [+] để thêm ảnh");
        }

        // 2. Cập nhật dải thumbnail
        thumbnailContainer.removeAll();
        for (int i = 0; i < imagePaths.size(); i++) {
            String path = imagePaths.get(i);
            final int index = i;

            // Load thumbnail
            ImageIcon originalIcon = ImageHelper.loadResizedIcon(path, THUMB_SIZE, THUMB_SIZE);

            // --- VẼ SỐ THỨ TỰ LÊN ẢNH ---
            // Tạo một ảnh mới để vẽ số lên trên
            BufferedImage combinedImage = new BufferedImage(THUMB_SIZE, THUMB_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = combinedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 1. Vẽ ảnh gốc
            if (originalIcon != null) {
                g.drawImage(originalIcon.getImage(), 0, 0, null);
            } else {
                // Vẽ placeholder nếu ảnh lỗi/không tìm thấy
                g.setColor(new Color(240, 240, 240));
                g.fillRect(0, 0, THUMB_SIZE, THUMB_SIZE);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.drawString("No Img", 10, 35);
            }

            // 2. Vẽ nền cho số (Góc trái trên)
            g.setColor(new Color(0, 0, 0, 150)); // Màu đen mờ
            g.fillRoundRect(2, 2, 20, 20, 10, 10);
            // 3. Vẽ số
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            // Căn giữa số trong ô tròn
            String numStr = String.valueOf(i + 1);
            FontMetrics fm = g.getFontMetrics();
            int textX = 2 + (20 - fm.stringWidth(numStr)) / 2;
            int textY = 2 + ((20 - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(numStr, textX, textY);
            g.dispose();

            JLabel lblImg = new JLabel(new ImageIcon(combinedImage));
            lblImg.setPreferredSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
            lblImg.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Viền: Ảnh đầu tiên (Cover) viền Đỏ đậm, còn lại viền Xám
            if (i == 0) {
                lblImg.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(3, 3, 3, 3, ThemeColor.DANGER),
                        BorderFactory.createEmptyBorder(1, 1, 1, 1)));
                lblImg.setToolTipText("★ ẢNH ĐẠI DIỆN (Click chuột phải để tùy chọn)");
            } else {
                lblImg.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                lblImg.setToolTipText("Vị trí " + (i + 1) + " (Click chuột phải để di chuyển/xóa)");
            }

            // Sự kiện: Click trái xem, Click phải mở menu
            lblImg.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        // Xem ảnh lớn
                        ImageIcon largeIcon = ImageHelper.loadResizedIcon(path, LARGE_IMG_SIZE, LARGE_IMG_SIZE);
                        lblLargeImage.setIcon(largeIcon);
                    }
                }
            });

            // --- MENU CHUỘT PHẢI ---
            JPopupMenu menu = new JPopupMenu();

            // 1. Đặt làm ảnh đại diện (Nếu không phải là ảnh đầu)
            if (index != 0) {
                JMenuItem itemCover = new JMenuItem("Đặt làm ảnh đại diện (Vị trí 1)");
                itemCover.setFont(new Font("Segoe UI", Font.BOLD, 12));
                itemCover.addActionListener(e -> {
                    Collections.swap(imagePaths, index, 0);
                    refreshView();
                });
                menu.add(itemCover);
                menu.addSeparator();
            }

            // 2. Dời đến vị trí...
            JMenu menuMove = new JMenu("Dời đến vị trí...");
            for (int k = 0; k < imagePaths.size(); k++) {
                if (k == index)
                    continue; // Bỏ qua vị trí hiện tại

                int targetPos = k; // Biến final cho lambda
                JMenuItem itemPos = new JMenuItem("Vị trí " + (k + 1));
                itemPos.addActionListener(e -> {
                    String item = imagePaths.remove(index);
                    imagePaths.add(targetPos, item);
                    refreshView();
                });
                menuMove.add(itemPos);
            }
            menu.add(menuMove);
            menu.addSeparator();

            // 3. Xóa ảnh
            JMenuItem itemDel = new JMenuItem("Xóa ảnh này");
            itemDel.setForeground(ThemeColor.DANGER);
            itemDel.addActionListener(e -> {
                imagePaths.remove(index);
                refreshView();
            });
            menu.add(itemDel);

            lblImg.setComponentPopupMenu(menu);

            thumbnailContainer.add(lblImg);
        }

        // Luôn thêm nút Add ở cuối
        thumbnailContainer.add(btnAdd);

        thumbnailContainer.revalidate();
        thumbnailContainer.repaint();
    }

    // Lấy ảnh đại diện (ảnh đầu tiên)
    public String getCoverImage() {
        if (imagePaths.isEmpty())
            return "";
        return imagePaths.get(0);
    }

    // Lấy tất cả ảnh
    public List<String> getAllImages() {
        return imagePaths;
    }

    // Set dữ liệu (dùng khi edit)
    public void setImages(List<String> paths) {
        this.imagePaths.clear();
        if (paths != null) {
            this.imagePaths.addAll(paths);
        }
        refreshView();
    }

    // --- INNER CLASS: TẠO KHUNG XEM TRƯỚC ẢNH TRONG JFILECHOOSER ---
    private class ImagePreview extends JPanel implements PropertyChangeListener {
        private JLabel label;
        private int maxImgWidth = 200;
        private int maxImgHeight = 200;

        public ImagePreview(JFileChooser fc) {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(210, 210));
            setBorder(BorderFactory.createTitledBorder("Xem trước"));

            label = new JLabel();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setText("Chưa chọn ảnh");
            add(label, BorderLayout.CENTER);

            // Lắng nghe sự kiện khi người dùng click vào file
            fc.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();

            // Khi file được chọn thay đổi
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
                File file = (File) e.getNewValue();
                if (isShowing() && file != null) {
                    loadImage(file);
                } else {
                    label.setIcon(null);
                    label.setText("Chưa chọn ảnh");
                }
            }
        }

        private void loadImage(File f) {
            try {
                ImageIcon icon = new ImageIcon(f.getPath());
                if (icon.getIconWidth() > 0) {
                    Image img = icon.getImage();
                    // Scale ảnh vừa khung preview
                    Image scaledImg = img.getScaledInstance(maxImgWidth, maxImgHeight, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImg));
                    label.setText("");
                } else {
                    label.setText("Không phải ảnh");
                }
            } catch (Exception e) {
                label.setText("Lỗi đọc ảnh");
            }
        }
    }
}