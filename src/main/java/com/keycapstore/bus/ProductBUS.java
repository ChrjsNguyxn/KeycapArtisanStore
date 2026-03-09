package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.dao.WishlistDAO;
import com.keycapstore.model.Product;
import java.sql.*;
import java.util.ArrayList;

public class ProductBUS {

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name, s.name AS supplier_name FROM Product p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "WHERE p.status IN ('Active', 'Hidden')";
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
                p.setSupplierName(rs.getString("supplier_name"));
                p.setSupplierId(rs.getInt("supplier_id"));
                p.setStatus(rs.getString("status"));
                p.setFeatured(rs.getBoolean("is_featured"));
                p.setDescription(rs.getString("description"));
                p.setProfile(rs.getString("profile"));
                p.setMaterial(rs.getString("material"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<Product> getActiveProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name, s.name AS supplier_name FROM Product p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
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
                p.setSupplierName(rs.getString("supplier_name"));
                p.setSupplierId(rs.getInt("supplier_id"));
                p.setStatus(rs.getString("status"));
                p.setFeatured(rs.getBoolean("is_featured"));
                p.setDescription(rs.getString("description"));
                p.setProfile(rs.getString("profile"));
                p.setMaterial(rs.getString("material"));
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

    public int addProduct(Product p, int employeeId, double entryPrice, String note) {
        Connection con = null;
        int newProductId = -1;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sql = "INSERT INTO Product(name, price, stock, image, category_id, origin, status, supplier_id, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
            pst.setString(7, p.getStatus());

            if (p.getSupplierId() > 0) {
                pst.setInt(8, p.getSupplierId());
            } else {
                pst.setNull(8, java.sql.Types.INTEGER);
            }

            pst.setBoolean(9, p.isFeatured());

            if (pst.executeUpdate() > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    newProductId = rs.getInt(1);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
            }
            return -1;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
        return newProductId;
    }

    public boolean updateProduct(Product p, int employeeId, double entryPrice, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            Product oldProduct = getProductById(p.getId());
            if (oldProduct == null) {
                return false;
            }

            String sql = "UPDATE Product SET name=?, price=?, stock=?, image=?, category_id=?, origin=?, status=?, supplier_id=?, is_featured=? WHERE product_id=?";
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
            pst.setString(7, p.getStatus());

            if (p.getSupplierId() > 0) {
                pst.setInt(8, p.getSupplierId());
            } else {
                pst.setNull(8, java.sql.Types.INTEGER);
            }
            pst.setBoolean(9, p.isFeatured());
            pst.setInt(10, p.getId());

            if (pst.executeUpdate() > 0) {
                if (p.getStock() != oldProduct.getStock()) {
                    int quantityAdded = p.getStock() - oldProduct.getStock();
                    String entrySql = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
                    PreparedStatement entryPst = con.prepareStatement(entrySql);
                    entryPst.setInt(1, p.getId());
                    entryPst.setInt(2, employeeId);
                    entryPst.setInt(3, quantityAdded);
                    entryPst.setDouble(4, entryPrice);
                    entryPst.setString(5, note);
                    entryPst.executeUpdate();
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

    public boolean deleteProduct(int productId, int employeeId, String note) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            Product oldProduct = getProductById(productId);
            if (oldProduct == null)
                return false;

            String sql = "UPDATE Product SET status = 'Inactive' WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, productId);

            if (pst.executeUpdate() > 0) {
                String entrySql = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
                PreparedStatement entryPst = con.prepareStatement(entrySql);
                entryPst.setInt(1, productId);
                entryPst.setInt(2, employeeId);
                entryPst.setInt(3, -oldProduct.getStock());
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

    public double getLatestEntryPrice(int productId) {
        double price = 0;
        String sql = "SELECT TOP 1 entry_price FROM StockEntry WHERE product_id = ? ORDER BY entry_date DESC";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("entry_price");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }

    public void saveProductImages(int productId, java.util.List<String> images) {
        if (images == null || images.isEmpty())
            return;

        String sqlDel = "DELETE FROM product_images WHERE product_id = ?";
        String sqlIns = "INSERT INTO product_images (product_id, image_path) VALUES (?, ?)";

        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement pstDel = con.prepareStatement(sqlDel)) {
                pstDel.setInt(1, productId);
                pstDel.executeUpdate();
            }

            try (PreparedStatement pstIns = con.prepareStatement(sqlIns)) {
                for (String path : images) {
                    pstIns.setInt(1, productId);
                    pstIns.setString(2, path);
                    pstIns.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public java.util.List<String> getProductImages(int productId) {
        java.util.List<String> list = new ArrayList<>();
        String sql = "SELECT image_path FROM product_images WHERE product_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String path = rs.getString("image_path");
                if (path != null && !path.isEmpty()) {
                    list.add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.List<Product> getWishlistProducts(int customerId) {
        java.util.List<Product> result = new ArrayList<>();
        WishlistDAO wishlistDAO = new WishlistDAO();
        java.util.List<com.keycapstore.model.Wishlist> wishlists = wishlistDAO.findByCustomerId(customerId);

        ArrayList<Product> allProducts = getAllProducts();

        for (com.keycapstore.model.Wishlist w : wishlists) {
            allProducts.stream()
                    .filter(p -> p.getId() == w.getProductId())
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }
}