// Modern Login Interface - Integrated System
package com.keycapstore.gui;

import com.keycapstore.bus.CustomerBUS;
import com.keycapstore.bus.EmployeeBUS;
import com.keycapstore.model.Customer;
import com.keycapstore.model.Employee;
import com.keycapstore.utils.EmailSender;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ModernLoginDialog extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;
    private JCheckBox chkRemember;

    private JTextField txtRegUser, txtRegFullName, txtRegEmail, txtRegPhone, txtRegAddress;
    private JPasswordField txtRegPass, txtRegConfirmPass;

    private JTextField txtForgotEmail, txtForgotOTP;
    private JPasswordField txtForgotNewPass;
    private JButton btnSendOTP, btnVerifyOTP, btnSavePass;
    private String currentOTP = "";
    private String verifyingEmail = "";

    private EmployeeBUS employeeBUS;
    private CustomerBUS customerBUS;
    private int pX, pY;

    private final Color COLOR_PRIMARY = new Color(62, 54, 46);
    private final Color COLOR_GLASS = new Color(255, 252, 245, 200);
    private final Color COLOR_INPUT_BG = new Color(62, 54, 46, 30);

    public ModernLoginDialog() {
        this.employeeBUS = new EmployeeBUS();
        this.customerBUS = new CustomerBUS();

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(900, 550);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 900, 550, 30, 30));

        try {
            URL iconURL = getClass().getResource("/keyforge_artisan_logo.png");
            if (iconURL != null)
                setIconImage(ImageIO.read(iconURL));
        } catch (Exception e) {
        }

        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pX = e.getX();
                pY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + (e.getX() - pX), getLocation().y + (e.getY() - pY));
            }
        };

        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(228, 220, 207);
                Color color2 = new Color(153, 143, 133);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        contentPane.setLayout(new GridBagLayout());
        contentPane.addMouseListener(dragListener);
        contentPane.addMouseMotionListener(dragListener);
        setContentPane(contentPane);

        JPanel glassPanel = new JPanel();
        glassPanel.setLayout(new GridLayout(1, 2));
        glassPanel.setPreferredSize(new Dimension(800, 480));
        glassPanel.setOpaque(false);

        JPanel roundedGlass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_GLASS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roundedGlass.setLayout(new BorderLayout());
        roundedGlass.setOpaque(false);
        roundedGlass.add(glassPanel, BorderLayout.CENTER);
        roundedGlass.addMouseListener(dragListener);
        roundedGlass.addMouseMotionListener(dragListener);

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        loadImage(lblImage);
        glassPanel.add(lblImage);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        cardsPanel.add(createLoginPanel(), "LOGIN");
        cardsPanel.add(createRegisterPanel(), "REGISTER");
        cardsPanel.add(createForgotPanel(), "FORGOT");

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);
        rightContainer.add(createControlPanel(), BorderLayout.NORTH);
        rightContainer.add(cardsPanel, BorderLayout.CENTER);

        glassPanel.add(rightContainer);
        contentPane.add(roundedGlass);

        // Tu dong dien thong tin neu da luu truoc do
        loadCredentials();
    }

    private JPanel createControlPanel() {
        JButton btnMinimize = new JButton("-");
        btnMinimize.setFont(new Font("Arial", Font.BOLD, 24));
        btnMinimize.setForeground(COLOR_PRIMARY);
        btnMinimize.setContentAreaFilled(false);
        btnMinimize.setBorderPainted(false);
        btnMinimize.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMinimize.addActionListener(e -> setState(Frame.ICONIFIED));

        JButton btnExit = new JButton("X");
        btnExit.setFont(new Font("Arial", Font.BOLD, 16));
        btnExit.setForeground(COLOR_PRIMARY);
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> System.exit(0));

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnl.setOpaque(false);
        pnl.add(btnMinimize);
        pnl.add(btnExit);
        return pnl;
    }

    private JPanel createLoginPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 40, 5, 40);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel lblTitle = new JLabel("Welcome to ForgeArtisan!");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnl.add(lblTitle, gbc);

        gbc.gridy++;
        pnl.add(new JLabel(" "), gbc);

        gbc.gridy++;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnl.add(lblUser, gbc);

        gbc.gridy++;
        txtLoginUser = new RoundedTextField(20, COLOR_INPUT_BG);
        pnl.add(txtLoginUser, gbc);

        gbc.gridy++;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnl.add(lblPass, gbc);

        gbc.gridy++;
        txtLoginPass = new RoundedPasswordField(20, COLOR_INPUT_BG);
        pnl.add(txtLoginPass, gbc);

        gbc.gridy++;
        JCheckBox chkShowPass = new JCheckBox("Show Password");
        chkShowPass.setOpaque(false);
        chkShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPass.setForeground(COLOR_PRIMARY);
        chkShowPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkShowPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                txtLoginPass.setEchoChar((char) 0);
            else
                txtLoginPass.setEchoChar('•');
        });
        pnl.add(chkShowPass, gbc);

        gbc.gridy++;
        chkRemember = new JCheckBox("Remember Me");
        chkRemember.setOpaque(false);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkRemember.setForeground(COLOR_PRIMARY);
        chkRemember.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnl.add(chkRemember, gbc);

        gbc.gridy++;
        JLabel lblForgot = new JLabel("Forgot Password?");
        lblForgot.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblForgot.setForeground(COLOR_PRIMARY);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.setHorizontalAlignment(SwingConstants.RIGHT);
        lblForgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardsPanel, "FORGOT");
            }
        });
        pnl.add(lblForgot, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 40, 10, 40);
        JButton btnLogin = createButton("LOGIN");
        btnLogin.addActionListener(e -> handleLogin());
        txtLoginUser.addActionListener(e -> handleLogin());
        txtLoginPass.addActionListener(e -> handleLogin());
        pnl.add(btnLogin, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 10, 40);
        JButton btnGuest = new JButton("Continue as Guest");
        btnGuest.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGuest.setForeground(COLOR_PRIMARY);
        btnGuest.setContentAreaFilled(false);
        btnGuest.setBorderPainted(false);
        btnGuest.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuest.addActionListener(e -> openMainFrame(null));
        pnl.add(btnGuest, gbc);

        gbc.gridy++;
        JPanel pnlReg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlReg.setOpaque(false);
        JLabel lblNoAcc = new JLabel("New here? ");
        JLabel lblReg = new JLabel("Create Account");
        lblReg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblReg.setForeground(COLOR_PRIMARY);
        lblReg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblReg.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardsPanel, "REGISTER");
            }
        });
        pnlReg.add(lblNoAcc);
        pnlReg.add(lblReg);
        pnl.add(pnlReg, gbc);

        return pnl;
    }

    private JPanel createRegisterPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 40, 2, 40);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Thêm nút Back ở góc trên cùng
        JButton btnTopBack = new JButton("← Back");
        btnTopBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTopBack.setForeground(COLOR_PRIMARY);
        btnTopBack.setContentAreaFilled(false);
        btnTopBack.setBorderPainted(false);
        btnTopBack.setHorizontalAlignment(SwingConstants.LEFT);
        btnTopBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTopBack.addActionListener(e -> cardLayout.show(cardsPanel, "LOGIN"));
        pnl.add(btnTopBack, gbc);

        gbc.gridy++;
        JLabel lblTitle = new JLabel("CREATE ACCOUNT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnl.add(lblTitle, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        txtRegUser = new RoundedTextField(15, COLOR_INPUT_BG);
        pnl.add(txtRegUser, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Password:"), gbc);
        gbc.gridy++;
        txtRegPass = new RoundedPasswordField(15, COLOR_INPUT_BG);
        pnl.add(txtRegPass, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridy++;
        txtRegConfirmPass = new RoundedPasswordField(15, COLOR_INPUT_BG);
        pnl.add(txtRegConfirmPass, gbc);

        // Thêm nút hiện/ẩn mật khẩu (Con mắt)
        gbc.gridy++;
        JCheckBox chkShowPass = new JCheckBox("Show Password");
        chkShowPass.setOpaque(false);
        chkShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPass.setForeground(COLOR_PRIMARY);
        chkShowPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkShowPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtRegPass.setEchoChar((char) 0);
                txtRegConfirmPass.setEchoChar((char) 0);
            } else {
                txtRegPass.setEchoChar('•');
                txtRegConfirmPass.setEchoChar('•');
            }
        });
        pnl.add(chkShowPass, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Full Name:"), gbc);
        gbc.gridy++;
        txtRegFullName = new RoundedTextField(15, COLOR_INPUT_BG);
        pnl.add(txtRegFullName, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Email:"), gbc);
        gbc.gridy++;
        txtRegEmail = new RoundedTextField(15, COLOR_INPUT_BG);
        pnl.add(txtRegEmail, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Phone (Optional):"), gbc);
        gbc.gridy++;
        txtRegPhone = new RoundedTextField(15, COLOR_INPUT_BG);
        pnl.add(txtRegPhone, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Address (Optional):"), gbc);
        gbc.gridy++;
        RoundedTextField txtAddress = new RoundedTextField(15, COLOR_INPUT_BG);
        txtAddress.setPlaceholder("So nha//Ten Duong//Huyen//Quan");
        txtRegAddress = txtAddress;
        pnl.add(txtRegAddress, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 40, 5, 40);
        JButton btnReg = createButton("REGISTER");
        btnReg.addActionListener(e -> handleRegister());
        pnl.add(btnReg, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 0, 40);
        JButton btnBack = new JButton("Back to Login");
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(cardsPanel, "LOGIN"));
        pnl.add(btnBack, gbc);

        JScrollPane scrollPane = new JScrollPane(pnl);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createForgotPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 40, 5, 40);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Thêm nút Back ở góc trên cùng
        JButton btnTopBack = new JButton("← Back");
        btnTopBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTopBack.setForeground(COLOR_PRIMARY);
        btnTopBack.setContentAreaFilled(false);
        btnTopBack.setBorderPainted(false);
        btnTopBack.setHorizontalAlignment(SwingConstants.LEFT);
        btnTopBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTopBack.addActionListener(e -> cardLayout.show(cardsPanel, "LOGIN"));
        pnl.add(btnTopBack, gbc);

        gbc.gridy++;
        JLabel lblTitle = new JLabel("RESET PASSWORD");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnl.add(lblTitle, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Enter your registered Email:"), gbc);
        gbc.gridy++;
        txtForgotEmail = new RoundedTextField(15, COLOR_INPUT_BG);
        pnl.add(txtForgotEmail, gbc);

        gbc.gridy++;
        btnSendOTP = createButton("SEND OTP");
        btnSendOTP.addActionListener(e -> handleSendOTP());
        pnl.add(btnSendOTP, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("Enter OTP Code:"), gbc);
        gbc.gridy++;
        txtForgotOTP = new RoundedTextField(15, COLOR_INPUT_BG);
        txtForgotOTP.setEnabled(false);
        pnl.add(txtForgotOTP, gbc);

        gbc.gridy++;
        btnVerifyOTP = createButton("VERIFY OTP");
        btnVerifyOTP.setEnabled(false);
        btnVerifyOTP.addActionListener(e -> handleVerifyOTP());
        pnl.add(btnVerifyOTP, gbc);

        gbc.gridy++;
        pnl.add(new JLabel("New Password:"), gbc);
        gbc.gridy++;
        txtForgotNewPass = new RoundedPasswordField(15, COLOR_INPUT_BG);
        txtForgotNewPass.setEnabled(false);
        pnl.add(txtForgotNewPass, gbc);

        gbc.gridy++;
        JCheckBox chkShowPass = new JCheckBox("Show Password");
        chkShowPass.setOpaque(false);
        chkShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPass.setForeground(COLOR_PRIMARY);
        chkShowPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkShowPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                txtForgotNewPass.setEchoChar((char) 0);
            else
                txtForgotNewPass.setEchoChar('•');
        });
        pnl.add(chkShowPass, gbc);

        gbc.gridy++;
        btnSavePass = createButton("SAVE NEW PASSWORD");
        btnSavePass.setEnabled(false);
        btnSavePass.addActionListener(e -> handleSaveNewPass());
        pnl.add(btnSavePass, gbc);

        gbc.gridy++;
        JButton btnBack = new JButton("Back to Login");
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(cardsPanel, "LOGIN"));
        pnl.add(btnBack, gbc);

        JScrollPane scrollPane = new JScrollPane(pnl);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(240, 240, 240));
        btn.setBackground(COLOR_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadCredentials() {
        Preferences prefs = Preferences.userNodeForPackage(ModernLoginDialog.class);
        String u = prefs.get("username", "");
        String p = prefs.get("password", "");
        if (!u.isEmpty()) {
            txtLoginUser.setText(u);
            txtLoginPass.setText(p);
            chkRemember.setSelected(true);
        }
    }

    private void handleLogin() {
        String u = txtLoginUser.getText();
        String p = new String(txtLoginPass.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tài khoản và mật khẩu!");
            return;
        }

        // Xu ly luu mat khau (Remember Me)
        Preferences prefs = Preferences.userNodeForPackage(ModernLoginDialog.class);
        if (chkRemember.isSelected()) {
            prefs.put("username", u);
            prefs.put("password", p);
        } else {
            prefs.remove("username");
            prefs.remove("password");
        }

        Employee emp = employeeBUS.login(u, p);
        if (emp != null) {

            if ("banned".equalsIgnoreCase(emp.getStatus()) || "quit".equalsIgnoreCase(emp.getStatus())) {
                JOptionPane.showMessageDialog(this, "Tài khoản của bạn hiện tại đã bị khóa!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String currentPin = emp.getPinCode();
            if (currentPin == null || currentPin.trim().isEmpty()) {

                openMainFrame(emp);
                this.dispose();
                return;
            }

            if (showPinDialog(emp)) {
                openMainFrame(emp);
                this.dispose();
            }

            return;
        }

        Customer cus = customerBUS.login(u, p);
        if (cus != null) {
            if ("banned".equalsIgnoreCase(cus.getStatus().trim())) {
                JOptionPane.showMessageDialog(this, "Tài khoản của bạn hiện tại đã bị khóa!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            openMainFrame(cus);
            this.dispose();
            return;
        }

        JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Đăng nhập thất bại",
                JOptionPane.ERROR_MESSAGE);
    }

    private void handleRegister() {
        String u = txtRegUser.getText();
        String p = new String(txtRegPass.getPassword());
        String cp = new String(txtRegConfirmPass.getPassword());
        String n = txtRegFullName.getText();
        String e = txtRegEmail.getText();
        String ph = txtRegPhone.getText();
        String addr = txtRegAddress.getText();

        if (u.isEmpty() || p.isEmpty() || cp.isEmpty() || n.isEmpty() || e.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields!");
            return;
        }

        if (!p.equals(cp)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Registration Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String error = customerBUS.checkDuplicate(u, e, ph);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Registration Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer newCus = new Customer(u, p, n, e, ph, addr);
        if (customerBUS.register(newCus)) {
            JOptionPane.showMessageDialog(this, "Registration Successful! Please Login.");
            clearRegisterForm();
            cardLayout.show(cardsPanel, "LOGIN");
        } else {
            JOptionPane.showMessageDialog(this, "Registration Failed!", "Error", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(this, "Registration Failed! Email or Phone number might already be in use.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearRegisterForm() {
        txtRegUser.setText("");
        txtRegPass.setText("");
        txtRegConfirmPass.setText("");
        txtRegFullName.setText("");
        txtRegEmail.setText("");
        txtRegPhone.setText("");
        txtRegAddress.setText("");
    }

    private void handleSendOTP() {
        String email = txtForgotEmail.getText();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email!");
            return;
        }
        if (!customerBUS.isEmailExist(email)) {
            JOptionPane.showMessageDialog(this, "Email not found in system!");
            return;
        }

        currentOTP = EmailSender.generateOTP();
        verifyingEmail = email;

        btnSendOTP.setText("SENDING...");
        btnSendOTP.setEnabled(false);

        new Thread(() -> {
            boolean sent = EmailSender.sendEmail(email, currentOTP);
            SwingUtilities.invokeLater(() -> {
                btnSendOTP.setText("SEND OTP");
                btnSendOTP.setEnabled(true);
                if (sent) {
                    JOptionPane.showMessageDialog(this, "OTP sent to your email!");
                    txtForgotOTP.setEnabled(true);
                    btnVerifyOTP.setEnabled(true);
                    txtForgotEmail.setEditable(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send email. Check internet connection.");
                }
            });
        }).start();
    }

    private void handleVerifyOTP() {
        String inputOTP = txtForgotOTP.getText();
        if (inputOTP.equals(currentOTP)) {
            JOptionPane.showMessageDialog(this, "OTP Verified!");
            txtForgotNewPass.setEnabled(true);
            btnSavePass.setEnabled(true);
            txtForgotOTP.setEditable(false);
            btnVerifyOTP.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid OTP!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveNewPass() {
        String newPass = new String(txtForgotNewPass.getPassword());
        if (newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty!");
            return;
        }
        if (customerBUS.updatePassword(verifyingEmail, newPass)) {
            JOptionPane.showMessageDialog(this, "Password updated successfully!");
            cardLayout.show(cardsPanel, "LOGIN");
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean showPinDialog(Employee emp) {
        String correctPin = emp.getPinCode();
        if (correctPin == null || correctPin.trim().isEmpty()) {
            return true;
        }

        String roleTitle = "Nhân viên";
        if (emp.getRole() != null) {
            switch (emp.getRole().toLowerCase()) {
                case "super_admin":
                    roleTitle = "Super Admin";
                    break;
                case "sales_manager":
                    roleTitle = "Sales Manager";
                    break;
                case "warehouse_manager":
                    roleTitle = "Warehouse Manager";
                    break;
                default:
                    roleTitle = emp.getRole();
                    break;
            }
        }

        PinDialog pinDialog = new PinDialog(this, roleTitle, correctPin);
        pinDialog.setVisible(true);
        return pinDialog.isVerified();
    }

    private void openMainFrame(Object user) {
        try {
            new MainFrame(user).setVisible(true);
            this.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImage(JLabel lblImage) {
        try {
            URL imgURL = getClass().getResource("/keyforge_artisan_logo.png");
            if (imgURL != null) {
                BufferedImage originalImage = ImageIO.read(imgURL);
                int type = (originalImage.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                int targetW = 320;
                int targetH = 320;
                double ratio = Math.max((double) targetW / originalImage.getWidth(),
                        (double) targetH / originalImage.getHeight());
                int w = (int) (originalImage.getWidth() * ratio);
                int h = (int) (originalImage.getHeight() * ratio);
                BufferedImage resizedImg = originalImage;
                int prevW = resizedImg.getWidth();
                int prevH = resizedImg.getHeight();
                while (prevW > w * 2 || prevH > h * 2) {
                    prevW /= 2;
                    prevH /= 2;
                    if (prevW < w)
                        prevW = w;
                    if (prevH < h)
                        prevH = h;
                    BufferedImage temp = new BufferedImage(prevW, prevH, type);
                    Graphics2D g2 = temp.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(resizedImg, 0, 0, prevW, prevH, null);
                    g2.dispose();
                    resizedImg = temp;
                }
                BufferedImage tempImg = new BufferedImage(w, h, type);
                Graphics2D g2 = tempImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(resizedImg, 0, 0, w, h, null);
                g2.dispose();
                int x = (w - targetW) / 2;
                int y = (h - targetH) / 2;
                BufferedImage croppedImg = tempImg.getSubimage(x, y, targetW, targetH);
                float[] sharpenMatrix = { -0.1f, -0.1f, -0.1f, -0.1f, 1.8f, -0.1f, -0.1f, -0.1f, -0.1f };
                Kernel kernel = new Kernel(3, 3, sharpenMatrix);
                ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
                BufferedImage sharpImg = new BufferedImage(targetW, targetH, type);
                op.filter(croppedImg, sharpImg);
                lblImage.setIcon(new ImageIcon(sharpImg));
            }
        } catch (Exception e) {
        }
    }

    // --- CUSTOM PIN DIALOG CLASS ---
    private class PinDialog extends JDialog {
        private boolean verified = false;
        private int pX, pY;

        public PinDialog(JFrame parent, String roleTitle, String correctPin) {
            super(parent, "Security Check", true); // Modal = true
            setUndecorated(true);
            setSize(400, 250);
            setLocationRelativeTo(parent);
            setShape(new RoundRectangle2D.Double(0, 0, 400, 250, 20, 20));

            // Background Gradient Panel
            JPanel contentPane = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    Color color1 = new Color(228, 220, 207);
                    Color color2 = new Color(153, 143, 133);
                    GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // Vẽ viền mỏng
                    g2d.setColor(COLOR_PRIMARY);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                }
            };
            contentPane.setLayout(new GridBagLayout());
            setContentPane(contentPane);

            // Drag Listener
            MouseAdapter drag = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    pX = e.getX();
                    pY = e.getY();
                }

                public void mouseDragged(MouseEvent e) {
                    setLocation(getLocation().x + (e.getX() - pX), getLocation().y + (e.getY() - pY));
                }
            };
            addMouseListener(drag);
            addMouseMotionListener(drag);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.gridx = 0;
            gbc.gridy = 0;

            // Title
            JLabel lblTitle = new JLabel("SECURITY CHECK");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitle.setForeground(COLOR_PRIMARY);
            contentPane.add(lblTitle, gbc);

            // Subtitle
            gbc.gridy++;
            JLabel lblMsg = new JLabel("Enter PIN for " + roleTitle);
            lblMsg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblMsg.setForeground(COLOR_PRIMARY);
            contentPane.add(lblMsg, gbc);

            // Password Field
            gbc.gridy++;
            RoundedPasswordField txtPin = new RoundedPasswordField(15, COLOR_INPUT_BG);
            txtPin.setHorizontalAlignment(JTextField.CENTER);
            contentPane.add(txtPin, gbc);

            // Buttons Panel
            gbc.gridy++;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            btnPanel.setOpaque(false);

            JButton btnUnlock = createButton("UNLOCK");
            btnUnlock.setPreferredSize(new Dimension(100, 35));
            btnUnlock.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JButton btnCancel = createButton("CANCEL");
            btnCancel.setPreferredSize(new Dimension(100, 35));
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCancel.setBackground(new Color(100, 90, 80)); // Màu xám nâu nhạt hơn cho nút Cancel

            // Logic
            ActionListener actionUnlock = e -> {
                String input = new String(txtPin.getPassword());
                if (input.equals(correctPin)) {
                    verified = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect PIN!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    txtPin.setText("");
                    txtPin.requestFocus();
                }
            };

            btnUnlock.addActionListener(actionUnlock);
            txtPin.addActionListener(actionUnlock); // Enter key support
            btnCancel.addActionListener(e -> dispose());

            btnPanel.add(btnUnlock);
            btnPanel.add(btnCancel);
            contentPane.add(btnPanel, gbc);
        }

        public boolean isVerified() {
            return verified;
        }
    }
}

class RoundedTextField extends JTextField {
    private Color bgColor;
    private String placeholder;

    public RoundedTextField(int cols, Color bgColor) {
        super(cols);
        this.bgColor = bgColor;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        super.paintComponent(g);

        if (getText().isEmpty() && placeholder != null && !hasFocus()) {
            g2.setColor(new Color(100, 100, 100, 150)); // Màu xám mờ
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            int paddingLeft = getInsets().left;
            int centerY = (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2;
            g2.drawString(placeholder, paddingLeft, centerY);
        }
        g2.dispose();
    }
}

class RoundedPasswordField extends JPasswordField {
    private Color bgColor;

    public RoundedPasswordField(int cols, Color bgColor) {
        super(cols);
        this.bgColor = bgColor;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        super.paintComponent(g);
        g2.dispose();
    }
}