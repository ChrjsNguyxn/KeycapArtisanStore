package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Invoice;
import com.keycapstore.model.InvoiceDetail;
import java.sql.*;
import java.util.ArrayList;

public class InvoiceBUS {
    public ArrayList<Invoice> getAllInvoices() {
        ArrayList<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.invoice_id, e.full_name, i.customer_name, i.customer_phone, i.created_at, i.total_amount "
                +
                "FROM Invoice i " +
                "LEFT JOIN employees e ON i.emp_id = e.employee_id " +
                "ORDER BY i.created_at DESC";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String cusName = rs.getString("customer_name");
                if (cusName == null)
                    cusName = "Khách lẻ";

                list.add(new Invoice(
                        rs.getInt("invoice_id"),
                        rs.getString("full_name"),
                        cusName,
                        rs.getString("customer_phone"),
                        rs.getTimestamp("created_at"),
                        rs.getDouble("total_amount")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<InvoiceDetail> getInvoiceDetails(int invoiceId) {
        ArrayList<InvoiceDetail> list = new ArrayList<>();

        String sql = "SELECT p.name, c.name AS category_name, d.quantity, d.price, d.product_id " +
                "FROM InvoiceDetail d " +
                "LEFT JOIN Product p ON d.product_id = p.product_id " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "WHERE d.invoice_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, invoiceId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                InvoiceDetail detail = new InvoiceDetail();

                String pName = rs.getString("name");
                if (pName == null)
                    pName = "Sản phẩm #" + rs.getInt("product_id") + " (Đã xóa)";
                detail.setProductName(pName);

                detail.setCategoryName(rs.getString("category_name") != null ? rs.getString("category_name") : "-");
                detail.setQuantity(rs.getInt("quantity"));
                detail.setPrice(rs.getDouble("price"));
                detail.setProductId(rs.getInt("product_id"));
                list.add(detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.HashMap<Integer, Object[]> getInvoiceSupplements() {
        java.util.HashMap<Integer, Object[]> map = new java.util.HashMap<>();

        String sql = "SELECT i.invoice_id, i.customer_id, c.rank_id, r.name as rank_name, " +
                "(SELECT SUM(d.quantity * d.price) FROM InvoiceDetail d WHERE d.invoice_id = i.invoice_id) as raw_total, "
                +
                "i.total_amount " +
                "FROM Invoice i " +
                "LEFT JOIN customers c ON i.customer_id = c.customer_id " +
                "LEFT JOIN customer_ranks r ON c.rank_id = r.rank_id";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("invoice_id");
                int cusId = rs.getInt("customer_id");
                int rankId = rs.getInt("rank_id");
                String rank = rs.getString("rank_name");

                if (cusId == 0) {

                    rank = "Không có";
                } else if (rankId == 1) {

                    rank = "Thành Viên";
                } else if (rank == null || rank.isEmpty()) {
                    rank = "Thành Viên";
                }

                double raw = rs.getDouble("raw_total");
                double finalTotal = rs.getDouble("total_amount");
                double discount = raw - finalTotal;

                if (discount < 0)
                    discount = 0;

                map.put(id, new Object[] { rank, discount });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}