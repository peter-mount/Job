-- The scheduler's schema

-- The run mode - must match the ScheduleRunMode enum
CREATE TYPE schedulerunmode AS ENUM (
    -- Job is disabled
    'DISABLED',
    -- A RUN_ONCE job states
    'SINGLE_WAITING',
    'SINGLE_RUNNING',
    'SINGLE_COMPLETED',
    'SINGLE_FAILED',
    -- Repeatable jobs
    'REPEAT_WAITING',
    'REPEAT_RUNNING',
    'REPEAT_COMPLETED',
    'REPEAT_FAILED'
);

CREATE TABLE schedule (
    id      SERIAL NOT NULL,
    -- Job identity
    cluster BIGINT NOT NULL REFERENCES cluster(id),
    job     BIGINT NOT NULL REFERENCES job(id),
    -- description
    descr   TEXT NOT NULL,
    -- the job mode
    mode    schedulerunmode NOT NULL default 'DISABLED',
    -- when this job last run
    lastRun TIMESTAMP WITHOUT TIME ZONE,
    -- when the job is next due to run
    nextRun TIMESTAMP WITHOUT TIME ZONE,
    -- textual description of last result, i.e. fail then exception text
    status TEXT,
    -- For REPEAT mode, the amount of time between invocations
    step    INTERVAL,

    PRIMARY KEY (id)
);

CREATE INDEX schedule_cj ON schedule(cluster,job);
CREATE INDEX schedule_mn ON schedule(mode,nextRun);
CREATE INDEX schedule_n ON schedule(nextRun);

GRANT ALL ON schedule TO job;

