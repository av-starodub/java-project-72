package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.repository.exception.DuplicateUrlException;
import hexlet.code.repository.exception.UrlDaoException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlDao extends AbstractBaseDao {

    public Url save(Url url) {
        var name = url.getName();
        if (isExist("SELECT * FROM urls WHERE name = ?", List.of(name))) {
            throw new DuplicateUrlException("Url %s already exist".formatted(name));
        }
        var createdAt = Timestamp.valueOf(LocalDateTime.now());
        var urlId = executeStatement(
                "INSERT INTO urls (name, created_at) VALUES (?, ?)", List.of(name, createdAt)
        );
        return new Url(urlId, name, createdAt);
    }

    public Optional<Url> findById(long id) {
        return executeSelect(
                "SELECT * FROM urls WHERE id = ?", List.of(id), resultSet -> {
                    try {
                        if (resultSet.next()) {
                            return mapResultSetToUrl(resultSet);
                        } else {
                            return null;
                        }
                    } catch (SQLException e) {
                        throw new UrlDaoException("Failed to handle ResultSet", e);
                    }
                }
        );
    }

    public List<Url> findAll() {
        return executeSelect("SELECT * FROM urls", List.of(), resultSet -> {
            var urls = new ArrayList<Url>();
            try {
                while (resultSet.next()) {
                    var url = mapResultSetToUrl(resultSet);
                    urls.add(url);
                }
                return urls;
            } catch (SQLException e) {
                throw new UrlDaoException("Failed to handle ResultSet", e);
            }
        }).orElseThrow(() -> new UrlDaoException("Unexpected error"));
    }

    private Url mapResultSetToUrl(ResultSet resultSet) {
        try {
            var urlId = resultSet.getLong("id");
            var urlName = resultSet.getString("name");
            var createdAt = resultSet.getTimestamp("created_at");
            return new Url(urlId, urlName, createdAt);
        } catch (SQLException e) {
            throw new UrlDaoException("Failed to map ResultSet to Url", e);
        }
    }
}
