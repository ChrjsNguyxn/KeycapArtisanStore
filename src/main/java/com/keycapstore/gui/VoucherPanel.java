package com.keycapstore.gui;

import com.keycapstore.bus.VoucherBUS;
import com.keycapstore.model.Voucher;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class VoucherPanel extends JPanel implements Refreshable {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtCode, txtDiscount, txtQuantity;
    private JSpinner dateStart, dateEnd;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private VoucherBUS bus;
    private int selectedId = -1;
    private ArrayList<Voucher> voucherList; // Lưu danh sách gốc để lấy ngày tháng

    public VoucherPanel() {
        bus = new VoucherBUS();
        voucherList = new ArrayList<>();
        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("QUẢN LÝ VOUCHER KHUYẾN MÃI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            // Tìm icon ticket.png cho Voucher Panel
            java.net.URL iconURL = getClass().getResource("/icons/ticket.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            } else {
                System.err.println("VoucherPanel: Không tìm thấy icon /icons/ticket.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(lblTitle, BorderLayout.NORTH);

        // --- FORM NHẬP LIỆU ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Voucher"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Mã Voucher:"), gbc);
        gbc.gridx = 1;
        txtCode = new JTextField(15);
        formPanel.add(txtCode, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Giảm giá (%):"), gbc);
        gbc.gridx = 1;
        txtDiscount = new JTextField(15);
        formPanel.add(txtDiscount, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Số lượng:"), gbc);
        gbc.gridx = 1;
        txtQuantity = new JTextField(15);
        formPanel.add(txtQuantity, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Ngày bắt đầu:"), gbc);
        gbc.gridx = 1;
        dateStart = new JSpinner(new SpinnerDateModel());
        dateStart.setEditor(new JSpinner.DateEditor(dateStart, "dd/MM/yyyy"));
        formPanel.add(dateStart, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Ngày kết thúc:"), gbc);
        gbc.gridx = 1;
        dateEnd = new JSpinner(new SpinnerDateModel());
        dateEnd.setEditor(new JSpinner.DateEditor(dateEnd, "dd/MM/yyyy"));
        formPanel.add(dateEnd, gbc);

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm mới");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.EAST);

        // --- TABLE ---
        String[] headers = { "ID", "Mã Code", "Giảm (%)", "Số Lượng", "Ngày Bắt Đầu", "Ngày Kết Thúc" };
        model = new DefaultTableModel(headers, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(ThemeColor.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtCode.setText(model.getValueAt(row, 1).toString());
                    txtDiscount.setText(model.getValueAt(row, 2).toString());
                    txtQuantity.setText(model.getValueAt(row, 3).toString());

                    // Lấy ngày tháng từ danh sách gốc
                    for (Voucher v : voucherList) {
                        if (v.getId() == selectedId) {
                            dateStart.setValue(v.getStartDate() != null ? v.getStartDate() : new Date());
                            dateEnd.setValue(v.getExpiredDate() != null ? v.getExpiredDate() : new Date());
                            break;
                        }
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        setupActions();
        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        voucherList = bus.getAllVouchers();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (Voucher v : voucherList) {
            String start = v.getStartDate() != null ? sdf.format(v.getStartDate()) : "";
            String end = v.getExpiredDate() != null ? sdf.format(v.getExpiredDate()) : "";
            model.addRow(new Object[] { v.getId(), v.getCode(), v.getDiscountPercent(), v.getQuantity(), start, end });
        }
    }

    private void setupActions() {
        btnAdd.addActionListener(e -> {
            try {
                Voucher v = new Voucher();
                v.setCode(txtCode.getText().trim());
                v.setDiscountPercent(Double.parseDouble(txtDiscount.getText().trim()));
                v.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));

                Date start = (Date) dateStart.getValue();
                Date end = (Date) dateEnd.getValue();

                if (start.after(end)) {
                    JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước ngày kết thúc!", "Lỗi ngày tháng",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (bus.addVoucher(v, start, end)) {
                    JOptionPane.showMessageDialog(this, "Thêm Voucher thành công!");
                    loadData();
                    clearForm();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi nhập liệu: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedId == -1)
                return;
            try {
                Voucher v = new Voucher();
                v.setId(selectedId);
                v.setCode(txtCode.getText().trim());
                v.setDiscountPercent(Double.parseDouble(txtDiscount.getText().trim()));
                v.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));

                Date start = (Date) dateStart.getValue();
                Date end = (Date) dateEnd.getValue();

                if (start.after(end)) {
                    JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước ngày kết thúc!", "Lỗi ngày tháng",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (bus.updateVoucher(v, start, end)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadData();
                    clearForm();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi nhập liệu!");
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedId == -1)
                return;
            if (JOptionPane.showConfirmDialog(this, "Xóa Voucher này?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (bus.deleteVoucher(selectedId)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa!");
                    loadData();
                    clearForm();
                }
            }
        });

        btnClear.addActionListener(e -> clearForm());
    }

    private void clearForm() {
        txtCode.setText("");
        txtDiscount.setText("");
        txtQuantity.setText("");
        dateStart.setValue(new Date());
        dateEnd.setValue(new Date());
        selectedId = -1;
        table.clearSelection();
    }
}