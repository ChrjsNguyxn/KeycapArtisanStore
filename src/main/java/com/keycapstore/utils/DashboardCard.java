package com.keycapstore.utils;

import java.awt.*;
import javax.swing.*;

public class DashboardCard extends JPanel {
    private Color color1;
    private Color color2;

    public DashboardCard(String title, String value, String iconName, Color c1, Color c2) {
        this.color1 = c1;
        this.color2 = c2;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(240, 240, 240));
        add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(Color.WHITE);
        add(lblValue, BorderLayout.CENTER);

        if (iconName != null && !iconName.isEmpty()) {
            try {

                ImageIcon icon = new ImageIcon(getClass().getResource("/icons/" + iconName));
                Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                JLabel lblIcon = new JLabel(new ImageIcon(img));
                add(lblIcon, BorderLayout.EAST);
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2.setPaint(gp);

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        super.paintComponent(g);
    }
}