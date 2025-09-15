package dao;

import config.ConnectionFactory;
import model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import service.LogService;

public class ClienteDAO {

    // SQLs
    private static final String SQL_INSERT
            = "INSERT INTO clientes (nome, email, senha) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE
            = "UPDATE clientes SET nome = ?, email = ?, senha = ? WHERE id = ?";

    private static final String SQL_DELETE
            = "DELETE FROM clientes WHERE id = ?";

    private static final String SQL_FIND_BY_ID
            = "SELECT id, nome, email, senha FROM clientes WHERE id = ?";

    private static final String SQL_FIND_ALL
            = "SELECT id, nome, email, senha FROM clientes ORDER BY id DESC";

    private static final String SQL_SEARCH_NOME_EMAIL
            = "SELECT id, nome, email, senha FROM clientes "
            + "WHERE nome LIKE ? OR email LIKE ? ORDER BY nome";

    private static final String SQL_EXISTS_EMAIL
            = "SELECT 1 FROM clientes WHERE email = ? LIMIT 1";

    /**
     * Cria um cliente e devolve o ID gerado.
     */
    public int insert(Cliente c) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getSenha()); // OBS: em produção, armazene hash (BCrypt/Argon2)
            ps.executeUpdate();
            // depois de ps.executeUpdate() e pegar o ID:
            service.LogService.logDb("INSERT clientes", SQL_INSERT,
                    c.getNome(), c.getEmail(), service.LogService.maskSecret(c.getSenha()));

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    c.setId(id);
                    return id;
                }
            }
            return -1; // não gerou ID (caso raro)
        }
    }

    /**
     * Atualiza um cliente pelo ID. Retorna true se alterou.
     */
    public boolean update(Cliente c) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            // ordem dos ? no SQL_UPDATE: nome, email, senha, id
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getSenha()); // **não** logamos em claro; só setamos no SQL
            ps.setInt(4, c.getId());

            boolean ok = ps.executeUpdate() > 0;

            // <-- AQUI entra o log (máscara na senha!)
            LogService.logDb("UPDATE clientes", SQL_UPDATE,
                    c.getNome(), c.getEmail(), LogService.maskSecret(c.getSenha()), c.getId());

            return ok;
        }
    }

    /**
     * Remove um cliente pelo ID.
     */
    public boolean delete(int id) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            service.LogService.logDb("DELETE clientes", SQL_DELETE, id);
            return ok;

        }
    }

    /**
     * Busca por ID.
     */
    public Cliente findById(int id) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                service.LogService.logDb("SELECT clientes (by id)", SQL_FIND_BY_ID, id);
                // ...
            }

        }
        return null;
    }

    /**
     * Lista todos.
     */
    public java.util.List<model.Cliente> findAll() throws java.sql.SQLException {
        java.util.List<model.Cliente> lista = new java.util.ArrayList<>();

        try (java.sql.Connection con = config.ConnectionFactory.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(SQL_FIND_ALL); java.sql.ResultSet rs = ps.executeQuery()) {

            // >>> LOG AQUI (uma única vez por chamada)
            LogService.logDb("SELECT clientes (all)", SQL_FIND_ALL);

            while (rs.next()) {
                lista.add(map(rs)); // seu método map(ResultSet)
            }
        }
        return lista;
    }

    /**
     * Pesquisa por nome ou email contendo o termo (case-insensitive depende do
     * collation).
     */
    public List<Cliente> searchByNomeOuEmail(String termo) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String like = "%" + termo + "%";
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_SEARCH_NOME_EMAIL)) {

            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                service.LogService.logDb("SEARCH clientes (nome ou email)", SQL_SEARCH_NOME_EMAIL, like, like);
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Verifica se já existe cliente com este email (útil para validação antes
     * do insert/update).
     */
    public boolean existsByEmail(String email) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_EXISTS_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ---- helper para mapear ResultSet -> Cliente
    private Cliente map(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha")
        );
    }

    // abaixo dos outros SQLs - Método de autenticação da tela de login
    private static final String SQL_LOGIN
            = "SELECT id, nome, email, senha FROM clientes WHERE email = ? AND senha = ?";

// ...
    /**
     * Autentica por email e senha. Retorna o Cliente logado ou null se falhar.
     */
    public Cliente autenticar(String email, String senha) throws SQLException {
        try (Connection con = ConnectionFactory.getConnection(); PreparedStatement ps = con.prepareStatement(SQL_LOGIN)) {
            ps.setString(1, email);
            ps.setString(2, senha);
            try (ResultSet rs = ps.executeQuery()) {
                boolean ok = rs.next();
                // Loga a tentativa de login como ANONIMO (antes de ter Session)
                service.LogService.logDb("LOGIN",
                        SQL_LOGIN,
                        email,
                        service.LogService.maskSecret(senha));

                if (ok) {
                    return map(rs);
                }
            }
        }
        return null;
    }

}
