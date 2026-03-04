package com.keycapstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.keycapstore.dto.OrderItem;

public class OrderItemDAO {

    public void insert(OrderItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getSubtotal());

            ps.executeUpdate();
        }
    }
}