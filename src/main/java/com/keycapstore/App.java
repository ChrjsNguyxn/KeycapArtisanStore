package com.keycapstore;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import com.keycapstore.gui.MainFrame;
import com.keycapstore.gui.ModernLoginDialog;
import com.keycapstore.gui.MuaHangPanel;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {

        }
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));

        final JFrame loadingFrame = new JFrame("Đang khởi động...");
        loadingFrame.setSize(350, 120);
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JLabel loadingLabel = new JLabel("Đang kiểm tra và thiết lập cơ sở dữ liệu...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingFrame.add(loadingLabel);
        loadingFrame.setVisible(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {

                return initializeDatabase();
            }

            @Override
            protected void done() {
                loadingFrame.dispose();
                try {
                    boolean success = get();
                    if (success) {

                        new ModernLoginDialog().setVisible(true);
                    } else {

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

    private static boolean initializeDatabase() {
        try (java.sql.Connection con = com.keycapstore.config.ConnectDB.getConnection();
                java.sql.Statement st = con.createStatement()) {

            java.sql.ResultSet rs = st.executeQuery("SELECT count(*) FROM categories WHERE name = 'Switch'");
            if (rs.next() && rs.getInt(1) == 0) {
                st.executeUpdate(
                        "INSERT INTO categories (name, description) VALUES ('Switch', 'Mechanical Keyboard Switches')");
            }

            java.sql.DatabaseMetaData dbm = con.getMetaData();

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

            try {
                st.executeQuery("SELECT is_featured FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD is_featured BIT DEFAULT 0");
                System.out.println("✅ Đã tự động thêm cột 'is_featured' vào bảng Product.");
            }

            try {
                st.executeQuery("SELECT maker_id FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD maker_id INT DEFAULT 1");
                System.out.println("✅ Đã tự động thêm cột 'maker_id' vào bảng Product.");
            }

            try {
                st.executeQuery("SELECT description FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD description NVARCHAR(MAX)");
                System.out.println("✅ Đã tự động thêm cột 'description' vào bảng Product.");
            }

            try {
                st.executeQuery("SELECT profile FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD profile NVARCHAR(50)");
                System.out.println("✅ Đã tự động thêm cột 'profile' vào bảng Product.");
            }

            try {
                st.executeQuery("SELECT material FROM Product WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE Product ADD material NVARCHAR(50)");
                System.out.println("✅ Đã tự động thêm cột 'material' vào bảng Product.");
            }

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

            try {
                st.executeQuery("SELECT rank_id, total_spending FROM customers WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE customers ADD rank_id INT DEFAULT 1");
                st.executeUpdate("ALTER TABLE customers ADD total_spending FLOAT DEFAULT 0");
                st.executeUpdate(
                        "ALTER TABLE customers ADD CONSTRAINT FK_Customer_Rank FOREIGN KEY (rank_id) REFERENCES customer_ranks(rank_id)");
                System.out.println("✅ Đã tự động thêm cột rank_id và total_spending vào bảng customers.");
            }

            st.executeUpdate("UPDATE customers SET rank_id = 1 WHERE rank_id IS NULL");

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

                st.executeUpdate(
                        "INSERT INTO vouchers (code, discount_percent, quantity, expired_date) VALUES ('SALE10', 10, 100, DATEADD(day, 30, GETDATE()))");
                st.executeUpdate(
                        "INSERT INTO vouchers (code, discount_percent, quantity, expired_date) VALUES ('SALE20', 20, 50, DATEADD(day, 30, GETDATE()))");
                st.executeUpdate(
                        "INSERT INTO vouchers (code, discount_percent, quantity, expired_date) VALUES ('FREESHIP', 0, 50, DATEADD(day, 30, GETDATE()))");

                System.out.println("✅ Đã tự động tạo bảng vouchers.");
            }

            java.sql.ResultSet shipTable = dbm.getTables(null, null, "shipping_methods", null);
            if (!shipTable.next()) {
                String createShipSql = "CREATE TABLE shipping_methods (" +
                        "shipping_method_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "name NVARCHAR(100), " +
                        "price FLOAT DEFAULT 0" +
                        ")";
                st.executeUpdate(createShipSql);

                st.executeUpdate("INSERT INTO shipping_methods (name, price) VALUES (N'Nhận tại cửa hàng', 0)");
                st.executeUpdate("INSERT INTO shipping_methods (name, price) VALUES (N'Giao hàng tiêu chuẩn', 30000)");
                st.executeUpdate("INSERT INTO shipping_methods (name, price) VALUES (N'Giao hàng hỏa tốc', 50000)");

                System.out.println("✅ Đã tự động tạo bảng shipping_methods.");
            }

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
            } else {

                try {
                    java.util.List<String> badFks = new java.util.ArrayList<>();
                    String findFkSql = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('reviews') AND referenced_object_id = OBJECT_ID('products')";
                    try (java.sql.ResultSet rsFk = st.executeQuery(findFkSql)) {
                        while (rsFk.next()) {
                            badFks.add(rsFk.getString("name"));
                        }
                    }

                    for (String fkName : badFks) {
                        System.out.println(
                                "⚠️ Phát hiện FK sai (" + fkName
                                        + ") trỏ đến 'products' trong bảng 'reviews'. Đang tự động xóa...");
                        st.executeUpdate("ALTER TABLE reviews DROP CONSTRAINT " + fkName);
                    }

                    String checkCorrectFk = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('reviews') AND referenced_object_id = OBJECT_ID('Product')";
                    if (!st.executeQuery(checkCorrectFk).next()) {
                        st.executeUpdate(
                                "ALTER TABLE reviews ADD CONSTRAINT FK_Reviews_Product FOREIGN KEY (product_id) REFERENCES Product(product_id)");
                        System.out.println("✅ Đã sửa FK reviews trỏ về bảng Product.");
                    }
                } catch (Exception ex) {

                }
            }

            java.sql.ResultSet warrantyTable = dbm.getTables(null, null, "warranties", null);
            if (!warrantyTable.next()) {
                String createWarrantySql = "CREATE TABLE warranties (" +
                        "warranty_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "invoice_id INT, " +
                        "product_id INT, " +
                        "customer_id INT, " +
                        "employee_id INT, " +
                        "issue NVARCHAR(MAX), " +
                        "end_date DATETIME, " +
                        "created_at DATETIME DEFAULT GETDATE(), " +
                        "resolution_note NVARCHAR(MAX), " +
                        "status NVARCHAR(50) DEFAULT 'pending', " +
                        "FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id), " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id)" +
                        ")";
                st.executeUpdate(createWarrantySql);
                System.out.println("✅ Đã tự động tạo bảng warranties.");
            } else {
                // Kiểm tra và thêm từng cột một cách an toàn
                try {
                    st.executeQuery("SELECT customer_id FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD customer_id INT");
                }
                try {
                    st.executeQuery("SELECT employee_id FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD employee_id INT");
                }
                try {
                    st.executeQuery("SELECT issue FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD issue NVARCHAR(MAX)");
                }
                try {
                    st.executeQuery("SELECT created_at FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD created_at DATETIME DEFAULT GETDATE()");
                }
                try {
                    st.executeQuery("SELECT resolution_note FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD resolution_note NVARCHAR(MAX)");
                }
                try {
                    st.executeQuery("SELECT product_id FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD product_id INT");
                }
                try {
                    st.executeQuery("SELECT invoice_id FROM warranties WHERE 1=0");
                } catch (Exception ex) {
                    st.executeUpdate("ALTER TABLE warranties ADD invoice_id INT");
                }

                // FIX BUGS LOGIC CŨ: Mở khóa cho phép các cột cũ được rỗng (NULL) để không bị
                // lỗi INSERT
                try {
                    st.executeQuery("SELECT order_item_id FROM warranties WHERE 1=0");
                    st.executeUpdate("ALTER TABLE warranties ALTER COLUMN order_item_id INT NULL");
                } catch (Exception ex) {
                }

                // Mở khóa cho cột rác 'reason' (nếu có từ bản cũ)
                try {
                    st.executeQuery("SELECT reason FROM warranties WHERE 1=0");
                    st.executeUpdate("ALTER TABLE warranties ALTER COLUMN reason NVARCHAR(MAX) NULL");
                } catch (Exception ex) {
                }

                // Mở khóa thêm cho cột rác 'issue_description' (phòng hờ)
                try {
                    st.executeQuery("SELECT issue_description FROM warranties WHERE 1=0");
                    st.executeUpdate("ALTER TABLE warranties ALTER COLUMN issue_description NVARCHAR(MAX) NULL");
                } catch (Exception ex) {
                }

                // FIX BUGS LOGIC: Mở rộng cột 'status' để chứa đủ trạng thái "in_progress" (11
                // ký tự)
                // Tránh lỗi "String or binary data would be truncated"
                try {
                    st.executeUpdate("ALTER TABLE warranties ALTER COLUMN status NVARCHAR(50)");
                } catch (Exception ex) {
                }

                // FIX BUGS LOGIC: Xóa bỏ các CHECK constraint cũ (nếu có) trên bảng warranties
                // Để ngăn chặn lỗi: "The UPDATE statement conflicted with the CHECK constraint"
                try {
                    java.util.List<String> chks = new java.util.ArrayList<>();
                    String sqlChk = "SELECT name FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('warranties')";
                    try (java.sql.ResultSet rsChk = st.executeQuery(sqlChk)) {
                        while (rsChk.next()) {
                            chks.add(rsChk.getString("name"));
                        }
                    }
                    for (String chk : chks) {
                        st.executeUpdate("ALTER TABLE warranties DROP CONSTRAINT " + chk);
                        System.out.println("✅ Đã xóa CHECK constraint cũ trên bảng warranties: " + chk);
                    }
                } catch (Exception ex) {
                }

                System.out.println("✅ Đã kiểm tra và thêm các cột còn thiếu cho bảng warranties.");
            }

            // TẠO BẢNG THÔNG BÁO CHO KHÁCH HÀNG
            java.sql.ResultSet notifTable = dbm.getTables(null, null, "notifications", null);
            if (!notifTable.next()) {
                String createNotifSql = "CREATE TABLE notifications (" +
                        "notification_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "customer_id INT, " +
                        "title NVARCHAR(255), " +
                        "message NVARCHAR(MAX), " +
                        "is_read BIT DEFAULT 0, " +
                        "created_at DATETIME DEFAULT GETDATE(), " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE" +
                        ")";
                st.executeUpdate(createNotifSql);
                System.out.println("✅ Đã tự động tạo bảng notifications.");
            }

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
                        "INSERT INTO suppliers (name, phone, address) VALUES (N'Keychron', '0909123456', N'China')");
                st.executeUpdate(
                        "INSERT INTO suppliers (name, phone, address) VALUES (N'Akko', '0909999888', N'China')");
                System.out.println("✅ Đã tự động tạo bảng suppliers.");
            }

            // Tự động thêm các nhà phân phối nổi tiếng nếu chưa có
            String[] popularSuppliers = {
                    "INSERT INTO suppliers (name, phone, address, email) VALUES (N'Akko', '0909999888', N'China', 'support@akkogear.com')",
                    "INSERT INTO suppliers (name, phone, address, email) VALUES (N'Gateron', '0907654321', N'China', 'sales@gateron.com')",
                    "INSERT INTO suppliers (name, phone, address, email) VALUES (N'GMK', '0905554443', N'Germany', 'keycaps@gmk.de')",
                    "INSERT INTO suppliers (name, phone, address, email) VALUES (N'Drop', '0901112223', N'USA', 'support@drop.com')",
                    "INSERT INTO suppliers (name, phone, address, email) VALUES (N'KBDfans', '0909990001', N'China', 'kbdfans@gmail.com')",
                    "INSERT INTO suppliers (name, phone, address, email) VALUES (N'Dwarf Factory', '0901119999', N'Vietnam', 'hello@dwarffactory.com')"
            };
            for (String sql : popularSuppliers) {
                String name = sql.substring(sql.indexOf("N'") + 2, sql.indexOf("',"));
                java.sql.ResultSet rsSup = st
                        .executeQuery("SELECT count(*) FROM suppliers WHERE name = N'" + name + "'");
                if (rsSup.next() && rsSup.getInt(1) == 0) {
                    st.executeUpdate(sql);
                }
            }

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
                }
            }

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

                    String checkCorrectFk = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('product_images') AND referenced_object_id = OBJECT_ID('Product')";
                    if (!st.executeQuery(checkCorrectFk).next()) {
                        st.executeUpdate(
                                "ALTER TABLE product_images ADD CONSTRAINT FK_ProductImages_Product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE");
                        System.out.println("✅ Đã sửa FK product_images trỏ về bảng Product.");
                    }
                } catch (Exception ex) {

                }
            }

            try {
                st.executeQuery("SELECT image_path FROM product_images WHERE 1=0");
            } catch (Exception ex) {
                st.executeUpdate("ALTER TABLE product_images ADD image_path NVARCHAR(255)");
                System.out.println("✅ Đã tự động thêm cột 'image_path' vào bảng product_images.");
            }

            try {
                st.executeQuery("SELECT image_url FROM product_images WHERE 1=0");

                try {
                    st.executeUpdate("ALTER TABLE product_images DROP COLUMN image_url");
                    System.out.println("✅ Đã xóa cột thừa 'image_url' khỏi bảng product_images.");
                } catch (Exception eDrop) {

                    st.executeUpdate("ALTER TABLE product_images ALTER COLUMN image_url NVARCHAR(255) NULL");
                    System.out.println("✅ Đã sửa cột 'image_url' thành cho phép NULL.");
                }
            } catch (Exception ex) {

            }

            java.sql.ResultSet wishlistTable = dbm.getTables(null, null, "wishlists", null);
            if (!wishlistTable.next()) {
                String createWishlistSql = "CREATE TABLE wishlists (" +
                        "wishlist_id INT PRIMARY KEY IDENTITY(1,1), " +
                        "customer_id INT, " +
                        "product_id INT, " +
                        "created_at DATETIME DEFAULT GETDATE(), " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE" +
                        ")";
                st.executeUpdate(createWishlistSql);
                System.out.println("✅ Đã tự động tạo bảng wishlists.");
            } else {
                try {
                    java.util.List<String> badFks = new java.util.ArrayList<>();
                    String findFkSql = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('wishlists') AND referenced_object_id = OBJECT_ID('products')";
                    try (java.sql.ResultSet rsFk = st.executeQuery(findFkSql)) {
                        while (rsFk.next()) {
                            badFks.add(rsFk.getString("name"));
                        }
                    }

                    for (String fkName : badFks) {
                        System.out.println(
                                "⚠️ Phát hiện FK sai (" + fkName
                                        + ") trỏ đến 'products' trong bảng 'wishlists'. Đang tự động xóa...");
                        st.executeUpdate("ALTER TABLE wishlists DROP CONSTRAINT " + fkName);
                    }

                    String checkCorrectFk = "SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('wishlists') AND referenced_object_id = OBJECT_ID('Product')";
                    if (!st.executeQuery(checkCorrectFk).next()) {
                        st.executeUpdate(
                                "ALTER TABLE wishlists ADD CONSTRAINT FK_Wishlists_Product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE");
                        System.out.println("✅ Đã sửa FK wishlists trỏ về bảng Product.");
                    }
                } catch (Exception ex) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}