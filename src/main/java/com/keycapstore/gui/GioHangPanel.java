package com.keycapstore.gui;

import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.EventObject;

public class GioHangPanel extends JDialog {
    
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalPriceLabel;
    private JButton btnCheckout, btnContinue, btnClearCart;
    private List<CartItem> cartItems;
    private DecimalFormat df = new DecimalFormat("#,###");
    private Object currentUser;
    
    // Màu sắc
    private Color primaryDark = new Color(62, 54, 46);
    private Color creamLight = new Color(228, 220, 207);
    private Color taupeGrey = new Color(153, 143, 133);
    private Color glassWhite = new Color(255, 252, 245);
    private Color successGreen = new Color(46, 204, 113);
    private Color infoBlue = new Color(52, 152, 219);
    private Color dangerRed = new Color(231, 76, 60);
    private Color textPrimary = new Color(51, 51, 51);
    
    // Font chữ
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
    private Font normalFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font smallFont = new Font("Segoe UI", Font.PLAIN, 12);
    
    // Class CartItem
    public static class CartItem {
        private String id;
        private String name;
        private String brand;
        private int price;
        private int quantity;
        private String imagePath;
        
        public CartItem(String id, String name, String brand, int price, int quantity, String imagePath) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.price = price;
            this.quantity = quantity;
            this.imagePath = imagePath;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public int getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getImagePath() { return imagePath; }
        public int getTotal() { return price * quantity; }
    }
    
    public GioHangPanel(JFrame parent, List<CartItem> items, Object user) {
        super(parent, "Giỏ hàng của bạn", true);
        this.cartItems = items != null ? items : new ArrayList<>();
        this.currentUser = user;
        initUI();
    }
    
    private void initUI() {
        setSize(1000, 550);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(creamLight);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Center - Bảng giỏ hàng
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        // Thiết lập renderer cho cột ảnh
        cartTable.getColumnModel().getColumn(0).setCellRenderer(new ImagePanelRenderer());
        
        loadCartData();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primaryDark);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("GIỎ HÀNG CỦA BẠN");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(glassWhite);
        
