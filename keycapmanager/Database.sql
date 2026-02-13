
USE master;
GO
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'JavaKADB')
BEGIN
    ALTER DATABASE [JavaKADB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [JavaKADB];
END
GO
CREATE DATABASE JavaKADB;
GO
USE JavaKADB;
GO

-- Phan 1: Quan tri va Nhan su

-- Bang Nhan vien
CREATE TABLE employees (
  employee_id INT IDENTITY(1,1) PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(15),
  role VARCHAR(20) DEFAULT 'sales_manager' CHECK (role IN ('super_admin', 'warehouse_manager', 'sales_manager')),
  status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'banned', 'quit')),
  created_at DATETIME DEFAULT GETDATE()
);

-- Bang Nhat ky he thong
CREATE TABLE system_logs (
  log_id INT IDENTITY(1,1) PRIMARY KEY,
  employee_id INT NOT NULL,
  action VARCHAR(50) NOT NULL,
  target_table VARCHAR(50),
  record_id INT,
  description NVARCHAR(MAX),
  log_date DATETIME DEFAULT GETDATE(),
  FOREIGN KEY (employee_id) REFERENCES employees (employee_id)
);

-- Phan 2: Kho va San pham

-- Bang Hang san xuat
CREATE TABLE makers (
  maker_id INT IDENTITY(1,1) PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  origin VARCHAR(50),
  website VARCHAR(255)
);

-- Bang Danh muc
CREATE TABLE categories (
  category_id INT IDENTITY(1,1) PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description NVARCHAR(MAX)
);

-- Bang San pham
CREATE TABLE products (
  product_id INT IDENTITY(1,1) PRIMARY KEY,
  category_id INT NOT NULL,
  maker_id INT NOT NULL,
  name NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  price DECIMAL(10, 2) NOT NULL,
  stock_quantity INT DEFAULT 0,
  profile VARCHAR(50),
  material VARCHAR(50),
  is_active BIT DEFAULT 1,
  created_at DATETIME DEFAULT GETDATE(),
  FOREIGN KEY (category_id) REFERENCES categories (category_id),
  FOREIGN KEY (maker_id) REFERENCES makers (maker_id)
);

-- Bang Nha cung cap
CREATE TABLE suppliers (
  supplier_id INT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(100) NOT NULL,
  phone VARCHAR(15),
  address NVARCHAR(MAX),
  email VARCHAR(100)
);

-- Bang Phieu nhap kho
CREATE TABLE import_orders (
  import_id INT IDENTITY(1,1) PRIMARY KEY,
  supplier_id INT NOT NULL,
  employee_id INT NOT NULL,
  total_cost DECIMAL(15, 2) NOT NULL,
  import_date DATETIME DEFAULT GETDATE(),
  note NVARCHAR(MAX),
  FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id),
  FOREIGN KEY (employee_id) REFERENCES employees (employee_id)
);

-- Bang Chi tiet phieu nhap
CREATE TABLE import_order_items (
  import_item_id INT IDENTITY(1,1) PRIMARY KEY,
  import_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  import_price DECIMAL(10, 2),
  FOREIGN KEY (import_id) REFERENCES import_orders (import_id),
  FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- Bang Hinh anh san pham
CREATE TABLE product_images (
  image_id INT IDENTITY(1,1) PRIMARY KEY,
  product_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_thumbnail BIT DEFAULT 0,
  FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- Phan 3: Ban hang va Khach hang

-- Bang Hang thanh vien
CREATE TABLE customer_ranks (
  rank_id INT IDENTITY(1,1) PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  min_spending DECIMAL(15, 2) NOT NULL,
  discount_percent DECIMAL(5, 2) DEFAULT 0,
  description NVARCHAR(MAX)
);

-- Bang Khach hang
CREATE TABLE customers (
  customer_id INT IDENTITY(1,1) PRIMARY KEY,
  rank_id INT DEFAULT 1,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name NVARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE,
  phone_number VARCHAR(15),
  address NVARCHAR(MAX),
  total_spending DECIMAL(15,2) DEFAULT 0,
  created_at DATETIME DEFAULT GETDATE(),
  FOREIGN KEY (rank_id) REFERENCES customer_ranks (rank_id)
);

-- Bang Phuong thuc van chuyen
CREATE TABLE shipping_methods (
  shipping_method_id INT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(50) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  estimated_days VARCHAR(50)
);

-- Bang Ma giam gia
CREATE TABLE vouchers (
  voucher_id INT IDENTITY(1,1) PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  discount_percent DECIMAL(5, 2),
  quantity INT DEFAULT 0,
  start_date DATETIME,
  expired_date DATETIME
);

-- Bang Don hang
CREATE TABLE orders (
  order_id INT IDENTITY(1,1) PRIMARY KEY,
  customer_id INT NOT NULL,
  employee_id INT,
  voucher_id INT,
  shipping_method_id INT NOT NULL,
  order_date DATETIME DEFAULT GETDATE(),
  total_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'shipping', 'delivered', 'cancelled', 'returned')),
  delivery_address NVARCHAR(MAX) NOT NULL,
  tracking_number VARCHAR(50),
  FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
  FOREIGN KEY (employee_id) REFERENCES employees (employee_id),
  FOREIGN KEY (voucher_id) REFERENCES vouchers (voucher_id),
  FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods (shipping_method_id)
);

