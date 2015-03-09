package net.airvantage.sched.db;

import java.io.PrintStream;

import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.operations.DatabaseOperation;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public class LockOperation extends DatabaseOperation<LockOperation> {

    @Override
    public LockOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
            DatabaseOperationOption option, PrintStream printStream) {

        try {

            if (option == null) {
                option = new DatabaseOperationOption();
            }

            SqlRunner runner = getSqlRunner(connectionProvider);
            String time = generateAppliedTimeStampAsString();

            try {
                runner.insert("insert into " + option.getChangelogTable()
                        + " (ID, APPLIED_AT, DESCRIPTION) values (?,?,?)", -1, time, "Upgrade in progress ...");

            } finally {
                runner.closeConnection();
            }

        } catch (Exception ex) {
            throw new MigrationException("Migration lock acquisition failed.", ex);
        }

        return this;
    }
}
