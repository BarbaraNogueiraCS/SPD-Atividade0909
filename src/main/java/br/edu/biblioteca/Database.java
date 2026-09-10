package br.edu.biblioteca;

import jakarta.persistence.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;
import org.h2.tools.RunScript;

/** Uma fábrica por banco; cada operação possui EntityManager e transação próprios. */
public class Database implements AutoCloseable {
    private final EntityManagerFactory factory;

    /** Recebe o caminho base do banco; o H2 acrescenta a extensão .mv.db. */
    public Database(String arquivo) throws SQLException, IOException {
        String url = "jdbc:h2:file:" + Path.of(arquivo).toAbsolutePath().normalize();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var input = Database.class.getResourceAsStream("/schema.sql")) {
            if (input == null) throw new IOException("schema.sql não encontrado");
            RunScript.execute(connection, new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        factory = Persistence.createEntityManagerFactory("biblioteca", Map.of("jakarta.persistence.jdbc.url", url));
    }

    public <T> Repository<T> repository(Class<T> entidade) {
        return new Repository<>(this, entidade);
    }

    /** Confirma a operação inteira ou desfaz suas alterações em caso de erro. */
    public <R> R transaction(Function<EntityManager, R> operacao) {
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R resultado = operacao.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException | Error e) {
            if (tx.isActive()) {
                try { tx.rollback(); } catch (RuntimeException rollbackError) { e.addSuppressed(rollbackError); }
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override public void close() { factory.close(); }
}
