package com.keycapstore.gui;

import com.keycapstore.bus.StatisticalBUS;
import com.keycapstore.utils.DashboardCard;
import com.keycapstore.utils.ThemeColor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardPanel extends JPanel implements Refreshable {

        private StatisticalBUS statsBus;
        private Consumer<String> onNavigate; // Callback để báo MainFrame chuyển menu

        public DashboardPanel() {
                this(null);
        }

        public DashboardPanel(Consumer<String> onNavigate) {
                statsBus = new StatisticalBUS();
                this.onNavigate = onNavigate;
                setLayout(new BorderLayout(20, 20));
                setBackground(ThemeColor.BG_LIGHT);
                setBorder(new EmptyBorder(20, 20, 20, 20));

                initUI();
        }

        @Override
        public void refresh() {
                initUI();
        }

        private void initUI() {
                removeAll();
                JLabel lblWelcome = new JLabel("Tổng quan hôm nay");
                lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
                lblWelcome.setForeground(ThemeColor.PRIMARY);
                add(lblWelcome, BorderLayout.NORTH);

                double revenue = statsBus.getTodayRevenue();
                int orders = statsBus.getTodayOrderCount();
                int employees = statsBus.getActiveEmployeeCount();
                int lowStock = statsBus.getLowStockCount();

                DecimalFormat df = new DecimalFormat("#,###");
                String revenueStr;
                if (revenue >= 1000000000) {
                        revenueStr = String.format("%.2f Tỷ", revenue / 1000000000);
                } else if (revenue >= 1000000) {
                        revenueStr = String.format("%.2f Tr", revenue / 1000000);
                } else {
                        revenueStr = (revenue == 0) ? "0 ₫" : df.format(revenue) + " ₫";
                }

                JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
                cardsPanel.setOpaque(false);
                cardsPanel.setPreferredSize(new Dimension(0, 140));

                cardsPanel.add(new DashboardCard("Doanh Thu Ngày", revenueStr, "cart.png",
                                new Color(46, 204, 113), new Color(39, 174, 96)));

                DashboardCard cardOrder = new DashboardCard("Đơn Hàng Mới", String.valueOf(orders), "box.png",
                                new Color(52, 152, 219), new Color(41, 128, 185));
                cardOrder.setCursor(new Cursor(Cursor.HAND_CURSOR));
                cardOrder.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                                navigate("HISTORY");
                        }
                });
                cardsPanel.add(cardOrder);

                DashboardCard cardEmp = new DashboardCard("Nhân Sự Online", String.valueOf(employees), "user.png",
                                new Color(243, 156, 18), new Color(211, 84, 0));
                cardEmp.setCursor(new Cursor(Cursor.HAND_CURSOR));
                cardEmp.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                                navigate("EMPLOYEE");
                        }
                });
                cardsPanel.add(cardEmp);

                DashboardCard cardStock = new DashboardCard("Sắp Hết Hàng (<5)", String.valueOf(lowStock),
                                "warning.png",
                                new Color(231, 76, 60), new Color(192, 57, 43));
                cardStock.setCursor(new Cursor(Cursor.HAND_CURSOR));
                cardStock.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                                navigate("PRODUCT");
                        }
                });
                cardsPanel.add(cardStock);

                add(cardsPanel, BorderLayout.CENTER);

                java.util.List<Object[]> chartData = statsBus.getRevenueLast7Days();

                com.keycapstore.utils.SimpleBarChart chart = new com.keycapstore.utils.SimpleBarChart(chartData);
                chart.setPreferredSize(new Dimension(0, 300));

                add(chart, BorderLayout.SOUTH);

                revalidate();
                repaint();
        }

        // Hàm chuyển trang trong CardLayout
        private void navigate(String cardName) {
                if (onNavigate != null) {
                        onNavigate.accept(cardName);
                }
        }
}