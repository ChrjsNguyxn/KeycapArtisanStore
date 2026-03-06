package com.keycapstore.gui;
import com.keycapstore.model.*;
import com.keycapstore.dao.*;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImportOrderGUI extends JFrame {

    private JComboBox<SupplierDTO> cbSupplier;
    private JComboBox<Employee> cbEmployee;
    private JComboBox<ProductDTO> cbProduct;

    private JTextField txtQuantity, txtImportPrice;
    private JTextArea txtNote;

    private JTable table;
    private DefaultTableModel model;

    private List<ImportOrderItemDTO> itemList = new ArrayList<>();

    private ImportOrderDAO importDAO = new ImportOrderDAO();
    private SupplierDAO supplierDAO = new SupplierDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ProductDAO productDAO = new ProductDAO();

    private WarehouseGUI parent;

    // ===== COLOR THEME =====
    private final Color PRIMARY_DARK = new Color(62, 54, 46);
    private final Color CREAM_LIGHT = new Color(228, 220, 207);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color INFO_BLUE = new Color(52, 152, 219);
    private final Color TEXT_PRIMARY = new Color(51, 51, 51);

    public ImportOrderGUI(WarehouseGUI parent) {

        this.parent = parent;

        setTitle("CREATE IMPORT ORDER");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(CREAM_LIGHT);

        initComponents();
        loadData();
    }

    private void initComponents() {

        JPanel topPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        topPanel.setBackground(CREAM_LIGHT);

        cbSupplier = new JComboBox<>();
        JButton btnAddSupplier = new JButton("+");
        cbEmployee = new JComboBox<>();
        cbProduct = new JComboBox<>();

        txtQuantity = new JTextField();
        txtImportPrice = new JTextField();
        txtNote = new JTextArea(3, 20);

        styleField(txtQuantity);
        styleField(txtImportPrice);
        styleCombo(cbSupplier);
        styleCombo(cbEmployee);
        styleCombo(cbProduct);

        topPanel.add(createLabel("Nhà Cung Cấp:"));

        JPanel supplierPanel = new JPanel(new BorderLayout());
        supplierPanel.add(cbSupplier, BorderLayout.CENTER);
        supplierPanel.add(btnAddSupplier, BorderLayout.EAST);

        topPanel.add(supplierPanel);
        topPanel.add(createLabel("Nhân Viên:"));
        topPanel.add(cbEmployee);
        topPanel.add(createLabel("Sản Phẩm:"));
        topPanel.add(cbProduct);
        topPanel.add(createLabel("Số lượng:"));
        topPanel.add(txtQuantity);
        topPanel.add(createLabel("Giá Tiền:"));
        topPanel.add(txtImportPrice);
        topPanel.add(createLabel("Note:"));
        topPanel.add(new JScrollPane(txtNote));

        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "Sản Phẩm", "Số Lượng", "Giá Tiền", "Tổng tiền"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(CREAM_LIGHT);

        JButton btnAddItem = new JButton("Add");
        JButton btnDeleteItem = new JButton("Delete");
        JButton btnSave = new JButton("Save");

        styleButton(btnAddItem, INFO_BLUE);
        styleButton(btnDeleteItem, new Color(231,76,60));
        styleButton(btnSave, SUCCESS_GREEN);

        bottomPanel.add(btnAddItem);
        bottomPanel.add(btnDeleteItem);
        bottomPanel.add(btnSave);

        add(bottomPanel, BorderLayout.SOUTH);

        btnAddItem.addActionListener(e -> addItem());
        btnDeleteItem.addActionListener(e -> deleteItem());
        btnSave.addActionListener(e -> saveImportOrder());
        btnAddSupplier.addActionListener(e -> openAddSupplier());
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

        cbSupplier.removeAllItems();

        for (SupplierDTO s : supplierDAO.getAll()) {
            cbSupplier.addItem(s);
        }


        for (Employee e : employeeDAO.getAll()) {
            cbEmployee.addItem(e);
        }

        for (ProductDTO p : productDAO.getAll()) {
            cbProduct.addItem(p);
        }
    }

    private void addItem() {

        try {

            ProductDTO product = (ProductDTO) cbProduct.getSelectedItem();
            if (product == null) {
                JOptionPane.showMessageDialog(this, "Chọn sản phẩm!");
                return;
            }

            int quantity = Integer.parseInt(txtQuantity.getText());
            double price = Double.parseDouble(txtImportPrice.getText());

            if (quantity <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng và giá tiền > 0!");
                return;
            }

            ImportOrderItemDTO item = new ImportOrderItemDTO();
            item.setProductId(product.getProductId());
            item.setQuantity(quantity);
            item.setImportPrice(price);

            itemList.add(item);

            model.addRow(new Object[]{
                    product.getName(),
                    quantity,
                    price,
                    quantity * price
            });

            txtQuantity.setText("");
            txtImportPrice.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Phải là số!");
        }
    }

    private void saveImportOrder() {

        if (itemList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có sản phẩm!");
            return;
        }

        ImportOrderDTO order = new ImportOrderDTO();

        order.setSupplierId(((SupplierDTO) cbSupplier.getSelectedItem()).getSupplierId());
        order.setEmployeeId(((Employee) cbEmployee.getSelectedItem()).getEmployeeId());
        order.setNote(txtNote.getText());

        double total = 0;
        for (ImportOrderItemDTO item : itemList) {
            total += item.getQuantity() * item.getImportPrice();
        }
        order.setTotalCost(total);

        if (importDAO.insertImportOrder(order, itemList)) {

            JOptionPane.showMessageDialog(this, "Nhập hàng thành công!");

            parent.loadData();
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Nhập hàng thất bại!");
        }
    }
    private void openAddSupplier() {

    JTextField txtName = new JTextField();
    JTextField txtPhone = new JTextField();
    JTextField txtAddress = new JTextField();
    JTextField txtEmail = new JTextField();

    Object[] fields = {
        "Name:", txtName,
        "Phone:", txtPhone,
        "Address:", txtAddress,
        "Email:", txtEmail
    };

    int option = JOptionPane.showConfirmDialog(
            this,
            fields,
            "Add Supplier",
            JOptionPane.OK_CANCEL_OPTION
    );

    if (option == JOptionPane.OK_OPTION) {

        SupplierDTO s = new SupplierDTO();
        s.setName(txtName.getText());
        s.setPhone(txtPhone.getText());
        s.setAddress(txtAddress.getText());
        s.setEmail(txtEmail.getText());

        if (supplierDAO.insert(s)) {

            cbSupplier.removeAllItems();
            for (SupplierDTO sup : supplierDAO.getAll()) {
                cbSupplier.addItem(sup);
            }

            JOptionPane.showMessageDialog(this, "Đã thêm nhà cung cấp!");
        }
    }
}
    private void deleteItem(){

    int row = table.getSelectedRow();

    if(row == -1){
        JOptionPane.showMessageDialog(this,"Chọn sản phẩm cần xóa!");
        return;
    }

    itemList.remove(row);
    model.removeRow(row);
}
}