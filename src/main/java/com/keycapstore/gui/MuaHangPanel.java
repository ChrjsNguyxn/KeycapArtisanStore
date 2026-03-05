package com.keycapstore.gui;

import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MuaHangPanel extends JPanel {
    
    private JPanel headerPanel, bannerPanel, categoryPanel, productsPanel;
    private JTextField searchField;
    private JLabel cartCountLabel;
    private JButton loginBtn;
    private int cartCount = 0;
    private DecimalFormat df = new DecimalFormat("#,###");
    private Object currentUser;
    private boolean isGuest = false;
    
    // MÀU SẮC THEO BẢNG CHUẨN
    private Color primaryDark = new Color(62, 54, 46);      // #3E362E - Màu chủ đạo
    private Color creamLight = new Color(228, 220, 207);    // #E4DCCF - Nền sáng
    private Color taupeGrey = new Color(153, 143, 133);     // #998F85 - Border
    private Color glassWhite = new Color(255, 252, 245);    // #FFFCF5 - Card sản phẩm
    private Color successGreen = new Color(46, 204, 113);   // #2ECC71 - Nút Thêm
    private Color infoBlue = new Color(52, 152, 219);       // #3498DB - Nút Xem
    private Color dangerRed = new Color(231, 76, 60);       // #E74C3C - Giá, Flash Sale
    private Color textPrimary = new Color(51, 51, 51);      // #333333 - Chữ nội dung
    
    // Font chữ
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
    private Font normalFont = new Font("Segoe UI", Font.PLAIN, 12);
    private Font smallFont = new Font("Segoe UI", Font.PLAIN, 10);
    private Font priceFont = new Font("Segoe UI", Font.BOLD, 14);
    private Font logoFont = new Font("Segoe UI", Font.BOLD, 22);
    
    // Danh sách sản phẩm mẫu
    private List<KeycapProduct> productList;
    
    // Class sản phẩm nội bộ
    class KeycapProduct {
        String id;
        String name;
        String brand;
        int price;
        int originalPrice;
        int discountPercent;
        String profile;
        String material;
        int soldCount;
        float rating;
        
        KeycapProduct(String id, String name, String brand, int price, int originalPrice, 
                     String profile, String material, int soldCount, float rating) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.price = price;
            this.originalPrice = originalPrice;
            this.discountPercent = originalPrice > 0 ? (int)((1 - (double)price/originalPrice) * 100) : 0;
            this.profile = profile;
            this.material = material;
            this.soldCount = soldCount;
            this.rating = rating;
        }
    }
    
    // Constructor nhận tham số user
    public MuaHangPanel(Object user) {
        this.currentUser = user;
        checkUserType();
        initData();
        initUI();
    }
    
    // Constructor mặc định (cho trường hợp không có user)
    public MuaHangPanel() {
        this(null);
    }
    
    private void checkUserType() {
        if (currentUser instanceof Customer) {
            Customer customer = (Customer) currentUser;
            // Kiểm tra nếu là Guest (username bắt đầu bằng "guest_")
            if (customer.getUsername() != null && customer.getUsername().startsWith("guest_")) {
                isGuest = true;
            } else {
                isGuest = false; // Đã đăng nhập bằng tài khoản thật
            }
        } else if (currentUser instanceof Employee) {
            isGuest = false; // Nhân viên đăng nhập
        } else {
            isGuest = true; // Không có user -> Guest
        }
    }
    
    private void initData() {
        productList = new ArrayList<>();
        
        // Thêm dữ liệu sản phẩm mẫu
        productList.add(new KeycapProduct("KC001", "GMK Olivia++ Dark", "GMK", 2890000, 3500000, "Cherry", "ABS", 15000, 4.9f));
        productList.add(new KeycapProduct("KC002", "PBT Hanami Dango", "Tai-Hao", 650000, 890000, "OEM", "PBT", 8500, 4.8f));
        productList.add(new KeycapProduct("KC003", "ePBT Kuro Shiro", "ePBT", 990000, 1290000, "Cherry", "PBT", 6200, 4.7f));
        productList.add(new KeycapProduct("KC004", "DOMIKEY SA", "DOMIKEY", 1450000, 1890000, "SA", "ABS", 4300, 4.6f));
        productList.add(new KeycapProduct("KC005", "NP PBT Crayon", "NP", 599000, 750000, "XDA", "PBT", 12100, 4.8f));
        productList.add(new KeycapProduct("KC006", "JTK Night Sakura", "JTK", 1690000, 2100000, "Cherry", "ABS", 2800, 4.9f));
        productList.add(new KeycapProduct("KC007", "Akko Neon", "Akko", 499000, 650000, "ASA", "PBT", 23500, 4.7f));
        productList.add(new KeycapProduct("KC008", "Maxkey SA", "Maxkey", 1290000, 1650000, "SA", "ABS", 5700, 4.5f));
        productList.add(new KeycapProduct("KC009", "Keychron OEM", "Keychron", 450000, 590000, "OEM", "PBT", 18200, 4.6f));
        productList.add(new KeycapProduct("KC010", "Ducky Joker", "Ducky", 890000, 1090000, "OEM", "PBT", 9400, 4.8f));
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(creamLight);
        
        // Header
        createHeader();
        
        // Main content với scroll
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(creamLight);
        
        // Banner
        mainContent.add(createBanner());
        
        // Danh mục (gộp title và button)
        mainContent.add(createCategorySection());
        
        // Flash Sale
        mainContent.add(createFlashSaleSection());
        
        // Sản phẩm nổi bật
        mainContent.add(createFeaturedTitle("SẢN PHẨM NỔI BẬT"));
        mainContent.add(createProductGrid(productList.subList(0, 4)));
        
        // Sản phẩm mới
        mainContent.add(createFeaturedTitle("SẢN PHẨM MỚI"));
        mainContent.add(createProductGrid(productList.subList(4, 8)));
        
        // Gợi ý cho bạn
        mainContent.add(createFeaturedTitle("GỢI Ý CHO BẠN"));
        mainContent.add(createProductGrid(productList.subList(2, 6)));
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(creamLight);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createHeader() {
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(primaryDark);
        
        // Top header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(primaryDark);
        topHeader.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel logoLabel = new JLabel("KEYCAP STORE");
        logoLabel.setFont(logoFont);
        logoLabel.setForeground(glassWhite);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(primaryDark);
        
        // Nút giỏ hàng
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        cartPanel.setBackground(primaryDark);
        
        JButton cartBtn = new JButton("Giỏ hàng");
        cartBtn.setFont(normalFont);
        cartBtn.setForeground(glassWhite);
        cartBtn.setBackground(primaryDark);
        cartBtn.setBorderPainted(false);
        cartBtn.setFocusPainted(false);
        cartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cartCountLabel = new JLabel("0");
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountLabel.setForeground(glassWhite);
        cartCountLabel.setBackground(dangerRed);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        
        cartPanel.add(cartBtn);
        cartPanel.add(cartCountLabel);
        
        rightPanel.add(cartPanel);
        
        // Chỉ hiển thị nút Đăng nhập nếu là Guest
        if (isGuest) {
            loginBtn = new JButton("Đăng nhập");
            loginBtn.setFont(normalFont);
            loginBtn.setForeground(primaryDark);
            loginBtn.setBackground(glassWhite);
            loginBtn.setBorderPainted(false);
            loginBtn.setFocusPainted(false);
            loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Thêm sự kiện cho nút đăng nhập
            loginBtn.addActionListener(e -> {
                // Mở dialog đăng nhập
                ModernLoginDialog loginDialog = new ModernLoginDialog();
                loginDialog.setVisible(true);
                // Sau khi đóng dialog, có thể cần refresh panel nếu đăng nhập thành công
            });
            
            rightPanel.add(loginBtn);
        } else {
            // Nếu đã đăng nhập, hiển thị tên người dùng
            String userName = "Khách";
            if (currentUser instanceof Customer) {
                userName = ((Customer) currentUser).getFullName();
            } else if (currentUser instanceof Employee) {
                userName = ((Employee) currentUser).getFullName();
            }
            
            JLabel userLabel = new JLabel("Xin chào, " + userName);
            userLabel.setFont(normalFont);
            userLabel.setForeground(glassWhite);
            rightPanel.add(userLabel);
        }
        
        topHeader.add(logoLabel, BorderLayout.WEST);
        topHeader.add(rightPanel, BorderLayout.EAST);
        
        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(primaryDark);
        searchPanel.setBorder(new EmptyBorder(5, 15, 15, 15));
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 40));
        searchField.setFont(normalFont);
        searchField.setForeground(textPrimary);
        searchField.setBackground(glassWhite);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(taupeGrey, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        JButton searchBtn = new JButton("Tìm kiếm");
        searchBtn.setFont(normalFont);
        searchBtn.setBackground(infoBlue);
        searchBtn.setForeground(glassWhite);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.setPreferredSize(new Dimension(100, 40));
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        
        headerPanel.add(topHeader);
        headerPanel.add(searchPanel);
    }
    
    private JPanel createBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(primaryDark);
        banner.setPreferredSize(new Dimension(0, 150));
        banner.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html><center>FLASH SALE CUỐI TUẦN<br/>GIẢM GIÁ LÊN ĐẾN 50%</center></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(successGreen);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subLabel = new JLabel("Chỉ từ 299.000đ");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(glassWhite);
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.CENTER);
        textPanel.add(subLabel, BorderLayout.SOUTH);
        
        banner.add(textPanel, BorderLayout.CENTER);
        
        return banner;
    }
    
    private JPanel createCategorySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(creamLight);
        section.setBorder(new EmptyBorder(20, 15, 10, 15));
        
        // Panel chứa button danh mục chính (đã gộp title)
        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        categoryButtonPanel.setBackground(creamLight);
        categoryButtonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // Button danh mục chính (đã gộp title)
        JButton btnCategory = new JButton("📋 DANH MỤC");
        btnCategory.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCategory.setBackground(primaryDark);
        btnCategory.setForeground(glassWhite);
        btnCategory.setFocusPainted(false);
        btnCategory.setBorderPainted(false);
        btnCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCategory.setPreferredSize(new Dimension(200, 45));
        btnCategory.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Panel chứa các danh mục con (ẩn ban đầu)
        JPanel subCategoryPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        subCategoryPanel.setBackground(creamLight);
        subCategoryPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        subCategoryPanel.setVisible(false); // Ẩn ban đầu
        
        String[] categories = {
            "Cherry MX", "GMK", "PBT", "ABS",
            "OEM", "SA", "XDA", "Custom"
        };
        
        for (String cat : categories) {
            JPanel catItem = new JPanel();
            catItem.setLayout(new BoxLayout(catItem, BoxLayout.Y_AXIS));
            catItem.setBackground(glassWhite);
            catItem.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(taupeGrey, 1),
                new EmptyBorder(10, 5, 10, 5)
            ));
            catItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            JLabel nameLabel = new JLabel(cat);
            nameLabel.setFont(normalFont);
            nameLabel.setForeground(textPrimary);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            catItem.add(nameLabel);
            
            // Thêm sự kiện click cho từng danh mục con
            catItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(MuaHangPanel.this, 
                        "Bạn đã chọn danh mục: " + cat, 
                        "Thông báo", 
                        JOptionPane.INFORMATION_MESSAGE);
                    // Sau này có thể lọc sản phẩm theo danh mục
                    btnCategory.setText("📋 " + cat);
                    subCategoryPanel.setVisible(false);
                }
            });
            
            subCategoryPanel.add(catItem);
        }
        
        // Xử lý sự kiện click cho button danh mục chính
        btnCategory.addActionListener(e -> {
            subCategoryPanel.setVisible(!subCategoryPanel.isVisible());
            revalidate();
            repaint();
        });
        
        categoryButtonPanel.add(btnCategory);
        
        section.add(categoryButtonPanel, BorderLayout.NORTH);
        section.add(subCategoryPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createFlashSaleSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(creamLight);
        section.setBorder(new EmptyBorder(20, 15, 10, 15));
        
        // Title với timer
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(creamLight);
        
        JLabel title = new JLabel("FLASH SALE");
        title.setFont(titleFont);
        title.setForeground(dangerRed);
        
        JLabel timer = new JLabel("23:59:45");
        timer.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timer.setForeground(dangerRed);
        
        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(timer, BorderLayout.EAST);
        
        // Sản phẩm flash sale
        JPanel productRow = new JPanel(new GridLayout(1, 3, 10, 0));
        productRow.setBackground(creamLight);
        productRow.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        List<KeycapProduct> flashSaleItems = productList.subList(1, 4);
        for (KeycapProduct p : flashSaleItems) {
            productRow.add(createFlashSaleItem(p));
        }
        
        section.add(titlePanel, BorderLayout.NORTH);
        section.add(productRow, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createFlashSaleItem(KeycapProduct p) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(glassWhite);
        item.setBorder(new LineBorder(taupeGrey, 1));
        
        JLabel imageLabel = new JLabel("Hình ảnh");
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        imageLabel.setForeground(primaryDark);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(80, 80));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel nameLabel = new JLabel(p.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(textPrimary);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel brandLabel = new JLabel(p.brand);
        brandLabel.setFont(smallFont);
        brandLabel.setForeground(taupeGrey);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pricePanel.setBackground(glassWhite);
        
        String originalPriceStr = df.format(p.originalPrice) + "₫";
        String priceStr = df.format(p.price) + "₫";
        
        JLabel originalPrice = new JLabel(originalPriceStr);
        originalPrice.setFont(smallFont);
        originalPrice.setForeground(taupeGrey);
        
        JLabel discountPrice = new JLabel(priceStr);
        discountPrice.setFont(priceFont);
        discountPrice.setForeground(dangerRed);
        
        pricePanel.add(originalPrice);
        pricePanel.add(discountPrice);
        
        JLabel discount = new JLabel("-" + p.discountPercent + "%");
        discount.setFont(new Font("Segoe UI", Font.BOLD, 10));
        discount.setForeground(glassWhite);
        discount.setBackground(dangerRed);
        discount.setOpaque(true);
        discount.setBorder(new EmptyBorder(2, 5, 2, 5));
        discount.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel soldLabel = new JLabel("Đã bán " + p.soldCount + "+");
        soldLabel.setFont(smallFont);
        soldLabel.setForeground(taupeGrey);
        soldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton addBtn = new JButton("Thêm vào giỏ");
        addBtn.setFont(smallFont);
        addBtn.setBackground(successGreen);
        addBtn.setForeground(glassWhite);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            cartCount++;
            cartCountLabel.setText(String.valueOf(cartCount));
            JOptionPane.showMessageDialog(this, "Đã thêm " + p.name + " vào giỏ hàng!");
        });
        
        item.add(Box.createVerticalStrut(10));
        item.add(imageLabel);
        item.add(Box.createVerticalStrut(5));
        item.add(nameLabel);
        item.add(brandLabel);
        item.add(Box.createVerticalStrut(5));
        item.add(pricePanel);
        item.add(Box.createVerticalStrut(3));
        item.add(discount);
        item.add(Box.createVerticalStrut(3));
        item.add(soldLabel);
        item.add(Box.createVerticalStrut(5));
        item.add(addBtn);
        item.add(Box.createVerticalStrut(10));
        
        return item;
    }
    
    private JPanel createFeaturedTitle(String titleText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(creamLight);
        panel.setBorder(new EmptyBorder(20, 15, 10, 15));
        
        JLabel title = new JLabel(titleText);
        title.setFont(titleFont);
        title.setForeground(primaryDark);
        
        JLabel viewAll = new JLabel("Xem tất cả →");
        viewAll.setFont(normalFont);
        viewAll.setForeground(infoBlue);
        viewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(title, BorderLayout.WEST);
        panel.add(viewAll, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createProductGrid(List<KeycapProduct> products) {
        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setBackground(creamLight);
        grid.setBorder(new EmptyBorder(0, 15, 20, 15));
        
        for (KeycapProduct p : products) {
            grid.add(createProductCard(p));
        }
        
        return grid;
    }
    
    private JPanel createProductCard(KeycapProduct p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(glassWhite);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(taupeGrey, 1),
            new EmptyBorder(12, 10, 12, 10)
        ));
        
        JLabel imageLabel = new JLabel("Hình ảnh");
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        imageLabel.setForeground(primaryDark);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(100, 80));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel brandLabel = new JLabel(p.brand);
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brandLabel.setForeground(primaryDark);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(p.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(textPrimary);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel specsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        specsPanel.setBackground(glassWhite);
        
        JLabel profileLabel = new JLabel(p.profile);
        profileLabel.setFont(smallFont);
        profileLabel.setForeground(taupeGrey);
        
        JLabel materialLabel = new JLabel(p.material);
        materialLabel.setFont(smallFont);
        materialLabel.setForeground(taupeGrey);
        
        specsPanel.add(profileLabel);
        specsPanel.add(new JLabel("•"));
        specsPanel.add(materialLabel);
        
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        ratingPanel.setBackground(glassWhite);
        
        JLabel ratingLabel = new JLabel("Đánh giá: " + p.rating + "/5");
        ratingLabel.setFont(smallFont);
        ratingLabel.setForeground(textPrimary);
        ratingPanel.add(ratingLabel);
        
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pricePanel.setBackground(glassWhite);
        
        String priceStr = df.format(p.price) + "₫";
        
        if (p.originalPrice > p.price) {
            String originalPriceStr = df.format(p.originalPrice) + "₫";
            JLabel originalPrice = new JLabel(originalPriceStr);
            originalPrice.setFont(smallFont);
            originalPrice.setForeground(taupeGrey);
            pricePanel.add(originalPrice);
        }
        
        JLabel priceLabel = new JLabel(priceStr);
        priceLabel.setFont(priceFont);
        priceLabel.setForeground(dangerRed);
        pricePanel.add(priceLabel);
        
        if (p.discountPercent > 0) {
            JLabel discountLabel = new JLabel("-" + p.discountPercent + "%");
            discountLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            discountLabel.setForeground(glassWhite);
            discountLabel.setBackground(dangerRed);
            discountLabel.setOpaque(true);
            discountLabel.setBorder(new EmptyBorder(2, 4, 2, 4));
            pricePanel.add(discountLabel);
        }
        
        JLabel soldLabel = new JLabel("Đã bán " + p.soldCount + "+");
        soldLabel.setFont(smallFont);
        soldLabel.setForeground(taupeGrey);
        soldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton addBtn = new JButton("Thêm vào giỏ");
        addBtn.setFont(smallFont);
        addBtn.setBackground(successGreen);
        addBtn.setForeground(glassWhite);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            cartCount++;
            cartCountLabel.setText(String.valueOf(cartCount));
            JOptionPane.showMessageDialog(this, "Đã thêm " + p.name + " vào giỏ hàng!");
        });
        
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(brandLabel);
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(specsPanel);
        card.add(ratingPanel);
        card.add(Box.createVerticalStrut(5));
        card.add(pricePanel);
        card.add(soldLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(addBtn);
        
        return card;
    }
}