package com.keycapstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    public boolean decreaseStock(int productId, int quantity, Connection conn) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);

            int affected = ps.executeUpdate();
            return affected > 0; // nếu = 0 nghĩa là không đủ stock
        }
    }

    public double getPriceById(int productId, Connection conn) throws SQLException {
        String sql = "SELECT price FROM products WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price");
            }
        }

        throw new SQLException("Product not found");
    }
}