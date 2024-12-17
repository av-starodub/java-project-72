package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.exception.UrlCheckDaoException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public final class UrlCheckDao extends AbstractBaseDao {

    public static UrlCheck save(UrlCheck urlCheck) {
        var urlId = urlCheck.getUrlId();
        var statusCode = urlCheck.getStatusCode();
        var h1 = urlCheck.getH1();
        var title = urlCheck.getTitle();
        var description = urlCheck.getDescription();
        var createdAt = LocalDateTime.now();
        var savedUrlCheckId = executeStatement(
                "INSERT INTO url_checks (url_id, status_code, created_at, title, h1, description)"
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                List.of(urlId, statusCode, Timestamp.valueOf(createdAt), title, h1, description)
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

    public static List<UrlCheck> findChecksByUrlId(Long urlId) {
        return executeSelect(
                "SELECT * FROM url_checks WHERE url_Id = ? ORDER BY created_at DESC",
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

    public Map<Long, UrlCheck> findLatestChecks() {
        return executeSelect(
                "SELECT DISTINCT ON (url_id) * FROM url_checks ORDER BY url_id DESC, id DESC",
                List.of(),
                resultSet -> {
                    var lastChecks = new HashMap<Long, UrlCheck>();
                    try {
                        while (resultSet.next()) {
                            var check = mapResultSetToUrlCheck(resultSet);
                            lastChecks.put(check.getUrlId(), check);
                        }
                        return lastChecks;
                    } catch (SQLException e) {
                        throw new UrlCheckDaoException("Failed to handle ResultSet", e);
                    }
                }
        ).orElseThrow(() -> new UrlCheckDaoException("Unexpected error"));
    }

    private static UrlCheck mapResultSetToUrlCheck(ResultSet resultSet) {
        try {
            return UrlCheck.builder()
                    .id(resultSet.getLong("id"))
                    .urlId(resultSet.getLong("url_id"))
                    .statusCode(resultSet.getInt("status_code"))
                    .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                    .title(resultSet.getString("title"))
                    .h1(resultSet.getString("h1"))
                    .description(resultSet.getString("description"))
                    .build();
        } catch (SQLException e) {
            throw new UrlCheckDaoException("Failed to map ResultSet to UrlCheck", e);
        }
    }
}
