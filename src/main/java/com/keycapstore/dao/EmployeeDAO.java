package com.keycapstore.dao;

import com.keycapstore.model.Employee;
import com.mycompany.mavenproject2.dao.BaseDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
    public List<Employee> getAll() {

    List<Employee> list = new ArrayList<>();
    String sql = "SELECT * FROM employees";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Employee emp = new Employee();

            emp.setEmployeeId(rs.getInt("employee_id"));
            emp.setUsername(rs.getString("username"));
            emp.setPassword(rs.getString("password"));
            emp.setFullName(rs.getString("full_name"));
            emp.setEmail(rs.getString("email"));
            emp.setPhone(rs.getString("phone"));
            emp.setRole(rs.getString("role"));
            emp.setStatus(rs.getString("status"));
            emp.setPinCode(rs.getString("pin_code"));

            list.add(emp);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}
    public Employee getById(int id){
    String sql = "SELECT * FROM employees WHERE employee_id = ?";
    try(Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){

        ps.setInt(1,id);
        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            Employee e = new Employee();
            e.setEmployeeId(rs.getInt("employee_id"));
            e.setFullName(rs.getString("full_name"));
            return e;
        }

    }catch(Exception ex){
        ex.printStackTrace();
    }
    return null;
}
}