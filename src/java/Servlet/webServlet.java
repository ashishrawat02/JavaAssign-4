/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlet;

import database.Credentials;
import static database.Credentials.getConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author c0649005
 */
@WebServlet("/product")
public class webServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Type", "text/plain-text");
        try (PrintWriter out = response.getWriter()) {
            if (!request.getParameterNames().hasMoreElements()) {
                // There are no parameters at all
                out.println(getResults("SELECT * FROM product"));
            } else {
                // There are some parameters
                int id = Integer.parseInt(request.getParameter("productID"));
                out.println(getResults("SELECT * FROM product WHERE productID = ?", String.valueOf(id)));
            }
        } catch (IOException ex) {
            Logger.getLogger(webServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {

            if (keySet.contains("productID") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String productID = request.getParameter("productID");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("INSERT INTO product (productID,name,description,quantity) VALUES (?, ?, ?, ?)", productID, name, description, quantity);
            } else {

                out.println("Error: Not enough data to input. Please use a URL of the form /servlet?name=XYZ&age=XYZ");
            }
        } catch (IOException ex) {
            Logger.getLogger(webServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int doUpdate(String query, String... params) {
        int numChanges = 0;
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numChanges = pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(webServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numChanges;
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("productID") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String productID = request.getParameter("productID");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                doUpdate("update product set productID = ?, name = ?, description = ?, quantity = ? where productID = ?", productID, name, description, quantity, productID);

            } else {

                out.println("Error: Not enough data to input. Please use a URL of the form /products?id=xx&name=XXX&description=XXX&quantity=xx");
            }
        } catch (IOException ex) {
            System.out.println("Error in writing output: " + ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("productID")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM TABLE`product` WHERE `productID`=" + request.getParameter("productID"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(webServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error in deleting entry.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: Not enough data in table to delete");
                response.setStatus(500);
            }
        } catch (SQLException ex) {
            Logger.getLogger(webServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getResults(String query, String... params) {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sb.append(String.format("%s\t%s\t%s\t%s\n", rs.getInt("productID"), rs.getString("name"), rs.getString("description"), rs.getInt("quantity")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(webServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

}