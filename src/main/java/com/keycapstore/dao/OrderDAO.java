package com.keycapstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.dto.OrderItem;

public class OrderDAO {

    public int createOrder(int customerId, int voucherId, double total) {

        String sql = """
                INSERT INTO orders(customer_id,voucher_id,shipping_method_id,total_amount,delivery_address)
                VALUES(?,?,?,?,?)
                """;

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {

            ps.setInt(1, customerId);

            if (voucherId == 0)
                ps.setNull(2, java.sql.Types.INTEGER);
            else
                ps.setInt(2, voucherId);

            ps.setInt(3, 1);
            ps.setDouble(4, total);
            ps.setString(5, "Default Address");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void insertOrderItems(int orderId, List<OrderItem> items) {

        String sql = """
                INSERT INTO order_items(order_id,product_id,quantity,price_at_purchase)
                VALUES(?,?,?,?)
                """;

        try (
                Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            for (OrderItem item : items) {

                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getPrice());

                ps.addBatch();
            }

            ps.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}