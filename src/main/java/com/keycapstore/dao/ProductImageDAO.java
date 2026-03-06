package com.keycapstore.dao;

import com.keycapstore.model.ProductImageDTO;
import com.keycapstore.config.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDAO {

    public List<ProductImageDTO> getImagesByProductId(int productId) {
        List<ProductImageDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM product_images WHERE product_id=?";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new ProductImageDTO(
                        rs.getInt("image_id"),
                        rs.getInt("product_id"),
                        rs.getString("image_url"),
                        rs.getBoolean("is_thumbnail")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean insert(ProductImageDTO img) {
        String sql = "INSERT INTO product_images(product_id, image_url, is_thumbnail) VALUES (?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, img.getProductId());
            ps.setString(2, img.getImageUrl());
            ps.setBoolean(3, img.isThumbnail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}