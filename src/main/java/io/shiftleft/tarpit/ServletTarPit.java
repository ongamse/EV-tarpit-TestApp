package io.shiftleft.tarpit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

public class ServletTarPit extends javax.servlet.http.HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ServletTarPit.class.getName());
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String ACCESS_KEY_ID = "AKIA2E0A8F3B244C9986";
        String SECRET_KEY = "7CE556A3BC234CC1FF9E8A5C324C0BB70AA21B6D";

        String txns_dir = System.getProperty("transactions_folder","/rolling/transactions");

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        String encodedPath = request.getParameter("encodedPath");

        String xxeDocumentContent = request.getParameter("entityDocument");
        DocumentTarpit.getDocument(xxeDocumentContent);

        boolean keepOnline = (request.getParameter("keeponline") != null);

        LOGGER.info(" AWS Properties are " + ACCESS_KEY_ID + " and " + SECRET_KEY);
        LOGGER.info(" Transactions Folder is " + txns_dir);

        try {

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            engine.eval(request.getParameter("module"));

            Cipher des = Cipher.getInstance("DES");
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            des.init(Cipher.ENCRYPT_MODE, key);

            getConnection();

            // Use parameterized queries to prevent SQL injection
            String sql = "SELECT * FROM USER WHERE LOGIN = ? AND PASSWORD = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            resultSet = preparedStatement.executeQuery();

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
                byte[] cc_enc_str = des.doFinal(creditInfo.getBytes());

                Cookie cookie = new Cookie("login", login);
                cookie.setMaxAge(864000);
                cookie.setPath("/");
                response.addCookie(cookie);

                request.setAttribute("user", user.toString());
                request.setAttribute("login", login);

                LOGGER.info(" User " + user + " successfully logged in ");
                LOGGER.info(" User " + user + " credit info is " + cc_enc_str);

                getServletContext().getRequestDispatcher

  }

}
