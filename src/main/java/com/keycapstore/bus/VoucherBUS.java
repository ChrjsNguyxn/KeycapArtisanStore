package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Voucher;
import java.sql.*;
import java.util.ArrayList;

public class VoucherBUS {

    public ArrayList<Voucher> getAllVouchers() {
        ArrayList<Voucher> list = new ArrayList<>();
        String sql = "SELECT * FROM vouchers";
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new Voucher(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getDouble("discount_percent"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("start_date"),
                        rs.getTimestamp("expired_date")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addVoucher(Voucher v, java.util.Date startDate, java.util.Date expiredDate) {
        String sql = "INSERT INTO vouchers (code, discount_percent, quantity, start_date, expired_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, v.getCode());
            pst.setDouble(2, v.getDiscountPercent());
            pst.setInt(3, v.getQuantity());
            pst.setTimestamp(4, new java.sql.Timestamp(startDate.getTime()));
            pst.setTimestamp(5, new java.sql.Timestamp(expiredDate.getTime()));
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateVoucher(Voucher v, java.util.Date startDate, java.util.Date expiredDate) {
        String sql = "UPDATE vouchers SET code=?, discount_percent=?, quantity=?, start_date=?, expired_date=? WHERE voucher_id=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, v.getCode());
            pst.setDouble(2, v.getDiscountPercent());
            pst.setInt(3, v.getQuantity());
            pst.setTimestamp(4, new java.sql.Timestamp(startDate.getTime()));
            pst.setTimestamp(5, new java.sql.Timestamp(expiredDate.getTime()));
            pst.setInt(6, v.getId());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteVoucher(int id) {
        String sql = "DELETE FROM vouchers WHERE voucher_id=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}