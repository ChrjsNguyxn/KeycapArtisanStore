package com.keycapstore.gui;

import com.keycapstore.bus.CategoryBUS;
import com.keycapstore.bus.ProductBUS;
import com.keycapstore.model.Category;
import com.keycapstore.model.Product;
import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.stream.Collectors;

public class MuaHangPanel extends JPanel {
    
    private JPanel headerPanel, filterPanel, productsPanel, paginationPanel;
    private JTextField searchField;
    private JLabel cartCountLabel;
    private JButton loginBtn, cartBtn, btnApplyFilter;
    private JComboBox<String> cbCategory, cbPriceRange, cbOrigin;
    private int cartCount = 0;
    private DecimalFormat df = new DecimalFormat("#,###");
    private Object currentUser;
    private boolean isGuest = false;
    private List<GioHangPanel.CartItem> cartItems;
    private ProductBUS productBus;
    private CategoryBUS categoryBus;
    
    // Màu sắc
    private Color primaryDark = new Color(62, 54, 46);
    private Color creamLight = new Color(228, 220, 207);
    private Color taupeGrey = new Color(153, 143, 133);
    private Color glassWhite = new Color(255, 252, 245);
    private Color successGreen = new Color(46, 204, 113);
    private Color infoBlue = new Color(52, 152, 219);
    private Color dangerRed = new Color(231, 76, 60);
    private Color textPrimary = new Color(51, 51, 51);
    private Color orangeColor = new Color(255, 159, 67);
    
    // Font chữ
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
    private Font normalFont = new Font("Segoe UI", Font.PLAIN, 12);
    private Font smallFont = new Font("Segoe UI", Font.PLAIN, 11);
    private Font priceFont = new Font("Segoe UI", Font.BOLD, 15);
    private Font logoFont = new Font("Segoe UI", Font.BOLD, 20);
    
    // Danh sách sản phẩm và phân trang
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private List<Product> currentPageList;
    private int currentPage = 1;
    private int itemsPerPage = 6;
    private int totalPages;
    private JLabel pageInfoLabel;
    private JButton btnPrev, btnNext, btnFirst, btnLast;
    
    public MuaHangPanel(Object user) {
        this.currentUser = user;
        this.cartItems = new ArrayList<>();
        this.productBus = new ProductBUS();
        this.categoryBus = new CategoryBUS();
        checkUserType();
        loadDataFromDatabase();
        initUI();
    }
    
    public MuaHangPanel() {
        this(null);
    }
    
    private void checkUserType() {
        if (currentUser instanceof Customer) {
            Customer customer = (Customer) currentUser;
            if (customer.getUsername() != null && customer.getUsername().startsWith("guest_")) {
                isGuest = true;
            } else {
                isGuest = false;
            }
        } else if (currentUser instanceof Employee) {
            isGuest = false;
        } else {
            isGuest = true;
        }
    }
    
