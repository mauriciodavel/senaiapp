package table;

import model.Audit;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditTableModel extends AbstractTableModel {

    private final String[] colunas = {"Data/Hora", "Usuário", "Operação", "SQL"};
    private final List<Audit> dados = new ArrayList<>();
    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void setData(List<Audit> lista) {
        dados.clear();
        if (lista != null) dados.addAll(lista);
        fireTableDataChanged();
    }

    public Audit getAt(int row) { return dados.get(row); }

    @Override public int getRowCount() { return dados.size(); }
    @Override public int getColumnCount() { return colunas.length; }
    @Override public String getColumnName(int column) { return colunas[column]; }
    @Override public Class<?> getColumnClass(int columnIndex) { return String.class; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Audit a = dados.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> a.getMomento() == null ? "" : a.getMomento().format(FMT);
            case 1 -> a.getUsuarioEmail() != null && !a.getUsuarioEmail().isEmpty()
                        ? a.getUsuarioEmail()
                        : (a.getUsuarioId() == null ? "" : "id=" + a.getUsuarioId());
            case 2 -> a.getOperacao();
            case 3 -> a.getSqlTexto();
            default -> "";
        };
    }
}
