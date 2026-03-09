package com.keycapstore.gui;

import com.keycapstore.bus.ProductBUS;
import com.keycapstore.dao.EmployeeDAO;
import com.keycapstore.dao.ImportOrderDAO;
import com.keycapstore.dao.ProductDAO;
import com.keycapstore.dao.SupplierDAO;
import com.keycapstore.model.Employee;
import com.keycapstore.model.ImportOrderDTO;
import com.keycapstore.model.ImportOrderItemDTO;
import com.keycapstore.model.ProductDTO;
import com.keycapstore.model.SupplierDTO;
import com.keycapstore.utils.ThemeColor;
import com.keycapstore.gui.components.MultiImageInput; // Import component mới

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ImportOrderGUI extends JFrame {

    // Thay đổi: Nhập tay Supplier và Product
    private JTextField txtSupplierName;
    private JTextField txtProductName;
    private JTextField txtOrigin; // Thêm trường Xuất xứ
    private JComboBox<Employee> cbEmployee;

    private JTextField txtQuantity, txtImportPrice;
    private JTextArea txtNote;

    private JTable table;
    private DefaultTableModel model;

    private List<ImportOrderItemDTO> itemList = new ArrayList<>();

    private ImportOrderDAO importDAO = new ImportOrderDAO();
    private SupplierDAO supplierDAO = new SupplierDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ProductDAO productDAO = new ProductDAO();
    private ProductBUS productBUS = new ProductBUS();

    private ImportManagementPanel parent;

    private MultiImageInput pnlImages;

    private final Color PRIMARY_DARK = ThemeColor.PRIMARY;
    private final Color CREAM_LIGHT = ThemeColor.BG_LIGHT;
    private final Color TEXT_PRIMARY = Color.BLACK;

    public ImportOrderGUI(ImportManagementPanel parent) {

        this.parent = parent;

        setTitle("TẠO ĐƠN NHẬP HÀNG");
        setSize(900, 600);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(CREAM_LIGHT);

        initComponents();
        loadData();
    }

    private void initComponents() {

        // Panel chính chứa nội dung
        JPanel mainContent = new JPanel(new BorderLayout());

        // SỬA: Dùng GridBagLayout để kiểm soát kích thước các ô tốt hơn
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        topPanel.setBackground(CREAM_LIGHT);

        txtSupplierName = new JTextField();
        cbEmployee = new JComboBox<>();
        txtProductName = new JTextField();
        txtOrigin = new JTextField(); // Init

        txtQuantity = new JTextField();
        txtImportPrice = new JTextField();
        txtNote = new JTextArea(3, 20);

        // Khởi tạo Panel nhập ảnh
        pnlImages = new MultiImageInput();

        styleField(txtSupplierName);
        styleField(txtProductName);
        styleField(txtOrigin);
        styleField(txtQuantity);
        styleField(txtImportPrice);
        styleCombo(cbEmployee);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST; // QUAN TRỌNG: Neo về góc trên trái
        gbc.weightx = 1.0; // QUAN TRỌNG: Chia đều không gian chiều ngang
        gbc.weighty = 0; // Không giãn chiều cao cho các ô nhập liệu

        // Reset gridy về 0
        gbc.gridy = 0;
        gbc.gridx = 0;

        // Xếp chồng từ trên xuống dưới (Label -> Field)
        addVerticalItem(topPanel, createLabel("Nhà cung cấp (Nhập tên):"), txtSupplierName, gbc);
        addVerticalItem(topPanel, createLabel("Nhân viên nhập:"), cbEmployee, gbc);
        addVerticalItem(topPanel, createLabel("Sản phẩm (Nhập tên):"), txtProductName, gbc);
        addVerticalItem(topPanel, createLabel("Xuất xứ:"), txtOrigin, gbc);
        addVerticalItem(topPanel, createLabel("Số lượng:"), txtQuantity, gbc);
        addVerticalItem(topPanel, createLabel("Giá nhập (VNĐ):"), txtImportPrice, gbc);

        // Ghi chú
        addVerticalItem(topPanel, createLabel("Ghi chú:"), new JScrollPane(txtNote), gbc);

        // Hình ảnh (Giãn chiều cao phần còn lại)
        topPanel.add(createLabel("Hình ảnh (Kéo thả để chọn ảnh đại diện):"), gbc);
        gbc.gridy++;
        gbc.weighty = 1.0; // Cho phép giãn chiều cao
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(pnlImages, gbc);

        mainContent.add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[] {
                "Sản phẩm", "Số lượng", "Giá nhập", "Thành tiền"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ngăn người dùng sửa trực tiếp trên bảng để tránh lỗi dữ liệu
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Đặt chiều cao cố định cho bảng để không bị co giãn quá mức
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(800, 200));
        mainContent.add(tableScroll, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(CREAM_LIGHT);

        JButton btnAddItem = new JButton("Thêm vào danh sách");
        JButton btnSave = new JButton("Lưu Đơn Nhập");

        styleButton(btnAddItem, ThemeColor.INFO);
        styleButton(btnSave, ThemeColor.SUCCESS);

        bottomPanel.add(btnAddItem);
        bottomPanel.add(btnSave);

        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        // THÊM SCROLLBAR CHO TOÀN BỘ GIAO DIỆN
        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        btnAddItem.addActionListener(e -> addItem());
        btnSave.addActionListener(e -> saveImportOrder());
    }

    // Helper mới: Thêm Label và Field theo chiều dọc (Top-Down)
    private void addVerticalItem(JPanel p, Component label, Component field, GridBagConstraints gbc) {
        p.add(label, gbc);
        gbc.gridy++; // Xuống dòng
        p.add(field, gbc);
        gbc.gridy++; // Xuống dòng tiếp cho cặp tiếp theo
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(200, 30));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(130, 35));
    }

    private void loadData() {
        try {
            for (Employee e : employeeDAO.getAll()) { // Lưu ý: EmployeeDAO cũ dùng getAllEmployees() hay getAll()? Kiểm
                                                      // tra lại
                cbEmployee.addItem(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void addItem() {

        try {

            String pName = txtProductName.getText().trim();
            if (pName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm!");
                return;
            }

            int quantity = Integer.parseInt(txtQuantity.getText());
            double price = Double.parseDouble(txtImportPrice.getText());

            if (quantity <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng và Giá phải lớn hơn 0!");
                return;
            }

            // Kiểm tra ảnh (Optional)
            if (pnlImages.getCoverImage().isEmpty()) {
                // Có thể cảnh báo hoặc cho phép không có ảnh
                // JOptionPane.showMessageDialog(this, "Chưa chọn ảnh sản phẩm!");
            }

            ImportOrderItemDTO item = new ImportOrderItemDTO();
            // Tạm thời set ID = 0, sẽ xử lý logic tìm/tạo mới khi bấm Lưu
            item.setProductId(0);
            item.setQuantity(quantity);
            item.setImportPrice(price);

            itemList.add(item);

            model.addRow(new Object[] {
                    pName, // Hiển thị tên nhập tay
                    quantity,
                    price,
                    quantity * price
            });

            txtQuantity.setText("");
            txtImportPrice.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format!");
        }
    }

    private void saveImportOrder() {

        if (itemList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách nhập đang trống!");
            return;
        }

        ImportOrderDTO order = new ImportOrderDTO();

        // XỬ LÝ NHÀ CUNG CẤP (Tự động tạo nếu chưa có)
        String supName = txtSupplierName.getText().trim();
        if (supName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên Nhà cung cấp!");
            return;
        }

        // Logic tìm hoặc tạo Supplier
        SupplierDTO supplier = supplierDAO.findByName(supName);
        if (supplier == null) {
            supplier = new SupplierDTO();
            supplier.setName(supName);
            supplier.setPhone("");
            supplier.setAddress("");
            supplier.setEmail("");
            if (supplierDAO.insert(supplier)) {
                // Lấy lại ID vừa tạo
                supplier = supplierDAO.findByName(supName);
            }
        }

        if (supplier == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không thể tạo Nhà cung cấp mới!");
            return;
        }

        order.setSupplierId(supplier.getSupplierId());

        // FIX: Kiểm tra nhân viên trước khi lấy ID để tránh lỗi NullPointerException
        Employee selectedEmp = (Employee) cbEmployee.getSelectedItem();
        if (selectedEmp == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên nhập hàng!");
            return;
        }
        order.setEmployeeId(selectedEmp.getEmployeeId());
        order.setNote(txtNote.getText().isEmpty() ? "Hàng vừa nhập" : txtNote.getText());

        double total = 0;
        for (ImportOrderItemDTO item : itemList) {
            total += item.getQuantity() * item.getImportPrice();
        }
        order.setTotalCost(total);

        // XỬ LÝ SẢN PHẨM (Tự động tạo nếu chưa có)
        // Duyệt qua table model để lấy tên sản phẩm tương ứng với từng item
        for (int i = 0; i < itemList.size(); i++) {
            ImportOrderItemDTO item = itemList.get(i);
            String pName = model.getValueAt(i, 0).toString(); // Lấy tên từ cột 0

            ProductDTO p = productDAO.findByName(pName);
            if (p == null) {
                // Tạo sản phẩm mới
                p = new ProductDTO();
                p.setName(pName);
                p.setPrice(0); // Giá bán chưa cập nhật
                p.setStockQuantity(0); // Stock sẽ được cộng bởi ImportDAO
                p.setCategoryId(1); // Mặc định danh mục (cần sửa sau)
                p.setMakerId(1); // Mặc định
                p.setDescription("Hàng mới nhập từ " + supName);
                p.setOrigin(txtOrigin.getText().trim()); // Lưu xuất xứ
                p.setSupplierId(supplier.getSupplierId()); // Set Supplier ID
                p.setStatus("Hidden"); // 🔥 QUAN TRỌNG: Set trạng thái Ẩn cho hàng mới

                // Lấy ảnh đại diện từ Panel
                String coverImg = pnlImages.getCoverImage();
                p.setImage(coverImg);

                if (productDAO.insert(p)) {
                    p = productDAO.findByName(pName);
                    if (p == null) {
                        JOptionPane.showMessageDialog(this,
                                "Lỗi hệ thống: Không tìm thấy sản phẩm sau khi tạo: " + pName);
                        return;
                    }
                    // LƯU DANH SÁCH ẢNH VÀO BẢNG PHỤ
                    productBUS.saveProductImages(p.getProductId(), pnlImages.getAllImages());

                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không thể tạo sản phẩm mới: " + pName
                            + "\n(Kiểm tra lại dữ liệu Hãng/Danh mục mặc định)");
                    return;
                }
            } else {
                // FIX: Nếu sản phẩm đã tồn tại, chỉ cập nhật lại Nhà cung cấp.
                // KHÔNG cập nhật Xuất xứ ở màn hình này để tránh ghi đè dữ liệu cũ.
                // Việc sửa Xuất xứ nên được thực hiện ở màn hình Kho Hàng.
                updateProductSupplier(p.getProductId(), supplier.getSupplierId());
            }
            item.setProductId(p.getProductId());
        }

        if (importDAO.insertImportOrder(order, itemList)) {

            JOptionPane.showMessageDialog(this, "Nhập hàng thành công!");

            parent.loadData();
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu đơn nhập!");
        }
    }

    // FIX: Hàm chỉ cập nhật nhà cung cấp cho sản phẩm đã có khi nhập hàng
    private void updateProductSupplier(int productId, int supplierId) {
        String sql = "UPDATE Product SET supplier_id = ? WHERE product_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, supplierId);
            pst.setInt(2, productId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}