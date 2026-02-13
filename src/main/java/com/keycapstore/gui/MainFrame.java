package com.keycapstore.gui;

import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    private Object currentUser;

    public MainFrame(Object user) {
        this.currentUser = user;

        setTitle("Keyforge Artisan Store Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    public MainFrame() {
        this(null);
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setBackground(new Color(62, 54, 46));

        JLabel lblWelcome = new JLabel();
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));

        if (currentUser == null) {

            lblWelcome.setText("Xin chào: Khách (Chế độ xem)");

        } else if (currentUser instanceof Employee) {

            Employee emp = (Employee) currentUser;
            lblWelcome.setText("Nhân viên: " + emp.getUsername() + " | Chức vụ: " + emp.getRole());

        } else if (currentUser instanceof Customer) {

            Customer cus = (Customer) currentUser;
            lblWelcome.setText("Khách hàng: " + cus.getFullname());
        }

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.addActionListener(e -> logout());

        headerPanel.add(lblWelcome);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(btnLogout);

        JPanel contentPanel = new JPanel();
        contentPanel.add(new JLabel("Đây là màn hình chính của ứng dụng!"));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?", "Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            new ModernLoginDialog().setVisible(true);
        }
    }
}