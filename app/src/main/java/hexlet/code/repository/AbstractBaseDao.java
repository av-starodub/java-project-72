package hexlet.code.repository;

import hexlet.code.datasource.DataSourceConfigurer;
import hexlet.code.repository.exception.DataBaseOperationException;
import hexlet.code.repository.exception.DuplicateEntityException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class AbstractBaseDao {

    public static final DataSource DATASOURCE = DataSourceConfigurer.createHikariDataSource();

    private static final int PRIMARY_KEY_COLUMN_INDEX = 1;

    protected final long executeStatement(String sqlQuery, List<Object> params) {
        checkArgs(sqlQuery, params);

        return executeTransaction(connection -> {
            try (var preparedStatement =
                         connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

                setPreparedStatementParameters(preparedStatement, params);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataBaseOperationException(
                            "No rows affected for query '%s' with params=%s".formatted(sqlQuery, params)
                    );
                }

                try (var resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getLong(PRIMARY_KEY_COLUMN_INDEX);
                    } else {
                        throw new DataBaseOperationException(
                                "No generated keys returned for query '%s' with params=%s".formatted(sqlQuery, params)
                        );
                    }
                }

            } catch (SQLException e) {
                if ("23505".equals(e.getSQLState()) || e instanceof SQLIntegrityConstraintViolationException) {
                    throw new DuplicateEntityException(
                            "Duplicate entity detected for query '%s' with params=%s".formatted(sqlQuery, params)
                    );
                } else {
                    throw new DataBaseOperationException(
                            "Failed to execute '%s' with params=%s".formatted(sqlQuery, params.toString()), e
                    );
                }
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
        } catch (DuplicateEntityException e) {
            throw e;
        } catch (Exception e) {
            throw new DataBaseOperationException(e.getMessage(), e);
        }
    }
}
