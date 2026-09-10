package br.edu.biblioteca;

import jakarta.persistence.*;
import java.time.LocalDate;


/** Entidade mapeada com Jakarta Persistence. */
@Entity
@Table(name = "leitor")
public class Leitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    public Leitor() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
