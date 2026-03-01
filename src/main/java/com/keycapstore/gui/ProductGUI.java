package com.keycapstore.gui;

import com.keycapstore.dao.ProductDAO;
import com.keycapstore.dao.MakerDAO;
import com.keycapstore.dao.CategoryDAO;
import com.keycapstore.model.ProductDTO;
import com.keycapstore.model.CategoryDTO;
import com.keycapstore.model.MakerDTO;
import com.mycompany.mavenproject2.dao.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProductGUI extends JFrame {

    // ===== THEME COLOR =====
    private final Color PRIMARY_DARK = new Color(62,54,46);
    private final Color CREAM_LIGHT = new Color(228,220,207);
    private final Color TAUPE_GREY = new Color(153,143,133);
    private final Color GLASS_WHITE = new Color(255,252,245);
    private final Color SUCCESS_GREEN = new Color(46,204,113);
    private final Color INFO_BLUE = new Color(52,152,219);
    private final Color DANGER_RED = new Color(231,76,60);
    private final Color TEXT_PRIMARY = new Color(51,51,51);

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtName, txtPrice, txtStock, txtProfile, txtMaterial;
    private JTextArea txtDescription;
    private JComboBox<CategoryDTO> cbCategory;
    private JComboBox<MakerDTO> cbMaker;

    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private MakerDAO makerDAO = new MakerDAO();

    private int selectedId = -1;

    public ProductGUI() {

        setTitle("PRODUCT MANAGEMENT");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15,15));

        getContentPane().setBackground(CREAM_LIGHT);

        initComponents();
        loadCategories();
        loadMakers();
        loadTable();
    }

    private void initComponents() {

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "ID","Category","Maker","Name","Price",
                "Stock","Profile","Material","Status"
        },0);

        table = new JTable(model);
        table.setRowHeight(28);
        table.setBackground(GLASS_WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(TAUPE_GREY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI",Font.BOLD,13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(TAUPE_GREY,1));

        add(scrollPane, BorderLayout.CENTER);

        // ===== FORM PANEL =====
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(GLASS_WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TAUPE_GREY,1),
                "Product Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(15);
        txtPrice = new JTextField(10);
        txtStock = new JTextField(10);
        txtProfile = new JTextField(10);
        txtMaterial = new JTextField(10);
        txtDescription = new JTextArea(3,15);

        cbCategory = new JComboBox<>();
        cbMaker = new JComboBox<>();

        int y=0;
        addField(panel, gbc, y++, "Category:", cbCategory);
        addField(panel, gbc, y++, "Maker:", cbMaker);
        addField(panel, gbc, y++, "Name:", txtName);
        addField(panel, gbc, y++, "Price:", txtPrice);
        addField(panel, gbc, y++, "Stock:", txtStock);
        addField(panel, gbc, y++, "Profile:", txtProfile);
        addField(panel, gbc, y++, "Material:", txtMaterial);

        gbc.gridx=0; gbc.gridy=y;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx=1;
        panel.add(new JScrollPane(txtDescription), gbc);

        // ===== BUTTON PANEL =====
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(GLASS_WHITE);

        JButton btnAdd = createButton("Add", SUCCESS_GREEN);
        JButton btnUpdate = createButton("Update", INFO_BLUE);
        JButton btnDelete = createButton("Delete", DANGER_RED);
        JButton btnClear = createButton("Clear", PRIMARY_DARK);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx=0; gbc.gridy=++y; gbc.gridwidth=2;
        panel.add(btnPanel, gbc);

        add(panel, BorderLayout.EAST);

        // ===== ACTION =====
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearForm());
    }

    private JButton createButton(String text, Color color){
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI",Font.BOLD,12));
        return btn;
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          int y, String label, JComponent comp) {
        gbc.gridx=0; gbc.gridy=y;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx=1;
        panel.add(comp, gbc);
    }

    private void loadCategories() {
        cbCategory.removeAllItems();
        for(CategoryDTO c : categoryDAO.getAll()){
            cbCategory.addItem(c);
        }
    }

    private void loadMakers() {
        cbMaker.removeAllItems();
        for(MakerDTO m : makerDAO.getAll()){
            cbMaker.addItem(m);
        }
    }

    private void loadTable() {
        model.setRowCount(0);
        List<ProductDTO> list = productDAO.getAll();

        for(ProductDTO p : list){
            model.addRow(new Object[]{
                    p.getProductId(),
                    p.getCategoryName(),
                    p.getMakerName(),
                    p.getName(),
                    p.getPrice(),
                    p.getStockQuantity(),
                    p.getProfile(),
                    p.getMaterial(),
                    p.getStockQuantity() > 0 ? "Còn hàng" : "Hết hàng"
            });
        }
    }

    private void addProduct() {
        JOptionPane.showMessageDialog(this,"Trùng sản phẩm.");
    }

    private void updateProduct() {}
    private void deleteProduct() {}
    private void clearForm() {}

    public static void main(String[] args) {
        new ProductGUI().setVisible(true);
    }
}