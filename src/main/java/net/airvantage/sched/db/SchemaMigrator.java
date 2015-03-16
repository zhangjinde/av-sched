package net.airvantage.sched.db;

import java.io.File;

import javax.sql.DataSource;

import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.JdbcConnectionProvider;
import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.operations.UpOperation;

public class SchemaMigrator {

    private ConnectionProvider connectionProvider;
    private FileMigrationLoader fileMigrationLoader;

    public SchemaMigrator(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        fileMigrationLoader = new FileMigrationLoader(new File("src/main/resources/migrations"), "utf-8", null);
    }

    @Deprecated
    public SchemaMigrator(DataSource dataSource) {
        connectionProvider = new DataSourceConnectionProvider(dataSource);
        fileMigrationLoader = new FileMigrationLoader(new File("src/main/resources/migrations"), "utf-8", null);
    }

    public void bootstrap() {
        new BootstrapOperation(true).operate(connectionProvider, fileMigrationLoader, null, null);
    }

    public void migrate() {
        new LockOperation().operate(connectionProvider, fileMigrationLoader, null, null);
        new UpOperation().operate(connectionProvider, fileMigrationLoader, null, null);
        new UnlockOperation().operate(connectionProvider, fileMigrationLoader, null, null);
    }

}
