package test;

import config.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbPing {
    public static void main(String[] args) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT DATABASE() AS db, VERSION() AS versao");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("Conectado com sucesso!");
                System.out.println("Banco atual: " + rs.getString("db"));
                System.out.println("MySQL versão: " + rs.getString("versao"));
            }
        } catch (Exception e) {
            System.err.println("Falha na conexão: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
