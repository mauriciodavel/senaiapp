package dao;

import config.ConnectionFactory;
import model.Audit;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    // INSERT de auditoria (usado pelo LogService)
    private static final String SQL_AUDIT_INSERT =
            "INSERT INTO auditoria (usuario_id, usuario_email, operacao, sql_texto) VALUES (?, ?, ?, ?)";

    /** Grava na tabela 'auditoria'. NÃO chame LogService aqui para evitar loop. */
    public static void audit(Integer usuarioId, String usuarioEmail, String operacao, String sqlTexto) throws Exception {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_AUDIT_INSERT)) {

            if (usuarioId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, usuarioId);
            ps.setString(2, usuarioEmail);
            ps.setString(3, operacao);
            ps.setString(4, sqlTexto);
            ps.executeUpdate();
        }
    }

    // ===== LEITURA COM FILTROS =====
    // Retorna até 'limit' linhas (default recomendado: 500)
    public List<Audit> search(String usuarioTerm, String dataStr, String operacaoTerm, String sqlTerm, int limit) throws Exception {
        StringBuilder sb = new StringBuilder(
                "SELECT id, momento, usuario_id, usuario_email, operacao, sql_texto " +
                "FROM auditoria WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        // Filtro de usuário: casa por email OU por id (se o termo for numérico)
        if (notEmpty(usuarioTerm)) {
            String term = usuarioTerm.trim();
            if (term.matches("\\d+")) {
                sb.append(" AND (usuario_id = ? OR COALESCE(usuario_email,'') LIKE ?)");
                params.add(Integer.parseInt(term));
                params.add("%" + term + "%");
            } else {
                sb.append(" AND COALESCE(usuario_email,'') LIKE ?");
                params.add("%" + term + "%");
            }
        }

        // Filtro de data: aceita "dd/MM/yyyy" (o dia inteiro) ou "dd/MM/yyyy HH:mm" (minuto exato)
        if (notEmpty(dataStr)) {
            LocalDateTime[] range = parseDateOrDateTime(dataStr.trim());
            if (range != null) {
                sb.append(" AND momento >= ? AND momento < ?");
                params.add(Timestamp.valueOf(range[0]));
                params.add(Timestamp.valueOf(range[1]));
            }
        }

        // Filtro de operação (LIKE)
        if (notEmpty(operacaoTerm)) {
            sb.append(" AND COALESCE(operacao,'') LIKE ?");
            params.add("%" + operacaoTerm.trim() + "%");
        }

        // Filtro por trecho do SQL (LIKE)
        if (notEmpty(sqlTerm)) {
            sb.append(" AND COALESCE(sql_texto,'') LIKE ?");
            params.add("%" + sqlTerm.trim() + "%");
        }

        sb.append(" ORDER BY momento DESC");
        if (limit > 0) sb.append(" LIMIT ").append(limit);

        List<Audit> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {

            // aplica parâmetros
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Audit a = new Audit();
                    a.setId(rs.getLong("id"));

                    Timestamp ts = rs.getTimestamp("momento");
                    if (ts != null) {
                        a.setMomento(ts.toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDateTime());
                    }

                    int uid = rs.getInt("usuario_id");
                    a.setUsuarioId(rs.wasNull() ? null : uid);
                    a.setUsuarioEmail(rs.getString("usuario_email"));
                    a.setOperacao(rs.getString("operacao"));
                    a.setSqlTexto(rs.getString("sql_texto"));

                    lista.add(a);
                }
            }
        }
        return lista;
    }

    // Overload prático (limit padrão 500)
    public List<Audit> search(String usuarioTerm, String dataStr, String operacaoTerm, String sqlTerm) throws Exception {
        return search(usuarioTerm, dataStr, operacaoTerm, sqlTerm, 500);
    }

    // ===== Helpers =====
    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /**
     * dd/MM/yyyy  -> [00:00:00, +1dia)
     * dd/MM/yyyy HH:mm -> [HH:mm:00, HH:mm:59.999]
     */
    private static LocalDateTime[] parseDateOrDateTime(String s) {
        try {
            if (s.matches("\\d{2}/\\d{2}/\\d{4}$")) {
                // apenas data
                LocalDate d = LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                return new LocalDateTime[]{ d.atStartOfDay(), d.plusDays(1).atStartOfDay() };
            }
            if (s.matches("\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}$")) {
                // data + hora:minuto
                LocalDateTime ldt = LocalDateTime.parse(s,
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                return new LocalDateTime[]{ ldt.withSecond(0).withNano(0),
                        ldt.withSecond(59).withNano(999_000_000) };
            }
        } catch (Exception ignored) {}
        return null;
    }
}
