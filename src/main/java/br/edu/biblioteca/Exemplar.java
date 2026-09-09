package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: exemplar. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "exemplar")
public class Exemplar {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = "livro_id", foreign = true, foreignAutoRefresh = true)
    private Livro livro;

    @DatabaseField(canBeNull = false, unique = true)
    private String codigo;

    public Exemplar() {}

    public Integer getId() { return id; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
