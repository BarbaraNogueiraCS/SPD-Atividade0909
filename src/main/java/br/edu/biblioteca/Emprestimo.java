package br.edu.biblioteca;

import jakarta.persistence.*;
import java.time.LocalDate;


/** Entidade mapeada com Jakarta Persistence. */
@Entity
@Table(name = "emprestimo")
public class Emprestimo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @Column(name = "realizado_em", nullable = false)
    private LocalDate realizadoEm;

    @Column(name = "previsto_para", nullable = false)
    private LocalDate previstoPara;

    @Column(name = "devolvido_em", nullable = true)
    private LocalDate devolvidoEm;

    public Emprestimo() {}

    public Integer getId() { return id; }
    public Leitor getLeitor() { return leitor; }
    public void setLeitor(Leitor leitor) { this.leitor = leitor; }
    public Exemplar getExemplar() { return exemplar; }
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }
    public LocalDate getRealizadoEm() { return realizadoEm; }
    public void setRealizadoEm(LocalDate realizadoEm) { this.realizadoEm = realizadoEm; }
    public LocalDate getPrevistoPara() { return previstoPara; }
    public void setPrevistoPara(LocalDate previstoPara) { this.previstoPara = previstoPara; }
    public LocalDate getDevolvidoEm() { return devolvidoEm; }
    public void setDevolvidoEm(LocalDate devolvidoEm) { this.devolvidoEm = devolvidoEm; }
}
