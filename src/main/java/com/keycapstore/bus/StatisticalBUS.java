package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import java.sql.*;
import java.time.LocalDate;

public class StatisticalBUS {

    public double getTodayRevenue() {
        double total = 0;

        String sql = "SELECT SUM(total_amount) FROM Invoice WHERE CAST(created_at AS DATE) = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public int getTodayOrderCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM Invoice WHERE CAST(created_at AS DATE) = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getActiveEmployeeCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM employees WHERE status = 'active'";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next())
                count = rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getLowStockCount() {
        int count = 0;

        String sql = "SELECT COUNT(*) FROM Product WHERE stock <= 5 AND status = 'Active'";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next())
                count = rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public java.util.List<Object[]> getRevenueLast7Days() {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        String sql = "SELECT CAST(created_at AS DATE) as date, SUM(total_amount) as total " +
                "FROM Invoice " +
                "WHERE created_at >= DATEADD(day, -7, GETDATE()) " +
                "GROUP BY CAST(created_at AS DATE) " +
                "ORDER BY date ASC";

        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new Object[] {
                        rs.getDate("date"),
                        rs.getDouble("total")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}