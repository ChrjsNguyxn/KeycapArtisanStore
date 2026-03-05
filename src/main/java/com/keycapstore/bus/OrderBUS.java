package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.dao.OrderDAO;
import com.keycapstore.dao.OrderItemDAO;
import com.keycapstore.dto.Order;
import com.keycapstore.dto.OrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderBUS {

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO itemDAO = new OrderItemDAO();
    private final List<OrderItem> cart = new ArrayList<>();

    public void createOrder(Order order, List<OrderItem> items) {
        Connection conn = null;
        try {
            conn = ConnectDB.getConnection();
            conn.setAutoCommit(false); // Start transaction

            int orderId = orderDAO.insert(order, conn);

            if (orderId == -1) {
                System.out.println("Create order failed");
                conn.rollback();
                return;
            }

            for (OrderItem item : items) {
                item.setOrderId(orderId);
                itemDAO.insert(item, conn);
            }

            conn.commit(); // Commit transaction
            System.out.println("Order created successfully");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Order creation failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Connection close failed: " + e.getMessage());
                }
            }
        }
    }

    public void addProduct(int productId, String productName, double price) {
        for (OrderItem item : cart) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + 1);
                item.setSubtotal(item.getQuantity() * price);
                return;
            }
        }
        cart.add(new OrderItem(productId, productName, 1, price));
    }

    public List<OrderItem> getCart() {
        return cart;
    }

    public double getTotal() {
        return cart.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }

    public void clearCart() {
        cart.clear();
    }
}