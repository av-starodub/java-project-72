package hexlet.code.repository;

import hexlet.code.datasource.DriverManager;

import javax.sql.DataSource;

public abstract class AbstractDao {
    public static final DataSource DATASOURCE = DriverManager.createConnectionPool();
}
