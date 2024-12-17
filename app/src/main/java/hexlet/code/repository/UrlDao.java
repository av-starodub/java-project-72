package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.repository.exception.UrlDaoException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlDao extends AbstractBaseDao {

    public static Url save(Url url) {
        var name = url.getName();
        var createdAt = Timestamp.valueOf(LocalDateTime.now());
        var urlId = executeStatement("INSERT INTO urls (name, created_at) VALUES (?, ?)", List.of(name, createdAt));
        return new Url(urlId, name, createdAt.toLocalDateTime());
    }

    public static Optional<Url> findById(long id) {
        return findByAttribute("SELECT * FROM urls WHERE id = ?", id);
    }

    public static Optional<Url> findByName(String url) {
        return findByAttribute("SELECT * FROM urls WHERE name = ?", url);
    }

    private static Optional<Url> findByAttribute(String sqlQuery, Object attribute) {
        return executeSelect(sqlQuery, List.of(attribute), resultSet -> {
            try {
                if (resultSet.next()) {
                    return mapResultSetToUrl(resultSet);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new UrlDaoException("Failed to handle ResultSet", e);
            }
        });
    }

    public static List<Url> findAll() {
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

    private static Url mapResultSetToUrl(ResultSet resultSet) {
        try {
            var urlId = resultSet.getLong("id");
            var urlName = resultSet.getString("name");
            var createdAt = resultSet.getTimestamp("created_at");
            return new Url(urlId, urlName, createdAt.toLocalDateTime());
        } catch (SQLException e) {
            throw new UrlDaoException("Failed to map ResultSet to Url", e);
        }
    }
}
