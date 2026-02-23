package com.keycapstore.gui.components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║             TableModel - Component Dùng Chung         ║
 * ║           Keyforge Artisan Store - Design System         ║
 * ╚══════════════════════════════════════════════════════════╝
 *
 * Gồm 2 class:
 *   1. TableModel  - Model với chức năng filter/search tích hợp
 *   2. TableModel.StyledTable - JTable đã style sẵn theo Design System
 *
 * ─────────────────────────────────────────────────────────────
 * CÁCH DÙNG - ĐƠN GIẢN NHẤT:
 *
 *   String[] headers = {"ID", "Tên SP", "Giá", "Tồn kho", "Trạng thái"};
 *   TableModel model = new TableModel(headers);
 *
 *   // Tạo bảng đã style sẵn
 *   TableModel.StyledTable table = new TableModel.StyledTable(model);
 *   JScrollPane scroll = TableModel.createScrollPane(table);
 *   panel.add(scroll, BorderLayout.CENTER);
 *
 * ─────────────────────────────────────────────────────────────
 * THÊM / XÓA DỮ LIỆU:
 *
 *   // Thêm hàng
 *   model.addRow(new Object[]{1, "Keycap SA", "250,000đ", 50, "Active"});
 *
 *   // Xóa hàng theo index
 *   model.removeRow(selectedRow);
 *
 *   // Xóa toàn bộ
 *   model.clearAll();
 *
 *   // Load lại từ danh sách
 *   List<Object[]> data = new ArrayList<>();
 *   data.add(new Object[]{1, "SP A", "100,000đ", 10, "Active"});
 *   model.loadData(data);
 *
 * ─────────────────────────────────────────────────────────────
 * TÌM KIẾM / LỌC (tích hợp với SearchBox):
 *
 *   searchBox.addSearchListener(keyword -> {
 *       model.filter(keyword);   // Tự động lọc tất cả cột
 *   });
 *
 *   // Hoặc lọc cột cụ thể (index cột)
 *   model.filterByColumn(keyword, 1); // Chỉ lọc cột "Tên SP"
 *
 *   // Bỏ filter, hiện tất cả
 *   model.clearFilter();
 *
 * ─────────────────────────────────────────────────────────────
 * KHÔNG CHO SỬA TRỰC TIẾP TRÊN Ô:
 *
 *   // Mặc định tất cả cột đều KHÔNG editable (readonly)
 *   // Nếu muốn cho phép sửa cột nào:
 *   model.setEditableColumns(2, 3); // Cho phép sửa cột 2 và 3
 *
 * ─────────────────────────────────────────────────────────────
 * ĐỔI MÀU HÀNG THEO ĐIỀU KIỆN:
 *
 *   // Tô màu đỏ nếu cột "Trạng thái" (index 4) = "Banned"
 *   table.setRowColorizer((row, col, value) -> {
 *       if (col == 4 && "Banned".equals(value))  return new Color(255, 200, 200);
 *       if (col == 4 && "Active".equals(value))  return new Color(200, 245, 220);
 *       return null; // null = dùng màu mặc định
 *   });
 */
public class TableModel extends DefaultTableModel {

    // ══════════════════════════════════════════════
    //  DESIGN SYSTEM - BẢNG MÀU CHUẨN
    // ══════════════════════════════════════════════
    static final Color COLOR_PRIMARY_DARK  = new Color(62,  54,  46);   // #3E362E  Header
    static final Color COLOR_CREAM_LIGHT   = new Color(228, 220, 207);  // #E4DCCF  Hàng chẵn
    static final Color COLOR_GLASS_WHITE   = new Color(255, 252, 245);  // #FFFDF5  Hàng lẻ
    static final Color COLOR_TAUPE_GREY    = new Color(153, 143, 133);  // #998F85  Border
    static final Color COLOR_TEXT_PRIMARY  = new Color(51,  51,  51);   // #333333  Text
    static final Color COLOR_SUCCESS_GREEN = new Color(46,  204, 113);  // #2ECC71
    static final Color COLOR_DANGER_RED    = new Color(231, 76,  60);   // #E74C3C
    static final Color COLOR_INFO_BLUE     = new Color(52,  152, 219);  // #3498DB
    static final Color COLOR_SELECTED      = new Color(62,  54,  46, 40); // Selection

    // ══════════════════════════════════════════════
    //  FIELDS
    // ══════════════════════════════════════════════
    private final String[]       columnNames;       // Tên cột cố định
    private List<Object[]>       masterData;        // Dữ liệu gốc đầy đủ (không bị filter)
    private java.util.Set<Integer> editableCols;    // Các cột được phép sửa

    // ══════════════════════════════════════════════
    //  CONSTRUCTORS
    // ══════════════════════════════════════════════

    /**
     * Tạo model với danh sách cột
     * @param columnNames Tên các cột (VD: "ID", "Tên", "Giá")
     */
    public TableModel(String... columnNames) {
        super(columnNames, 0);
        this.columnNames  = columnNames;
        this.masterData   = new ArrayList<>();
        this.editableCols = new java.util.HashSet<>();
    }

