package com.keycapstore.gui;

import com.keycapstore.bus.EmployeeBUS;
import com.keycapstore.bus.ProductBUS;
import com.keycapstore.bus.ReviewBUS;
import com.keycapstore.bus.SalesBUS;
import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import com.keycapstore.model.InvoiceDetail;
import com.keycapstore.model.Product;
import com.keycapstore.model.ShippingMethod;
import com.keycapstore.model.Voucher;
import com.keycapstore.utils.ExportHelper;
import com.keycapstore.utils.ImageHelper;
import com.keycapstore.utils.ThemeColor;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.AbstractCellEditor;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class CartPanel extends JPanel implements Refreshable {

    public static ArrayList<InvoiceDetail> cartItems = new ArrayList<>();

    private Object currentUser;
    private Consumer<String> onNavigate;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblSubTotal, lblDiscount, lblShippingFee, lblFinalTotal;
    private JComboBox<Voucher> cbVoucher; // Thay JTextField bằng JComboBox
    private JTextField txtAddress;
    private JComboBox<ShippingMethod> cbShipping;
    private JButton btnApplyVoucher;
    private JLabel lblVoucherStt;

    private ProductBUS productBUS;
    private ReviewBUS reviewBUS;
    private SalesBUS salesBus;
    private EmployeeBUS employeeBUS;
    private DecimalFormat df = new DecimalFormat("#,###");
    private Voucher currentVoucher = null;

    private Color primaryColor = new Color(62, 54, 46);
    private Color borderColor = new Color(153, 143, 133);
    private Color textPrimary = new Color(51, 51, 51);
    private Color successGreen = new Color(46, 204, 113);
    private Color dangerRed = new Color(231, 76, 60);

    static {
        loadCartFromFile();
    }

    public CartPanel(Object user, Consumer<String> onNavigate) {
        this.currentUser = user;
        this.onNavigate = onNavigate;
        this.salesBus = new SalesBUS();
        this.productBUS = new ProductBUS();
        this.reviewBUS = new ReviewBUS();
        this.employeeBUS = new EmployeeBUS();

        if (cartItems == null)
            cartItems = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("GIỎ HÀNG CỦA BẠN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        String[] headers = { "ID", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá", "Thành Tiền", "" };
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // FIX: Cho phép sửa cột Số lượng (index 2) và cột Button (index 5)
                return column == 2 || column == 5;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 2) { // Cột Số Lượng
                    try {
                        String input = aValue.toString().trim();
                        if (input.isEmpty())
                            return;

                        int newQty = Integer.parseInt(input);
                        if (newQty <= 0) {
                            JOptionPane.showMessageDialog(CartPanel.this, "Số lượng phải lớn hơn 0!");
                            return; // Không cập nhật
                        }

                        InvoiceDetail item = cartItems.get(row);
                        Product p = productBUS.getProductById(item.getProductId());

                        if (p != null && newQty > p.getStock()) {
                            JOptionPane.showMessageDialog(CartPanel.this,
                                    "Vượt quá tồn kho! (Kho còn: " + p.getStock() + ")");
                            return;
                        }

                        item.setQuantity(newQty);
                        super.setValueAt(newQty, row, column);
                        super.setValueAt(df.format(item.getTotal()) + " ₫", row, 4); // Cập nhật cột Thành Tiền
                        updateMoney();
                        saveCartToFile();

                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(CartPanel.this, "Vui lòng nhập số nguyên hợp lệ!");
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(ThemeColor.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row != -1 && col != 2 && col != table.getColumnCount() - 1) {
                    showProductDetailsForSelectedRow(row);
                }
            }
        });

        new ButtonColumn(table, headers.length - 1);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createTitledBorder("THÔNG TIN THANH TOÁN"));
        rightPanel.setPreferredSize(new Dimension(350, 0));

        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        pnlInfo.add(new JLabel("Địa chỉ giao hàng:"), gbc);
        gbc.gridy++;
        txtAddress = new JTextField();
        if (currentUser instanceof Customer) {
            txtAddress.setText(((Customer) currentUser).getAddress());
        }
        pnlInfo.add(txtAddress, gbc);

        gbc.gridy++;
        pnlInfo.add(new JLabel("Vận chuyển:"), gbc);
        gbc.gridy++;
        cbShipping = new JComboBox<>();
        loadShippingMethods();
        cbShipping.addActionListener(e -> updateMoney());
        pnlInfo.add(cbShipping, gbc);

        gbc.gridy++;
        pnlInfo.add(new JLabel("Mã giảm giá:"), gbc);
        gbc.gridy++;
        JPanel pnlVoucher = new JPanel(new BorderLayout(5, 0));
        pnlVoucher.setBackground(Color.WHITE);

        cbVoucher = new JComboBox<>();
        loadVouchers(); // Load danh sách voucher

        // Thêm sự kiện hiển thị trạng thái voucher khi chọn
        cbVoucher.addActionListener(e -> {
            Voucher selected = (Voucher) cbVoucher.getSelectedItem();
            if (selected != null) {
                if (selected.getId() != -1) {
                    lblVoucherStt.setText("Sẵn sàng: Còn " + selected.getQuantity() + " lượt (Bấm Áp dụng)");
                    lblVoucherStt.setForeground(new Color(0, 102, 204));
                } else {
                    lblVoucherStt.setText(" ");
                }
                currentVoucher = null;
                updateMoney();
            }
        });

        btnApplyVoucher = new JButton("Áp dụng");
        btnApplyVoucher.addActionListener(e -> applyVoucher());
        pnlVoucher.add(cbVoucher, BorderLayout.CENTER);
        pnlVoucher.add(btnApplyVoucher, BorderLayout.EAST);
        pnlInfo.add(pnlVoucher, gbc);

        gbc.gridy++;
        lblVoucherStt = new JLabel(" ");
        lblVoucherStt.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        pnlInfo.add(lblVoucherStt, gbc);

        gbc.gridy++;
        JPanel pnlMath = new JPanel(new GridLayout(4, 1, 0, 5));
        pnlMath.setBackground(Color.WHITE);
        pnlMath.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        lblSubTotal = new JLabel("Tiền hàng: 0 ₫");
        lblDiscount = new JLabel("Giảm giá: 0 ₫");
        lblDiscount.setForeground(ThemeColor.SUCCESS);
        lblShippingFee = new JLabel("Phí ship: 0 ₫");
        lblFinalTotal = new JLabel("TỔNG CỘNG: 0 ₫");
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFinalTotal.setForeground(ThemeColor.DANGER);

        pnlMath.add(lblSubTotal);
        pnlMath.add(lblDiscount);
        pnlMath.add(lblShippingFee);
        pnlMath.add(lblFinalTotal);

        gbc.insets = new Insets(20, 5, 5, 5);
        pnlInfo.add(pnlMath, gbc);

        gbc.gridy++;
        JButton btnCheckout = new JButton("ĐẶT HÀNG");
        btnCheckout.setBackground(ThemeColor.SUCCESS);
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCheckout.setPreferredSize(new Dimension(0, 45));
        btnCheckout.addActionListener(e -> checkout());
        pnlInfo.add(btnCheckout, gbc);

        rightPanel.add(pnlInfo, BorderLayout.NORTH);
        add(rightPanel, BorderLayout.EAST);

        loadCartData();
    }

    public CartPanel(Object user) {
        this(user, null);
    }

    private void loadShippingMethods() {
        cbShipping.removeAllItems();
        ArrayList<ShippingMethod> ships = salesBus.getAllShipping();
        if (ships != null && !ships.isEmpty()) {
            for (ShippingMethod s : ships) {
                if (s.getPrice() > 0)
                    cbShipping.addItem(s);
            }
        }

        if (cbShipping.getItemCount() == 0) {
            cbShipping.addItem(new ShippingMethod(0, "Giao hàng tiêu chuẩn", 30000));
            cbShipping.addItem(new ShippingMethod(0, "Giao hàng hỏa tốc", 50000));
        }

        if (cbShipping.getItemCount() > 0) {
            cbShipping.setSelectedIndex(0);
        }
    }

    private void loadVouchers() {
        cbVoucher.removeAllItems();
        Voucher defaultV = new Voucher();
        defaultV.setId(-1);
        defaultV.setCode("Chọn mã giảm giá");
        defaultV.setDiscountPercent(0);
        cbVoucher.addItem(defaultV);

        ArrayList<Voucher> vouchers = salesBus.getActiveVouchers();
        for (Voucher v : vouchers) {
            cbVoucher.addItem(v);
        }
    }

    private void applyVoucher() {
        Voucher selected = (Voucher) cbVoucher.getSelectedItem();

        if (selected == null || selected.getId() == -1) {
            currentVoucher = null;
            lblVoucherStt.setText(" ");
            updateMoney();
            return;
        }

        Voucher v = salesBus.getValidVoucher(selected.getCode());
        if (v != null) {
            currentVoucher = v;
            lblVoucherStt.setText("Đã áp dụng: Giảm " + v.getDiscountPercent() + "%");
            lblVoucherStt.setForeground(ThemeColor.SUCCESS);
            JOptionPane.showMessageDialog(this, "Áp dụng mã giảm giá thành công!");
        } else {
            currentVoucher = null;
            lblVoucherStt.setText("Mã không hợp lệ hoặc hết hạn!");
            lblVoucherStt.setForeground(ThemeColor.DANGER);
            loadVouchers(); // Tải lại nếu mã hết hạn
        }
        updateMoney();
    }

    public static void addToCart(Product p, int qty) {
        boolean found = false;
        for (InvoiceDetail item : cartItems) {
            if (item.getProductId() == p.getId()) {
                item.setQuantity(item.getQuantity() + qty);
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(new InvoiceDetail(p.getId(), p.getName(), qty, p.getPrice()));
        }
        saveCartToFile();
    }

    private void loadCartData() {
        model.setRowCount(0);
        for (InvoiceDetail item : cartItems) {
            model.addRow(new Object[] {
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    df.format(item.getPrice()),
                    df.format(item.getTotal()) + " ₫",
                    "Xóa"
            });
        }
        updateMoney();
    }

    private void updateMoney() {
        double subTotal = 0;
        for (InvoiceDetail item : cartItems) {
            subTotal += item.getTotal();
        }

        double discountPercent = (currentVoucher != null) ? currentVoucher.getDiscountPercent() : 0;

        double discountAmount = subTotal * (discountPercent / 100.0);

        ShippingMethod sm = (ShippingMethod) cbShipping.getSelectedItem();
        double shippingFee = (sm != null) ? sm.getPrice() : 0;

        lblSubTotal.setText("Tiền hàng: " + df.format(subTotal) + " ₫");
        lblDiscount.setText("Giảm giá: -" + df.format(discountAmount) + " ₫");
        lblShippingFee.setText("Phí ship: +" + df.format(shippingFee) + " ₫");
        lblFinalTotal.setText("TỔNG CỘNG: " + df.format(subTotal - discountAmount + shippingFee) + " ₫");
    }

    private void removeItem(int row) {
        cartItems.remove(row);
        saveCartToFile();
        loadCartData();
    }

    private void clearCart() {
        cartItems.clear();
        saveCartToFile();
        loadCartData();
    }

    private void updateCartCount() {
        loadCartData();
    }

    private void showProductDetailsForSelectedRow(int row) {
        if (row >= 0 && row < cartItems.size()) {
            InvoiceDetail item = cartItems.get(row);
            Product p = productBUS.getProductById(item.getProductId());
            if (p != null) {
                p.setName(item.getProductName());
                showProductDetail(p);
            }
        }
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
            return;
        }
        if (!(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để thanh toán!");
            return;
        }

        Customer cus = (Customer) currentUser;
        String address = txtAddress.getText().trim();
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ giao hàng!");
            return;
        }

        String totalStr = lblFinalTotal.getText().replaceAll("[^0-9]", "");
        double finalTotal = Double.parseDouble(totalStr);

        int voucherId = (currentVoucher != null) ? currentVoucher.getId() : 0;
        ShippingMethod sm = (ShippingMethod) cbShipping.getSelectedItem();
        int shipMethodId = (sm != null) ? sm.getId() : 0;

        int systemEmpId = 1;
        ArrayList<Employee> employees = employeeBUS.getAllEmployees();
        if (employees != null && !employees.isEmpty()) {
            systemEmpId = employees.get(0).getEmployeeId();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: Chưa có nhân viên nào trong cơ sở dữ liệu để xử lý đơn hàng.\n" +
                            "Vui lòng liên hệ quản trị viên tạo ít nhất 1 nhân viên.",
                    "Lỗi cấu hình", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = salesBus.superCheckout(systemEmpId, cus.getCustomerId(), cus.getFullName(), cus.getPhone(),
                finalTotal, cartItems, voucherId, "Chuyển khoản", true, address, shipMethodId);

        if (success) {
            JOptionPane.showMessageDialog(this, "Đặt hàng thành công! Cảm ơn bạn đã mua sắm.");

            clearCart();
            if (cbVoucher.getItemCount() > 0)
                cbVoucher.setSelectedIndex(0); // Reset voucher
        } else {
            JOptionPane.showMessageDialog(this, "Đặt hàng thất bại. Vui lòng thử lại.");
        }
    }

    @Override
    public void refresh() {
        loadCartData();
        loadVouchers();
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
        JLabel lblBrand = new JLabel(p.getMakerName() != null ? p.getMakerName() : "Keyforge");
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

        java.util.List<com.keycapstore.model.Review> reviews = reviewBUS.getReviewsByProduct(p.getId());

        if (reviews == null || reviews.isEmpty()) {
            JLabel lblNoReview = new JLabel("Chưa có đánh giá nào cho sản phẩm này.", SwingConstants.CENTER);
            lblNoReview.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblNoReview.setForeground(Color.GRAY);
            pnlReviews.add(lblNoReview, BorderLayout.CENTER);
        } else {
            double avgRating = reviewBUS.getAverageRating(p.getId());
            JPanel pnlHeaderReview = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlHeaderReview.setBackground(Color.WHITE);
            JLabel lblAvg = new JLabel(String.format("%.1f/5 ★", avgRating));
            lblAvg.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblAvg.setForeground(new Color(255, 200, 0)); // Vàng
            JLabel lblCount = new JLabel("(" + reviews.size() + " đánh giá)");
            lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            pnlHeaderReview.add(lblAvg);
            pnlHeaderReview.add(lblCount);
            pnlReviews.add(pnlHeaderReview, BorderLayout.NORTH);

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

    class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JTable table;
        private JButton renderButton;
        private JButton editButton;

        public ButtonColumn(JTable table, int column) {
            super();
            this.table = table;
            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);

            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            tableColumn.setCellRenderer(this);
            tableColumn.setCellEditor(this);
            tableColumn.setPreferredWidth(60);
            tableColumn.setMaxWidth(60);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            renderButton.setText("Xóa");
            renderButton.setBackground(dangerRed);
            renderButton.setForeground(Color.WHITE);
            renderButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            return renderButton;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            editButton.setText("Xóa");
            editButton.setBackground(dangerRed);
            editButton.setForeground(Color.WHITE);
            editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            return editButton;
        }

        @Override
        public Object getCellEditorValue() {
            return "Xóa";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getEditingRow();
            fireEditingStopped();
            if (row != -1) {
                int editingRow = table.convertRowIndexToModel(row);
                removeItem(editingRow);
            }
        }
    }

    private static final String CART_FILE = "cart_data.txt";

    private static void saveCartToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CART_FILE))) {
            for (InvoiceDetail item : cartItems) {
                String line = item.getProductId() + "|" +
                        item.getProductName() + "|" +
                        item.getQuantity() + "|" +
                        item.getPrice();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadCartFromFile() {
        File f = new File(CART_FILE);
        if (!f.exists())
            return;

        if (cartItems == null)
            cartItems = new ArrayList<>();
        cartItems.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    int qty = Integer.parseInt(parts[2]);
                    double price = Double.parseDouble(parts[3]);
                    cartItems.add(new InvoiceDetail(id, name, qty, price));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            cartItems.clear();
        }
    }
}
