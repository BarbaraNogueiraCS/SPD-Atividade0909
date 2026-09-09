package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: livro. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "livro")
public class Livro {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String titulo;

    @DatabaseField(canBeNull = false, unique = true)
    private String isbn;

    public Livro() {}

    public Integer getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
}
