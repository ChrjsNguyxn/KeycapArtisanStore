package com.keycapstore.dao;

import com.keycapstore.model.MakerDTO;
import com.mycompany.mavenproject2.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MakerDAO {

    public List<MakerDTO> getAll() {
        List<MakerDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM makers";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new MakerDTO(
                        rs.getInt("maker_id"),
                        rs.getString("name"),
                        rs.getString("origin"),
                        rs.getString("website")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}