package com.keycapstore.service;

import com.keycapstore.dao.ReviewDAO;
import com.keycapstore.gui.ThemeColor;
import com.keycapstore.gui.components.CustomButton;
import com.keycapstore.gui.components.SearchBox;
import com.keycapstore.gui.components.TableModel;
import com.keycapstore.model.Review;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReviewPanel extends JPanel {

    private static final Color C_PRIMARY = ThemeColor.PRIMARY_DARK;
    private static final Color C_CREAM   = ThemeColor.CREAM_LIGHT;
    private static final Color C_GREY    = ThemeColor.TAUPE_GREY;
    private static final Color C_GLASS   = ThemeColor.GLASS_WHITE;
    private static final Color C_DANGER  = ThemeColor.DANGER_RED;
    private static final Color C_TEXT    = ThemeColor.TEXT_PRIMARY;

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReviewDAO reviewDAO;

    private int currentRatingFilter = 0;

    private TableModel.StyledTable table;
    private TableModel             tableModel;
    private SearchBox              searchBox;

    private JLabel    lblDetailProduct, lblDetailCustomer, lblDetailRating, lblDetailDate;
    private JTextArea txtDetailComment;
    private CustomButton btnDelete, btnRefresh;
    private JLabel    lblCount;

    private JLabel[] lblStarCount = new JLabel[5];
    private JLabel   lblAvgRating;

    public ReviewPanel() {
        this.reviewDAO = new ReviewDAO();
        buildUI();
        loadAll();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_CREAM);
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(C_PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("★");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel title    = new JLabel("Quản Lý Đánh Giá");
        JLabel subtitle = new JLabel("Kiểm duyệt & xóa review vi phạm");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(255, 255, 255, 160));
        titleBlock.add(title);
        titleBlock.add(subtitle);

        left.add(icon);
        left.add(titleBlock);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        searchBox = new SearchBox("Tìm theo sản phẩm / khách hàng...");
        searchBox.setPreferredSize(new Dimension(260, 38));
        searchBox.addSearchListener(kw -> {
            tableModel.filter(kw);
            updateCount();
        });

        right.add(searchBox);
        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setBackground(C_CREAM);
        center.setBorder(new EmptyBorder(14, 14, 14, 14));
        center.add(buildStatsBar(),    BorderLayout.NORTH);
        center.add(buildTableArea(),   BorderLayout.CENTER);
        center.add(buildDetailPanel(), BorderLayout.EAST);
        return center;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        JPanel roundedAvg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roundedAvg.setOpaque(false);
        roundedAvg.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JLabel starIcon = new JLabel("★");
        starIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblAvgRating = new JLabel("—");
        lblAvgRating.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvgRating.setForeground(Color.WHITE);
        JLabel avgLbl = new JLabel("Điểm TB");
        avgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        avgLbl.setForeground(new Color(255, 255, 255, 180));
        roundedAvg.add(starIcon);
        roundedAvg.add(lblAvgRating);
        roundedAvg.add(avgLbl);
        bar.add(roundedAvg);

        for (int i = 1; i <= 5; i++) {
            bar.add(createStarChip(i));
        }

        JButton btnAll = createFilterBtn("Tất cả", 0);
        bar.add(btnAll);

        lblCount = new JLabel();
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(C_GREY);
        bar.add(Box.createHorizontalStrut(16));
        bar.add(lblCount);

        return bar;
    }

    private JPanel createStarChip(int star) {
        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = currentRatingFilter == star;
                g2.setColor(active ? new Color(255, 193, 7) : new Color(255, 255, 255, 180));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                if (!active) {
                    g2.setColor(C_GREY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 3));
        chip.setPreferredSize(new Dimension(55, 28));
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel("★ " + star);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(currentRatingFilter == star ? Color.WHITE : C_PRIMARY);
        chip.add(lbl);

        lblStarCount[star - 1] = new JLabel("");
        lblStarCount[star - 1].setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblStarCount[star - 1].setForeground(C_GREY);

        chip.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentRatingFilter = star;
                filterByRating(star);
                chip.repaint();
                lbl.setForeground(Color.WHITE);
            }
        });

        return chip;
    }

    private JButton createFilterBtn(String text, int rating) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(C_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            currentRatingFilter = rating;
            loadAll();
        });
        return btn;
    }

    private JPanel buildTableArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        String[] cols = {"#ID", "Sản phẩm ID", "Khách hàng ID", "Rating", "Bình luận", "Ngày đăng"};
        tableModel = new TableModel(cols);
        table      = new TableModel.StyledTable(tableModel);

        table.setRowColorizer((row, col, value) -> {
            if (col == 3 && value != null) {
                int r;
                try { r = Integer.parseInt(value.toString()); } catch (Exception ex) { return null; }
                if (r <= 2) return new Color(248, 215, 218);
                if (r == 5) return new Color(212, 237, 218);
            }
            return null;
        });

        int[] widths = {45, 90, 100, 60, 240, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });

        JScrollPane scroll = TableModel.createScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(C_GLASS);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 143, 133, 80)),
                new EmptyBorder(14, 14, 14, 14)));
        panel.setPreferredSize(new Dimension(270, 0));

        JLabel title = new JLabel("Chi Tiết Review");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(C_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(12));

        lblDetailProduct  = buildDetailLabel("—");
        lblDetailCustomer = buildDetailLabel("—");
        lblDetailRating   = buildDetailLabel("—");
        lblDetailDate     = buildDetailLabel("—");

        panel.add(wrapDetail("Sản phẩm ID:", lblDetailProduct));
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapDetail("Khách hàng ID:", lblDetailCustomer));
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapDetail("Rating:", lblDetailRating));
        panel.add(Box.createVerticalStrut(8));
        panel.add(wrapDetail("Ngày đăng:", lblDetailDate));
        panel.add(Box.createVerticalStrut(12));

        JLabel commentLbl = new JLabel("Nội dung bình luận:");
        commentLbl.setFont(FONT_LABEL);
        commentLbl.setForeground(C_TEXT);
        commentLbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(commentLbl);
        panel.add(Box.createVerticalStrut(6));

        txtDetailComment = new JTextArea(5, 0);
        txtDetailComment.setFont(FONT_BODY);
        txtDetailComment.setEditable(false);
        txtDetailComment.setLineWrap(true);
        txtDetailComment.setWrapStyleWord(true);
        txtDetailComment.setBackground(new Color(245, 242, 235));
        txtDetailComment.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane commentScroll = new JScrollPane(txtDetailComment);
        commentScroll.setBorder(BorderFactory.createLineBorder(new Color(153, 143, 133, 80)));
        commentScroll.setAlignmentX(LEFT_ALIGNMENT);
        commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.add(commentScroll);
        panel.add(Box.createVerticalStrut(16));

        JPanel spamBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        spamBox.setBackground(new Color(248, 215, 218));
        spamBox.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60, 80)));
        JLabel spamLbl = new JLabel("Xóa review này vĩnh viễn");
        spamLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        spamLbl.setForeground(new Color(114, 28, 36));
        spamBox.add(spamLbl);
        spamBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        spamBox.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(spamBox);
        panel.add(Box.createVerticalStrut(8));

        btnDelete = new CustomButton("Xóa Review (Spam)", CustomButton.Variant.DANGER);
        btnDelete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnDelete.setAlignmentX(LEFT_ALIGNMENT);
        btnDelete.addActionListener(e -> handleDelete());
        panel.add(btnDelete);

        panel.add(Box.createVerticalGlue());

        btnRefresh = new CustomButton("↻  Tải lại", CustomButton.Variant.GHOST);
        btnRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnRefresh.setAlignmentX(LEFT_ALIGNMENT);
        btnRefresh.addActionListener(e -> loadAll());
        panel.add(btnRefresh);

        return panel;
    }

    // ════════════════════════════════════════════════════════
    //  DATA
    // ════════════════════════════════════════════════════════
    private void loadAll() {
        List<Review> list = reviewDAO.findAll();
        populateTable(list);
        updateStats(list);
        updateCount();
    }

    private void filterByRating(int rating) {
        tableModel.filterByColumn(String.valueOf(rating), 3);
        updateCount();
    }

    private void populateTable(List<Review> list) {
        tableModel.clearAll();
        for (Review r : list) {
            tableModel.addRow(new Object[]{
                r.getReviewId(),
                r.getProductId(),
                r.getCustomerId(),
                r.getRating(),
                truncate(r.getComment(), 60),
                r.getCreatedAt() != null ? r.getCreatedAt().format(DT_FMT) : "-"
            });
        }
    }

    private void updateStats(List<Review> list) {
        if (list.isEmpty()) { lblAvgRating.setText("—"); return; }
        double sum = 0;
        for (Review r : list) {
            if (r.getRating() >= 1 && r.getRating() <= 5) sum += r.getRating();
        }
        lblAvgRating.setText(String.format("%.1f", sum / list.size()));
    }

    private void updateCount() {
        if (lblCount != null)
            lblCount.setText(tableModel.getDisplayedRowCount() + " đánh giá");
    }

    // ════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════
    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        lblDetailProduct.setText(String.valueOf(table.getValueAt(row, 1)));
        lblDetailCustomer.setText(String.valueOf(table.getValueAt(row, 2)));

        Object ratingObj = table.getValueAt(row, 3);
        int rating = 0;
        if (ratingObj != null) {
            try { rating = Integer.parseInt(ratingObj.toString()); }
            catch (NumberFormatException ignored) {}
        }

        if (rating > 0) {
            lblDetailRating.setText("★".repeat(rating) + " (" + rating + "/5)");
            lblDetailRating.setForeground(rating <= 2 ? C_DANGER : new Color(39, 174, 96));
        } else {
            lblDetailRating.setText("Chưa đánh giá");
            lblDetailRating.setForeground(C_GREY);
        }

        lblDetailDate.setText(String.valueOf(table.getValueAt(row, 5)));

        int reviewId    = (int) table.getValueAt(row, 0);
        Review full     = reviewDAO.findById(reviewId);
        txtDetailComment.setText(full != null && full.getComment() != null ? full.getComment() : "");
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Vui lòng chọn review cần xóa!"); return; }
        int reviewId = (int) table.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn XÓA review #" + reviewId + "?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa review",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (reviewDAO.delete(reviewId)) {
                showSuccess("Đã xóa review #" + reviewId + " thành công.");
                clearDetail();
                loadAll();
            } else {
                showError("Xóa thất bại! Vui lòng thử lại.");
            }
        }
    }

    private void clearDetail() {
        lblDetailProduct.setText("—");
        lblDetailCustomer.setText("—");
        lblDetailRating.setText("—");
        lblDetailDate.setText("—");
        txtDetailComment.setText("");
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private JLabel buildDetailLabel(String value) {
        JLabel lbl = new JLabel(value);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(C_TEXT);
        return lbl;
    }

    private JPanel wrapDetail(String keyText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel key = new JLabel(keyText);
        key.setFont(FONT_LABEL);
        key.setForeground(C_GREY);
        key.setPreferredSize(new Dimension(90, 18));
        row.add(key, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
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
}