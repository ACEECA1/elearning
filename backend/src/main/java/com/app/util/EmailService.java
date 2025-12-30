package com.app.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;
public class EmailService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String FROM_EMAIL =  dotenv.get("FROM_EMAIL");
    private static final String APP_PASSWORD = dotenv.get("APP_PASSWORD");

    public static void sendEmail(String to, String subject, String body) {
        // 1. SMTP Server Settings
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Secure connection
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 2. Create Session with Authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // 4. Send Email
            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    public static void sendVerificationEmail(String to, String code) {
        String subject = "Email Verification Code";
        String body = "<h3>Your verification code is:</h3>"
                    + "<h2 style='color: blue;'>" + code + "</h2>"
                    + "<p>This code will expire in 15 minutes.</p>";
        sendEmail(to, subject, body);
    }
}