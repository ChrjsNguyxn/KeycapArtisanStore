package com.keycapstore;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;
import com.keycapstore.gui.ModernLoginDialog;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));

        try (java.sql.Connection con = com.keycapstore.config.ConnectDB.getConnection();
                java.sql.Statement st = con.createStatement()) {

            java.sql.ResultSet rs = st.executeQuery("SELECT count(*) FROM categories WHERE name = 'Switch'");
            if (rs.next() && rs.getInt(1) == 0) {
                st.executeUpdate(
                        "INSERT INTO categories (name, description) VALUES ('Switch', 'Mechanical Keyboard Switches')");
            }

            java.sql.DatabaseMetaData dbm = con.getMetaData();
            java.sql.ResultSet tables = dbm.getTables(null, null, "StockEntry", null);
            if (!tables.next()) {

                String createTableSql = "CREATE TABLE StockEntry (" +
                        "entry_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "product_id INT NOT NULL, " +
                        "employee_id INT NOT NULL, " +
                        "quantity_added INT NOT NULL, " +
                        "entry_price FLOAT, " +
                        "entry_date DATETIME DEFAULT GETDATE(), " +
                        "note NVARCHAR(MAX), " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id), " +
                        "FOREIGN KEY (employee_id) REFERENCES employees(employee_id)" +
                        ")";
                st.executeUpdate(createTableSql);
                System.out.println("✅ Đã tự động tạo bảng StockEntry.");
            } else {

                try {
                    st.executeQuery("SELECT note FROM StockEntry WHERE 1=0");
                } catch (Exception ex) {

                    st.executeUpdate("ALTER TABLE StockEntry ADD note NVARCHAR(MAX)");
                    System.out.println("✅ Đã tự động thêm cột 'note' vào bảng StockEntry.");
                }
            }

            try {
                st.executeQuery("SELECT origin FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD origin NVARCHAR(100)");
                System.out.println("✅ Đã tự động thêm cột 'origin' vào bảng Product.");
            }

            java.sql.ResultSet rankTable = dbm.getTables(null, null, "customer_ranks", null);
            if (!rankTable.next()) {
                String createRankSql = "CREATE TABLE customer_ranks (" +
                        "rank_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "name NVARCHAR(50) NOT NULL, " +
                        "min_spending FLOAT DEFAULT 0, " +
                        "discount_percent FLOAT DEFAULT 0" +
                        ")";
                st.executeUpdate(createRankSql);

                // Thêm dữ liệu mẫu cho Rank
                st.executeUpdate(
                        "INSERT INTO customer_ranks (name, min_spending, discount_percent) VALUES (N'Thành Viên', 0, 0)");
                st.executeUpdate(
                        "INSERT INTO customer_ranks (name, min_spending, discount_percent) VALUES (N'Bạc', 5000000, 2)");
                st.executeUpdate(
                        "INSERT INTO customer_ranks (name, min_spending, discount_percent) VALUES (N'Vàng', 20000000, 5)");
                st.executeUpdate(
                        "INSERT INTO customer_ranks (name, min_spending, discount_percent) VALUES (N'Kim Cương', 50000000, 10)");

                System.out.println("✅ Đã tự động tạo bảng customer_ranks và dữ liệu mẫu.");
            }

            // Kiểm tra và thêm cột rank_id, total_spending vào bảng customers
            try {
                st.executeQuery("SELECT rank_id, total_spending FROM customers WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE customers ADD rank_id INT DEFAULT 1");
                st.executeUpdate("ALTER TABLE customers ADD total_spending FLOAT DEFAULT 0");
                st.executeUpdate(
                        "ALTER TABLE customers ADD CONSTRAINT FK_Customer_Rank FOREIGN KEY (rank_id) REFERENCES customer_ranks(rank_id)");
                System.out.println("✅ Đã tự động thêm cột rank_id và total_spending vào bảng customers.");
            }

            // Fix lỗi dữ liệu cũ: Cập nhật rank_id = 1 cho các khách hàng bị NULL để tránh
            // lỗi hiển thị
            st.executeUpdate("UPDATE customers SET rank_id = 1 WHERE rank_id IS NULL");

            // 5. TỰ ĐỘNG TẠO BẢNG vouchers
            java.sql.ResultSet voucherTable = dbm.getTables(null, null, "vouchers", null);
            if (!voucherTable.next()) {
                String createVoucherSql = "CREATE TABLE vouchers (" +
                        "voucher_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "code VARCHAR(20) UNIQUE NOT NULL, " +
                        "discount_percent FLOAT DEFAULT 0, " +
                        "quantity INT DEFAULT 0, " +
                        "start_date DATETIME DEFAULT GETDATE(), " +
                        "expired_date DATETIME" +
                        ")";
                st.executeUpdate(createVoucherSql);

                // Thêm dữ liệu mẫu Voucher
                st.executeUpdate(
                        "INSERT INTO vouchers (code, discount_percent, quantity, expired_date) VALUES ('SALE10', 10, 100, DATEADD(day, 30, GETDATE()))");
                st.executeUpdate(
                        "INSERT INTO vouchers (code, discount_percent, quantity, expired_date) VALUES ('SALE20', 20, 50, DATEADD(day, 30, GETDATE()))");
                st.executeUpdate(
                        "INSERT INTO vouchers (code, discount_percent, quantity, expired_date) VALUES ('FREESHIP', 0, 50, DATEADD(day, 30, GETDATE()))");

                System.out.println("✅ Đã tự động tạo bảng vouchers.");
            }

            // 6. TỰ ĐỘNG TẠO BẢNG shipping_methods
            java.sql.ResultSet shipTable = dbm.getTables(null, null, "shipping_methods", null);
            if (!shipTable.next()) {
                String createShipSql = "CREATE TABLE shipping_methods (" +
                        "shipping_method_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "name NVARCHAR(100), " +
                        "price FLOAT DEFAULT 0" +
                        ")";
                st.executeUpdate(createShipSql);

                // Thêm dữ liệu mẫu Shipping
                st.executeUpdate("INSERT INTO shipping_methods (name, price) VALUES (N'Nhận tại cửa hàng', 0)");
                st.executeUpdate("INSERT INTO shipping_methods (name, price) VALUES (N'Giao hàng tiêu chuẩn', 30000)");
                st.executeUpdate("INSERT INTO shipping_methods (name, price) VALUES (N'Giao hàng hỏa tốc', 50000)");

                System.out.println("✅ Đã tự động tạo bảng shipping_methods.");
            }

            // 7. TỰ ĐỘNG TẠO BẢNG payments (Lịch sử thanh toán)
            java.sql.ResultSet payTable = dbm.getTables(null, null, "payments", null);
            if (!payTable.next()) {
                String createPaySql = "CREATE TABLE payments (" +
                        "payment_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "invoice_id INT, " +
                        "payment_method NVARCHAR(50), " +
                        "payment_date DATETIME DEFAULT GETDATE(), " +
                        "FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id)" +
                        ")";
                st.executeUpdate(createPaySql);
                System.out.println("✅ Đã tự động tạo bảng payments.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {

            new ModernLoginDialog().setVisible(true);
        });
    }
}