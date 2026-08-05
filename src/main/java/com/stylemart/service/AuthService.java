package com.stylemart.service;

import com.stylemart.dao.UserDAO;
import com.stylemart.model.User;
import com.stylemart.util.EmailService;
import com.stylemart.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final UserDAO userDAO = new UserDAO();
    private final SecureRandom random = new SecureRandom();

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }

    public int register(String fullName, String email, String phone, String password) throws AuthException {
        try {
            if (fullName == null || fullName.isBlank()) {
                throw new AuthException("Full name is required");
            }
            if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new AuthException("Please enter a valid email address");
            }
            if (userDAO.emailExists(email)) {
                throw new AuthException("Email is already registered");
            }
            String cleanedPhone = null;
            if (phone != null && !phone.isBlank()) {
                cleanedPhone = phone.trim().replaceAll("[\\s-]", "");
                if (!PHONE_PATTERN.matcher(cleanedPhone).matches()) {
                    throw new AuthException("Phone number must be exactly 10 digits");
                }
            }
            if (password == null || password.length() < 6) {
                throw new AuthException("Password must be at least 6 characters");
            }

            User user = new User();
            user.setFullName(fullName.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPhone(cleanedPhone);
            user.setPasswordHash(PasswordUtil.hash(password));

            String otp = generateOtp();
            int userId = userDAO.create(user, otp);

            EmailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);

            return userId;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Registration failed for email=" + email, e);
            // TEMPORARY DEBUG: showing the real DB error on-page so it's easy to
            // screenshot. Revert to the generic message once the root cause is fixed
            // -- never ship raw SQL error text to end users in production.
            throw new AuthException("Registration failed: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String otp) throws AuthException {
        if (email == null || otp == null || otp.isBlank()) {
            throw new AuthException("Please enter the 6-digit code");
        }
        try {
            return userDAO.verifyOtp(email.trim().toLowerCase(), otp.trim());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "OTP verification failed for email=" + email, e);
            throw new AuthException("Verification failed: database error");
        }
    }

    /**
     * Regenerates and re-emails the OTP for an unverified account. Enforces a
     * server-side 60-second cooldown (client-side timers alone can be bypassed
     * by simply resubmitting the form) using the otp_sent_at column.
     */
    public void resendOtp(String email) throws AuthException {
        if (email == null || email.isBlank()) {
            throw new AuthException("Missing email address");
        }
        String normalizedEmail = email.trim().toLowerCase();
        try {
            long secondsSinceLast = userDAO.secondsSinceLastOtp(normalizedEmail);
            if (secondsSinceLast >= 0 && secondsSinceLast < RESEND_COOLDOWN_SECONDS) {
                long wait = RESEND_COOLDOWN_SECONDS - secondsSinceLast;
                throw new AuthException("Please wait " + wait + "s before requesting another code");
            }

            String otp = generateOtp();
            boolean updated = userDAO.resendOtp(normalizedEmail, otp);
            if (!updated) {
                // Don't reveal whether the account exists/is already verified -- generic message.
                throw new AuthException("Unable to resend a code for this account");
            }

            User user = userDAO.findByEmail(normalizedEmail);
            EmailService.sendOtpEmail(normalizedEmail, user != null ? user.getFullName() : null, otp);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "OTP resend failed for email=" + normalizedEmail, e);
            throw new AuthException("Resend failed: database error");
        }
    }

    public User login(String email, String password) throws AuthException {
        try {
            User user = userDAO.findByEmail(email);
            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
                throw new AuthException("Invalid email or password");
            }
            if (user.isBlocked()) {
                throw new AuthException("Your account has been blocked. Contact support.");
            }
            if (!user.isVerified()) {
                throw new AuthException("Please verify your email before logging in.");
            }
            return user;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Login failed for email=" + email, e);
            throw new AuthException("Login failed: database error");
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
