package com.keycapstore.gui;

import com.keycapstore.bus.CategoryBUS;
import com.keycapstore.bus.ProductBUS;
import com.keycapstore.model.Category;
import com.keycapstore.model.Employee;
import com.keycapstore.model.Product;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class ProductPanel extends JPanel implements Refreshable {

    private Employee currentUser;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtPrice, txtStock, txtImage, txtOrigin, txtEntryPrice;
    private JComboBox<Category> cbCategory;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private JTextField txtSearch;
    private JComboBox<String> cbSort;
    private ProductBUS bus;
    private CategoryBUS catBus;
    private int selectedId = -1;
    private DecimalFormat df = new DecimalFormat("#,###");
    private ArrayList<Product> allProducts;

    public ProductPanel(Employee currentUser) {
        this.currentUser = currentUser;
        bus = new ProductBUS();
        catBus = new CategoryBUS();
        allProducts = new ArrayList<>();

        MouseAdapter outsideClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearForm();
            }
        };
        this.addMouseListener(outsideClick);

        setLayout(new BorderLayout(10, 10));
        setBackground(ThemeColor.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.addMouseListener(outsideClick);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHO KEYCAP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(ThemeColor.PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // --- BỘ LỌC VÀ TÌM KIẾM ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        filterPanel.addMouseListener(outsideClick);

        filterPanel.add(createLabel("Tìm kiếm:"));
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập tên hoặc ID...");
        filterPanel.add(txtSearch);

        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(createLabel("Sắp xếp:"));
        cbSort = new JComboBox<>(new String[] { "Tất cả (Mặc định)", "Giá: Thấp -> Cao", "Giá: Cao -> Thấp",
                "Tồn kho: Thấp -> Cao", "Tồn kho: Cao -> Thấp", "Loại: Switch", "Loại: Keycap Set",
                "Loại: Artisan Keycap" });
        cbSort.setFocusable(false);
        filterPanel.add(cbSort);

        topPanel.add(filterPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        formPanel.addMouseListener(outsideClick);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addInput(formPanel, gbc, "Tên Keycap:", txtName = new JTextField());

        gbc.gridy++;
        formPanel.add(createLabel("Danh mục:"), gbc);
        gbc.gridy++;
        cbCategory = new JComboBox<>();
        cbCategory.setBackground(Color.WHITE);
        cbCategory.setPreferredSize(new Dimension(280, 30));
        formPanel.add(cbCategory, gbc);

        addInput(formPanel, gbc, "Giá bán (VNĐ):", txtPrice = new JTextField());
        addInput(formPanel, gbc, "Giá nhập (VNĐ):", txtEntryPrice = new JTextField());
        addInput(formPanel, gbc, "Xuất xứ:", txtOrigin = new JTextField());
        addInput(formPanel, gbc, "Số lượng tồn kho:", txtStock = new JTextField());
        addInput(formPanel, gbc, "Tên file ảnh (VD: gmk.png):", txtImage = new JTextField());

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        btnPanel.setOpaque(false);
        btnAdd = createButton("THÊM", ThemeColor.SUCCESS);
        btnUpdate = createButton("SỬA", ThemeColor.INFO);
        btnDelete = createButton("XÓA", ThemeColor.DANGER);
        btnClear = createButton("LÀM MỚI", ThemeColor.WARNING);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 10, 10);
        formPanel.add(btnPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(340, 0));
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColor.PRIMARY, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.EAST);

        String[] headers = { "ID", "Tên Sản Phẩm", "Danh Mục", "Giá Bán", "Tồn Kho", "Xuất xứ", "Hình Ảnh" };
        model = new DefaultTableModel(headers, 0);

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    try {
                        int modelRow = convertRowIndexToModel(row);
                        Object stockObj = getModel().getValueAt(modelRow, 4);

                        int stock = 0;
                        if (stockObj instanceof Number) {
                            stock = ((Number) stockObj).intValue();
                        } else if (stockObj != null) {
                            stock = Integer.parseInt(stockObj.toString());
                        }

                        if (stock < 5) {
                            c.setBackground(new Color(255, 200, 200));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };
        table.setRowHeight(30);
        table.getTableHeader().setBackground(ThemeColor.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionBackground(new Color(255, 224, 178));
        table.setSelectionForeground(Color.BLACK);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) == -1) {
                    clearForm();
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    try {
                        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                        txtName.setText(model.getValueAt(row, 1).toString());

                        String catName = model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "";
                        for (int i = 0; i < cbCategory.getItemCount(); i++) {
                            if (cbCategory.getItemAt(i).getName().equals(catName)) {
                                cbCategory.setSelectedIndex(i);
                                break;
                            }
                        }

                        String rawPrice = model.getValueAt(row, 3).toString().replace(",", "").replace(".", "")
                                .replace(" ₫", "").trim();
                        txtPrice.setText(rawPrice);
                        txtEntryPrice.setText("0");
                        txtStock.setText(model.getValueAt(row, 4).toString());
                        txtOrigin.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
                        txtImage.setText(model.getValueAt(row, 6).toString());

                        btnAdd.setEnabled(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        setupActions();
        loadCategories();
        loadData();
    }

    @Override
    public void refresh() {
        loadCategories();
        loadData();
    }

    private int getCategoryRank(String categoryName) {
        if (categoryName == null)
            return 99;
        switch (categoryName) {
            case "Switch":
                return 1;
            case "Keycap Set":
                return 2;
            case "Artisan Keycap":
                return 3;
            default:
                return 4;
        }
    }

    private void loadCategories() {
        cbCategory.removeAllItems();
        ArrayList<Category> list = catBus.getAllCategories();
        for (Category c : list) {
            cbCategory.addItem(c);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(ThemeColor.PRIMARY);
        return lbl;
    }

    private void addInput(JPanel p, GridBagConstraints gbc, String label, Component c) {
        gbc.gridy++;
        p.add(createLabel(label), gbc);
        gbc.gridy++;
        c.setPreferredSize(new Dimension(280, 30));
        p.add(c, gbc);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadData() {
        allProducts = bus.getAllProducts();
        updateTable();
    }

    private void updateTable() {
        String keyword = txtSearch.getText().toLowerCase();
        int sortType = cbSort.getSelectedIndex();
        String selectedItem = (String) cbSort.getSelectedItem();

        ArrayList<Product> filtered = (ArrayList<Product>) allProducts.stream()
                .filter(p -> {
                    boolean matchKeyword = p.getName().toLowerCase().contains(keyword)
                            || String.valueOf(p.getId()).contains(keyword);
                    boolean matchCategory = true;
                    if (selectedItem != null && selectedItem.startsWith("Loại: ")) {
                        String targetCat = selectedItem.substring(6);
                        matchCategory = targetCat.equals(p.getCategoryName());
                    }
                    return matchKeyword && matchCategory;
                }).collect(Collectors.toList());

        Comparator<Product> comparator = Comparator.comparingInt(Product::getId);
        switch (sortType) {
            case 1:
                comparator = Comparator.comparingDouble(Product::getPrice);
                break;
            case 2:
                comparator = Comparator.comparingDouble(Product::getPrice).reversed();
                break;
            case 3:
                comparator = Comparator.comparingInt(Product::getStock);
                break;
            case 4:
                comparator = Comparator.comparingInt(Product::getStock).reversed();
                break;
        }
        filtered.sort(comparator);

        model.setRowCount(0);
        for (Product p : filtered) {
            model.addRow(new Object[] {
                    p.getId(), p.getName(), p.getCategoryName(), df.format(p.getPrice()), p.getStock(),
                    p.getOrigin(), p.getImage()
            });
        }
    }

    private void setupActions() {
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updateTable();
            }
        });
        cbSort.addActionListener(e -> updateTable());

        btnAdd.addActionListener(e -> {
            try {
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không xác định được người dùng hiện tại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (txtName.getText().trim().isEmpty() || txtPrice.getText().trim().isEmpty()
                        || txtEntryPrice.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ: Tên, Giá bán, Giá nhập và Số lượng!");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thêm sản phẩm này vào kho?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Product p = new Product();
                    p.setName(txtName.getText().trim());
                    p.setPrice(Double.parseDouble(txtPrice.getText().trim()));
                    p.setStock(Integer.parseInt(txtStock.getText().trim()));
                    p.setImage(txtImage.getText().trim());
                    p.setOrigin(txtOrigin.getText().trim());

                    if (cbCategory.getSelectedItem() != null) {
                        Category c = (Category) cbCategory.getSelectedItem();
                        p.setCategoryId(c.getCategoryId());
                    }

                    double entryPrice = Double.parseDouble(txtEntryPrice.getText().trim());

                    String note = "Nhập mới";
                    if (bus.addProduct(p, currentUser.getEmployeeId(), entryPrice, note)) {
                        JOptionPane.showMessageDialog(this, "Nhập kho thành công!");
                        loadData();
                        clearForm();
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng: Giá và Số lượng phải là số hợp lệ!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedId == -1)
                return;

            try {
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không xác định được người dùng hiện tại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int newStock = Integer.parseInt(txtStock.getText().trim());
                int oldStock = 0;
                for (Product p : allProducts) {
                    if (p.getId() == selectedId) {
                        oldStock = p.getStock();
                        break;
                    }
                }

                String note = "";
                if (newStock < oldStock) {
                    note = JOptionPane.showInputDialog(this,
                            "Phát hiện giảm số lượng tồn kho.\nVui lòng nhập lý do (Bắt buộc):");
                    if (note == null || note.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Bạn phải nhập lý do khi giảm số lượng!", "Cảnh báo",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else if (newStock > oldStock) {
                    note = "Cập nhật tăng số lượng";
                } else {
                    note = "Cập nhật thông tin";
                }

                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn cập nhật thông tin sản phẩm này?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Product p = new Product();
                    p.setId(selectedId);
                    p.setName(txtName.getText().trim());
                    p.setPrice(Double.parseDouble(txtPrice.getText().trim()));
                    p.setStock(newStock);
                    p.setImage(txtImage.getText().trim());
                    p.setOrigin(txtOrigin.getText().trim());

                    if (cbCategory.getSelectedItem() != null) {
                        Category c = (Category) cbCategory.getSelectedItem();
                        p.setCategoryId(c.getCategoryId());
                    }
                    double entryPrice = Double.parseDouble(txtEntryPrice.getText().trim());

                    if (bus.updateProduct(p, currentUser.getEmployeeId(), entryPrice, note)) {
                        JOptionPane.showMessageDialog(this, "Cập nhật kho xong!");
                        loadData();
                        clearForm();
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng: Giá và Số lượng phải là số hợp lệ!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedId == -1)
                return;

            try {
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không xác định được người dùng hiện tại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String note = JOptionPane.showInputDialog(this, "Vui lòng nhập lý do XÓA sản phẩm (Bắt buộc):");
                if (note == null || note.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Bạn phải nhập lý do để xóa sản phẩm!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this, "Chắc chắn hủy mã Keycap này?", "Xác nhận",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (bus.deleteProduct(selectedId, currentUser.getEmployeeId(), note)) {
                        JOptionPane.showMessageDialog(this, "Đã xóa khỏi kho!");
                        loadData();
                        clearForm();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> clearForm());
    }

    private void clearForm() {
        txtName.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        txtOrigin.setText("");
        txtEntryPrice.setText("");
        txtImage.setText("");
        if (cbCategory.getItemCount() > 0)
            cbCategory.setSelectedIndex(0);
        selectedId = -1;
        btnAdd.setEnabled(true);
        table.clearSelection();
    }
}
