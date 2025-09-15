package session;

import model.Cliente;

public class Session {
    private static Cliente usuario;
    public static Cliente getUsuario() { return usuario; }
    public static void setUsuario(Cliente u) { usuario = u; }
    public static void clear() { usuario = null; }
}
