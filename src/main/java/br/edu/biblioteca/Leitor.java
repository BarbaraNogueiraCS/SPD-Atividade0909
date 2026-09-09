package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: leitor. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "leitor")
public class Leitor {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String nome;

    @DatabaseField(canBeNull = false, unique = true)
    private String email;

    public Leitor() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
