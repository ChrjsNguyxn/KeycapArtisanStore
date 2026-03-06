package com.keycapstore.gui;

import com.keycapstore.model.*;
import com.keycapstore.dao.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.io.File;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class ProductGUI extends JFrame {
    
    private final Color PRIMARY_DARK = new Color(62, 54, 46);
    private final Color CREAM_LIGHT = new Color(228, 220, 207);
    private final Color TAUPE_GREY = new Color(153, 143, 133);
    private final Color GLASS_WHITE = new Color(255, 252, 245);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color INFO_BLUE = new Color(52, 152, 219);
    private final Color DANGER_RED = new Color(231, 76, 60);
    private final Color TEXT_PRIMARY = new Color(51, 51, 51);

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtName, txtPrice, txtStock, txtProfile, txtMaterial, txtImageUrl,txtSearch;
    private JTextArea txtDescription;
    private JComboBox<CategoryDTO> cbCategory;
    private JComboBox<MakerDTO> cbMaker;
    private JComboBox<String> cbFilterCategory;
    private JComboBox<String> cbFilterMaker;
    private JComboBox<String> cbFilterStatus;

private TableRowSorter<DefaultTableModel> sorter;

    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private MakerDAO makerDAO = new MakerDAO();

    private int selectedId = -1;

    public ProductGUI() {

        setTitle("PRODUCT MANAGEMENT");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(CREAM_LIGHT);

        initComponents();
        loadCategories();
        loadMakers();
        loadTable();
        loadFilterData();
    }

    private void initComponents() {

        model = new DefaultTableModel(new String[]{
            "Ảnh",
            "ID", "Danh Mục", "Hãng", "Tên Sản Phẩm", "Giá Tiền",
            "Số Lượng", "Profile", "Chất Liệu", "Trạng Thái"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) {
                    return ImageIcon.class;
                }
                return Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(60);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        
        table.setBackground(GLASS_WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(TAUPE_GREY);
        
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        

        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(TAUPE_GREY, 1));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
        topPanel.setBackground(CREAM_LIGHT);

        txtSearch = new JTextField(15);

        cbFilterCategory = new JComboBox<>();
        cbFilterMaker = new JComboBox<>();
        cbFilterStatus = new JComboBox<>();

        topPanel.add(new JLabel("Danh Mục:"));
        topPanel.add(cbFilterCategory);

        topPanel.add(new JLabel("Hãng:"));
        topPanel.add(cbFilterMaker);

        topPanel.add(new JLabel("Trạng thái:"));
        topPanel.add(cbFilterStatus);

        topPanel.add(new JLabel("Tìm Kiếm:"));
        topPanel.add(txtSearch);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){

        public void insertUpdate(javax.swing.event.DocumentEvent e){ applyFilter(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e){ applyFilter(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e){ applyFilter(); }

        });

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

      
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(GLASS_WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TAUPE_GREY, 1),
                "Product Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(15);
        txtPrice = new JTextField(10);
        txtStock = new JTextField(10);
        txtProfile = new JTextField(10);
        txtMaterial = new JTextField(10);
        txtImageUrl = new JTextField(15);
        txtImageUrl.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        chooseImage();
    }
});
        txtDescription = new JTextArea(3, 15);

        cbCategory = new JComboBox<>();
        cbMaker = new JComboBox<>();

        int y = 0;
        addField(panel, gbc, y++, "Danh Mục:", cbCategory);
        addField(panel, gbc, y++, "Hãng:", cbMaker);
        addField(panel, gbc, y++, "Tên Sản Phẩm:", txtName);
        addField(panel, gbc, y++, "Giá Tiền:", txtPrice);
        addField(panel, gbc, y++, "Số Lượng:", txtStock);
        addField(panel, gbc, y++, "Profile:", txtProfile);
        addField(panel, gbc, y++, "Chất Liệu:", txtMaterial);
        addField(panel, gbc, y++, "Ảnh:", txtImageUrl);

        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(txtDescription), gbc);

        // ===== BUTTON PANEL =====
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(GLASS_WHITE);

        JButton btnAdd = createButton("Add", SUCCESS_GREEN);
        JButton btnUpdate = createButton("Update", INFO_BLUE);
        JButton btnDelete = createButton("Delete", DANGER_RED);
        JButton btnClear = createButton("Clear", PRIMARY_DARK);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy = ++y;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        add(panel, BorderLayout.EAST);

       
        table.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {

      int row = table.getSelectedRow();

        if (row >= 0) {

            int modelRow = table.convertRowIndexToModel(row);

            selectedId = (int) model.getValueAt(modelRow, 1);

            ProductDTO p = productDAO.getById(selectedId);

            txtName.setText(p.getName());
            txtPrice.setText(String.valueOf(p.getPrice()));
            txtStock.setText(String.valueOf(p.getStockQuantity()));
            txtProfile.setText(p.getProfile());
            txtMaterial.setText(p.getMaterial());
            txtDescription.setText(p.getDescription());

            // load image
            ProductImageDAO imageDAO = new ProductImageDAO();
            List<ProductImageDTO> imgs = imageDAO.getImagesByProductId(selectedId);

            if (!imgs.isEmpty()) {
                txtImageUrl.setText(imgs.get(0).getImageUrl());
            } else {
                txtImageUrl.setText("");
            }


            selectCategory(p.getCategoryId());
            selectMaker(p.getMakerId());
        }
    }
});
        cbCategory.addActionListener(e -> handleCategorySelection());
        cbMaker.addActionListener(e -> handleMakerSelection());
        cbFilterCategory.addActionListener(e -> applyFilter());
        cbFilterMaker.addActionListener(e -> applyFilter());
        cbFilterStatus.addActionListener(e -> applyFilter());
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearForm());
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
            int y, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private void loadCategories() {

    cbCategory.removeAllItems();

    for (CategoryDTO c : categoryDAO.getAll()) {
        cbCategory.addItem(c);
    }

  
    cbCategory.addItem(new CategoryDTO(-1, "➕ Thêm Danh Mục Mới",""));
}

    private void loadMakers() {
    cbMaker.removeAllItems();
    for (MakerDTO m : makerDAO.getAll()) {
        cbMaker.addItem(m);
    }

    cbMaker.addItem(new MakerDTO(-1, "➕ Thêm Hãng Mới", "", ""));
}

    private void loadTable() {
        model.setRowCount(0);
        List<ProductDTO> list = productDAO.getAll();
        ProductImageDAO imageDAO = new ProductImageDAO();

        for (ProductDTO p : list) {

            ImageIcon icon = null;

            try {
                List<ProductImageDTO> images
                        = imageDAO.getImagesByProductId(p.getProductId());

                if (!images.isEmpty()) {
                    String path = images.get(0).getImageUrl();

                    if (path.startsWith("http")) {
                        icon = new ImageIcon(new java.net.URL(path));
                    } else {
                        icon = new ImageIcon(path);
                    }

                    Image img = icon.getImage()
                            .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(img);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            model.addRow(new Object[]{
                icon,
                p.getProductId(),
                p.getCategoryName(),
                p.getMakerName(),
                p.getName(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getProfile(),
                p.getMaterial(),
                p.getStockQuantity() > 0 ? "Còn hàng" : "Hết hàng"
            });
        }
    }
    private boolean isDuplicateProduct(ProductDTO newProduct) {

    List<ProductDTO> list = productDAO.getAll();

    for (ProductDTO p : list) {

        if (p.getCategoryId() == newProduct.getCategoryId()
                && p.getMakerId() == newProduct.getMakerId()
                && p.getName().equalsIgnoreCase(newProduct.getName())
                && p.getProfile().equalsIgnoreCase(newProduct.getProfile())
                && p.getMaterial().equalsIgnoreCase(newProduct.getMaterial())) {

            return true;
        }
    }

    return false;
}

    private void addProduct() {

    try {

        CategoryDTO c = (CategoryDTO) cbCategory.getSelectedItem();
        MakerDTO m = (MakerDTO) cbMaker.getSelectedItem();

        ProductDTO p = new ProductDTO();

        p.setName(txtName.getText().trim());
        p.setPrice(Double.parseDouble(txtPrice.getText()));
        p.setStockQuantity(Integer.parseInt(txtStock.getText()));
        p.setProfile(txtProfile.getText().trim());
        p.setMaterial(txtMaterial.getText().trim());
        p.setDescription(txtDescription.getText());

        p.setCategoryId(c.getCategoryId());
        p.setMakerId(m.getMakerId());

      
        if (isDuplicateProduct(p)) {
            JOptionPane.showMessageDialog(this,
                    "Sản phẩm đã tồn tại với cùng Category, Maker, Name, Profile, Material!");
            return;
        }

        int newId = productDAO.insertAndGetId(p);

        if (newId > 0) {

            if (!txtImageUrl.getText().isEmpty()) {

                ProductImageDAO imageDAO = new ProductImageDAO();

                ProductImageDTO img = new ProductImageDTO(
                        0,
                        newId,
                        txtImageUrl.getText(),
                        true
                );

                imageDAO.insert(img);
            }

            JOptionPane.showMessageDialog(this, "Thêm thành công!");

            loadTable();
            clearForm();
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!");
    }
}

    private void updateProduct() {

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm cần sửa!");
            return;
        }

        try {
            ProductDTO p = new ProductDTO();

            p.setProductId(selectedId);
            p.setName(txtName.getText());
            p.setPrice(Double.parseDouble(txtPrice.getText()));
            p.setStockQuantity(Integer.parseInt(txtStock.getText()));
            p.setProfile(txtProfile.getText());
            p.setMaterial(txtMaterial.getText());
            p.setDescription(txtDescription.getText());

            CategoryDTO c = (CategoryDTO) cbCategory.getSelectedItem();
            MakerDTO m = (MakerDTO) cbMaker.getSelectedItem();

            p.setCategoryId(c.getCategoryId());
            p.setMakerId(m.getMakerId());

            boolean result = productDAO.update(p);

            if (result) {

                // ===== XỬ LÝ ẢNH =====
                ProductImageDAO imageDAO = new ProductImageDAO();

                // Xóa ảnh cũ
                imageDAO.deleteByProductId(selectedId);

                // Thêm ảnh mới nếu có
                if (!txtImageUrl.getText().trim().isEmpty()) {
                    ProductImageDTO img = new ProductImageDTO(
                            0,
                            selectedId,
                            txtImageUrl.getText().trim(),
                            true
                    );
                    imageDAO.insert(img);
                }

                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadTable();
                clearForm();

            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!");
        }
    }

    private void deleteProduct() {

    if (selectedId == -1) {
        JOptionPane.showMessageDialog(this, "Chọn sản phẩm cần xóa!");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn chắc chắn muốn xóa sản phẩm này?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {

        boolean result = productDAO.delete(selectedId);

        if (result) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadTable();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại!");
        }
    }
}

    private void clearForm() {

    txtName.setText("");
    txtPrice.setText("");
    txtStock.setText("");
    txtProfile.setText("");
    txtMaterial.setText("");
    txtDescription.setText("");
    txtImageUrl.setText("");

    if (cbCategory.getItemCount() > 0)
        cbCategory.setSelectedIndex(0);

    if (cbMaker.getItemCount() > 0)
        cbMaker.setSelectedIndex(0);

    selectedId = -1;

    table.clearSelection();
}
    private void chooseImage() {

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Chọn ảnh sản phẩm");

    int result = chooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {

        File file = chooser.getSelectedFile();

        String path = file.getAbsolutePath();

        txtImageUrl.setText(path);
    }
}
    private void handleCategorySelection() {

    CategoryDTO selected = (CategoryDTO) cbCategory.getSelectedItem();

    if (selected != null && selected.getCategoryId() == -1) {

        JTextField txtName = new JTextField();
        JTextField txtDescription = new JTextField();

        Object[] message = {
            "Tên Danh Mục:", txtName,
            "Mô Tả:", txtDescription
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Thêm Danh Mục Mới",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            String name = txtName.getText().trim();
            String description = txtDescription.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên Danh Mục không được để trống!");
                return;
            }

            CategoryDTO newCategory = new CategoryDTO();
            newCategory.setName(name);
            newCategory.setDescription(description);

            boolean result = categoryDAO.insert(newCategory);

            if (result) {
                JOptionPane.showMessageDialog(this, "Thêm Danh Mục thành công!");
                loadCategories();
            }
        }

        cbCategory.setSelectedIndex(0);
    }
}
    private void handleMakerSelection() {

    MakerDTO selected = (MakerDTO) cbMaker.getSelectedItem();

    if (selected != null && selected.getMakerId() == -1) {

        JTextField txtName = new JTextField();
        JTextField txtOrigin = new JTextField();
        JTextField txtWebsite = new JTextField();

        Object[] message = {
            "Tên Hãng:", txtName,
            "Xuất Xứ:", txtOrigin,
            "Website:", txtWebsite
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Thêm Hãng mới",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            String name = txtName.getText().trim();
            String origin = txtOrigin.getText().trim();
            String website = txtWebsite.getText().trim();

            if (!name.isEmpty()) {

                MakerDTO newMaker = new MakerDTO();
                newMaker.setName(name);
                newMaker.setOrigin(origin);
                newMaker.setWebsite(website);

                boolean result = makerDAO.insert(newMaker);

                if (result) {
                    JOptionPane.showMessageDialog(this, "Thêm Hãng thành công!");
                    loadMakers();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm Hãng thất bại!");
                }
            }
        }

        cbMaker.setSelectedIndex(0);
    }
}
    private void selectCategory(int categoryId) {

    for (int i = 0; i < cbCategory.getItemCount(); i++) {

        CategoryDTO c = cbCategory.getItemAt(i);

        if (c.getCategoryId() == categoryId) {
            cbCategory.setSelectedIndex(i);
            break;
        }
    }
}

private void selectMaker(int makerId) {

    for (int i = 0; i < cbMaker.getItemCount(); i++) {

        MakerDTO m = cbMaker.getItemAt(i);

        if (m.getMakerId() == makerId) {
            cbMaker.setSelectedIndex(i);
            break;
        }
    }
}private void loadFilterData(){

    cbFilterCategory.removeAllItems();
    cbFilterMaker.removeAllItems();
    cbFilterStatus.removeAllItems();

    cbFilterCategory.addItem("All");
    for(CategoryDTO c : categoryDAO.getAll()){
        cbFilterCategory.addItem(c.getName());
    }

    cbFilterMaker.addItem("All");
    for(MakerDTO m : makerDAO.getAll()){
        cbFilterMaker.addItem(m.getName());
    }

    cbFilterStatus.addItem("All");
    cbFilterStatus.addItem("Còn hàng");
    cbFilterStatus.addItem("Hết hàng");
}

 private void applyFilter(){

    if(cbFilterCategory.getSelectedItem() == null ||
       cbFilterMaker.getSelectedItem() == null ||
       cbFilterStatus.getSelectedItem() == null){
        return;
    }

    RowFilter<DefaultTableModel,Object> rf = new RowFilter<DefaultTableModel,Object>(){

        public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry){

            String category = cbFilterCategory.getSelectedItem().toString();
            String maker = cbFilterMaker.getSelectedItem().toString();
            String status = cbFilterStatus.getSelectedItem().toString();
            String search = txtSearch.getText().toLowerCase();

            String tableCategory = entry.getStringValue(2);
            String tableMaker = entry.getStringValue(3);
            String tableName = entry.getStringValue(4).toLowerCase();
            String tableStatus = entry.getStringValue(9);

            if(!category.equals("All") && !tableCategory.equals(category))
                return false;

            if(!maker.equals("All") && !tableMaker.equals(maker))
                return false;

            if(!status.equals("All") && !tableStatus.equals(status))
                return false;

            if(!search.isEmpty() && !tableName.contains(search))
                return false;

            return true;
        }
    };

    sorter.setRowFilter(rf);
}
    public static void main(String[] args) {
        new ProductGUI().setVisible(true);
    }
}