    private void loadDataFromDatabase() {
        allProducts = productBus.getAvailableProducts();
        filteredProducts = new ArrayList<>(allProducts);
        
        System.out.println("Đã load " + allProducts.size() + " sản phẩm từ database");
        
        // Tính tổng số trang
        updatePagination();
    }
    
    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        if (totalPages < 1) totalPages = 1;
        currentPage = 1;
        updateCurrentPage();
    }
    
    private void updateCurrentPage() {
        if (filteredProducts.isEmpty()) {
            currentPageList = new ArrayList<>();
            return;
        }
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredProducts.size());
        currentPageList = filteredProducts.subList(start, end);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(creamLight);
        
        // Header
        createHeader();
        
        // Filter Panel
        createFilterPanel();
        
        // Main content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(creamLight);
        
        // Products panel
        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBackground(creamLight);
        productsPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        // Title
        updateTitle();
        
        // Product grid
        updateProductGrid();
        
        // Pagination
        createPagination();
        
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(creamLight);
        
        mainContent.add(filterPanel, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(paginationPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }
    
    private void createFilterPanel() {
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterPanel.setBackground(creamLight);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 15, 5, 15),
            BorderFactory.createTitledBorder(
                new LineBorder(taupeGrey, 1),
                "BỘ LỌC",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                primaryDark
            )
        ));
        
        // Lọc theo danh mục
        filterPanel.add(new JLabel("Danh mục:"));
        cbCategory = new JComboBox<>();
        cbCategory.addItem("Tất cả");
        
        ArrayList<Category> categories = categoryBus.getAllCategories();
        for (Category cat : categories) {
            cbCategory.addItem(cat.getName());
        }
        cbCategory.setPreferredSize(new Dimension(120, 25));
        cbCategory.setBackground(glassWhite);
        filterPanel.add(cbCategory);
        
        // Lọc theo khoảng giá
        filterPanel.add(new JLabel("Khoảng giá:"));
        String[] priceRanges = {
            "Tất cả",
            "< 500k",
            "500k - 1tr",
            "1tr - 2tr",
            "> 2tr"
        };
        cbPriceRange = new JComboBox<>(priceRanges);
        cbPriceRange.setPreferredSize(new Dimension(90, 25));
        cbPriceRange.setBackground(glassWhite);
        filterPanel.add(cbPriceRange);
        
        // Lọc theo xuất xứ - Thêm Nhật Bản, USA, Trung Quốc
        filterPanel.add(new JLabel("Xuất xứ:"));
        cbOrigin = new JComboBox<>();
        cbOrigin.addItem("Tất cả");
        cbOrigin.addItem("Việt Nam");
        cbOrigin.addItem("Nhật Bản");
        cbOrigin.addItem("USA");
        cbOrigin.addItem("Trung Quốc");
        cbOrigin.addItem("Hàn Quốc");
        
        // Lấy các xuất xứ từ database và thêm vào nếu chưa có
        List<String> dbOrigins = allProducts.stream()
            .map(p -> p.getProfile())
            .filter(o -> o != null && !o.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        for (String origin : dbOrigins) {
            boolean exists = false;
            for (int i = 0; i < cbOrigin.getItemCount(); i++) {
                if (cbOrigin.getItemAt(i).equals(origin)) {
                    exists = true;
                    break;
                }
            }
            if (!exists && !origin.equals("Tất cả")) {
                cbOrigin.addItem(origin);
            }
        }
        
        cbOrigin.setPreferredSize(new Dimension(100, 25));
        cbOrigin.setBackground(glassWhite);
        filterPanel.add(cbOrigin);
        
        // Nút áp dụng
        btnApplyFilter = new JButton("Lọc");
        btnApplyFilter.setBackground(infoBlue);
        btnApplyFilter.setForeground(glassWhite);
        btnApplyFilter.setFont(smallFont);
        btnApplyFilter.setBorderPainted(false);
        btnApplyFilter.setFocusPainted(false);
        btnApplyFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApplyFilter.addActionListener(e -> applyFilters());
        filterPanel.add(btnApplyFilter);
        
        // Nút xóa lọc
        JButton btnClearFilter = new JButton("Xóa");
        btnClearFilter.setBackground(taupeGrey);
        btnClearFilter.setForeground(glassWhite);
        btnClearFilter.setFont(smallFont);
        btnClearFilter.setBorderPainted(false);
        btnClearFilter.setFocusPainted(false);
        btnClearFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearFilter.addActionListener(e -> clearFilters());
        filterPanel.add(btnClearFilter);
    }
    
    private void applyFilters() {
        String selectedCategory = (String) cbCategory.getSelectedItem();
        String selectedPriceRange = (String) cbPriceRange.getSelectedItem();
        String selectedOrigin = (String) cbOrigin.getSelectedItem();
        
        filteredProducts = new ArrayList<>(allProducts);
        
        // Lọc theo danh mục
        if (selectedCategory != null && !selectedCategory.equals("Tất cả")) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> selectedCategory.equals(p.getCategoryName()))
                .collect(Collectors.toList());
        }
        
        // Lọc theo khoảng giá
        if (selectedPriceRange != null && !selectedPriceRange.equals("Tất cả")) {
            switch (selectedPriceRange) {
                case "< 500k":
                    filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() < 500000)
                        .collect(Collectors.toList());
                    break;
                case "500k - 1tr":
                    filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() >= 500000 && p.getPrice() <= 1000000)
                        .collect(Collectors.toList());
                    break;
                case "1tr - 2tr":
                    filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() >= 1000000 && p.getPrice() <= 2000000)
                        .collect(Collectors.toList());
                    break;
                case "> 2tr":
                    filteredProducts = filteredProducts.stream()
                        .filter(p -> p.getPrice() > 2000000)
                        .collect(Collectors.toList());
                    break;
            }
        }
        
        // Lọc theo xuất xứ
        if (selectedOrigin != null && !selectedOrigin.equals("Tất cả")) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> selectedOrigin.equals(p.getProfile()))
                .collect(Collectors.toList());
        }
        
        updatePagination();
        updateTitle();
        updateProductGrid();
        pageInfoLabel.setText("Trang " + currentPage + "/" + totalPages);
        updatePaginationButtons();
    }
    
    private void clearFilters() {
        cbCategory.setSelectedIndex(0);
        cbPriceRange.setSelectedIndex(0);
        cbOrigin.setSelectedIndex(0);
        
        filteredProducts = new ArrayList<>(allProducts);
        updatePagination();
        updateTitle();
        updateProductGrid();
        pageInfoLabel.setText("Trang " + currentPage + "/" + totalPages);
        updatePaginationButtons();
    }
    
    private void updateTitle() {
        // Xóa title cũ nếu có
        Component[] components = productsPanel.getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            productsPanel.remove(0);
        }
        
        // Thêm title mới
        JLabel titleLabel = new JLabel("TẤT CẢ SẢN PHẨM (" + filteredProducts.size() + " sản phẩm)", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(primaryDark);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(5, 0, 15, 0));
        productsPanel.add(titleLabel, 0);
    }
    
    private void updateProductGrid() {
        // Xóa grid cũ (giữ lại title)
        while (productsPanel.getComponentCount() > 1) {
            productsPanel.remove(1);
        }
        
        if (currentPageList.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có sản phẩm phù hợp", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            emptyLabel.setForeground(taupeGrey);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            productsPanel.add(emptyLabel);
        } else {
            // Tạo panel chứa grid với FlowLayout để căn giữa
            JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            centerPanel.setBackground(creamLight);
            
            // Tạo grid với 3 cột
            JPanel grid = new JPanel(new GridLayout(0, 3, 20, 20));
            grid.setBackground(creamLight);
            
            // Đặt kích thước cố định cho grid
            int cardWidth = 250;
            int cardHeight = 300;
            int gap = 20;
            int gridWidth = 3 * cardWidth + 2 * gap;
            grid.setPreferredSize(new Dimension(gridWidth, calculateGridHeight()));
            grid.setMaximumSize(new Dimension(gridWidth, calculateGridHeight()));
            grid.setMinimumSize(new Dimension(gridWidth, calculateGridHeight()));
            
            // Thêm sản phẩm
            for (Product p : currentPageList) {
                grid.add(createProductCard(p));
            }
            
            // Thêm panel trống để giữ layout 3 cột
            int itemsPerRow = 3;
            int totalItems = currentPageList.size();
            int remainder = totalItems % itemsPerRow;
            
            if (remainder != 0) {
                int emptySlots = itemsPerRow - remainder;
                for (int i = 0; i < emptySlots; i++) {
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setBackground(creamLight);
                    emptyPanel.setPreferredSize(new Dimension(cardWidth, cardHeight));
                    emptyPanel.setVisible(false);
                    grid.add(emptyPanel);
                }
            }
            
            centerPanel.add(grid);
            productsPanel.add(centerPanel);
        }
        
        // Thêm glue để đẩy lên trên
        productsPanel.add(Box.createVerticalGlue());
        
        productsPanel.revalidate();
        productsPanel.repaint();
    }
    
    private int calculateGridHeight() {
        if (currentPageList.isEmpty()) return 0;
        
        int cardHeight = 300;
        int gap = 20;
        int rows = (int) Math.ceil((double) currentPageList.size() / 3);
        
        return rows * cardHeight + (rows - 1) * gap;
    }
    
    private void createPagination() {
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        paginationPanel.setBackground(creamLight);
        
        btnFirst = new JButton("<<");
        btnPrev = new JButton("<");
        pageInfoLabel = new JLabel("Trang " + currentPage + "/" + totalPages);
        btnNext = new JButton(">");
        btnLast = new JButton(">>");
        
        Font paginationFont = new Font("Segoe UI", Font.BOLD, 12);
        Dimension buttonSize = new Dimension(45, 30);
        
        for (JButton btn : new JButton[]{btnFirst, btnPrev, btnNext, btnLast}) {
            btn.setFont(paginationFont);
            btn.setPreferredSize(buttonSize);
            btn.setBackground(primaryDark);
            btn.setForeground(glassWhite);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        pageInfoLabel.setFont(normalFont);
        pageInfoLabel.setForeground(textPrimary);
        pageInfoLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        btnFirst.addActionListener(e -> goToPage(1));
        btnPrev.addActionListener(e -> goToPage(currentPage - 1));
        btnNext.addActionListener(e -> goToPage(currentPage + 1));
        btnLast.addActionListener(e -> goToPage(totalPages));
        
        paginationPanel.add(btnFirst);
        paginationPanel.add(btnPrev);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(btnNext);
        paginationPanel.add(btnLast);
        
        updatePaginationButtons();
    }
    
    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) return;
        
        currentPage = page;
        updateCurrentPage();
        updateProductGrid();
        
        pageInfoLabel.setText("Trang " + currentPage + "/" + totalPages);
        updatePaginationButtons();
    }
    
    private void updatePaginationButtons() {
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
    }
    
    private void createHeader() {
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(primaryDark);
        
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(primaryDark);
        topHeader.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        JLabel logoLabel = new JLabel("KEYCAP STORE");
        logoLabel.setFont(logoFont);
        logoLabel.setForeground(glassWhite);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(primaryDark);
        
        // Nút giỏ hàng
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        cartPanel.setBackground(primaryDark);
        
        cartBtn = new JButton("Giỏ hàng");
        cartBtn.setFont(normalFont);
        cartBtn.setForeground(glassWhite);
        cartBtn.setBackground(primaryDark);
        cartBtn.setBorderPainted(false);
        cartBtn.setFocusPainted(false);
        cartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cartBtn.addActionListener(e -> openCart());
        
        cartCountLabel = new JLabel("0");
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountLabel.setForeground(glassWhite);
        cartCountLabel.setBackground(dangerRed);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        
        cartPanel.add(cartBtn);
        cartPanel.add(cartCountLabel);
        
        rightPanel.add(cartPanel);
        
        // Nút đăng nhập hoặc tên người dùng
        if (isGuest) {
            loginBtn = new JButton("Đăng nhập");
            loginBtn.setFont(normalFont);
            loginBtn.setForeground(primaryDark);
            loginBtn.setBackground(glassWhite);
            loginBtn.setBorderPainted(false);
            loginBtn.setFocusPainted(false);
            loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            loginBtn.addActionListener(e -> {
                ModernLoginDialog loginDialog = new ModernLoginDialog();
                loginDialog.setVisible(true);
            });
            
            rightPanel.add(loginBtn);
        } else {
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
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(primaryDark);
        searchPanel.setBorder(new EmptyBorder(0, 15, 8, 15));
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.setFont(normalFont);
        searchField.setForeground(textPrimary);
        searchField.setBackground(glassWhite);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(taupeGrey, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm sản phẩm...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchProducts();
            }
        });
        
        JButton searchBtn = new JButton("Tìm");
        searchBtn.setFont(normalFont);
        searchBtn.setBackground(infoBlue);
        searchBtn.setForeground(glassWhite);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.setPreferredSize(new Dimension(70, 35));
        searchBtn.addActionListener(e -> searchProducts());
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        
        headerPanel.add(topHeader);
        headerPanel.add(searchPanel);
    }
    
    private void searchProducts() {
        String keyword = searchField.getText().toLowerCase().trim();
        
        if (keyword.isEmpty()) {
            filteredProducts = new ArrayList<>(allProducts);
        } else {
            filteredProducts = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        }
        
        updatePagination();
        updateTitle();
        updateProductGrid();
        pageInfoLabel.setText("Trang " + currentPage + "/" + totalPages);
        updatePaginationButtons();
    }
    
    private void openCart() {
        GioHangPanel cartDialog = new GioHangPanel(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            cartItems, 
            currentUser
        );
        cartDialog.setVisible(true);
        
        cartCountLabel.setText(String.valueOf(cartItems.size()));
    }
    
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(glassWhite);
        card.setBorder(new LineBorder(taupeGrey, 1));
        card.setPreferredSize(new Dimension(250, 300));
        card.setMaximumSize(new Dimension(250, 300));
        card.setMinimumSize(new Dimension(250, 300));
        
        // Panel chứa ảnh
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(210, 130));
        imagePanel.setMaximumSize(new Dimension(210, 130));
        imagePanel.setBackground(new Color(240, 240, 240));
        imagePanel.setOpaque(true);
        
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String imagePath = p.getImage();
                URL imageUrl = getClass().getResource(imagePath);
                
                if (imageUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(imageUrl);
                    Image originalImage = originalIcon.getImage();
                    
                    int maxWidth = 190;
                    int maxHeight = 110;
                    
                    Image scaledImage = originalImage.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    imageLabel.setText("No Image");
                    imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                }
            } catch (Exception e) {
                imageLabel.setText("No Image");
                imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            }
        } else {
            imageLabel.setText("No Image");
            imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        // Tên sản phẩm
        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(textPrimary);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Danh mục
        JLabel categoryLabel = new JLabel("Danh mục: " + p.getCategoryName());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        categoryLabel.setForeground(infoBlue);
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Xuất xứ
        String origin = p.getProfile();
        if (origin == null || origin.isEmpty()) {
            origin = "Không rõ";
        }
        JLabel originLabel = new JLabel("XS: " + origin);
        originLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        originLabel.setForeground(taupeGrey);
        originLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Giá
        String priceStr = df.format(p.getPrice()) + "₫";
        JLabel priceLabel = new JLabel(priceStr);
        priceLabel.setFont(priceFont);
        priceLabel.setForeground(dangerRed);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Panel chứa 2 nút
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(glassWhite);
        buttonPanel.setMaximumSize(new Dimension(220, 30));
        buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        JButton addToCartBtn = new JButton("Thêm");
        addToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        addToCartBtn.setBackground(successGreen);
        addToCartBtn.setForeground(glassWhite);
        addToCartBtn.setBorderPainted(false);
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToCartBtn.addActionListener(e -> {
            boolean exists = false;
            for (GioHangPanel.CartItem item : cartItems) {
                if (item.getId().equals(String.valueOf(p.getId()))) {
                    item.setQuantity(item.getQuantity() + 1);
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                cartItems.add(new GioHangPanel.CartItem(
                    String.valueOf(p.getId()), 
                    p.getName(), 
                    p.getCategoryName(), 
                    (int) p.getPrice(), 
                    1, 
                    p.getImage()
                ));
            }
            
            cartCountLabel.setText(String.valueOf(cartItems.size()));
            JOptionPane.showMessageDialog(this, "Đã thêm " + p.getName() + " vào giỏ hàng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton buyNowBtn = new JButton("Mua");
        buyNowBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        buyNowBtn.setBackground(orangeColor);
        buyNowBtn.setForeground(glassWhite);
        buyNowBtn.setBorderPainted(false);
        buyNowBtn.setFocusPainted(false);
        buyNowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyNowBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Bạn đã chọn mua ngay: " + p.getName() + "\nGiá: " + df.format(p.getPrice()) + "₫", "Mua ngay", JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(addToCartBtn);
        buttonPanel.add(buyNowBtn);
        
        card.add(Box.createVerticalStrut(8));
        card.add(imagePanel);
        card.add(Box.createVerticalStrut(8));
        card.add(nameLabel);
        card.add(categoryLabel);
        card.add(originLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(priceLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(buttonPanel);
        card.add(Box.createVerticalStrut(8));
        
        return card;
    }
}