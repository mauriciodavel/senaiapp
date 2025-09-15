package controller;

import dao.ClienteDAO;
import model.Cliente;
import session.Session;
import view.LoginView;
import view.PrincipalView;

import javax.swing.*;
import java.sql.SQLException;

public class LoginController {

    private final LoginView view;
    private final ClienteDAO dao = new ClienteDAO();

    public LoginController(LoginView view) {
        this.view = view;

        // botão padrão (Enter faz login)
        this.view.getRootPane().setDefaultButton(this.view.getBtnLogar());

        // listeners
        this.view.getBtnLogar().addActionListener(e -> logar());
        this.view.getBtnCancelar().addActionListener(e -> cancelar());
    }

    private void logar() {
        String email = view.getTxtLoginMail().getText().trim();
        char[] pass = view.getTxtLoginSenha().getPassword();
        String senha = new String(pass);

        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Informe e-mail e senha.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Cliente usuario = dao.autenticar(email, senha);
            // higiene do array de senha
            java.util.Arrays.fill(pass, '\0');

            if (usuario != null) {
                // guarda usuário logado (opcional)
                Session.setUsuario(usuario);

                // abre a tela principal e fecha o login
                SwingUtilities.invokeLater(() -> {
                    new PrincipalView().setVisible(true);
                    view.dispose();
                });
            } else {
                JOptionPane.showMessageDialog(view, "Credenciais inválidas.", "Falha no login", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Erro na autenticação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        int op = JOptionPane.showConfirmDialog(view, "Deseja sair da aplicação?", "Sair", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) System.exit(0);
    }
}
