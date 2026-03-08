package com.keycapstore;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;
import com.keycapstore.gui.ModernLoginDialog;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));

        // --- TẠO MÀN HÌNH CHỜ (LOADING SCREEN) ---
        final JFrame loadingFrame = new JFrame("Đang khởi động...");
        loadingFrame.setSize(350, 120);
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JLabel loadingLabel = new JLabel("Đang kiểm tra và thiết lập cơ sở dữ liệu...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingFrame.add(loadingLabel);
        loadingFrame.setVisible(true);

        // --- CHẠY TÁC VỤ NẶNG (DB) TRÊN LUỒNG NỀN ---
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Gọi hàm kiểm tra DB
                return initializeDatabase();
            }

            @Override
            protected void done() {
                loadingFrame.dispose(); // Tắt màn hình chờ
                try {
                    boolean success = get();
                    if (success) {
                        // Nếu thành công, mở màn hình đăng nhập
                        new ModernLoginDialog().setVisible(true);
                    } else {
                        // Nếu thất bại, hiển thị lỗi và thoát
                        JOptionPane.showMessageDialog(null,
                                "Không thể kết nối hoặc thiết lập cơ sở dữ liệu.\nVui lòng kiểm tra lại cấu hình.",
                                "Lỗi Khởi Động", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Lỗi không xác định trong quá trình khởi tạo: " + e.getMessage(),
                            "Lỗi Khởi Động", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        }.execute();
    }

    /**
     * Hàm này thực hiện tất cả các tác vụ kiểm tra và cập nhật CSDL.
     * Chạy trên luồng nền để không làm treo giao diện.
     * 
     * @return true nếu thành công, false nếu có lỗi.
     */
    private static boolean initializeDatabase() {
        try (java.sql.Connection con = com.keycapstore.config.ConnectDB.getConnection();
                java.sql.Statement st = con.createStatement()) {

            java.sql.ResultSet rs = st.executeQuery("SELECT count(*) FROM categories WHERE name = 'Switch'");
            if (rs.next() && rs.getInt(1) == 0) {
                st.executeUpdate(
                        "INSERT INTO categories (name, description) VALUES ('Switch', 'Mechanical Keyboard Switches')");
            }

            java.sql.DatabaseMetaData dbm = con.getMetaData();

            // TỰ ĐỘNG TẠO BẢNG makers (Hãng sản xuất) - Cần thiết cho ImportOrderGUI
            java.sql.ResultSet makerTable = dbm.getTables(null, null, "makers", null);
            if (!makerTable.next()) {
                String createMakerSql = "CREATE TABLE makers (" +
                        "maker_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "name NVARCHAR(100), " +
                        "origin NVARCHAR(100), " +
                        "website VARCHAR(255)" +
                        ")";
                st.executeUpdate(createMakerSql);
                st.executeUpdate("INSERT INTO makers (name, origin) VALUES ('Keychron', 'China')");
                System.out.println("✅ Đã tự động tạo bảng makers.");
            }

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

            // TỰ ĐỘNG THÊM CỘT maker_id VÀO BẢNG Product
            try {
                st.executeQuery("SELECT maker_id FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD maker_id INT DEFAULT 1");
                System.out.println("✅ Đã tự động thêm cột 'maker_id' vào bảng Product.");
            }

            // TỰ ĐỘNG THÊM CỘT description
            try {
                st.executeQuery("SELECT description FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD description NVARCHAR(MAX)");
                System.out.println("✅ Đã tự động thêm cột 'description' vào bảng Product.");
            }

            // TỰ ĐỘNG THÊM CỘT profile
            try {
                st.executeQuery("SELECT profile FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD profile NVARCHAR(50)");
                System.out.println("✅ Đã tự động thêm cột 'profile' vào bảng Product.");
            }

            // TỰ ĐỘNG THÊM CỘT material
            try {
                st.executeQuery("SELECT material FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD material NVARCHAR(50)");
                System.out.println("✅ Đã tự động thêm cột 'material' vào bảng Product.");
            }

            // TỰ ĐỘNG THÊM CỘT supplier_id VÀO BẢNG products
            try {
                st.executeQuery("SELECT supplier_id FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD supplier_id INT DEFAULT 1");
                System.out.println("✅ Đã tự động thêm cột 'supplier_id' vào bảng Product.");
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

            // 8. TỰ ĐỘNG TẠO BẢNG reviews (Đánh giá sản phẩm)
            java.sql.ResultSet reviewTable = dbm.getTables(null, null, "reviews", null);
            if (!reviewTable.next()) {
                String createReviewSql = "CREATE TABLE reviews (" +
                        "review_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "product_id INT, " +
                        "customer_id INT, " +
                        "rating INT CHECK (rating >= 1 AND rating <= 5), " +
                        "comment NVARCHAR(MAX), " +
                        "created_at DATETIME DEFAULT GETDATE(), " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id), " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
                        ")";
                st.executeUpdate(createReviewSql);
                System.out.println("✅ Đã tự động tạo bảng reviews.");
            }

            // 9. TỰ ĐỘNG TẠO BẢNG warranties (Bảo hành)
            java.sql.ResultSet warrantyTable = dbm.getTables(null, null, "warranties", null);
            if (!warrantyTable.next()) {
                String createWarrantySql = "CREATE TABLE warranties (" +
                        "warranty_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "invoice_id INT, " +
                        "product_id INT, " +
                        "end_date DATETIME, " +
                        "status NVARCHAR(50) DEFAULT 'Active', " + // Active, Expired, Voided
                        "FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id), " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id)" +
                        ")";
                st.executeUpdate(createWarrantySql);
                System.out.println("✅ Đã tự động tạo bảng warranties.");
            }

            // 10. TỰ ĐỘNG TẠO BẢNG suppliers (Nhà cung cấp) - MỚI
            java.sql.ResultSet supplierTable = dbm.getTables(null, null, "suppliers", null);
            if (!supplierTable.next()) {
                String createSupplierSql = "CREATE TABLE suppliers (" +
                        "supplier_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "name NVARCHAR(100) NOT NULL, " +
                        "phone VARCHAR(20), " +
                        "address NVARCHAR(255), " +
                        "email VARCHAR(100)" +
                        ")";
                st.executeUpdate(createSupplierSql);
                st.executeUpdate(
                        "INSERT INTO suppliers (name, phone, address) VALUES (N'Keychron VN', '0909123456', N'HCM')");
                st.executeUpdate(
                        "INSERT INTO suppliers (name, phone, address) VALUES (N'Akko Distributor', '0909999888', N'Hanoi')");
                System.out.println("✅ Đã tự động tạo bảng suppliers.");
            }

            // 11. TỰ ĐỘNG TẠO BẢNG import_orders (Đơn nhập hàng chi tiết) - MỚI
            java.sql.ResultSet importTable = dbm.getTables(null, null, "import_orders", null);
            if (!importTable.next()) {
                String createImportSql = "CREATE TABLE import_orders (" +
                        "import_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "supplier_id INT, " +
                        "employee_id INT, " +
                        "import_date DATETIME DEFAULT GETDATE(), " +
                        "total_cost FLOAT, " +
                        "note NVARCHAR(MAX), " +
                        "FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id), " +
                        "FOREIGN KEY (employee_id) REFERENCES employees(employee_id)" +
                        ")";
                st.executeUpdate(createImportSql);
                System.out.println("✅ Đã tự động tạo bảng import_orders.");
            }

            // 12. TỰ ĐỘNG TẠO BẢNG import_order_items VÀ SỬA LỖI FK
            java.sql.ResultSet importItemTable = dbm.getTables(null, null, "import_order_items", null);
            if (!importItemTable.next()) {
                String createImportItemSql = "CREATE TABLE import_order_items (" +
                        "import_item_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "import_id INT, " +
                        "product_id INT, " +
                        "quantity INT, " +
                        "import_price FLOAT, " +
                        "FOREIGN KEY (import_id) REFERENCES import_orders(import_id), " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id)" + // Trỏ đúng về Product
                        ")";
                st.executeUpdate(createImportItemSql);
                System.out.println("✅ Đã tự động tạo bảng import_order_items.");
            } else {
                // FIX LỖI SSMS: Kiểm tra nếu FK đang trỏ sai đến bảng 'products' (cũ) thì sửa
                // lại
                try {
                    String findFkSql = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('import_order_items') AND referenced_object_id = OBJECT_ID('products')";
                    java.sql.ResultSet rsFk = st.executeQuery(findFkSql);
                    if (rsFk.next()) {
                        String fkName = rsFk.getString("name");
                        System.out.println(
                                "⚠️ Phát hiện FK sai (" + fkName + ") trỏ đến 'products'. Đang tự động sửa...");
                        st.executeUpdate("ALTER TABLE import_order_items DROP CONSTRAINT " + fkName);
                        st.executeUpdate(
                                "ALTER TABLE import_order_items ADD CONSTRAINT FK_ImportItem_Product FOREIGN KEY (product_id) REFERENCES Product(product_id)");
                        System.out.println("✅ Đã sửa FK import_order_items trỏ về bảng Product.");
                    }
                } catch (Exception ex) {
                    /* Bỏ qua nếu không tìm thấy lỗi */ }
            }

            // 13. TỰ ĐỘNG TẠO BẢNG product_images (Lưu nhiều ảnh)
            java.sql.ResultSet imgTable = dbm.getTables(null, null, "product_images", null);
            if (!imgTable.next()) {
                String createImgSql = "CREATE TABLE product_images (" +
                        "image_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "product_id INT, " +
                        "image_path NVARCHAR(255), " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE" +
                        ")";
                st.executeUpdate(createImgSql);
                System.out.println("✅ Đã tự động tạo bảng product_images.");
            } else {
                // FIX LỖI FK: Kiểm tra và sửa FK nếu trỏ sai bảng 'products' (số nhiều)
                try {
                    java.util.List<String> badFks = new java.util.ArrayList<>();
                    String findFkSql = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('product_images') AND referenced_object_id = OBJECT_ID('products')";
                    try (java.sql.ResultSet rsFk = st.executeQuery(findFkSql)) {
                        while (rsFk.next()) {
                            badFks.add(rsFk.getString("name"));
                        }
                    }

                    for (String fkName : badFks) {
                        System.out.println(
                                "⚠️ Phát hiện FK sai (" + fkName + ") trỏ đến 'products'. Đang tự động xóa...");
                        st.executeUpdate("ALTER TABLE product_images DROP CONSTRAINT " + fkName);
                    }

                    // Kiểm tra xem đã có FK trỏ đúng vào 'Product' chưa
                    String checkCorrectFk = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('product_images') AND referenced_object_id = OBJECT_ID('Product')";
                    if (!st.executeQuery(checkCorrectFk).next()) {
                        st.executeUpdate(
                                "ALTER TABLE product_images ADD CONSTRAINT FK_ProductImages_Product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE");
                        System.out.println("✅ Đã sửa FK product_images trỏ về bảng Product.");
                    }
                } catch (Exception ex) {
                    // Bỏ qua nếu không tìm thấy lỗi
                }
            }

            // FIX LỖI: Tự động thêm cột image_path nếu bảng đã tồn tại nhưng thiếu cột
            try {
                st.executeQuery("SELECT image_path FROM product_images WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE product_images ADD image_path NVARCHAR(255)");
                System.out.println("✅ Đã tự động thêm cột 'image_path' vào bảng product_images.");
            }

            // FIX LỖI: Xử lý cột 'image_url' cũ gây lỗi NOT NULL
            try {
                st.executeQuery("SELECT image_url FROM product_images WHERE 1=0");
                // Nếu cột tồn tại, thử xóa nó đi vì không dùng nữa
                try {
                    st.executeUpdate("ALTER TABLE product_images DROP COLUMN image_url");
                    System.out.println("✅ Đã xóa cột thừa 'image_url' khỏi bảng product_images.");
                } catch (Exception eDrop) {
                    // Nếu không xóa được (do ràng buộc), thì sửa thành cho phép NULL
                    st.executeUpdate("ALTER TABLE product_images ALTER COLUMN image_url NVARCHAR(255) NULL");
                    System.out.println("✅ Đã sửa cột 'image_url' thành cho phép NULL.");
                }
            } catch (Exception ex) {
                // Bỏ qua nếu không có cột image_url
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Trả về false nếu có lỗi để luồng chính xử lý
            return false;
        }
        // Trả về true nếu mọi thứ OK
        return true;
    }
}