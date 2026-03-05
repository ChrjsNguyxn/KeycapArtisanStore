package com.keycapstore.utils;

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

        int width = getWidth();
        int height = getHeight();
        int padding = 40; // Lề
        int barWidth = (width - 2 * padding) / data.size() - 20;

        double maxVal = 0;
        for (Object[] row : data)
            maxVal = Math.max(maxVal, (double) row[1]);

        int x = padding;
        DecimalFormat df = new DecimalFormat("#,###");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

        for (Object[] row : data) {
            java.sql.Date date = (java.sql.Date) row[0];
            double value = (double) row[1];

            int barHeight = (int) ((value / maxVal) * (height - 2 * padding - 20));

            g2.setColor(ThemeColor.PRIMARY);
            g2.fillRoundRect(x, height - padding - barHeight, barWidth, barHeight, 10, 10);

            g2.setColor(Color.BLACK);
            String valStr = df.format(value / 1000) + "k";
            int strWidth = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, x + (barWidth - strWidth) / 2, height - padding - barHeight - 5);

            g2.setColor(Color.GRAY);
            String dateStr = sdf.format(date);
            int dateWidth = g2.getFontMetrics().stringWidth(dateStr);
            g2.drawString(dateStr, x + (barWidth - dateWidth) / 2, height - padding + 15);

            x += barWidth + 20;
        }

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(padding - 10, height - padding, width - padding, height - padding);
    }
}