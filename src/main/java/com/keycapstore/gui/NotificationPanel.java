package com.keycapstore.gui;

import com.keycapstore.model.Customer;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class NotificationPanel extends JPanel implements Refreshable {

    private Object currentUser;
    private JPanel listPanel;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public NotificationPanel(Object user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("THÔNG BÁO CỦA TÔI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(ThemeColor.BG_LIGHT);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnMarkAllRead = new JButton("Đánh dấu đã đọc tất cả");
        btnMarkAllRead.setBackground(ThemeColor.INFO);
        btnMarkAllRead.setForeground(Color.WHITE);
        btnMarkAllRead.setFocusPainted(false);
        btnMarkAllRead.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMarkAllRead.addActionListener(e -> markAllAsRead());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(ThemeColor.BG_LIGHT);
        bottomPanel.add(btnMarkAllRead);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        listPanel.removeAll();
        if (!(currentUser instanceof Customer))
            return;
        int cusId = ((Customer) currentUser).getCustomerId();

        String sql = "SELECT notification_id, title, message, is_read, created_at FROM notifications WHERE customer_id = ? ORDER BY created_at DESC";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, cusId);
            ResultSet rs = pst.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("notification_id");
                String title = rs.getString("title");
                String message = rs.getString("message");
                boolean isRead = rs.getBoolean("is_read");
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");

                JPanel item = createNotificationItem(id, title, message, isRead, createdAt);
                listPanel.add(item);
                listPanel.add(Box.createVerticalStrut(10));
            }

            if (!hasData) {
                JLabel lblEmpty = new JLabel("Bạn chưa có thông báo nào.");
                lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                lblEmpty.setForeground(Color.GRAY);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
                listPanel.add(lblEmpty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createNotificationItem(int id, String title, String message, boolean isRead,
            java.sql.Timestamp createdAt) {
        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setBackground(isRead ? Color.WHITE : new Color(230, 240, 255)); // Đổi màu xanh nếu chưa đọc
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isRead ? Color.LIGHT_GRAY : ThemeColor.INFO, 1),
                new EmptyBorder(15, 15, 15, 15)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", isRead ? Font.PLAIN : Font.BOLD, 16));
        lblTitle.setForeground(ThemeColor.PRIMARY);

        JLabel lblTime = new JLabel(sdf.format(createdAt));
        lblTime.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTime.setForeground(Color.GRAY);

        JTextArea txtMessage = new JTextArea(message);
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        txtMessage.setEditable(false);
        txtMessage.setOpaque(false);
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel topP = new JPanel(new BorderLayout());
        topP.setOpaque(false);
        topP.add(lblTitle, BorderLayout.CENTER);
        topP.add(lblTime, BorderLayout.EAST);

        p.add(topP, BorderLayout.NORTH);
        p.add(txtMessage, BorderLayout.CENTER);

        if (!isRead) {
            p.setCursor(new Cursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    markAsRead(id);
                    loadData(); // Load lại để mất màu highlight
                }
            });
        }

        return p;
    }

    private void markAsRead(int id) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (Exception e) {
        }
    }

    private void markAllAsRead() {
        if (!(currentUser instanceof Customer))
            return;
        int cusId = ((Customer) currentUser).getCustomerId();
        String sql = "UPDATE notifications SET is_read = 1 WHERE customer_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, cusId);
            pst.executeUpdate();
            loadData();
        } catch (Exception e) {
        }
    }

    @Override
    public void refresh() {
        loadData();
    }
}