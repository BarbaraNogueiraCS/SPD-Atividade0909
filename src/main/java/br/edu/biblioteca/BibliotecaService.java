package br.edu.biblioteca;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;

/** Circulação: cada operação é executada em uma única transação JPA. */
public class BibliotecaService {
    private final Database db;
    public BibliotecaService(Database db) { this.db = db; }

    public Emprestimo emprestar(Leitor leitor, Exemplar exemplar, LocalDate inicio, LocalDate prazo) {
        if (inicio == null || prazo == null || prazo.isBefore(inicio))
            throw new IllegalArgumentException("Prazo deve ser igual ou posterior ao início");
        if (leitor == null || leitor.getId() == null || exemplar == null || exemplar.getId() == null)
            throw new IllegalArgumentException("Leitor e exemplar devem estar salvos");
        return db.transaction(em -> {
            Leitor leitorAtual = em.find(Leitor.class, leitor.getId());
            Exemplar exemplarAtual = em.find(Exemplar.class, exemplar.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (leitorAtual == null || exemplarAtual == null)
                throw new IllegalArgumentException("Leitor ou exemplar inexistente");
            Emprestimo e = new Emprestimo();
            e.setLeitor(leitorAtual); e.setExemplar(exemplarAtual);
            e.setRealizadoEm(inicio); e.setPrevistoPara(prazo);
            // A unicidade no H2 bloqueia empréstimos ativos concorrentes, inclusive fora do serviço.
            em.persist(e);
            return e;
        });
    }

    public void devolver(int id, LocalDate data) {
        db.transaction(em -> {
            Emprestimo e = em.find(Emprestimo.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (e == null) throw new IllegalArgumentException("Empréstimo inexistente");
            if (e.getDevolvidoEm() != null) throw new IllegalStateException("Empréstimo já devolvido");
            if (data == null || data.isBefore(e.getRealizadoEm()))
                throw new IllegalArgumentException("Devolução anterior ao empréstimo");
            e.setDevolvidoEm(data); // Dirty checking: Hibernate grava a alteração no commit.
            return null;
        });
    }
}
