-- The scheduler's schema
DROP TABLE schedule;
DROP TYPE schedulestatus;
DROP TYPE scheduletype;

DROP TYPE schedulerunmode;
DROP TYPE schedulestatus;

-- The job status
CREATE TYPE schedulestate AS ENUM (
    -- Never run
    'WAITING',
    -- Schedule triggered, waiting to be run
    'PENDING',
    -- Job running
    'RUNNING',
    -- Job completed
    'COMPLETED',
    -- Job failed
    'FAILED'
);

--- When the schedule kicks in
CREATE TYPE scheduletype AS ENUM (
    -- Run the job once
    'RUN_ONCE',
    -- Every minute
    'MINUTE',
    -- Every hour
    'HOURLY',
    -- Once a day
    'DAILY',
    -- Once a week
    'WEEKLY'
);

CREATE TABLE schedule (
    id      SERIAL NOT NULL,
    -- Job identity
    cluster BIGINT NOT NULL REFERENCES cluster(id),
    job     BIGINT NOT NULL REFERENCES job(id),
    -- description
    descr   TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    state   schedulestate NOT NULL DEFAULT 'WAITING',
    type    scheduletype NOT NULL DEFAULT 'RUN_ONCE',
    -- when this job last run
    lastRun TIMESTAMP WITHOUT TIME ZONE,
    -- when the job is next due to run
    nextRun TIMESTAMP WITHOUT TIME ZONE,
    -- textual description of last result, i.e. fail then exception text
    status TEXT,
    -- For REPEAT mode, the amount of time between invocations
    step    INTEGER,

    PRIMARY KEY (id)
);

CREATE INDEX schedule_cj ON schedule(cluster,job);
CREATE INDEX schedule_sn ON schedule(state,nextRun);
CREATE INDEX schedule_n ON schedule(nextRun);

GRANT ALL ON schedule TO job;

INSERT INTO schedule values (1,1,1,'test',true,'WAITING','MINUTE',null,now(),'',1);

CREATE OR REPLACE FUNCTION schedule_next()
RETURNS TABLE (cluster NAME,job NAME)
AS $$
DECLARE
    rec     RECORD;
BEGIN
    FOR rec IN
        SELECT c.name AS cluster, j.name AS job,
            s.id, s.enabled,
            s.state, s.type, s.nextRun
            FROM schedule s
            INNER JOIN cluster c ON s.cluster=c.id
            INNER JOIN job j ON s.job=j.id
            WHERE j.enabled AND s.enabled
              AND (state = 'WAITING' OR (type!='RUN_ONCE' AND state IN ('COMPLETED','FAILED')))
    LOOP
        UPDATE schedule SET state='PENDING' WHERE id=rec.id;
        RETURN NEXT;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
