package com.keycapstore.dao;

import com.keycapstore.model.Wishlist;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *   insert(Wishlist)              → Thêm sản phẩm vào wishlist
 *   findByCustomerId(int)         → Lấy toàn bộ wishlist của khách hàng
 *   isExist(int, int)             → Kiểm tra sản phẩm đã trong wishlist chưa
 *   delete(int)                   → Xóa theo wishlist_id
 *   deleteByCustomerAndProduct    → Xóa theo customer_id + product_id
 *   deleteAllByCustomerId(int)    → Xóa toàn bộ wishlist của khách hàng
 */
public class WishlistDAO extends BaseDAO {

    /**
     * Thêm sản phẩm vào wishlist
     */
    public boolean insert(Wishlist w) {
        String sql = "INSERT INTO wishlists (customer_id, product_id, created_at) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, w.getCustomerId());
            pst.setInt(2, w.getProductId());

            pst.setTimestamp(3, w.getCreatedAt() != null
                ? Timestamp.valueOf(w.getCreatedAt())
                : Timestamp.valueOf(LocalDateTime.now()));

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy toàn bộ wishlist của một khách hàng
     */
    public List<Wishlist> findByCustomerId(int customerId) {
        List<Wishlist> list = new ArrayList<>();
        String sql = "SELECT * FROM wishlists WHERE customer_id = ? ORDER BY created_at DESC";

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

    /**
     * Kiểm tra sản phẩm đã có trong wishlist của khách hàng chưa
     * Dùng trước khi insert để tránh trùng
     *
     * return true nếu đã tồn tại
     */
    public boolean isExist(int customerId, int productId) {
        String sql = "SELECT wishlist_id FROM wishlists WHERE customer_id = ? AND product_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            pst.setInt(2, productId);
            return pst.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Xóa theo wishlist_id
     */
    public boolean delete(int wishlistId) {
        String sql = "DELETE FROM wishlists WHERE wishlist_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, wishlistId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa theo customer_id + product_id
     * Dùng khi người dùng bỏ tim sản phẩm mà không cần biết wishlist_id
     */
    public boolean deleteByCustomerAndProduct(int customerId, int productId) {
        String sql = "DELETE FROM wishlists WHERE customer_id = ? AND product_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            pst.setInt(2, productId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa toàn bộ wishlist của một khách hàng
     */
    public boolean deleteAllByCustomerId(int customerId) {
        String sql = "DELETE FROM wishlists WHERE customer_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private Wishlist mapRow(ResultSet rs) throws SQLException {
        Wishlist w = new Wishlist();

        w.setWishlistId(rs.getInt("wishlist_id"));
        w.setCustomerId(rs.getInt("customer_id"));
        w.setProductId(rs.getInt("product_id"));


        Timestamp ts = rs.getTimestamp("created_at");
        w.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return w;
    }
}