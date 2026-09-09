package br.edu.biblioteca;

import java.sql.SQLException;
import java.time.LocalDate;

/** Operações de circulação. O banco também impede empréstimos ativos duplicados. */
public class BibliotecaService {
    private final Database db;
    public BibliotecaService(Database db) { this.db = db; }

    public Emprestimo emprestar(Leitor leitor, Exemplar exemplar,
            LocalDate inicio, LocalDate prazo) throws SQLException {
        if (inicio == null || prazo == null || prazo.isBefore(inicio))
            throw new IllegalArgumentException("Prazo deve ser igual ou posterior ao início");
        if (leitor == null || leitor.getId() == null || exemplar == null || exemplar.getId() == null)
            throw new IllegalArgumentException("Leitor e exemplar devem estar salvos");
        Emprestimo e = new Emprestimo();
        e.setLeitor(leitor); e.setExemplar(exemplar);
        e.setRealizadoEm(inicio.toString()); e.setPrevistoPara(prazo.toString());
        db.dao(Emprestimo.class).create(e);
        return e;
    }

    public void devolver(int id, LocalDate data) throws SQLException {
        var dao = db.dao(Emprestimo.class);
        Emprestimo e = dao.queryForId(id);
        if (e == null) throw new IllegalArgumentException("Empréstimo inexistente");
        if (e.getDevolvidoEm() != null) throw new IllegalStateException("Empréstimo já devolvido");
        if (data == null || data.isBefore(LocalDate.parse(e.getRealizadoEm())))
            throw new IllegalArgumentException("Devolução anterior ao empréstimo");
        e.setDevolvidoEm(data.toString()); dao.update(e);
    }
}
