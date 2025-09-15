package controller;

import dao.ClienteDAO;
import model.Cliente;
import table.ClienteTableModel;
import view.ClienteView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.sql.SQLException;
import java.util.List;

public class ClienteController {
    private final ClienteView view;
    private final ClienteDAO dao = new ClienteDAO();
    private final ClienteTableModel tableModel = new ClienteTableModel();

    public ClienteController(ClienteView view) {
        this.view = view;

        // configura tabela
        view.getTblClientes().setModel(tableModel);
        view.getTblClientes().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // listeners dos botões
        view.getBtnCadastrar().addActionListener(e -> cadastrar());
        view.getBtnAlterar().addActionListener(e -> alterar());
        view.getBtnApagar().addActionListener(e -> apagar());
        view.getBtnPesquisar().addActionListener(e -> pesquisar());

        // seleção da tabela -> preenche campos
        view.getTblClientes().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) preencherCamposComSelecionado();
            }
        });

        // carrega dados iniciais
        refreshTable();
    }

    private void refreshTable() {
        try {
            tableModel.setData(dao.findAll());
        } catch (SQLException ex) {
            erro("Erro ao carregar clientes: " + ex.getMessage());
        }
    }

    private void cadastrar() {
        String nome  = view.getTxtNome().getText().trim();
        String email = view.getTxtEmail().getText().trim();
        String senha = view.getTxtSenha().getText().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            aviso("Preencha Nome, E-mail e Senha.");
            return;
        }
        try {
            if (dao.existsByEmail(email)) {
                aviso("Já existe cliente com este e-mail.");
                return;
            }
            Cliente c = new Cliente(nome, email, senha);
            dao.insert(c);
            sucesso("Cliente cadastrado (ID " + c.getId() + ").");
            limparCampos();
            refreshTable();
        } catch (SQLException ex) {
            erro("Erro ao cadastrar: " + ex.getMessage());
        }
    }

    private void alterar() {
        int row = view.getTblClientes().getSelectedRow();
        if (row < 0) { aviso("Selecione um cliente na tabela para alterar."); return; }
        Cliente selecionado = tableModel.getAt(row);

        String nome  = view.getTxtNome().getText().trim();
        String email = view.getTxtEmail().getText().trim();
        String senha = view.getTxtSenha().getText().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            aviso("Preencha Nome, E-mail e Senha."); return;
        }
        try {
            if (!email.equalsIgnoreCase(selecionado.getEmail()) && dao.existsByEmail(email)) {
                aviso("Já existe cliente com este e-mail."); return;
            }
            selecionado.setNome(nome);
            selecionado.setEmail(email);
            selecionado.setSenha(senha);

            if (dao.update(selecionado)) {
                sucesso("Cliente atualizado.");
                refreshTable();
            } else {
                aviso("Nada alterado.");
            }
        } catch (SQLException ex) {
            erro("Erro ao alterar: " + ex.getMessage());
        }
    }

    private void apagar() {
        int row = view.getTblClientes().getSelectedRow();
        if (row < 0) { aviso("Selecione um cliente na tabela para apagar."); return; }
        Cliente selecionado = tableModel.getAt(row);

        int opc = JOptionPane.showConfirmDialog(view,
                "Excluir o cliente '" + selecionado.getNome() + "'?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (opc != JOptionPane.YES_OPTION) return;

        try {
            if (dao.delete(selecionado.getId())) {
                sucesso("Cliente removido.");
                limparCampos();
                refreshTable();
            } else {
                aviso("Cliente não removido.");
            }
        } catch (SQLException ex) {
            erro("Erro ao apagar: " + ex.getMessage());
        }
    }

    private void pesquisar() {
        String termo = view.getTxtNome().getText().trim();
        if (termo.isEmpty()) termo = view.getTxtEmail().getText().trim();
        try {
            if (termo.isEmpty()) {
                refreshTable(); // sem termo -> lista tudo
            } else {
                List<Cliente> lista = dao.searchByNomeOuEmail(termo);
                tableModel.setData(lista);
                if (lista.isEmpty()) aviso("Nenhum cliente encontrado para: " + termo);
            }
        } catch (SQLException ex) {
            erro("Erro na pesquisa: " + ex.getMessage());
        }
    }

    private void preencherCamposComSelecionado() {
        int row = view.getTblClientes().getSelectedRow();
        if (row >= 0) {
            Cliente c = tableModel.getAt(row);
            view.getTxtNome().setText(c.getNome());
            view.getTxtEmail().setText(c.getEmail());
            view.getTxtSenha().setText(c.getSenha());
        }
    }

    private void limparCampos() {
        view.getTxtNome().setText("");
        view.getTxtEmail().setText("");
        view.getTxtSenha().setText("");
        view.getTblClientes().clearSelection();
        view.getTxtNome().requestFocus();
    }

    private void sucesso(String msg){ JOptionPane.showMessageDialog(view, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE); }
    private void aviso(String msg){ JOptionPane.showMessageDialog(view, msg, "Atenção", JOptionPane.WARNING_MESSAGE); }
    private void erro(String msg){ JOptionPane.showMessageDialog(view, msg, "Erro", JOptionPane.ERROR_MESSAGE); }
}
