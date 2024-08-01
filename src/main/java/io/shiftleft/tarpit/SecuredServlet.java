package io.shiftleft.tarpit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDateTime;

@WebServlet(name = "securedServlet", urlPatterns = {"/"})
public class SecuredServlet extends HttpServlet {
    @Override
	@Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        Principal principal = req.getUserPrincipal();
        if (principal == null || !req.isUserInRole("employee")) {
            LoginHandlerServlet.forwardToLogin(req, resp, null);
            return;
        }
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.println("Welcome to the secured page!");
        
        // Use HtmlUtils.htmlEscape to escape user input before outputting
        String userName = HtmlUtils.htmlEscape(req.getRemoteUser());
        writer.printf("<br/>User: " + userName);
        
        // Do not directly format dates and times for output
        writer.printf("<br/>time: " + LocalDateTime.now());
        
        // Use HtmlUtils.htmlEscape to escape URLs before outputting
        String logoutUrl = HtmlUtils.htmlEscape("/logout");
        writer.println("<br/><a href='" + logoutUrl + "'>Logout</a>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
