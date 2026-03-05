package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Product;
import java.sql.*;
import java.util.ArrayList;

public class ProductBUS {

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name, p.origin FROM Product p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "WHERE p.status = 'Active'";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("image"),
                        rs.getString("status"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));
                p.setOrigin(rs.getString("origin"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Product getProductById(int productId) {
        Product p = null;
        String sql = "SELECT * FROM Product WHERE product_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                p = new Product();
                p.setId(rs.getInt("product_id"));
                p.setStock(rs.getInt("stock"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    // Thêm tham số 'note'
    public boolean addProduct(Product p, int employeeId, double entryPrice, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sql = "INSERT INTO Product(name, price, stock, image, category_id, origin, status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());
            pst.setString(4, p.getImage());

            // Kiểm tra nếu categoryId hợp lệ (>0) thì mới set, ngược lại set NULL
            if (p.getCategoryId() > 0) {
                pst.setInt(5, p.getCategoryId());
            } else {
                pst.setNull(5, java.sql.Types.INTEGER);
            }

            pst.setString(6, p.getOrigin());

            if (pst.executeUpdate() > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    int newProductId = rs.getInt(1);
                    // Lưu vào lịch sử nhập kho với ghi chú
                    String entrySql = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
                    PreparedStatement entryPst = con.prepareStatement(entrySql);
                    entryPst.setInt(1, newProductId);
                    entryPst.setInt(2, employeeId);
                    entryPst.setInt(3, p.getStock());
                    entryPst.setDouble(4, entryPrice);
                    entryPst.setString(5, note);
                    entryPst.executeUpdate();
                }
                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    // Thêm tham số 'note'
    public boolean updateProduct(Product p, int employeeId, double entryPrice, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            Product oldProduct = getProductById(p.getId());
            if (oldProduct == null) {
                return false;
            }

            String sql = "UPDATE Product SET name=?, price=?, stock=?, image=?, category_id=?, origin=? WHERE product_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());
            pst.setString(4, p.getImage());

            if (p.getCategoryId() > 0) {
                pst.setInt(5, p.getCategoryId());
            } else {
                pst.setNull(5, java.sql.Types.INTEGER);
            }

            pst.setString(6, p.getOrigin());
            pst.setInt(7, p.getId());

            if (pst.executeUpdate() > 0) {
                // Chỉ ghi vào lịch sử nếu số lượng thực sự thay đổi
                if (p.getStock() != oldProduct.getStock()) {
                    int quantityAdded = p.getStock() - oldProduct.getStock();
                    String entrySql = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
                    PreparedStatement entryPst = con.prepareStatement(entrySql);
                    entryPst.setInt(1, p.getId());
                    entryPst.setInt(2, employeeId);
                    entryPst.setInt(3, quantityAdded); // Số âm nếu giảm, dương nếu tăng
                    entryPst.setDouble(4, entryPrice);
                    entryPst.setString(5, note);
                    entryPst.executeUpdate();
                }
                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi SQL khi cập nhật sản phẩm: " + e.getMessage()); // In lỗi rõ ràng ra Console
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

    // Cập nhật hàm xóa để ghi log
    public boolean deleteProduct(int productId, int employeeId, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Lấy thông tin cũ để biết số lượng tồn kho hiện tại
            Product oldProduct = getProductById(productId);
            if (oldProduct == null)
                return false;

            String sql = "UPDATE Product SET status = 'Inactive' WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, productId);

            if (pst.executeUpdate() > 0) {
                // Ghi log xuất kho toàn bộ số lượng còn lại
                String entrySql = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
                PreparedStatement entryPst = con.prepareStatement(entrySql);
                entryPst.setInt(1, productId);
                entryPst.setInt(2, employeeId);
                entryPst.setInt(3, -oldProduct.getStock()); // Trừ hết tồn kho
                entryPst.setDouble(4, 0);
                entryPst.setString(5, "Xóa sản phẩm: " + note);
                entryPst.executeUpdate();

                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}