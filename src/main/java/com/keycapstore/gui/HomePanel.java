package com.keycapstore.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

public class HomePanel extends JPanel {

    // ══════════════════════════════════════════════════════════
    //  DESIGN TOKENS — ánh xạ trực tiếp từ ThemeColor.java
    //
    //  ThemeColor (gui):
    //    PRIMARY_DARK = (62, 54, 46)     → nâu đậm chính
    //    CREAM_LIGHT  = (228, 220, 207)  → kem sáng — nền trang
    //    TAUPE_GREY   = (153, 143, 133)  → xám taupe — text phụ
    //    GLASS_WHITE  = (255, 252, 245)  → trắng ngà — card, text trên tối
    //    TEXT_PRIMARY = (51, 51, 51)     → text tối chính
    //    DANGER_RED   = (231, 76, 60)
    //    SUCCESS_GREEN= (46, 204, 113)
    //    INFO_BLUE    = (52, 152, 219)
    //
    //  ACCENT_WARM đã được đổi → PRIMARY_DARK (62,54,46), không còn dùng cam
    //  CustomButton.Variant.PRIMARY base = (62, 54, 46)  → nâu
    // ══════════════════════════════════════════════════════════

    // Nền — light cream theme
    private static final Color BG_PAGE     = ThemeColor.CREAM_LIGHT;            // (228,220,207) — nền toàn trang
    private static final Color BG_CARD     = ThemeColor.GLASS_WHITE;            // (255,252,245) — card, input
    private static final Color BG_SECTION  = new Color(238, 232, 222);          // kem trung tính — section xen kẽ
    private static final Color BG_DARK     = ThemeColor.PRIMARY_DARK;           // (62,54,46)    — navbar/hero/footer
    private static final Color BG_DARK_MID = new Color(80, 70, 60);             // PRIMARY hover

    // Accent
    private static final Color ACCENT      = ThemeColor.PRIMARY_DARK;           // (62,54,46)  — nút chính PRIMARY
    private static final Color ACCENT_HOT  = new Color(80, 70, 60);             // hover nâu
    private static final Color DUSTY_ROSE  = new Color(196, 137, 122);          // #C4897A — Dusty Rose, điểm nhấn tinh tế
    private static final Color ACCENT_WARM = ThemeColor.PRIMARY_DARK;           // đồng nhất PRIMARY (62,54,46) — không còn cam

    // Text
    private static final Color TEXT_HI     = ThemeColor.GLASS_WHITE;            // (255,252,245) — text trên nền tối
    private static final Color TEXT_DARK   = ThemeColor.TEXT_PRIMARY;           // (51,51,51)    — text trên nền sáng
    private static final Color TEXT_MID    = ThemeColor.TAUPE_GREY;             // (153,143,133) — text phụ
    private static final Color TEXT_LO     = new Color(180, 170, 158);          // nhạt hơn TAUPE

    // Border
    private static final Color BORDER_LIGHT = new Color(210, 202, 190);         // viền nhạt trên nền kem
    private static final Color BORDER_DARK  = new Color(90, 80, 68);            // viền tối

    // Footer — dùng PRIMARY_DARK làm nền footer (tương phản với body kem)
    private static final Color FOOTER_BG   = ThemeColor.PRIMARY_DARK;           // (62,54,46)
    private static final Color FOOTER_MID  = new Color(75, 65, 55);             // panel phụ trong footer

    // ── Font ────────────────────────────────────────────────────────
    private static final Font FONT_HERO_TITLE = new Font("Segoe UI", Font.BOLD, 52);
    private static final Font FONT_HERO_SUB   = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_NAV        = new Font("Segoe UI", Font.BOLD, 12);

    // Callbacks
    private final Runnable onLoginClick;
    private final Runnable onRegisterClick;

    // ── Making carousel ──────────────────────────────────────────────
    private JPanel makingSlideContainer;
    private JPanel[] makingSlides;
    private JPanel[] makingDots;
    private int makingIdx = 0;
    private Timer makingTimer;

    // ── Featured carousel ────────────────────────────────────────────
    private JPanel[] featuredCards;
    private JPanel[] featuredDots;
    private int featuredIdx = 0;
    private static final int FEAT_VISIBLE = 3;

    // ── Feedback carousel ────────────────────────────────────────────
    private static final int FEED_VISIBLE = 3;

    // ── Scroll ───────────────────────────────────────────────────────
    private JScrollPane scrollPane;

    // ─────────────────────────────────────────────────────────────────
    public HomePanel(Runnable onLoginClick, Runnable onRegisterClick) {
        this.onLoginClick    = onLoginClick;
        this.onRegisterClick = onRegisterClick;
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        buildUI();
    }

    public HomePanel() { this(() -> {}, () -> {}); }

    // ═══════════════════════════════════════════════════════════════
    //  BUILD UI
    // ═══════════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_PAGE);

