package io.shiftleft.tarpit;

import io.shiftleft.tarpit.model.User;
import io.shiftleft.tarpit.DocumentTarpit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
// Added from AutoFix suggestion
import org.apache.commons.dbcp2.BasicDataSource;

@WebServlet(name = "simpleServlet", urlPatterns = { "/vulns" }, loadOnStartup = 1)
public class ServletTarPit extends HttpServlet {

    private static final long serialVersionUID = -3462096228274971485L;
    private static BasicDataSource dataSource;

    private final static Logger LOGGER = Logger.getLogger(ServletTarPit.class.getName());

// Added from AutoFix suggestion
    @Override
    public void init() throws ServletException {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mydatabase");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ACCESS_KEY_ID = "AKIA2E0A8F3B244C9986";
        String SECRET_KEY = "7CE556A3BC234CC1FF9E8A5C324C0BB70AA21B6D";

        String txns_dir = System.getProperty("transactions_folder", "/rolling/transactions");

        String login = request.getParameter("login");
        String password = request.getParameter("password");

        String xxeDocumentContent = request.getParameter("entityDocument");
        DocumentTarpit.getDocument(xxeDocumentContent);

        boolean keepOnline = (request.getParameter("keeponline") != null);

        LOGGER.info(" AWS Properties are " + ACCESS_KEY_ID + " and " + SECRET_KEY);
        LOGGER.info(" Transactions Folder is " + txns_dir);

  // Added from AutoFix suggestion
        try (Connection connection = dataSource.getConnection()) {
            String userInput = "attacker-controlled";
            String query = "SELECT * FROM users WHERE username = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userInput);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    login = resultSet.getString("login");
                    password = resultSet.getString("password");

                    User user = new User(login,
                            resultSet.getString("fname"),
                            resultSet.getString("lname"),
                            resultSet.getString("passportnum"),
                            resultSet.getString("address1"),
                            resultSet.getString("address2"),
                            resultSet.getString("zipCode"));

                    String creditInfo = resultSet.getString("userCreditCardInfo");
                    Cipher des = Cipher.getInstance("DES");
                    SecretKey key = KeyGenerator.getInstance("DES").generateKey();
                    des.init(Cipher.ENCRYPT_MODE, key);
                    byte[] cc_enc_str = des.doFinal(creditInfo.getBytes());

                    Cookie cookie = new Cookie("login", login);
                    cookie.setMaxAge(864000);
                    cookie.setPath("/");
                    response.addCookie(cookie);

                    request.setAttribute("user", user.toString());
                    request.setAttribute("login", login);

                    LOGGER.info(" User " + user + " successfully logged in ");
                    LOGGER.info(" User " + user + " credit info is " + cc_enc_str);

                    getServletContext().getRequestDispatcher("/dashboard.jsp").forward(request, response);

                } else {
                    request.setAttribute("login", login);
                    request.setAttribute("password", password);
                    request.setAttribute("keepOnline", keepOnline);
                    request.setAttribute("message", "Failed to Sign in. Please verify credentials");

                    LOGGER.info(" UserId " + login + " failed to logged in ");

                    getServletContext().getRequestDispatcher("/signIn.jsp").forward(request, response);
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private Connection connection;

    private void getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost/DBPROD", "admin", "1234");
    }
}
