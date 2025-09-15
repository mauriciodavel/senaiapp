package service;

import model.Cliente;
import session.Session;
import util.SqlFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class LogService {

    private static final Logger LOGGER = Logger.getLogger("SenaiAppAudit");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    // habilitar escrita também no banco?
    private static final boolean WRITE_DB = true;

    static {
        try {
            Files.createDirectories(Paths.get("logs"));
            // 5 arquivos de até 1MB cada (rotação)
            FileHandler fh = new FileHandler("logs/senaiapp-%g.log", 1024 * 1024, 5, true);
            fh.setEncoding("UTF-8");
            fh.setFormatter(new Formatter() {
                @Override
                public synchronized String format(LogRecord r) {
                    return String.format("%s [%s] %s%n",
                            TS.format(java.time.Instant.ofEpochMilli(r.getMillis())),
                            r.getLevel().getName(),
                            r.getMessage());
                }
            });
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(fh);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            e.printStackTrace(); // último recurso
        }
    }

    /**
     * Registra uma operação de banco.
     * @param operacao  Ex.: "INSERT clientes", "UPDATE clientes", "LOGIN"
     * @param sqlTemplate SQL com '?'
     * @param params     valores dos '?'. NUNCA passe senha em claro.
     */
    public static void logDb(String operacao, String sqlTemplate, Object... params) {
        // formata SQL com valores
        String fullSql = SqlFormatter.formatSql(sqlTemplate, params);

        // usuário logado
        Cliente u = Session.getUsuario();
        String usuario = (u != null)
                ? (u.getId() + "|" + safe(u.getEmail()))
                : "ANONIMO";

        String linha = String.format("user=%s op=%s sql=%s", usuario, operacao, fullSql);
        LOGGER.info(linha);

        // opcional: também grava na tabela 'auditoria'
        if (WRITE_DB) {
            try {
                dao.AuditDAO.audit(usuarioIdOrNull(u), (u != null ? u.getEmail() : null), operacao, fullSql);
            } catch (Exception ex) {
                // não interrompe o fluxo da aplicação por falha no log
                LOGGER.log(Level.WARNING, "Falha ao gravar auditoria no banco: " + ex.getMessage());
            }
        }
    }

    public static String maskSecret(Object any) {
        return "***"; // centraliza máscara de segredos (senha, token, etc.)
    }

    private static Integer usuarioIdOrNull(Cliente u){ return (u == null) ? null : u.getId(); }
    private static String safe(String s){ return s == null ? "" : s; }
}
