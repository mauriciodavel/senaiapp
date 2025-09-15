package model;

import java.time.LocalDateTime;

public class Audit {
    private long id;
    private LocalDateTime momento;
    private Integer usuarioId;     // pode ser null
    private String usuarioEmail;   // pode ser null
    private String operacao;
    private String sqlTexto;

    public Audit() {}

    public Audit(long id, LocalDateTime momento, Integer usuarioId, String usuarioEmail,
                 String operacao, String sqlTexto) {
        this.id = id;
        this.momento = momento;
        this.usuarioId = usuarioId;
        this.usuarioEmail = usuarioEmail;
        this.operacao = operacao;
        this.sqlTexto = sqlTexto;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDateTime getMomento() { return momento; }
    public void setMomento(LocalDateTime momento) { this.momento = momento; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getOperacao() { return operacao; }
    public void setOperacao(String operacao) { this.operacao = operacao; }

    public String getSqlTexto() { return sqlTexto; }
    public void setSqlTexto(String sqlTexto) { this.sqlTexto = sqlTexto; }
}
