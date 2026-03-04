package com.keycapstore.gui;

import com.keycapstore.gui.ImportOrderGUI;
import com.keycapstore.dao.SupplierDAO;
import com.keycapstore.dao.ImportOrderDAO;
import com.keycapstore.dao.EmployeeDAO;
import com.keycapstore.model.SupplierDTO;
import com.keycapstore.model.ImportOrderDTO;
import com.keycapstore.model.Employee;
import com.mycompany.mavenproject2.dao.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class WarehouseGUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private ImportOrderDAO importDAO = new ImportOrderDAO();
    private SupplierDAO supplierDAO = new SupplierDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();

    // ===== COLOR THEME =====
    private final Color PRIMARY_DARK = new Color(62, 54, 46);
    private final Color CREAM_LIGHT = new Color(228, 220, 207);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color INFO_BLUE = new Color(52, 152, 219);
    private final Color TEXT_PRIMARY = new Color(51, 51, 51);

    public WarehouseGUI() {

        setTitle("IMPORT ORDER LIST");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(CREAM_LIGHT);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "ID", "Supplier", "Employee", "Date", "Total"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(CREAM_LIGHT);

        add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(CREAM_LIGHT);

        JButton btnAdd = new JButton("Add New");
        JButton btnRefresh = new JButton("Refresh");

        styleButton(btnAdd, SUCCESS_GREEN);
        styleButton(btnRefresh, INFO_BLUE);

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== EVENTS =====
        btnAdd.addActionListener(e -> {
            new ImportOrderGUI(this).setVisible(true);
        });

        btnRefresh.addActionListener(e -> loadData());

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

        List<ImportOrderDTO> list = importDAO.getAll();

        for (ImportOrderDTO order : list) {

            SupplierDTO supplier = supplierDAO.getById(order.getSupplierId());
            Employee employee = employeeDAO.getById(order.getEmployeeId());

            model.addRow(new Object[]{
                    order.getImportId(),
                    supplier != null ? supplier.getName() : "",
                    employee != null ? employee.getFullName() : "",
                    order.getImportDate(),
                    order.getTotalCost()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WarehouseGUI().setVisible(true));
    }
}