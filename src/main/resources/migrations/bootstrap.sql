--// Create Changelog

-- Default DDL for changelog table that will keep a record of the migrations that have been run.
-- You can modify this to suit your database before running your first migration.
-- Be sure that ID and DESCRIPTION fields exist in BigInteger and String compatible fields respectively.
-- Notice that this table is used by the Oracle checker, so update it if the table schema change.

-----------------------------------------------------------------------------------------------------------------------
-- Create schema for CHANGELOG model
-----------------------------------------------------------------------------------------------------------------------  

CREATE TABLE IF NOT EXISTS CHANGELOG (
    ID NUMERIC(20,0) PRIMARY KEY,
    APPLIED_AT VARCHAR(25) NOT NULL,
    DESCRIPTION VARCHAR(255) NOT NULL
);