-- Bang Chi tiet don hang
CREATE TABLE order_items (
  order_item_id INT IDENTITY(1,1) PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  price_at_purchase DECIMAL(10, 2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders (order_id),
  FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- Bang Bao hanh
CREATE TABLE warranties (
  warranty_id INT IDENTITY(1,1) PRIMARY KEY,
  order_item_id INT NOT NULL,
  customer_id INT NOT NULL,
  employee_id INT,
  reason NVARCHAR(MAX) NOT NULL,
  status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'completed')),
  request_date DATETIME DEFAULT GETDATE(),
  response_note NVARCHAR(MAX),
  FOREIGN KEY (order_item_id) REFERENCES order_items (order_item_id),
  FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
  FOREIGN KEY (employee_id) REFERENCES employees (employee_id)
);

-- Bang Danh gia
CREATE TABLE reviews (
  review_id INT IDENTITY(1,1) PRIMARY KEY,
  customer_id INT NOT NULL,
  product_id INT NOT NULL,
  rating INT,
  comment NVARCHAR(MAX),
  created_at DATETIME DEFAULT GETDATE(),
  FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
  FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- Bang Yeu thich
CREATE TABLE wishlists (
  wishlist_id INT IDENTITY(1,1) PRIMARY KEY,
  customer_id INT NOT NULL,
  product_id INT NOT NULL,
  created_at DATETIME DEFAULT GETDATE(),
  FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
  FOREIGN KEY (product_id) REFERENCES products (product_id)
);

-- Bang Thanh toan
CREATE TABLE payments (
  payment_id INT IDENTITY(1,1) PRIMARY KEY,
  order_id INT NOT NULL UNIQUE,
  payment_method VARCHAR(20) DEFAULT 'cod' CHECK (payment_method IN ('cod', 'banking', 'momo')),
  payment_status VARCHAR(20) DEFAULT 'unpaid' CHECK (payment_status IN ('unpaid', 'paid', 'refunded')),
  paid_at DATETIME,
  FOREIGN KEY (order_id) REFERENCES orders (order_id)
);

-- Du lieu mau

-- Admin mac dinh
INSERT INTO employees (username, password, full_name, email, role, status)
VALUES ('admin', '123', 'Super Admin', 'admin@keycap.store', 'super_admin', 'active');

-- Hang thanh vien
INSERT INTO customer_ranks (name, min_spending, discount_percent, description)
VALUES ('Bronze', 0, 0, 'Thành viên mới'),
       ('Silver', 1000000, 5, 'Giảm 5%'),
       ('Gold', 5000000, 10, 'Giảm 10%');

-- Hang san xuat
INSERT INTO makers (name, origin) VALUES ('Artkey', 'Vietnam'), ('Jelly Key', 'Vietnam');

-- Danh muc
INSERT INTO categories (name) VALUES ('Artisan Keycap'), ('Keycap Set');