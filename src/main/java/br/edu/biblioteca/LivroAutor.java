package br.edu.biblioteca;

import jakarta.persistence.*;
import java.time.LocalDate;


/** Entidade mapeada com Jakarta Persistence. */
@Entity
@Table(name = "livro_autor", uniqueConstraints = @UniqueConstraint(columnNames = {"livro_id", "autor_id"}))
public class LivroAutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Autor autor;

    public LivroAutor() {}

    public Integer getId() { return id; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }
}
