package com.keycapstore.utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ModernTable extends JTable {

    public ModernTable(DefaultTableModel model) {
        super(model);
        setShowVerticalLines(false);
        setRowHeight(35);
        setFillsViewportHeight(true);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setSelectionBackground(new Color(230, 230, 230));
        setSelectionForeground(Color.BLACK);

        getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        getTableHeader().setBackground(new Color(62, 54, 46));
        getTableHeader().setForeground(Color.WHITE);
        getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        getTableHeader().setPreferredSize(new Dimension(0, 40));

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {

                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }
}