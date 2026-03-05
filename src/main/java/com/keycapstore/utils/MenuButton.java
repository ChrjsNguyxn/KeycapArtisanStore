package com.keycapstore.utils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.*;

public class MenuButton extends JButton {
    private Color normalColor = ThemeColor.PRIMARY;
    private Color hoverColor = new Color(80, 70, 60);
    private boolean isSelected = false;

    public MenuButton(String text, String iconName) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBackground(normalColor);

        if (iconName != null && !iconName.isEmpty()) {
            try {
                URL iconURL = getClass().getResource("/icons/" + iconName);
                if (iconURL != null) {
                    ImageIcon icon = new ImageIcon(iconURL);

                    // Xử lý ảnh chất lượng cao (High Quality Rendering) để khử răng cưa
                    int w = 24;
                    int h = 24;
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = img.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.drawImage(icon.getImage(), 0, 0, w, h, null);
                    g2.dispose();

                    setIcon(new ImageIcon(img));
                }
            } catch (Exception e) {
                System.out.println("Không tìm thấy icon: " + iconName);
            }
        }

        setFocusPainted(false);
        setBorderPainted(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(15);
        setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected)
                    setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected)
                    setBackground(normalColor);
            }
        });
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (selected) {
            setBackground(ThemeColor.BG_LIGHT);
            setForeground(ThemeColor.PRIMARY);
        } else {
            setBackground(normalColor);
            setForeground(Color.WHITE);
        }
    }
}