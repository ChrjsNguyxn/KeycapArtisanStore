package com.keycapstore.gui;

import com.keycapstore.bus.ProductBUS;
import com.keycapstore.model.Product;
import com.keycapstore.utils.ImageHelper;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HomePanel extends JPanel {

    // Bảng màu chuẩn Vibe Dark/Edgy
    private final Color COLOR_BG_DARK = new Color(18, 18, 18); // Đen nhám nền
    private final Color COLOR_CARD_BG = new Color(30, 30, 30); // Đen xám cho thẻ sản phẩm
    private final Color COLOR_TEXT_PRIMARY = new Color(255, 255, 255); // Trắng
    private final Color COLOR_ACCENT = new Color(217, 4, 41); // Đỏ nhấn (nút bấm, giá tiền)

    private ProductBUS productBUS;
    private DecimalFormat df = new DecimalFormat("#,###");

    public HomePanel() {
        productBUS = new ProductBUS();

        setLayout(new BorderLayout());
        setBackground(COLOR_BG_DARK);

        // 1. HEADER / HERO BANNER (Khu vực truyền cảm hứng)
        JPanel bannerPanel = new JPanel();
        bannerPanel.setBackground(COLOR_BG_DARK);
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel lblTitle = new JLabel("KEYFORGE ARTISAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSlogan = new JLabel("- MORE INSPIRATIONAL -");
        lblSlogan.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lblSlogan.setForeground(Color.LIGHT_GRAY);
        lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        bannerPanel.add(lblTitle);
        bannerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bannerPanel.add(lblSlogan);

        // 2. PRODUCT GRID (Khu vực trưng bày Keycap)
        JPanel gridPanel = new JPanel();
        gridPanel.setBackground(COLOR_BG_DARK);
        // Chia lưới: số dòng linh hoạt (0), 3 cột, khoảng cách 20px
        gridPanel.setLayout(new GridLayout(0, 3, 20, 20));
        gridPanel.setBorder(new EmptyBorder(20, 40, 40, 40));

        // Load data thật từ DB
        loadProducts(gridPanel);

        // 3. GÓI TẤT CẢ VÀO SCROLLPANE
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(COLOR_BG_DARK); // Set nền đen cho container chính
        mainContent.add(bannerPanel, BorderLayout.NORTH);
        mainContent.add(gridPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Lăn chuột cho mượt
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadProducts(JPanel gridPanel) {
        List<Product> list = productBUS.getAllProducts();

        if (list == null || list.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có sản phẩm nào trong kho.");
            lblEmpty.setForeground(Color.GRAY);
            gridPanel.add(lblEmpty);
            return;
        }

        // Hiển thị tối đa 9 sản phẩm nổi bật hoặc mới nhất
        int limit = Math.min(list.size(), 9);
        for (int i = 0; i < limit; i++) {
            Product p = list.get(i);
            // Tạo tag giả lập cho đẹp (Logic thực tế có thể dựa vào ngày tạo hoặc tồn kho)
            String tag = "";
            if (p.getStock() < 5)
                tag = "Sắp hết";
            else if (i < 3)
                tag = "Hot";
            else if (i > list.size() - 3)
                tag = "Mới";

            gridPanel.add(createProductCard(p, tag));
        }
    }

    // Hàm chế tạo từng cái Card Sản Phẩm
    private JPanel createProductCard(Product p, String tag) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                new EmptyBorder(15, 15, 15, 15)));

        // Khu vực Ảnh
        JLabel lblImage = new JLabel();
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(60, 60, 60));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(200, 150));
        lblImage.setMaximumSize(new Dimension(300, 200));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Load ảnh thật dùng Helper
        ImageIcon icon = ImageHelper.loadResizedIcon(p.getImage(), 200, 150);
        if (icon != null) {
            lblImage.setIcon(icon);
            lblImage.setText("");
        } else {
            lblImage.setText("No Image");
            lblImage.setForeground(Color.WHITE);
        }

        // Tên sản phẩm
        JLabel lblName = new JLabel(p.getName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(COLOR_TEXT_PRIMARY);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Giá tiền & Tag sale
        String priceStr = df.format(p.getPrice()) + "đ";
        JLabel lblPrice = new JLabel(priceStr + "  " + (tag.isEmpty() ? "" : "[" + tag + "]"));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(COLOR_ACCENT);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nút Thêm vào giỏ
        JButton btnAdd = new JButton("Thêm vào giỏ");
        btnAdd.setBackground(COLOR_ACCENT);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Sự kiện thêm vào giỏ
        btnAdd.addActionListener(e -> {
            CartPanel.addToCart(p, 1);
            JOptionPane.showMessageDialog(this, "Đã thêm " + p.getName() + " vào giỏ hàng!");
        });

        // Ráp mọi thứ vào Card
        card.add(lblImage);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(lblName);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblPrice);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(btnAdd);

        return card;
    }

    // --- MAIN METHOD ĐỂ TEST GIAO DIỆN ---
    public static void main(String[] args) {
        // Setup FlatLaf Dark để test cho đẹp (nếu có thư viện)
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception ex) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Home UI - Keyforge Artisan");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new HomePanel());
            frame.setVisible(true);
        });
    }
}
