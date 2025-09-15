package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    // ====== AJUSTE A URL CONFORME SUA PORTA ======
    // USBWebserver (porta 3306):
    private static final String URL = "jdbc:mysql://localhost:3306/senaiapp"
            + "?useSSL=false"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8"
            + "&serverTimezone=America/Sao_Paulo"
            + "&allowPublicKeyRetrieval=true";


    private static final String USER = "root";
    private static final String PASS = "usbw";

    static {
        // Garante o carregamento do driver (útil em projetos Ant mais antigos)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Driver JDBC do MySQL não encontrado no classpath.", ex);
        }
    }

    /**
     * Obtém uma conexão com o banco 'senaiapp'.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Fecha silenciosamente uma Connection.
     */
    public static void closeQuietly(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Fecha silenciosamente qualquer AutoCloseable (ResultSet/Statement/PreparedStatement).
     */
    public static void closeQuietly(AutoCloseable ac) {
        if (ac != null) {
            try { ac.close(); } catch (Exception ignored) {}
        }
    }
}
