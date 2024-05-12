package hexlet.code.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractDao {
    public static final DataSource DATASOURCE = createConnectionPool();


    private static DataSource createConnectionPool() {
        var config = new HikariConfig();

        var appMode = System.getenv().get("APP_MODE");
        if ("production".equals(appMode)) {
            config.setJdbcUrl(System.getenv().get("JDBC_DATABASE_URL"));
            config.setUsername(System.getenv().get("USER_NAME"));
            config.setPassword(System.getenv().get("PASSWORD"));
        } else {
            var fileName = "db_default.properties";
            var dbProp = new Properties();
            try (var is = AbstractDao.class.getClassLoader().getResourceAsStream(fileName)) {
                dbProp.load(is);
            } catch (IOException e) {
                throw new RuntimeException("db default properties load error ");
            }
            config.setJdbcUrl(dbProp.getProperty("db.url"));
            config.setUsername(dbProp.getProperty("db.user"));
            config.setPassword(dbProp.getProperty("db.password"));
        }

        config.setPoolName("HikariPageAnalyzerPool");
        config.setAutoCommit(false);
        config.setRegisterMbeans(true);

        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(3000); //ms
        config.setIdleTimeout(60000); //ms
        config.setMaxLifetime(600000);//ms

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}
