package hexlet.code.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.repository.AbstractBaseDao;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

import static java.util.Objects.nonNull;

public final class DataSourceConfigurer {
    private static final int MIN_IDLE_CONNECTIONS = 5;
    private static final int MIN_POOL_SIZE = 10;
    private static final int CONNECTION_TIMOUT = 3000; //ms
    private static final int MAX_IDLE_TIMOUT = 60000; //ms
    private static final int MAX_CONNECTION_LIFETIME = 600000; //ms

    private DataSourceConfigurer() {
    }

    public static DataSource createHikariDataSource() {
        var config = new HikariConfig();

        var dbUrl = System.getenv().get("JDBC_DATABASE_URL");
        if (nonNull(dbUrl)) {
            config.setJdbcUrl(dbUrl);
            config.setUsername(System.getenv().get("USER_NAME"));
            config.setPassword(System.getenv().get("PASSWORD"));
        } else {
            var fileName = "db_default.properties";
            var dbProp = new Properties();
            try (var is = AbstractBaseDao.class.getClassLoader().getResourceAsStream(fileName)) {
                dbProp.load(is);
            } catch (IOException e) {
                throw new DatabasePropertiesReadException("Default database properties load error", e);
            }
            config.setJdbcUrl(dbProp.getProperty("db.url"));
            config.setUsername(dbProp.getProperty("db.user"));
            config.setPassword(dbProp.getProperty("db.password"));
        }
        config.setPoolName("HikariPageAnalyzerPool");
        config.setRegisterMbeans(true);

        config.setMinimumIdle(MIN_IDLE_CONNECTIONS);
        config.setMaximumPoolSize(MIN_POOL_SIZE);
        config.setConnectionTimeout(CONNECTION_TIMOUT);
        config.setIdleTimeout(MAX_IDLE_TIMOUT);
        config.setMaxLifetime(MAX_CONNECTION_LIFETIME);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}
