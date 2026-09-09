package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: carteirinha. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "carteirinha")
public class Carteirinha {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = "leitor_id", foreign = true, foreignAutoRefresh = true, unique = true)
    private Leitor leitor;

    @DatabaseField(canBeNull = false, unique = true)
    private String numero;

    @DatabaseField(canBeNull = false, columnName = "emitida_em")
    private String emitidaEm;

    public Carteirinha() {}

    public Integer getId() { return id; }
    public Leitor getLeitor() { return leitor; }
    public void setLeitor(Leitor leitor) { this.leitor = leitor; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getEmitidaEm() { return emitidaEm; }
    public void setEmitidaEm(String emitidaEm) { this.emitidaEm = emitidaEm; }
}
