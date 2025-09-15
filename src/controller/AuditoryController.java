package controller;

import dao.AuditDAO;
import table.AuditTableModel;
import view.AuditoryView;

import javax.swing.*;
import java.util.List;
import model.Audit;

public class AuditoryController {

    private final AuditoryView view;
    private final AuditDAO dao = new AuditDAO();
    private final AuditTableModel tableModel = new AuditTableModel();

    public AuditoryController(AuditoryView view) {
        this.view = view;

        // configura tabela
        view.getTblAuditory().setModel(tableModel);
        configurarColunas();
        view.getTblAuditory().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ação do botão
        view.getBtnPesquisarAudit().addActionListener(e -> pesquisar());

        // ENTER em qualquer campo também pesquisa
        var enterAction = new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { pesquisar(); }
        };
        view.getTxtUsuarioAudit().addActionListener(enterAction);
        view.getTxtDataAudit().addActionListener(enterAction);
        view.getTxtOperAudit().addActionListener(enterAction);
        view.getTxtSqlAudit().addActionListener(enterAction);

        // carrega últimos registros ao abrir
        pesquisar();
    }
    
    // no topo: import javax.swing.JTable;

private void configurarColunas() {
    var tabela = view.getTblAuditory();

    // usa as larguras preferenciais abaixo
    tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    var col = tabela.getColumnModel();
    col.getColumn(0).setPreferredWidth(140); // Data/Hora
    col.getColumn(1).setPreferredWidth(180); // Usuário
    col.getColumn(2).setPreferredWidth(140); // Operação
    col.getColumn(3).setPreferredWidth(650); // SQL

    tabela.setRowHeight(22);
    tabela.setAutoCreateRowSorter(true); // permite ordenar clicando no cabeçalho
}

    private void pesquisar() {
        String usuario = view.getTxtUsuarioAudit().getText();
        String data    = view.getTxtDataAudit().getText();
        String oper    = view.getTxtOperAudit().getText();
        String sqlLike = view.getTxtSqlAudit().getText();

        try {
            List<Audit> lista = dao.search(usuario, data, oper, sqlLike);
            tableModel.setData(lista);
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Nenhum registro encontrado para os filtros informados.",
                        "Pesquisa", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Erro na pesquisa: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
