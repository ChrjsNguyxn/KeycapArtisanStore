package com.keycapstore.gui;

import com.keycapstore.bus.ShippingBUS;
import com.keycapstore.model.ShippingOrder;
import com.keycapstore.utils.ModernTable;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ShippingPanel extends JPanel implements Refreshable {

    private JTabbedPane tabbedPane;
    private ShippingBUS bus;
    private DecimalFormat df = new DecimalFormat("#,###");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Các bảng cho từng tab
    private ModernTable tbPending, tbShipping, tbHistory;
    private DefaultTableModel modPending, modShipping, modHistory;

    // Thêm các ô tìm kiếm cho mỗi tab
    private JTextField txtSearchPending, txtSearchShipping, txtSearchHistory;

    public ShippingPanel() {
        bus = new ShippingBUS();
        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("QUẢN LÝ VẬN ĐƠN");
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

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // --- TAB 1: CHỜ XỬ LÝ ---
        JPanel pnlPending = createTabPanel("PENDING");
        tabbedPane.addTab("Chờ Xử Lý", new ImageIcon(), pnlPending);

        // --- TAB 2: ĐANG GIAO ---
        JPanel pnlShipping = createTabPanel("SHIPPING");
        tabbedPane.addTab("Đang Giao Hàng", new ImageIcon(), pnlShipping);

        // --- TAB 3: LỊCH SỬ ---
        JPanel pnlHistory = createTabPanel("HISTORY");
        tabbedPane.addTab("Lịch Sử / Đã Hủy", new ImageIcon(), pnlHistory);

        add(tabbedPane, BorderLayout.CENTER);

        // Sự kiện chuyển tab -> Load lại dữ liệu
        tabbedPane.addChangeListener(e -> refresh());

        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private JPanel createTabPanel(String type) {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- THANH TÌM KIẾM ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Tìm Mã Vận Đơn:"));
        searchPanel.add(new JLabel("Tìm kiếm (Mã đơn/Vận đơn/SĐT):"));
        JTextField txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadData(); // Tải lại dữ liệu với bộ lọc
            }
        });

        pnl.add(searchPanel, BorderLayout.NORTH);

        String[] headers = { "Mã Đơn", "Khách Hàng", "SĐT", "Địa Chỉ", "Vận chuyển", "Mã Vận Đơn", "Tổng Tiền",
                "Ngày Tạo",
                "Trạng Thái" };
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ModernTable table = new ModernTable(model);
        // Thêm màu highlight khi chọn dòng
        table.setSelectionBackground(new Color(255, 224, 178));
        table.setSelectionForeground(Color.BLACK);

        // Lưu tham chiếu để load data sau này
        if (type.equals("PENDING")) {
            modPending = model;
            tbPending = table;
            txtSearchPending = txtSearch;
        } else if (type.equals("SHIPPING")) {
            modShipping = model;
            tbShipping = table;
            txtSearchShipping = txtSearch;
        } else {
            modHistory = model;
            tbHistory = table;
            txtSearchHistory = txtSearch;
        }

        // Thêm sự kiện double-click để nhập mã vận đơn
        if (type.equals("PENDING") || type.equals("SHIPPING")) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        actionUpdateTracking(table, model);
                    }
                }
            });
        }

        pnl.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- BUTTONS PANEL ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        if (type.equals("PENDING")) {
            JButton btnShip = createButton("Giao Cho Shipper", ThemeColor.INFO);
            btnShip.addActionListener(e -> actionUpdateStatus(tbPending, modPending, "Shipping"));
            btnPanel.add(btnShip);

            JButton btnCancel = createButton("Hủy Đơn", ThemeColor.DANGER);
            btnCancel.addActionListener(e -> actionUpdateStatus(tbPending, modPending, "Cancelled"));
            btnPanel.add(btnCancel);
        } else if (type.equals("SHIPPING")) {
            JButton btnDone = createButton("Xác Nhận Đã Giao", ThemeColor.SUCCESS);
            btnDone.addActionListener(e -> actionUpdateStatus(tbShipping, modShipping, "Delivered"));
            btnPanel.add(btnDone);

            JButton btnFail = createButton("Giao Thất Bại / Hoàn", ThemeColor.DANGER);
            btnFail.addActionListener(e -> actionUpdateStatus(tbShipping, modShipping, "Cancelled"));
            btnPanel.add(btnFail);
        }

        // Thêm label hướng dẫn
        if (type.equals("PENDING") || type.equals("SHIPPING")) {
            JLabel lblHint = new JLabel("Mẹo: Click đúp vào đơn hàng để nhập/sửa Mã Vận Đơn.");
            lblHint.setForeground(Color.GRAY);
            btnPanel.add(lblHint, 0); // Thêm vào đầu
        }

        pnl.add(btnPanel, BorderLayout.SOUTH);
        return pnl;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    private void loadData() {
        loadTableData(modPending, "PENDING", txtSearchPending);
        loadTableData(modShipping, "SHIPPING", txtSearchShipping);
        loadTableData(modHistory, "HISTORY", txtSearchHistory);
    }

    private void loadTableData(DefaultTableModel model, String type, JTextField searchField) {
        if (model == null)
            return;
        model.setRowCount(0);
        ArrayList<ShippingOrder> list = bus.getOrdersByStatus(type);
        if (list == null)
            list = new ArrayList<>();

        String keyword = (searchField != null && searchField.getText() != null)
                ? searchField.getText().trim().toLowerCase()
                : "";

        for (ShippingOrder o : list) {
            String id = String.valueOf(o.getInvoiceId());
            String tracking = o.getTrackingNumber() != null ? o.getTrackingNumber().toLowerCase() : "";
            String customer = o.getCustomerName() != null ? o.getCustomerName().toLowerCase() : "";
            String phone = o.getPhone() != null ? o.getPhone() : "";

            // Tìm kiếm đa năng: Mã đơn OR Mã vận đơn OR Tên khách OR SĐT
            if (keyword.isEmpty() || id.contains(keyword) || tracking.contains(keyword) || customer.contains(keyword)
                    || phone.contains(keyword)) {
                model.addRow(new Object[] {
                        o.getInvoiceId(),
                        o.getCustomerName(),
                        o.getPhone(),
                        o.getAddress(),
                        o.getShippingMethod(),
                        o.getTrackingNumber() != null ? o.getTrackingNumber() : "-", // Hiển thị mã vận đơn
                        df.format(o.getTotalAmount()) + " ₫",
                        o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "",
                        o.getStatus()
                });
            }
        }
    }

    private void actionUpdateStatus(JTable table, DefaultTableModel model, String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng!");
            return;
        }
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        String msg = "Bạn có chắc muốn chuyển trạng thái đơn #" + id + " sang: " + newStatus + "?";
        if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (bus.updateStatus(id, newStatus)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionUpdateTracking(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            // Không cần thông báo vì đây là double click
            return;
        }
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        Object trackingObj = model.getValueAt(row, 5);
        String currentTracking = (trackingObj == null || "-".equals(trackingObj.toString())) ? ""
                : trackingObj.toString();

        String newTracking = JOptionPane.showInputDialog(this, "Nhập mã vận đơn cho đơn hàng #" + id + ":",
                currentTracking);

        // Nếu người dùng không bấm Cancel
        if (newTracking != null) {
            if (bus.updateTracking(id, newTracking.trim())) {
                JOptionPane.showMessageDialog(this, "Cập nhật mã vận đơn thành công!");
                loadData(); // Tải lại để thấy thay đổi
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật mã vận đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}