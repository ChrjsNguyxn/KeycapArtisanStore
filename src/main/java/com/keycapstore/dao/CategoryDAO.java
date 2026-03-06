/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.keycapstore.dao;

import com.keycapstore.model.CategoryDTO;
import com.keycapstore.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

   public List<CategoryDTO> getAll() {
    List<CategoryDTO> list = new ArrayList<>();
    String sql = "SELECT * FROM categories";

    try {
        Connection conn = DBConnection.getConnection();
        System.out.println("Connected: " + conn);

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new CategoryDTO(
                    rs.getInt("category_id"),
                    rs.getString("name"),
                    rs.getString("description")
            ));
        }

    } catch (Exception e) {
    System.out.println("LỖI: " + e.getMessage());
    e.printStackTrace();
}

    return list;
}
  public boolean insert(CategoryDTO c) {

    String sql = "INSERT INTO categories(name, description) VALUES(?, ?)";

    try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){

        ps.setString(1, c.getName());
        ps.setString(2, c.getDescription());

        return ps.executeUpdate() > 0;

    }catch(Exception e){
        e.printStackTrace();
    }

    return false;
}
}