    // ══════════════════════════════════════════════
    //  OVERRIDE - Không cho sửa ô (mặc định)
    // ══════════════════════════════════════════════
    @Override
    public boolean isCellEditable(int row, int column) {
        return editableCols.contains(column);
    }

    // ══════════════════════════════════════════════
    //  DATA MANAGEMENT - Quản lý dữ liệu
    // ══════════════════════════════════════════════

    /**
     * Thêm một hàng dữ liệu mới
     * @param rowData Mảng Object[] tương ứng với số cột
     */
    @Override
    public void addRow(Object[] rowData) {
        masterData.add(rowData.clone());
        super.addRow(rowData);
    }

    /**
     * Load toàn bộ dữ liệu từ danh sách (xóa cũ, thêm mới)
     * @param data List<Object[]> - Danh sách các hàng
     */
    public void loadData(List<Object[]> data) {
        clearAll();
        for (Object[] row : data) {
            masterData.add(row.clone());
            super.addRow(row);
        }
    }

    /**
     * Xóa một hàng khỏi bảng VÀ masterData
     * Dùng sau khi lấy selected row index từ JTable
     * @param viewRow Index hàng trên bảng (đã qua filter)
     */
    public void removeDisplayedRow(int viewRow) {
        if (viewRow < 0 || viewRow >= getRowCount()) return;
        // Tìm hàng tương ứng trong masterData (so sánh cột đầu)
        Object keyValue = getValueAt(viewRow, 0);
        masterData.removeIf(row -> row.length > 0 && String.valueOf(row[0]).equals(String.valueOf(keyValue)));
        removeRow(viewRow);
    }

    /**
     * Xóa toàn bộ dữ liệu (cả bảng hiển thị và masterData)
     */
    public void clearAll() {
        masterData.clear();
        setRowCount(0);
    }

    // ══════════════════════════════════════════════
    //  SEARCH / FILTER - Tìm kiếm & Lọc
    // ══════════════════════════════════════════════

    /**
     * Lọc dữ liệu theo từ khóa trên TẤT CẢ cột
     * Dùng với SearchBox.addSearchListener()
     * @param keyword Từ khóa tìm kiếm (không phân biệt hoa thường)
     */
    public void filter(String keyword) {
        setRowCount(0); // Xóa bảng hiển thị
        if (keyword == null || keyword.trim().isEmpty()) {
            // Không có từ khóa -> hiện tất cả
            for (Object[] row : masterData) {
                super.addRow(row);
            }
            return;
        }
        String lower = keyword.toLowerCase().trim();
        for (Object[] row : masterData) {
            for (Object cell : row) {
                if (cell != null && cell.toString().toLowerCase().contains(lower)) {
                    super.addRow(row);
                    break; // Chỉ thêm 1 lần nếu tìm thấy ở bất kỳ cột nào
                }
            }
        }
    }

    /**
     * Lọc dữ liệu theo từ khóa chỉ trên MỘT CỘT cụ thể
     * @param keyword   Từ khóa tìm kiếm
     * @param colIndex  Index cột cần lọc (bắt đầu từ 0)
     */
    public void filterByColumn(String keyword, int colIndex) {
        setRowCount(0);
        if (keyword == null || keyword.trim().isEmpty()) {
            for (Object[] row : masterData) super.addRow(row);
            return;
        }
        String lower = keyword.toLowerCase().trim();
        for (Object[] row : masterData) {
            if (colIndex < row.length && row[colIndex] != null
                    && row[colIndex].toString().toLowerCase().contains(lower)) {
                super.addRow(row);
            }
        }
    }

    /**
     * Bỏ filter, hiển thị lại toàn bộ dữ liệu gốc
     */
    public void clearFilter() {
        filter("");
    }

    // ══════════════════════════════════════════════
    //  CONFIGURATION
    // ══════════════════════════════════════════════

    /**
     * Đặt các cột được phép sửa trực tiếp trên bảng
     * Mặc định: tất cả cột đều KHÔNG editable
     * @param columnIndexes Index các cột muốn cho phép sửa
     */
    public void setEditableColumns(int... columnIndexes) {
        editableCols.clear();
        for (int idx : columnIndexes) editableCols.add(idx);
    }

    /**
     * Lấy số hàng đang hiển thị (sau filter)
     */
    public int getDisplayedRowCount() {
        return getRowCount();
    }

    /**
     * Lấy tổng số hàng trong masterData (trước filter)
     */
    public int getTotalRowCount() {
        return masterData.size();
    }


    // ══════════════════════════════════════════════════════════════════
    //  INNER CLASS: StyledTable
    //  JTable đã được style sẵn theo Design System của Keyforge
    // ══════════════════════════════════════════════════════════════════
    public static class StyledTable extends JTable {

        // Interface để tô màu hàng theo điều kiện
        @FunctionalInterface
        public interface RowColorizer {
            /**
             * Trả về Color để tô nền ô, hoặc null để dùng màu mặc định
             * @param row   Index hàng
             * @param col   Index cột
             * @param value Giá trị ô hiện tại
             */
            Color getColor(int row, int col, Object value);
        }

