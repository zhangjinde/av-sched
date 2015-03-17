package net.airvantage.sched.db;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

/**
 * SchemaMigrator using Flyway.
 * 
 * The migrations are in src/main/resources/db/migration.
 * 
 * Note that flyway supports clustering by default :http://flywaydb.org/documentation/faq.html#parallel 
 * 
 */
public class SchemaMigrator {

    private Flyway flyway;

    public SchemaMigrator(DataSource dataSource) {
        this.flyway = new Flyway();
        flyway.setDataSource(dataSource);
    }

    public void migrate() {
        flyway.migrate();
    }

}