        mainContent.add(buildNavbar());
        mainContent.add(buildHero());
        mainContent.add(buildMakingSection());
        mainContent.add(buildFeaturedSection());
        mainContent.add(buildFeedbackSection());
        mainContent.add(buildFooter());

        scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBackground(BG_PAGE);
        add(scrollPane, BorderLayout.CENTER);

        startMakingCarousel();
    }

    // ═══════════════════════════════════════════════════════════════
    //  1. NAVBAR — nền PRIMARY nâu, logo + nút outline/filled
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildNavbar() {
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // bottom separator
                g2.setColor(ACCENT_WARM);
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        nav.setOpaque(false);
        nav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        nav.setPreferredSize(new Dimension(0, 62));
        nav.setBorder(new EmptyBorder(0, 36, 0, 36));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoPanel.setOpaque(false);
        JLabel lKey = new JLabel("KEY");
        lKey.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lKey.setForeground(TEXT_HI);
        JLabel lSmith = new JLabel("SMITH");
        lSmith.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lSmith.setForeground(ThemeColor.GLASS_WHITE); // logo chữ trắng ngà trên nền tối
        logoPanel.add(lKey);
        logoPanel.add(lSmith);


        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnLogin = buildNavBtn("ĐĂNG NHẬP", false);
        JButton btnReg   = buildNavBtn("ĐĂNG KÝ",   true);
        btnLogin.addActionListener(e -> onLoginClick.run());
        btnReg.addActionListener(e   -> onRegisterClick.run());
        btnPanel.add(btnLogin);
        btnPanel.add(btnReg);

        nav.add(logoPanel, BorderLayout.WEST);
        nav.add(btnPanel,  BorderLayout.EAST);
        return nav;
    }

    private JButton buildNavBtn(String text, boolean filled) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (filled) {
                    g2.setColor(getModel().isRollover() ? ACCENT_HOT : ACCENT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                } else {
                    g2.setColor(getModel().isRollover()
                        ? new Color(255,252,245,60) : new Color(255,252,245,25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(new Color(255, 252, 245, 100));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_NAV);
        btn.setForeground(TEXT_HI);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(115, 36));
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. HERO — nền PRIMARY tối, text sáng
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout()) {
            private BufferedImage heroImg;
            {
                try {
                    URL url = getClass().getResource("/resources/keyforge_artisan_box.png");
                    if (url != null) heroImg = ImageIO.read(url);
                } catch (Exception ignored) {}
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                if (heroImg != null) {
                    g2.drawImage(heroImg, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Fallback: gradient nâu đậm
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(50, 43, 36),
                        getWidth(), getHeight(), new Color(30, 25, 20));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    // Grid pattern
                    g2.setColor(new Color(255, 252, 245, 8));
                    for (int i = 0; i < getWidth(); i += 60)  g2.drawLine(i, 0, i, getHeight());
                    for (int j = 0; j < getHeight(); j += 60) g2.drawLine(0, j, getWidth(), j);
                }
                // overlay tối bên trái để text dễ đọc
                GradientPaint overlay = new GradientPaint(
                    0, 0, new Color(40, 33, 27, 215),
                    getWidth()*3/5, 0, new Color(40, 33, 27, 30));
                g2.setPaint(overlay);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // bottom fade sang kem
                GradientPaint bottomFade = new GradientPaint(
                    0, getHeight()-100, new Color(40, 33, 27, 0),
                    0, getHeight(),     new Color(228, 220, 207, 255));
                g2.setPaint(bottomFade);
                g2.fillRect(0, getHeight()-100, getWidth(), 100);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(0, 520));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(120, 64, 60, 40));

        JLabel eyebrow = new JLabel("ARTISAN KEYCAP STORE");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(DUSTY_ROSE);
        eyebrow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title1 = new JLabel("CRAFTED FOR");
        title1.setFont(FONT_HERO_TITLE);
        title1.setForeground(TEXT_HI);
        title1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title2 = new JLabel("PERFECTIONISTS");
        title2.setFont(FONT_HERO_TITLE);
        title2.setForeground(DUSTY_ROSE);
        title2.setAlignmentX(LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><div style='width:360px'>Khám phá bộ sưu tập keycap artisan thủ công cao cấp.<br>Mỗi phím là một tác phẩm nghệ thuật độc nhất.</div></html>");
        desc.setFont(FONT_HERO_SUB);
        desc.setForeground(new Color(200, 192, 180));
        desc.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnExplore = buildPrimaryBtn("KHÁM PHÁ NGAY  →");
        btnExplore.setAlignmentX(LEFT_ALIGNMENT);
        btnExplore.setMaximumSize(new Dimension(210, 48));
        btnExplore.setPreferredSize(new Dimension(210, 48));

        content.add(eyebrow);
        content.add(Box.createVerticalStrut(18));
        content.add(title1);
        content.add(title2);
        content.add(Box.createVerticalStrut(22));
        content.add(desc);
        content.add(Box.createVerticalStrut(30));
        content.add(btnExplore);

        hero.add(content, BorderLayout.WEST);
        return hero;
    }

    // ═══════════════════════════════════════════════════════════════
    //  3. MAKING OF — nền PRIMARY tối
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildMakingSection() {
        String[][] slides = {
            {"THE MAKING OF", "ARTISAN SERIES",
             "Producing thousands of keycaps with the highest standard of crafting and finishing is indeed an achievement. Come and join with us this making session to explore unexpected things.", "⚙"},
            {"THE ART OF", "SCULPTING",
             "Each artisan keycap starts as a concept sketch. Our sculptors spend weeks perfecting every curve and detail before the first mold is ever cast.", "✦"},
            {"FROM RESIN", "TO LEGEND",
             "High-grade casting resin, hand-poured and meticulously cured. Every batch is inspected to ensure the color depth and clarity our collectors expect.", "◈"}
        };

        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(BG_DARK);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));
        section.setPreferredSize(new Dimension(0, 460));

        makingSlideContainer = new JPanel(new CardLayout());
        makingSlideContainer.setBackground(BG_DARK);
        makingSlides = new JPanel[slides.length];
        for (int i = 0; i < slides.length; i++) {
            makingSlides[i] = buildMakingSlide(slides[i][0], slides[i][1], slides[i][2], slides[i][3]);
            makingSlideContainer.add(makingSlides[i], "slide" + i);
        }

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(10, 60, 10, 60));

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dots.setOpaque(false);
        makingDots = new JPanel[slides.length];
        for (int i = 0; i < slides.length; i++) {
            final int idx = i;
            makingDots[i] = buildDot(i == 0, true);
            makingDots[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { goToMakingSlide(idx); }
            });
            dots.add(makingDots[i]);
        }

        JPanel arrows = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        arrows.setOpaque(false);
        arrows.add(buildArrowBtn("‹", true,
            () -> goToMakingSlide((makingIdx - 1 + makingSlides.length) % makingSlides.length)));
        arrows.add(buildArrowBtn("›", true,
            () -> goToMakingSlide((makingIdx + 1) % makingSlides.length)));

        controls.add(dots,   BorderLayout.WEST);
        controls.add(arrows, BorderLayout.EAST);
        section.add(makingSlideContainer, BorderLayout.CENTER);
        section.add(controls, BorderLayout.SOUTH);
        return section;
    }

    private JPanel buildMakingSlide(String t1, String t2, String desc, String icon) {
        JPanel slide = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // pattern bên phải
                g2.setColor(new Color(255, 252, 245, 5));
                for (int x = getWidth()/2; x < getWidth(); x += 40)
                    for (int y = 0; y < getHeight(); y += 40)
                        g2.fillRect(x, y, 20, 20);
                // fade phải
                GradientPaint gp = new GradientPaint(
                    getWidth()/2, 0, new Color(62,54,46,0),
                    getWidth(),   0, new Color(62,54,46,180));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // accent line trái
                g2.setColor(ACCENT_WARM);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(0, 0, 0, getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        slide.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(60, 80, 40, 40));

        JLabel tag = buildTag("KEYSMITH STUDIO", true);
        tag.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl1 = new JLabel(t1);
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lbl1.setForeground(TEXT_HI);
        lbl1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl2 = new JLabel(t2);
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lbl2.setForeground(DUSTY_ROSE); // Dusty Rose — dòng tiêu đề phụ
        lbl2.setAlignmentX(LEFT_ALIGNMENT);

        // Divider
        JPanel divider = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        divider.setOpaque(false);
        divider.setAlignmentX(LEFT_ALIGNMENT);
        JPanel d1 = new JPanel(); d1.setBackground(ACCENT_WARM); d1.setPreferredSize(new Dimension(40, 2));
        JPanel d2 = new JPanel(); d2.setBackground(ACCENT_WARM); d2.setPreferredSize(new Dimension(8, 2));
        JPanel d3 = new JPanel(); d3.setBackground(ACCENT_WARM); d3.setPreferredSize(new Dimension(8, 2));
        divider.add(d1); divider.add(d2); divider.add(d3);

        JLabel descLbl = new JLabel("<html><div style='width:340px'>" + desc + "</div></html>");
        descLbl.setFont(FONT_BODY);
        descLbl.setForeground(new Color(190, 182, 170));
        descLbl.setAlignmentX(LEFT_ALIGNMENT);

        JButton btn = buildRoseBtn("READ MORE");
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 42));

        content.add(tag);
        content.add(Box.createVerticalStrut(16));
        content.add(lbl1);
        content.add(lbl2);
        content.add(Box.createVerticalStrut(14));
        content.add(divider);
        content.add(Box.createVerticalStrut(18));
        content.add(descLbl);
        content.add(Box.createVerticalStrut(24));
        content.add(btn);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(360, 0));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 140));
        iconLbl.setForeground(new Color(255, 252, 245, 18));
        right.add(iconLbl);

        slide.add(content, BorderLayout.CENTER);
        slide.add(right,   BorderLayout.EAST);
        return slide;
    }

    // ═══════════════════════════════════════════════════════════════
    //  4. FEATURED — nền BG_PAGE kem sáng
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildFeaturedSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BG_PAGE);
        section.setBorder(new EmptyBorder(70, 0, 70, 0));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 40, 0));
        header.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("BEST SELLER COLLECTION");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sub.setForeground(DUSTY_ROSE);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("FEATURED");
        title.setFont(new Font("Segoe UI", Font.BOLD, 52));
        title.setForeground(BG_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JPanel sep = new JPanel();
        sep.setBackground(ACCENT_WARM);
        sep.setPreferredSize(new Dimension(60, 3));
        sep.setMaximumSize(new Dimension(60, 3));
        sep.setAlignmentX(CENTER_ALIGNMENT);

        header.add(sub);
        header.add(Box.createVerticalStrut(8));
        header.add(title);
        header.add(Box.createVerticalStrut(12));
        header.add(sep);
        section.add(header);

        String[][] products = {
            {"Lord Of The Rings", "White Tree Keycap Artisan", "85$", "◈"},
            {"Attack On Titan",   "Wings of Freedom Artisan",  "85$", "✦"},
            {"Lord Of The Rings", "Horn Of Gordor Artisan",    "90$", "⬡"},
            {"Dark Souls",        "Estus Flask Artisan",       "95$", "◉"},
            {"Cyberpunk 2077",    "Corpo Logo Artisan",        "88$", "⚡"},
        };

        featuredCards = new JPanel[products.length];
        for (int i = 0; i < products.length; i++)
            featuredCards[i] = buildProductCard(products[i][0], products[i][1], products[i][2], products[i][3]);

        JPanel viewport = new JPanel(new BorderLayout());
        viewport.setOpaque(false);
        viewport.setBorder(new EmptyBorder(0, 60, 0, 60));

        JPanel visibleRow = new JPanel(new GridLayout(1, FEAT_VISIBLE, 20, 0));
        visibleRow.setOpaque(false);
        updateFeaturedView(visibleRow, 0);
        viewport.add(visibleRow, BorderLayout.CENTER);
        section.add(viewport);
        section.add(Box.createVerticalStrut(28));

        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        dotsPanel.setOpaque(false);
        featuredDots = new JPanel[products.length - FEAT_VISIBLE + 1];
        for (int i = 0; i < featuredDots.length; i++) {
            featuredDots[i] = buildDot(i == 0, false);
            dotsPanel.add(featuredDots[i]);
        }

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        navRow.setOpaque(false);
        navRow.add(buildArrowBtn("‹", false, () -> {
            featuredIdx = Math.max(0, featuredIdx - 1);
            updateFeaturedView(visibleRow, featuredIdx);
            updateDots(featuredDots, featuredIdx, false);
        }));
        navRow.add(dotsPanel);
        navRow.add(buildArrowBtn("›", false, () -> {
            featuredIdx = Math.min(products.length - FEAT_VISIBLE, featuredIdx + 1);
            updateFeaturedView(visibleRow, featuredIdx);
            updateDots(featuredDots, featuredIdx, false);
        }));
        section.add(navRow);
        return section;
    }

    private void updateFeaturedView(JPanel visibleRow, int startIdx) {
        visibleRow.removeAll();
        for (int i = startIdx; i < startIdx + FEAT_VISIBLE && i < featuredCards.length; i++)
            visibleRow.add(featuredCards[i]);
        visibleRow.revalidate();
        visibleRow.repaint();
    }

    private JPanel buildProductCard(String collection, String name, String price, String icon) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);  // GLASS_WHITE
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Image area — kem trung
        JPanel imgArea = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(238, 232, 222),
                    getWidth(), getHeight(), new Color(228, 220, 207));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        imgArea.setOpaque(false);
        imgArea.setPreferredSize(new Dimension(0, 200));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        iconLbl.setForeground(new Color(62, 54, 46, 45));
        imgArea.add(iconLbl);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 16, 14, 16));

        JLabel collLbl = new JLabel(collection.toUpperCase());
        collLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        collLbl.setForeground(TEXT_MID);
        collLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel("<html>" + name + "</html>");
        nameLbl.setFont(FONT_CARD_TITLE);
        nameLbl.setForeground(TEXT_DARK);
        nameLbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel priceRow = new JPanel(new BorderLayout(8, 0));
        priceRow.setOpaque(false);
        priceRow.setAlignmentX(LEFT_ALIGNMENT);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel priceLbl = new JLabel(price);
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLbl.setForeground(ACCENT_WARM);  // PRIMARY_DARK nâu

        JButton addBtn = new JButton("+") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_DARK : BORDER_LIGHT);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        addBtn.setForeground(TEXT_DARK);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(34, 34));
        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setForeground(TEXT_HI); addBtn.repaint(); }
            public void mouseExited(MouseEvent e)  { addBtn.setForeground(TEXT_DARK); addBtn.repaint(); }
        });

        priceRow.add(priceLbl, BorderLayout.WEST);
        priceRow.add(addBtn,   BorderLayout.EAST);

        body.add(collLbl);
        body.add(Box.createVerticalStrut(4));
        body.add(nameLbl);
        body.add(Box.createVerticalStrut(10));
        body.add(priceRow);

        JPanel bodyWrap = new JPanel(new BorderLayout());
        bodyWrap.setOpaque(false);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_LIGHT);
        bodyWrap.add(sep, BorderLayout.NORTH);
        bodyWrap.add(body, BorderLayout.CENTER);

        card.add(imgArea, BorderLayout.CENTER);
        card.add(bodyWrap, BorderLayout.SOUTH);

        // Hover — viền nâu
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(BG_DARK, 2));
                card.repaint();
            }
            public void mouseExited(MouseEvent e)  {
                card.setBorder(null);
                card.repaint();
            }
        });
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  5. FEEDBACK — nền BG_SECTION kem trung tính
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildFeedbackSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BG_SECTION);
        section.setBorder(new EmptyBorder(70, 60, 70, 60));

        JLabel sub = new JLabel("WHAT OUR CUSTOMERS SAY?");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sub.setForeground(DUSTY_ROSE);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("REVIEWS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(BG_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JPanel sep = new JPanel();
        sep.setBackground(ACCENT_WARM);
        sep.setPreferredSize(new Dimension(50, 3));
        sep.setMaximumSize(new Dimension(50, 3));
        sep.setAlignmentX(CENTER_ALIGNMENT);

        section.add(sub);
        section.add(Box.createVerticalStrut(8));
        section.add(title);
        section.add(Box.createVerticalStrut(10));
        section.add(sep);
        section.add(Box.createVerticalStrut(36));

        String[][] feedbacks = {
            {"Nguyễn Minh Anh", "★★★★★", "Keycap chất lượng tuyệt vời! Màu sắc đẹp và sắc nét hơn mong đợi. Đóng gói cẩn thận. Sẽ mua lại lần sau.", "Hà Nội"},
            {"Trần Văn Khoa",   "★★★★★", "Sản phẩm đúng như mô tả, resin trong và không có bọt khí. Cảm giác gõ rất thỏa mãn. Highly recommended!", "TP.HCM"},
            {"Lê Thị Thu Hà",   "★★★★☆", "Thiết kế độc đáo, không đụng hàng. Giao hàng nhanh, shop support nhiệt tình. Chỉ tiếc giá hơi cao.", "Đà Nẵng"},
            {"Phạm Hoàng Long",  "★★★★★", "Mua cho bàn phím custom của mình, fit hoàn hảo. Artisan detail rất sắc, không bị nhòe màu. 10/10!", "Cần Thơ"},
            {"Đinh Quốc Bảo",   "★★★★★", "Đây là lần thứ 3 tôi mua ở đây. Luôn hài lòng với chất lượng. Bộ sưu tập ngày càng đa dạng hơn.", "Bình Dương"},
        };

        JPanel[] feedCards = new JPanel[feedbacks.length];
        for (int i = 0; i < feedbacks.length; i++)
            feedCards[i] = buildFeedbackCard(feedbacks[i][0], feedbacks[i][1], feedbacks[i][2], feedbacks[i][3]);

        JPanel visibleRow = new JPanel(new GridLayout(1, FEED_VISIBLE, 16, 0));
        visibleRow.setOpaque(false);
        visibleRow.setAlignmentX(CENTER_ALIGNMENT);
        updateFeedbackView(visibleRow, feedCards, 0);
        section.add(visibleRow);
        section.add(Box.createVerticalStrut(24));

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        navRow.setOpaque(false);
        navRow.setAlignmentX(CENTER_ALIGNMENT);

        JPanel[] fDots = new JPanel[feedbacks.length - FEED_VISIBLE + 1];
        for (int i = 0; i < fDots.length; i++) {
            fDots[i] = buildDot(i == 0, false);
            navRow.add(fDots[i]);
        }
        section.add(navRow);
        section.add(Box.createVerticalStrut(12));

        int[] fIdx = {0};
        JPanel arrowRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        arrowRow.setOpaque(false);
        arrowRow.setAlignmentX(CENTER_ALIGNMENT);
        arrowRow.add(buildArrowBtn("‹", false, () -> {
            fIdx[0] = Math.max(0, fIdx[0] - 1);
            updateFeedbackView(visibleRow, feedCards, fIdx[0]);
            updateDots(fDots, fIdx[0], false);
        }));
        arrowRow.add(buildArrowBtn("›", false, () -> {
            fIdx[0] = Math.min(feedbacks.length - FEED_VISIBLE, fIdx[0] + 1);
            updateFeedbackView(visibleRow, feedCards, fIdx[0]);
            updateDots(fDots, fIdx[0], false);
        }));
        section.add(arrowRow);
        return section;
    }

    private void updateFeedbackView(JPanel row, JPanel[] cards, int startIdx) {
        row.removeAll();
        for (int i = startIdx; i < startIdx + FEED_VISIBLE && i < cards.length; i++)
            row.add(cards[i]);
        row.revalidate();
        row.repaint();
    }

    private JPanel buildFeedbackCard(String name, String stars, String comment, String city) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);  // GLASS_WHITE trắng ngà
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                // Left accent bar — nâu PRIMARY
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0, 0, 3, getHeight(), 2, 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel starLbl = new JLabel(stars);
        starLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        starLbl.setForeground(ACCENT_WARM);  // PRIMARY nâu

        JLabel commentLbl = new JLabel("<html><div style='width:200px'>" + comment + "</div></html>");
        commentLbl.setFont(FONT_BODY);
        commentLbl.setForeground(TEXT_MID);

        JLabel nameLbl = new JLabel("— " + name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(TEXT_DARK);

        JLabel cityLbl = new JLabel(city);
        cityLbl.setFont(FONT_SMALL);
        cityLbl.setForeground(TEXT_LO);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        namePanel.add(nameLbl);
        namePanel.add(cityLbl);

        card.add(starLbl,    BorderLayout.NORTH);
        card.add(commentLbl, BorderLayout.CENTER);
        card.add(namePanel,  BorderLayout.SOUTH);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  6. FOOTER — nền PRIMARY tối
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(FOOTER_BG);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // ── Banner ────────────────────────────────────────────────
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBackground(BG_PAGE);  // kem — transition từ section trên
        banner.setBorder(new EmptyBorder(44, 0, 38, 0));

        JLabel bannerTitle = new JLabel("ARE YOU ENJOYING?");
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        bannerTitle.setForeground(BG_DARK);
        bannerTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel bannerSub = new JLabel("CONTINUE EXPLORE");
        bannerSub.setFont(new Font("Segoe UI", Font.BOLD, 28));
        bannerSub.setForeground(DUSTY_ROSE);
        bannerSub.setAlignmentX(CENTER_ALIGNMENT);

        // Separator
        JPanel sepLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ACCENT_WARM);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sepLine.setOpaque(false);
        sepLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sepLine.setPreferredSize(new Dimension(0, 2));

        banner.add(bannerTitle);
        banner.add(Box.createVerticalStrut(6));
        banner.add(bannerSub);
        banner.add(Box.createVerticalStrut(32));
        banner.add(sepLine);

        // ── Main row ──────────────────────────────────────────────
        JPanel mainRow = new JPanel(new BorderLayout(0, 0));
        mainRow.setBackground(FOOTER_BG);

        // Table trái
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(new EmptyBorder(0, 0, 0, 0));

        Color T_BG  = new Color(153, 143, 133);  // TAUPE_GREY
        Color T_BDR = new Color(120, 110, 100);
        Color T_TXT = new Color(30, 25, 20);

        JPanel tablePanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(T_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                int w = getWidth();
                int rowH = 40, suppH = 36;
                int c1 = 160, c2 = 200, c3 = w - c1 - c2;

                drawCell(g2, 0, 0, c1, rowH*4, "ABOUT US", new Font("Segoe UI", Font.BOLD,16),
                    SwingConstants.CENTER, T_TXT, T_BDR);
                drawCell(g2, c1, 0,    c2, rowH, "HELP CENTER",         new Font("Segoe UI", Font.BOLD,13), SwingConstants.CENTER, T_TXT, T_BDR);
                drawCell(g2, c1, rowH, c2, rowH, "BLOG & NEWS",         new Font("Segoe UI", Font.BOLD,13), SwingConstants.CENTER, T_TXT, T_BDR);
                drawCell(g2, c1, rowH*2, c2, rowH, "DISCORD",           new Font("Segoe UI", Font.BOLD,13), SwingConstants.LEFT,   T_TXT, T_BDR);
                drawCell(g2, c1, rowH*3, c2, rowH, "SOCIAL MEDIA",      new Font("Segoe UI", Font.BOLD,13), SwingConstants.LEFT,   T_TXT, T_BDR);
                drawCellMultiline(g2, c1+c2, 0, c3, rowH*2, "SHIPPING &\nRETURN POLICY",
                    new Font("Segoe UI", Font.BOLD,13), T_TXT, T_BDR);
                drawCellRight(g2, c1+c2, rowH*2, c3, rowH, "ⓓ", new Font("Segoe UI", Font.BOLD,18), T_TXT, T_BDR);
                drawCellRight(g2, c1+c2, rowH*3, c3, rowH, "⊕  ⊞  ♪  ▶  ✕", new Font("Segoe UI", Font.BOLD,14), T_TXT, T_BDR);

                // Support row
                int suppY = rowH*4;
                g2.setColor(T_BDR);
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(0, suppY, w, suppH);
                Font sf = new Font("Segoe UI", Font.BOLD, 12);
                g2.setFont(sf); g2.setColor(T_TXT);
                FontMetrics fm = g2.getFontMetrics();
                int ty = suppY + (suppH + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("SUPPORT:", 12, ty);
                String email = "SUPPORT@KEYSMITH.COM";
                g2.drawString(email, w - fm.stringWidth(email) - 12, ty);
                g2.dispose();
            }
            private void drawCell(Graphics2D g2, int x, int y, int w, int h,
                    String text, Font font, int align, Color tc, Color bc) {
                g2.setColor(bc); g2.setStroke(new BasicStroke(1)); g2.drawRect(x, y, w, h);
                g2.setFont(font); g2.setColor(tc);
                FontMetrics fm = g2.getFontMetrics();
                int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
                int tx = (align == SwingConstants.CENTER)
                    ? x + (w - fm.stringWidth(text)) / 2 : x + 12;
                g2.drawString(text, tx, ty);
            }
            private void drawCellMultiline(Graphics2D g2, int x, int y, int w, int h,
                    String text, Font font, Color tc, Color bc) {
                g2.setColor(bc); g2.setStroke(new BasicStroke(1)); g2.drawRect(x, y, w, h);
                g2.setFont(font); g2.setColor(tc);
                FontMetrics fm = g2.getFontMetrics();
                String[] lines = text.split("\n");
                int totalH = lines.length * fm.getHeight();
                int startY = y + (h - totalH) / 2 + fm.getAscent();
                for (String line : lines) {
                    int lx = x + (w - fm.stringWidth(line.trim())) / 2;
                    g2.drawString(line.trim(), lx, startY);
                    startY += fm.getHeight();
                }
            }
            private void drawCellRight(Graphics2D g2, int x, int y, int w, int h,
                    String text, Font font, Color tc, Color bc) {
                g2.setColor(bc); g2.setStroke(new BasicStroke(1)); g2.drawRect(x, y, w, h);
                g2.setFont(font); g2.setColor(tc);
                FontMetrics fm = g2.getFontMetrics();
                int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x + w - fm.stringWidth(text) - 12, ty);
            }
        };
        tablePanel.setOpaque(true);
        tablePanel.setBackground(T_BG);
        tablePanel.setPreferredSize(new Dimension(760, 216));

        tableWrap.add(tablePanel, BorderLayout.CENTER);

        // Subscribe panel — nền PRIMARY nâu
        JPanel subscribePanel = new JPanel();
        subscribePanel.setLayout(new BoxLayout(subscribePanel, BoxLayout.Y_AXIS));
        subscribePanel.setBackground(BG_DARK);
        subscribePanel.setBorder(new EmptyBorder(22, 22, 22, 22));
        subscribePanel.setPreferredSize(new Dimension(320, 0));

        JLabel subTitle = new JLabel("SUBSCRIBE & GET INCENTIVES");
        subTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subTitle.setForeground(Color.WHITE);
        subTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subDesc = new JLabel("<html><div style='width:260px'>Sign up and get the latest news about new design, group buy, straight to your inbox.</div></html>");
        subDesc.setFont(FONT_SMALL);
        subDesc.setForeground(new Color(255, 255, 255, 210));
        subDesc.setAlignmentX(LEFT_ALIGNMENT);

        JLabel bonus = new JLabel("Bonus: Many hot offers and giveaway await!");
        bonus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bonus.setForeground(Color.WHITE);
        bonus.setAlignmentX(LEFT_ALIGNMENT);

        JTextField emailField = new JTextField("YOUR E-MAIL:") {
            {
                setForeground(TEXT_MID);
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        if (getText().equals("YOUR E-MAIL:")) {
                            setText(""); setForeground(TEXT_DARK);
                        }
                    }
                    public void focusLost(FocusEvent e) {
                        if (getText().trim().isEmpty()) {
                            setText("YOUR E-MAIL:"); setForeground(TEXT_MID);
                        }
                    }
                });
            }
        };
        emailField.setFont(FONT_SMALL);
        emailField.setBackground(Color.WHITE);
        emailField.setBorder(new EmptyBorder(8, 10, 8, 10));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        emailField.setAlignmentX(LEFT_ALIGNMENT);

        subscribePanel.add(subTitle);
        subscribePanel.add(Box.createVerticalStrut(10));
        subscribePanel.add(subDesc);
        subscribePanel.add(Box.createVerticalStrut(10));
        subscribePanel.add(bonus);
        subscribePanel.add(Box.createVerticalStrut(12));
        subscribePanel.add(emailField);

        JPanel leftSide = new JPanel(new BorderLayout(0, 0));
        leftSide.setOpaque(false);
        leftSide.setBorder(new EmptyBorder(16, 20, 16, 0));
        leftSide.add(tableWrap, BorderLayout.CENTER);

        mainRow.add(leftSide,       BorderLayout.CENTER);
        mainRow.add(subscribePanel, BorderLayout.EAST);

        // Copyright
        JPanel copyright = new JPanel(new FlowLayout(FlowLayout.CENTER));
        copyright.setBackground(new Color(40, 35, 30));
        JLabel copy = new JLabel("© 2025 KeySmith Artisan Store. All rights reserved.");
        copy.setFont(FONT_SMALL);
        copy.setForeground(new Color(153, 143, 133));
        copyright.add(copy);

        footer.add(banner,    BorderLayout.NORTH);
        footer.add(mainRow,   BorderLayout.CENTER);
        footer.add(copyright, BorderLayout.SOUTH);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSEL LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void startMakingCarousel() {
        makingTimer = new Timer(4500, e -> goToMakingSlide((makingIdx + 1) % makingSlides.length));
        makingTimer.start();
    }

    private void goToMakingSlide(int idx) {
        makingIdx = idx;
        ((CardLayout) makingSlideContainer.getLayout()).show(makingSlideContainer, "slide" + idx);
        updateDots(makingDots, idx, true);
        if (makingTimer != null) makingTimer.restart();
    }

    private void updateDots(JPanel[] dots, int activeIdx, boolean onDark) {
        for (int i = 0; i < dots.length; i++) {
            boolean[] state = (boolean[]) dots[i].getClientProperty("isActive");
            if (state != null) {
                state[0] = (i == activeIdx);
            }
            dots[i].revalidate();
            dots[i].repaint();
        }
        // Revalidate parent để FlowLayout tính lại kích thước
        if (dots.length > 0 && dots[0].getParent() != null) {
            dots[0].getParent().revalidate();
            dots[0].getParent().repaint();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILITY BUILDERS
    // ═══════════════════════════════════════════════════════════════

    /** onDark=true → dot trên nền tối, false → dot trên nền kem
     *  Dùng boolean[] state để dot tự vẽ đúng kích thước khi active thay đổi */
    private JPanel buildDot(boolean active, boolean onDark) {
        boolean[] isActive = {active};
        Color activeColor  = DUSTY_ROSE;
        Color inactiveColor = onDark ? new Color(100, 92, 82) : BORDER_LIGHT;

        JPanel dot = new JPanel() {
            @Override public Dimension getPreferredSize() {
                return isActive[0] ? new Dimension(28, 8) : new Dimension(8, 8);
            }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isActive[0] ? activeColor : inactiveColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.putClientProperty("isActive", isActive);
        dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return dot;
    }

    /** onDark=true → arrow trên nền tối, false → trên nền kem */
    private JButton buildArrowBtn(String arrow, boolean onDark, Runnable action) {
        JButton btn = new JButton(arrow) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(onDark ? ACCENT_WARM : BG_DARK);
                    g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                } else {
                    g2.setColor(onDark ? new Color(255,255,255,30) : BORDER_LIGHT);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        btn.setForeground(onDark ? new Color(228,220,207) : TEXT_DARK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(TEXT_HI); btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(onDark ? new Color(228,220,207) : TEXT_DARK); btn.repaint(); }
        });
        return btn;
    }

    /** Nút chính — nền PRIMARY nâu, text trắng ngà */
    private JButton buildPrimaryBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 3, getWidth()-3, getHeight()-2, 6, 6);
                g2.setColor(getModel().isRollover() ? ACCENT_HOT : ACCENT);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-2, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(255, 252, 245));  // GLASS_WHITE
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    /** Nút Rose — nền DUSTY_ROSE hồng đất, text trắng ngà */
    private JButton buildRoseBtn(String text) {
        Color roseHover = new Color(180, 120, 106);
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 3, getWidth()-3, getHeight()-2, 6, 6);
                g2.setColor(getModel().isRollover() ? roseHover : DUSTY_ROSE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-2, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(255, 252, 245));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel buildTag(String text, boolean onDark) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(62, 54, 46, onDark ? 35 : 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.setColor(new Color(62, 54, 46, onDark ? 160 : 110));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 3, 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(ACCENT_WARM);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        lbl.setOpaque(false);
        return lbl;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLEANUP
    // ═══════════════════════════════════════════════════════════════
    public void cleanup() {
        if (makingTimer != null && makingTimer.isRunning()) makingTimer.stop();
    }

    // ═══════════════════════════════════════════════════════════════
    //  STANDALONE TEST
    // ═══════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("HomePanel Test");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1280, 800);
            f.setLocationRelativeTo(null);
            f.add(new HomePanel(
                () -> JOptionPane.showMessageDialog(null, "→ Login"),
                () -> JOptionPane.showMessageDialog(null, "→ Register")
            ));
            f.setVisible(true);
        });
    }
}