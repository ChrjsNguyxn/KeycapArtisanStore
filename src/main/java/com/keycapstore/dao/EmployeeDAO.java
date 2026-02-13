package com.keycapstore.dao;

import com.keycapstore.model.Employee;
import java.sql.*;

public class EmployeeDAO extends BaseDAO {

    // Kiem tra dang nhap
    public Employee checkLogin(String username, String password) {
        Employee emp = null;
        String sql = "SELECT * FROM employees WHERE username = ? AND password = ?";

        try (Connection conn = getConnection()) {
            if (conn == null)
                return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emp;
    }

    // Kiem tra ket noi
    public boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}