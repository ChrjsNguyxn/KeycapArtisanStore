package com.keycapstore.bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class WarrantyBUS {

    private static final int WARRANTY_MONTHS = 12;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_REJECTED = "rejected";

    public static final String RETURN_REFUND = "REFUND";
    public static final String RETURN_EXCHANGE = "EXCHANGE";

    public WarrantyBUS() {
    }

    public BUSResult createWarrantyRequest(int invoiceId, int productId, int customerId, String issue) {
        if (invoiceId <= 0)
            return BUSResult.fail("Mã hóa đơn không hợp lệ.");
        if (productId <= 0)
            return BUSResult.fail("Mã sản phẩm không hợp lệ.");
        if (customerId <= 0)
            return BUSResult.fail("Mã khách hàng không hợp lệ.");
        if (issue == null || issue.trim().length() < 10)
            return BUSResult.fail("Mô tả vấn đề cần ít nhất 10 ký tự.");

        LocalDateTime orderDate = getOrderDateByInvoice(invoiceId, productId);
        if (orderDate == null)
            return BUSResult.fail("Không tìm thấy sản phẩm #" + productId + " trong hóa đơn #" + invoiceId + ".");
        if (!isWithinWarrantyPeriod(orderDate))
            return BUSResult.fail("Sản phẩm đã hết hạn bảo hành (12 tháng kể từ ngày đặt hàng).");

        boolean created = false;
        String sql = "INSERT INTO warranties (invoice_id, product_id, customer_id, issue, status, created_at) VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, invoiceId);
            pst.setInt(2, productId);
            pst.setInt(3, customerId);
            pst.setString(4, issue.trim());
            pst.setString(5, STATUS_PENDING);
            created = pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return created
                ? BUSResult.success("Yêu cầu bảo hành đã được gửi thành công!")
                : BUSResult.fail("Gửi yêu cầu thất bại. Vui lòng thử lại.");
    }

    public BUSResult approveWarranty(int warrantyId, int employeeId, String solution) {
        if (solution == null || solution.trim().isEmpty())
            return BUSResult.fail("Vui lòng nhập phương án xử lý dự kiến.");

        String status = getWarrantyStatusById(warrantyId);
        if (status == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_PENDING.equals(status))
            return BUSResult.fail("Chỉ có thể duyệt yêu cầu ở trạng thái Chờ xử lý.");

        boolean updated = updateWarrantyStatus(warrantyId, STATUS_APPROVED, "[DUYỆT] " + solution.trim(), employeeId);
        if (updated)
            sendNotification(getCustomerIdByWarranty(warrantyId), "Bảo hành đã được duyệt",
                    "Yêu cầu bảo hành #" + warrantyId + " đã được duyệt.\nPhương án xử lý: " + solution.trim());

        return updated
                ? BUSResult.success("Đã duyệt yêu cầu bảo hành #" + warrantyId + ".")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    public BUSResult rejectWarranty(int warrantyId, int employeeId, String reason) {
        if (reason == null || reason.trim().isEmpty())
            return BUSResult.fail("Vui lòng nhập lý do từ chối.");

        String status = getWarrantyStatusById(warrantyId);
        if (status == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_PENDING.equals(status))
            return BUSResult.fail("Chỉ có thể từ chối yêu cầu ở trạng thái Chờ xử lý.");

        boolean updated = updateWarrantyStatus(warrantyId, STATUS_REJECTED, "[TỪ CHỐI] " + reason.trim(), employeeId);
        if (updated)
            sendNotification(getCustomerIdByWarranty(warrantyId), "Từ chối bảo hành", "Yêu cầu bảo hành #" + warrantyId
                    + " của bạn không đủ điều kiện bảo hành.\nLý do: " + reason.trim());

        return updated
                ? BUSResult.success("Đã từ chối yêu cầu bảo hành #" + warrantyId + ".")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    public BUSResult processReturn(int warrantyId, int employeeId, String returnType, String note) {
        if (!RETURN_REFUND.equals(returnType) && !RETURN_EXCHANGE.equals(returnType))
            return BUSResult.fail("Loại xử lý không hợp lệ (REFUND hoặc EXCHANGE).");

        String status = getWarrantyStatusById(warrantyId);
        if (status == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_APPROVED.equals(status))
            return BUSResult.fail("Yêu cầu phải được duyệt trước khi xử lý.");

        String label = RETURN_REFUND.equals(returnType) ? "HOÀN TIỀN" : "ĐỔI HÀNG";
        String processNote = "[" + label + "] " + (note != null ? note.trim() : "");

        boolean updated = updateWarrantyStatus(warrantyId, STATUS_IN_PROGRESS, processNote, employeeId);

        // TỰ ĐỘNG XỬ LÝ KHO NẾU LÀ ĐỔI HÀNG
        if (updated && RETURN_EXCHANGE.equals(returnType)) {
            deductStockForExchange(warrantyId, employeeId);
            sendNotification(getCustomerIdByWarranty(warrantyId), "Đang xử lý bảo hành", "Sản phẩm của bạn (Mã BH: #"
                    + warrantyId + ") đang được xuất kho ĐỔI MỚI.\nGhi chú: " + processNote);
        } else if (updated && RETURN_REFUND.equals(returnType)) {
            sendNotification(getCustomerIdByWarranty(warrantyId), "Đang xử lý bảo hành",
                    "Yêu cầu bảo hành #" + warrantyId + " đang được HOÀN TIỀN.\nGhi chú: " + processNote);
        }

        return updated
                ? BUSResult.success("Đã bắt đầu xử lý "
                        + (RETURN_REFUND.equals(returnType) ? "hoàn tiền" : "đổi sản phẩm")
                        + " cho yêu cầu #" + warrantyId + ".")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    public BUSResult completeWarranty(int warrantyId, int employeeId, String finalNote) {
        String status = getWarrantyStatusById(warrantyId);
        if (status == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_IN_PROGRESS.equals(status))
            return BUSResult.fail("Yêu cầu phải đang ở trạng thái Đang xử lý.");

        String note = "[HOÀN TẤT] " + (finalNote != null && !finalNote.trim().isEmpty()
                ? finalNote.trim()
                : "Đã xử lý xong.");

        boolean updated = updateWarrantyStatus(warrantyId, STATUS_COMPLETED, note, employeeId);
        if (updated)
            sendNotification(getCustomerIdByWarranty(warrantyId), "Hoàn tất bảo hành",
                    "Yêu cầu bảo hành #" + warrantyId + " đã hoàn tất!\n" + note);

        return updated
                ? BUSResult.success("Yêu cầu bảo hành #" + warrantyId + " đã hoàn tất.")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    private String getWarrantyStatusById(int warrantyId) {
        String sql = "SELECT status FROM warranties WHERE warranty_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, warrantyId);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
                return rs.getString("status");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean updateWarrantyStatus(int warrantyId, String status, String note, int employeeId) {
        String sql = "UPDATE warranties SET status = ?, resolution_note = ?, employee_id = ? WHERE warranty_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setString(2, note);
            pst.setInt(3, employeeId);
            pst.setInt(4, warrantyId);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deductStockForExchange(int warrantyId, int employeeId) {
        String sqlSelect = "SELECT product_id FROM warranties WHERE warranty_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {
            pstSelect.setInt(1, warrantyId);
            ResultSet rs = pstSelect.executeQuery();
            if (rs.next()) {
                int productId = rs.getInt("product_id");
                String sqlUpdate = "UPDATE Product SET stock = stock - 1 WHERE product_id = ? AND stock > 0";
                try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
                    pstUpdate.setInt(1, productId);
                    if (pstUpdate.executeUpdate() > 0) {
                        String sqlEntry = "INSERT INTO StockEntry (product_id, employee_id, quantity_added, entry_price, entry_date, note) VALUES (?, ?, -1, 0, GETDATE(), ?)";
                        try (PreparedStatement pstEntry = con.prepareStatement(sqlEntry)) {
                            pstEntry.setInt(1, productId);
                            pstEntry.setInt(2, employeeId);
                            pstEntry.setString(3, "Xuất kho đổi trả bảo hành #" + warrantyId);
                            pstEntry.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private int getCustomerIdByWarranty(int warrantyId) {
        String sql = "SELECT customer_id FROM warranties WHERE warranty_id = ?";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, warrantyId);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
                return rs.getInt("customer_id");
        } catch (Exception e) {
        }
        return -1;
    }

    private void sendNotification(int customerId, String title, String message) {
        if (customerId <= 0)
            return;
        String sql = "INSERT INTO notifications (customer_id, title, message) VALUES (?, ?, ?)";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, customerId);
            pst.setString(2, title);
            pst.setString(3, message);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // CÁC HÀM MỚI BỔ SUNG CHO UI (ADMIN & CUSTOMER)
    // =========================================================================

    /**
     * Lấy trạng thái bảo hành hiện tại của một sản phẩm trong đơn hàng (Dành cho
     * Customer)
     */
    public String getWarrantyStatusForCustomer(int invoiceId, int productId) {
        String sql = "SELECT TOP 1 status FROM warranties WHERE invoice_id = ? AND product_id = ? ORDER BY created_at DESC";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, invoiceId);
            pst.setInt(2, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Chưa từng yêu cầu bảo hành
    }

    /**
     * Lấy toàn bộ danh sách bảo hành để hiển thị lên bảng cho Admin
     */
    public List<Object[]> getAllWarrantiesForDisplay() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT w.warranty_id, w.invoice_id, w.product_id, w.customer_id, w.issue, w.status, w.created_at, w.resolution_note "
                +
                "FROM warranties w ORDER BY w.created_at DESC";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[] {
                        rs.getInt("warranty_id"),
                        rs.getInt("invoice_id"),
                        rs.getInt("product_id"),
                        rs.getInt("customer_id"),
                        rs.getString("issue"), // Đổi thành tên cột thực tế trong CSDL
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        rs.getString("resolution_note")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private LocalDateTime getOrderDateByInvoice(int invoiceId, int productId) {
        String sql = "SELECT TOP 1 i.created_at FROM InvoiceDetail d "
                + "JOIN Invoice i ON d.invoice_id = i.invoice_id "
                + "WHERE d.invoice_id = ? AND d.product_id = ? ORDER BY i.created_at DESC";
        try (Connection con = com.keycapstore.config.ConnectDB.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, invoiceId);
            pst.setInt(2, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                return ts != null ? ts.toLocalDateTime() : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isWithinWarrantyPeriod(LocalDateTime orderDate) {
        if (orderDate == null)
            return false;
        return LocalDateTime.now().isBefore(orderDate.plusMonths(WARRANTY_MONTHS));
    }

    public static class BUSResult {
        private final boolean success;
        private final String message;

        private BUSResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static BUSResult success(String msg) {
            return new BUSResult(true, msg);
        }

        public static BUSResult fail(String msg) {
            return new BUSResult(false, msg);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}