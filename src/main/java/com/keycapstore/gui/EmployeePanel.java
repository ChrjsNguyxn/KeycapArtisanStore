package com.keycapstore.gui;

import com.keycapstore.bus.EmployeeBUS;
import com.keycapstore.model.Employee;
import com.keycapstore.utils.ModernButton;
import com.keycapstore.utils.ModernTable;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class EmployeePanel extends JPanel implements Refreshable {

    private ModernTable table;
    private DefaultTableModel model;
    private JTextField txtUser, txtPass, txtName, txtEmail, txtPin, txtPhone;
    private JComboBox<String> cbRole, cbStatus;
    private ModernButton btnAdd, btnUpdate, btnDelete, btnClear;
    private EmployeeBUS bus;
    private int selectedId = -1;

    public EmployeePanel() {
        bus = new EmployeeBUS();

        bus.fixGhostAccounts();

        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeColor.BG_LIGHT); // Nền màu kem sáng
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN SỰ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        add(lblTitle, BorderLayout.NORTH);

        // 2. FORM PANEL (Bên Phải)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addInput(formPanel, gbc, "Tên đăng nhập:", txtUser = new JTextField());
        addInput(formPanel, gbc, "Mật khẩu:", txtPass = new JTextField());
        addInput(formPanel, gbc, "Họ và Tên:", txtName = new JTextField());
        addInput(formPanel, gbc, "Số điện thoại:", txtPhone = new JTextField());
        addInput(formPanel, gbc, "Email:", txtEmail = new JTextField());
        addInput(formPanel, gbc, "Mã PIN (Bảo mật):", txtPin = new JTextField());

        gbc.gridy++;
        formPanel.add(createLabel("Chức vụ:"), gbc);
        gbc.gridy++;
        // Fix: Dung gia tri khop voi Database (sales_manager, etc.)
        cbRole = new JComboBox<>(new String[] { "sales_manager", "warehouse_manager", "super_admin" });
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRole.setBackground(Color.WHITE);
        formPanel.add(cbRole, gbc);

        gbc.gridy++;
        formPanel.add(createLabel("Trạng thái:"), gbc);
        gbc.gridy++;
        cbStatus = new JComboBox<>(new String[] { "active", "banned", "quit" });
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        formPanel.add(cbStatus, gbc);

        // BUTTON GROUP
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setOpaque(false);

        btnAdd = new ModernButton("THÊM", ThemeColor.SUCCESS);
        btnUpdate = new ModernButton("SỬA", ThemeColor.INFO);
        btnDelete = new ModernButton("XÓA", ThemeColor.DANGER);
        btnClear = new ModernButton("MỚI", ThemeColor.WARNING);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        formPanel.add(btnPanel, gbc);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[] headers = { "ID", "User", "Họ Tên", "SĐT", "Role", "Email", "PIN", "Status" };
        model = new DefaultTableModel(headers, 0);
        table = new ModernTable(model);
        table.setSelectionBackground(new Color(255, 224, 178));
        table.setSelectionForeground(Color.BLACK);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtUser.setText(model.getValueAt(row, 1).toString());
                    txtUser.setEditable(false);

                    txtPass.setText("");
                    txtPass.setEditable(true);

                    txtName.setText(model.getValueAt(row, 2).toString());
                    txtPhone.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    cbRole.setSelectedItem(
                            model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "sales_manager");
                    txtEmail.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
                    txtPin.setText(model.getValueAt(row, 6) != null ? model.getValueAt(row, 6).toString() : "");
                    cbStatus.setSelectedItem(model.getValueAt(row, 7).toString());

                    btnAdd.setEnabled(false);
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                } else {

                    clearForm();
                }
            }
        });

        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // SETUP ACTIONS (Logic cũ giữ nguyên)
        setupActions();

        add(tablePanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(340, 0));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.EAST);

        // Click ra vung trong bat ky de clear form
        MouseAdapter outsideClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearForm();
            }
        };
        this.addMouseListener(outsideClick);
        formPanel.addMouseListener(outsideClick);

        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(ThemeColor.PRIMARY);
        return lbl;
    }

    private void addInput(JPanel p, GridBagConstraints gbc, String label, JTextField c) {
        gbc.gridy++;
        p.add(createLabel(label), gbc);
        gbc.gridy++;
        c.setPreferredSize(new Dimension(280, 30));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        p.add(c, gbc);
    }

    private void loadData() {
        model.setRowCount(0);
        ArrayList<Employee> list = bus.getAllEmployees();
        for (Employee e : list) {
            model.addRow(new Object[] {
                    e.getEmployeeId(), e.getUsername(), e.getFullName(), e.getPhone(), e.getRole(), e.getEmail(),
                    e.getPinCode(), e.getStatus()
            });
        }
    }

    private void setupActions() {
        btnAdd.addActionListener(e -> {

            if (txtUser.getText().trim().isEmpty() || txtPass.getText().trim().isEmpty()
                    || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ: Tên đăng nhập, Mật khẩu và Họ tên!",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (bus.checkDuplicate(txtUser.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập này đã tồn tại!", "Trùng lặp",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Employee emp = new Employee();
            emp.setUsername(txtUser.getText().trim());
            emp.setPassword(txtPass.getText().trim());
            emp.setFullName(txtName.getText().trim());
            emp.setPhone(txtPhone.getText().trim());
            emp.setEmail(txtEmail.getText().trim());
            emp.setRole(cbRole.getSelectedItem().toString());
            emp.setPinCode(txtPin.getText().trim());

            if (bus.addEmployee(emp)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại! Vui lòng kiểm tra lại thông tin.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedId == 1) {
                JOptionPane.showMessageDialog(this, "⛔ CẤM: Không thể xóa Root Admin!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (bus.deleteEmployee(selectedId)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa thành công (Trạng thái -> Quit)!");
                    loadData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại! Có thể do lỗi hệ thống.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Employee emp = new Employee();
            emp.setEmployeeId(selectedId);
            emp.setFullName(txtName.getText().trim());
            emp.setPhone(txtPhone.getText().trim());
            emp.setEmail(txtEmail.getText().trim());
            emp.setRole(cbRole.getSelectedItem().toString());
            emp.setPinCode(txtPin.getText().trim());
            emp.setStatus(cbStatus.getSelectedItem().toString());
            emp.setPassword(txtPass.getText().trim());

            if (bus.updateEmployee(emp)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClear.addActionListener(e -> clearForm());
    }

    private void clearForm() {
        txtUser.setText("");
        txtUser.setEditable(true);

        txtPass.setText("");
        txtPass.setEditable(true);

        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtPin.setText("");
        cbRole.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);

        selectedId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        table.clearSelection();
    }
}