package com.keycapstore.gui;

import com.keycapstore.dao.SupplierDAO;
import com.keycapstore.dao.ProductDAO;
import com.keycapstore.dao.ImportOrderDAO;
import com.keycapstore.dao.EmployeeDAO;
import com.keycapstore.model.SupplierDTO;
import com.keycapstore.model.ProductDTO;
import com.keycapstore.model.ImportOrderItemDTO;
import com.keycapstore.model.ImportOrderDTO;
import com.keycapstore.model.Employee;
import com.mycompany.mavenproject2.dao.*;
import com.mycompany.mavenproject2.gui.ImportOrderListGUI;

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

    private ImportOrderListGUI parent;

    // ===== COLOR THEME =====
    private final Color PRIMARY_DARK = new Color(62, 54, 46);
    private final Color CREAM_LIGHT = new Color(228, 220, 207);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color INFO_BLUE = new Color(52, 152, 219);
    private final Color TEXT_PRIMARY = new Color(51, 51, 51);

    public ImportOrderGUI(ImportOrderListGUI parent) {

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

        topPanel.add(createLabel("Supplier:"));
        topPanel.add(cbSupplier);
        topPanel.add(createLabel("Employee:"));
        topPanel.add(cbEmployee);
        topPanel.add(createLabel("Product:"));
        topPanel.add(cbProduct);
        topPanel.add(createLabel("Quantity:"));
        topPanel.add(txtQuantity);
        topPanel.add(createLabel("Import Price:"));
        topPanel.add(txtImportPrice);
        topPanel.add(createLabel("Note:"));
        topPanel.add(new JScrollPane(txtNote));

        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "Product", "Quantity", "Import Price", "Total"
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

        JButton btnAddItem = new JButton("Add Item");
        JButton btnSave = new JButton("Save");

        styleButton(btnAddItem, INFO_BLUE);
        styleButton(btnSave, SUCCESS_GREEN);

        bottomPanel.add(btnAddItem);
        bottomPanel.add(btnSave);

        add(bottomPanel, BorderLayout.SOUTH);

        btnAddItem.addActionListener(e -> addItem());
        btnSave.addActionListener(e -> saveImportOrder());
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
                JOptionPane.showMessageDialog(this, "Select product!");
                return;
            }

            int quantity = Integer.parseInt(txtQuantity.getText());
            double price = Double.parseDouble(txtImportPrice.getText());

            if (quantity <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity & Price must be > 0!");
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
            JOptionPane.showMessageDialog(this, "Invalid number format!");
        }
    }

    private void saveImportOrder() {

        if (itemList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items!");
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

            JOptionPane.showMessageDialog(this, "Import Success!");

            parent.loadData();
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Import Failed!");
        }
    }
}