package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Employee;
import java.sql.*;

public class EmployeeBUS {

    public Employee login(String username, String password) {
        Employee emp = null;
        String sql = "SELECT * FROM Employee WHERE username = ? AND password = ? AND status = 'Active'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                emp = new Employee(
                        rs.getInt("emp_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getString("pin_code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emp;
    }
}