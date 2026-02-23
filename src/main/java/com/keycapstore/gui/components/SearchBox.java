package com.keycapstore.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║              SearchBox - Component Dùng Chung            ║
 * ║           Keyforge Artisan Store - Design System         ║
 * ╚══════════════════════════════════════════════════════════╝
 *
 * CÁCH DÙNG - CĂN BẢN:
 *   SearchBox searchBox = new SearchBox("Tìm kiếm sản phẩm...");
 *   panel.add(searchBox);
 *
 *   // Lắng nghe sự kiện nhập liệu theo thời gian thực
 *   searchBox.addSearchListener(text -> {
 *       // text = nội dung người dùng đang gõ
 *       filterTable(text);
 *   });
 *
 * CÁCH DÙNG - NÂNG CAO:
 *   // Lắng nghe khi nhấn Enter
 *   searchBox.addActionListener(e -> {
 *       String query = searchBox.getText();
 *       performSearch(query);
 *   });
 *
 *   // Lấy text hiện tại
 *   String keyword = searchBox.getText();
 *
 *   // Xóa nội dung bằng code
 *   searchBox.clear();
 *
 *   // Thay đổi placeholder
 *   searchBox.setPlaceholder("Nhập tên khách hàng...");
 *
 *   // Thay đổi chiều rộng
 *   searchBox.setPreferredSize(new Dimension(300, 40));
 */
public class SearchBox extends JPanel {

    // ══════════════════════════════════════════════
    //  DESIGN SYSTEM - BẢNG MÀU CHUẨN
    // ══════════════════════════════════════════════
    private static final Color COLOR_PRIMARY_DARK  = new Color(62,  54,  46);   // #3E362E
    private static final Color COLOR_CREAM_LIGHT   = new Color(228, 220, 207);  // #E4DCCF
    private static final Color COLOR_TAUPE_GREY    = new Color(153, 143, 133);  // #998F85
    private static final Color COLOR_GLASS_WHITE   = new Color(255, 252, 245);  // #FFFDF5
    private static final Color COLOR_TEXT_PRIMARY  = new Color(51,  51,  51);   // #333333
    private static final Color COLOR_INPUT_BG      = new Color(62,  54,  46, 18);
    private static final Color COLOR_BORDER        = new Color(153, 143, 133, 100);
    private static final Color COLOR_BORDER_FOCUS  = new Color(62,  54,  46,  180);
    private static final Color COLOR_PLACEHOLDER   = new Color(153, 143, 133);

    // ══════════════════════════════════════════════
    //  FIELDS
    // ══════════════════════════════════════════════
    private JTextField    txtInput;
    private JLabel        lblSearchIcon;
    private JButton       btnClear;
    private String        placeholder;
    private boolean       focused    = false;
    private boolean       hovered    = false;
    private SearchListener searchListener;

    // ══════════════════════════════════════════════
    //  INTERFACE - Callback khi có thay đổi
    // ══════════════════════════════════════════════
    @FunctionalInterface
    public interface SearchListener {
        /**
         * Gọi mỗi khi nội dung thay đổi (realtime)
         * @param text - Nội dung hiện tại trong ô tìm kiếm
         */
        void onSearch(String text);
    }

    // ══════════════════════════════════════════════
    //  CONSTRUCTORS
    // ══════════════════════════════════════════════

    /** SearchBox mặc định với placeholder "Tìm kiếm..." */
    public SearchBox() {
        this("Tìm kiếm...");
    }

    /** SearchBox với placeholder tùy chỉnh */
    public SearchBox(String placeholder) {
        this.placeholder = placeholder;
        initialize();
    }

