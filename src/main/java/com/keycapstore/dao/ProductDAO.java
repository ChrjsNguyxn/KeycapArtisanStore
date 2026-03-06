package com.keycapstore.dao;

import com.keycapstore.model.ProductDTO;
import com.keycapstore.config.DBConnection; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<ProductDTO> getAll(){
        List<ProductDTO> list = new ArrayList<>();

        String sql = "SELECT p.*, c.name AS category_name, m.name AS maker_name " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN makers m ON p.maker_id = m.maker_id";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){

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

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public List<ProductDTO> filter(String name,
                                   Integer categoryId,
                                   Double minPrice,
                                   Double maxPrice){

        List<ProductDTO> list = new ArrayList<>();

        String sql = "SELECT p.*, c.name AS category_name, m.name AS maker_name " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "JOIN makers m ON p.maker_id = m.maker_id WHERE 1=1 ";

        if(name != null && !name.isEmpty())
            sql += " AND p.name LIKE ?";

        if(categoryId != null)
            sql += " AND p.category_id = ?";

        if(minPrice != null)
            sql += " AND p.price >= ?";

        if(maxPrice != null)
            sql += " AND p.price <= ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            int index = 1;

            if(name != null && !name.isEmpty())
                ps.setString(index++, "%" + name + "%");

            if(categoryId != null)
                ps.setInt(index++, categoryId);

            if(minPrice != null)
                ps.setDouble(index++, minPrice);

            if(maxPrice != null)
                ps.setDouble(index++, maxPrice);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){

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

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }
    public int insertAndGetId(ProductDTO p) {

    String sql = "INSERT INTO products(category_id,maker_id,name,price,stock_quantity,profile,material,description) VALUES (?,?,?,?,?,?,?,?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, p.getCategoryId());
        ps.setInt(2, p.getMakerId());
        ps.setString(3, p.getName());
        ps.setDouble(4, p.getPrice());
        ps.setInt(5, p.getStockQuantity());
        ps.setString(6, p.getProfile());
        ps.setString(7, p.getMaterial());
        ps.setString(8, p.getDescription());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()){
            return rs.getInt(1);
        }

    } catch (Exception e){
        e.printStackTrace();
    }

    return -1;
}
    public boolean delete(int id) {

    String deleteImportItems = "DELETE FROM import_order_items WHERE product_id = ?";
    String deleteImages = "DELETE FROM product_images WHERE product_id = ?";
    String deleteProduct = "DELETE FROM products WHERE product_id = ?";

    Connection conn = null;

    try {

        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement ps1 = conn.prepareStatement(deleteImportItems);
        ps1.setInt(1, id);
        ps1.executeUpdate();

        PreparedStatement ps2 = conn.prepareStatement(deleteImages);
        ps2.setInt(1, id);
        ps2.executeUpdate();

        PreparedStatement ps3 = conn.prepareStatement(deleteProduct);
        ps3.setInt(1, id);

        int rows = ps3.executeUpdate();

        conn.commit();

        return rows > 0;

    } catch (Exception e) {

        try {
            if (conn != null) conn.rollback();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        e.printStackTrace();
    }

    return false;
}
    public boolean update(ProductDTO p) { 
    String sql = "UPDATE products SET " 
            + "category_id = ?, " 
            + "maker_id = ?, " 
            + "name = ?, " 
            + "price = ?, " 
            + "stock_quantity = ?, " 
            + "profile = ?, " 
            + "material = ?, " 
            + "description = ? " 
            + "WHERE product_id = ?"; 
    try (Connection conn = DBConnection.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) { 
        ps.setInt(1, p.getCategoryId()); 
        ps.setInt(2, p.getMakerId()); 
        ps.setString(3, p.getName()); 
        ps.setDouble(4, p.getPrice()); 
        ps.setInt(5, p.getStockQuantity()); 
        ps.setString(6, p.getProfile()); 
        ps.setString(7, p.getMaterial()); 
        ps.setString(8, p.getDescription()); 
        ps.setInt(9, p.getProductId()); 
        int rows = ps.executeUpdate(); 
        return rows > 0; 
    } catch (SQLException e) { 
        e.printStackTrace(); 
    } 
    return false; }
    public ProductDTO getById(int id) {

    String sql = "SELECT * FROM products WHERE product_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            ProductDTO p = new ProductDTO();

            p.setProductId(rs.getInt("product_id"));
            p.setName(rs.getString("name"));
            p.setPrice(rs.getDouble("price"));
            p.setStockQuantity(rs.getInt("stock_quantity"));
            p.setProfile(rs.getString("profile"));
            p.setMaterial(rs.getString("material"));
            p.setDescription(rs.getString("description"));
            p.setCategoryId(rs.getInt("category_id"));
            p.setMakerId(rs.getInt("maker_id"));

            return p;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
}