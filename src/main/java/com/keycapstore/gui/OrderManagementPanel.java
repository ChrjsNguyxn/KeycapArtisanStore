package com.keycapstore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.keycapstore.bus.OrderBUS;
import com.keycapstore.dao.ProductDAO;
import com.keycapstore.dto.OrderItem;
import com.keycapstore.dto.Product;

public class OrderManagementPanel extends JPanel {

    private JTable tblProducts;
    private JTable tblCart;

    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;

    private JTextField txtVoucher;
    private JLabel lblTotal;

    private ArrayList<OrderItem> cart = new ArrayList<>();

    private ProductDAO productDAO = new ProductDAO();
    private OrderBUS orderBUS = new OrderBUS();

    public OrderManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.CREAM_LIGHT);

        initUI();
        loadProducts();
    }

    private void initUI() {

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeColor.CREAM_LIGHT);

        JLabel title = new JLabel(" Quản lý đơn hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ThemeColor.PRIMARY_DARK);

        topPanel.add(title, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        centerPanel.setBackground(ThemeColor.CREAM_LIGHT);

        centerPanel.add(createProductPanel());
        centerPanel.add(createCartPanel());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createProductPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Sản phẩm"));

        productModel = new DefaultTableModel(
                new String[]{"ID","Tên","Giá","Số lượng"},0
        );

        tblProducts = new JTable(productModel);
        JScrollPane scroll = new JScrollPane(tblProducts);

        panel.add(scroll, BorderLayout.CENTER);

        JButton btnAdd = new JButton("Thêm vào giỏ hàng");
        btnAdd.setBackground(ThemeColor.PRIMARY_DARK);
        btnAdd.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> addProductToCart());

        panel.add(btnAdd, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Giỏ hàng"));

        cartModel = new DefaultTableModel(
                new String[]{"Mã sản phẩm","Tên","Giá","Số lượng","Thành tiền"},0
        );

        tblCart = new JTable(cartModel);
        JScrollPane scroll = new JScrollPane(tblCart);

        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(4,1,5,5));

        JPanel voucherPanel = new JPanel(new BorderLayout());

        txtVoucher = new JTextField();
        JButton btnApply = new JButton("Áp dụng voucher");

        btnApply.addActionListener(e -> applyVoucher());

        voucherPanel.add(txtVoucher,BorderLayout.CENTER);
        voucherPanel.add(btnApply,BorderLayout.EAST);

        lblTotal = new JLabel("Tổng: 0");

        JButton btnCheckout = new JButton("Thanh toán");
        btnCheckout.setBackground(ThemeColor.SUCCESS_GREEN);
        btnCheckout.setForeground(Color.WHITE);

        btnCheckout.addActionListener(e -> checkout());

        bottom.add(voucherPanel);
        bottom.add(lblTotal);
        bottom.add(btnCheckout);

        panel.add(bottom,BorderLayout.SOUTH);

        return panel;
    }

    private void loadProducts() {

        productModel.setRowCount(0);

        try {

            java.util.List<Product> products = productDAO.getAllProducts();

            for(Product p : products){

                productModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getName(),
                        p.getPrice(),
                        p.getStock()
                });

            }

        }catch(Exception e){
            // log error
        }

    }

    private void addProductToCart(){

        int row = tblProducts.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn sản phẩm");
            return;
        }

        int id = (int) productModel.getValueAt(row,0);
        String name = (String) productModel.getValueAt(row,1);
        double price = (double) productModel.getValueAt(row,2);

        int qty = 1;

        OrderItem item = new OrderItem();

        item.setProductId(id);
        item.setQuantity(qty);
        item.setPrice(price);

        cart.add(item);

        cartModel.addRow(new Object[]{
                id,
                name,
                price,
                qty,
                price * qty
        });

        updateTotal();
    }

    private void updateTotal(){

        double total = 0;

        for(OrderItem item : cart){

            total += item.getPrice() * item.getQuantity();

        }

        lblTotal.setText("Total: " + total);
    }

    private void applyVoucher(){

        String code = txtVoucher.getText();

        boolean success = orderBUS.applyVoucher(code);

        if(!success){

            JOptionPane.showMessageDialog(this,"Voucher không hợp lệ");

            return;
        }

        updateTotal();

    }

    private void checkout(){

        String[] options = {"COD","Thanh toán khi nhận hàng"};

        int payment = JOptionPane.showOptionDialog(
                this,
                "Chọn phương thức thanh toán",
                "Thanh toán",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if(payment == -1) return;

        orderBUS.checkout(1); // placeholder customer ID

        JOptionPane.showMessageDialog(this,"Đặt hàng thành công");

        cart.clear();
        cartModel.setRowCount(0);

        updateTotal();

    }

}