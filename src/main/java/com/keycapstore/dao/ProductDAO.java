package com.keycapstore.dao;

import com.keycapstore.model.ProductDTO;
import com.mycompany.mavenproject2.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // ===== LẤY TẤT CẢ (JOIN category + maker) =====
    public List<ProductDTO> getAll() {
        List<ProductDTO> list = new ArrayList<>();

        String sql = """
            SELECT p.*, c.name AS category_name, m.name AS maker_name
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            JOIN makers m ON p.maker_id = m.maker_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ProductDTO p = new ProductDTO(
                        rs.getInt("product_id"),
                        rs.getInt("category_id"),
                        rs.getInt("maker_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("profile"),
                        rs.getString("material"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at")
                );

                p.setCategoryName(rs.getString("category_name"));
                p.setMakerName(rs.getString("maker_name"));

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===== INSERT =====
    public boolean insert(ProductDTO p) {

        String sql = """
            INSERT INTO products
            (category_id, maker_id, name, description, price,
             stock_quantity, profile, material, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getMakerId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setDouble(5, p.getPrice());
            ps.setInt(6, p.getStockQuantity());
            ps.setString(7, p.getProfile());
            ps.setString(8, p.getMaterial());

            // 🔥 TỰ ĐỘNG SET ACTIVE THEO STOCK
            ps.setBoolean(9, p.getStockQuantity() > 0);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ===== UPDATE =====
    public boolean update(ProductDTO p) {

        String sql = """
            UPDATE products SET
            category_id=?, maker_id=?, name=?, description=?,
            price=?, stock_quantity=?, profile=?, material=?, is_active=?
            WHERE product_id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getMakerId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setDouble(5, p.getPrice());
            ps.setInt(6, p.getStockQuantity());
            ps.setString(7, p.getProfile());
            ps.setString(8, p.getMaterial());

            // 🔥 TỰ ĐỘNG SET ACTIVE THEO STOCK
            ps.setBoolean(9, p.getStockQuantity() > 0);

            ps.setInt(10, p.getProductId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ===== DELETE =====
    public boolean delete(int id) {

        String sql = "DELETE FROM products WHERE product_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}