package hexlet.code.repository;

import hexlet.code.datasource.DriverManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class AbstractBaseDao<T> implements Dao<T> {

    public static final DataSource DATASOURCE = DriverManager.createConnectionPool();

    private static final int PRIMARY_KEY_COLUMN_INDEX = 1;

    protected final long executeStatement(String sqlQuery, List<Object> params) {
        checkArgs(sqlQuery, params);

        return executeTransaction(connection -> {
            try (var preparedStatement =
                         connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

                for (var idx = 0; idx < params.size(); idx++) {
                    preparedStatement.setObject(idx + 1, params.get(idx));
                }
                preparedStatement.executeUpdate();

                try (var resultSet = preparedStatement.getGeneratedKeys()) {
                    resultSet.next();
                    return resultSet.getLong(PRIMARY_KEY_COLUMN_INDEX);
                }

            } catch (SQLException e) {
                throw new DataBaseOperationException("executeStatement error", e);
            }
        });
    }

    protected final Optional<T> executeSelect(String sqlQuery, List<Object> params, Function<ResultSet, T> rsHandler) {
        checkArgs(sqlQuery, params);
        requireNonNull(rsHandler, "the ResultSet handler function must not be null ");

        return executeTransaction(connection -> {
            try (var preparedStatement = connection.prepareStatement(sqlQuery)) {
                for (var idx = 0; idx < params.size(); idx++) {
                    preparedStatement.setObject(idx + 1, params.get(idx));
                }
                try (var resultSet = preparedStatement.executeQuery()) {
                    return Optional.ofNullable(rsHandler.apply(resultSet));
                }
            } catch (SQLException e) {
                throw new DataBaseOperationException("executeSelect error", e);
            }
        });
    }

    protected final boolean executeDelete(String sqlQuery, List<Object> params) {
        checkArgs(sqlQuery, params);

        return executeTransaction(connection -> {
            try (var preparedStatement =
                         connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

                for (var idx = 0; idx < params.size(); idx++) {
                    preparedStatement.setObject(idx + 1, params.get(idx));
                }

                return preparedStatement.executeUpdate() > 0;

            } catch (SQLException e) {
                throw new DataBaseOperationException("executeDelete error", e);
            }
        });
    }

    private void checkArgs(String sqlQuery, List<Object> params) {
        requireNonNull(sqlQuery, "sql-query must not be null ");
        requireNonNull(params, "the list with sql-query parameters can be empty, but not null ");
        if (sqlQuery.isEmpty()) {
            throw new IllegalArgumentException("sql query must not be empty ");
        }
    }

    private <V> V executeTransaction(Function<Connection, V> action) {
        requireNonNull(action, "action must not be null ");

        return wrapException(() -> {
            try (var connection = DATASOURCE.getConnection()) {
                var savePoint = connection.setSavepoint();
                try {
                    var result = action.apply(connection);
                    connection.commit();
                    return result;
                } catch (SQLException e) {
                    connection.rollback(savePoint);
                    throw new DataBaseOperationException("transaction error ", e);
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
