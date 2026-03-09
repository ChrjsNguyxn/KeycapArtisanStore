package com.keycapstore.dao;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Review;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY created_at DESC";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setProductId(rs.getInt("product_id"));
                r.setCustomerId(rs.getInt("customer_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));

                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    r.setCreatedAt(timestamp.toLocalDateTime());
                }
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Review> findByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE product_id = ? ORDER BY created_at DESC";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setProductId(rs.getInt("product_id"));
                r.setCustomerId(rs.getInt("customer_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));

                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    r.setCreatedAt(timestamp.toLocalDateTime());
                }
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Review r) {
        String sql = "INSERT INTO reviews (product_id, customer_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getProductId());
            ps.setInt(2, r.getCustomerId());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getComment());
            ps.setTimestamp(5,
                    Timestamp.valueOf(r.getCreatedAt() != null ? r.getCreatedAt() : java.time.LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}