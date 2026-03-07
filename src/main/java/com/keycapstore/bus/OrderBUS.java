package com.keycapstore.bus;

import java.util.ArrayList;
import java.util.List;

import com.keycapstore.dao.OrderDAO;
import com.keycapstore.dao.VoucherDAO;
import com.keycapstore.dto.OrderItem;
import com.keycapstore.dto.Voucher;

public class OrderBUS {

    private List<OrderItem> cart = new ArrayList<>();

    private Voucher voucher;

    private final OrderDAO orderDAO = new OrderDAO();
    private final VoucherDAO voucherDAO = new VoucherDAO();

    public void addProduct(int id, String name, double price) {

        for (OrderItem item : cart) {

            if (item.getProductId() == id) {

                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        cart.add(new OrderItem(id, name, price, 1));
    }

    public List<OrderItem> getCart() {
        return cart;
    }

    public double getTotal() {

        double total = 0;

        for (OrderItem item : cart) {
            total += item.getTotal();
        }

        if (voucher != null) {
            total = total - (total * voucher.getDiscountPercent() / 100);
        }

        return total;
    }

    public boolean applyVoucher(String code) {

        voucher = voucherDAO.findByCode(code);

        return voucher != null;
    }

    public void checkout(int customerId) {

        int voucherId = voucher == null ? 0 : voucher.getVoucherId();

        int orderId = orderDAO.createOrder(customerId, voucherId, getTotal());

        orderDAO.insertOrderItems(orderId, cart);

        cart.clear();
        voucher = null;
    }

    public void clearCart() {
        cart.clear();
    }
}