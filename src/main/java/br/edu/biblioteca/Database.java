package br.edu.biblioteca;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcSingleConnectionSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/** Conexão única: mantém a configuração de integridade em todas as operações. */
public class Database implements AutoCloseable {
    private final JdbcSingleConnectionSource connection;

    public Database(String arquivo) throws SQLException, IOException {
        String url = "jdbc:sqlite:" + arquivo;
        connection = new JdbcSingleConnectionSource(url, java.sql.DriverManager.getConnection(url));
        try {
            Dao<Leitor, Integer> executor = dao(Leitor.class);
            executor.executeRaw("PRAGMA foreign_keys = ON");
            try (var input = Database.class.getResourceAsStream("/schema.sql")) {
                if (input == null) throw new IOException("schema.sql não encontrado");
                for (String sql : new String(input.readAllBytes(), StandardCharsets.UTF_8).split(";")) {
                    if (!sql.isBlank()) executor.executeRaw(sql.trim());
                }
            }
        } catch (SQLException | IOException | RuntimeException e) {
            try { connection.close(); } catch (Exception closeError) { e.addSuppressed(closeError); }
            throw e;
        }
    }

    public <T> Dao<T, Integer> dao(Class<T> entidade) throws SQLException {
        return DaoManager.createDao(connection, entidade);
    }

    @Override public void close() throws Exception { connection.close(); }
}
