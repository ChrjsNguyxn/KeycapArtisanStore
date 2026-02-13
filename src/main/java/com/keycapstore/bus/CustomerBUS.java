package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Customer;
import java.sql.*;

public class CustomerBUS {

    public Customer login(String user, String pass) {
        Customer cus = null;
        String sql = "SELECT * FROM customers WHERE username = ? AND password = ? AND status = 'active'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, user);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                cus = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getString("status") != null ? rs.getString("status") : "active");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cus;
    }

    public boolean register(Customer cus) {
        String sql = "INSERT INTO customers(username, password, full_name, email, phone_number, address, status) VALUES (?, ?, ?, ?, ?, ?, 'active')";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cus.getUsername());
            pst.setString(2, cus.getPassword());
            pst.setString(3, cus.getFullName());
            pst.setString(4, cus.getEmail());
            pst.setString(5, cus.getPhone());
            pst.setString(6, cus.getAddress());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String checkDuplicate(String user, String email, String phone) {
        try (Connection con = ConnectDB.getConnection()) {

            PreparedStatement p1 = con.prepareStatement("SELECT username FROM customers WHERE username = ?");
            p1.setString(1, user);
            if (p1.executeQuery().next())
                return "Tên đăng nhập đã tồn tại!";

            PreparedStatement p2 = con.prepareStatement("SELECT email FROM customers WHERE email = ?");
            p2.setString(1, email);
            if (p2.executeQuery().next())
                return "Email này đã được sử dụng!";

            if (phone != null && !phone.trim().isEmpty()) {
                PreparedStatement p3 = con
                        .prepareStatement("SELECT phone_number FROM customers WHERE phone_number = ?");
                p3.setString(1, phone);
                if (p3.executeQuery().next())
                    return "Số điện thoại này đã được sử dụng!";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEmailExist(String email) {
        String sql = "SELECT email FROM customers WHERE email = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, email);
            return pst.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(String email, String newPass) {
        String sql = "UPDATE customers SET password = ? WHERE email = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, newPass);
            pst.setString(2, email);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}