package com.keycapstore.gui;

import com.keycapstore.bus.ProductBUS;
import com.keycapstore.bus.ReviewBUS;
import com.keycapstore.dao.WishlistDAO;
import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import com.keycapstore.model.Product;
import com.keycapstore.model.Wishlist;
import com.keycapstore.utils.ImageHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MuaHangPanel extends JPanel implements Refreshable {

    private JPanel headerPanel;
    private JTextField searchField;
    private JLabel cartCountLabel;
    private JButton loginBtn;
    private DecimalFormat df = new DecimalFormat("#,###");
    private Object currentUser;
    private Consumer<String> onNavigate;
    private boolean isGuest = false;

    private ProductBUS productBUS;
    private WishlistDAO wishlistDAO;
    private ReviewBUS reviewBUS; // Thêm ReviewBUS

    // MÀU SẮC THEO BẢNG CHUẨN (VIBE CŨ: KEM/NÂU)
    private Color primaryColor = new Color(62, 54, 46); // #3E362E - Nâu đậm
    private Color bgColor = new Color(228, 220, 207); // #E4DCCF - Kem nền
    private Color cardColor = new Color(255, 252, 245); // #FFFCF5 - Trắng ngà
    private Color borderColor = new Color(153, 143, 133); // #998F85 - Xám nâu
    private Color textPrimary = new Color(51, 51, 51); // #333333 - Đen xám
    private Color textSecondary = new Color(102, 102, 102); // Xám
    private Color hoverColor = new Color(240, 235, 225); // Hover nhẹ
    private Color successGreen = new Color(46, 204, 113); // Xanh lá (Nút thêm)
    private Color infoBlue = new Color(52, 152, 219); // Xanh dương (Tìm kiếm)
    private Color dangerRed = new Color(231, 76, 60); // Đỏ (Giá tiền)

    // Font chữ
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
    private Font normalFont = new Font("Segoe UI", Font.PLAIN, 12);
    private Font smallFont = new Font("Segoe UI", Font.PLAIN, 10);
    private Font priceFont = new Font("Segoe UI", Font.BOLD, 14);
    private Font logoFont = new Font("Segoe UI", Font.BOLD, 22);

    // Danh sách sản phẩm
    private List<Product> productList;

    // Banner Variables
    private Timer bannerTimer; // Timer chuyển slide (3s)
    private Timer bannerAnimator; // Timer hiệu ứng fade (16ms)
    private ArrayList<ImageIcon> bannerImages;
    private Image bannerCurrentImg, bannerNextImg;
    private float bannerAlpha = 1.0f; // Độ mờ (0.0 -> 1.0)
    private int bannerIndex = 0;

    // Page Transition Variables
    private float contentAlpha = 1.0f;
    private Timer transitionTimer;

    private JPanel mainContent; // Panel chính để thay đổi nội dung

    // Constructor nhận tham số user và consumer để điều hướng
    public MuaHangPanel(Object user, Consumer<String> onNavigate) {
        this.productBUS = new ProductBUS();
        this.wishlistDAO = new WishlistDAO();
        this.reviewBUS = new ReviewBUS(); // Khởi tạo
        this.currentUser = user;
        this.onNavigate = onNavigate;
        checkUserType();
        initData();
        initUI();
    }

    // Constructor phụ
    public MuaHangPanel(Object user) {
        this(user, null);
    }

    // Constructor mặc định
    public MuaHangPanel() {
        this(null, null);
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

    private void initData() {
        // LẤY DỮ LIỆU THẬT TỪ DATABASE
        // SỬA LỖI: Chỉ lấy các sản phẩm có trạng thái "Active" để hiển thị cho khách
        // hàng.
        productList = productBUS.getActiveProducts();

        // Load ảnh banner một lần duy nhất
        bannerImages = new ArrayList<>();
        // CÁCH 1: Quét thư mục (Dành cho môi trường Dev/IDE) - Giống code cũ để đảm bảo
        // tìm thấy ảnh
        try {
            File dir = new File("src/main/resources/img/bg/background");
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpeg"));

                if (files != null && files.length > 0) {
                    for (File f : files) {
                        bannerImages.add(new ImageIcon(f.getAbsolutePath()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // CÁCH 2: Nếu Cách 1 không tìm thấy (VD: Chạy file JAR hoặc đường dẫn khác),
        // dùng Resource Loader
        if (bannerImages.isEmpty()) {
            String[] bannerFiles = { "banner1.jpg", "banner2.jpg", "banner3.jpg", "banner4.jpg",
                    "banner1.png", "banner2.png", "banner3.png", "banner4.png" };
            String[] possiblePaths = { "/img/bg/background/", "/images/banner/", "/images/", "/banners/" };

            for (String fileName : bannerFiles) {
                for (String path : possiblePaths) {
                    try {
                        java.net.URL imgURL = getClass().getResource(path + fileName);
                        if (imgURL != null) {
                            bannerImages.add(new ImageIcon(imgURL));
                            break; // Tìm thấy ảnh này thì break loop path
                        }
                    } catch (Exception e) {
                        // Ignore error
                    }
                }
            }
        }
        if (!bannerImages.isEmpty())
            bannerCurrentImg = bannerImages.get(0).getImage();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(bgColor);

        // Header
        createHeader();

        // Main content với scroll
        mainContent = new JPanel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // FIX: Clamp alpha to [0.0, 1.0] to avoid IllegalArgumentException
                float alpha = Math.max(0.0f, Math.min(1.0f, contentAlpha));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                super.paint(g2);
                g2.dispose();
            }
        };
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(bgColor);

        // Hiển thị trang chủ mặc định
        showHome();
        updateCartCount(); // Cập nhật số lượng giỏ hàng ngay khi mở

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(bgColor);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // --- CẬP NHẬT SỐ LƯỢNG GIỎ HÀNG ---
    private void updateCartCount() {
        int total = 0;
        if (CartPanel.cartItems != null) {
            for (com.keycapstore.model.InvoiceDetail item : CartPanel.cartItems) {
                total += item.getQuantity();
            }
        }
        if (cartCountLabel != null) {
            cartCountLabel.setText(total > 99 ? "99+" : String.valueOf(total));
        }
    }

    // --- HIỆU ỨNG CHUYỂN TRANG (FADE) ---
    private void runTransition(Runnable updateTask) {
        if (transitionTimer != null && transitionTimer.isRunning())
            transitionTimer.stop();

        transitionTimer = new Timer(10, new ActionListener() {
            boolean fadingOut = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (fadingOut) {
                    contentAlpha -= 0.1f; // Tốc độ mờ dần
                    if (contentAlpha <= 0.0f) {
                        contentAlpha = 0.0f;
                        fadingOut = false;
                        updateTask.run(); // Cập nhật nội dung khi đã ẩn hoàn toàn
                        // Scroll lên đầu
                        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class,
                                mainContent);
                        if (scrollPane != null)
                            scrollPane.getVerticalScrollBar().setValue(0);
                    }
                } else {
                    contentAlpha += 0.1f; // Tốc độ hiện dần
                    if (contentAlpha >= 1.0f) {
                        contentAlpha = 1.0f;
                        ((Timer) e.getSource()).stop();
                    }
                }
                mainContent.repaint();
            }
        });
        transitionTimer.start();
    }

    // --- HIỂN THỊ TRANG CHỦ ---
    private void showHome() {
        mainContent.removeAll();
        mainContent.add(createNavBar());
        mainContent.add(createBanner());
        mainContent.add(createPolicySection());

        // --- PHẦN SẢN PHẨM NỔI BẬT ---
        // Lọc ra các sản phẩm được đánh dấu là "Nổi bật"
        List<Product> featuredList = productList.stream()
                .filter(Product::isFeatured)
                .collect(Collectors.toList());

        if (featuredList.size() > 0) {
            mainContent.add(createSectionPanel("SẢN PHẨM NỔI BẬT", featuredList,
                    () -> runTransition(() -> showCategory("All", "SẢN PHẨM NỔI BẬT"))));
            mainContent.add(Box.createVerticalStrut(20)); // Khoảng cách
        }
        mainContent.add(createCategoryProductSection("GÓC KEYCAP", "Keycap Set"));
        mainContent.add(createCategoryProductSection("THẾ GIỚI SWITCH", "Switch"));
        mainContent.add(createCategoryProductSection("ARTISAN CAO CẤP", "Artisan Keycap"));
        mainContent.add(createFooter());
        mainContent.revalidate();
        mainContent.repaint();
    }

    // --- HIỂN THỊ TRANG DANH MỤC (LỌC SẢN PHẨM) ---
    private void showCategory(String categoryName, String title) {
        mainContent.removeAll();
        mainContent.add(createNavBar());

        // Tiêu đề danh mục
        mainContent.add(createFeaturedTitle(title, null));

        // Lọc sản phẩm
        List<Product> filtered;
        if ("All".equals(categoryName)) {
            filtered = new ArrayList<>(productList);
        } else if (categoryName != null) {
            filtered = productList.stream()
                    .filter(p -> categoryName.equals(p.getCategoryName()))
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>();
        }

        mainContent.add(createProductGrid(filtered));
        mainContent.add(createFooter());

        mainContent.revalidate();
        mainContent.repaint();
    }

    // --- LOGIC TÌM KIẾM ---
    private void performSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            runTransition(this::showHome);
            return;
        }

        runTransition(() -> {
            mainContent.removeAll();
            mainContent.add(createNavBar());
            mainContent.add(createFeaturedTitle("KẾT QUẢ TÌM KIẾM: \"" + keyword + "\"", null));

            List<Product> filtered = productList.stream()
                    .filter(p -> {
                        String name = p.getName() != null ? p.getName().toLowerCase() : "";
                        String maker = p.getMakerName() != null ? p.getMakerName().toLowerCase() : "";
                        String cat = p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "";
                        return name.contains(keyword) || maker.contains(keyword) || cat.contains(keyword);
                    })
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                JPanel pnlEmpty = new JPanel(new FlowLayout());
                pnlEmpty.setBackground(bgColor);
                JLabel lbl = new JLabel("Không tìm thấy sản phẩm nào phù hợp.");
                lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                lbl.setForeground(textSecondary);
                pnlEmpty.add(lbl);
                mainContent.add(pnlEmpty);
            } else {
                mainContent.add(createProductGrid(filtered));
            }

            mainContent.add(createFooter());
            mainContent.revalidate();
            mainContent.repaint();
        });
    }

    // --- HIỂN THỊ TRANG CHI TIẾT CHÍNH SÁCH ---
    private void showPolicyDetail(String title, String htmlContent) {
        mainContent.removeAll();
        mainContent.add(createNavBar());

        // Container cho nội dung chính sách
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new EmptyBorder(30, 100, 50, 100)); // Padding rộng 2 bên

        // Tiêu đề lớn
        JLabel lblHeader = new JLabel(title.toUpperCase());
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHeader.setForeground(primaryColor);
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        lblHeader.setBorder(new EmptyBorder(0, 0, 30, 0));
        contentPanel.add(lblHeader, BorderLayout.NORTH);

        // Nội dung HTML
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(htmlContent);
        editorPane.setEditable(false);
        editorPane.setBackground(Color.WHITE); // Nền trắng rõ ràng
        editorPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(30, 40, 30, 40)));
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        contentPanel.add(editorPane, BorderLayout.CENTER);

        // Nút quay lại
        JButton btnBack = new JButton("← Quay lại Trang Chủ");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(primaryColor);
        btnBack.setBackground(cardColor);
        btnBack.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnBack.setContentAreaFilled(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> runTransition(this::showHome));

        contentPanel.add(btnBack, BorderLayout.SOUTH);

        mainContent.add(contentPanel);
        mainContent.add(createFooter());

        mainContent.revalidate();
        mainContent.repaint();

        // Scroll lên đầu trang
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, mainContent);
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private void createHeader() {
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(primaryColor);

        // Top header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(primaryColor);
        topHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel logoLabel = new JLabel("KEYFORGE");
        logoLabel.setFont(logoFont);
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                runTransition(MuaHangPanel.this::showHome);
            }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(primaryColor);

        // Nút giỏ hàng
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        cartPanel.setBackground(primaryColor);

        JButton cartBtn = new JButton("Giỏ hàng");
        cartBtn.setFont(normalFont);
        cartBtn.setForeground(Color.WHITE);
        cartBtn.setBackground(primaryColor);
        cartBtn.setBorderPainted(false);
        cartBtn.setFocusPainted(false);
        cartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cartBtn.addActionListener(e -> {
            if (onNavigate != null) {
                onNavigate.accept("CART");
            }
        });

        try {
            java.net.URL iconURL = getClass().getResource("/icons/sales.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                cartBtn.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cartCountLabel = new JLabel("0");
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setBackground(dangerRed);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBorder(new EmptyBorder(2, 6, 2, 6));

        // FIX: Ẩn nút giỏ hàng đối với Admin/Nhân viên
        if (!(currentUser instanceof Employee)) {
            cartPanel.add(cartBtn);
            cartPanel.add(cartCountLabel);
        }

        rightPanel.add(cartPanel);

        // Chỉ hiển thị nút Đăng nhập nếu là Guest
        if (isGuest) {
            loginBtn = new JButton("Đăng nhập");
            loginBtn.setFont(normalFont);
            loginBtn.setForeground(primaryColor);
            loginBtn.setBackground(cardColor);
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
            userLabel.setForeground(Color.WHITE);
            rightPanel.add(userLabel);
        }

        topHeader.add(logoLabel, BorderLayout.WEST);
        topHeader.add(rightPanel, BorderLayout.EAST);

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(primaryColor);
        searchPanel.setBorder(new EmptyBorder(5, 15, 15, 15));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 40));
        searchField.setFont(normalFont);
        searchField.setForeground(textPrimary);
        searchField.setBackground(cardColor);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(5, 10, 5, 10)));
        searchField.setCaretColor(Color.BLACK);

        JButton searchBtn = new JButton("Tìm kiếm");
        searchBtn.setFont(normalFont);
        searchBtn.setBackground(primaryColor);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.setPreferredSize(new Dimension(100, 40));

        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        headerPanel.add(topHeader);
        headerPanel.add(searchPanel);
    }

    // --- NAVIGATION BAR (MENU NGANG) ---
    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        navBar.setBackground(bgColor);
        navBar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, borderColor));

        addNavButton(navBar, "TRANG CHỦ", e -> runTransition(this::showHome));
        addNavButton(navBar, "KEYCAP BỘ", e -> runTransition(() -> showCategory("Keycap Set", "KEYCAP BỘ")));
        addNavButton(navBar, "SWITCH", e -> runTransition(() -> showCategory("Switch", "SWITCH")));
        addNavButton(navBar, "KEYCAP ARTISAN",
                e -> runTransition(() -> showCategory("Artisan Keycap", "KEYCAP ARTISAN")));
        addNavButton(navBar, "BẢO HÀNH", e -> runTransition(this::showWarrantyPage));

        return navBar;
    }

    private void addNavButton(JPanel panel, String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(textPrimary);
        btn.setBackground(cardColor);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(dangerRed);
            }

            public void mouseExited(MouseEvent e) {
                btn.setForeground(textPrimary);
            }
        });
        btn.addActionListener(action);
        panel.add(btn);
    }

    private JPanel createBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(bgColor);
        banner.setPreferredSize(new Dimension(0, 300));

        if (bannerImages == null || bannerImages.isEmpty()) {
            JLabel titleLabel = new JLabel(
                    "<html><center>FLASH SALE CUỐI TUẦN<br/>GIẢM GIÁ LÊN ĐẾN 50%</center></html>");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(successGreen);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            banner.add(titleLabel, BorderLayout.CENTER);
        } else {
            JPanel imagePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    if (bannerCurrentImg != null) {
                        drawCoverImage(g2, bannerCurrentImg, w, h, 1.0f);
                    }

                    if (bannerNextImg != null && bannerAlpha > 0) {
                        // FIX: Clamp alpha to [0.0, 1.0]
                        float alpha = Math.max(0.0f, Math.min(1.0f, bannerAlpha));
                        drawCoverImage(g2, bannerNextImg, w, h, alpha);
                    }
                }
            };
            banner.add(imagePanel, BorderLayout.CENTER);

            if (bannerTimer != null && bannerTimer.isRunning())
                bannerTimer.stop();
            if (bannerAnimator != null && bannerAnimator.isRunning())
                bannerAnimator.stop();

            bannerTimer = new Timer(3000, e -> {
                if (bannerImages.isEmpty())
                    return;

                bannerIndex = (bannerIndex + 1) % bannerImages.size();
                bannerNextImg = bannerImages.get(bannerIndex).getImage();
                bannerAlpha = 0.0f;

                bannerAnimator.start();
            });

            bannerAnimator = new Timer(16, e -> {
                bannerAlpha += 0.05f;
                if (bannerAlpha >= 1.0f) {
                    bannerAlpha = 1.0f;
                    bannerCurrentImg = bannerNextImg;
                    bannerNextImg = null;
                    bannerAnimator.stop();
                }
                imagePanel.repaint();
            });

            bannerTimer.start();
        }

        return banner;
    }

    // Hàm vẽ ảnh chế độ COVER (Lấp đầy)
    private void drawCoverImage(Graphics2D g2, Image img, int w, int h, float alpha) {
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        if (imgW <= 0 || imgH <= 0)
            return;

        double scale = Math.max((double) w / imgW, (double) h / imgH);
        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);
        int x = (w - newW) / 2;
        int y = (h - newH) / 2;

        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(img, x, y, newW, newH, null);
        g2.setComposite(oldComp);
    }

    // --- POLICY SECTION (CAM KẾT) ---
    private JPanel createPolicySection() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String imgShip = getIconUrl("shipping.png");
        String imgAuth = getIconUrl("product2.png");
        String imgSupport = getIconUrl("service.png");
        String imgReturn = getIconUrl("invoice.png");

        String shippingContent = "<html><body style='font-family: Segoe UI; color: #333;'>"
                + "<center><img src='" + imgShip + "' width='100' height='100'></center>"
                + "<h2 style='color: #3E362E; text-align: center;'>CHÍNH SÁCH GIAO HÀNG</h2>"
                + "<p><b>Keyforge Artisan</b> hợp tác với các đơn vị vận chuyển uy tín nhất hiện nay (GHTK, Viettel Post, Ahamove) để đảm bảo sản phẩm đến tay bạn nhanh chóng và an toàn.</p>"
                + "<ul>"
                + "<li><b>Nội thành TP.HCM:</b> Giao hỏa tốc trong 2h hoặc trong ngày.</li>"
                + "<li><b>Các tỉnh thành khác:</b> Thời gian giao hàng từ 2 - 4 ngày làm việc.</li>"
                + "<li><b>Freeship:</b> Miễn phí vận chuyển cho đơn hàng từ 1.000.000đ.</li>"
                + "</ul>"
                + "<p><i>Lưu ý: Quý khách được kiểm tra hàng trước khi thanh toán (đồng kiểm).</i></p>"
                + "</body></html>";

        String authContent = "<html><body style='font-family: Segoe UI; color: #333;'>"
                + "<center><img src='" + imgAuth + "' width='100' height='100'></center>"
                + "<h2 style='color: #3E362E; text-align: center;'>CAM KẾT CHÍNH HÃNG</h2>"
                + "<p>Tại <b>Keyforge Artisan</b>, chúng tôi nói KHÔNG với hàng giả, hàng nhái (Fake/Replica).</p>"
                + "<p>Tất cả sản phẩm (Keycap, Switch, Kit bàn phím) đều được nhập khẩu trực tiếp từ các thương hiệu danh tiếng:</p>"
                + "<ul>"
                + "<li><b>Keycap:</b> GMK, SP, Domikey, Keychron...</li>"
                + "<li><b>Switch:</b> Cherry, Gateron, Kailh, TTC...</li>"
                + "</ul>"
                + "<p style='color: #E74C3C;'><b>★ CAM KẾT: Hoàn tiền 200% nếu phát hiện hàng giả.</b></p>"
                + "</body></html>";

        String supportContent = "<html><body style='font-family: Segoe UI; color: #333;'>"
                + "<center><img src='" + imgSupport + "' width='100' height='100'></center>"
                + "<h2 style='color: #3E362E; text-align: center;'>HỖ TRỢ KỸ THUẬT</h2>"
                + "<p>Đội ngũ của chúng tôi không chỉ là người bán hàng, mà còn là những người chơi phím cơ (Enthusiasts) lâu năm.</p>"
                + "<p>Chúng tôi sẵn sàng hỗ trợ bạn:</p>"
                + "<ul>"
                + "<li>Tư vấn build phím, chọn switch, keycap phù hợp.</li>"
                + "<li>Hướng dẫn mod phím, lube switch, cân wire.</li>"
                + "<li>Khắc phục các lỗi phần mềm, firmware.</li>"
                + "</ul>"
                + "<p><b>Hotline/Zalo:</b> 0909.123.456<br><b>Fanpage:</b> fb.com/keyforge</p>"
                + "</body></html>";

        String returnContent = "<html><body style='font-family: Segoe UI; color: #333;'>"
                + "<center><img src='" + imgReturn + "' width='100' height='100'></center>"
                + "<h2 style='color: #3E362E; text-align: center;'>CHÍNH SÁCH ĐỔI TRẢ</h2>"
                + "<p>Để đảm bảo quyền lợi tốt nhất cho khách hàng, Keyforge áp dụng chính sách đổi trả minh bạch:</p>"
                + "<ul>"
                + "<li><b>1 đổi 1 trong 7 ngày đầu</b> nếu sản phẩm có lỗi từ nhà sản xuất (liệt switch, nứt keycap, lỗi PCB...).</li>"
                + "<li>Hỗ trợ bảo hành sửa chữa miễn phí trong suốt thời gian bảo hành.</li>"
                + "</ul>"
                + "<p><i>Điều kiện: Sản phẩm còn nguyên vẹn, đầy đủ hộp và phụ kiện đi kèm.</i></p>"
                + "</body></html>";

        panel.add(createPolicyButton("Giao hàng toàn quốc", "Nhận hàng trong 2-4 ngày", shippingContent));
        panel.add(createPolicyButton("Sản phẩm chính hãng", "Cam kết chất lượng 100%", authContent));
        panel.add(createPolicyButton("Hỗ trợ 24/7", "Tư vấn nhiệt tình", supportContent));
        panel.add(createPolicyButton("Đổi trả dễ dàng", "Trong vòng 7 ngày", returnContent));

        return panel;
    }

    private String getIconUrl(String iconName) {
        java.net.URL url = getClass().getResource("/icons/" + iconName);
        if (url != null)
            return url.toString();

        // Fallback tìm ở thư mục khác nếu không thấy trong /icons/
        url = getClass().getResource("/images/" + iconName);
        if (url != null)
            return url.toString();

        // Fallback cuối cùng: Trả về chuỗi rỗng (sẽ hiện ảnh vỡ hoặc text alt)
        // Hoặc có thể trả về icon mặc định nếu có
        return "";
    }

    private JButton createPolicyButton(String title, String sub, String htmlContent) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(primaryColor);

        JLabel lblSub = new JLabel(sub, SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(textPrimary);

        btn.add(lblTitle, BorderLayout.CENTER);
        btn.add(lblSub, BorderLayout.SOUTH);

        btn.addActionListener(e -> runTransition(() -> showPolicyDetail(title, htmlContent)));

        return btn;
    }

    private JPanel createFeaturedTitle(String titleText, Runnable onViewAll) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(20, 15, 10, 15));

        JLabel title = new JLabel(titleText);
        title.setFont(titleFont);
        title.setForeground(textPrimary);

        JLabel viewAll = new JLabel("Xem tất cả →");
        viewAll.setFont(normalFont);
        viewAll.setForeground(textSecondary);

        if (onViewAll != null) {
            viewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
            viewAll.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onViewAll.run();
                }
            });
        } else {
            viewAll.setVisible(false);
        }

        panel.add(title, BorderLayout.WEST);
        panel.add(viewAll, BorderLayout.EAST);

        return panel;
    }

    // --- CAROUSEL SECTION (Sản phẩm dàn 3 cột + Mũi tên) ---
    private JPanel createSectionPanel(String title, List<Product> products, Runnable onViewAll) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(bgColor);
        container.add(createFeaturedTitle(title, onViewAll), BorderLayout.NORTH);
        container.add(createProductCarousel(products), BorderLayout.CENTER);
        return container;
    }

    // --- SECTION SẢN PHẨM THEO DANH MỤC ---
    private JPanel createCategoryProductSection(String title, String categoryName) {
        List<Product> filtered = new ArrayList<>();
        if (categoryName != null) {
            filtered = productList.stream()
                    .filter(p -> categoryName.equals(p.getCategoryName()))
                    .collect(Collectors.toList());
        }

        return createSectionPanel(title, filtered, () -> runTransition(() -> showCategory(categoryName, title)));
    }

    private JPanel createProductCarousel(List<Product> products) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(0, 15, 20, 15));

        int visibleCount = 3;

        JPanel cardContainer = new JPanel(new GridLayout(1, visibleCount, 15, 0));
        cardContainer.setBackground(bgColor);

        JButton btnLeft = createArrowButton("<");
        JButton btnRight = createArrowButton(">");

        final int[] currentIndex = { 0 };

        Runnable updateView = () -> {
            cardContainer.removeAll();
            for (int i = 0; i < visibleCount; i++) {
                int idx = currentIndex[0] + i;
                if (idx < products.size()) {
                    cardContainer.add(createProductCard(products.get(idx)));
                } else {
                    JPanel empty = new JPanel();
                    empty.setBackground(bgColor);
                    cardContainer.add(empty);
                }
            }
            btnLeft.setEnabled(currentIndex[0] > 0);
            btnRight.setEnabled(currentIndex[0] + visibleCount < products.size());

            cardContainer.revalidate();
            cardContainer.repaint();
        };

        btnLeft.addActionListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateView.run();
            }
        });

        btnRight.addActionListener(e -> {
            if (currentIndex[0] + visibleCount < products.size()) {
                currentIndex[0]++;
                updateView.run();
            }
        });

        updateView.run();

        if (products.size() > visibleCount) {
            panel.add(btnLeft, BorderLayout.WEST);
            panel.add(btnRight, BorderLayout.EAST);
        }
        panel.add(cardContainer, BorderLayout.CENTER);

        return panel;
    }

    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(textPrimary);
        btn.setBackground(cardColor);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 40));
        return btn;
    }

    private JPanel createProductGrid(List<Product> products) {
        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setBackground(bgColor);
        grid.setBorder(new EmptyBorder(0, 15, 20, 15));

        for (Product p : products) {
            grid.add(createProductCard(p));
        }

        return grid;
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
                card.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(cardColor);
            }
        });

        JPanel topActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topActionPanel.setOpaque(false);
        topActionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topActionPanel.setMaximumSize(new Dimension(180, 30));

        // Vẽ icon trái tim thay vì dùng text để tránh lỗi font
        JLabel lblHeart = new JLabel() {
            private boolean isFilled = false;

            @Override
            public void setText(String text) {
                // Dùng setText để lưu trạng thái ("♥" = filled, "♡" = empty)
                this.isFilled = "♥".equals(text);
                super.setText(text); // Vẫn set text để logic bên dưới hoạt động
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                // Không gọi super.paintComponent để không vẽ text
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dangerRed);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 8;
                int x = (w - size) / 2;
                int y = (h - size) / 2 + 2;

                if (isFilled) {
                    g2.fillArc(x, y, size / 2, size / 2, 0, 180);
                    g2.fillArc(x + size / 2, y, size / 2, size / 2, 0, 180);
                    int[] px = { x, x + size / 2, x + size };
                    int[] py = { y + size / 4 + 1, y + size, y + size / 4 + 1 };
                    g2.fillPolygon(px, py, 3);
                } else {
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawArc(x, y, size / 2, size / 2, 0, 180);
                    g2.drawArc(x + size / 2, y, size / 2, size / 2, 0, 180);
                    g2.drawLine(x, y + size / 4 + 1, x + size / 2, y + size);
                    g2.drawLine(x + size, y + size / 4 + 1, x + size / 2, y + size);
                }
            }
        };
        lblHeart.setText("♡"); // Mặc định rỗng
        lblHeart.setPreferredSize(new Dimension(30, 30));
        lblHeart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!isGuest && currentUser instanceof Customer) {
            int cusId = ((Customer) currentUser).getCustomerId();
            // SỬA LỖI: Sử dụng ProductBUS để kiểm tra wishlist thay vì gọi trực tiếp DAO có
            // thể thiếu method
            boolean isLiked = false;
            List<Product> wishList = productBUS.getWishlistProducts(cusId);
            for (Product wp : wishList) {
                if (wp.getId() == p.getId()) {
                    isLiked = true;
                    break;
                }
            }

            if (isLiked) {
                lblHeart.setText("♥");
            }
        }

        lblHeart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleWishlist(p, lblHeart);
                e.consume();
            }
        });
        topActionPanel.add(lblHeart);

        JLabel imageLabel = new JLabel("Hình ảnh");
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        imageLabel.setForeground(textSecondary);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(180, 130));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = ImageHelper.loadResizedIcon(p.getImage(), 180, 130);
        if (icon != null)
            imageLabel.setIcon(icon);
        imageLabel.setText("");

        // SỬA LỖI: Dùng SupplierName thay vì MakerName (vì BUS chưa populate MakerName)
        String maker = p.getSupplierName() != null ? p.getSupplierName() : "Unknown";
        if (icon != null && "Unknown".equals(maker)) {
            maker = " ";
        }

        JLabel brandLabel = new JLabel(maker);
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brandLabel.setForeground(textSecondary);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(textPrimary);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel specsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        specsPanel.setOpaque(false);

        JLabel profileLabel = new JLabel(p.getProfile() != null ? p.getProfile() : "-");
        profileLabel.setFont(smallFont);
        profileLabel.setForeground(textSecondary);

        JLabel materialLabel = new JLabel(p.getMaterial() != null ? p.getMaterial() : "-");
        materialLabel.setFont(smallFont);
        materialLabel.setForeground(textSecondary);

        specsPanel.add(profileLabel);
        specsPanel.add(new JLabel("•"));
        specsPanel.add(materialLabel);

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        ratingPanel.setOpaque(false);

        JLabel ratingLabel = new JLabel("Đánh giá: 5.0/5");
        ratingLabel.setFont(smallFont);
        ratingLabel.setForeground(textSecondary);
        ratingPanel.add(ratingLabel);

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pricePanel.setOpaque(false);

        String priceStr = df.format(p.getPrice()) + "₫";

        JLabel priceLabel = new JLabel(priceStr);
        priceLabel.setFont(priceFont);
        priceLabel.setForeground(dangerRed);
        pricePanel.add(priceLabel);

        JLabel soldLabel = new JLabel("Đã bán " + (p.getId() * 3) + "+");
        soldLabel.setFont(smallFont);
        soldLabel.setForeground(textSecondary);
        soldLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addBtn = new JButton("Thêm vào giỏ");
        addBtn.setFont(smallFont);
        addBtn.setBackground(successGreen);
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FIX: Logic nút mua hàng (Admin chỉ xem)
        if (currentUser instanceof Employee) {
            addBtn.setEnabled(false);
            addBtn.setText("Chế độ Admin");
            addBtn.setBackground(Color.LIGHT_GRAY);
            addBtn.setToolTipText("Tài khoản quản trị chỉ có quyền xem.");
        } else {
            addBtn.addActionListener(e -> {
                CartPanel.addToCart(p, 1);
                updateCartCount();
                JOptionPane.showMessageDialog(this, "Đã thêm " + p.getName() + " vào giỏ hàng!");
            });
            if (p.getStock() <= 0) {
                addBtn.setEnabled(false);
                addBtn.setText("Hết hàng");
                addBtn.setBackground(Color.GRAY);
            }
        }

        card.add(topActionPanel);
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

    // --- FOOTER ---
    private JPanel createFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 3, 20, 0));
        footer.setBackground(primaryColor);
        footer.setBorder(new EmptyBorder(30, 30, 30, 30));

        footer.add(createFooterCol("VỀ KEYCAP STORE",
                "<html>Giới thiệu<br>Tuyển dụng<br>Liên hệ<br>Tin tức</html>"));
        footer.add(createFooterCol("CHÍNH SÁCH",
                "<html>Chính sách bảo hành<br>Chính sách đổi trả<br>Chính sách bảo mật<br>Điều khoản dịch vụ</html>"));
        footer.add(createFooterCol("THÔNG TIN LIÊN HỆ",
                "<html>Hotline: 0909.123.456<br>Email: support@keycap.vn<br>Địa chỉ: TP.HCM</html>"));

        return footer;
    }

    private JPanel createFooterCol(String title, String content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblContent = new JLabel(content);
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblContent.setForeground(new Color(200, 200, 200));
        lblContent.setBorder(new EmptyBorder(10, 0, 0, 0));

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(lblContent, BorderLayout.CENTER);
        return p;
    }

    // --- POPUP CHI TIẾT SẢN PHẨM ---
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
                @Override
                public void mouseEntered(MouseEvent e) {
                    lblThumb.setBorder(BorderFactory.createLineBorder(dangerRed));
                    ImageIcon large = ImageHelper.loadResizedIcon(imgPath, 500, 400);
                    if (large != null)
                        lblMainImg.setIcon(large);
                }

                @Override
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

        JPanel pnlMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlMeta.setBackground(Color.WHITE);
        pnlMeta.add(new JLabel("Thương hiệu: "));
        // SỬA LỖI: Dùng SupplierName
        JLabel lblBrand = new JLabel(p.getSupplierName() != null ? p.getSupplierName() : "Keyforge");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBrand.setForeground(primaryColor);
        pnlMeta.add(lblBrand);
        pnlMeta.add(new JLabel("  |  Tình trạng: "));
        JLabel lblStatus = new JLabel(p.getStock() > 0 ? "Còn hàng" : "Hết hàng");
        lblStatus.setForeground(p.getStock() > 0 ? successGreen : dangerRed);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlMeta.add(lblStatus);
        rightPanel.add(pnlMeta);
        rightPanel.add(Box.createVerticalStrut(15));

        JLabel lblPrice = new JLabel(df.format(p.getPrice()) + " ₫");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblPrice.setForeground(dangerRed);
        rightPanel.add(lblPrice);
        rightPanel.add(Box.createVerticalStrut(20));

        String specsHtml = "<html><ul style='margin-left: 10px; padding: 0px; list-style-type: square;'>"
                + "<li><b>Profile:</b> " + (p.getProfile() != null ? p.getProfile() : "N/A") + "</li>"
                + "<li><b>Chất liệu:</b> " + (p.getMaterial() != null ? p.getMaterial() : "N/A") + "</li>"
                + "<li><b>Xuất xứ:</b> " + (p.getOrigin() != null ? p.getOrigin() : "N/A") + "</li>"
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
        spnQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        rightPanel.add(Box.createVerticalStrut(25));

        if (p.getStock() <= 0) {
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
            btnAddToCart.setText("HẾT HÀNG");
            btnBuyNow.setText("HẾT HÀNG");
        }

        // FIX: Disable nút mua trong chi tiết sản phẩm với Admin
        if (currentUser instanceof Employee) {
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
            // Nếu còn hàng thì đổi text để báo hiệu, nếu hết hàng thì giữ nguyên text Hết
            // hàng
            if (p.getStock() > 0) {
                btnAddToCart.setText("ADMIN VIEW");
                btnBuyNow.setText("ADMIN VIEW");
            }
        }

        JPanel pnlPolicy = new JPanel(new GridLayout(3, 1, 0, 10));
        pnlPolicy.setBackground(Color.WHITE);
        pnlPolicy.add(createDetailPolicyItem("shipping.png", "Giao hàng toàn quốc", "Đồng kiểm khi nhận hàng"));
        pnlPolicy.add(createDetailPolicyItem("product2.png", "Bảo hành chính hãng", "Cam kết 100% chính hãng"));
        pnlPolicy.add(createDetailPolicyItem("invoice.png", "Đổi trả trong 7 ngày", "Nếu có lỗi nhà sản xuất"));
        rightPanel.add(pnlPolicy);

        topPanel.add(leftPanel);
        topPanel.add(rightPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        JPanel pnlReviews = new JPanel(new BorderLayout());
        pnlReviews.setBackground(Color.WHITE);

        // --- LOGIC HIỂN THỊ ĐÁNH GIÁ ---
        java.util.List<com.keycapstore.model.Review> reviews = reviewBUS.getReviewsByProduct(p.getId());

        if (reviews == null || reviews.isEmpty()) {
            JLabel lblNoReview = new JLabel("Chưa có đánh giá nào cho sản phẩm này.", SwingConstants.CENTER);
            lblNoReview.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblNoReview.setForeground(Color.GRAY);
            pnlReviews.add(lblNoReview, BorderLayout.CENTER);
        } else {
            // Header: Điểm trung bình
            double avgRating = reviewBUS.getAverageRating(p.getId());
            JPanel pnlHeaderReview = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlHeaderReview.setBackground(Color.WHITE);
            JLabel lblAvg = new JLabel(avgRating + "/5 ★");
            lblAvg.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblAvg.setForeground(new Color(255, 200, 0)); // Vàng
            JLabel lblCount = new JLabel("(" + reviews.size() + " đánh giá)");
            lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            pnlHeaderReview.add(lblAvg);
            pnlHeaderReview.add(lblCount);
            pnlReviews.add(pnlHeaderReview, BorderLayout.NORTH);

            // List: Danh sách comment
            JPanel pnlList = new JPanel();
            pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
            pnlList.setBackground(Color.WHITE);

            for (com.keycapstore.model.Review r : reviews) {
                JPanel item = new JPanel(new BorderLayout());
                item.setBackground(Color.WHITE);
                item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        new EmptyBorder(10, 10, 10, 10)));

                String stars = "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating());
                JLabel lblUserStar = new JLabel("<html><b>Khách hàng</b> <span style='color:#FFC800; font-size:14px;'>"
                        + stars + "</span></html>");
                JTextArea txtCmt = new JTextArea(r.getComment());
                txtCmt.setLineWrap(true);
                txtCmt.setWrapStyleWord(true);
                txtCmt.setEditable(false);

                item.add(lblUserStar, BorderLayout.NORTH);
                item.add(txtCmt, BorderLayout.CENTER);
                pnlList.add(item);
            }
            pnlReviews.add(new JScrollPane(pnlList), BorderLayout.CENTER);
        }
        tabbedPane.addTab("Đánh giá khách hàng", pnlReviews);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        dialog.add(mainPanel);

        btnAddToCart.addActionListener(e -> {
            int qty = (int) spnQty.getValue();
            CartPanel.addToCart(p, qty);
            updateCartCount();
            JOptionPane.showMessageDialog(dialog, "Đã thêm " + qty + " sản phẩm vào giỏ!");
        });

        btnBuyNow.addActionListener(e -> {
            int qty = (int) spnQty.getValue();
            CartPanel.addToCart(p, qty);
            updateCartCount();
            dialog.dispose();
            if (onNavigate != null)
                onNavigate.accept("CART");
        });

        dialog.setVisible(true);
    }

    // --- TRANG BẢO HÀNH (THIẾT KẾ RIÊNG) ---
    private void showWarrantyPage() {
        String content = "<html><body style='font-family: Segoe UI; color: #444; padding: 10px;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<h2 style='color: #E74C3C; margin: 0;'>CHÍNH SÁCH BẢO HÀNH KEYFORGE</h2>"
                + "<p style='font-style: italic; color: #777;'>Cam kết chất lượng - Hỗ trợ tận tâm</p>"
                + "</div>"

                + "<table style='width: 100%; border-collapse: collapse;'>"
                + "<tr>"
                + "<td style='vertical-align: top; width: 50%; padding-right: 20px;'>"
                + "<h3 style='color: #3E362E; border-bottom: 2px solid #E74C3C; padding-bottom: 5px;'>1. THỜI HẠN BẢO HÀNH</h3>"
                + "<ul style='list-style-type: square;'>"
                + "<li><b>Bàn phím cơ (Full/Kit):</b> 12 tháng.</li>"
                + "<li><b>Switch & Keycap:</b> 03 tháng (lỗi kỹ thuật).</li>"
                + "<li><b>Dịch vụ Mod/Lube:</b> 01 tháng.</li>"
                + "<li><b>Phụ kiện (Cáp, Kê tay):</b> 01 tháng.</li>"
                + "</ul>"
                + "</td>"
                + "<td style='vertical-align: top; width: 50%;'>"
                + "<h3 style='color: #3E362E; border-bottom: 2px solid #E74C3C; padding-bottom: 5px;'>2. ĐIỀU KIỆN BẢO HÀNH</h3>"
                + "<ul>"
                + "<li>Sản phẩm còn trong thời hạn bảo hành.</li>"
                + "<li>Tem bảo hành (nếu có) còn nguyên vẹn.</li>"
                + "<li>Lỗi xác định do nhà sản xuất (PCB, LED, Kết nối).</li>"
                + "<li>Có hóa đơn mua hàng hoặc thông tin đơn hàng trên hệ thống.</li>"
                + "</ul>"
                + "</td>"
                + "</tr>"
                + "</table>"

                + "<div style='background-color: #FFF5F5; padding: 15px; border-radius: 10px; margin-top: 20px; border: 1px solid #FFCCCC;'>"
                + "<h3 style='color: #C0392B; margin-top: 0;'>⚠️ 3. CÁC TRƯỜNG HỢP TỪ CHỐI BẢO HÀNH</h3>"
                + "<ul style='margin-bottom: 0;'>"
                + "<li>Sản phẩm bị hư hỏng do tác động vật lý (rơi, vỡ, cấn, móp).</li>"
                + "<li>Sản phẩm bị vào nước, hóa chất, hoặc có dấu hiệu côn trùng xâm nhập.</li>"
                + "<li>Hư hỏng do sử dụng sai nguồn điện (VD: Dùng sạc nhanh điện thoại cắm cho phím).</li>"
                + "<li>Tự ý tháo mở, sửa chữa, can thiệp vào mạch điện (trừ thao tác thay switch trên phím Hotswap).</li>"
                + "</ul>"
                + "</div>"

                + "<div style='margin-top: 25px;'>"
                + "<h3 style='color: #3E362E; border-bottom: 2px solid #E74C3C; padding-bottom: 5px;'>4. QUY TRÌNH & ĐỊA CHỈ</h3>"
                + "<p><b>Bước 1:</b> Liên hệ Fanpage hoặc Hotline <b>0909.123.456</b> để báo lỗi.</p>"
                + "<p><b>Bước 2:</b> Gửi sản phẩm về trung tâm bảo hành:</p>"
                + "<blockquote style='background-color: #F9F9F9; padding: 10px; border-left: 4px solid #3E362E;'>"
                + "<b>KEYFORGE ARTISAN STORE</b><br>"
                + "Địa chỉ: 123 Đường ABC, Quận 1, TP. Hồ Chí Minh<br>"
                + "Người nhận: Bộ phận Bảo Hành - 0909.123.456"
                + "</blockquote>"
                + "<p><b>Bước 3:</b> Kỹ thuật viên kiểm tra và xử lý trong vòng <b>3-7 ngày làm việc</b>.</p>"
                + "</div>"
                + "</body></html>";

        showPolicyDetail("CHÍNH SÁCH BẢO HÀNH", content);
    }

    private JPanel createDetailPolicyItem(String iconName, String title, String sub) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Color.WHITE);

        JLabel lblIcon = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/" + iconName));
            Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblIcon.setText("✓");
            lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblIcon.setForeground(successGreen);
        }

        JPanel textP = new JPanel(new GridLayout(2, 1));
        textP.setBackground(Color.WHITE);
        JLabel l1 = new JLabel(title);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel l2 = new JLabel(sub);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l2.setForeground(Color.GRAY);

        textP.add(l1);
        textP.add(l2);

        p.add(lblIcon);
        p.add(textP);
        return p;
    }

    // --- XỬ LÝ YÊU THÍCH ---
    private void toggleWishlist(Product p, JLabel lblHeart) {
        if (isGuest || !(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để sử dụng tính năng Yêu thích!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cusId = ((Customer) currentUser).getCustomerId();
        // SỬA LỖI: Logic toggle wishlist an toàn hơn
        if (lblHeart.getText().equals("♥")) {
            // Đang thích -> Bỏ thích
            wishlistDAO.deleteByCustomerAndProduct(cusId, p.getId());
            lblHeart.setText("♡");
            JOptionPane.showMessageDialog(this, "Đã xóa khỏi danh sách yêu thích.");
        } else {
            // Chưa thích -> Thêm thích
            // Lưu ý: Constructor Wishlist(cusId, productId) phải tồn tại trong Model
            wishlistDAO.insert(new Wishlist(cusId, p.getId()));
            lblHeart.setText("♥");
            JOptionPane.showMessageDialog(this, "Đã thêm vào danh sách yêu thích!");
        }
    }

    @Override
    public void refresh() {
        updateCartCount();
    }
}
