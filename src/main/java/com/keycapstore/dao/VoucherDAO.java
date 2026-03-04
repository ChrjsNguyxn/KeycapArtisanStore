package com.keycapstore.dao;
import com.keycapstore.dto.Voucher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO extends BaseDAO {

    public Voucher getByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE code=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Voucher(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getDouble("discount_percent"),
                        rs.getDouble("max_discount"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("is_active")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Voucher> getAll() {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT * FROM vouchers";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Voucher v = new Voucher(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getDouble("discount_percent"),
                        rs.getDouble("max_discount"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("is_active")
                );
                list.add(v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}