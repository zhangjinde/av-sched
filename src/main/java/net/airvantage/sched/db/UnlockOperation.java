package net.airvantage.sched.db;

import java.io.PrintStream;

import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.operations.DatabaseOperation;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public class UnlockOperation extends DatabaseOperation<UnlockOperation> {

    @Override
    public UnlockOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
            DatabaseOperationOption option, PrintStream printStream) {

        try {

            if (option == null) {
                option = new DatabaseOperationOption();
            }

            SqlRunner runner = getSqlRunner(connectionProvider);

            try {
                runner.insert("delete from " + option.getChangelogTable() + " where ID = ?", -1);

            } finally {
                runner.closeConnection();
            }

        } catch (Exception ex) {
            throw new MigrationException("Migration lock release failed.", ex);
        }

        return this;
    }
}
