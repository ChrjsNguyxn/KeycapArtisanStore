package com.keycapstore.utils;

import com.keycapstore.gui.ThemeColor;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;

public class SimpleBarChart extends JPanel {
    private List<Object[]> data;

    public SimpleBarChart(List<Object[]> data) {
        this.data = data;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("BIỂU ĐỒ DOANH THU 7 NGÀY QUA"));
        setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty())
            return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Điều chỉnh lề để hiển thị nhãn trục Y
        int paddingLeft = 60;
        int paddingBottom = 40;
        int paddingTop = 40;
        int paddingRight = 30;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        double maxVal = 0;
        for (Object[] row : data)
            maxVal = Math.max(maxVal, (double) row[1]);

        if (maxVal == 0)
            maxVal = 1; // Tránh chia cho 0
        maxVal = maxVal * 1.1; // Thêm khoảng trống ở trên đỉnh biểu đồ

        // --- VẼ LƯỚI NGANG & TRỤC Y ---
        int gridLines = 5;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        for (int i = 0; i <= gridLines; i++) {
            int y = paddingTop + chartHeight - (int) ((i * 1.0 / gridLines) * chartHeight);

            // Vẽ dòng kẻ ngang mờ
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(paddingLeft, y, width - paddingRight, y);

            // Vẽ nhãn giá trị trục Y (Ví dụ: 1.5M, 500k)
            double val = (maxVal * i) / gridLines;
            String label = (val >= 1000000) ? String.format("%.1fM", val / 1000000)
                    : (val >= 1000) ? String.format("%.0fk", val / 1000) : String.format("%.0f", val);

            g2.setColor(Color.GRAY);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, paddingLeft - labelWidth - 8, y + 4);
        }

        // --- VẼ CỘT ---
        int barWidth = Math.min(50, (chartWidth / data.size()) - 20); // Giới hạn độ rộng cột tối đa là 50px
        int gap = (chartWidth / data.size());
        int startX = paddingLeft + (gap - barWidth) / 2;

        DecimalFormat df = new DecimalFormat("#,###");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            java.util.Date date = (java.util.Date) row[0]; // Dùng java.util.Date để linh hoạt hơn
            double value = (double) row[1];

            int barHeight = (int) ((value / maxVal) * chartHeight);
            int x = startX + (i * gap);
            int y = paddingTop + chartHeight - barHeight;

            // 1. Bóng đổ (Shadow)
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(x + 3, y + 3, barWidth, barHeight, 10, 10);

            // 2. Gradient bắt mắt (Teal - Blue)
            GradientPaint gradient = new GradientPaint(
                    x, height - paddingBottom, new Color(0, 150, 136), // Dark Teal ở dưới
                    x, y, new Color(72, 201, 176) // Light Teal ở trên
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

            // 3. Giá trị trên đầu cột
            g2.setColor(new Color(60, 60, 60));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valStr = df.format(value / 1000) + "k";
            int strWidth = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, x + (barWidth - strWidth) / 2, y - 5);

            // 4. Ngày tháng ở dưới
            g2.setColor(new Color(100, 100, 100));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String dateStr = sdf.format(date);
            int dateWidth = g2.getFontMetrics().stringWidth(dateStr);
            g2.drawString(dateStr, x + (barWidth - dateWidth) / 2, height - paddingBottom + 18);
        }

        // Đường kẻ trục hoành
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom);
    }
}