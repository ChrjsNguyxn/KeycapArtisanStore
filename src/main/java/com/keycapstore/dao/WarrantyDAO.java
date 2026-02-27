package com.keycapstore.dao;

import com.keycapstore.config.ConnectDB;
import com.keycapstore.model.Warranty;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *   insert(Warranty)                            → Thêm yêu cầu bảo hành mới
 *   findById(int)                               → Tìm theo warranty_id
 *   findAll()                                   → Lấy tất cả
 *   findByCustomerId(int)                       → Lấy theo khách hàng
 *   findByStatus(String)                        → Lọc theo trạng thái
 *   updateStatus(int, String, int, String)      → Cập nhật trạng thái xử lý
 *   delete(int)                                 → Xóa yêu cầu
 */
public class WarrantyDAO extends BaseDAO {
    /**
     * Thêm yêu cầu bảo hành mới
     */
    public boolean insert(Warranty w) {
        String sql = "INSERT INTO warranties (order_item_id, customer_id, employee_id, reason, status, request_date, response_note) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, w.getOrderItemId());
            pst.setInt(2, w.getCustomerId());

            // employee_id NULL khi chưa có nhân viên xử lý
            if (w.getEmployeeId() > 0)
                pst.setInt(3, w.getEmployeeId());
            else
                pst.setNull(3, Types.INTEGER);

            pst.setString(4, w.getReason());

            // status NULL cho phép — mặc định "pending"
            pst.setString(5, w.getStatus() != null ? w.getStatus() : "pending");

            pst.setTimestamp(6, Timestamp.valueOf(
                w.getRequestDate() != null ? w.getRequestDate() : LocalDateTime.now()
            ));

            if (w.getResponseNote() != null)
                pst.setString(7, w.getResponseNote());
            else
                pst.setNull(7, Types.NVARCHAR);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tìm warranty theo warranty_id
     */
    public Warranty findById(int warrantyId) {
        String sql = "SELECT * FROM warranties WHERE warranty_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, warrantyId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách, mới nhất lên đầu
     */
    public List<Warranty> findAll() {
        List<Warranty> list = new ArrayList<>();
        String sql = "SELECT * FROM warranties ORDER BY request_date DESC";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả warranty của một khách hàng
     */
    
    public List<Warranty> findByCustomerId(int customerId) {
        List<Warranty> list = new ArrayList<>();
        String sql = "SELECT * FROM warranties WHERE customer_id = ? ORDER BY request_date DESC";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lọc warranty theo trạng thái
     * "pending" | "approved" | "rejected" | "completed"
     */
    public List<Warranty> findByStatus(String status) {
        List<Warranty> list = new ArrayList<>();
        String sql = "SELECT * FROM warranties WHERE status = ? ORDER BY request_date DESC";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, status);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    /**
     * Nhân viên cập nhật trạng thái xử lý bảo hành
     * warrantyId   ID warranty cần cập nhật
     * newStatus    Trạng thái mới: "approved" | "rejected" | "completed"
     * employeeId   ID nhân viên xử lý (0 nếu chưa gán)
     * responseNote Ghi chú phản hồi
     */
    public boolean updateStatus(int warrantyId, String newStatus, int employeeId, String responseNote) {
        String sql = "UPDATE warranties SET status = ?, employee_id = ?, response_note = ? WHERE warranty_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, newStatus);

            if (employeeId > 0)
                pst.setInt(2, employeeId);
            else
                pst.setNull(2, Types.INTEGER);

            if (responseNote != null)
                pst.setString(3, responseNote);
            else
                pst.setNull(3, Types.NVARCHAR);

            pst.setInt(4, warrantyId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa warranty theo ID
     */
    public boolean delete(int warrantyId) {
        String sql = "DELETE FROM warranties WHERE warranty_id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, warrantyId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Chuyển một hàng ResultSet thành Warranty object
     * Xử lý an toàn các cột NULL: employee_id, status, response_note
     */
    private Warranty mapRow(ResultSet rs) throws SQLException {
        Warranty w = new Warranty();

        w.setWarrantyId(rs.getInt("warranty_id"));
        w.setOrderItemId(rs.getInt("order_item_id"));
        w.setCustomerId(rs.getInt("customer_id"));

        // employee_id - nullable
        int empId = rs.getInt("employee_id");
        w.setEmployeeId(rs.wasNull() ? 0 : empId);

        w.setReason(rs.getString("reason"));

        // status - nullable
        w.setStatus(rs.getString("status"));

        // request_date → LocalDateTime
        Timestamp ts = rs.getTimestamp("request_date");
        w.setRequestDate(ts != null ? ts.toLocalDateTime() : null);

        // response_note - nullable
        w.setResponseNote(rs.getString("response_note"));

        return w;
    }
}