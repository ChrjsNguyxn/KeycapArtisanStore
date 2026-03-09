package com.keycapstore.dao;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public List<Review> findByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE product_id = ? ORDER BY created_at DESC";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setCustomerId(rs.getInt("customer_id"));
                r.setProductId(rs.getInt("product_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    r.setCreatedAt(ts.toLocalDateTime());
                }
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Review t) {

        String sql = "INSERT INTO reviews (product_id, customer_id, rating, comment, created_at) VALUES (?, ?, ?, ?, GETDATE())";

        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, t.getProductId());
            pst.setInt(2, t.getCustomerId());
            pst.setInt(3, t.getRating());
            pst.setString(4, t.getComment());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }
}
