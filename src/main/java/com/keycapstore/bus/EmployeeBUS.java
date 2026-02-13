package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Employee;
import java.sql.*;
import java.util.ArrayList;

public class EmployeeBUS {

    public Employee login(String user, String pass) {
        Employee emp = null;
        // Note: 'pin_code' is assumed to exist in DB based on Model requirements
        String sql = "SELECT * FROM employees WHERE username = ? AND password = ? AND status = 'active'";

        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, user);
            pst.setString(2, pass);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                emp = new Employee(
                        rs.getInt("employee_id"),
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

    public ArrayList<Employee> getAllEmployees() {
        ArrayList<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE status != 'quit'";
        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getString("pin_code")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkDuplicate(String user) {
        String sql = "SELECT username FROM employees WHERE username = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, user);
            return pst.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees(username, password, full_name, email, phone, role, status, pin_code) VALUES (?, ?, ?, ?, ?, ?, 'active', ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, emp.getUsername());
            pst.setString(2, emp.getPassword());
            pst.setString(3, emp.getFullName());
            pst.setString(4, emp.getEmail());
            pst.setString(5, emp.getPhone());
            pst.setString(6, emp.getRole());
            pst.setString(7, emp.getPinCode());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteEmployee(int id) {
        String sql = "UPDATE employees SET status = 'quit' WHERE employee_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET full_name=?, email=?, phone=?, role=?, status=?, pin_code=? WHERE employee_id=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, emp.getFullName());
            pst.setString(2, emp.getEmail());
            pst.setString(3, emp.getPhone());
            pst.setString(4, emp.getRole());
            pst.setString(5, emp.getStatus());
            pst.setString(6, emp.getPinCode());
            pst.setInt(7, emp.getEmployeeId());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}