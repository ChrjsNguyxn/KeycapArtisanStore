package com.keycapstore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class OrderManagementPanel extends JPanel {

    private JTable tblProducts;
    private JTable tblCart;
    private JTextField txtQuantity;
    private JLabel lblTotal;

    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;

    public OrderManagementPanel() {
        initUI();
    }

    private void initUI() {

        setLayout(new BorderLayout());
        setBackground(ThemeColor.CREAM_LIGHT);

        // ===== TITLE =====
        JLabel lblTitle = new JLabel("ORDER MANAGEMENT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeColor.PRIMARY_DARK);
        lblTitle.setBorder(new EmptyBorder(20, 20, 10, 20));
        add(lblTitle, BorderLayout.NORTH);

        // ===== LEFT: PRODUCT LIST =====
        productModel = new DefaultTableModel(
                new String[]{"ID", "Tên sản phẩm", "Giá", "Tồn kho"}, 0);

        tblProducts = new JTable(productModel);
        styleTable(tblProducts);

        JScrollPane productScroll = new JScrollPane(tblProducts);
        productScroll.setBorder(BorderFactory.createLineBorder(ThemeColor.TAUPE_GREY));

        JPanel leftPanel = createGlassPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblProduct = new JLabel("Danh sách sản phẩm");
        lblProduct.setForeground(ThemeColor.TEXT_PRIMARY);
        lblProduct.setFont(new Font("Segoe UI", Font.BOLD, 16));

        leftPanel.add(lblProduct, BorderLayout.NORTH);
        leftPanel.add(productScroll, BorderLayout.CENTER);

        JPanel addPanel = new JPanel();
        addPanel.setBackground(ThemeColor.GLASS_WHITE);

        txtQuantity = new JTextField(5);
        JButton btnAdd = new JButton("Thêm");
        btnAdd.setBackground(ThemeColor.SUCCESS_GREEN);
        btnAdd.setForeground(Color.WHITE);

        addPanel.add(new JLabel("Số lượng:"));
        addPanel.add(txtQuantity);
        addPanel.add(btnAdd);

        leftPanel.add(addPanel, BorderLayout.SOUTH);

        // ===== RIGHT: CART =====
        cartModel = new DefaultTableModel(
                new String[]{"ID", "SL", "Đơn giá", "Thành tiền"}, 0);

        tblCart = new JTable(cartModel);
        styleTable(tblCart);

        JScrollPane cartScroll = new JScrollPane(tblCart);
        cartScroll.setBorder(BorderFactory.createLineBorder(ThemeColor.TAUPE_GREY));

        JPanel rightPanel = createGlassPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblCart = new JLabel("Giỏ hàng");
        lblCart.setForeground(ThemeColor.TEXT_PRIMARY);
        lblCart.setFont(new Font("Segoe UI", Font.BOLD, 16));

        rightPanel.add(lblCart, BorderLayout.NORTH);
        rightPanel.add(cartScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ThemeColor.GLASS_WHITE);

        lblTotal = new JLabel("Tổng tiền: 0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(ThemeColor.PRIMARY_DARK);

        JButton btnCreate = new JButton("Tạo đơn");
        btnCreate.setBackground(ThemeColor.PRIMARY_DARK);
        btnCreate.setForeground(Color.WHITE);

        bottomPanel.add(lblTotal, BorderLayout.WEST);
        bottomPanel.add(btnCreate, BorderLayout.EAST);

        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ===== SPLIT =====
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );

        splitPane.setDividerLocation(600);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // ===== DEMO DATA (chỉ để test chọn sản phẩm) =====
        loadDemoData();

        // ===== EVENT: THÊM VÀO GIỎ =====
        btnAdd.addActionListener(e -> addToCart());
    }

    private JPanel createGlassPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ThemeColor.GLASS_WHITE);
        panel.setBorder(BorderFactory.createLineBorder(ThemeColor.TAUPE_GREY));
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setBackground(Color.WHITE);
        table.setForeground(ThemeColor.TEXT_PRIMARY);
        table.getTableHeader().setBackground(ThemeColor.PRIMARY_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private void loadDemoData() {
        productModel.addRow(new Object[]{1, "Bàn phím cơ", 500000, 20});
        productModel.addRow(new Object[]{2, "Chuột gaming", 300000, 15});
        productModel.addRow(new Object[]{3, "Tai nghe", 700000, 10});
    }

    private void addToCart() {

        int row = tblProducts.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm!");
            return;
        }

        int id = (int) productModel.getValueAt(row, 0);
        double price = (double) productModel.getValueAt(row, 2);
        int quantity = Integer.parseInt(txtQuantity.getText());

        double subtotal = price * quantity;

        cartModel.addRow(new Object[]{
                id,
                quantity,
                price,
                subtotal
        });

        updateTotal();
    }

    private void updateTotal() {

        double total = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += (double) cartModel.getValueAt(i, 3);
        }

        lblTotal.setText("Tổng tiền: " + total + " VND");
    }
}