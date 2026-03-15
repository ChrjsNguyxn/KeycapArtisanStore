package com.keycapstore.gui;

import com.keycapstore.bus.ShippingBUS;
import com.keycapstore.model.Customer;
import com.keycapstore.model.ShippingOrder;
import com.keycapstore.utils.ThemeColor;
import com.keycapstore.gui.components.TableModel;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MyShippingPanel extends JPanel implements Refreshable {

    private Object currentUser;
    private ShippingBUS bus;
    private DecimalFormat df = new DecimalFormat("#,###");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private TableModel model;
    private TableModel.StyledTable table;
    private JComboBox<String> cbStatusFilter;

    public MyShippingPanel(Object user) {
        this.currentUser = user;
        this.bus = new ShippingBUS();

        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("THEO DÕI ĐƠN HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.net.URL iconURL = getClass().getResource("/icons/shipping.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
        }
        add(lblTitle, BorderLayout.NORTH);

        // Bộ lọc trạng thái
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(ThemeColor.BG_LIGHT);
        filterPanel.add(new JLabel("Lọc theo trạng thái: "));
        cbStatusFilter = new JComboBox<>(new String[] { "Tất cả", "Chờ Xử Lý", "Đang Giao", "Hoàn Tất / Đã Hủy" });
        cbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStatusFilter.addActionListener(e -> loadData());
        filterPanel.add(cbStatusFilter);

        // Khởi tạo bảng với TableModel Component
        String[] headers = { "Mã Đơn", "Địa Chỉ Nhận", "Phương Thức", "Mã Vận Đơn", "Tổng Tiền", "Ngày Tạo",
                "Trạng Thái" };
        model = new TableModel(headers);
        table = new TableModel.StyledTable(model);

        // Thay đổi màu sắc của hàng dựa theo trạng thái giống hệt bên Admin
        table.setRowColorizer((row, col, value) -> {
            try {
                String status = table.getModel().getValueAt(row, 6).toString();
                if (status.contains("Chờ xử lý"))
                    return new Color(255, 250, 205); // Vàng nhạt
                if (status.contains("Đang giao"))
                    return new Color(225, 245, 255); // Xanh dương nhạt
                if (status.contains("Đã giao"))
                    return new Color(225, 255, 225); // Xanh lá nhạt
                if (status.contains("Đã hủy") || status.contains("thất bại"))
                    return new Color(255, 228, 225); // Đỏ nhạt
            } catch (Exception e) {
            }
            return null;
        });

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(TableModel.createScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        if (!(currentUser instanceof Customer))
            return;
        Customer cus = (Customer) currentUser;
        model.clearAll();

        ArrayList<ShippingOrder> allOrders = new ArrayList<>();
        int filterIndex = cbStatusFilter.getSelectedIndex();

        // Lấy dữ liệu từ Database thông qua ShippingBUS
        if (filterIndex == 0 || filterIndex == 1)
            allOrders.addAll(bus.getOrdersByStatus("PENDING"));
        if (filterIndex == 0 || filterIndex == 2)
            allOrders.addAll(bus.getOrdersByStatus("SHIPPING"));
        if (filterIndex == 0 || filterIndex == 3)
            allOrders.addAll(bus.getOrdersByStatus("HISTORY"));

        // Lọc hiển thị chỉ những đơn hàng của Khách hàng hiện tại (dựa trên SĐT)
        for (ShippingOrder o : allOrders) {
            if (o.getPhone() != null && o.getPhone().equals(cus.getPhone())) {
                String statusDisplay = o.getStatus();
                // Dịch sang tiếng Việt cho dễ hiểu
                if ("Pending".equalsIgnoreCase(statusDisplay))
                    statusDisplay = "Chờ xử lý";
                else if ("Shipping".equalsIgnoreCase(statusDisplay))
                    statusDisplay = "Đang giao";
                else if ("Delivered".equalsIgnoreCase(statusDisplay))
                    statusDisplay = "Đã giao";
                else if ("Cancelled".equalsIgnoreCase(statusDisplay))
                    statusDisplay = "Đã hủy/Giao thất bại";

                model.addRow(new Object[] { o.getInvoiceId(), o.getAddress(), o.getShippingMethod(),
                        o.getTrackingNumber() != null ? o.getTrackingNumber() : "Đang chờ cập nhật...",
                        df.format(o.getTotalAmount()) + " ₫",
                        o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "", statusDisplay });
            }
        }
    }

    @Override
    public void refresh() {
        loadData();
    }
}