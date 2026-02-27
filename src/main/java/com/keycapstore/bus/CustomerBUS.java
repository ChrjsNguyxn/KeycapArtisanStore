package com.keycapstore.bus;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Customer;
import java.sql.*;
import java.util.ArrayList;

public class CustomerBUS {

    public Customer login(String user, String pass) {
        Customer cus = null;
        String sql = "SELECT * FROM customers WHERE username = ? AND password = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, user);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                cus = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getString("status") != null ? rs.getString("status").trim() : "active");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cus;
    }

    public boolean register(Customer cus) {
        // 1. Kiểm tra Email trước (Quyền lực tối cao)
        if (isEmailExist(cus.getEmail())) {
            return false;
        }

        // 2. Kiểm tra SĐT để "Nhận Vơ" dữ liệu POS
        Customer posCustomer = getCustomerByPhone(cus.getPhone());

        try (Connection con = ConnectDB.getConnection()) {
            if (posCustomer != null) {
                // NGÃ RẼ B: SĐT quen quen (Đã mua ở POS) -> Kiểm tra xem đã có TK Web chưa?
                if (posCustomer.getUsername() != null && !posCustomer.getUsername().startsWith("guest_")) {
                    // Đã có tài khoản chính thức -> Báo lỗi trùng SĐT (Đáng lẽ đã bị chặn ở
                    // checkDuplicate)
                    return false;
                } else {
                    // Chưa có tài khoản Web (đang là guest) -> UPDATE để "Claim" (Nhận vơ) tài
                    // khoản
                    String sqlUpdate = "UPDATE customers SET username=?, password=?, full_name=?, email=?, address=?, status='active' WHERE customer_id=?";
                    PreparedStatement pst = con.prepareStatement(sqlUpdate);
                    pst.setString(1, cus.getUsername());
                    pst.setString(2, cus.getPassword());
                    pst.setString(3, cus.getFullName()); // Cập nhật lại tên chuẩn do khách nhập
                    pst.setString(4, cus.getEmail());
                    pst.setString(5, cus.getAddress());
                    pst.setInt(6, posCustomer.getCustomerId());
                    return pst.executeUpdate() > 0;
                }
            } else {
                // NGÃ RẼ A: Khách mới tinh 100% -> INSERT mới hoàn toàn
                String sqlInsert = "INSERT INTO customers(username, password, full_name, email, phone_number, address, status, rank_id, total_spending) VALUES (?, ?, ?, ?, ?, ?, 'active', 1, 0)";
                PreparedStatement pst = con.prepareStatement(sqlInsert);
                pst.setString(1, cus.getUsername());
                pst.setString(2, cus.getPassword());
                pst.setString(3, cus.getFullName());
                pst.setString(4, cus.getEmail());
                pst.setString(5, cus.getPhone());
                pst.setString(6, cus.getAddress());
                return pst.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hàm thêm khách hàng dành cho Admin (có thể set trạng thái)
    public boolean addCustomer(Customer cus) {
        String sql = "INSERT INTO customers(username, password, full_name, email, phone_number, address, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cus.getUsername());
            pst.setString(2, cus.getPassword());
            pst.setString(3, cus.getFullName());
            pst.setString(4, cus.getEmail());
            pst.setString(5, cus.getPhone());
            pst.setString(6, cus.getAddress());
            pst.setString(7, cus.getStatus());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String checkDuplicate(String user, String email, String phone) {
        try (Connection con = ConnectDB.getConnection()) {

            PreparedStatement p1 = con.prepareStatement("SELECT username FROM customers WHERE username = ?");
            p1.setString(1, user);
            if (p1.executeQuery().next())
                return "Tên đăng nhập đã tồn tại!";

            PreparedStatement p2 = con.prepareStatement("SELECT email FROM customers WHERE email = ?");
            p2.setString(1, email);
            if (p2.executeQuery().next())
                return "Email này đã được sử dụng!";

            // Check Phone (SMART CHECK): Chỉ chặn nếu SĐT đã gắn với tài khoản thật (không
            // phải guest_)
            if (phone != null && !phone.trim().isEmpty()) {
                PreparedStatement p3 = con.prepareStatement("SELECT username FROM customers WHERE phone_number = ?");
                p3.setString(1, phone);
                ResultSet rs = p3.executeQuery();
                if (rs.next()) {
                    String existUser = rs.getString("username");
                    // Nếu user tồn tại và KHÔNG bắt đầu bằng guest_ -> Tức là đã có chủ xịn -> Báo
                    // lỗi
                    if (existUser != null && !existUser.startsWith("guest_")) {
                        return "Số điện thoại này đã được sử dụng!";
                    }
                    // Nếu là guest_ -> Return null (Cho phép đi tiếp vào hàm register để Claim)
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEmailExist(String email) {
        String sql = "SELECT email FROM customers WHERE email = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, email);
            return pst.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(String email, String newPass) {
        String sql = "UPDATE customers SET password = ? WHERE email = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, newPass);
            pst.setString(2, email);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Customer> getAllCustomers() {
        ArrayList<Customer> list = new ArrayList<>();

        String sql = "SELECT c.*, r.name as rank_name, r.discount_percent " +
                "FROM customers c " +
                "LEFT JOIN customer_ranks r ON c.rank_id = r.rank_id " +
                "ORDER BY c.total_spending DESC";

        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getString("status") != null ? rs.getString("status").trim() : "active");

                // Set thêm thông tin Rank
                try {
                    c.setTotalSpending(rs.getDouble("total_spending"));
                    c.setRankName(rs.getString("rank_name"));
                    c.setCurrentDiscount(rs.getDouble("discount_percent"));
                } catch (SQLException e) {
                } // Bỏ qua nếu cột chưa tồn tại

                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateCustomerByAdmin(Customer c) {
        StringBuilder sql = new StringBuilder(
                "UPDATE customers SET full_name=?, email=?, phone_number=?, address=?, status=?");

        if (c.getPassword() != null && !c.getPassword().isEmpty()) {
            sql.append(", password=?");
        }
        sql.append(" WHERE customer_id=?");

        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql.toString())) {
            pst.setString(1, c.getFullName());
            pst.setString(2, c.getEmail());
            pst.setString(3, c.getPhone());
            pst.setString(4, c.getAddress());
            pst.setString(5, c.getStatus());

            int index = 6;
            if (c.getPassword() != null && !c.getPassword().isEmpty()) {
                pst.setString(index++, c.getPassword());
            }
            pst.setInt(index, c.getCustomerId());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Không xóa hẳn mà chỉ khóa tài khoản (status = banned) hoặc xóa nếu cần thiết
    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Customer getCustomerByPhone(String phone) {
        // Fix: Sửa c.phone thành c.phone_number cho đúng với Database
        String sql = "SELECT c.customer_id, c.full_name, c.phone_number, c.total_spending, " +
                "r.name as rank_name, r.discount_percent " +
                "FROM customers c " +
                "JOIN customer_ranks r ON c.rank_id = r.rank_id " +
                "WHERE c.phone_number = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, phone);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("customer_id"), rs.getString("full_name"), rs.getString("phone_number"),
                        rs.getDouble("total_spending"), rs.getString("rank_name"), rs.getDouble("discount_percent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Hàm thêm nhanh khách hàng tại quầy (Chỉ cần Tên + SĐT)
    public boolean addQuickCustomer(Customer cus) {
        // Mặc định rank_id = 1 (Thành Viên), total_spending = 0
        // Fix: Thêm username/password giả định để tránh lỗi NOT NULL trong Database
        // Thêm timestamp để đảm bảo username luôn duy nhất, tránh lỗi trùng lặp khi tạo
        // lại
        String dummyUser = "guest_" + cus.getPhone() + "_" + System.currentTimeMillis();
        // Tạo email giả định duy nhất để tránh lỗi UNIQUE constraint trên cột email
        // (nếu có)
        String dummyEmail = dummyUser + "@guest.local";
        String dummyPass = "123";

        String sql = "INSERT INTO customers(username, password, full_name, phone_number, email, status, rank_id, total_spending) VALUES (?, ?, ?, ?, ?, 'active', 1, 0)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dummyUser);
            pst.setString(2, dummyPass);
            pst.setString(3, cus.getFullName());
            pst.setString(4, cus.getPhone());
            pst.setString(5, dummyEmail);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Hàm cập nhật riêng địa chỉ (Dùng cho SalesPanel khi ship hàng)
    public boolean updateAddress(int customerId, String newAddress) {
        String sql = "UPDATE customers SET address = ? WHERE customer_id = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, newAddress);
            pst.setInt(2, customerId);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}