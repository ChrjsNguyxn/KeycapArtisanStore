package com.keycapstore.gui;

import com.keycapstore.bus.CustomerBUS;
import com.keycapstore.model.Customer;
import com.keycapstore.utils.ModernButton;
import com.keycapstore.utils.ModernTable;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CustomerPanel extends JPanel implements Refreshable {

    private ModernTable table;
    private DefaultTableModel model;
    private JTextField txtUser, txtPassword, txtName, txtEmail, txtPhone, txtAddress;
    private JComboBox<String> cbStatus;
    private ModernButton btnAdd, btnUpdate, btnDelete, btnClear;
    private CustomerBUS bus;
    private int selectedId = -1;
    private ArrayList<Customer> customerList;

    public CustomerPanel() {
        bus = new CustomerBUS();
        customerList = new ArrayList<>();
        setLayout(new BorderLayout(20, 20));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER
        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            java.net.URL iconURL = getClass().getResource("/icons/customer.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
        }
        add(lblTitle, BorderLayout.NORTH);

        // 2. FORM PANEL (Bên Phải)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addInput(formPanel, gbc, "Tên đăng nhập (Read-only):", txtUser = new JTextField());

        addInput(formPanel, gbc, "Mật khẩu mới:", txtPassword = new JTextField());
        addInput(formPanel, gbc, "Họ và Tên:", txtName = new JTextField());
        addInput(formPanel, gbc, "Email:", txtEmail = new JTextField());
        addInput(formPanel, gbc, "Số điện thoại:", txtPhone = new JTextField());
        addInput(formPanel, gbc, "Địa chỉ:", txtAddress = new JTextField());

        gbc.gridy++;
        formPanel.add(createLabel("Trạng thái:"), gbc);
        gbc.gridy++;
        cbStatus = new JComboBox<>(new String[] { "active", "banned" });
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        formPanel.add(cbStatus, gbc);

        // Nhắc nhở nhẹ cho nhân viên
        gbc.gridy++;
        JLabel lblNote = new JLabel(
                "<html><i>* Hạng và Tổng chi tiêu sẽ tự động cập nhật khi khách mua hàng.</i></html>");
        lblNote.setForeground(Color.GRAY);
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        formPanel.add(lblNote, gbc);

        // BUTTON GROUP
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setOpaque(false);

        btnAdd = new ModernButton("THÊM", ThemeColor.SUCCESS);
        btnUpdate = new ModernButton("CẬP NHẬT", ThemeColor.INFO);
        btnDelete = new ModernButton("XÓA", ThemeColor.DANGER);
        btnClear = new ModernButton("LÀM MỚI", ThemeColor.WARNING);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        formPanel.add(btnPanel, gbc);

        // 3. TABLE PANEL
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[] headers = { "ID", "User", "Họ Tên", "SĐT", "Tổng Chi Tiêu", "Hạng Thành Viên", "Giảm Giá", "Status" };
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
                    txtUser.setEditable(false); // Khong cho sua username khi update
                    txtPassword.setText(""); // Reset mat khau khi chon dong moi
                    txtName.setText(model.getValueAt(row, 2).toString());
                    txtPhone.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    // Các cột khác không hiển thị trên form nên không cần set

                    // Tìm thông tin chi tiết (Email, Address) từ danh sách gốc
                    for (Customer c : customerList) {
                        if (c.getCustomerId() == selectedId) {
                            txtEmail.setText(c.getEmail());
                            txtAddress.setText(c.getAddress());
                            break;
                        }
                    }

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

        // SETUP ACTIONS
        setupActions();

        add(tablePanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(400, 0)); // Tăng chiều rộng form lên 400
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.EAST);

        // Click ra vung trong bat ky de clear form (Giong EmployeePanel)
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
        c.setPreferredSize(new Dimension(320, 30)); // Tăng chiều rộng ô nhập liệu
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        p.add(c, gbc);
    }

    private void loadData() {
        model.setRowCount(0);
        if (bus != null) {
            ArrayList<Customer> list = bus.getAllCustomers();
            if (list != null) {
                customerList = list;
            }
        }
        if (customerList == null) {
            customerList = new ArrayList<>();
        }
        DecimalFormat df = new DecimalFormat("#,###");
        for (Customer c : customerList) {
            model.addRow(new Object[] {
                    c.getCustomerId(), c.getUsername(), c.getFullName(), c.getPhone(),
                    df.format(c.getTotalSpending()) + " ₫", c.getRankName(), c.getCurrentDiscount() + "%",
                    c.getStatus()
            });
        }
    }

    private void setupActions() {
        btnAdd.addActionListener(e -> {
            if (txtUser.getText().trim().isEmpty() || txtPassword.getText().trim().isEmpty()
                    || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Username, Mật khẩu và Họ tên!", "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check duplicate
            String error = bus.checkDuplicate(txtUser.getText().trim(), txtEmail.getText().trim(),
                    txtPhone.getText().trim());
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Trùng lặp", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Customer c = new Customer();
            c.setUsername(txtUser.getText().trim());
            c.setPassword(txtPassword.getText().trim());
            c.setFullName(txtName.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            c.setPhone(txtPhone.getText().trim());
            c.setAddress(txtAddress.getText().trim());
            c.setStatus(cbStatus.getSelectedItem().toString());

            if (bus.addCustomer(c)) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                loadData();
                clearForm();
            }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedId == -1)
                return;

            Customer c = new Customer();
            c.setCustomerId(selectedId);
            c.setPassword(txtPassword.getText().trim());
            c.setFullName(txtName.getText().trim());
            c.setEmail(txtEmail.getText().trim());
            c.setPhone(txtPhone.getText().trim());
            c.setAddress(txtAddress.getText().trim());
            c.setStatus(cbStatus.getSelectedItem().toString());

            if (bus.updateCustomerByAdmin(c)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedId == -1)
                return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa khách hàng này?\nDữ liệu sẽ mất vĩnh viễn.",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (bus.deleteCustomer(selectedId)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                    loadData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnClear.addActionListener(e -> clearForm());
    }

    private void clearForm() {
        txtUser.setText("");
        txtUser.setEditable(true); // Cho phep nhap username khi them moi
        txtPassword.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        cbStatus.setSelectedIndex(0);
        selectedId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        table.clearSelection();
    }
}
