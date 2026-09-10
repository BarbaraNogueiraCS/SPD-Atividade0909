package br.edu.biblioteca;

import jakarta.persistence.*;


/** Entidade mapeada com Jakarta Persistence. */
@Entity
@Table(name = "exemplar")
public class Exemplar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(nullable = false, unique = true)
    private String codigo;

    public Exemplar() {}

    public Integer getId() { return id; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
