package com.stylemart.listener;

import com.stylemart.util.DBConnection;
import com.stylemart.util.PasswordUtil;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The seed SQL ships a placeholder ('$2a$12$examplehashreplaceatruntime') for
 * the default admin because a real BCrypt hash can't be hand-written without
 * running the app's own PasswordUtil (which depends on the exact jBCrypt
 * version on the classpath). On startup, if that placeholder is still in the
 * database, this swaps it for a real hash of the documented default password.
 *
 * Default admin login after first boot: admin@stylemart.com / Admin@123
 * CHANGE THIS PASSWORD after logging in for the first time.
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppInitListener.class.getName());
    private static final String PLACEHOLDER_HASH = "$2a$12$examplehashreplaceatruntime";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@stylemart.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String sql = "UPDATE admins SET password_hash = ? WHERE email = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(DEFAULT_ADMIN_PASSWORD));
            ps.setString(2, DEFAULT_ADMIN_EMAIL);
            ps.setString(3, PLACEHOLDER_HASH);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                LOGGER.info(() -> "Default admin password initialized. Login: " + DEFAULT_ADMIN_EMAIL
                        + " / " + DEFAULT_ADMIN_PASSWORD + " -- change this after first login.");
            }
        } catch (SQLException e) {
            // Non-fatal: the site should still start even if the DB isn't reachable yet
            // (e.g. XAMPP MySQL not started). Admin login just won't work until it is.
            LOGGER.log(Level.WARNING, "Could not initialize default admin password (is MySQL running?)", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }
}
