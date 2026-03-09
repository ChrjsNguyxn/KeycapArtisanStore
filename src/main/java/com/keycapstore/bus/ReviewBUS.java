package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.dao.ReviewDAO;
import com.keycapstore.model.Review;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ReviewBUS {

    private ReviewDAO reviewDAO;

    public ReviewBUS() {
        this.reviewDAO = new ReviewDAO();
    }

    public List<Review> getReviewsByProduct(int productId) {
        return reviewDAO.findByProductId(productId);
    }

    public boolean addReview(Review review) {
        return reviewDAO.insert(review);
    }

    // Tính điểm trung bình (VD: 4.5/5)
    public double getAverageRating(int productId) {
        List<Review> reviews = getReviewsByProduct(productId);
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Review r : reviews) {
            sum += r.getRating();
        }
        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }

    public boolean hasPurchasedProduct(int customerId, int productId) {
        String sql = "SELECT COUNT(*) FROM Invoice i " +
                "JOIN InvoiceDetail d ON i.invoice_id = d.invoice_id " +
                "WHERE i.customer_id = ? AND d.product_id = ? " +
                "AND (i.shipping_status = 'Completed' OR i.shipping_status = 'Delivered' OR i.shipping_status IS NULL)";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, customerId);
            pst.setInt(2, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasReviewed(int customerId, int productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE customer_id = ? AND product_id = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, customerId);
            pst.setInt(2, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}