package com.keycapstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.dto.Product;

public class ProductDAO {

    /**
     * Lấy toàn bộ sản phẩm đang active
     */
    public List<Product> getAllProducts() {

        List<Product> productList = new ArrayList<>();

        String sql = "SELECT product_id, name, price, stock_quantity " +
                     "FROM products WHERE is_active = 1";

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Product product = new Product();

                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock_quantity"));

                productList.add(product);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return productList;
    }


    /**
     * Lấy sản phẩm theo ID
     */
    public Product getProductById(int id) {

        String sql = "SELECT product_id, name, price, stock_quantity " +
                     "FROM products WHERE product_id = ?";

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Product product = new Product();

                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock_quantity"));

                return product;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Giảm stock sau khi đặt hàng
     */
    public boolean updateStock(int productId, int quantity) {

        String sql = "UPDATE products " +
                     "SET stock_quantity = stock_quantity - ? " +
                     "WHERE product_id = ?";

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}