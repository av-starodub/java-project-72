package hexlet.code.repository;

import hexlet.code.datasource.DriverManager;
import hexlet.code.repository.exception.DataBaseOperationException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class AbstractBaseDao {

    public static final DataSource DATASOURCE = DriverManager.createConnectionPool();

    private static final int PRIMARY_KEY_COLUMN_INDEX = 1;

    protected final boolean isExist(String sqlQuery, List<Object> params) {
        checkArgs(sqlQuery, params);
        return executeTransaction(connection -> {
            try (var preparedStatement = connection.prepareStatement(sqlQuery)) {
                setPreparedStatementParameters(preparedStatement, params);
                try (var resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            } catch (SQLException e) {
                throw new DataBaseOperationException(
                        "Failed to execute '%s' with params=%s".formatted(sqlQuery, params.toString()), e
                );
            }
        });
    }

    protected final long executeStatement(String sqlQuery, List<Object> params) {
        checkArgs(sqlQuery, params);

        return executeTransaction(connection -> {
            try (var preparedStatement =
                         connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                setPreparedStatementParameters(preparedStatement, params);
                preparedStatement.executeUpdate();

                try (var resultSet = preparedStatement.getGeneratedKeys()) {
                    resultSet.next();
                    return resultSet.getLong(PRIMARY_KEY_COLUMN_INDEX);
                }

            } catch (SQLException e) {
                throw new DataBaseOperationException(
                        "Failed to execute '%s' with params=%s".formatted(sqlQuery, params.toString()), e
                );
            }
        });
    }

    protected final <T> Optional<T> executeSelect(
            String sqlQuery, List<Object> params, Function<ResultSet, T> rsHandler
    ) {
        checkArgs(sqlQuery, params);
        requireNonNull(rsHandler, "the ResultSet handler function must not be null ");

        return executeTransaction(connection -> {
            try (var preparedStatement = connection.prepareStatement(sqlQuery)) {
                setPreparedStatementParameters(preparedStatement, params);
                try (var resultSet = preparedStatement.executeQuery()) {
                    return Optional.ofNullable(rsHandler.apply(resultSet));
                }
            } catch (SQLException e) {
                throw new DataBaseOperationException(
                        "Failed to execute '%s' with params=%s".formatted(sqlQuery, params.toString()), e
                );
            }
        });
    }

    protected final boolean executeDelete(String sqlQuery, List<Object> params) {
        checkArgs(sqlQuery, params);
        return executeTransaction(connection -> {
            try (var preparedStatement =
                         connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                setPreparedStatementParameters(preparedStatement, params);
                return preparedStatement.executeUpdate() > 0;

            } catch (SQLException e) {
                throw new DataBaseOperationException(
                        "Failed to execute '%s' with params=%s".formatted(sqlQuery, params.toString()), e
                );
            }
        });
    }

    private void setPreparedStatementParameters(PreparedStatement preparedStatement, List<Object> queryParams)
            throws SQLException {
        for (var idx = 0; idx < queryParams.size(); idx++) {
            preparedStatement.setObject(idx + 1, queryParams.get(idx));
        }
    }

    private void checkArgs(String sqlQuery, List<Object> params) {
        requireNonNull(sqlQuery, "Parameter sqlQuery must not be null");
        requireNonNull(params, "The list with sql-query parameters can be empty, but not null");
        if (sqlQuery.isEmpty()) {
            throw new IllegalArgumentException("Parameter sqlQuery must not be empty");
        }
    }

    private <V> V executeTransaction(Function<Connection, V> action) {
        requireNonNull(action, "Parameter action must not be null");

        return wrapException(() -> {
            try (var connection = DATASOURCE.getConnection()) {
                connection.setAutoCommit(false);
                var savePoint = connection.setSavepoint();
                try {
                    var result = action.apply(connection);
                    connection.commit();
                    return result;
                } catch (SQLException e) {
                    connection.rollback(savePoint);
                    throw new DataBaseOperationException("Failed to execute transaction", e);
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        });
    }

    private <V> V wrapException(Callable<V> action) {
        try {
            return action.call();
        } catch (Exception e) {
            throw new DataBaseOperationException(e.getMessage(), e);
        }
    }
}
