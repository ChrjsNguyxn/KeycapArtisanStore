package com.keycapstore.gui;

import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MyOrdersPanel extends JPanel implements Refreshable {

    public MyOrdersPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("LỊCH SỬ MUA HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Placeholder content
        JLabel lblContent = new JLabel("Danh sách đơn hàng đã mua sẽ hiển thị tại đây...");
        lblContent.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblContent.setForeground(Color.GRAY);
        lblContent.setHorizontalAlignment(SwingConstants.CENTER);

        add(lblTitle, BorderLayout.NORTH);
        add(lblContent, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        // Logic tải lịch sử đơn hàng của khách sẽ nằm ở đây
    }
}
