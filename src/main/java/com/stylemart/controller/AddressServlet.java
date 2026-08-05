package com.stylemart.controller;

import com.stylemart.dao.AddressDAO;
import com.stylemart.model.Address;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Address book management. Sits under /account/*, so AuthFilter already
 * guarantees a logged-in user before any method here runs.
 *   GET  /account/addresses         -> list + add/edit form
 *   POST /account/addresses/save    -> create (no id param) or update (id param)
 *   POST /account/addresses/delete  -> remove one
 *   POST /account/addresses/default -> mark one as the default
 */
@WebServlet({"/account/addresses", "/account/addresses/save",
        "/account/addresses/delete", "/account/addresses/default"})
public class AddressServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AddressServlet.class.getName());
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/account/addresses".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int userId = currentUserId(request);

        try {
            List<Address> addresses = addressDAO.getByUser(userId);
            request.setAttribute("addresses", addresses);

            int editId = parseInt(request.getParameter("edit"), -1);
            if (editId > 0) {
                Address editing = addressDAO.getByIdForUser(editId, userId);
                request.setAttribute("editingAddress", editing);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load addresses for userId=" + userId, e);
            request.setAttribute("errorMessage", "Unable to load your addresses right now.");
        }

        String redirect = request.getParameter("redirect");
        if (redirect != null) request.setAttribute("redirect", redirect);

        request.getRequestDispatcher("/WEB-INF/views/account/addresses.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = currentUserId(request);
        String path = request.getServletPath();
        String ctx = request.getContextPath();

        try {
            switch (path) {
                case "/account/addresses/save" -> handleSave(request, userId);
                case "/account/addresses/delete" -> addressDAO.delete(parseInt(request.getParameter("id"), -1), userId);
                case "/account/addresses/default" -> addressDAO.setDefault(parseInt(request.getParameter("id"), -1), userId);
                default -> {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Address operation failed for userId=" + userId + " path=" + path, e);
            request.getSession().setAttribute("flashError", "Something went wrong saving that address.");
        }

        String redirect = request.getParameter("redirect");
        String target = "checkout".equals(redirect) ? ctx + "/checkout" : ctx + "/account/addresses";
        response.sendRedirect(target);
    }

    private void handleSave(HttpServletRequest request, int userId) throws SQLException {
        Address a = new Address();
        a.setUserId(userId);
        a.setLabel(blankToDefault(request.getParameter("label"), "Home"));
        a.setFullName(trimmed(request.getParameter("fullName")));
        a.setPhone(trimmed(request.getParameter("phone")));
        a.setAddressLine1(trimmed(request.getParameter("addressLine1")));
        a.setAddressLine2(trimmed(request.getParameter("addressLine2")));
        a.setCity(trimmed(request.getParameter("city")));
        a.setState(trimmed(request.getParameter("state")));
        a.setPincode(trimmed(request.getParameter("pincode")));
        a.setDefault(request.getParameter("isDefault") != null);

        if (a.getFullName() == null || a.getPhone() == null || a.getAddressLine1() == null
                || a.getCity() == null || a.getState() == null || a.getPincode() == null) {
            return; // silently ignore incomplete submissions; form still shows required attrs client-side
        }

        int id = parseInt(request.getParameter("id"), -1);
        if (id > 0) {
            a.setId(id);
            addressDAO.update(a);
        } else {
            addressDAO.insert(a);
        }
    }

    private int currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Integer id = session == null ? null : (Integer) session.getAttribute("userId");
        return id == null ? -1 : id;
    }

    private String trimmed(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String blankToDefault(String s, String fallback) {
        String t = trimmed(s);
        return t == null ? fallback : t;
    }

    private int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
