package com.keycapstore.dao;

import com.keycapstore.model.ImportOrderDTO;
import com.keycapstore.model.ImportOrderItemDTO;
import com.mycompany.mavenproject2.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportOrderDAO {

    // ================= INSERT =================
    public boolean insertImportOrder(ImportOrderDTO order, List<ImportOrderItemDTO> items) {

        String insertOrderSQL =
                "INSERT INTO import_orders (supplier_id, employee_id, total_cost, note) VALUES (?, ?, ?, ?)";

        String insertItemSQL =
                "INSERT INTO import_order_items (import_id, product_id, quantity, import_price) VALUES (?, ?, ?, ?)";

        String updateStockSQL =
                "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int importId;

            // Insert Order
            try (PreparedStatement psOrder =
                         conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {

                psOrder.setInt(1, order.getSupplierId());
                psOrder.setInt(2, order.getEmployeeId());
                psOrder.setDouble(3, order.getTotalCost());
                psOrder.setString(4, order.getNote());

                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        importId = rs.getInt(1);
                    } else {
                        throw new SQLException("Không lấy được import_id.");
                    }
                }
            }

            // Insert Items + Update Stock
            for (ImportOrderItemDTO item : items) {

                try (PreparedStatement psItem = conn.prepareStatement(insertItemSQL)) {

                    psItem.setInt(1, importId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getImportPrice());

                    psItem.executeUpdate();
                }

                try (PreparedStatement psStock = conn.prepareStatement(updateStockSQL)) {

                    psStock.setInt(1, item.getQuantity());
                    psStock.setInt(2, item.getProductId());

                    psStock.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= DELETE =================
    public boolean deleteImportOrder(int importId) {

        String getItemsSQL =
                "SELECT product_id, quantity FROM import_order_items WHERE import_id = ?";

        String deleteItemsSQL =
                "DELETE FROM import_order_items WHERE import_id = ?";

        String deleteOrderSQL =
                "DELETE FROM import_orders WHERE import_id = ?";

        String rollbackStockSQL =
                "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Lấy danh sách item để rollback stock
            List<ImportOrderItemDTO> items = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(getItemsSQL)) {
                ps.setInt(1, importId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    ImportOrderItemDTO item = new ImportOrderItemDTO();
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    items.add(item);
                }
            }

            // Rollback stock
            for (ImportOrderItemDTO item : items) {
                try (PreparedStatement psStock = conn.prepareStatement(rollbackStockSQL)) {
                    psStock.setInt(1, item.getQuantity());
                    psStock.setInt(2, item.getProductId());
                    psStock.executeUpdate();
                }
            }

            // Delete items
            try (PreparedStatement ps = conn.prepareStatement(deleteItemsSQL)) {
                ps.setInt(1, importId);
                ps.executeUpdate();
            }

            // Delete order
            try (PreparedStatement ps = conn.prepareStatement(deleteOrderSQL)) {
                ps.setInt(1, importId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {

            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= GET ALL =================
    public List<ImportOrderDTO> getAll() {

        List<ImportOrderDTO> list = new ArrayList<>();

        String sql = "SELECT * FROM import_orders ORDER BY import_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ImportOrderDTO order = new ImportOrderDTO();

                order.setImportId(rs.getInt("import_id"));
                order.setSupplierId(rs.getInt("supplier_id"));
                order.setEmployeeId(rs.getInt("employee_id"));
                order.setTotalCost(rs.getDouble("total_cost"));
                order.setImportDate(rs.getTimestamp("import_date"));
                order.setNote(rs.getString("note"));

                list.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}