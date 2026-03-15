package com.keycapstore.gui;

import com.keycapstore.bus.WarrantyBUS;
import com.keycapstore.gui.components.TableModel;
import com.keycapstore.model.Employee;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WarrantyPanel extends JPanel implements Refreshable {

    private Employee currentEmp;
    private WarrantyBUS bus;
    private TableModel model;
    private TableModel.StyledTable table;

    // Các nút thao tác (State Machine)
    private JButton btnApprove, btnReject, btnRefund, btnExchange, btnComplete;
    private int selectedWarrantyId = -1;
    private String selectedStatus = "";

    public WarrantyPanel(Employee emp) {
        this.currentEmp = emp;
        this.bus = new WarrantyBUS();

        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- HEADER ---
        JLabel lblTitle = new JLabel("QUẢN LÝ BẢO HÀNH & ĐỔI TRẢ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        add(lblTitle, BorderLayout.NORTH);

        // --- CENTER TABLE ---
        String[] headers = { "Mã BH", "Mã Hóa Đơn", "Mã SP", "Mã KH", "Lỗi Của Khách", "Trạng Thái", "Ngày Yêu Cầu",
                "Ghi Chú Xử Lý" };
        model = new TableModel(headers);
        table = new TableModel.StyledTable(model);

        // Đổi màu dòng dựa trên Trạng Thái
        table.setRowColorizer((row, col, value) -> {
            try {
                String status = table.getModel().getValueAt(row, 5).toString();
                if (WarrantyBUS.STATUS_PENDING.equals(status))
                    return new Color(255, 250, 205); // Vàng nhạt (Chờ duyệt)
                if (WarrantyBUS.STATUS_REJECTED.equals(status))
                    return new Color(255, 228, 225); // Đỏ nhạt (Từ chối)
                if (WarrantyBUS.STATUS_COMPLETED.equals(status))
                    return new Color(225, 255, 225); // Xanh nhạt (Xong)
            } catch (Exception e) {
            }
            return null;
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedWarrantyId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    selectedStatus = table.getValueAt(row, 5).toString();
                    updateButtonStates();
                }
            }
        });

        JScrollPane scrollPane = TableModel.createScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- SOUTH: ACTION CARD (STATE MACHINE) ---
        JPanel actionPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        actionPanel.setBackground(ThemeColor.BG_LIGHT);
        actionPanel.setBorder(BorderFactory.createTitledBorder("Bảng Điều Khiển Xử Lý"));

        btnApprove = createButton("Duyệt Bảo Hành", ThemeColor.INFO);
        btnReject = createButton("Từ Chối", ThemeColor.DANGER);
        btnExchange = createButton("Xử lý ĐỔI HÀNG", new Color(155, 89, 182)); // Tím
        btnRefund = createButton("Xử lý HOÀN TIỀN", new Color(230, 126, 34)); // Cam
        btnComplete = createButton("Đóng Ca (Hoàn Tất)", ThemeColor.SUCCESS);

        actionPanel.add(btnApprove);
        actionPanel.add(btnReject);
        actionPanel.add(btnExchange);
        actionPanel.add(btnRefund);
        actionPanel.add(btnComplete);

        add(actionPanel, BorderLayout.SOUTH);

        setupActions();
        updateButtonStates(); // Reset buttons ban đầu
        loadData();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    // Hàm này quyết định nút nào được sáng lên dựa vào logic thực tế
    private void updateButtonStates() {
        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);
        btnExchange.setEnabled(false);
        btnRefund.setEnabled(false);
        btnComplete.setEnabled(false);

        if (selectedWarrantyId != -1) {
            switch (selectedStatus) {
                case WarrantyBUS.STATUS_PENDING:
                    btnApprove.setEnabled(true);
                    btnReject.setEnabled(true);
                    break;
                case WarrantyBUS.STATUS_APPROVED:
                    btnExchange.setEnabled(true);
                    btnRefund.setEnabled(true);
                    break;
                case WarrantyBUS.STATUS_IN_PROGRESS:
                    btnComplete.setEnabled(true);
                    break;
                // Nếu Completed hoặc Rejected thì khóa tất cả, coi như hồ sơ đã đóng.
            }
        }
    }

    private void loadData() {
        model.clearAll();
        List<Object[]> rawData = bus.getAllWarrantiesForDisplay();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (Object[] row : rawData) {
            // Format lại ngày tháng cho đẹp
            if (row[6] != null) {
                row[6] = sdf.format(row[6]);
            }
            model.addRow(row);
        }
        selectedWarrantyId = -1;
        selectedStatus = "";
        updateButtonStates();
    }

    private void setupActions() {
        int empId = (currentEmp != null) ? currentEmp.getEmployeeId() : 1; // Default ID nếu có lỗi session

        btnApprove.addActionListener(e -> {
            String solution = JOptionPane.showInputDialog(this, "Nhập phương án xử lý dự kiến (VD: Cho đổi mới):");
            if (solution != null && !solution.trim().isEmpty()) {
                WarrantyBUS.BUSResult result = bus.approveWarranty(selectedWarrantyId, empId, solution);
                handleResult(result);
            }
        });

        btnReject.addActionListener(e -> {
            String reason = JOptionPane.showInputDialog(this, "Nhập lý do từ chối bảo hành (VD: Rơi vỡ, rớt nước):");
            if (reason != null && !reason.trim().isEmpty()) {
                WarrantyBUS.BUSResult result = bus.rejectWarranty(selectedWarrantyId, empId, reason);
                handleResult(result);
            }
        });

        btnExchange.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Khách hàng sẽ được ĐỔI HÀNG MỚI.\nBạn có chắc chắn? (Kho sẽ được trừ sau khi bạn tự tay xuất hàng)",
                    "Xử lý Đổi Hàng", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String note = JOptionPane.showInputDialog(this, "Ghi chú thêm (Mã vận đơn gửi lại KH, v.v...):");
                if (note != null) { // Ngăn lỗi xử lý nhầm nếu user bấm Cancel ở popup
                    WarrantyBUS.BUSResult result = bus.processReturn(selectedWarrantyId, empId,
                            WarrantyBUS.RETURN_EXCHANGE, note);
                    handleResult(result);
                }
            }
        });

        btnRefund.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Khách hàng sẽ được HOÀN TIỀN.\nBạn có chắc chắn? (Kế toán sẽ chịu trách nhiệm chuyển khoản)",
                    "Xử lý Hoàn Tiền", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String note = JOptionPane.showInputDialog(this, "Ghi chú hoàn tiền (STK Khách hàng, số tiền hoàn...):");
                if (note != null) { // Ngăn lỗi xử lý nhầm nếu user bấm Cancel ở popup
                    WarrantyBUS.BUSResult result = bus.processReturn(selectedWarrantyId, empId,
                            WarrantyBUS.RETURN_REFUND, note);
                    handleResult(result);
                }
            }
        });

        btnComplete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Đóng ca bảo hành này? Trạng thái sẽ chuyển thành Hoàn tất.",
                    "Đóng hồ sơ", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                WarrantyBUS.BUSResult result = bus.completeWarranty(selectedWarrantyId, empId,
                        "Khách đã nhận lại hàng/tiền.");
                handleResult(result);
            }
        });
    }

    private void handleResult(WarrantyBUS.BUSResult result) {
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        loadData();
    }
}