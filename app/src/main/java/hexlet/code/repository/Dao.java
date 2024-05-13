package hexlet.code.repository;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    long insert(T entity);

    Optional<T> findById(long id);

    List<T> findAll();

    long update(T entity);

    boolean delete(long id);

    boolean clear();
}