    // ══════════════════════════════════════════════
    //  SETUP
    // ══════════════════════════════════════════════
    private void initialize() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(250, 40));
        setCursor(new Cursor(Cursor.TEXT_CURSOR));

        // --- Icon kính lúp (Unicode) ---
        lblSearchIcon = new JLabel("\uD83D\uDD0D"); // 🔍
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblSearchIcon.setForeground(COLOR_TAUPE_GREY);
        lblSearchIcon.setBorder(new EmptyBorder(0, 12, 0, 6));
        lblSearchIcon.setCursor(new Cursor(Cursor.TEXT_CURSOR));

        // --- Input field ---
        txtInput = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                // Vẽ nền trong suốt (panel cha đã vẽ bg)
                super.paintComponent(g);

                // Vẽ placeholder khi rỗng và không focus
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_PLACEHOLDER);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(SearchBox.this.placeholder, 2, y);
                }
            }
        };
        txtInput.setOpaque(false);
        txtInput.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.setForeground(COLOR_TEXT_PRIMARY);
        txtInput.setCaretColor(COLOR_PRIMARY_DARK);

        // --- Nút X (xóa) ---
        btnClear = new JButton("✕");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.setForeground(COLOR_TAUPE_GREY);
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setBorder(new EmptyBorder(0, 4, 0, 10));
        btnClear.setVisible(false); // Ẩn khi chưa nhập gì

        // --- Lắp vào panel ---
        add(lblSearchIcon, BorderLayout.WEST);
        add(txtInput,      BorderLayout.CENTER);
        add(btnClear,      BorderLayout.EAST);

        // --- Sự kiện ---
        setupEvents();
    }

    private void setupEvents() {
        // === Lắng nghe thay đổi text realtime ===
        txtInput.getDocument().addDocumentListener(new DocumentListener() {
            private void onChanged() {
                String text = txtInput.getText();
                // Hiện/ẩn nút X
                btnClear.setVisible(!text.isEmpty());
                // Callback cho SearchListener
                if (searchListener != null) {
                    searchListener.onSearch(text);
                }
                repaint();
            }

            @Override public void insertUpdate (DocumentEvent e) { onChanged(); }
            @Override public void removeUpdate (DocumentEvent e) { onChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onChanged(); }
        });

        // === Focus: đổi màu viền ===
        txtInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                lblSearchIcon.setForeground(COLOR_PRIMARY_DARK);
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                lblSearchIcon.setForeground(COLOR_TAUPE_GREY);
                repaint();
            }
        });

        // === Hover hiệu ứng ===
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
        };
        addMouseListener(hoverListener);
        lblSearchIcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { txtInput.requestFocus(); }
        });

        // === Nút X: xóa nội dung ===
        btnClear.addActionListener(e -> {
            txtInput.setText("");
            txtInput.requestFocus();
        });

        // === Hover nút X: đổi màu ===
        btnClear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClear.setForeground(new Color(192, 57, 43)); // Đỏ khi hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnClear.setForeground(COLOR_TAUPE_GREY);
            }
        });
    }

    // ══════════════════════════════════════════════
    //  PAINT - Vẽ khung tìm kiếm
    // ══════════════════════════════════════════════
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w   = getWidth()  - 1;
        int h   = getHeight() - 1;
        int arc = 20; // Bo góc tròn

        // --- Đổ bóng nhẹ ---
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fillRoundRect(1, 2, w - 1, h, arc, arc);

        // --- Nền ---
        Color bgColor = focused
            ? new Color(255, 252, 245)           // Trắng kem khi focus
            : hovered
                ? new Color(255, 252, 245, 220)  // Nhạt hơn khi hover
                : COLOR_GLASS_WHITE;
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // --- Viền ---
        g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
        g2.setColor(focused ? COLOR_BORDER_FOCUS : (hovered ? COLOR_TAUPE_GREY : COLOR_BORDER));
        g2.drawRoundRect(0, 0, w, h, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

    // ══════════════════════════════════════════════
    //  PUBLIC API
    // ══════════════════════════════════════════════

    /**
     * Lắng nghe thay đổi realtime (gõ từng ký tự)
     * @param listener - Callback nhận String text
     */
    public void addSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }

    /**
     * Lắng nghe sự kiện nhấn Enter
     * @param listener - ActionListener
     */
    public void addActionListener(ActionListener listener) {
        txtInput.addActionListener(listener);
    }

    /**
     * Lấy nội dung hiện tại trong ô tìm kiếm
     * @return String text
     */
    public String getText() {
        return txtInput.getText().trim();
    }

    /**
     * Đặt nội dung cho ô tìm kiếm bằng code
     * @param text - Nội dung cần đặt
     */
    public void setText(String text) {
        txtInput.setText(text);
    }

    /**
     * Xóa toàn bộ nội dung
     */
    public void clear() {
        txtInput.setText("");
    }

    /**
     * Thay đổi placeholder text
     * @param placeholder - Chữ gợi ý hiển thị khi chưa nhập
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    /**
     * Lấy JTextField bên trong nếu cần thao tác nâng cao
     * @return JTextField
     */
    public JTextField getTextField() {
        return txtInput;
    }

    /**
     * Focus vào ô nhập liệu
     */
    @Override
    public boolean requestFocusInWindow() {
        return txtInput.requestFocusInWindow();
    }
}