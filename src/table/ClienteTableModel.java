package table;

import model.Cliente;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ClienteTableModel extends AbstractTableModel {
    private final String[] colunas = {"ID", "Nome", "E-mail"};
    private final List<Cliente> dados = new ArrayList<>();

    public void setData(List<Cliente> clientes) {
        dados.clear();
        if (clientes != null) dados.addAll(clientes);
        fireTableDataChanged();
    }

    public Cliente getAt(int row) {
        return dados.get(row);
    }

    @Override public int getRowCount() { return dados.size(); }
    @Override public int getColumnCount() { return colunas.length; }
    @Override public String getColumnName(int column) { return colunas[column]; }
    @Override public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Integer.class : String.class;
    }
    @Override public Object getValueAt(int row, int col) {
        Cliente c = dados.get(row);
        return switch (col) {
            case 0 -> c.getId();
            case 1 -> c.getNome();
            case 2 -> c.getEmail();
            default -> null;
        };
    }
}
