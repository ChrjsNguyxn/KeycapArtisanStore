package com.keycapstore.gui;

import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import com.keycapstore.utils.MenuButton;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class MainFrame extends JFrame {

    private Object currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;

    private MenuButton btnDashboard, btnProduct, btnEmployee, btnCustomer, btnSales, btnLogout, btnRefresh, btnHistory,
            btnStockHistory, btnVoucher, btnShipping; // Thêm btnShipping

    public MainFrame(Object user) {
        this.currentUser = user;
        initUI();
    }

    public MainFrame() {
        this(null);
    }

    private void initUI() {
        String userName = "Khách";
        if (currentUser instanceof Employee) {
            userName = ((Employee) currentUser).getFullName();
        } else if (currentUser instanceof Customer) {
            userName = ((Customer) currentUser).getFullName();
        }

        setTitle("Keyforge Artisan Manager - Xin chào: " + userName);
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(ThemeColor.PRIMARY);
        sidebarPanel.setPreferredSize(new Dimension(220, 0));

        JLabel lblBrand = new JLabel("KEYFORGE");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBrand.setHorizontalAlignment(SwingConstants.CENTER);
        lblBrand.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        lblBrand.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblBrand.getPreferredSize().height));
        sidebarPanel.add(lblBrand);

        btnDashboard = createMenuBtn("Dashboard", "dashboard.png");
        btnProduct = createMenuBtn("Kho Hàng", "warehouse.png");
        btnSales = createMenuBtn("Bán Hàng", "sales.png");
        btnStockHistory = createMenuBtn("Lịch Sử Nhập", "invoice2.png");
        btnShipping = createMenuBtn("Vận Đơn", "shipping.png"); // Icon mới
        btnCustomer = createMenuBtn("Khách Hàng", "customer.png");
        btnHistory = createMenuBtn("Lịch Sử Đơn", "invoice.png");
        btnVoucher = createMenuBtn("Voucher", "ticket.png");
        btnEmployee = createMenuBtn("Nhân Sự", "employees.png");
        btnRefresh = createMenuBtn("Làm Mới", "reload.png");
        btnLogout = createMenuBtn("Đăng Xuất", "exit.png");

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeColor.BG_LIGHT);

        if (currentUser instanceof Employee) {
            contentPanel.add(new DashboardPanel(this::navigateTo), "DASHBOARD");
            contentPanel.add(new ProductPanel((Employee) currentUser), "PRODUCT");
            contentPanel.add(new SalesPanel((Employee) currentUser), "SALES");
        } else {
            contentPanel.add(new JPanel(), "SALES");
            contentPanel.add(new JPanel(), "PRODUCT");
        }
        contentPanel.add(new OrderHistoryPanel(), "HISTORY");
        contentPanel.add(new ShippingPanel(), "SHIPPING"); // Thêm Panel mới
        contentPanel.add(new CustomerPanel(), "CUSTOMER");
        contentPanel.add(new VoucherPanel(), "VOUCHER");

        boolean isSuperAdmin = false;
        if (currentUser instanceof Employee) {
            Employee emp = (Employee) currentUser;
            if ("super_admin".equals(emp.getRole())) {
                isSuperAdmin = true;
                contentPanel.add(new EmployeePanel(), "EMPLOYEE");
            }
        }

        if (currentUser instanceof Employee) {
            Employee emp = (Employee) currentUser;
            String role = emp.getRole();

            if ("super_admin".equals(role)) {
                sidebarPanel.add(btnDashboard);
                sidebarPanel.add(btnEmployee);
                sidebarPanel.add(btnCustomer);
                sidebarPanel.add(btnVoucher); // Chỉ Super Admin mới thấy
            }

            if ("super_admin".equals(role) || "warehouse_manager".equals(role)) {
                sidebarPanel.add(btnProduct);
                sidebarPanel.add(btnStockHistory);
            }

            if ("super_admin".equals(role) || "sales_manager".equals(role)) {
                sidebarPanel.add(btnSales);
                sidebarPanel.add(btnShipping); // Sales Manager được thấy Vận Đơn
            }
        } else {
            sidebarPanel.add(btnDashboard);
        }

        sidebarPanel.add(btnHistory);
        sidebarPanel.add(btnRefresh);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);

        cardLayout.show(contentPanel, "DASHBOARD");
        updateActiveButton(btnDashboard);

        if (isSuperAdmin) {
            cardLayout.show(contentPanel, "EMPLOYEE");
            updateActiveButton(btnEmployee);
        }

        btnDashboard.addActionListener(e -> navigateTo("DASHBOARD"));

        btnProduct.addActionListener(e -> navigateTo("PRODUCT"));

        btnSales.addActionListener(e -> navigateTo("SALES"));

        btnShipping.addActionListener(e -> navigateTo("SHIPPING"));

        btnCustomer.addActionListener(e -> navigateTo("CUSTOMER"));

        btnEmployee.addActionListener(e -> navigateTo("EMPLOYEE"));

        btnStockHistory.addActionListener(e -> navigateTo("STOCK_HISTORY"));

        btnHistory.addActionListener(e -> navigateTo("HISTORY"));

        btnVoucher.addActionListener(e -> navigateTo("VOUCHER"));

        btnRefresh.addActionListener(e -> refreshCurrentPage());

        btnLogout.addActionListener(e -> logout());

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setupRefreshShortcut();
        contentPanel.add(new StockEntryHistoryPanel(), "STOCK_HISTORY");
    }

    // Hàm điều hướng chung, được gọi từ Dashboard hoặc các nơi khác
    public void navigateTo(String cardName) {
        cardLayout.show(contentPanel, cardName);

        // Cập nhật trạng thái nút Menu tương ứng
        switch (cardName) {
            case "DASHBOARD":
                updateActiveButton(btnDashboard);
                break;
            case "PRODUCT":
                updateActiveButton(btnProduct);
                break;
            case "STOCK_HISTORY":
                updateActiveButton(btnStockHistory);
                break;
            case "SALES":
                updateActiveButton(btnSales);
                break;
            case "SHIPPING":
                updateActiveButton(btnShipping);
                break;
            case "HISTORY":
                updateActiveButton(btnHistory);
                break;
            case "CUSTOMER":
                updateActiveButton(btnCustomer);
                break;
            case "EMPLOYEE":
                updateActiveButton(btnEmployee);
                break;
            case "VOUCHER":
                updateActiveButton(btnVoucher);
                break;
        }

        // Refresh dữ liệu của trang đích nếu cần
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof Refreshable)
                ((Refreshable) comp).refresh();
        }
    }

    private void refreshCurrentPage() {

        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible()) {
                if (comp instanceof Refreshable) {
                    ((Refreshable) comp).refresh();

                    Toast("Đã làm mới dữ liệu!");
                }
                break;
            }
        }
    }

    private void setupRefreshShortcut() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "refreshApp");
        getRootPane().getActionMap().put("refreshApp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentPage();
            }
        });
    }

    private MenuButton createMenuBtn(String text, String iconName) {
        MenuButton btn = new MenuButton(text, iconName);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void updateActiveButton(MenuButton activeBtn) {

        btnDashboard.setSelected(false);
        btnProduct.setSelected(false);
        btnSales.setSelected(false);
        btnShipping.setSelected(false);
        btnStockHistory.setSelected(false);
        btnHistory.setSelected(false);
        if (btnCustomer != null)
            btnCustomer.setSelected(false);
        if (btnEmployee != null)
            btnEmployee.setSelected(false);
        btnVoucher.setSelected(false);
        btnRefresh.setSelected(false);

        if (activeBtn != null)
            activeBtn.setSelected(true);
    }

    private void logout() {
        this.dispose();
        new ModernLoginDialog().setVisible(true);
    }

    private void Toast(String msg) {

        System.out.println("Toast: " + msg);
    }
}