package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.InvoiceDetail;
import com.keycapstore.model.ShippingMethod;
import com.keycapstore.model.Voucher;
import java.sql.*;
import java.util.ArrayList;

public class SalesBUS {

    public ArrayList<ShippingMethod> getAllShipping() {
        ArrayList<ShippingMethod> list = new ArrayList<>();
        String sql = "SELECT * FROM shipping_methods";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new ShippingMethod(rs.getInt("shipping_method_id"), rs.getString("name"),
                        rs.getDouble("price")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. CHECK VOUCHER
    public Voucher getValidVoucher(String code) {
        // Chỉ lấy Voucher còn hạn và còn số lượng
        // Fix: Dùng UPPER để không phân biệt hoa thường
        String sql = "SELECT * FROM vouchers WHERE code = ? AND quantity > 0 AND CAST(expired_date AS DATE) >= CAST(GETDATE() AS DATE) AND CAST(start_date AS DATE) <= CAST(GETDATE() AS DATE)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, code.toUpperCase()); // Chuyển mã về chữ hoa trước khi query
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Voucher(rs.getInt("voucher_id"), rs.getString("code"), rs.getDouble("discount_percent"),
                        rs.getInt("quantity"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkout(int empId, String customerName, String customerPhone, double totalAmount,
            ArrayList<InvoiceDetail> cart) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // 1. Tạo Hóa Đơn (Invoice)
            String sqlInvoice = "INSERT INTO Invoice (emp_id, customer_name, customer_phone, total_amount, created_at) VALUES (?, ?, ?, ?, GETDATE())";
            PreparedStatement pstInv = con.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS);
            pstInv.setInt(1, empId);
            pstInv.setString(2, customerName.isEmpty() ? "Khách Lẻ" : customerName);

            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                pstInv.setNull(3, java.sql.Types.VARCHAR);
            } else {
                pstInv.setString(3, customerPhone);
            }

            pstInv.setDouble(4, totalAmount);
            pstInv.executeUpdate();

            // Lấy ID hóa đơn vừa tạo
            ResultSet rsKeys = pstInv.getGeneratedKeys();
            int currentInvoiceId = -1;
            if (rsKeys.next()) {
                currentInvoiceId = rsKeys.getInt(1);
            } else {
                throw new SQLException("Không lấy được ID hóa đơn.");
            }

            // 2. Xử lý từng sản phẩm trong giỏ
            String sqlDetail = "INSERT INTO InvoiceDetail (invoice_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement pstDetail = con.prepareStatement(sqlDetail);

            String sqlUpdateStock = "UPDATE Product SET stock = stock - ? WHERE product_id = ?";
            PreparedStatement pstStock = con.prepareStatement(sqlUpdateStock);

            String sqlStockEntry = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
            PreparedStatement pstEntry = con.prepareStatement(sqlStockEntry);

            for (InvoiceDetail item : cart) {

                pstDetail.setInt(1, currentInvoiceId);
                pstDetail.setInt(2, item.getProductId());
                pstDetail.setInt(3, item.getQuantity());
                pstDetail.setDouble(4, item.getPrice());
                pstDetail.executeUpdate();

                pstStock.setInt(1, item.getQuantity());
                pstStock.setInt(2, item.getProductId());
                pstStock.executeUpdate();

                pstEntry.setInt(1, item.getProductId());
                pstEntry.setInt(2, empId);
                pstEntry.setInt(3, -item.getQuantity());
                pstEntry.setDouble(4, item.getPrice());
                pstEntry.setString(5, "Bán hàng - Hóa đơn #" + currentInvoiceId);
                pstEntry.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean superCheckout(int empId, int customerId, String customerName, String customerPhone,
            double totalAmount, ArrayList<InvoiceDetail> cart, int voucherId, String paymentMethod,
            boolean isShipping, String shippingAddress, int shippingMethodId) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlInvoice = "INSERT INTO Invoice (emp_id, customer_id, customer_name, customer_phone, total_amount, created_at, is_shipping, shipping_address, shipping_status, shipping_method_id) VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, ?)";
            PreparedStatement pstInv = con.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS);
            pstInv.setInt(1, empId);
            if (customerId > 0) {
                pstInv.setInt(2, customerId);
            } else {
                pstInv.setNull(2, java.sql.Types.INTEGER);
            }
            pstInv.setString(3, customerName);
            pstInv.setString(4, customerPhone);
            pstInv.setDouble(5, totalAmount);

            // Các trường mới cho Shipping
            pstInv.setBoolean(6, isShipping);
            pstInv.setString(7, shippingAddress);
            // Nếu là ship thì trạng thái là Pending, ngược lại là Completed
            pstInv.setString(8, isShipping ? "Pending" : "Completed");

            if (isShipping && shippingMethodId > 0) {
                pstInv.setInt(9, shippingMethodId);
            } else {
                pstInv.setNull(9, java.sql.Types.INTEGER);
            }

            pstInv.executeUpdate();

            ResultSet rsKeys = pstInv.getGeneratedKeys();
            int currentInvoiceId = -1;
            if (rsKeys.next())
                currentInvoiceId = rsKeys.getInt(1);

            String sqlDetail = "INSERT INTO InvoiceDetail (invoice_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE Product SET stock = stock - ? WHERE product_id = ?";
            PreparedStatement pstDetail = con.prepareStatement(sqlDetail);
            PreparedStatement pstStock = con.prepareStatement(sqlUpdateStock);

            for (InvoiceDetail item : cart) {
                pstDetail.setInt(1, currentInvoiceId);
                pstDetail.setInt(2, item.getProductId());
                pstDetail.setInt(3, item.getQuantity());
                pstDetail.setDouble(4, item.getPrice());
                pstDetail.executeUpdate();

                pstStock.setInt(1, item.getQuantity());
                pstStock.setInt(2, item.getProductId());
                pstStock.executeUpdate();
            }

            // Ghi log xuất kho (StockEntry) - Tách ra vòng lặp riêng hoặc gộp chung đều
            // được, nhưng phải đảm bảo chạy
            // Ở đây mình dùng lại PreparedStatement pstEntry nếu đã khai báo, hoặc tạo mới
            String sqlStockEntry = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, ?, ?, GETDATE(), ?)";
            PreparedStatement pstEntry = con.prepareStatement(sqlStockEntry);

            for (InvoiceDetail item : cart) {
                pstEntry.setInt(1, item.getProductId());
                pstEntry.setInt(2, empId);
                pstEntry.setInt(3, -item.getQuantity()); // Số âm cho xuất kho
                pstEntry.setDouble(4, item.getPrice());
                pstEntry.setString(5, "Bán hàng - Hóa đơn #" + currentInvoiceId);
                pstEntry.executeUpdate();
            }

            if (voucherId > 0) {
                String sqlVoucher = "UPDATE vouchers SET quantity = quantity - 1 WHERE voucher_id = ?";
                PreparedStatement pstVoucher = con.prepareStatement(sqlVoucher);
                pstVoucher.setInt(1, voucherId);
                pstVoucher.executeUpdate();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null)
                    con.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}