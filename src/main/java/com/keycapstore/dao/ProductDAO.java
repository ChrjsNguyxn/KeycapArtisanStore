package com.keycapstore.dao;

import com.keycapstore.model.ProductDTO;
import com.keycapstore.config.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<ProductDTO> getAll() {
        List<ProductDTO> list = new ArrayList<>();

        String sql = """
                    SELECT p.*, c.name AS category_name, m.name AS maker_name, s.name AS supplier_name
                    FROM Product p
                    JOIN categories c ON p.category_id = c.category_id
                    LEFT JOIN makers m ON p.maker_id = m.maker_id
                    LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
                """;

        try (Connection conn = ConnectDB.getConnection();
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
                        rs.getInt("stock"),
                        rs.getString("profile"),
                        rs.getString("material"),
                        "Active".equalsIgnoreCase(rs.getString("status")),
                        rs.getTimestamp("created_at"));

                p.setCategoryName(rs.getString("category_name"));
                p.setMakerName(rs.getString("maker_name"));
                p.setOrigin(rs.getString("origin"));
                p.setStatus(rs.getString("status"));

                if (rs.getString("supplier_name") != null) {
                    p.setSupplierName(rs.getString("supplier_name"));
                }

                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean insert(ProductDTO p) {

        String sql = """
                    INSERT INTO Product
                    (category_id, maker_id, name, description, price,
                     stock, profile, material, status, supplier_id, origin)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getMakerId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setDouble(5, p.getPrice());
            ps.setInt(6, p.getStockQuantity());
            ps.setString(7, p.getProfile());
            ps.setString(8, p.getMaterial());

            String status = p.getStatus();
            if (status == null || status.isEmpty()) {
                status = p.getStockQuantity() > 0 ? "Active" : "Inactive";
            }
            ps.setString(9, status);

            ps.setInt(10, p.getSupplierId() > 0 ? p.getSupplierId() : 1);
            ps.setString(11, p.getOrigin());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public ProductDTO findByName(String name) {
        String sql = "SELECT * FROM Product WHERE name = ?";
        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProductDTO p = new ProductDTO();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setStockQuantity(rs.getInt("stock"));
                p.setStatus(rs.getString("status"));
                return p;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(ProductDTO p) {

        String sql = """
                    UPDATE Product SET
                    category_id=?, maker_id=?, name=?, description=?,
                    price=?, stock=?, profile=?, material=?, status=?, origin=?
                    WHERE product_id=?
                """;

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getMakerId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setDouble(5, p.getPrice());
            ps.setInt(6, p.getStockQuantity());
            ps.setString(7, p.getProfile());
            ps.setString(8, p.getMaterial());

            String status = p.getStatus();
            if (status == null || status.isEmpty()) {
                status = p.getStockQuantity() > 0 ? "Active" : "Inactive";
            }
            ps.setString(9, status);
            ps.setString(10, p.getOrigin());

            ps.setInt(11, p.getProductId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(int id) {

        String sql = "DELETE FROM Product WHERE product_id=?";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}