package com.keycapstore.gui;

import com.keycapstore.dao.SupplierDAO;
import com.keycapstore.dao.ImportOrderDAO;
import com.keycapstore.dao.EmployeeDAO;
import com.keycapstore.model.SupplierDTO;
import com.keycapstore.model.ImportOrderDTO;
import com.keycapstore.model.Employee;
import com.keycapstore.utils.ThemeColor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ImportManagementPanel extends JPanel implements Refreshable {

    private JTable table;
    private DefaultTableModel model;

    private ImportOrderDAO importDAO = new ImportOrderDAO();
    private SupplierDAO supplierDAO = new SupplierDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();

    public ImportManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JLabel lblTitle = new JLabel("QUẢN LÝ NHẬP HÀNG (IMPORT)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[] {
                "ID", "Nhà Cung Cấp", "Nhân Viên Nhập", "Ngày Nhập", "Tổng Tiền"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setBackground(ThemeColor.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.setSelectionBackground(new Color(255, 224, 178));
        table.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(ThemeColor.BG_LIGHT);

        JButton btnAdd = new JButton("Tạo Đơn Nhập Mới");
        JButton btnRefresh = new JButton("Làm Mới");

        styleButton(btnAdd, ThemeColor.SUCCESS);
        styleButton(btnRefresh, ThemeColor.INFO);

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== EVENTS =====
        btnAdd.addActionListener(e -> {
            new ImportOrderGUI(this).setVisible(true);
        });

        btnRefresh.addActionListener(e -> loadData());

        // Load data ban đầu
        try {
            loadData();
        } catch (Exception e) {
            System.err.println("ImportManagementPanel: Chưa thể load dữ liệu (Có thể do thiếu DAO/DTO)");
        }
    }

    @Override
    public void refresh() {
        loadData();
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(130, 35));
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            List<ImportOrderDTO> list = importDAO.getAll();
            if (list != null) {
                for (ImportOrderDTO order : list) {
                    SupplierDTO supplier = supplierDAO.getById(order.getSupplierId());
                    Employee employee = employeeDAO.getById(order.getEmployeeId());

                    model.addRow(new Object[] {
                            order.getImportId(),
                            supplier != null ? supplier.getName() : "N/A",
                            employee != null ? employee.getFullName() : "N/A",
                            order.getImportDate(),
                            String.format("%,.0f ₫", order.getTotalCost())
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}