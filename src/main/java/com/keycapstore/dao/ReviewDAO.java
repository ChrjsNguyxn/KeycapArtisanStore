package com.keycapstore.dao;

import com.keycapstore.model.Review;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *   insert(Review)           → Thêm đánh giá mới
 *   findById(int)            → Tìm theo review_id
 *   findAll()                → Lấy tất cả
 *   findByProductId(int)     → Lấy đánh giá theo sản phẩm
 *   findByCustomerId(int)    → Lấy đánh giá theo khách hàng
 *   update(Review)           → Sửa đánh giá
 *   delete(int)              → Xóa đánh giá
 */
public class ReviewDAO extends BaseDAO {

    public boolean insert(Review r) {
        String sql = "INSERT INTO reviews (customer_id, product_id, rating, comment, created_at) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, r.getCustomerId());
            pst.setInt(2, r.getProductId());

            if (r.getRating() > 0)
                pst.setInt(3, r.getRating());
            else
                pst.setNull(3, Types.INTEGER);

            if (r.getComment() != null)
                pst.setString(4, r.getComment());
            else
                pst.setNull(4, Types.NVARCHAR);

            pst.setTimestamp(5, r.getCreatedAt() != null
                ? Timestamp.valueOf(r.getCreatedAt())
                : Timestamp.valueOf(LocalDateTime.now()));

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Review findById(int reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, reviewId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY created_at DESC";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy tất cả đánh giá của một sản phẩm */
    public List<Review> findByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE product_id = ? ORDER BY created_at DESC";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy tất cả đánh giá của một khách hàng */
    public List<Review> findByCustomerId(int customerId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE customer_id = ? ORDER BY created_at DESC";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean update(Review r) {
        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE review_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            if (r.getRating() > 0)
                pst.setInt(1, r.getRating());
            else
                pst.setNull(1, Types.INTEGER);

            if (r.getComment() != null)
                pst.setString(2, r.getComment());
            else
                pst.setNull(2, Types.NVARCHAR);

            pst.setInt(3, r.getReviewId());
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean delete(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, reviewId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();

        r.setReviewId(rs.getInt("review_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setProductId(rs.getInt("product_id"));

        int rating = rs.getInt("rating");
        r.setRating(rs.wasNull() ? 0 : rating);
        r.setComment(rs.getString("comment"));

        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return r;
    }
}