package com.keycapstore.gui;

import com.keycapstore.bus.CustomerBUS;
import com.keycapstore.bus.ProductBUS;
import com.keycapstore.bus.SalesBUS;
import com.keycapstore.utils.ExportHelper;
import com.keycapstore.model.*;
import com.keycapstore.utils.ThemeColor;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class SalesPanel extends JPanel implements Refreshable {

    private Employee currentUser;
    private ProductBUS productBus;
    private SalesBUS salesBus;
    private CustomerBUS customerBus;

    // Dữ liệu tạm thời của phiên giao dịch
    private ArrayList<Product> listProducts;
    private ArrayList<Product> displayedProducts; // Danh sách đang hiển thị (đã lọc)
    private ArrayList<InvoiceDetail> cartItems;
    private Customer currentCustomer = null; // Khách mặc định (NULL = Vãng lai)
    private Voucher currentVoucher = null; // Voucher đang áp dụng

    // UI Components
    private JTable tbProducts, tbCart;
    private DefaultTableModel modProducts, modCart;
    private JTextField txtPhone, txtVoucher;
    private JTextArea txtAddress; // Thêm trường địa chỉ
    private JRadioButton rdAtStore, rdDelivery; // Thêm lựa chọn giao hàng
    private JLabel lblCustomerName, lblRank, lblVoucherStt;
    private JComboBox<ShippingMethod> cbShipping;
    private JComboBox<String> cbPayment;
    private JLabel lblSubTotal, lblDiscount, lblShippingFee, lblFinalTotal;
    private JButton btnCheckout, btnClearCart, btnAdd;
    private JTextField txtSearch;
    private JComboBox<String> cbSort;
    private JSpinner spnQuantity;

    private DecimalFormat df = new DecimalFormat("#,###");

    public SalesPanel(Employee user) {
        this.currentUser = user;
        this.productBus = new ProductBUS();
        this.salesBus = new SalesBUS();
        this.customerBus = new CustomerBUS();
        this.cartItems = new ArrayList<>();
        this.displayedProducts = new ArrayList<>();

        MouseAdapter outsideClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tbProducts.clearSelection();
                tbCart.clearSelection();
            }
        };
        this.addMouseListener(outsideClick);

        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        splitPane.addMouseListener(outsideClick);

        // ==========================================
        // 1. BÊN TRÁI: KHO HÀNG (Nhặt hàng)
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createTitledBorder("📦 DANH SÁCH KEYCAP (Click đúp để thêm)"));
        leftPanel.addMouseListener(outsideClick);

        // --- THÊM BỘ LỌC VÀ TÌM KIẾM ---
        JPanel filterPanel = new JPanel(new BorderLayout(5, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
        filterPanel.addMouseListener(outsideClick);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm tên hoặc ID...");

        cbSort = new JComboBox<>(new String[] { "Tất cả (Mặc định)", "Giá: Thấp -> Cao", "Giá: Cao -> Thấp",
                "Tồn kho: Thấp -> Cao", "Tồn kho: Cao -> Thấp", "Loại: Switch", "Loại: Keycap Set",
                "Loại: Artisan Keycap" });
        cbSort.setFocusable(false);

        filterPanel.add(txtSearch, BorderLayout.CENTER);
        filterPanel.add(cbSort, BorderLayout.EAST);

        leftPanel.add(filterPanel, BorderLayout.NORTH);

        String[] prodHeaders = { "ID", "Tên Sản Phẩm", "Danh Mục", "Giá (VNĐ)", "Tồn Kho" };
        modProducts = new DefaultTableModel(prodHeaders, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbProducts = new JTable(modProducts);
        tbProducts.setRowHeight(30);
        tbProducts.getTableHeader().setBackground(ThemeColor.PRIMARY);
        tbProducts.getTableHeader().setForeground(Color.WHITE);
        tbProducts.setSelectionBackground(new Color(255, 224, 178)); // Màu cam nhạt đồng bộ
        tbProducts.setSelectionForeground(Color.BLACK);

        tbProducts.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (tbProducts.rowAtPoint(e.getPoint()) == -1) {
                    tbProducts.clearSelection();
                }
                if (e.getClickCount() == 2 && tbProducts.getSelectedRow() != -1) {
                    addToCart(tbProducts.getSelectedRow());
                }
            }
        });

        leftPanel.add(new JScrollPane(tbProducts), BorderLayout.CENTER);

        // --- PANEL SỐ LƯỢNG & THÊM ---
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quantityPanel.setBackground(Color.WHITE);
        quantityPanel.addMouseListener(outsideClick);
        quantityPanel.add(new JLabel("Số lượng:"));
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQuantity.setPreferredSize(new Dimension(60, 30));
        quantityPanel.add(spnQuantity);

        btnAdd = createButton("THÊM", ThemeColor.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(80, 30));
        quantityPanel.add(btnAdd);
        leftPanel.add(quantityPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        // ==========================================
        // 2. BÊN PHẢI: GIỎ HÀNG & CHỐT ĐƠN (Siêu POS)
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.addMouseListener(outsideClick);

        // 2.1 GIỎ HÀNG (Ở TRÊN)
        String[] cartHeaders = { "Tên SP", "SL", "Đơn Giá", "Thành Tiền" };
        modCart = new DefaultTableModel(cartHeaders, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbCart = new JTable(modCart);
        tbCart.setRowHeight(30);
        tbCart.getTableHeader().setBackground(ThemeColor.INFO);
        tbCart.getTableHeader().setForeground(Color.WHITE);
        tbCart.setSelectionBackground(new Color(255, 224, 178)); // Màu cam nhạt đồng bộ
        tbCart.setSelectionForeground(Color.BLACK);

        tbCart.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (tbCart.rowAtPoint(e.getPoint()) == -1) {
                    tbCart.clearSelection();
                }
                if (e.getClickCount() == 2 && tbCart.getSelectedRow() != -1) {
                    removeFromCart(tbCart.getSelectedRow());
                }
            }
        });

        // 2.2 KHU VỰC ĐIỀN THÔNG TIN & TÍNH TIỀN (Ở DƯỚI)
        JPanel pnlCheckout = new JPanel(new GridBagLayout());
        pnlCheckout.setBackground(Color.WHITE);
        // Xóa border cũ để giao diện sạch hơn khi đưa vào ScrollPane
        pnlCheckout.setBorder(new EmptyBorder(10, 10, 10, 10));

        pnlCheckout.addMouseListener(outsideClick);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // --- Dòng 1: Khách Hàng ---
        pnlCheckout.add(new JLabel("SĐT Khách:"), gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(12);
        pnlCheckout.add(txtPhone, gbc);
        gbc.gridx = 2;
        JButton btnCheckPhone = new JButton("Tìm");
        pnlCheckout.add(btnCheckPhone, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        lblCustomerName = new JLabel("Khách Vãng Lai");
        lblCustomerName.setForeground(ThemeColor.PRIMARY);
        lblRank = new JLabel("Hạng: Đồng (0%)");
        JPanel pnlCusInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlCusInfo.setBackground(Color.WHITE);
        pnlCusInfo.add(lblCustomerName);
        pnlCusInfo.add(lblRank);
        pnlCheckout.add(pnlCusInfo, gbc);

        // --- Dòng 2: Voucher ---
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        pnlCheckout.add(new JLabel("Mã Giảm Giá:"), gbc);
        gbc.gridx = 1;
        txtVoucher = new JTextField(12);
        pnlCheckout.add(txtVoucher, gbc);
        gbc.gridx = 2;
        JButton btnApplyVoucher = new JButton("Áp dụng");
        pnlCheckout.add(btnApplyVoucher, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        lblVoucherStt = new JLabel("Chưa áp dụng mã");
        lblVoucherStt.setForeground(Color.GRAY);
        pnlCheckout.add(lblVoucherStt, gbc);

        // --- Dòng 3: Thanh Toán (Được dời lên trên) ---
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        pnlCheckout.add(new JLabel("Thanh Toán:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cbPayment = new JComboBox<>(new String[] { "Tiền mặt", "Chuyển khoản (Momo/Bank)" });
        pnlCheckout.add(cbPayment, gbc);

        // --- Dòng 4: Hình thức giao hàng (Radio Button) ---
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        pnlCheckout.add(new JLabel("Hình thức:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlRadio.setBackground(Color.WHITE);

        ButtonGroup grpShip = new ButtonGroup();
        rdAtStore = new JRadioButton("Mua tại chỗ");
        rdDelivery = new JRadioButton("Đơn ship");
        rdAtStore.setBackground(Color.WHITE);
        rdDelivery.setBackground(Color.WHITE);
        rdAtStore.setSelected(true); // Mặc định mua tại chỗ

        grpShip.add(rdAtStore);
        grpShip.add(rdDelivery);
        pnlRadio.add(rdAtStore);
        pnlRadio.add(Box.createHorizontalStrut(15));
        pnlRadio.add(rdDelivery);
        pnlCheckout.add(pnlRadio, gbc);

        // --- Dòng 5: Địa chỉ (Chỉ hiện khi ship) ---
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        pnlCheckout.add(new JLabel("Địa chỉ:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtAddress = new JTextArea(2, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtAddress.setEnabled(false); // Mặc định disable
        pnlCheckout.add(txtAddress, gbc);

        // --- Dòng 6: Vận chuyển (Đổi tên label & Fix lỗi hiển thị) ---
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        pnlCheckout.add(new JLabel("Vận chuyển:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cbShipping = new JComboBox<>();
        loadShippingMethods(); // Tải dữ liệu lần đầu
        cbShipping.setEnabled(false); // Mặc định disable
        pnlCheckout.add(cbShipping, gbc);

        // --- Dòng 7: BẢNG TÍNH TIỀN ---
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        JPanel pnlMath = new JPanel(new GridLayout(4, 1, 0, 5));
        pnlMath.setBackground(Color.WHITE);
        pnlMath.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        lblSubTotal = new JLabel("Tiền Hàng: 0 ₫");
        lblDiscount = new JLabel("Giảm Giá: 0 ₫");
        lblDiscount.setForeground(ThemeColor.SUCCESS);
        lblShippingFee = new JLabel("Phí Ship: 0 ₫");
        lblFinalTotal = new JLabel("TỔNG PHẢI TRẢ: 0 ₫");
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFinalTotal.setForeground(ThemeColor.DANGER);

        pnlMath.add(lblSubTotal);
        pnlMath.add(lblDiscount);
        pnlMath.add(lblShippingFee);
        pnlMath.add(lblFinalTotal);
        pnlCheckout.add(pnlMath, gbc);

        // --- NÚT CHỐT ĐƠN ---
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnClearCart = createButton("HỦY GIỎ HÀNG", ThemeColor.WARNING);
        btnCheckout = createButton("THANH TOÁN (F12)", ThemeColor.SUCCESS);
        btnPanel.add(btnClearCart);
        btnPanel.add(btnCheckout);

        gbc.gridy++;
        gbc.insets = new Insets(15, 5, 5, 5);
        pnlCheckout.add(btnPanel, gbc);

        // --- CẤU TRÚC LẠI RIGHT PANEL VỚI SPLIT PANE ---
        // Dùng SplitPane dọc: Trên là Giỏ hàng, Dưới là Form thanh toán (có Scrollbar)
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setResizeWeight(0.4); // Giỏ hàng chiếm 40%
        rightSplit.setDividerSize(3);
        rightSplit.setBorder(null);

        JPanel pnlCartWrapper = new JPanel(new BorderLayout());
        pnlCartWrapper.add(new JScrollPane(tbCart), BorderLayout.CENTER);
        pnlCartWrapper.setBorder(BorderFactory.createTitledBorder("🛒 GIỎ HÀNG"));

        JScrollPane scrollCheckout = new JScrollPane(pnlCheckout);
        scrollCheckout.setBorder(BorderFactory.createTitledBorder("THÔNG TIN THANH TOÁN"));
        scrollCheckout.getVerticalScrollBar().setUnitIncrement(16);

        rightSplit.setTopComponent(pnlCartWrapper);
        rightSplit.setBottomComponent(scrollCheckout);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        setupActions();
        loadProducts();

        // Nút tìm SĐT
        btnCheckPhone.addActionListener(e -> checkPhone());

        // Nút áp Voucher
        btnApplyVoucher.addActionListener(e -> applyVoucher());

        // Thay đổi loại ship -> Tính lại tiền
        cbShipping.addActionListener(e -> updateMoney());

        // --- SỰ KIỆN RADIO BUTTON ---
        rdAtStore.addActionListener(e -> {
            cbShipping.setEnabled(false);
            txtAddress.setEnabled(false);
            txtAddress.setBackground(new Color(240, 240, 240)); // Màu xám
            updateMoney();
        });

        rdDelivery.addActionListener(e -> {
            cbShipping.setEnabled(true);
            txtAddress.setEnabled(true);
            loadShippingMethods(); // FIX: Tải lại danh sách vận chuyển khi chọn Đơn ship
            txtAddress.setBackground(Color.WHITE);
            updateMoney();
        });
    }

    @Override
    public void refresh() {
        loadProducts();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    private void loadProducts() {
        listProducts = productBus.getAllProducts();
        filterAndSort(); // Gọi hàm lọc thay vì add trực tiếp
    }

    private void loadShippingMethods() {
        cbShipping.removeAllItems(); // FIX: Xóa danh sách cũ trước khi tải mới để tránh trùng lặp
        ArrayList<ShippingMethod> ships = salesBus.getAllShipping();

        if (ships == null || ships.isEmpty()) {
            // Fallback: Nếu DB chưa có dữ liệu hoặc lỗi kết nối, thêm dữ liệu cứng để không
            // bị lỗi giao diện
            cbShipping.addItem(new ShippingMethod(0, "Giao hàng tiêu chuẩn", 30000));
            cbShipping.addItem(new ShippingMethod(0, "Giao hàng hỏa tốc", 50000));
        } else {
            for (ShippingMethod s : ships) {
                // Chỉ thêm các phương thức có phí > 0 (vì phí = 0 là Mua tại chỗ)
                if (s.getPrice() > 0) {
                    cbShipping.addItem(s);
                }
            }
        }
        // Chọn mặc định phần tử đầu tiên nếu có
        if (cbShipping.getItemCount() > 0)
            cbShipping.setSelectedIndex(0);
    }

    private void filterAndSort() {
        String keyword = txtSearch.getText().toLowerCase();
        int sortType = cbSort.getSelectedIndex();
        String selectedItem = (String) cbSort.getSelectedItem();

        // 1. Lọc dữ liệu
        displayedProducts = (ArrayList<Product>) listProducts.stream()
                .filter(p -> {
                    boolean matchKeyword = p.getName().toLowerCase().contains(keyword)
                            || String.valueOf(p.getId()).contains(keyword);
                    boolean matchCategory = true;
                    // Nếu chọn mục lọc theo loại
                    if (selectedItem != null && selectedItem.startsWith("Loại: ")) {
                        String targetCat = selectedItem.substring(6);
                        matchCategory = targetCat.equals(p.getCategoryName());
                    }
                    return matchKeyword && matchCategory;
                }).collect(Collectors.toList());

        // 2. Sắp xếp dữ liệu
        Comparator<Product> comparator = Comparator.comparingInt(Product::getId);
        switch (sortType) {
            case 1:
                comparator = Comparator.comparingDouble(Product::getPrice);
                break; // Giá thấp -> cao
            case 2:
                comparator = Comparator.comparingDouble(Product::getPrice).reversed();
                break; // Giá cao -> thấp
            case 3:
                comparator = Comparator.comparingInt(Product::getStock);
                break; // Tồn kho thấp -> cao
            case 4:
                comparator = Comparator.comparingInt(Product::getStock).reversed();
                break; // Tồn kho cao -> thấp
            // Các case > 4 là lọc, giữ nguyên sắp xếp mặc định
        }
        displayedProducts.sort(comparator);

        modProducts.setRowCount(0);
        for (Product p : displayedProducts) {
            modProducts.addRow(new Object[] {
                    p.getId(), p.getName(), p.getCategoryName(), df.format(p.getPrice()), p.getStock()
            });
        }
    }

    private void addToCart(int row) {
        // Lấy từ danh sách đang hiển thị (đã lọc) thay vì danh sách gốc
        if (row < 0 || row >= displayedProducts.size())
            return;
        Product p = displayedProducts.get(row);
        int qty = (int) spnQuantity.getValue();

        if (p.getStock() < qty) {
            JOptionPane.showMessageDialog(this, "Không đủ tồn kho! (Còn: " + p.getStock() + ")", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean found = false;
        for (InvoiceDetail item : cartItems) {
            if (item.getProductId() == p.getId()) {
                if (item.getQuantity() + qty > p.getStock()) {
                    JOptionPane.showMessageDialog(this, "Không đủ tồn kho!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(item.getQuantity() + qty);
                found = true;
                break;
            }
        }

        if (!found) {
            cartItems.add(new InvoiceDetail(p.getId(), p.getName(), qty, p.getPrice()));
        }

        updateCartUI();
    }

    private void removeFromCart(int row) {
        InvoiceDetail item = cartItems.get(row);
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cartItems.remove(row);
        }
        updateCartUI();
    }

    private void updateCartUI() {
        modCart.setRowCount(0);
        for (InvoiceDetail item : cartItems) {
            modCart.addRow(new Object[] {
                    item.getProductName(), item.getQuantity(), df.format(item.getPrice()), df.format(item.getTotal())
            });
        }
        updateMoney();
    }

    // Hàm trái tim: Tính lại toàn bộ tiền
    private void updateMoney() {
        double subTotal = 0;
        for (InvoiceDetail item : cartItems)
            subTotal += item.getTotal();

        double rankDiscountPercent = (currentCustomer != null) ? currentCustomer.getCurrentDiscount() : 0;
        double voucherDiscountPercent = (currentVoucher != null) ? currentVoucher.getDiscountPercent() : 0;

        // Trừ % của Rank + % của Voucher
        double totalDiscountPercent = rankDiscountPercent + voucherDiscountPercent;
        double discountAmount = subTotal * (totalDiscountPercent / 100.0);

        // Lấy tiền ship
        double shippingFee = 0;
        if (rdDelivery.isSelected()) {
            ShippingMethod ship = (ShippingMethod) cbShipping.getSelectedItem();
            shippingFee = (ship != null) ? ship.getPrice() : 0;
        } else {
            // Mua tại chỗ -> Free ship
            shippingFee = 0;
        }

        double finalTotal = subTotal - discountAmount + shippingFee;

        lblSubTotal.setText("Tiền Hàng: " + df.format(subTotal) + " ₫");
        lblDiscount.setText("Giảm Giá (" + totalDiscountPercent + "%): -" + df.format(discountAmount) + " ₫");
        lblShippingFee.setText("Phí Ship: +" + df.format(shippingFee) + " ₫");
        lblFinalTotal.setText("TỔNG PHẢI TRẢ: " + df.format(finalTotal) + " ₫");
    }

    private void checkPhone() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty())
            return;

        Customer c = customerBus.getCustomerByPhone(phone);
        if (c != null) {
            currentCustomer = c;
            lblCustomerName.setText(c.getFullName());
            lblRank.setText("Hạng: " + c.getRankName() + " (" + c.getCurrentDiscount() + "%)");

            // Tự động điền địa chỉ nếu có
            if (c.getAddress() != null && !c.getAddress().isEmpty()) {
                txtAddress.setText(c.getAddress());
            }
        } else {
            // FIX: Reset về khách vãng lai ngay lập tức nếu không tìm thấy
            currentCustomer = null;
            lblCustomerName.setText("Khách Vãng Lai");
            lblRank.setText("Hạng: Đồng (0%)");
            txtAddress.setText(""); // Reset địa chỉ

            // Khách mới
            int confirm = JOptionPane.showConfirmDialog(this, "SĐT này chưa có trong hệ thống. Tạo khách hàng mới?",
                    "Khách mới",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String name = JOptionPane.showInputDialog("Nhập tên khách hàng mới:");
                if (name != null && !name.isEmpty()) {
                    Customer newCus = new Customer();
                    newCus.setFullName(name);
                    newCus.setPhone(phone);

                    // Sử dụng hàm thêm nhanh (chỉ cần Tên + SĐT)
                    if (customerBus.addQuickCustomer(newCus)) {
                        currentCustomer = customerBus.getCustomerByPhone(phone); // Lấy lại để có ID và Rank mặc định
                        if (currentCustomer != null) {
                            lblCustomerName.setText(currentCustomer.getFullName());
                            String rName = currentCustomer.getRankName() != null ? currentCustomer.getRankName()
                                    : "Thành Viên";
                            lblRank.setText("Hạng: " + rName + " (" + currentCustomer.getCurrentDiscount() + "%)");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi: Không thể tạo khách hàng (SĐT có thể đã tồn tại).");
                    }
                }
            }
        }
        updateMoney();
    }

    private void applyVoucher() {
        String code = txtVoucher.getText().trim().toUpperCase(); // Chuyển về chữ hoa
        if (code.isEmpty()) {
            currentVoucher = null;
            lblVoucherStt.setText("Đã hủy mã");
            lblVoucherStt.setForeground(Color.GRAY);
            updateMoney();
            return;
        }

        Voucher v = salesBus.getValidVoucher(code);
        if (v != null) {
            currentVoucher = v;
            lblVoucherStt.setText(
                    "Đã áp dụng: Giảm " + v.getDiscountPercent() + "% (Còn " + v.getQuantity() + " lượt)");
            lblVoucherStt.setForeground(ThemeColor.SUCCESS);
            JOptionPane.showMessageDialog(this, "Áp dụng mã giảm giá thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            currentVoucher = null;
            lblVoucherStt.setText("Mã không hợp lệ hoặc đã hết hạn!");
            lblVoucherStt.setForeground(ThemeColor.DANGER);
        }
        updateMoney();
    }

    private void setupActions() {

        // Sự kiện tìm kiếm và sắp xếp
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterAndSort();
            }
        });
        cbSort.addActionListener(e -> filterAndSort());

        btnAdd.addActionListener(e -> {
            int row = tbProducts.getSelectedRow();
            if (row != -1)
                addToCart(row);
            else
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm!");
        });

        btnClearCart.addActionListener(e -> {
            cartItems.clear();
            updateCartUI();
            txtPhone.setText("");
            currentCustomer = null;
            currentVoucher = null;
            lblCustomerName.setText("Khách Vãng Lai");
            lblRank.setText("Hạng: Đồng (0%)");
            lblVoucherStt.setText("Chưa áp dụng mã");
            lblVoucherStt.setForeground(Color.GRAY);
            txtVoucher.setText("");
            txtAddress.setText("");
            rdAtStore.setSelected(true); // Reset về mua tại chỗ
            cbShipping.setEnabled(false);
            updateMoney();
        });

        btnCheckout.addActionListener(e -> processCheckout());

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F12"), "checkout");
        getActionMap().put("checkout", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                processCheckout();
            }
        });
    }

    private void processCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra logic Đơn Ship
        if (rdDelivery.isSelected()) {
            String addr = txtAddress.getText().trim();
            if (addr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ giao hàng!", "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                txtAddress.requestFocus();
                return;
            }
        }

        // LOGIC MỚI: Xử lý thông tin khách hàng thông minh hơn
        int cusId = 0;
        String cName = "Khách vãng lai";
        String cPhone = txtPhone.getText().trim();
        String deliveryAddress = txtAddress.getText().trim();

        if (currentCustomer != null) {
            // Trường hợp 1: Đã chọn khách hàng từ hệ thống
            cusId = currentCustomer.getCustomerId();
            cName = currentCustomer.getFullName();
            cPhone = currentCustomer.getPhone();
        } else if (!cPhone.isEmpty()) {
            // Trường hợp 2: Chưa chọn khách (currentCustomer null) nhưng CÓ nhập SĐT
            // Thử tìm ngầm xem SĐT này có trong DB không (phòng trường hợp quên bấm nút
            // Tìm)
            Customer tempC = customerBus.getCustomerByPhone(cPhone);
            if (tempC != null) {
                cusId = tempC.getCustomerId();
                cName = tempC.getFullName();
            }
            // Nếu không tìm thấy thì vẫn giữ cPhone để lưu vào hóa đơn (Khách vãng lai có
            // SĐT)
        }

        // LOGIC LƯU ĐỊA CHỈ: Nếu là đơn ship và có ID khách hàng (cũ hoặc mới tìm thấy)
        if (rdDelivery.isSelected() && cusId > 0 && !deliveryAddress.isEmpty()) {
            // Kiểm tra xem địa chỉ hiện tại trong DB có trống không hoặc khác không
            // Ở đây ta ưu tiên cập nhật luôn nếu khách nhập mới để lần sau dùng lại
            boolean needUpdate = true;
            if (currentCustomer != null && deliveryAddress.equals(currentCustomer.getAddress())) {
                needUpdate = false;
            }

            if (needUpdate) {
                // Cập nhật địa chỉ vào DB
                customerBus.updateAddress(cusId, deliveryAddress);
                // System.out.println("Đã cập nhật địa chỉ cho khách hàng ID: " + cusId);
            }
        }

        int voucherId = (currentVoucher != null) ? currentVoucher.getId() : 0;
        String payMethod = cbPayment.getSelectedItem().toString();

        // Lấy lại cái Tổng Tiền cuối cùng bằng cách chẻ chuỗi (Mẹo nhỏ khỏi khai báo
        // biến
        // toàn cục)
        String totalStr = lblFinalTotal.getText().replaceAll("[^0-9]", "");
        double finalTotal = Double.parseDouble(totalStr);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Thu của khách: " + df.format(finalTotal) + " ₫?",
                "Chốt Đơn", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Lấy thông tin ship
            boolean isShipping = rdDelivery.isSelected();
            String shipAddr = isShipping ? txtAddress.getText().trim() : null;
            int shipMethodId = 0;
            if (isShipping) {
                ShippingMethod sm = (ShippingMethod) cbShipping.getSelectedItem();
                if (sm != null)
                    shipMethodId = sm.getId();
            }

            boolean success = salesBus.superCheckout(currentUser.getEmployeeId(), cusId, cName, cPhone, finalTotal,
                    cartItems, voucherId, payMethod, isShipping, shipAddr, shipMethodId);

            if (success) {
                JOptionPane.showMessageDialog(this, "Giao dịch thành công! Đã tự động cộng điểm Rank.");

                // --- TỰ ĐỘNG IN HÓA ĐƠN PDF ---
                try {
                    // Tạo ID hóa đơn tạm thời cho tên file và nội dung
                    String invoiceIdForPdf = "HD" + System.currentTimeMillis();
                    String totalText = lblFinalTotal.getText(); // "TỔNG PHẢI TRẢ: 1,500,000 ₫"

                    // Lấy phần số tiền và đơn vị
                    String totalAmount = totalText.substring(totalText.indexOf(":") + 1).trim();

                    // HỎI NGƯỜI DÙNG NƠI LƯU FILE
                    String filePath = ExportHelper.promptSaveLocation(this, "HoaDon_" + invoiceIdForPdf + ".pdf", "pdf",
                            "Hóa đơn PDF");

                    if (filePath != null) {
                        ExportHelper.exportBillToPDF(tbCart, invoiceIdForPdf, totalAmount, filePath);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi xuất hóa đơn PDF: " + ex.getMessage(), "Lỗi PDF",
                            JOptionPane.ERROR_MESSAGE);
                }

                // Reset form
                try {
                    cartItems.clear();
                    currentCustomer = null;
                    currentVoucher = null;
                    txtPhone.setText("");
                    txtVoucher.setText("");
                    txtAddress.setText("");
                    lblCustomerName.setText("Khách Vãng Lai");
                    lblRank.setText("Hạng: Đồng (0%)");
                    lblVoucherStt.setText("Chưa áp dụng mã");
                    lblVoucherStt.setForeground(Color.GRAY);
                    if (cbShipping.getItemCount() > 0)
                        cbShipping.setSelectedIndex(0);
                    if (cbPayment.getItemCount() > 0)
                        cbPayment.setSelectedIndex(0);

                    rdAtStore.setSelected(true); // Reset về mặc định
                    cbShipping.setEnabled(false);

                    updateCartUI(); // Cập nhật giao diện giỏ hàng NGAY LẬP TỨC
                    loadProducts(); // Sau đó mới tải lại kho hàng
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi chốt đơn. Đã Rollback giao dịch!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