        private RowColorizer rowColorizer;

        /**
         * Tạo StyledTable với TableModel
         * @param model TableModel đã khởi tạo
         */
        public StyledTable(TableModel model) {
            super(model);
            applyStyle();
        }

        private void applyStyle() {
            // === Bảng ===
            setShowVerticalLines(false);
            setShowHorizontalLines(true);
            setGridColor(new Color(228, 220, 207));       // Đường kẻ màu kem
            setRowHeight(38);
            setFillsViewportHeight(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(COLOR_TEXT_PRIMARY);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Màu khi chọn hàng
            setSelectionBackground(new Color(62, 54, 46, 50));
            setSelectionForeground(COLOR_TEXT_PRIMARY);

            // === Header (tiêu đề cột) ===
            JTableHeader header = getTableHeader();
            header.setFont(new Font("Segoe UI", Font.BOLD, 13));
            header.setBackground(COLOR_PRIMARY_DARK);
            header.setForeground(Color.WHITE);
            header.setReorderingAllowed(false);           // Không cho kéo đổi thứ tự cột
            header.setResizingAllowed(true);
            header.setBorder(BorderFactory.createEmptyBorder());
            header.setPreferredSize(new Dimension(0, 42));

            // Header renderer: căn giữa, màu nâu đậm
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBackground(COLOR_PRIMARY_DARK);
                    setForeground(Color.WHITE);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(90, 80, 70)));
                    setOpaque(true);
                    return this;
                }
            };
            for (int i = 0; i < getColumnCount(); i++) {
                getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }

            // === Cell renderer: zebra striping + custom color ===
            setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {

                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    // Padding trong ô
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));

                    if (!isSelected) {
                        // Kiểm tra custom color trước
                        Color customColor = null;
                        if (rowColorizer != null) {
                            customColor = rowColorizer.getColor(row, column, value);
                        }

                        if (customColor != null) {
                            setBackground(customColor);
                            setForeground(COLOR_TEXT_PRIMARY);
                        } else {
                            // Zebra striping: hàng chẵn kem nhạt, hàng lẻ trắng ngà
                            setBackground(row % 2 == 0 ? COLOR_GLASS_WHITE : COLOR_CREAM_LIGHT);
                            setForeground(COLOR_TEXT_PRIMARY);
                        }
                    } else {
                        setBackground(new Color(62, 54, 46, 60));
                        setForeground(COLOR_TEXT_PRIMARY);
                    }

                    setOpaque(true);
                    return this;
                }
            });
        }

        /**
         * Đặt colorizer để tô màu hàng theo điều kiện nghiệp vụ
         *
         * Ví dụ:
         *   table.setRowColorizer((row, col, value) -> {
         *       if (col == 4 && "Banned".equals(value))  return new Color(255, 220, 210);
         *       if (col == 4 && "Active".equals(value))  return new Color(210, 245, 225);
         *       return null;
         *   });
         *
         * @param colorizer RowColorizer functional interface
         */
        public void setRowColorizer(RowColorizer colorizer) {
            this.rowColorizer = colorizer;
            repaint();
        }

        /**
         * Lấy TableModel đang dùng
         */
        public TableModel getAppModel() {
            return (TableModel) getModel();
        }

        /**
         * Lấy giá trị ô ở cột chỉ định của hàng đang chọn
         * @param colIndex Index cột
         * @return Object value hoặc null nếu không có hàng nào được chọn
         */
        public Object getSelectedValue(int colIndex) {
            int row = getSelectedRow();
            if (row < 0) return null;
            return getValueAt(row, colIndex);
        }

        /**
         * Lấy giá trị cột đầu tiên (thường là ID) của hàng đang chọn
         * @return Object (thường là Integer ID) hoặc null
         */
        public Object getSelectedId() {
            return getSelectedValue(0);
        }
    }


    // ══════════════════════════════════════════════════════════════════
    //  STATIC FACTORY - Tạo JScrollPane đã style sẵn
    // ══════════════════════════════════════════════════════════════════

    /**
     * Tạo JScrollPane đã style sẵn bọc quanh StyledTable
     * Viền màu Taupe Grey, không có viền ngoài trông cứng nhắc
     *
     * Cách dùng:
     *   JScrollPane scroll = TableModel.createScrollPane(table);
     *   panel.add(scroll, BorderLayout.CENTER);
     *
     * @param table StyledTable cần bọc
     * @return JScrollPane đã style
     */
    public static JScrollPane createScrollPane(StyledTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(153, 143, 133, 100), 1));
        scroll.getViewport().setBackground(COLOR_GLASS_WHITE);
        scroll.setBackground(COLOR_GLASS_WHITE);

        // Style scrollbar dọc
        JScrollBar vBar = scroll.getVerticalScrollBar();
        vBar.setUnitIncrement(16);
        vBar.setBackground(COLOR_CREAM_LIGHT);

        return scroll;
    }
}