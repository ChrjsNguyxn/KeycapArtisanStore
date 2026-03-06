package com.keycapstore.dao;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    // Lấy tất cả sản phẩm
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "ORDER BY p.product_id DESC";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Lấy sản phẩm theo ID
    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Thêm sản phẩm mới
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (category_id, maker_id, name, description, price, " +
                     "stock_quantity, profile, material, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, product.getCategoryId());
            ps.setInt(2, product.getMakerId() > 0 ? product.getMakerId() : 1); // Mặc định maker_id = 1
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getStock());
            ps.setString(7, product.getProfile());
            ps.setString(8, product.getMaterial());
            ps.setBoolean(9, product.isActive());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                // Lấy ID vừa sinh ra
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setId(rs.getInt(1));
                    }
                }
                
                // Thêm ảnh vào bảng product_images nếu có
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    addProductImage(conn, product.getId(), product.getImage());
                }
                
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Cập nhật sản phẩm
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET category_id = ?, maker_id = ?, name = ?, description = ?, " +
                     "price = ?, stock_quantity = ?, profile = ?, material = ?, is_active = ? " +
                     "WHERE product_id = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, product.getCategoryId());
            ps.setInt(2, product.getMakerId() > 0 ? product.getMakerId() : 1);
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getStock());
            ps.setString(7, product.getProfile());
            ps.setString(8, product.getMaterial());
            ps.setBoolean(9, product.isActive());
            ps.setInt(10, product.getId());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                // Cập nhật ảnh
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    updateProductImage(conn, product.getId(), product.getImage());
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Xóa sản phẩm (xóa cứng)
    public boolean deleteProduct(int id) {
        // Xóa ảnh trước
        deleteProductImages(id);
        
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Xóa mềm (chỉ set is_active = false)
    public boolean softDeleteProduct(int id) {
        String sql = "UPDATE products SET is_active = 0 WHERE product_id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Tìm kiếm sản phẩm theo tên
    public List<Product> searchProductsByName(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.name LIKE ? ORDER BY p.product_id DESC";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Lấy sản phẩm theo danh mục
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.category_id = ? ORDER BY p.product_id DESC";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Lấy sản phẩm còn hàng (stock > 0)
    public List<Product> getAvailableProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.stock_quantity > 0 AND p.is_active = 1 ORDER BY p.product_id DESC";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Cập nhật số lượng tồn kho
    public boolean updateStock(int productId, int newQuantity) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Giảm số lượng tồn kho (khi bán hàng)
    public boolean decreaseStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND stock_quantity >= ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Tăng số lượng tồn kho (khi nhập hàng)
    public boolean increaseStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // === PHƯƠNG THỨC HỖ TRỢ ẢNH SẢN PHẨM ===
    
    // Thêm ảnh cho sản phẩm
    private boolean addProductImage(Connection conn, int productId, String imageUrl) {
        String sql = "INSERT INTO product_images (product_id, image_url, is_thumbnail) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, imageUrl);
            ps.setBoolean(3, true); // Mặc định là thumbnail
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Cập nhật ảnh sản phẩm
    private boolean updateProductImage(Connection conn, int productId, String imageUrl) {
        // Kiểm tra xem đã có ảnh chưa
        String checkSql = "SELECT COUNT(*) FROM product_images WHERE product_id = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, productId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Đã có ảnh -> update
                String updateSql = "UPDATE product_images SET image_url = ? WHERE product_id = ? AND is_thumbnail = 1";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setString(1, imageUrl);
                    updatePs.setInt(2, productId);
                    return updatePs.executeUpdate() > 0;
                }
            } else {
                // Chưa có ảnh -> insert
                return addProductImage(conn, productId, imageUrl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Xóa ảnh sản phẩm
    private boolean deleteProductImages(int productId) {
        String sql = "DELETE FROM product_images WHERE product_id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lấy ảnh thumbnail của sản phẩm
    public String getProductThumbnail(int productId) {
        String sql = "SELECT image_url FROM product_images WHERE product_id = ? AND is_thumbnail = 1";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image_url");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // === PHƯƠNG THỨC MAP DỮ LIỆU ===
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock_quantity"));
        p.setProfile(rs.getString("profile"));
        p.setMaterial(rs.getString("material"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setMakerId(rs.getInt("maker_id"));
        p.setActive(rs.getBoolean("is_active"));
        
        // Lấy ảnh từ bảng product_images
        String image = getProductThumbnail(p.getId());
        p.setImage(image != null ? image : "");
        
        return p;
    }
}