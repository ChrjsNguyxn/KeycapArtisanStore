package com.keycapstore.dao;

import com.keycapstore.dto.Order;
import java.sql.*;

public class OrderDAO {

    public int insert(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO orders (employee_id, customer_id, total_amount, status, created_at) VALUES (?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, order.getEmployeeId());
            ps.setInt(2, order.getCustomerId());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        throw new SQLException("Cannot insert order.");
    }
}