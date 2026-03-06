package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Product;
import java.sql.*;
import java.util.ArrayList;

public class ProductBUS {

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "WHERE p.is_active = 1 ORDER BY p.product_id DESC";
        
        try (Connection con = ConnectDB.getConnection(); 
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock_quantity"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));
                p.setMakerId(rs.getInt("maker_id"));
                p.setProfile(rs.getString("profile")); // Xuất xứ sẽ được lưu vào đây
                p.setMaterial(rs.getString("material"));
                p.setDescription(rs.getString("description"));
                
                // Lấy ảnh từ bảng product_images
                String imageSql = "SELECT image_url FROM product_images WHERE product_id = ? AND is_thumbnail = 1";
                try (PreparedStatement imgPs = con.prepareStatement(imageSql)) {
                    imgPs.setInt(1, rs.getInt("product_id"));
                    ResultSet imgRs = imgPs.executeQuery();
                    if (imgRs.next()) {
                        p.setImage(imgRs.getString("image_url"));
                    }
                }
                
                list.add(p);
            }
            System.out.println("Đã load " + list.size() + " sản phẩm từ database");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Product getProductById(int productId) {
        Product p = null;
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection con = ConnectDB.getConnection(); 
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                p = new Product();
                p.setId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock_quantity"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setMakerId(rs.getInt("maker_id"));
                p.setProfile(rs.getString("profile"));
                p.setMaterial(rs.getString("material"));
                p.setDescription(rs.getString("description"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    // Thêm sản phẩm mới - BỎ QUA bảng StockEntry
    public boolean addProduct(Product p, int employeeId, double entryPrice, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Lấy maker_id mặc định
            int defaultMakerId = getDefaultMakerId(con);
            
            // Insert với đầy đủ các cột - profile là ô "Xuất xứ" trong UI
            String sql = "INSERT INTO products(name, price, stock_quantity, category_id, maker_id, profile, material, description, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());

            // Xử lý category_id
            if (p.getCategoryId() > 0) {
                pst.setInt(4, p.getCategoryId());
            } else {
                pst.setNull(4, java.sql.Types.INTEGER);
            }
            
            // Xử lý maker_id
            if (p.getMakerId() > 0) {
                pst.setInt(5, p.getMakerId());
            } else {
                pst.setInt(5, defaultMakerId);
            }
            
            // Profile - đây là ô "Xuất xứ" trong UI
            pst.setString(6, p.getProfile());
            
            // Material
            pst.setString(7, p.getMaterial());
            
            // Description
            pst.setString(8, p.getDescription());

            System.out.println("Đang thêm sản phẩm: " + p.getName());
            System.out.println("Xuất xứ (profile): " + p.getProfile());
            System.out.println("Category ID: " + p.getCategoryId());
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    int newProductId = rs.getInt(1);
                    System.out.println("Đã thêm sản phẩm với ID: " + newProductId);
                    
                    // Thêm ảnh vào bảng product_images nếu có
                    if (p.getImage() != null && !p.getImage().isEmpty()) {
                        try {
                            String imgSql = "INSERT INTO product_images (product_id, image_url, is_thumbnail) VALUES (?, ?, 1)";
                            PreparedStatement imgPst = con.prepareStatement(imgSql);
                            imgPst.setInt(1, newProductId);
                            imgPst.setString(2, p.getImage());
                            imgPst.executeUpdate();
                            System.out.println("Đã thêm ảnh: " + p.getImage());
                        } catch (Exception e) {
                            System.out.println("Lỗi thêm ảnh: " + e.getMessage());
                        }
                    }
                    
                    // Ghi log vào system_logs thay vì StockEntry
                    try {
                        String logSql = "INSERT INTO system_logs (employee_id, action, target_table, record_id, description, log_date) VALUES (?, ?, ?, ?, ?, GETDATE())";
                        PreparedStatement logPst = con.prepareStatement(logSql);
                        logPst.setInt(1, employeeId);
                        logPst.setString(2, "INSERT");
                        logPst.setString(3, "products");
                        logPst.setInt(4, newProductId);
                        logPst.setString(5, note);
                        logPst.executeUpdate();
                        System.out.println("Đã ghi log hệ thống");
                    } catch (Exception e) {
                        System.out.println("Lỗi ghi log: " + e.getMessage());
                    }
                }
                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi SQL khi thêm sản phẩm: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
        return false;
    }

    // Cập nhật sản phẩm - BỎ QUA bảng StockEntry
    public boolean updateProduct(Product p, int employeeId, double entryPrice, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            Product oldProduct = getProductById(p.getId());
            if (oldProduct == null) {
                return false;
            }

            int defaultMakerId = getDefaultMakerId(con);
            
            String sql = "UPDATE products SET name=?, price=?, stock_quantity=?, category_id=?, maker_id=?, profile=?, material=?, description=? WHERE product_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());

            if (p.getCategoryId() > 0) {
                pst.setInt(4, p.getCategoryId());
            } else {
                pst.setNull(4, java.sql.Types.INTEGER);
            }
            
            if (p.getMakerId() > 0) {
                pst.setInt(5, p.getMakerId());
            } else {
                pst.setInt(5, defaultMakerId);
            }
            
            // Profile - xuất xứ
            pst.setString(6, p.getProfile());
            pst.setString(7, p.getMaterial());
            pst.setString(8, p.getDescription());
            pst.setInt(9, p.getId());

            System.out.println("Đang cập nhật sản phẩm ID: " + p.getId());
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Đã cập nhật sản phẩm thành công");
                
                // Cập nhật ảnh
                if (p.getImage() != null && !p.getImage().isEmpty()) {
                    try {
                        String imgSql = "UPDATE product_images SET image_url = ? WHERE product_id = ? AND is_thumbnail = 1";
                        PreparedStatement imgPst = con.prepareStatement(imgSql);
                        imgPst.setString(1, p.getImage());
                        imgPst.setInt(2, p.getId());
                        int imgUpdated = imgPst.executeUpdate();
                        
                        if (imgUpdated == 0) {
                            // Chưa có ảnh, thêm mới
                            String insertImgSql = "INSERT INTO product_images (product_id, image_url, is_thumbnail) VALUES (?, ?, 1)";
                            PreparedStatement insertImgPst = con.prepareStatement(insertImgSql);
                            insertImgPst.setInt(1, p.getId());
                            insertImgPst.setString(2, p.getImage());
                            insertImgPst.executeUpdate();
                        }
                        System.out.println("Đã cập nhật ảnh");
                    } catch (Exception e) {
                        System.out.println("Lỗi cập nhật ảnh: " + e.getMessage());
                    }
                }
                
                // Ghi log thay đổi
                try {
                    String logSql = "INSERT INTO system_logs (employee_id, action, target_table, record_id, description, log_date) VALUES (?, ?, ?, ?, ?, GETDATE())";
                    PreparedStatement logPst = con.prepareStatement(logSql);
                    logPst.setInt(1, employeeId);
                    logPst.setString(2, "UPDATE");
                    logPst.setString(3, "products");
                    logPst.setInt(4, p.getId());
                    logPst.setString(5, note);
                    logPst.executeUpdate();
                    System.out.println("Đã ghi log hệ thống");
                } catch (Exception e) {
                    System.out.println("Lỗi ghi log: " + e.getMessage());
                }
                
                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi SQL khi cập nhật sản phẩm: " + e.getMessage());
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
        return false;
    }

    // Xóa sản phẩm (soft delete)
    public boolean deleteProduct(int productId, int employeeId, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            Product oldProduct = getProductById(productId);
            if (oldProduct == null)
                return false;

            String sql = "UPDATE products SET is_active = 0 WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, productId);

            System.out.println("Đang xóa sản phẩm ID: " + productId);
            
            if (pst.executeUpdate() > 0) {
                // Ghi log xóa
                try {
                    String logSql = "INSERT INTO system_logs (employee_id, action, target_table, record_id, description, log_date) VALUES (?, ?, ?, ?, ?, GETDATE())";
                    PreparedStatement logPst = con.prepareStatement(logSql);
                    logPst.setInt(1, employeeId);
                    logPst.setString(2, "DELETE");
                    logPst.setString(3, "products");
                    logPst.setInt(4, productId);
                    logPst.setString(5, note);
                    logPst.executeUpdate();
                    System.out.println("Đã ghi log hệ thống");
                } catch (Exception e) {
                    System.out.println("Lỗi ghi log: " + e.getMessage());
                }
                
                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi SQL khi xóa sản phẩm: " + e.getMessage());
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
        return false;
    }

    // Thêm phương thức này vào class ProductBUS
public ArrayList<Product> getAvailableProducts() {
    ArrayList<Product> list = new ArrayList<>();
    String sql = "SELECT p.*, c.name AS category_name FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.category_id " +
            "WHERE p.stock_quantity > 0 AND p.is_active = 1";
    try (Connection con = ConnectDB.getConnection(); 
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        
        while (rs.next()) {
            Product p = new Product();
            p.setId(rs.getInt("product_id"));
            p.setName(rs.getString("name"));
            p.setPrice(rs.getDouble("price"));
            p.setStock(rs.getInt("stock_quantity"));
            p.setCategoryId(rs.getInt("category_id"));
            p.setCategoryName(rs.getString("category_name"));
            p.setMakerId(rs.getInt("maker_id"));
            p.setProfile(rs.getString("profile"));
            p.setMaterial(rs.getString("material"));
            
            // Lấy ảnh
            String imageSql = "SELECT image_url FROM product_images WHERE product_id = ? AND is_thumbnail = 1";
            try (PreparedStatement imgPs = con.prepareStatement(imageSql)) {
                imgPs.setInt(1, rs.getInt("product_id"));
                ResultSet imgRs = imgPs.executeQuery();
                if (imgRs.next()) {
                    p.setImage(imgRs.getString("image_url"));
                }
            }
            
            list.add(p);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}
    
    // Hàm lấy maker_id mặc định
    private int getDefaultMakerId(Connection con) {
        String sql = "SELECT TOP 1 maker_id FROM makers ORDER BY maker_id";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("maker_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}