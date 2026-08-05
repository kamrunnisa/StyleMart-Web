package com.stylemart.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends the OTP email over SMTP using credentials from mail.properties.
 * If mail.enabled=false (the default, so a fresh checkout still runs without
 * any SMTP setup) it falls back to logging the OTP to the server console --
 * the same behavior the project had before, just centralized in one place.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = EmailService.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in != null) {
                CONFIG.load(in);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load mail.properties, falling back to console OTP logging", e);
        }
    }

    private EmailService() {}

    public static void sendOtpEmail(String toEmail, String recipientName, String otp) {
        boolean enabled = Boolean.parseBoolean(CONFIG.getProperty("mail.enabled", "false"));

        if (!enabled) {
            // Dev-mode fallback: no SMTP configured yet.
            LOGGER.info(() -> "[DEV MODE - email disabled] OTP for " + toEmail + " is: " + otp);
            return;
        }

        String host = CONFIG.getProperty("mail.smtp.host");
        String port = CONFIG.getProperty("mail.smtp.port", "587");
        String username = CONFIG.getProperty("mail.smtp.username");
        String password = CONFIG.getProperty("mail.smtp.password");
        String from = CONFIG.getProperty("mail.from", username);
        String fromName = CONFIG.getProperty("mail.from.name", "StyleMart");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your StyleMart verification code");
            message.setText(
                    "Hi " + (recipientName == null ? "" : recipientName) + ",\n\n" +
                    "Your StyleMart verification code is: " + otp + "\n\n" +
                    "This code expires in 5 minutes. If you didn't request this, you can ignore this email.\n\n" +
                    "— StyleMart"
            );
            Transport.send(message);
        } catch (Exception e) {
            // Never let an email failure block registration -- log it and let the
            // caller decide how to inform the user (they can still use resend).
            LOGGER.log(Level.SEVERE, "Failed to send OTP email to " + toEmail, e);
            LOGGER.info(() -> "[FALLBACK] OTP for " + toEmail + " is: " + otp);
        }
    }
}
