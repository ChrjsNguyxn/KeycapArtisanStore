package com.keycapstore.service;

import com.keycapstore.dao.WishlistDAO;
import com.keycapstore.gui.ThemeColor;
import com.keycapstore.gui.components.CustomButton;
import com.keycapstore.gui.components.SearchBox;
import com.keycapstore.gui.components.TableModel;
import com.keycapstore.model.Wishlist;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WishlistPanel extends JPanel {

    private static final Color C_PRIMARY = ThemeColor.PRIMARY_DARK;
    private static final Color C_CREAM   = ThemeColor.CREAM_LIGHT;
    private static final Color C_GREY    = ThemeColor.TAUPE_GREY;
    private static final Color C_GLASS   = ThemeColor.GLASS_WHITE;
    private static final Color C_SUCCESS = ThemeColor.SUCCESS_GREEN;
    private static final Color C_INFO    = ThemeColor.INFO_BLUE;
    private static final Color C_TEXT    = ThemeColor.TEXT_PRIMARY;
    private static final Color C_HOT     = new Color(230, 57, 70);
    private static final Color C_WARM    = new Color(255, 193, 7);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int HOT_THRESHOLD = 3;

    private final WishlistDAO wishlistDAO;

    private List<Wishlist>     allData       = new ArrayList<>();
    private Map<Integer, Long> productHeatMap = new LinkedHashMap<>();

    private TableModel.StyledTable table;
    private TableModel             tableModel;
    private SearchBox              searchBox;

    private JPanel  hotListPanel;

    private JLabel  lblTotalWishlists, lblUniqueProducts, lblUniqueCustomers;
    private JPanel  chipTotalWishlists, chipUniqueProducts, chipUniqueCustomers;

    private JTextField   txtPromoCode, txtPromoDiscount;
    private JTextArea    txtPromoNote;
    private JComboBox<String> cmbTargetProduct;
    private CustomButton btnSendPromo;

    private JTextField   txtLookupCustomer;
    private CustomButton btnLookup;

    public WishlistPanel() {
        this.wishlistDAO = new WishlistDAO();
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

        JLabel icon = new JLabel("♥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel title    = new JLabel("Danh Sách Yêu Thích");
        JLabel subtitle = new JLabel("Phân tích wishlist & gợi ý khuyến mãi sản phẩm hot");
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

        txtLookupCustomer = new JTextField(10);
        txtLookupCustomer.setFont(FONT_BODY);
        txtLookupCustomer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80)),
                new EmptyBorder(4, 8, 4, 8)));
        txtLookupCustomer.setBackground(new Color(255, 255, 255, 30));
        txtLookupCustomer.setForeground(Color.WHITE);
        txtLookupCustomer.setCaretColor(Color.WHITE);
        txtLookupCustomer.setPreferredSize(new Dimension(130, 36));
        txtLookupCustomer.setText("Customer ID...");
        txtLookupCustomer.setForeground(new Color(255, 255, 255, 120));
        txtLookupCustomer.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtLookupCustomer.getText().equals("Customer ID...")) {
                    txtLookupCustomer.setText("");
                    txtLookupCustomer.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtLookupCustomer.getText().isEmpty()) {
                    txtLookupCustomer.setText("Customer ID...");
                    txtLookupCustomer.setForeground(new Color(255, 255, 255, 120));
                }
            }
        });

        btnLookup = new CustomButton("Xem", CustomButton.Variant.WARNING, CustomButton.Size.SMALL);
        btnLookup.addActionListener(e -> handleCustomerLookup());

        searchBox = new SearchBox("Tìm product ID / customer ID...");
        searchBox.setPreferredSize(new Dimension(230, 38));
        searchBox.addSearchListener(kw -> tableModel.filter(kw));

        right.add(new JLabel("<html><font color='#FFFFFF99' size='2'>Lọc theo KH:</font></html>"));
        right.add(txtLookupCustomer);
        right.add(btnLookup);
        right.add(Box.createHorizontalStrut(8));
        right.add(searchBox);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setBackground(C_CREAM);
        center.setBorder(new EmptyBorder(14, 14, 14, 14));

        center.add(buildStatsRow(),  BorderLayout.NORTH);

        JPanel middleRow = new JPanel(new BorderLayout(12, 0));
        middleRow.setOpaque(false);
        middleRow.add(buildTablePanel(),   BorderLayout.CENTER);
        middleRow.add(buildRightColumn(),  BorderLayout.EAST);

        center.add(middleRow, BorderLayout.CENTER);
        return center;
    }

    // ── FIX #6: buildStatChip trả về JPanel, lưu JLabel riêng ──────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        // Build chips — lưu cả panel lẫn label
        chipTotalWishlists  = buildStatChip("♥", "Tổng Wishlists", "0", C_HOT);
        chipUniqueProducts  = buildStatChip("📦", "Sản phẩm",      "0", C_PRIMARY);
        chipUniqueCustomers = buildStatChip("👤", "Khách hàng",    "0", C_INFO);

        // Lấy label từ chip để update sau (label nằm ở component index 1 → textCol → index 0)
        lblTotalWishlists  = extractValueLabel(chipTotalWishlists);
        lblUniqueProducts  = extractValueLabel(chipUniqueProducts);
        lblUniqueCustomers = extractValueLabel(chipUniqueCustomers);

        row.add(chipTotalWishlists);   // add trực tiếp chip panel — không cần getParent()
        row.add(chipUniqueProducts);
        row.add(chipUniqueCustomers);

        JButton btnReload = new JButton("↻ Tải lại");
        btnReload.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReload.setForeground(C_PRIMARY);
        btnReload.setContentAreaFilled(false);
        btnReload.setBorderPainted(false);
        btnReload.setFocusPainted(false);
        btnReload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReload.addActionListener(e -> loadAll());
        row.add(Box.createHorizontalStrut(12));
        row.add(btnReload);

        return row;
    }

    /**
     * FIX #6: buildStatChip giờ trả về JPanel thay vì JLabel,
     * tránh gọi getParent() trên label chưa được add vào container.
     */
    private JPanel buildStatChip(String emoji, String label, String value, Color accent) {
        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_GLASS);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        chip.setPreferredSize(new Dimension(160, 60));

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 0));
        textCol.setOpaque(false);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLbl.setForeground(accent);

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLbl.setForeground(C_GREY);

        textCol.add(valueLbl);
        textCol.add(labelLbl);

        chip.add(emojiLbl);
        chip.add(textCol);

        return chip; // trả về chip panel
    }

    /**
     * Helper lấy JLabel value từ chip panel đã build.
     * Cấu trúc: chip → FlowLayout → [emojiLbl, textCol(GridLayout) → [valueLbl, labelLbl]]
     */
    private JLabel extractValueLabel(JPanel chip) {
        // component 1 = textCol (JPanel GridLayout)
        JPanel textCol = (JPanel) chip.getComponent(1);
        return (JLabel) textCol.getComponent(0); // component 0 = valueLbl
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_GLASS);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 143, 133, 80)),
                new EmptyBorder(0, 0, 0, 0)));

        JLabel header = new JLabel("  Danh Sách Wishlist");
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(C_PRIMARY);
        header.setOpaque(true);
        header.setBackground(new Color(240, 235, 225));
        header.setBorder(new EmptyBorder(10, 14, 10, 14));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"#ID", "Customer ID", "Product ID", "Ngày thêm"};
        tableModel = new TableModel(cols);
        table      = new TableModel.StyledTable(tableModel);

        int[] widths = {50, 100, 100, 140};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = TableModel.createScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRightColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.setPreferredSize(new Dimension(290, 0));
        col.add(buildHotProductsCard());
        col.add(Box.createVerticalStrut(12));
        col.add(buildPromoCard());
        return col;
    }

    private JPanel buildHotProductsCard() {
        JPanel card = buildCard("Sản Phẩm Hot (Top Wishlist)");
        hotListPanel = new JPanel();
        hotListPanel.setLayout(new BoxLayout(hotListPanel, BoxLayout.Y_AXIS));
        hotListPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(hotListPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 180));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.add(scroll);
        return card;
    }

    private JPanel buildPromoCard() {
        JPanel card = buildCard("Gợi Ý Khuyến Mãi");

        addFormRow(card, "Sản phẩm mục tiêu (Product ID):");
        cmbTargetProduct = new JComboBox<>();
        cmbTargetProduct.setFont(FONT_BODY);
        cmbTargetProduct.setBackground(Color.WHITE);
        cmbTargetProduct.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cmbTargetProduct.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbTargetProduct);
        card.add(Box.createVerticalStrut(8));

        addFormRow(card, "Mã khuyến mãi:");
        txtPromoCode = buildTextField("VD: SUMMER25");
        card.add(txtPromoCode);
        card.add(Box.createVerticalStrut(8));

        addFormRow(card, "Mức giảm giá (%):");
        txtPromoDiscount = buildTextField("VD: 15");
        card.add(txtPromoDiscount);
        card.add(Box.createVerticalStrut(8));

        addFormRow(card, "Ghi chú nội dung gợi ý:");
        txtPromoNote = new JTextArea(2, 0);
        txtPromoNote.setFont(FONT_BODY);
        txtPromoNote.setLineWrap(true);
        txtPromoNote.setWrapStyleWord(true);
        txtPromoNote.setBorder(new EmptyBorder(6, 8, 6, 8));
        txtPromoNote.setBackground(Color.WHITE);
        JScrollPane noteScroll = new JScrollPane(txtPromoNote);
        noteScroll.setBorder(fieldBorder());
        noteScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 55));
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        noteScroll.setAlignmentX(LEFT_ALIGNMENT);
        card.add(noteScroll);
        card.add(Box.createVerticalStrut(12));

        JLabel infoLbl = new JLabel(
            "<html><font color='#998F85' size='2'>Gợi ý sẽ được ghi nhận để gửi voucher<br>cho khách đã wishlist sản phẩm này.</font></html>");
        infoLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(infoLbl);
        card.add(Box.createVerticalStrut(10));

        btnSendPromo = new CustomButton("Tạo Gợi Ý Khuyến Mãi", CustomButton.Variant.SUCCESS);
        btnSendPromo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSendPromo.setAlignmentX(LEFT_ALIGNMENT);
        btnSendPromo.addActionListener(e -> handleSendPromo());
        card.add(btnSendPromo);

        return card;
    }

    private void loadAll() {
        allData = wishlistDAO.findAll();
        populateTable(allData);
        rebuildStats(allData);
        refreshHotList();
        refreshPromoDropdown();
    }

    private void populateTable(List<Wishlist> data) {
        tableModel.clearAll();
        for (Wishlist w : data) {
            tableModel.addRow(new Object[]{
                w.getWishlistId(),
                w.getCustomerId(),
                w.getProductId(),
                w.getCreatedAt() != null ? w.getCreatedAt().format(DT_FMT) : "-"
            });
        }
    }

    private void rebuildStats(List<Wishlist> data) {
        Set<Integer> uniqueProducts  = new HashSet<>();
        Set<Integer> uniqueCustomers = new HashSet<>();
        Map<Integer, Long> heatMap   = new LinkedHashMap<>();

        for (Wishlist w : data) {
            uniqueProducts.add(w.getProductId());
            uniqueCustomers.add(w.getCustomerId());
            heatMap.merge(w.getProductId(), 1L, Long::sum);
        }

        productHeatMap = heatMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        lblTotalWishlists.setText(String.valueOf(data.size()));
        lblUniqueProducts.setText(String.valueOf(uniqueProducts.size()));
        lblUniqueCustomers.setText(String.valueOf(uniqueCustomers.size()));
    }

    private void refreshHotList() {
        hotListPanel.removeAll();
        if (productHeatMap.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu wishlist");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(C_GREY);
            hotListPanel.add(empty);
        }
        int rank = 1;
        for (Map.Entry<Integer, Long> entry : productHeatMap.entrySet()) {
            if (rank > 8) break;
            hotListPanel.add(buildHotRow(rank, entry.getKey(), entry.getValue()));
            hotListPanel.add(Box.createVerticalStrut(4));
            rank++;
        }
        hotListPanel.revalidate();
        hotListPanel.repaint();
    }

    private JPanel buildHotRow(int rank, int productId, long count) {
        boolean isHot = count >= HOT_THRESHOLD;
        JPanel row = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isHot ? new Color(255, 240, 240) : new Color(248, 246, 240);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                if (isHot) {
                    g2.setColor(new Color(230, 57, 70, 80));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 10, 6, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rankLbl = new JLabel(rank <= 3 ? rank + "." : String.format("%2d", rank));
        rankLbl.setFont(rank <= 3
                ? new Font("Segoe UI Emoji", Font.PLAIN, 16)
                : new Font("Segoe UI", Font.BOLD, 12));
        rankLbl.setForeground(C_GREY);
        rankLbl.setPreferredSize(new Dimension(28, 24));

        JLabel prodLbl = new JLabel("Product #" + productId);
        prodLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prodLbl.setForeground(C_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        JLabel countLbl = new JLabel(count + " likes");
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLbl.setForeground(isHot ? C_HOT : C_GREY);

        if (isHot) {
            JLabel hotBadge = new JLabel(" HOT ");
            hotBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
            hotBadge.setForeground(Color.WHITE);
            hotBadge.setOpaque(true);
            hotBadge.setBackground(C_HOT);
            hotBadge.setBorder(new EmptyBorder(1, 4, 1, 4));
            right.add(hotBadge);
        }
        right.add(countLbl);

        row.add(rankLbl, BorderLayout.WEST);
        row.add(prodLbl, BorderLayout.CENTER);
        row.add(right,   BorderLayout.EAST);
        return row;
    }

    private void refreshPromoDropdown() {
        cmbTargetProduct.removeAllItems();
        for (Integer productId : productHeatMap.keySet()) {
            long count = productHeatMap.get(productId);
            cmbTargetProduct.addItem("Product #" + productId + "  (" + count + " ♥)");
        }
    }

    // ════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════
    private void handleCustomerLookup() {
        String txt = txtLookupCustomer.getText().trim();
        if (txt.isEmpty() || txt.equals("Customer ID...")) {
            populateTable(allData);
            return;
        }
        int customerId;
        try {
            customerId = Integer.parseInt(txt);
        } catch (NumberFormatException ex) {
            showError("Customer ID phải là số nguyên.");
            return;
        }
        List<Wishlist> filtered = wishlistDAO.findByCustomerId(customerId);
        populateTable(filtered);
        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Khách hàng #" + customerId + " chưa có sản phẩm nào trong wishlist.",
                    "Không tìm thấy", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleSendPromo() {
        String code     = txtPromoCode.getText().trim();
        String discount = txtPromoDiscount.getText().trim();
        String note     = txtPromoNote.getText().trim();
        Object selected = cmbTargetProduct.getSelectedItem();

        if (code.isEmpty() || discount.isEmpty()) {
            showError("Vui lòng nhập đầy đủ mã khuyến mãi và mức giảm giá!");
            return;
        }
        double discountVal;
        try {
            discountVal = Double.parseDouble(discount);
            if (discountVal <= 0 || discountVal > 100) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Mức giảm giá phải là số từ 1 đến 100.");
            return;
        }

        int targetProductId = extractProductId(selected != null ? selected.toString() : "");
        long customerCount  = allData.stream()
                .filter(w -> w.getProductId() == targetProductId)
                .map(Wishlist::getCustomerId)
                .distinct().count();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format(
                    "Tạo gợi ý khuyến mãi:\n" +
                    "  • Sản phẩm: Product #%d\n" +
                    "  • Mã: %s  |  Giảm: %.0f%%\n" +
                    "  • Áp dụng cho: %d khách hàng đã wishlist\n\nXác nhận?",
                    targetProductId, code, discountVal, customerCount),
                "Xác nhận tạo khuyến mãi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.printf("[PROMO] Code=%s | Discount=%.0f%% | ProductId=%d | Customers=%d | Note=%s%n",
                    code, discountVal, targetProductId, customerCount, note);
            showSuccess(String.format(
                    "Đã tạo gợi ý khuyến mãi \"%s\" (%.0f%%) cho Product #%d!\n"
                    + "Áp dụng cho %d khách hàng đã wishlist sản phẩm này.",
                    code, discountVal, targetProductId, customerCount));
            txtPromoCode.setText("");
            txtPromoDiscount.setText("");
            txtPromoNote.setText("");
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
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
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
        tf.setAlignmentX(LEFT_ALIGNMENT);
        return tf;
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 143, 133, 120), 1),
                new EmptyBorder(4, 8, 4, 8));
    }

    private int extractProductId(String comboText) {
        try {
            String trimmed = comboText.trim().replaceAll("\\s+", " ");
            String[] parts = trimmed.split(" ");
            return Integer.parseInt(parts[1].replace("#", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}