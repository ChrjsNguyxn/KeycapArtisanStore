package com.keycapstore.gui;

import com.keycapstore.bus.ReviewBUS;
import com.keycapstore.model.Product;
import com.keycapstore.model.Review;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ReviewDialog extends JDialog {

    private int rating = 5;
    private JTextArea txtComment;
    private JLabel[] stars;
    private ReviewBUS reviewBUS;
    private boolean success = false;
    private int customerId;
    private Product product;

    public ReviewDialog(Frame parent, int customerId, Product product) {
        super(parent, "Đánh giá sản phẩm", true);
        this.customerId = customerId;
        this.product = product;
        this.reviewBUS = new ReviewBUS();

        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel lblTitle = new JLabel("Đánh giá sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblProduct = new JLabel(product.getName());
        lblProduct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblProduct.setForeground(Color.GRAY);
        lblProduct.setHorizontalAlignment(SwingConstants.CENTER);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblProduct, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // Content
        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(10, 40, 20, 40));

        // Star Rating
        JPanel pnlStars = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlStars.setBackground(Color.WHITE);
        stars = new JLabel[5];
        Font starFont = new Font("Segoe UI Symbol", Font.BOLD, 32);

        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            stars[i] = new JLabel("★");
            stars[i].setFont(starFont);
            stars[i].setForeground(new Color(255, 200, 0)); // Màu vàng
            stars[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            stars[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    setRating(starIndex);
                }
            });
            pnlStars.add(stars[i]);
        }

        JLabel lblRatingText = new JLabel("Tuyệt vời");
        lblRatingText.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRatingText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRatingText.setForeground(new Color(255, 200, 0));

        pnlContent.add(pnlStars);
        pnlContent.add(Box.createVerticalStrut(5));
        pnlContent.add(lblRatingText);
        pnlContent.add(Box.createVerticalStrut(20));

        // Comment Area
        JLabel lblComment = new JLabel("Chia sẻ cảm nhận của bạn:");
        lblComment.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtComment = new JTextArea(5, 20);
        txtComment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtComment);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        pnlContent.add(lblComment);
        pnlContent.add(Box.createVerticalStrut(5));
        pnlContent.add(scroll);

        add(pnlContent, BorderLayout.CENTER);

        // Footer Buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        pnlFooter.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setBackground(new Color(240, 240, 240));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = new JButton("Gửi Đánh Giá");
        btnSubmit.setBackground(ThemeColor.PRIMARY);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.addActionListener(e -> submitReview());

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnSubmit);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void setRating(int r) {
        this.rating = r;
        for (int i = 0; i < 5; i++) {
            if (i < r) {
                stars[i].setForeground(new Color(255, 200, 0)); // Vàng
                stars[i].setText("★");
            } else {
                stars[i].setForeground(Color.LIGHT_GRAY); // Xám
                stars[i].setText("☆");
            }
        }
    }

    private void submitReview() {
        String comment = txtComment.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung đánh giá!");
            return;
        }

        Review review = new Review(customerId, product.getId(), rating, comment);
        if (reviewBUS.addReview(review)) {
            JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã đánh giá!");
            success = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi gửi đánh giá.");
        }
    }

    public boolean isSuccess() {
        return success;
    }
}