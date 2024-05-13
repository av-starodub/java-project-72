package hexlet.code.repository;

import hexlet.code.model.Url;

import java.util.List;
import java.util.Optional;

public final class UrlDao extends AbstractBaseDao<Url> {

    @Override
    public long insert(Url entity) {
        return -1;
    }

    @Override
    public Optional<Url> findById(long id) {
        return Optional.empty();
    }

    @Override
    public List<Url> findAll() {
        return null;
    }

    @Override
    public long update(Url entity) {
        return -1;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }

    @Override
    public boolean clear() {
        return false;
    }
}
