package com.keycapstore.gui;

import com.keycapstore.bus.InvoiceBUS;
import com.keycapstore.model.Invoice;
import com.keycapstore.model.InvoiceDetail;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class OrderHistoryPanel extends JPanel implements Refreshable {

    private InvoiceBUS bus;
    private JTable tbInvoice, tbDetail;
    private DefaultTableModel modInvoice, modDetail;
    private DecimalFormat df = new DecimalFormat("#,###");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private ArrayList<Invoice> allInvoices;
    private JSpinner dateFrom, dateTo;
    private JCheckBox chkDateFilter;
    private java.util.HashMap<Integer, Object[]> supplements; // Map lưu thông tin bổ sung

    public OrderHistoryPanel() {
        bus = new InvoiceBUS();
        allInvoices = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("LỊCH SỬ GIAO DỊCH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.net.URL iconURL = getClass().getResource("/icons/invoice.png");
            if (iconURL == null) {
                iconURL = getClass().getResource("/icons/bill.png");
            }
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
        }
        add(lblTitle, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // --- 1. KHỞI TẠO PANEL CHI TIẾT TRƯỚC (Để tránh lỗi NullPointer khi click bảng
        // trên) ---
        JPanel pnlDetail = new JPanel(new BorderLayout());
        pnlDetail.setBackground(Color.WHITE);
        pnlDetail.setBorder(BorderFactory.createTitledBorder("📦 Chi tiết Hóa Đơn đang chọn"));

        String[] headerDetail = { "Tên Keycap", "Danh Mục", "SL", "Đơn Giá", "Voucher/Rank", "Tiền Giảm",
                "Thành Tiền" };
        modDetail = new DefaultTableModel(headerDetail, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbDetail = new JTable(modDetail);
        tbDetail.setRowHeight(30);
        tbDetail.getTableHeader().setBackground(ThemeColor.PRIMARY);
        tbDetail.getTableHeader().setForeground(Color.WHITE);
        tbDetail.setSelectionBackground(new Color(255, 224, 178));
        tbDetail.setSelectionForeground(Color.BLACK);

        pnlDetail.add(new JScrollPane(tbDetail), BorderLayout.CENTER);
        splitPane.setBottomComponent(pnlDetail);

        // --- 2. KHỞI TẠO PANEL DANH SÁCH HÓA ĐƠN ---
        JPanel pnlInvoice = new JPanel(new BorderLayout());
        pnlInvoice.setBackground(Color.WHITE);
        pnlInvoice.setBorder(BorderFactory.createTitledBorder("📜 Danh sách Hóa Đơn (Click để xem chi tiết)"));

        // --- BỘ LỌC NGÀY ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);

        chkDateFilter = new JCheckBox("Lọc theo ngày:");
        chkDateFilter.setBackground(Color.WHITE);
        filterPanel.add(chkDateFilter);

        SpinnerDateModel modelFrom = new SpinnerDateModel();
        dateFrom = new JSpinner(modelFrom);
        dateFrom.setEditor(new JSpinner.DateEditor(dateFrom, "dd/MM/yyyy"));
        filterPanel.add(dateFrom);

        filterPanel.add(new JLabel("-"));

        SpinnerDateModel modelTo = new SpinnerDateModel();
        dateTo = new JSpinner(modelTo);
        dateTo.setEditor(new JSpinner.DateEditor(dateTo, "dd/MM/yyyy"));
        filterPanel.add(dateTo);

        chkDateFilter.addActionListener(e -> filterInvoices());
        dateFrom.addChangeListener(e -> {
            if (chkDateFilter.isSelected())
                filterInvoices();
        });
        dateTo.addChangeListener(e -> {
            if (chkDateFilter.isSelected())
                filterInvoices();
        });

        pnlInvoice.add(filterPanel, BorderLayout.NORTH);

        String[] headerInv = { "Mã Bill", "Nhân Viên", "Khách Hàng", "SĐT Khách", "Hạng TV", "Voucher/Giảm", "Ngày Tạo",
                "Tổng Tiền" };
        modInvoice = new DefaultTableModel(headerInv, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbInvoice = new JTable(modInvoice);
        tbInvoice.setRowHeight(30);
        tbInvoice.getTableHeader().setBackground(ThemeColor.PRIMARY);
        tbInvoice.getTableHeader().setForeground(Color.WHITE);
        tbInvoice.setSelectionBackground(new Color(255, 224, 178));
        tbInvoice.setSelectionForeground(Color.BLACK);

        tbInvoice.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tbInvoice.getSelectedRow();
                if (row >= 0) {
                    Object idObj = modInvoice.getValueAt(row, 0);
                    int invoiceId = (idObj != null) ? Integer.parseInt(idObj.toString()) : -1;

                    Object nameObj = modInvoice.getValueAt(row, 2);
                    String customerName = (nameObj != null) ? nameObj.toString() : "Khách lẻ";

                    Object phoneObj = modInvoice.getValueAt(row, 3);
                    String customerPhone = (phoneObj != null && !phoneObj.toString().isEmpty()) ? phoneObj.toString()
                            : "Không có";
                    pnlDetail.setBorder(BorderFactory
                            .createTitledBorder("📦 Chi tiết cho: " + customerName + " - SĐT: " + customerPhone));
                    loadDetails(invoiceId);
                }
            }
        });

        pnlInvoice.add(new JScrollPane(tbInvoice), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Làm mới danh sách");
        btnRefresh.setBackground(ThemeColor.INFO);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadInvoices());
        pnlInvoice.add(btnRefresh, BorderLayout.SOUTH);

        splitPane.setTopComponent(pnlInvoice);

        add(splitPane, BorderLayout.CENTER);

        loadInvoices();
    }

    @Override
    public void refresh() {
        loadInvoices();
    }

    private void loadInvoices() {
        allInvoices = bus.getAllInvoices();
        supplements = bus.getInvoiceSupplements(); // Tải thông tin Rank/Voucher
        filterInvoices();
    }

    private void filterInvoices() {
        modInvoice.setRowCount(0);
        if (allInvoices == null)
            return;

        boolean filterDate = chkDateFilter.isSelected();
        Date start = null, end = null;
        if (filterDate) {
            start = getStartOfDay((Date) dateFrom.getValue());
            end = getEndOfDay((Date) dateTo.getValue());
        }

        for (Invoice i : allInvoices) {
            boolean matchDate = true;
            if (filterDate && i.getCreatedAt() != null) {
                if (i.getCreatedAt().before(start) || i.getCreatedAt().after(end))
                    matchDate = false;
            }

            if (matchDate) {
                // Lấy thông tin bổ sung từ Map
                Object[] sup = (supplements != null) ? supplements.get(i.getId()) : null;
                String rank = (sup != null && sup[0] != null) ? sup[0].toString() : "Không có";
                double discVal = (sup != null && sup[1] != null) ? (Double) sup[1] : 0;
                String discStr = (discVal > 0) ? "-" + df.format(discVal) : "-";

                modInvoice.addRow(new Object[] {
                        i.getId(),
                        i.getEmpName(),
                        i.getCustomerName(),
                        i.getCustomerPhone(),
                        rank,
                        discStr,
                        sdf.format(i.getCreatedAt()),
                        df.format(i.getTotalAmount()) + " ₫"
                });
            }
        }
    }

    private Invoice getInvoiceById(int id) {
        if (allInvoices == null)
            return null;
        for (Invoice i : allInvoices) {
            if (i.getId() == id)
                return i;
        }
        return null;
    }

    private void loadDetails(int invoiceId) {
        modDetail.setRowCount(0);
        ArrayList<InvoiceDetail> list = bus.getInvoiceDetails(invoiceId);
        Invoice inv = getInvoiceById(invoiceId);

        double totalRaw = 0;
        for (InvoiceDetail d : list) {
            totalRaw += d.getTotal();
        }

        // Tính toán tỷ lệ giảm giá thực tế (Net Reduction)
        // Net Reduction = Tổng gốc - Tổng thực trả (đã bao gồm ship)
        double finalInvoiceTotal = (inv != null) ? inv.getTotalAmount() : totalRaw;
        double netReduction = totalRaw - finalInvoiceTotal;
        double discountRatio = (totalRaw > 0 && netReduction > 0) ? (netReduction / totalRaw) : 0;

        for (InvoiceDetail d : list) {
            double rawTotal = d.getTotal();
            double discountAmt = rawTotal * discountRatio;
            double finalTotal = rawTotal - discountAmt;

            String voucherInfo = (discountAmt > 0) ? "Theo HĐ" : "-";
            String discountStr = (discountAmt > 0) ? "-" + df.format(discountAmt) : "0";

            modDetail.addRow(new Object[] {
                    d.getProductName(),
                    d.getCategoryName(),
                    d.getQuantity(),
                    df.format(d.getPrice()),
                    voucherInfo,
                    discountStr,
                    df.format(finalTotal)
            });
        }
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}