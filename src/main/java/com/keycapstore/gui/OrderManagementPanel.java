package com.keycapstore.gui;

import com.keycapstore.bus.OrderBUS;
import com.keycapstore.dto.OrderItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderManagementPanel extends JPanel {

    private JTable tblProducts;
    private JTable tblCart;

    private JLabel lblTotal;

    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;

    private final OrderBUS orderBUS = new OrderBUS();

    public OrderManagementPanel() {

        initUI();

    }

    private void initUI() {

        setLayout(new BorderLayout());
        setBackground(ThemeColor.CREAM_LIGHT);

        // ===== TITLE =====
        JLabel lblTitle = new JLabel("ORDER");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeColor.PRIMARY_DARK);
        lblTitle.setBorder(new EmptyBorder(20, 20, 10, 20));

        add(lblTitle, BorderLayout.NORTH);

        // ===== PRODUCT TABLE =====

        productModel = new DefaultTableModel(
                new String[]{"ID", "Tên sản phẩm", "Giá", "Tồn kho"}, 0);

        tblProducts = new JTable(productModel);
        styleTable(tblProducts);

        JScrollPane productScroll = new JScrollPane(tblProducts);

        // ===== CART TABLE =====

        cartModel = new DefaultTableModel(
                new String[]{"Tên", "Giá", "SL", "Thành tiền"}, 0);

        tblCart = new JTable(cartModel);
        styleTable(tblCart);

        JScrollPane cartScroll = new JScrollPane(tblCart);

        // ===== LEFT PANEL =====

        JPanel leftPanel = createGlassPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(new EmptyBorder(15,15,15,15));

        JLabel lblProduct = new JLabel("Danh sách sản phẩm");
        lblProduct.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblProduct.setForeground(ThemeColor.TEXT_PRIMARY);

        leftPanel.add(lblProduct, BorderLayout.NORTH);
        leftPanel.add(productScroll, BorderLayout.CENTER);

        JButton btnAdd = new JButton("Thêm vào giỏ");
        btnAdd.setBackground(ThemeColor.SUCCESS_GREEN);
        btnAdd.setForeground(Color.WHITE);

        JPanel addPanel = new JPanel();
        addPanel.setBackground(ThemeColor.GLASS_WHITE);
        addPanel.add(btnAdd);

        leftPanel.add(addPanel, BorderLayout.SOUTH);

        // ===== RIGHT PANEL =====

        JPanel rightPanel = createGlassPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(15,15,15,15));

        JLabel lblCart = new JLabel("Giỏ hàng");
        lblCart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCart.setForeground(ThemeColor.TEXT_PRIMARY);

        rightPanel.add(lblCart, BorderLayout.NORTH);
        rightPanel.add(cartScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ThemeColor.GLASS_WHITE);

        lblTotal = new JLabel("Tổng tiền: 0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(ThemeColor.PRIMARY_DARK);

        JButton btnPay = new JButton("Thanh toán");
        btnPay.setBackground(ThemeColor.PRIMARY_DARK);
        btnPay.setForeground(Color.WHITE);

        bottomPanel.add(lblTotal, BorderLayout.WEST);
        bottomPanel.add(btnPay, BorderLayout.EAST);

        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ===== SPLIT =====

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );

        splitPane.setDividerLocation(500);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // ===== LOAD DEMO DATA =====

        loadProducts();

        // ===== EVENTS =====

        btnAdd.addActionListener(e -> addProductToCart());

        btnPay.addActionListener(e -> paymentDialog());

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

    private void loadProducts() {

        productModel.addRow(new Object[]{1, "Keycap Sakura", 250000, 10});
        productModel.addRow(new Object[]{2, "Keycap PBT", 300000, 20});
        productModel.addRow(new Object[]{3, "Keycap RGB", 400000, 15});

    }

    private void addProductToCart() {

        int row = tblProducts.getSelectedRow();

        if (row == -1) {

            JOptionPane.showMessageDialog(this, "Chọn sản phẩm trước");
            return;

        }

        int id = (int) productModel.getValueAt(row,0);
        String name = (String) productModel.getValueAt(row,1);
        double price = (double) productModel.getValueAt(row,2);

        orderBUS.addProduct(id,name,price);

        refreshCart();

    }

    private void refreshCart() {

        cartModel.setRowCount(0);

        for(OrderItem item : orderBUS.getCart()) {

            cartModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getTotal()
            });

        }

        lblTotal.setText("Tổng tiền: " + orderBUS.getTotal() + " VND");

    }

    private void paymentDialog() {

        if(orderBUS.getCart().isEmpty()) {

            JOptionPane.showMessageDialog(this,"Giỏ hàng trống");
            return;

        }

        String[] methods = {
                "Thanh toán khi nhận hàng (COD)",
                "Thanh toán tiền mặt tại quầy"
        };

        int choice = JOptionPane.showOptionDialog(
                this,
                "Chọn phương thức thanh toán",
                "Thanh toán",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                methods,
                methods[0]
        );

        if(choice == -1) return;

        JOptionPane.showMessageDialog(this,
                "Đặt hàng thành công!");

        orderBUS.clearCart();

        refreshCart();

    }

}