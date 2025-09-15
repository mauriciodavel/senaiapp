package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SqlFormatter {

    public static String formatSql(String sql, Object... params) {
        if (params == null || params.length == 0) return sql;
        StringBuilder out = new StringBuilder();
        int p = 0;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '?' && p < params.length) {
                out.append(toSqlLiteral(params[p++]));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    private static String toSqlLiteral(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Number) return v.toString();
        if (v instanceof Boolean) return ((Boolean) v) ? "1" : "0";
        if (v instanceof java.sql.Date || v instanceof java.sql.Timestamp || v instanceof java.util.Date) {
            Date d = (v instanceof Date) ? (Date) v : new Date();
            String s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
            return "'" + s + "'";
        }
        // strings e outros objetos
        String s = String.valueOf(v);
        s = s.replace("'", "''");
        return "'" + s + "'";
    }
}
