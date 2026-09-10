package br.edu.biblioteca;

import jakarta.persistence.*;
import java.time.LocalDate;


/** Entidade mapeada com Jakarta Persistence. */
@Entity
@Table(name = "livro")
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, unique = true)
    private String isbn;

    @OneToMany(mappedBy = "livro")
    private java.util.List<Exemplar> exemplares = new java.util.ArrayList<>();

    public java.util.List<Exemplar> getExemplares() { return exemplares; }

    public Livro() {}

    public Integer getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
}
