package com.keycapstore;

import com.keycapstore.bus.OrderBUS;
import com.keycapstore.dto.OrderItem;

/**
 * Simple test class to verify cart functionality without UI
 */
public class TestDebug {
    public static void main(String[] args) {
        System.out.println("=== Testing OrderBUS Cart Functionality ===");
        
        OrderBUS bus = new OrderBUS();
        
        // Test adding products
        System.out.println("\n1. Adding products to cart:");
        bus.addProduct(1, "Keycap Sakura", 250000);
        bus.addProduct(2, "Keycap PBT", 300000);
        bus.addProduct(1, "Keycap Sakura", 250000); // Duplicate - should increase quantity
        
        System.out.println("   Cart size: " + bus.getCart().size());
        for (OrderItem item : bus.getCart()) {
            System.out.println("   - " + item.getProductName() + 
                             ": Qty=" + item.getQuantity() + 
                             ", Price=" + item.getPrice() + 
                             ", Total=" + item.getTotal());
        }
        
        // Test total calculation
        System.out.println("\n2. Total calculation:");
        System.out.println("   Grand Total: " + bus.getTotal());
        
        // Test quantity update
        System.out.println("\n3. Updating first item quantity to 5:");
        OrderItem firstItem = bus.getCart().get(0);
        firstItem.setQuantity(5);
        System.out.println("   Item: " + firstItem.getProductName() + 
                         ", New Qty: " + firstItem.getQuantity() + 
                         ", New Total: " + firstItem.getTotal());
        System.out.println("   Grand Total: " + bus.getTotal());
        
        // Test voucher application
        System.out.println("\n4. Testing voucher (should return false for invalid code):");
        boolean voucherApplied = bus.applyVoucher("INVALID_CODE");
        System.out.println("   Voucher applied: " + voucherApplied);
        
        // Test clear cart
        System.out.println("\n5. Clearing cart:");
        bus.clearCart();
        System.out.println("   Cart size after clear: " + bus.getCart().size());
        
        System.out.println("\n=== Test Complete ===");
    }
}
