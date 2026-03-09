package com.keycapstore.gui;

import com.keycapstore.bus.ProductBUS;
import com.keycapstore.dao.WishlistDAO;
import com.keycapstore.model.Customer;
import com.keycapstore.model.Product;
import com.keycapstore.utils.ImageHelper;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class WishlistPanel extends JPanel implements Refreshable {

    private Object currentUser;
    private ProductBUS productBUS;
    private WishlistDAO wishlistDAO;
    private JPanel gridPanel;
    private DecimalFormat df = new DecimalFormat("#,###");
    private Consumer<String> onNavigate;

    private Color borderColor = new Color(153, 143, 133);
    private Color primaryColor = new Color(62, 54, 46);
    private Color dangerRed = new Color(231, 76, 60);
    private Color successGreen = new Color(46, 204, 113);
    private Color textSecondary = new Color(102, 102, 102);
    private Color textPrimary = new Color(51, 51, 51);
    private Color bgColor = new Color(228, 220, 207);
    private Color cardColor = new Color(255, 252, 245);

    public WishlistPanel(Object user, Consumer<String> onNavigate) {
        this.currentUser = user;
        this.onNavigate = onNavigate;
        this.productBUS = new ProductBUS();
        this.wishlistDAO = new WishlistDAO();

        setLayout(new BorderLayout());
        setBackground(bgColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(bgColor);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("DANH SÁCH YÊU THÍCH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(primaryColor);

        try {
            java.net.URL iconURL = getClass().getResource("/icons/wishlist.png");
            if (iconURL == null) {
                File f = new File("src/main/resources/icons/wishlist.png");
                if (f.exists()) {
                    iconURL = f.toURI().toURL();
                }
            }

            if (iconURL == null)
                iconURL = getClass().getResource("/icons/heart.png");

            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            } else {
                System.err.println("WishlistPanel: Không tìm thấy icon wishlist.png hoặc heart.png trong /icons/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnClearAll = new JButton("Xóa tất cả");
        btnClearAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClearAll.setForeground(Color.WHITE);
        btnClearAll.setBackground(dangerRed);
        btnClearAll.setFocusPainted(false);
        btnClearAll.setBorderPainted(false);
        btnClearAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearAll.addActionListener(e -> clearAllWishlist());

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);
        rightHeader.add(btnClearAll);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setBackground(bgColor);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(bgColor);
        add(scrollPane, BorderLayout.CENTER);

        loadWishlist();
    }

    private void clearAllWishlist() {
        if (!(currentUser instanceof Customer))
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa toàn bộ sản phẩm trong danh sách yêu thích?",
                "Xác nhận xóa tất cả", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int cusId = ((Customer) currentUser).getCustomerId();
            if (wishlistDAO.deleteAllByCustomerId(cusId)) {
                JOptionPane.showMessageDialog(this, "Đã xóa sạch danh sách yêu thích!");
                loadWishlist();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadWishlist() {
        gridPanel.removeAll();

        if (!(currentUser instanceof Customer)) {
            JLabel lblMsg = new JLabel("Vui lòng đăng nhập để xem danh sách yêu thích!");
            lblMsg.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(lblMsg, BorderLayout.CENTER);
            return;
        }

        int customerId = ((Customer) currentUser).getCustomerId();
        List<Product> products = productBUS.getWishlistProducts(customerId);

        if (products == null || products.isEmpty()) {
            JLabel lblEmpty = new JLabel("Danh sách yêu thích trống.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            lblEmpty.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(lblEmpty, BorderLayout.CENTER);
        } else {
            gridPanel.setLayout(new GridLayout(0, 4, 25, 25));
            gridPanel.setLayout(new GridLayout(0, 4, 25, 25));
            for (Product p : products) {
                gridPanel.add(createProductCard(p));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 20));
                g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 10, 10);
            }
        };
        card.setOpaque(true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(new LineBorder(borderColor, 1), new EmptyBorder(10, 10, 10, 10))));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProductDetail(p);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 235, 225));
                card.setBackground(new Color(240, 235, 225));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(cardColor);
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.setMaximumSize(new Dimension(200, 30));

        JLabel lblHeart = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dangerRed);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 8;
                int x = (w - size) / 2;
                int y = (h - size) / 2 + 2;

                g2.fillArc(x, y, size / 2, size / 2, 0, 180);
                g2.fillArc(x + size / 2, y, size / 2, size / 2, 0, 180);
                int[] px = { x, x + size / 2, x + size };
                int[] py = { y + size / 4 + 1, y + size, y + size / 4 + 1 };
                g2.fillPolygon(px, py, 3);
            }
        };
        lblHeart.setPreferredSize(new Dimension(30, 30));
        lblHeart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHeart.setToolTipText("Bỏ thích (Xóa khỏi danh sách)");

        lblHeart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
                if (currentUser instanceof Customer) {
                    int cusId = ((Customer) currentUser).getCustomerId();
                    wishlistDAO.deleteByCustomerAndProduct(cusId, p.getId());
                    loadWishlist();
                }
            }
        });
        topPanel.add(lblHeart);

        JLabel lblImg = new JLabel();
        lblImg.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImg.setPreferredSize(new Dimension(180, 140));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icon = ImageHelper.loadResizedIcon(p.getImage(), 180, 140);
        if (icon != null)
            lblImg.setIcon(icon);
        else
            lblImg.setText("No Image");

        JLabel lblName = new JLabel("<html><center>" + p.getName() + "</center></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setForeground(textPrimary);

        JLabel lblPrice = new JLabel(df.format(p.getPrice()) + " ₫");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPrice.setForeground(dangerRed);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(topPanel);
        card.add(lblImg);
        card.add(Box.createVerticalStrut(15));
        card.add(lblName);
        card.add(Box.createVerticalStrut(5));
        card.add(lblPrice);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private void showProductDetail(Product p) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), p.getName(), true);
        dialog.setSize(1100, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setBackground(Color.WHITE);
        JLabel lblMainImg = new JLabel();
        lblMainImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblMainImg.setPreferredSize(new Dimension(500, 400));
        lblMainImg.setBorder(BorderFactory.createLineBorder(borderColor));
        ImageIcon mainIcon = ImageHelper.loadResizedIcon(p.getImage(), 500, 400);
        if (mainIcon != null)
            lblMainImg.setIcon(mainIcon);
        else
            lblMainImg.setText("Đang cập nhật ảnh");

        JPanel pnlThumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThumb.setBackground(Color.WHITE);
        List<String> images = productBUS.getProductImages(p.getId());
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            if (p.getImage() != null)
                images.add(p.getImage());
        }
        for (String imgPath : images) {
            JLabel lblThumb = new JLabel();
            lblThumb.setPreferredSize(new Dimension(80, 80));
            lblThumb.setBorder(BorderFactory.createLineBorder(borderColor));
            lblThumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            ImageIcon thumbIcon = ImageHelper.loadResizedIcon(imgPath, 80, 80);
            if (thumbIcon != null)
                lblThumb.setIcon(thumbIcon);
            lblThumb.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    lblThumb.setBorder(BorderFactory.createLineBorder(dangerRed));
                    ImageIcon large = ImageHelper.loadResizedIcon(imgPath, 500, 400);
                    if (large != null)
                        lblMainImg.setIcon(large);
                }

                public void mouseExited(MouseEvent e) {
                    lblThumb.setBorder(BorderFactory.createLineBorder(borderColor));
                }
            });
            pnlThumb.add(lblThumb);
        }
        leftPanel.add(lblMainImg, BorderLayout.CENTER);
        leftPanel.add(new JScrollPane(pnlThumb, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel lblName = new JLabel("<html>" + p.getName() + "</html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblName.setForeground(textPrimary);
        rightPanel.add(lblName);
        rightPanel.add(Box.createVerticalStrut(10));

        JLabel lblPrice = new JLabel(df.format(p.getPrice()) + " ₫");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblPrice.setForeground(dangerRed);
        rightPanel.add(lblPrice);
        rightPanel.add(Box.createVerticalStrut(20));

        String specsHtml = "<html><ul style='margin-left: 10px; padding: 0px; list-style-type: square;'>"
                + "<li><b>Hãng:</b> " + (p.getMakerName() != null ? p.getMakerName() : "N/A") + "</li>"
                + "<li><b>Profile:</b> " + (p.getProfile() != null ? p.getProfile() : "N/A") + "</li>"
                + "<li><b>Chất liệu:</b> " + (p.getMaterial() != null ? p.getMaterial() : "N/A") + "</li>"
                + "</ul></html>";
        JLabel lblSpecs = new JLabel(specsHtml);
        lblSpecs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rightPanel.add(lblSpecs);
        rightPanel.add(Box.createVerticalStrut(20));

        JPanel pnlQty = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlQty.setBackground(Color.WHITE);
        pnlQty.add(new JLabel("Số lượng: "));
        JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, p.getStock()), 1));
        spnQty.setPreferredSize(new Dimension(70, 35));
        pnlQty.add(spnQty);
        rightPanel.add(pnlQty);
        rightPanel.add(Box.createVerticalStrut(25));

        JPanel pnlActions = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlActions.setBackground(Color.WHITE);
        pnlActions.setMaximumSize(new Dimension(600, 50));

        JButton btnAddToCart = new JButton("THÊM VÀO GIỎ");
        btnAddToCart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddToCart.setForeground(primaryColor);
        btnAddToCart.setBackground(Color.WHITE);
        btnAddToCart.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnBuyNow = new JButton("MUA NGAY");
        btnBuyNow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuyNow.setForeground(Color.WHITE);
        btnBuyNow.setBackground(dangerRed);
        btnBuyNow.setBorderPainted(false);
        btnBuyNow.setFocusPainted(false);
        btnBuyNow.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlActions.add(btnAddToCart);
        pnlActions.add(btnBuyNow);
        rightPanel.add(pnlActions);

        if (p.getStock() <= 0) {
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
            btnAddToCart.setText("HẾT HÀNG");
            btnBuyNow.setText("HẾT HÀNG");
        }

        topPanel.add(leftPanel);
        topPanel.add(rightPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        JEditorPane txtDesc = new JEditorPane();
        txtDesc.setContentType("text/html");
        txtDesc.setText("<html><body style='font-family: Segoe UI; padding: 20px; font-size: 14px;'>"
                + (p.getDescription() != null ? p.getDescription() : "Đang cập nhật mô tả...")
                + "</body></html>");
        txtDesc.setEditable(false);
        tabbedPane.addTab("Mô tả chi tiết", new JScrollPane(txtDesc));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(mainPanel);

        btnAddToCart.addActionListener(e -> {
            int qty = (int) spnQty.getValue();
            CartPanel.addToCart(p, qty);
            JOptionPane.showMessageDialog(dialog, "Đã thêm " + qty + " sản phẩm vào giỏ!");
        });

        btnBuyNow.addActionListener(e -> {
            int qty = (int) spnQty.getValue();
            CartPanel.addToCart(p, qty);
            dialog.dispose();
            if (onNavigate != null)
                onNavigate.accept("CART");
        });

        dialog.setVisible(true);
    }

    @Override
    public void refresh() {
        loadWishlist();
    }
}
