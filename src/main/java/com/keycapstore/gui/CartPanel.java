package com.keycapstore.gui;

import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CartPanel extends JPanel implements Refreshable {

    public CartPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("GIỎ HÀNG CỦA BẠN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            java.net.URL iconURL = getClass().getResource("/icons/sales.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                lblTitle.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Placeholder content
        JLabel lblContent = new JLabel("Chức năng giỏ hàng đang được phát triển...");
        lblContent.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblContent.setForeground(Color.GRAY);
        lblContent.setHorizontalAlignment(SwingConstants.CENTER);

        add(lblTitle, BorderLayout.NORTH);
        add(lblContent, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        // Logic làm mới giỏ hàng sẽ nằm ở đây
    }
}
