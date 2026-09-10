package br.edu.biblioteca;

import jakarta.persistence.criteria.Path;
import java.util.List;

/** CRUD JPA. Objetos retornados ficam destacados após o fechamento da transação. */
public class Repository<T> {
    private final Database db;
    private final Class<T> type;

    Repository(Database db, Class<T> type) { this.db = db; this.type = type; }

    public T save(T entidade) {
        return db.transaction(em -> { em.persist(entidade); return entidade; });
    }

    public T findById(int id) {
        return db.transaction(em -> em.find(type, id));
    }

    /** Caminhos são atributos Java, por exemplo livro.id, e não nomes de colunas SQL. */
    public List<T> findBy(String atributo, Object valor) {
        return db.transaction(em -> {
            var cb = em.getCriteriaBuilder();
            var query = cb.createQuery(type);
            var root = query.from(type);
            Path<?> path = root;
            for (String parte : atributo.split("\\.")) path = path.get(parte);
            query.select(root).where(valor == null ? cb.isNull(path) : cb.equal(path, valor));
            return em.createQuery(query).getResultList();
        });
    }

    public long count() {
        return db.transaction(em -> {
            var cb = em.getCriteriaBuilder();
            var query = cb.createQuery(Long.class);
            query.select(cb.count(query.from(type)));
            return em.createQuery(query).getSingleResult();
        });
    }

    public T update(T entidade) {
        return db.transaction(em -> em.merge(entidade));
    }

    public void delete(T entidade) {
        db.transaction(em -> {
            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entidade);
            T atual = em.find(type, id);
            if (atual != null) em.remove(atual);
            return null;
        });
    }
}
