package net.airvantage.sched.db;

import java.io.File;

import javax.sql.DataSource;

import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.JdbcConnectionProvider;
import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.operations.UpOperation;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaMigrator {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaMigrator.class);
    
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
        DatabaseOperationOption options = new DatabaseOperationOption();
        options.setAutoCommit(true);
        new BootstrapOperation(true).operate(connectionProvider, fileMigrationLoader, options, System.out);
    }

    public void migrate() {
        DatabaseOperationOption options = new DatabaseOperationOption();
        options.setAutoCommit(true);
        new LockOperation().operate(connectionProvider, fileMigrationLoader, options, System.out);
        new UpOperation().operate(connectionProvider, fileMigrationLoader, options, System.out);
        new UnlockOperation().operate(connectionProvider, fileMigrationLoader, options, System.out);
    }

}
