package com.keycapstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.dto.Voucher;

public class VoucherDAO {

    public Voucher findByCode(String code) {

        String sql = "SELECT * FROM vouchers WHERE code = ?";

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, code);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Voucher(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getDouble("discount_percent")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}