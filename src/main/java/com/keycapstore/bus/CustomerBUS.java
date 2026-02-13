package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Customer;
import java.sql.*;

public class CustomerBUS {

    public Customer login(String user, String pass) {
        Customer cus = null;
        String sql = "SELECT * FROM Customer WHERE username = ? AND password = ? AND status = 'Active'";
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
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cus;
    }

    public boolean register(Customer cus) {
        String sql = "INSERT INTO Customer(username, password, full_name, email, phone, address, status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cus.getUsername());
            pst.setString(2, cus.getPassword());
            pst.setString(3, cus.getFullname());
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

            PreparedStatement p1 = con.prepareStatement("SELECT username FROM Customer WHERE username = ?");
            p1.setString(1, user);
            if (p1.executeQuery().next())
                return "Tên đăng nhập đã tồn tại!";

            PreparedStatement p2 = con.prepareStatement("SELECT email FROM Customer WHERE email = ?");
            p2.setString(1, email);
            if (p2.executeQuery().next())
                return "Email này đã được sử dụng!";

            if (phone != null && !phone.trim().isEmpty()) {
                PreparedStatement p3 = con.prepareStatement("SELECT phone FROM Customer WHERE phone = ?");
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
        String sql = "SELECT email FROM Customer WHERE email = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, email);
            return pst.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(String email, String newPass) {
        String sql = "UPDATE Customer SET password = ? WHERE email = ?";
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