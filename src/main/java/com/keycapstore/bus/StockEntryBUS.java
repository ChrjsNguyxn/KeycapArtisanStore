package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.StockEntryRecord;
import java.sql.*;
import java.util.ArrayList;

public class StockEntryBUS {
    public ArrayList<StockEntryRecord> getStockEntryHistory() {
        ArrayList<StockEntryRecord> list = new ArrayList<>();
        String sql = "SELECT se.entry_id, p.name AS product_name, e.full_name AS employee_name, " +
                "se.quantity_added, se.entry_price, se.entry_date, se.note " +
                "FROM StockEntry se " +
                "JOIN Product p ON se.product_id = p.product_id " +
                "JOIN employees e ON se.employee_id = e.employee_id " +
                "ORDER BY se.entry_date DESC";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new StockEntryRecord(
                        rs.getInt("entry_id"),
                        rs.getString("product_name"),
                        rs.getString("employee_name"),
                        rs.getInt("quantity_added"),
                        rs.getDouble("entry_price"),
                        rs.getTimestamp("entry_date"),
                        rs.getString("note")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}