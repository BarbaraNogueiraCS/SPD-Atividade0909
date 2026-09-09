package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: emprestimo. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "emprestimo")
public class Emprestimo {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = "leitor_id", foreign = true, foreignAutoRefresh = true)
    private Leitor leitor;

    @DatabaseField(canBeNull = false, columnName = "exemplar_id", foreign = true, foreignAutoRefresh = true)
    private Exemplar exemplar;

    @DatabaseField(canBeNull = false, columnName = "realizado_em")
    private String realizadoEm;

    @DatabaseField(canBeNull = false, columnName = "previsto_para")
    private String previstoPara;

    @DatabaseField(canBeNull = true, columnName = "devolvido_em")
    private String devolvidoEm;

    public Emprestimo() {}

    public Integer getId() { return id; }
    public Leitor getLeitor() { return leitor; }
    public void setLeitor(Leitor leitor) { this.leitor = leitor; }
    public Exemplar getExemplar() { return exemplar; }
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }
    public String getRealizadoEm() { return realizadoEm; }
    public void setRealizadoEm(String realizadoEm) { this.realizadoEm = realizadoEm; }
    public String getPrevistoPara() { return previstoPara; }
    public void setPrevistoPara(String previstoPara) { this.previstoPara = previstoPara; }
    public String getDevolvidoEm() { return devolvidoEm; }
    public void setDevolvidoEm(String devolvidoEm) { this.devolvidoEm = devolvidoEm; }
}
