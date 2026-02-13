package com.keycapstore.utils;

import java.util.Properties;
import java.util.Random;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String EMAIL_GUI = "keyforgeartisan@gmail.com";

    private static final String MAT_KHAU_UNG_DUNG = "kxtf motw ykxr ojog";

    public static String generateOTP() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    public static boolean sendEmail(String toEmail, String otpCode) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_GUI, MAT_KHAU_UNG_DUNG);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_GUI));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            message.setSubject("ONE TIME PASSWORD (OTP)- KEYFORGE ARTISAN");

            String htmlContent = "<h3>Xin chào,</h3>"
                    + "<p>Mã xác nhận (OTP) để đặt lại mật khẩu của bạn là:</p>"
                    + "<h2 style='color: #d35400;'>" + otpCode + "</h2>"
                    + "<p>Vui lòng không chia sẻ mã này cho ai khác.</p>"
                    + "<br><p>Trân trọng,<br>Keyforge Team</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Gửi mail thành công đến: " + toEmail);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Gửi mail thất bại!");
            return false;
        }
    }
}