        JLabel countLabel = new JLabel(cartItems.size() + " sản phẩm");
        countLabel.setFont(normalFont);
        countLabel.setForeground(glassWhite);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(creamLight);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Ảnh", "Tên sản phẩm", "Đơn giá", "Số lượng", "Thành tiền", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 5;
            }
        };
        
        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(80);
        cartTable.setFont(smallFont);
        cartTable.getTableHeader().setFont(normalFont);
        cartTable.getTableHeader().setBackground(primaryDark);
        cartTable.getTableHeader().setForeground(glassWhite);
        cartTable.setSelectionBackground(new Color(220, 220, 220));
        
        // Căn giữa các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // Set độ rộng cột
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        
        // Set CellEditor cho cột số lượng và thao tác
        cartTable.getColumnModel().getColumn(3).setCellRenderer(new QuantityCellRenderer());
        cartTable.getColumnModel().getColumn(3).setCellEditor(new QuantityCellEditor());
        
        cartTable.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        cartTable.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor());
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(new LineBorder(taupeGrey, 1));
        scrollPane.getViewport().setBackground(glassWhite);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(creamLight);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(creamLight);
        
        JLabel totalLabel = new JLabel("Tổng cộng:");
        totalLabel.setFont(titleFont);
        totalLabel.setForeground(textPrimary);
        
        totalPriceLabel = new JLabel("0 đ");
        totalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalPriceLabel.setForeground(dangerRed);
        
        totalPanel.add(totalLabel);
        totalPanel.add(Box.createHorizontalStrut(10));
        totalPanel.add(totalPriceLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(creamLight);
        
        btnClearCart = new JButton("Xóa giỏ hàng");
        btnClearCart.setFont(normalFont);
        btnClearCart.setBackground(taupeGrey);
        btnClearCart.setForeground(glassWhite);
        btnClearCart.setBorderPainted(false);
        btnClearCart.setFocusPainted(false);
        btnClearCart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearCart.addActionListener(e -> clearCart());
        
        btnContinue = new JButton("Tiếp tục mua hàng");
        btnContinue.setFont(normalFont);
        btnContinue.setBackground(infoBlue);
        btnContinue.setForeground(glassWhite);
        btnContinue.setBorderPainted(false);
        btnContinue.setFocusPainted(false);
        btnContinue.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnContinue.addActionListener(e -> dispose());
        
        btnCheckout = new JButton("Thanh toán");
        btnCheckout.setFont(normalFont);
        btnCheckout.setBackground(successGreen);
        btnCheckout.setForeground(glassWhite);
        btnCheckout.setBorderPainted(false);
        btnCheckout.setFocusPainted(false);
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckout.addActionListener(e -> checkout());
        
        buttonPanel.add(btnClearCart);
        buttonPanel.add(btnContinue);
        buttonPanel.add(btnCheckout);
        
        footer.add(totalPanel, BorderLayout.NORTH);
        footer.add(buttonPanel, BorderLayout.SOUTH);
        
        return footer;
    }
    
    private void loadCartData() {
        tableModel.setRowCount(0);
        
        for (CartItem item : cartItems) {
            String nameText = item.getName() + " (" + item.getBrand() + ")";
            String priceText = df.format(item.getPrice()) + " đ";
            String totalText = df.format(item.getTotal()) + " đ";
            
            tableModel.addRow(new Object[]{
                item.getImagePath(),
                nameText,
                priceText,
                item,
                totalText,
                "Xóa"
            });
        }
        
        updateTotalPrice();
    }
    
    private JPanel createImagePanel(String imagePath) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(glassWhite);
        panel.setPreferredSize(new Dimension(70, 70));
        
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(imageUrl);
                    Image originalImage = originalIcon.getImage();
                    
                    int maxWidth = 60;
                    int maxHeight = 60;
                    
                    Image scaledImage = originalImage.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    panel.add(imageLabel, BorderLayout.CENTER);
                } else {
                    JLabel noImageLabel = new JLabel("No Image");
                    noImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    noImageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    panel.add(noImageLabel, BorderLayout.CENTER);
                }
            } catch (Exception e) {
                JLabel noImageLabel = new JLabel("No Image");
                noImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noImageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                panel.add(noImageLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noImageLabel = new JLabel("No Image");
            noImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noImageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            panel.add(noImageLabel, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    private void updateRowTotal(int rowIndex, CartItem item) {
        tableModel.setValueAt(df.format(item.getTotal()) + " đ", rowIndex, 4);
        updateTotalPrice();
    }
    
    private void updateTotalPrice() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotal();
        }
        totalPriceLabel.setText(df.format(total) + " đ");
    }
    
    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ hàng?", 
            "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            loadCartData();
        }
    }
    
    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Giỏ hàng của bạn đang trống!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("HÓA ĐƠN MUA HÀNG\n\n");
        message.append("Tổng số sản phẩm: ").append(cartItems.size()).append("\n");
        
        int total = 0;
        for (CartItem item : cartItems) {
            message.append("- ").append(item.getName())
                   .append(" x ").append(item.getQuantity())
                   .append(" = ").append(df.format(item.getTotal())).append(" đ\n");
            total += item.getTotal();
        }
        
        message.append("\nTổng tiền: ").append(df.format(total)).append(" đ\n\n");
        message.append("Cảm ơn bạn đã mua hàng!");
        
        JOptionPane.showMessageDialog(this, 
            message.toString(), 
            "Thanh toán thành công", 
            JOptionPane.INFORMATION_MESSAGE);
            
        cartItems.clear();
        loadCartData();
    }
    
    // CellRenderer cho ảnh
    class ImagePanelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof String) {
                return createImagePanel((String) value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    // CellRenderer cho số lượng
    class QuantityCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof CartItem) {
                CartItem item = (CartItem) value;
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 20));
                panel.setBackground(glassWhite);
                
                JButton btnMinus = new JButton("-");
                btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btnMinus.setPreferredSize(new Dimension(30, 25));
                btnMinus.setBackground(taupeGrey);
                btnMinus.setForeground(glassWhite);
                btnMinus.setBorderPainted(false);
                btnMinus.setFocusPainted(false);
                
                JLabel quantityLabel = new JLabel(String.valueOf(item.getQuantity()));
                quantityLabel.setFont(normalFont);
                quantityLabel.setPreferredSize(new Dimension(30, 25));
                quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                JButton btnPlus = new JButton("+");
                btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btnPlus.setPreferredSize(new Dimension(30, 25));
                btnPlus.setBackground(successGreen);
                btnPlus.setForeground(glassWhite);
                btnPlus.setBorderPainted(false);
                btnPlus.setFocusPainted(false);
                
                panel.add(btnMinus);
                panel.add(quantityLabel);
                panel.add(btnPlus);
                
                return panel;
            }
            
            return new JLabel("1", SwingConstants.CENTER);
        }
    }
    
    // CellEditor cho số lượng
    class QuantityCellEditor extends AbstractCellEditor implements TableCellEditor {
        private int currentRow;
        private JPanel panel;
        private JLabel quantityLabel;
        private JButton btnMinus, btnPlus;
        private CartItem currentItem;
        
        public QuantityCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 20));
            panel.setBackground(glassWhite);
            
            btnMinus = new JButton("-");
            btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnMinus.setPreferredSize(new Dimension(30, 25));
            btnMinus.setBackground(taupeGrey);
            btnMinus.setForeground(glassWhite);
            btnMinus.setBorderPainted(false);
            btnMinus.setFocusPainted(false);
            btnMinus.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            quantityLabel = new JLabel();
            quantityLabel.setFont(normalFont);
            quantityLabel.setPreferredSize(new Dimension(30, 25));
            quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            btnPlus = new JButton("+");
            btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnPlus.setPreferredSize(new Dimension(30, 25));
            btnPlus.setBackground(successGreen);
            btnPlus.setForeground(glassWhite);
            btnPlus.setBorderPainted(false);
            btnPlus.setFocusPainted(false);
            btnPlus.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            panel.add(btnMinus);
            panel.add(quantityLabel);
            panel.add(btnPlus);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            currentItem = (CartItem) value;
            quantityLabel.setText(String.valueOf(currentItem.getQuantity()));
            
            // Xóa ActionListener cũ
            for (ActionListener al : btnMinus.getActionListeners()) {
                btnMinus.removeActionListener(al);
            }
            for (ActionListener al : btnPlus.getActionListeners()) {
                btnPlus.removeActionListener(al);
            }
            
            btnMinus.addActionListener(e -> {
                if (currentItem.getQuantity() > 1) {
                    currentItem.setQuantity(currentItem.getQuantity() - 1);
                    quantityLabel.setText(String.valueOf(currentItem.getQuantity()));
                    updateRowTotal(currentRow, currentItem);
                }
            });
            
            btnPlus.addActionListener(e -> {
                currentItem.setQuantity(currentItem.getQuantity() + 1);
                quantityLabel.setText(String.valueOf(currentItem.getQuantity()));
                updateRowTotal(currentRow, currentItem);
            });
            
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return currentItem;
        }
    }
    
    // CellRenderer cho thao tác
    class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JButton deleteBtn = new JButton("X");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setBackground(dangerRed);
            deleteBtn.setForeground(glassWhite);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setPreferredSize(new Dimension(40, 25));
            
            return deleteBtn;
        }
    }
    
    // CellEditor cho thao tác
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private int currentRow;
        private JButton deleteBtn;
        
        public ActionCellEditor() {
            deleteBtn = new JButton("X");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteBtn.setBackground(dangerRed);
            deleteBtn.setForeground(glassWhite);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.setPreferredSize(new Dimension(40, 25));
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            // Xóa ActionListener cũ
            for (ActionListener al : deleteBtn.getActionListeners()) {
                deleteBtn.removeActionListener(al);
            }
            
            deleteBtn.addActionListener(e -> {
                cartItems.remove(currentRow);
                loadCartData();
                fireEditingStopped();
            });
            
            return deleteBtn;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Xóa";
        }
    }
}