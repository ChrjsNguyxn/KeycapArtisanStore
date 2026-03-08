package com.keycapstore.service;

import com.keycapstore.bus.WarrantyBUS;
import com.keycapstore.bus.WarrantyBUS.BUSResult;
import com.keycapstore.dao.WarrantyDAO;
import com.keycapstore.gui.ThemeColor;
import com.keycapstore.gui.components.CustomButton;
import com.keycapstore.gui.components.SearchBox;
import com.keycapstore.gui.components.TableModel;
import com.keycapstore.model.Warranty;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WarrantyPanel extends JPanel {

    // ── Design System ────────────────────────────────────────
    private static final Color C_PRIMARY   = ThemeColor.PRIMARY_DARK;   // #3E362E
    private static final Color C_CREAM     = ThemeColor.CREAM_LIGHT;    // #E4DCCF
    private static final Color C_GREY      = ThemeColor.TAUPE_GREY;     // #998F85
    private static final Color C_GLASS     = ThemeColor.GLASS_WHITE;    // #FFFDF5
    private static final Color C_SUCCESS   = ThemeColor.SUCCESS_GREEN;
    private static final Color C_INFO      = ThemeColor.INFO_BLUE;
    private static final Color C_DANGER    = ThemeColor.DANGER_RED;
    private static final Color C_TEXT      = ThemeColor.TEXT_PRIMARY;

    private static final Font  FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font  FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── BUS / DAO ────────────────────────────────────────────
    private final WarrantyBUS warrantyBUS;
    private final WarrantyDAO warrantyDAO;

    // ── State ────────────────────────────────────────────────
    private int    loggedEmployeeId = 1; // TODO: truyền vào từ session
    private String currentFilter    = "ALL";

    // ── UI Components ────────────────────────────────────────
    private TableModel.StyledTable table;
    private TableModel             tableModel;
    private SearchBox              searchBox;

    // Form tiếp nhận
    private JTextField  txtOrderItemId, txtCustomerId;
    private JTextArea   txtIssue;
    private CustomButton btnSubmitNew;

    // Form xử lý
    private JTextArea   txtNote;
    private CustomButton btnApprove, btnReject, btnProcessRefund,
                         btnProcessExchange, btnComplete;

    // Info label
    private JLabel lblStatus;

    public WarrantyPanel() {
        this.warrantyBUS = new WarrantyBUS();
        this.warrantyDAO = new WarrantyDAO();
        buildUI();
        loadData("ALL");
    }

    // ════════════════════════════════════════════════════════
    //  BUILD UI
    // ════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_CREAM);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── HEADER ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(C_PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        // Left: icon + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 0));
        titleBlock.setOpaque(false);
        JLabel title    = new JLabel("Quản Lý Bảo Hành");
        JLabel subtitle = new JLabel("Tiếp nhận & xử lý yêu cầu bảo hành");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(255, 255, 255, 160));
        titleBlock.add(title);
        titleBlock.add(subtitle);

        left.add(titleBlock);

        // Right: search box
        searchBox = new SearchBox("Tìm kiếm yêu cầu...");
        searchBox.setPreferredSize(new Dimension(240, 38));
        searchBox.addSearchListener(kw -> {
            tableModel.filter(kw);
            setStatus("Tìm: \"" + kw + "\" — " + tableModel.getDisplayedRowCount() + " kết quả");
        });

        header.add(left, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.EAST);
        return header;
    }

    // ── CONTENT (3 columns) ──────────────────────────────────
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setBackground(C_CREAM);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Left: filter tabs + table
        content.add(buildTableSection(), BorderLayout.CENTER);

        // Right: action panels
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);
        rightColumn.setPreferredSize(new Dimension(300, 0));
        rightColumn.add(buildNewRequestCard());
        rightColumn.add(Box.createVerticalStrut(12));
        rightColumn.add(buildActionCard());

        content.add(rightColumn, BorderLayout.EAST);
        return content;
    }

    // ── TABLE SECTION ────────────────────────────────────────
    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Filter tab bar
        panel.add(buildFilterBar(), BorderLayout.NORTH);

        // Table
        String[] cols = {"#ID", "Order Item", "Khách hàng", "Lý do", "Trạng thái", "Ngày gửi", "Ghi chú"};
        tableModel = new TableModel(cols);
        table      = new TableModel.StyledTable(tableModel);

        // Color rows by status
        table.setRowColorizer((row, col, value) -> {
            if (col == 4 && value != null) {
                switch (value.toString().toLowerCase()) {
                    case "pending":     return new Color(255, 243, 205);
                    case "approved":    return new Color(209, 236, 241);
                    case "in_progress": return new Color(204, 229, 255);
                    case "completed":   return new Color(212, 237, 218);
                    case "rejected":    return new Color(248, 215, 218);
                }
            }
            return null;
        });

        // Column widths
        int[] widths = {45, 80, 90, 160, 90, 110, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = TableModel.createScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── FILTER BAR ───────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setOpaque(false);

        String[][] filters = {
            {"ALL",         "Tất cả"},
            {"pending",     "Chờ duyệt"},
            {"approved",    "Đã duyệt"},
            {"in_progress", "Đang xử lý"},
            {"completed",   "Hoàn tất"},
            {"rejected",    "Từ chối"}
        };

        for (String[] f : filters) {
            JButton btn = createFilterTab(f[0], f[1]);
            bar.add(btn);
        }
        return bar;
    }

    private JButton createFilterTab(String filterKey, String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = filterKey.equals(currentFilter);
                g2.setColor(active ? C_PRIMARY : new Color(255, 255, 255, 180));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                if (!active) {
                    g2.setColor(C_GREY);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(filterKey.equals(currentFilter) ? Color.WHITE : C_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 30));

        btn.addActionListener(e -> {
            currentFilter = filterKey;
            loadData(filterKey);
            // Repaint toàn bộ bar
            JPanel bar = (JPanel) btn.getParent();
            for (Component c : bar.getComponents()) c.repaint();
        });
        return btn;
    }

    // ── NEW REQUEST CARD ─────────────────────────────────────
    private JPanel buildNewRequestCard() {
        JPanel card = buildCard("Tiếp Nhận Yêu Cầu Mới");

        addFormRow(card, "Order Item ID:");
        txtOrderItemId = buildTextField("Mã chi tiết đơn hàng");
        card.add(txtOrderItemId);
        card.add(Box.createVerticalStrut(8));

        addFormRow(card, "Customer ID:");
        txtCustomerId = buildTextField("Mã khách hàng");
        card.add(txtCustomerId);
        card.add(Box.createVerticalStrut(8));

        addFormRow(card, "Mô tả vấn đề:");
        txtIssue = buildTextArea(4);
        JScrollPane sp = new JScrollPane(txtIssue);
        sp.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        sp.setBorder(fieldBorder());
        card.add(sp);
        card.add(Box.createVerticalStrut(12));

        btnSubmitNew = new CustomButton("Gửi Yêu Cầu", CustomButton.Variant.SUCCESS);
        btnSubmitNew.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSubmitNew.addActionListener(e -> handleCreateRequest());
        card.add(btnSubmitNew);

        return card;
    }

    // ── ACTION CARD ──────────────────────────────────────────
    private JPanel buildActionCard() {
        JPanel card = buildCard("Xử Lý Yêu Cầu Đã Chọn");

        addFormRow(card, "Ghi chú / Phương án:");
        txtNote = buildTextArea(3);
        JScrollPane sp = new JScrollPane(txtNote);
        sp.setPreferredSize(new Dimension(Integer.MAX_VALUE, 70));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        sp.setBorder(fieldBorder());
        card.add(sp);
        card.add(Box.createVerticalStrut(10));

        // Row 1: Approve / Reject
        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnApprove = new CustomButton("Duyệt",  CustomButton.Variant.SUCCESS, CustomButton.Size.SMALL);
        btnReject  = new CustomButton("Từ chối", CustomButton.Variant.DANGER,  CustomButton.Size.SMALL);
        row1.add(btnApprove);
        row1.add(btnReject);
        card.add(row1);
        card.add(Box.createVerticalStrut(6));

        // Row 2: Refund / Exchange
        JPanel row2 = new JPanel(new GridLayout(1, 2, 8, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnProcessRefund   = new CustomButton("Hoàn tiền",   CustomButton.Variant.INFO, CustomButton.Size.SMALL);
        btnProcessExchange = new CustomButton("Đổi hàng",    CustomButton.Variant.INFO, CustomButton.Size.SMALL);
        row2.add(btnProcessRefund);
        row2.add(btnProcessExchange);
        card.add(row2);
        card.add(Box.createVerticalStrut(6));

        // Row 3: Complete
        btnComplete = new CustomButton("Hoàn tất bảo hành", CustomButton.Variant.PRIMARY);
        btnComplete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(btnComplete);

        // Wire actions
        btnApprove.addActionListener(e -> handleAction("approve"));
        btnReject.addActionListener(e -> handleAction("reject"));
        btnProcessRefund.addActionListener(e -> handleAction("refund"));
        btnProcessExchange.addActionListener(e -> handleAction("exchange"));
        btnComplete.addActionListener(e -> handleAction("complete"));

        return card;
    }

    // ── STATUS BAR ───────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        bar.setBackground(C_PRIMARY);

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(255, 255, 255, 180));
        bar.add(lblStatus);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setForeground(C_CREAM);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData(currentFilter));
        bar.add(btnRefresh);

        return bar;
    }

    // ════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════
    private void loadData(String filter) {
        tableModel.clearAll();
        List<Warranty> list = "ALL".equals(filter)
                ? warrantyDAO.findAll()
                : warrantyDAO.findByStatus(filter);

        for (Warranty w : list) {
            tableModel.addRow(new Object[]{
                w.getWarrantyId(),
                w.getOrderItemId(),
                w.getCustomerId(),
                truncate(w.getReason(), 40),
                w.getStatus(),
                w.getRequestDate() != null ? w.getRequestDate().format(DT_FMT) : "-",
                truncate(w.getResponseNote(), 40)
            });
        }
        setStatus(filter.equals("ALL") ? "Tất cả" : "Filter: " + filter
                + " — " + list.size() + " yêu cầu");
    }

    // ════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ════════════════════════════════════════════════════════
    private void handleCreateRequest() {
        String oidStr = txtOrderItemId.getText().trim();
        String cidStr = txtCustomerId.getText().trim();
        String issue  = txtIssue.getText().trim();

        int orderItemId, customerId;
        try {
            orderItemId = Integer.parseInt(oidStr);
            customerId  = Integer.parseInt(cidStr);
        } catch (NumberFormatException ex) {
            showError("Order Item ID và Customer ID phải là số nguyên.");
            return;
        }

        BUSResult result = warrantyBUS.createWarrantyRequest(orderItemId, customerId, issue);
        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            txtOrderItemId.setText("");
            txtCustomerId.setText("");
            txtIssue.setText("");
            loadData(currentFilter);
        } else {
            showError(result.getMessage());
        }
    }

    private void handleAction(String action) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Vui lòng chọn một yêu cầu bảo hành trong bảng!");
            return;
        }

        int warrantyId = (int) table.getValueAt(selectedRow, 0);
        String note    = txtNote.getText().trim();

        BUSResult result;
        switch (action) {
            case "approve":
                if (note.isEmpty()) { showError("Nhập phương án xử lý dự kiến."); return; }
                result = warrantyBUS.approveWarranty(warrantyId, loggedEmployeeId, note);
                break;
            case "reject":
                if (note.isEmpty()) { showError("Nhập lý do từ chối."); return; }
                result = warrantyBUS.rejectWarranty(warrantyId, loggedEmployeeId, note);
                break;
            case "refund":
                result = warrantyBUS.processReturn(warrantyId, loggedEmployeeId, WarrantyBUS.RETURN_REFUND, note);
                break;
            case "exchange":
                result = warrantyBUS.processReturn(warrantyId, loggedEmployeeId, WarrantyBUS.RETURN_EXCHANGE, note);
                break;
            case "complete":
                result = warrantyBUS.completeWarranty(warrantyId, loggedEmployeeId, note);
                break;
            default:
                return;
        }

        if (result.isSuccess()) {
            showSuccess(result.getMessage());
            txtNote.setText("");
            loadData(currentFilter);
        } else {
            showError(result.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_GLASS);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 143, 133, 80), 1),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel lbl = new JLabel(titleText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(C_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    private void addFormRow(JPanel card, String labelText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
    }

    private JTextField buildTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(fieldBorder());
        tf.setBackground(Color.WHITE);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        tf.putClientProperty("placeholder", placeholder);
        return tf;
    }

    private JTextArea buildTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 0);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(6, 8, 6, 8));
        ta.setBackground(Color.WHITE);
        return ta;
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 143, 133, 120), 1),
                new EmptyBorder(4, 8, 4, 8));
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    /** Cho phép set employeeId từ session sau khi đăng nhập */
    public void setLoggedEmployeeId(int id) {
        this.loggedEmployeeId = id;
    }
}