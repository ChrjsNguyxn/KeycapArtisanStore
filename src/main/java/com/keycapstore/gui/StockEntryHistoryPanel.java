package com.keycapstore.gui;

import com.keycapstore.bus.StockEntryBUS;
import com.keycapstore.model.StockEntryRecord;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StockEntryHistoryPanel extends JPanel implements Refreshable {

    private JTable table;
    private DefaultTableModel model;
    private StockEntryBUS bus;
    private DecimalFormat df = new DecimalFormat("#,###");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private JTextField txtSearch;
    private ArrayList<StockEntryRecord> allRecords;
    private JSpinner dateFrom, dateTo;
    private JCheckBox chkDateFilter;

    public StockEntryHistoryPanel() {
        bus = new StockEntryBUS();
        allRecords = new ArrayList<>(); // Khởi tạo trước để tránh NullPointerException

        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH SỬ NHẬP KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            // Tìm icon reload.png cho Stock Entry Panel
            java.net.URL iconURL = getClass().getResource("/icons/reload.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
        }
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // --- THANH TÌM KIẾM ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm: "));
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập tên SP, nhân viên...");
        searchPanel.add(txtSearch);

        searchPanel.add(Box.createHorizontalStrut(15));
        chkDateFilter = new JCheckBox("Lọc ngày:");
        chkDateFilter.setOpaque(false);
        searchPanel.add(chkDateFilter);

        SpinnerDateModel modelFrom = new SpinnerDateModel();
        dateFrom = new JSpinner(modelFrom);
        dateFrom.setEditor(new JSpinner.DateEditor(dateFrom, "dd/MM/yyyy"));
        dateFrom.setPreferredSize(new Dimension(100, 25));
        searchPanel.add(dateFrom);

        searchPanel.add(new JLabel(" - "));

        SpinnerDateModel modelTo = new SpinnerDateModel();
        dateTo = new JSpinner(modelTo);
        dateTo.setEditor(new JSpinner.DateEditor(dateTo, "dd/MM/yyyy"));
        dateTo.setPreferredSize(new Dimension(100, 25));
        searchPanel.add(dateTo);

        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        String[] headers = { "Mã", "Tên Sản Phẩm", "Nhân Viên", "SL Nhập", "SL Xuất", "Giá Nhập", "Ngày", "Ghi Chú" };
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(ThemeColor.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(255, 224, 178));
        table.setSelectionForeground(Color.BLACK);

        // Sự kiện click để xem ghi chú chi tiết
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                // Cột ghi chú là cột cuối cùng (index 7)
                if (row >= 0 && col == 7) {
                    Object noteObj = model.getValueAt(row, 7);
                    String note = noteObj != null ? noteObj.toString() : "";
                    if (!note.isEmpty()) {
                        JOptionPane.showMessageDialog(StockEntryHistoryPanel.this, note, "Chi tiết ghi chú",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // Sự kiện tìm kiếm
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterData();
            }
        });

        chkDateFilter.addActionListener(e -> filterData());
        dateFrom.addChangeListener(e -> {
            if (chkDateFilter.isSelected())
                filterData();
        });
        dateTo.addChangeListener(e -> {
            if (chkDateFilter.isSelected())
                filterData();
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        loadData();
    }

    @Override
    public void refresh() {
        loadData();
    }

    private void loadData() {
        allRecords = bus.getStockEntryHistory();
        if (allRecords == null)
            allRecords = new ArrayList<>();
        filterData();
    }

    private void filterData() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
        model.setRowCount(0);

        if (allRecords == null)
            return;

        boolean filterDate = chkDateFilter.isSelected();
        Date start = null, end = null;
        if (filterDate) {
            start = getStartOfDay((Date) dateFrom.getValue());
            end = getEndOfDay((Date) dateTo.getValue());
        }

        for (StockEntryRecord r : allRecords) {
            // Kiểm tra null trước khi toLowerCase() để tránh lỗi
            String pName = r.getProductName() == null ? "" : r.getProductName().toLowerCase();
            String eName = r.getEmployeeName() == null ? "" : r.getEmployeeName().toLowerCase();
            String id = String.valueOf(r.getEntryId());

            boolean matchKeyword = pName.contains(keyword) || eName.contains(keyword) || id.contains(keyword);
            boolean matchDate = true;

            if (filterDate && r.getEntryDate() != null) {
                if (r.getEntryDate().before(start) || r.getEntryDate().after(end))
                    matchDate = false;
            }

            if (matchKeyword && matchDate) {
                int qty = r.getQuantityAdded();
                String importQty = qty > 0 ? "+" + qty : "-";
                String exportQty = qty < 0 ? String.valueOf(qty) : "-";

                String note = r.getNote() != null ? r.getNote() : "";

                model.addRow(new Object[] {
                        r.getEntryId(),
                        r.getProductName(),
                        r.getEmployeeName(),
                        importQty,
                        exportQty,
                        df.format(r.getEntryPrice()) + " ₫",
                        sdf.format(r.getEntryDate()),
                        note
                });
            }
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