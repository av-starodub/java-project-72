package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.exception.UrlCheckDaoException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlCheckDao extends AbstractBaseDao {

    public UrlCheck save(UrlCheck urlCheck) {
        var urlId = urlCheck.getUrlId();
        var statusCode = urlCheck.getStatusCode();
        var h1 = urlCheck.getH1();
        var title = urlCheck.getTitle();
        var description = urlCheck.getDescription();
        var createdAt = Timestamp.valueOf(LocalDateTime.now());
        var savedUrlCheckId = executeStatement(
                "INSERT INTO url_checks (url_id, status_code, created_at, title, h1, description)"
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                List.of(urlId, statusCode, createdAt, title, h1, description)
        );
        return UrlCheck.builder()
                .id(savedUrlCheckId)
                .urlId(urlId)
                .statusCode(statusCode)
                .title(title)
                .h1(h1)
                .description(description)
                .createdAt(createdAt)
                .build();
    }

    public List<UrlCheck> findChecksByUrlId(Long urlId) {
        return executeSelect(
                "SELECT * FROM url_checks WHERE id = ? ORDER BY created_at DESC",
                List.of(urlId),
                resultSet -> {
                    var checks = new ArrayList<UrlCheck>();
                    try {
                        while (resultSet.next()) {
                            var check = mapResultSetToUrlCheck(resultSet);
                            checks.add(check);
                        }
                        return checks;
                    } catch (SQLException e) {
                        throw new UrlCheckDaoException("Failed to handle ResultSet", e);
                    }
                }
        ).orElseThrow(() -> new UrlCheckDaoException("Unexpected error"));
    }

    public Optional<UrlCheck> findLastCheckByUrlId(Long urlId) {
        return executeSelect(
                "SELECT * FROM url_checks WHERE id = ? ORDER BY created_at DESC LIMIT 1",
                List.of(urlId),
                resultSet -> {
                    try {
                        if (resultSet.next()) {
                            return mapResultSetToUrlCheck(resultSet);
                        } else {
                            return null;
                        }
                    } catch (SQLException e) {
                        throw new UrlCheckDaoException("Failed to handle ResultSet", e);
                    }
                }
        );
    }

    private UrlCheck mapResultSetToUrlCheck(ResultSet resultSet) {
        try {
            return UrlCheck.builder()
                    .id(resultSet.getLong("id"))
                    .urlId(resultSet.getLong("url_id"))
                    .statusCode(resultSet.getInt("status_code"))
                    .createdAt(resultSet.getTimestamp("created_at"))
                    .title(resultSet.getString("title"))
                    .h1(resultSet.getString("h1"))
                    .description(resultSet.getString("description"))
                    .build();
        } catch (SQLException e) {
            throw new UrlCheckDaoException("Failed to map ResultSet to UrlCheck", e);
        }
    }
}
