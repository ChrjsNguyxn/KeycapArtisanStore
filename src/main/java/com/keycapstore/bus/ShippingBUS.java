package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.ShippingOrder;
import java.sql.*;
import java.util.ArrayList;

public class ShippingBUS {

    public ArrayList<ShippingOrder> getOrdersByStatus(String tabType) {
        ArrayList<ShippingOrder> list = new ArrayList<>();
        String sql = "SELECT i.invoice_id, i.customer_name, i.customer_phone, i.shipping_address, " +
                "i.total_amount, i.shipping_status, i.created_at, i.tracking_number, s.name as ship_method " +
                "FROM Invoice i " +
                "LEFT JOIN shipping_methods s ON i.shipping_method_id = s.shipping_method_id " +
                "WHERE i.is_shipping = 1 ";

        if (tabType.equals("PENDING")) {
            sql += "AND i.shipping_status = 'Pending' ";
        } else if (tabType.equals("SHIPPING")) {
            sql += "AND i.shipping_status = 'Shipping' ";
        } else if (tabType.equals("HISTORY")) {
            sql += "AND i.shipping_status IN ('Delivered', 'Cancelled') ";
        }

        sql += "ORDER BY i.created_at DESC";

        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                // FIX: Xử lý trường hợp ship_method bị null (do ID = 0 hoặc chưa có trong DB)
                String method = rs.getString("ship_method");
                if (method == null || method.isEmpty()) {
                    method = "Giao hàng tiêu chuẩn";
                }

                list.add(new ShippingOrder(
                        rs.getInt("invoice_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("shipping_address"),
                        rs.getDouble("total_amount"),
                        rs.getString("shipping_status"),
                        method, // Sử dụng biến đã xử lý null
                        rs.getTimestamp("created_at"),
                        rs.getString("tracking_number")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int invoiceId, String newStatus) {
        String sql = "UPDATE Invoice SET shipping_status = ? WHERE invoice_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, newStatus);
            pst.setInt(2, invoiceId);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTracking(int invoiceId, String tracking) {
        String sql = "UPDATE Invoice SET tracking_number = ? WHERE invoice_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tracking);
            pst.setInt(2, invoiceId);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}