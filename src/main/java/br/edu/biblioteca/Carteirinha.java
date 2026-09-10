package br.edu.biblioteca;

import jakarta.persistence.*;
import java.time.LocalDate;


/** Entidade mapeada com Jakarta Persistence. */
@Entity
@Table(name = "carteirinha")
public class Carteirinha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "leitor_id", nullable = false, unique = true)
    private Leitor leitor;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(name = "emitida_em", nullable = false)
    private LocalDate emitidaEm;

    public Carteirinha() {}

    public Integer getId() { return id; }
    public Leitor getLeitor() { return leitor; }
    public void setLeitor(Leitor leitor) { this.leitor = leitor; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getEmitidaEm() { return emitidaEm; }
    public void setEmitidaEm(LocalDate emitidaEm) { this.emitidaEm = emitidaEm; }
}
