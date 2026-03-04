package com.keycapstore.dao;

import com.keycapstore.model.CategoryDTO;
import com.mycompany.mavenproject2.DBConnection;

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
}