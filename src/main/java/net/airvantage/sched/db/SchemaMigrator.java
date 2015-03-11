package net.airvantage.sched.db;

import java.io.File;

import javax.sql.DataSource;

import org.apache.ibatis.migration.DataSourceConnectionProvider;
import org.apache.ibatis.migration.FileMigrationLoader;
import org.apache.ibatis.migration.operations.BootstrapOperation;
import org.apache.ibatis.migration.operations.UpOperation;

public class SchemaMigrator {

    private DataSourceConnectionProvider dataSourceConnectionProvider;
    private FileMigrationLoader fileMigrationLoader;

    public SchemaMigrator(DataSource dataSource) {
        dataSourceConnectionProvider = new DataSourceConnectionProvider(dataSource);
        fileMigrationLoader = new FileMigrationLoader(new File("src/main/resources/migrations"), "utf-8", null);
    }

    public void bootstrap() {
        // new LockOperation().operate(dataSourceConnectionProvider, fileMigrationLoader, null, null);
        // new BootstrapOperation(true).operate(dataSourceConnectionProvider, fileMigrationLoader, null, null);
        new UnlockOperation().operate(dataSourceConnectionProvider, fileMigrationLoader, null, null);
    }

    public void migrate() {
        // new LockOperation().operate(dataSourceConnectionProvider, fileMigrationLoader, null, null);
        // new UpOperation().operate(dataSourceConnectionProvider, fileMigrationLoader, null, null);
        new UnlockOperation().operate(dataSourceConnectionProvider, fileMigrationLoader, null, null);
    }

}
