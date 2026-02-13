package com.keycapstore.dao;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Employee;
import java.sql.*;

public class EmployeeDAO {

    // Kiem tra dang nhap
    public Employee checkLogin(String username, String password) {
        Employee emp = null;
        String sql = "SELECT * FROM Employees WHERE username = ? AND password = ?";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                emp = new Employee();
                emp.setEmployeeId(rs.getInt("employee_id"));
                emp.setUsername(rs.getString("username"));
                emp.setFullName(rs.getString("full_name"));
                emp.setPhone(rs.getString("phone"));
                emp.setRole(rs.getString("role"));
                emp.setStatus(rs.getString("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emp;
    }
}