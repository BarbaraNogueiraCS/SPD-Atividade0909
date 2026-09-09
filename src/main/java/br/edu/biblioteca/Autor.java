package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: autor. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "autor")
public class Autor {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String nome;

    public Autor() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
