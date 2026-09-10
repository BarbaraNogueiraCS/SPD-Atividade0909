package br.edu.biblioteca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaTest {
    @TempDir Path pasta;

    @Test void rollbackDesfazTodasAsOperacoes() throws Exception {
        try (Database db = new Database(pasta.resolve("rollback").toString())) {
            assertThrows(IllegalStateException.class, () -> db.transaction(em -> {
                Autor autor = new Autor(); autor.setNome("Não deve permanecer"); em.persist(autor);
                em.flush();
                throw new IllegalStateException("Falha depois da gravação");
            }));
            assertEquals(0, db.repository(Autor.class).count());
            Autor autor = new Autor(); autor.setNome("Nova transação"); db.repository(Autor.class).save(autor);
            assertEquals(1, db.repository(Autor.class).count());
        }
    }

    @Test void bancoValidaDatasEUnicidadeMesmoSemServico() throws Exception {
        try (Database db = new Database(pasta.resolve("restricoes").toString())) {
            Leitor l = new Leitor(); l.setNome("Leitor"); l.setEmail("leitor@example.com"); db.repository(Leitor.class).save(l);
            Livro livro = new Livro(); livro.setTitulo("Livro"); livro.setIsbn("9780000000003"); db.repository(Livro.class).save(livro);
            Exemplar exemplar = new Exemplar(); exemplar.setLivro(livro); exemplar.setCodigo("E1"); db.repository(Exemplar.class).save(exemplar);
            LocalDate dia = LocalDate.of(2026,9,9);
            assertThrows(PersistenceException.class, () -> db.repository(Emprestimo.class).save(emprestimo(l, exemplar, dia, dia.minusDays(1))));
            Emprestimo devolucaoInvalida = emprestimo(l, exemplar, dia, dia.plusDays(7));
            devolucaoInvalida.setDevolvidoEm(dia.minusDays(1));
            assertThrows(PersistenceException.class, () -> db.repository(Emprestimo.class).save(devolucaoInvalida));
            db.repository(Emprestimo.class).save(emprestimo(l, exemplar, dia, dia.plusDays(7)));
            assertThrows(PersistenceException.class, () -> db.repository(Emprestimo.class).save(emprestimo(l, exemplar, dia, dia.plusDays(7))));
            assertEquals(1, db.repository(Emprestimo.class).count());
            // Coleção 1:N é lazy: navegação dentro da transação.
            assertEquals(1, db.transaction(em -> em.find(Livro.class, livro.getId()).getExemplares().size()).intValue());
        }
    }

    @Test void emprestimosConcorrentesNaoDuplicamExemplar() throws Exception {
        try (Database db = new Database(pasta.resolve("concorrencia").toString())) {
            Leitor l = new Leitor(); l.setNome("Leitor"); l.setEmail("concorrente@example.com"); db.repository(Leitor.class).save(l);
            Livro livro = new Livro(); livro.setTitulo("Livro"); livro.setIsbn("9780000000004"); db.repository(Livro.class).save(livro);
            Exemplar e = new Exemplar(); e.setLivro(livro); e.setCodigo("E1"); db.repository(Exemplar.class).save(e);
            var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
            var inicio = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Boolean> tentativa = () -> {
                inicio.await();
                try {
                    new BibliotecaService(db).emprestar(l, e, LocalDate.of(2026,9,9), LocalDate.of(2026,9,16));
                    return true;
                } catch (PersistenceException esperado) { return false; }
            };
            try {
                var a = executor.submit(tentativa); var b = executor.submit(tentativa); inicio.countDown();
                int sucessos = (a.get(20, java.util.concurrent.TimeUnit.SECONDS) ? 1 : 0)
                    + (b.get(20, java.util.concurrent.TimeUnit.SECONDS) ? 1 : 0);
                assertEquals(1, sucessos);
                assertEquals(1, db.repository(Emprestimo.class).count());
            } finally { executor.shutdownNow(); }
        }
    }

    private Emprestimo emprestimo(Leitor leitor, Exemplar exemplar, LocalDate inicio, LocalDate prazo) {
        Emprestimo e = new Emprestimo(); e.setLeitor(leitor); e.setExemplar(exemplar);
        e.setRealizadoEm(inicio); e.setPrevistoPara(prazo); return e;
    }

    @Test void relacionamentosCrudEReabertura() throws Exception {
        String arquivo = pasta.resolve("teste.db").toString();
        int leitorId;
        try (Database db = new Database(arquivo)) {
            Leitor leitor = new Leitor(); leitor.setNome("Ana"); leitor.setEmail("ana@example.com");
            db.repository(Leitor.class).save(leitor); leitorId = leitor.getId();
            Carteirinha c = new Carteirinha(); c.setLeitor(leitor); c.setNumero("001"); c.setEmitidaEm(LocalDate.parse("2026-09-09"));
            db.repository(Carteirinha.class).save(c);
            assertEquals("Ana", db.repository(Carteirinha.class).findById(c.getId()).getLeitor().getNome());
            Carteirinha outra = new Carteirinha(); outra.setLeitor(leitor); outra.setNumero("002"); outra.setEmitidaEm(LocalDate.parse("2026-09-09"));
            assertThrows(PersistenceException.class, () -> db.repository(Carteirinha.class).save(outra));
            Autor a = new Autor(); a.setNome("Autora A"); db.repository(Autor.class).save(a);
            Autor b = new Autor(); b.setNome("Autor B"); db.repository(Autor.class).save(b);
            Livro livro = new Livro(); livro.setTitulo("Livro A"); livro.setIsbn("9780000000001"); db.repository(Livro.class).save(livro);
            Livro segundo = new Livro(); segundo.setTitulo("Livro B"); segundo.setIsbn("9780000000002"); db.repository(Livro.class).save(segundo);
            for (Livro l : java.util.List.of(livro, segundo)) {
                for (Autor autor : java.util.List.of(a,b)) {
                    LivroAutor la = new LivroAutor(); la.setLivro(l); la.setAutor(autor); db.repository(LivroAutor.class).save(la);
                }
            }
            assertEquals(2, db.repository(LivroAutor.class).findBy("livro.id", livro.getId()).size());
            assertEquals(2, db.repository(LivroAutor.class).findBy("autor.id", a.getId()).size());
            LivroAutor repetida = new LivroAutor(); repetida.setLivro(livro); repetida.setAutor(a);
            assertThrows(PersistenceException.class, () -> db.repository(LivroAutor.class).save(repetida));
            Exemplar e = new Exemplar(); e.setLivro(livro); e.setCodigo("EX1"); db.repository(Exemplar.class).save(e);
            Exemplar copia = new Exemplar(); copia.setLivro(livro); copia.setCodigo("EX2"); db.repository(Exemplar.class).save(copia);
            assertEquals(2, db.repository(Exemplar.class).findBy("livro.id", livro.getId()).size());
            BibliotecaService service = new BibliotecaService(db);
            LocalDate inicio = LocalDate.of(2026,9,9);
            assertThrows(IllegalArgumentException.class, () -> service.emprestar(leitor,e,inicio,inicio.minusDays(1)));
            Emprestimo emp = service.emprestar(leitor,e,inicio,inicio.plusDays(7));
            assertThrows(PersistenceException.class, () -> service.emprestar(leitor,e,inicio,inicio.plusDays(7)));
            assertThrows(IllegalArgumentException.class, () -> service.devolver(emp.getId(),inicio.minusDays(1)));
            service.devolver(emp.getId(),inicio.plusDays(2));
            assertThrows(IllegalStateException.class, () -> service.devolver(emp.getId(),inicio.plusDays(3)));
            service.emprestar(leitor,e,inicio.plusDays(3),inicio.plusDays(10));
            assertEquals(2, db.repository(Emprestimo.class).count());
            assertThrows(PersistenceException.class, () -> db.repository(Leitor.class).delete(leitor));
            // A FK protege também inserções, independentemente do serviço.
            Leitor removido = new Leitor(); removido.setNome("Temporário"); removido.setEmail("temp@example.com");
            db.repository(Leitor.class).save(removido); db.repository(Leitor.class).delete(removido);
            Carteirinha orfa = new Carteirinha(); orfa.setLeitor(removido); orfa.setNumero("999"); orfa.setEmitidaEm(LocalDate.parse("2026-09-09"));
            assertThrows(PersistenceException.class, () -> db.repository(Carteirinha.class).save(orfa));
            leitor.setNome("Ana Atualizada"); db.repository(Leitor.class).update(leitor);
            Autor temporario = new Autor(); temporario.setNome("Temporário"); db.repository(Autor.class).save(temporario);
            db.repository(Autor.class).delete(temporario); assertNull(db.repository(Autor.class).findById(temporario.getId()));
        }
        try (Database db = new Database(arquivo)) {
            assertEquals("Ana Atualizada", db.repository(Leitor.class).findById(leitorId).getNome());
            assertEquals(2, db.repository(Emprestimo.class).count());
        }
    }
}
