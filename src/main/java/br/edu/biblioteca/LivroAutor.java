package br.edu.biblioteca;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/** Entidade persistente: livro_autor. Datas usam texto ISO yyyy-MM-dd. */
@DatabaseTable(tableName = "livro_autor")
public class LivroAutor {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = "livro_id", foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private Livro livro;

    @DatabaseField(canBeNull = false, columnName = "autor_id", foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private Autor autor;

    public LivroAutor() {}

    public Integer getId() { return id; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }
}
