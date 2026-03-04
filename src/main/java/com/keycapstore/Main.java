package com.keycapstore;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.keycapstore.gui.OrderManagementPanel;

public class Main {
    public static void main(String[] args) {

        // Chạy UI trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Keyforge Artisan Store - POS");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);

            // Gắn panel vào frame
            frame.setContentPane(new OrderManagementPanel());

            frame.setVisible(true);
        });
    }
}