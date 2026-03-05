package com.keycapstore.bus;

import com.keycapstore.dao.WarrantyDAO;
import com.keycapstore.model.Warranty;

import java.time.LocalDateTime;

/**
 * WarrantyBUS - Business Logic cho Bảo hành
 *
 * Workflow: pending → approved → in_progress → completed
 *                             ↘ rejected
 */
public class WarrantyBUS {

    private static final int WARRANTY_MONTHS = 12;

    public static final String STATUS_PENDING     = "pending";
    public static final String STATUS_APPROVED    = "approved";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED   = "completed";
    public static final String STATUS_REJECTED    = "rejected";

    public static final String RETURN_REFUND   = "REFUND";
    public static final String RETURN_EXCHANGE = "EXCHANGE";

    private final WarrantyDAO warrantyDAO;

    public WarrantyBUS() {
        this.warrantyDAO = new WarrantyDAO();
    }

    // ── 1. TẠO YÊU CẦU ──────────────────────────────────────

    public BUSResult createWarrantyRequest(int orderItemId, int customerId, String issue) {
        if (orderItemId <= 0)
            return BUSResult.fail("Mã chi tiết đơn hàng không hợp lệ.");
        if (customerId <= 0)
            return BUSResult.fail("Mã khách hàng không hợp lệ.");
        if (issue == null || issue.trim().length() < 10)
            return BUSResult.fail("Mô tả vấn đề cần ít nhất 10 ký tự.");

        Warranty warranty = new Warranty(orderItemId, customerId, issue.trim());
        boolean created = warrantyDAO.insert(warranty);

        return created
                ? BUSResult.success("Yêu cầu bảo hành đã được gửi thành công!")
                : BUSResult.fail("Gửi yêu cầu thất bại. Vui lòng thử lại.");
    }

    // ── 2. DUYỆT (pending → approved) ───────────────────────

    public BUSResult approveWarranty(int warrantyId, int employeeId, String solution) {
        if (solution == null || solution.trim().isEmpty())
            return BUSResult.fail("Vui lòng nhập phương án xử lý dự kiến.");

        Warranty warranty = warrantyDAO.findById(warrantyId);
        if (warranty == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_PENDING.equals(warranty.getStatus()))
            return BUSResult.fail("Chỉ có thể duyệt yêu cầu ở trạng thái Chờ xử lý.");

        boolean updated = warrantyDAO.updateStatus(
                warrantyId, STATUS_APPROVED, employeeId, "[DUYỆT] " + solution.trim());

        return updated
                ? BUSResult.success("Đã duyệt yêu cầu bảo hành #" + warrantyId + ".")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    // ── 3. TỪ CHỐI (pending → rejected) ─────────────────────

    public BUSResult rejectWarranty(int warrantyId, int employeeId, String reason) {
        if (reason == null || reason.trim().isEmpty())
            return BUSResult.fail("Vui lòng nhập lý do từ chối.");

        Warranty warranty = warrantyDAO.findById(warrantyId);
        if (warranty == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_PENDING.equals(warranty.getStatus()))
            return BUSResult.fail("Chỉ có thể từ chối yêu cầu ở trạng thái Chờ xử lý.");

        boolean updated = warrantyDAO.updateStatus(
                warrantyId, STATUS_REJECTED, employeeId, "[TỪ CHỐI] " + reason.trim());

        return updated
                ? BUSResult.success("Đã từ chối yêu cầu bảo hành #" + warrantyId + ".")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    // ── 4. XỬ LÝ HOÀN TIỀN / ĐỔI HÀNG (approved → in_progress)

    public BUSResult processReturn(int warrantyId, int employeeId, String returnType, String note) {
        if (!RETURN_REFUND.equals(returnType) && !RETURN_EXCHANGE.equals(returnType))
            return BUSResult.fail("Loại xử lý không hợp lệ (REFUND hoặc EXCHANGE).");

        Warranty warranty = warrantyDAO.findById(warrantyId);
        if (warranty == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_APPROVED.equals(warranty.getStatus()))
            return BUSResult.fail("Yêu cầu phải được duyệt trước khi xử lý.");

        String label = RETURN_REFUND.equals(returnType) ? "HOÀN TIỀN" : "ĐỔI HÀNG";
        String processNote = "[" + label + "] " + (note != null ? note.trim() : "");

        boolean updated = warrantyDAO.updateStatus(
                warrantyId, STATUS_IN_PROGRESS, employeeId, processNote);

        return updated
                ? BUSResult.success("Đã bắt đầu xử lý "
                        + (RETURN_REFUND.equals(returnType) ? "hoàn tiền" : "đổi sản phẩm")
                        + " cho yêu cầu #" + warrantyId + ".")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    // ── 5. HOÀN TẤT (in_progress → completed) ───────────────

    public BUSResult completeWarranty(int warrantyId, int employeeId, String finalNote) {
        Warranty warranty = warrantyDAO.findById(warrantyId);
        if (warranty == null)
            return BUSResult.fail("Không tìm thấy yêu cầu bảo hành #" + warrantyId);
        if (!STATUS_IN_PROGRESS.equals(warranty.getStatus()))
            return BUSResult.fail("Yêu cầu phải đang ở trạng thái Đang xử lý.");

        String note = "[HOÀN TẤT] " + (finalNote != null && !finalNote.trim().isEmpty()
                ? finalNote.trim() : "Đã xử lý xong.");

        boolean updated = warrantyDAO.updateStatus(
                warrantyId, STATUS_COMPLETED, employeeId, note);

        return updated
                ? BUSResult.success("Yêu cầu bảo hành #" + warrantyId + " đã hoàn tất.")
                : BUSResult.fail("Cập nhật thất bại. Vui lòng thử lại.");
    }

    // ── HELPER ───────────────────────────────────────────────

    private boolean isWithinWarrantyPeriod(LocalDateTime orderDate) {
        if (orderDate == null) return false;
        return LocalDateTime.now().isBefore(orderDate.plusMonths(WARRANTY_MONTHS));
    }

    // ── INNER CLASS: BUSResult ───────────────────────────────

    public static class BUSResult {
        private final boolean success;
        private final String  message;

        private BUSResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static BUSResult success(String msg) { return new BUSResult(true,  msg); }
        public static BUSResult fail(String msg)    { return new BUSResult(false, msg); }

        public boolean isSuccess()  { return success; }
        public String  getMessage() { return message; }
    }
}