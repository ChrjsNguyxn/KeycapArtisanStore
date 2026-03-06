/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.keycapstore.dao;

import com.keycapstore.model.MakerDTO;
import com.keycapstore.config.DBConnection;

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
    public boolean insert(MakerDTO m) {

    String sql = "INSERT INTO makers(name, origin, website) VALUES(?,?,?)";

    try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){

        ps.setString(1, m.getName());
        ps.setString(2, m.getOrigin());
        ps.setString(3, m.getWebsite());

        return ps.executeUpdate() > 0;

    }catch(Exception e){
        e.printStackTrace();
    }

    return false;
}
}
