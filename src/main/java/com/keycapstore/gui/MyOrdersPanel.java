package com.keycapstore.gui;

import com.keycapstore.bus.InvoiceBUS;
import com.keycapstore.bus.ProductBUS;
import com.keycapstore.bus.ReviewBUS;
import com.keycapstore.bus.WarrantyBUS; // Import WarrantyBUS
import com.keycapstore.model.Customer;
import com.keycapstore.model.Invoice;
import com.keycapstore.model.InvoiceDetail;
import com.keycapstore.model.Product;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MyOrdersPanel extends JPanel implements Refreshable {

    private Object currentUser;
    private InvoiceBUS invoiceBus;
    private JTable table;
    private DefaultTableModel model;
    private DecimalFormat df = new DecimalFormat("#,###");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private ReviewBUS reviewBUS;
    private ProductBUS productBUS;
    private WarrantyBUS warrantyBUS;

    public MyOrdersPanel(Object user) {
        this.currentUser = user;
        this.invoiceBus = new InvoiceBUS();
        this.reviewBUS = new ReviewBUS(); // Khởi tạo đúng cú pháp
        this.productBUS = new ProductBUS();
        this.warrantyBUS = new WarrantyBUS(); // Khởi tạo WarrantyBUS

        setLayout(new BorderLayout());
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // HEADER
        JLabel lblTitle = new JLabel("LỊCH SỬ MUA HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        // TABLE
        String[] headers = { "Mã Đơn", "Ngày Mua", "Tổng Tiền", "Trạng Thái" };
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(ThemeColor.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(255, 224, 178));
        table.setSelectionForeground(Color.BLACK);

        // Sự kiện click vào đơn hàng để xem chi tiết và đánh giá
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    Object idObj = table.getValueAt(table.getSelectedRow(), 0);
                    if (idObj != null) {
                        showOrderDetail(Integer.parseInt(idObj.toString()));
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Nút làm mới
        JButton btnRefresh = new JButton("Làm Mới");
        btnRefresh.setBackground(ThemeColor.INFO);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadData());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(ThemeColor.BG_LIGHT);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        if (!(currentUser instanceof Customer)) {
            return;
        }

        Customer cus = (Customer) currentUser;
        ArrayList<Invoice> allInvoices = invoiceBus.getAllInvoices();

        if (allInvoices != null) {
            for (Invoice inv : allInvoices) {
                // Lọc theo SĐT khách hàng (vì Invoice lưu SĐT)
                if (inv.getCustomerPhone() != null && inv.getCustomerPhone().equals(cus.getPhone())) {
                    model.addRow(new Object[] {
                            inv.getId(),
                            inv.getCreatedAt() != null ? sdf.format(inv.getCreatedAt()) : "",
                            df.format(inv.getTotalAmount()) + " ₫",
                            "Thành công" // Mặc định là thành công vì đã có hóa đơn
                    });
                }
            }
        }
    }

    private void showOrderDetail(int invoiceId) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        JDialog dialog = new JDialog(parentFrame, "Chi tiết đơn hàng #" + invoiceId, true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        ArrayList<InvoiceDetail> details = invoiceBus.getInvoiceDetails(invoiceId);

        JPanel pnlItems = new JPanel();
        pnlItems.setLayout(new BoxLayout(pnlItems, BoxLayout.Y_AXIS));
        pnlItems.setBackground(Color.WHITE);
        pnlItems.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (InvoiceDetail d : details) {
            JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    new EmptyBorder(10, 0, 10, 0)));

            JLabel lblInfo = new JLabel("<html><b>" + d.getProductName() + "</b><br>" +
                    "SL: " + d.getQuantity() + " | Giá: " + df.format(d.getPrice()) + "₫</html>");
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            // Nút Đánh giá
            JButton btnRate = new JButton("Viết đánh giá");
            btnRate.setBackground(ThemeColor.WARNING);
            btnRate.setForeground(Color.WHITE);
            btnRate.setFocusPainted(false);
            btnRate.setFont(new Font("Segoe UI", Font.BOLD, 12));

            // Nút Bảo hành
            JButton btnWarranty = new JButton("Yêu cầu bảo hành");
            btnWarranty.setBackground(ThemeColor.INFO); // Đã sửa thành biến màu đúng chuẩn của project
            btnWarranty.setForeground(Color.WHITE);
            btnWarranty.setFocusPainted(false);
            btnWarranty.setFont(new Font("Segoe UI", Font.BOLD, 12));

            // Kiểm tra xem đã đánh giá chưa
            if (currentUser instanceof Customer) {
                int cusId = ((Customer) currentUser).getCustomerId();

                // FIX: Kiểm tra ID sản phẩm hợp lệ (Tránh lỗi FK khi ID = 0)
                if (d.getProductId() <= 0) {
                    btnRate.setText("Sản phẩm lỗi");
                    btnRate.setEnabled(false);
                    btnRate.setToolTipText("Không tìm thấy thông tin sản phẩm này");

                    btnWarranty.setEnabled(false);
                }
                // Kiểm tra xem đã đánh giá chưa
                else if (reviewBUS != null && reviewBUS.hasReviewed(cusId, d.getProductId())) {
                    btnRate.setText("Đã đánh giá");
                    btnRate.setEnabled(false);
                    btnRate.setBackground(Color.LIGHT_GRAY);
                } else {
                    btnRate.addActionListener(e -> {
                        Product p = productBUS.getProductById(d.getProductId());

                        // FIX: Nếu sản phẩm đã bị xóa vĩnh viễn khỏi DB (p == null), không cho đánh giá
                        if (p == null) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Sản phẩm này đã bị xóa khỏi hệ thống nên không thể đánh giá.",
                                    "Không thể đánh giá", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // Fix: getProductById trả về object sơ sài, cần set lại tên để hiển thị
                        p.setName(d.getProductName());

                        // Gọi ReviewDialog đúng cú pháp
                        ReviewDialog rvDialog = new ReviewDialog(parentFrame, cusId, p);
                        rvDialog.setVisible(true);
                        if (rvDialog.isSuccess()) {
                            btnRate.setText("Đã đánh giá");
                            btnRate.setEnabled(false);
                            btnRate.setBackground(Color.LIGHT_GRAY);
                        }
                    });
                }

                // FIX LOGIC: Kiểm tra trạng thái bảo hành từ Database
                String currentWarrantyStatus = warrantyBUS.getWarrantyStatusForCustomer(invoiceId, d.getProductId());
                if (currentWarrantyStatus != null) {
                    btnWarranty.setEnabled(false); // Khóa nút không cho spam gửi yêu cầu
                    switch (currentWarrantyStatus) {
                        case WarrantyBUS.STATUS_PENDING:
                            btnWarranty.setText("Đang chờ duyệt");
                            btnWarranty.setBackground(Color.GRAY);
                            break;
                        case WarrantyBUS.STATUS_APPROVED:
                            btnWarranty.setText("Đã duyệt - Hãy gửi hàng");
                            btnWarranty.setBackground(ThemeColor.INFO);
                            break;
                        case WarrantyBUS.STATUS_IN_PROGRESS:
                            btnWarranty.setText("Hãng đang xử lý");
                            btnWarranty.setBackground(ThemeColor.WARNING);
                            break;
                        case WarrantyBUS.STATUS_COMPLETED:
                            btnWarranty.setText("Bảo hành hoàn tất");
                            btnWarranty.setBackground(ThemeColor.SUCCESS);
                            break;
                        case WarrantyBUS.STATUS_REJECTED:
                            btnWarranty.setText("Từ chối bảo hành");
                            btnWarranty.setBackground(ThemeColor.DANGER);
                            // Nút mở lại nếu bị từ chối và khách muốn gửi khiếu nại khác? (Tùy chọn)
                            // btnWarranty.setEnabled(true);
                            break;
                    }
                }

                // Sự kiện cho nút Bảo Hành
                btnWarranty.addActionListener(e -> {
                    String reason = JOptionPane.showInputDialog(dialog,
                            "Nhập mô tả chi tiết lỗi của sản phẩm [" + d.getProductName()
                                    + "]:\n(Lưu ý: Chỉ áp dụng cho lỗi từ nhà sản xuất)",
                            "Yêu cầu Bảo hành", JOptionPane.QUESTION_MESSAGE);

                    if (reason != null && !reason.trim().isEmpty()) {
                        try {
                            // Mã sản phẩm thực tế
                            int productId = d.getProductId();

                            // GỌI DB THẬT
                            WarrantyBUS.BUSResult result = warrantyBUS.createWarrantyRequest(invoiceId, productId,
                                    cusId, reason);

                            if (!result.isSuccess()) {
                                JOptionPane.showMessageDialog(dialog, result.getMessage(), "Thất bại",
                                        JOptionPane.ERROR_MESSAGE);
                                return; // Dừng nếu lỗi (quá hạn bảo hành, v.v..)
                            }

                            // Báo thành công (UI chạy trước)
                            JOptionPane.showMessageDialog(dialog,
                                    "Đã gửi yêu cầu bảo hành cho:\n- SP: " + d.getProductName() + "\n- Lỗi: " + reason,
                                    "Tiếp nhận", JOptionPane.INFORMATION_MESSAGE);

                            btnWarranty.setText("Đã yêu cầu");
                            btnWarranty.setEnabled(false);
                            btnWarranty.setBackground(Color.LIGHT_GRAY);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(dialog, "Có lỗi xảy ra: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            } else {
                btnRate.setVisible(false); // Ẩn nếu không phải khách hàng
                btnWarranty.setVisible(false);
            }

            JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            pnlAction.setBackground(Color.WHITE);
            pnlAction.add(btnWarranty);
            pnlAction.add(btnRate);

            itemPanel.add(lblInfo, BorderLayout.CENTER);
            itemPanel.add(pnlAction, BorderLayout.EAST);
            pnlItems.add(itemPanel);
        }

        dialog.add(new JScrollPane(pnlItems), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    @Override
    public void refresh() {
        loadData();
    }
}
