package br.edu.biblioteca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaTest {
    @TempDir Path pasta;

    @Test void relacionamentosCrudEReabertura() throws Exception {
        String arquivo = pasta.resolve("teste.db").toString();
        int leitorId;
        try (Database db = new Database(arquivo)) {
            Leitor leitor = new Leitor(); leitor.setNome("Ana"); leitor.setEmail("ana@example.com");
            db.dao(Leitor.class).create(leitor); leitorId = leitor.getId();
            Carteirinha c = new Carteirinha(); c.setLeitor(leitor); c.setNumero("001"); c.setEmitidaEm("2026-09-09");
            db.dao(Carteirinha.class).create(c);
            assertEquals("Ana", db.dao(Carteirinha.class).queryForId(c.getId()).getLeitor().getNome());
            Carteirinha outra = new Carteirinha(); outra.setLeitor(leitor); outra.setNumero("002"); outra.setEmitidaEm("2026-09-09");
            assertThrows(SQLException.class, () -> db.dao(Carteirinha.class).create(outra));
            Autor a = new Autor(); a.setNome("Autora A"); db.dao(Autor.class).create(a);
            Autor b = new Autor(); b.setNome("Autor B"); db.dao(Autor.class).create(b);
            Livro livro = new Livro(); livro.setTitulo("Livro A"); livro.setIsbn("9780000000001"); db.dao(Livro.class).create(livro);
            Livro segundo = new Livro(); segundo.setTitulo("Livro B"); segundo.setIsbn("9780000000002"); db.dao(Livro.class).create(segundo);
            for (Livro l : java.util.List.of(livro, segundo)) {
                for (Autor autor : java.util.List.of(a,b)) {
                    LivroAutor la = new LivroAutor(); la.setLivro(l); la.setAutor(autor); db.dao(LivroAutor.class).create(la);
                }
            }
            assertEquals(2, db.dao(LivroAutor.class).queryForEq("livro_id", livro.getId()).size());
            assertEquals(2, db.dao(LivroAutor.class).queryForEq("autor_id", a.getId()).size());
            LivroAutor repetida = new LivroAutor(); repetida.setLivro(livro); repetida.setAutor(a);
            assertThrows(SQLException.class, () -> db.dao(LivroAutor.class).create(repetida));
            Exemplar e = new Exemplar(); e.setLivro(livro); e.setCodigo("EX1"); db.dao(Exemplar.class).create(e);
            Exemplar copia = new Exemplar(); copia.setLivro(livro); copia.setCodigo("EX2"); db.dao(Exemplar.class).create(copia);
            assertEquals(2, db.dao(Exemplar.class).queryForEq("livro_id", livro.getId()).size());
            BibliotecaService service = new BibliotecaService(db);
            LocalDate inicio = LocalDate.of(2026,9,9);
            assertThrows(IllegalArgumentException.class, () -> service.emprestar(leitor,e,inicio,inicio.minusDays(1)));
            Emprestimo emp = service.emprestar(leitor,e,inicio,inicio.plusDays(7));
            assertThrows(SQLException.class, () -> service.emprestar(leitor,e,inicio,inicio.plusDays(7)));
            assertThrows(IllegalArgumentException.class, () -> service.devolver(emp.getId(),inicio.minusDays(1)));
            service.devolver(emp.getId(),inicio.plusDays(2));
            assertThrows(IllegalStateException.class, () -> service.devolver(emp.getId(),inicio.plusDays(3)));
            service.emprestar(leitor,e,inicio.plusDays(3),inicio.plusDays(10));
            assertEquals(2, db.dao(Emprestimo.class).countOf());
            assertThrows(SQLException.class, () -> db.dao(Leitor.class).delete(leitor));
            // A FK protege também inserções, independentemente do serviço.
            db.dao(Leitor.class).executeRaw("INSERT INTO leitor(id,nome,email) VALUES(999,'Temporário','temp@example.com')");
            Leitor removido = db.dao(Leitor.class).queryForId(999); db.dao(Leitor.class).delete(removido);
            Carteirinha orfa = new Carteirinha(); orfa.setLeitor(removido); orfa.setNumero("999"); orfa.setEmitidaEm("2026-09-09");
            assertThrows(SQLException.class, () -> db.dao(Carteirinha.class).create(orfa));
            leitor.setNome("Ana Atualizada"); db.dao(Leitor.class).update(leitor);
            Autor temporario = new Autor(); temporario.setNome("Temporário"); db.dao(Autor.class).create(temporario);
            db.dao(Autor.class).delete(temporario); assertNull(db.dao(Autor.class).queryForId(temporario.getId()));
        }
        try (Database db = new Database(arquivo)) {
            assertEquals("Ana Atualizada", db.dao(Leitor.class).queryForId(leitorId).getNome());
            assertEquals(2, db.dao(Emprestimo.class).countOf());
        }
    }
}
