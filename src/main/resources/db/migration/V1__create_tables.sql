create table if not exists sched_job_configs (
    id VARCHAR(255) primary key,
    url VARCHAR(255),
    timeout BIGINT 
);
create table if not exists sched_job_locks (
    id VARCHAR(255) primary key,
    expires_at TIMESTAMP
);