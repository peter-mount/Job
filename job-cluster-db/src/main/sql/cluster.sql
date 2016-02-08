-- Reference table that holds available cluster's

DROP TABLE runonce;
DROP TABLE runevery;

DROP FUNCTION nextjobs();
DROP TABLE schedule;
DROP SEQUENCE scheduleid;

DROP TABLE jobdependency;

DROP TABLE jobdef;
DROP TABLE jobname;
DROP TABLE jobnode;
DROP TABLE jobstate;
DROP TABLE jobtype;

-- ====================================================================================================
-- Normalisation of Job node's
CREATE TABLE jobnode (
    id SERIAL NOT NULL,
    name NAME NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX jobnode_n ON jobnode(name);
ALTER TABLE jobnode OWNER TO job;
ALTER SEQUENCE jobnode_id_seq OWNER TO job;

-- Normalisation of Job names
CREATE TABLE jobname (
    id          SERIAL NOT NULL,
    nodeid      BIGINT NOT NULL REFERENCES jobnode(id),
    name        NAME NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX jobname_cn ON jobname(nodeid,name);
CREATE INDEX jobname_n ON jobname(name);
ALTER TABLE jobname OWNER TO job;
ALTER SEQUENCE jobname_id_seq OWNER TO job;

-- The actual job text with versioning
CREATE TABLE jobdef (
    id      BIGINT NOT NULL REFERENCES jobname(id),
    text    TEXT NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE jobdef OWNER TO job;

-- ====================================================================================================
-- Job Dependencies - i.e. A job can only run if all dependent jobs have run successfully.
-- For example, we have a job that retrieves the current GFS Grib files. No point in running map
-- generation jobs if that job has not run correctly.

CREATE TABLE jobdependency (
    id      BIGINT NOT NULL REFERENCES jobname(id),
    depends BIGINT NOT NULL REFERENCES jobname(id),
    PRIMARY KEY(id,depends)
);
CREATE INDEX jobdependency_i ON jobdependency(id);
CREATE INDEX jobdependency_d ON jobdependency(depends);
ALTER TABLE jobdependency OWNER TO job;

-- ====================================================================================================
-- The job type
CREATE TABLE jobtype (
    id      INTEGER NOT NULL,
    name    NAME,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX jobtype_i ON jobtype(id);
CREATE UNIQUE INDEX jobtype_n ON jobtype(name);
ALTER TABLE jobtype OWNER TO job;
INSERT INTO jobtype VALUES (1,'runonce');
INSERT INTO jobtype VALUES (2,'runevery');
INSERT INTO jobtype VALUES (3,'runcron');

-- ====================================================================================================
-- The job status
CREATE TABLE jobstate (
    id      INTEGER NOT NULL,
    name    NAME,
    PRIMARY KEY(id)
);
CREATE UNIQUE INDEX jobstate_i ON jobstate(id);
CREATE UNIQUE INDEX jobstate_n ON jobstate(name);
ALTER TABLE jobstate OWNER TO job;

-- Never run
INSERT INTO jobstate VALUES (0,'WAITING');
-- Schedule triggered, waiting to be run
INSERT INTO jobstate VALUES (1,'PENDING');
-- Job running
INSERT INTO jobstate VALUES (2,'RUNNING');
-- Job completed
INSERT INTO jobstate VALUES (3,'COMPLETED');
-- Job failed
INSERT INTO jobstate VALUES (4,'FAILED');

-- ====================================================================================================
-- The core schedule table

CREATE SEQUENCE scheduleid;
ALTER SEQUENCE scheduleid OWNER TO job;

CREATE TABLE schedule (
    id          BIGINT NOT NULL,
    -- The job this schedule relates to
    jobid       BIGINT NOT NULL REFERENCES jobname(id),
    -- The job type
    type        INTEGER NOT NULL REFERENCES jobtype(id),
    -- The job state
    state       INTEGER NOT NULL REFERENCES jobstate(id) DEFAULT 0,
    -- When the job last run and next expected to run
    lastrun     TIMESTAMP WITHOUT TIME ZONE,
    runat       TIMESTAMP WITHOUT TIME ZONE,
    nextrun     TIMESTAMP WITHOUT TIME ZONE,
    attempt     INTEGER NOT NULL DEFAULT 0,
    -- The time of day this job is valid between
    valid       TIMERANGE,
    -- The retry count
    retry       INTERVAL,
    maxretry    INTEGER NOT NULL DEFAULT (2^31)-1,
    -- The job timeout
    timeout     INTERVAL NOT NULL DEFAULT '24 hours',
    error       TEXT,
    PRIMARY KEY(id)
);
CREATE INDEX schedule_i ON schedule(id);
CREATE INDEX schedule_ij ON schedule(id,jobid);
CREATE INDEX schedule_j ON schedule(jobid);
CREATE INDEX schedule_js ON schedule(jobid,state);
CREATE INDEX schedule_jn ON schedule(jobid,nextrun);
ALTER TABLE schedule OWNER TO job;

-- Defines a job to run once in the future
CREATE TABLE runonce (
    PRIMARY KEY (id)
) INHERITS (schedule);

CREATE INDEX runonce_i ON runonce(id);
CREATE INDEX runonce_ij ON runonce(id,jobid);
CREATE INDEX runonce_j ON runonce(jobid);
CREATE INDEX runonce_nextrun ON runonce(state,nextrun);
ALTER TABLE runonce OWNER TO job;

-- Defines a repeating job
CREATE TABLE runevery (
    step    INTERVAL NOT NULL,
    PRIMARY KEY (id)
) INHERITS (schedule);
CREATE INDEX runevery_i ON runevery(id);
CREATE INDEX runevery_ij ON runevery(id,jobid);
CREATE INDEX runevery_j ON runevery(jobid);
CREATE INDEX runevery_js ON runevery(jobid,state);
CREATE INDEX runevery_jn ON runevery(jobid,nextrun);
CREATE INDEX runevery_jsn ON runevery(jobid,state,nextrun);
ALTER TABLE runevery OWNER TO job;

-- ====================================================================================================
-- Return the internal JobNode id
CREATE OR REPLACE FUNCTION getJobNode( pname NAME )
RETURNS INTEGER AS $$
DECLARE
    rec     RECORD;
    aname   NAME;
BEGIN
    IF pname IS NULL OR pname = '' THEN
        aname = 'TEST';
    ELSE
        aname = UPPER(pname);
    END IF;
    LOOP
        SELECT INTO REC * FROM jobnode WHERE name=aname;
        IF FOUND THEN
            RETURN rec.id;
        END IF;
        BEGIN
            INSERT INTO jobnode (name) VALUES (aname);
            RETURN currval('jobnode_id_seq');
        EXCEPTION WHEN unique_violation THEN
            -- Do nothing, loop & try again
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Return the internal JobName id
CREATE OR REPLACE FUNCTION getJobName( pnode NAME, pname NAME )
RETURNS INTEGER AS $$
DECLARE
    rec     RECORD;
    anodeid INTEGER;
    aname   NAME;
BEGIN
    IF pname IS NULL OR pname = '' THEN
        aname = 'TEST';
    ELSE
        aname = UPPER(pname);
    END IF;
    anodeid = getJobNode(pnode);
    LOOP
        SELECT INTO rec * FROM jobname WHERE nodeid=anodeid AND name=aname;
        IF FOUND THEN
            RETURN rec.id;
        END IF;
        BEGIN
            INSERT INTO jobname (nodeid,name) VALUES (anodeid,aname);
            RETURN currval('jobname_id_seq');
        EXCEPTION WHEN unique_violation THEN
            -- Do nothing, loop & try again
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Return the job script by name
CREATE OR REPLACE FUNCTION getJob( pnode NAME, pname NAME )
RETURNS TABLE (text TEXT) AS $$
    SELECT j.text
        FROM jobdef j
            INNER JOIN jobname d ON j.id=d.id
            INNER JOIN jobnode n ON d.nodeid=n.id
        WHERE n.name=UPPER(pnode) AND d.name=UPPER(pname);
$$ LANGUAGE sql STABLE;

-- ====================================================================================================

CREATE OR REPLACE FUNCTION xtInt(pxml XML,pattr NAME,pdef INTEGER)
RETURNS INTEGER AS $$
DECLARE
    tm INTEGER;
BEGIN
    tm=(xpath(pattr,pxml))[1]::TEXT::INTEGER;
    IF tm IS NULL THEN
        RETURN pdef;
    ELSE
        RETURN tm;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
CREATE OR REPLACE FUNCTION scheduleJob( pnameid INTEGER, pxml XML )
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    sid     BIGINT;
    anameid INTEGER;
    axml    XML;
    ats     TIMESTAMP WITHOUT TIME ZONE;
    anode   NAME;
    aname   NAME;
    timeout INTERVAL;
BEGIN
    -- Wipe down all existing schedules & dependencies
    DELETE FROM schedule WHERE jobid=pnameid;
    DELETE FROM jobdependency where id=pnameid;

    -- dependencies. Only create a dependency if the name exists
    FOREACH axml IN ARRAY xpath('//depends',pxml)
    LOOP
        anode = (xpath('@node',axml))[1]::TEXT::NAME;
        aname = (xpath('@name',axml))[1]::TEXT::NAME;

        -- node is optional, if missing then use the same node as this job
        IF anode IS NULL OR anode = '' THEN
            SELECT INTO rec j.name FROM jobnode j INNER JOIN jobname n ON j.id=n.nodeid WHERE n.name=UPPER(aname);
            IF FOUND THEN
                anode=rec.name;
            END IF;
        END IF;

        -- If we have anode then look up the dependency and link as needed
        IF anode IS NOT NULL AND anode != '' THEN
            SELECT INTO rec n.*
                FROM jobname n
                INNER JOIN jobnode j ON n.nodeid=j.id
                WHERE j.name = UPPER(anode) AND n.name = UPPER(aname);
            IF FOUND THEN
                INSERT INTO jobdependency (id,depends) VALUES (pnameid,rec.id);
            END IF;
        END IF;
    END LOOP;

    -- Run immediately
    FOREACH axml IN ARRAY xpath('//immediately',pxml)
    LOOP
        timeout = (xpath('@timeout',axml))[1]::TEXT::INTERVAL;
        IF timeout IS NULL THEN
            timeout = '1 day';
        END IF;

        INSERT INTO runonce (
            id,jobid,type,
            valid, timeout,
            retry,maxretry,
            runat, nextrun
        ) VALUES (
            nextval('scheduleid'), pnameid, 1,
            NULL, timeout,
            NULL, 1,
            now(), now()
        );
    END LOOP;

    -- run once at a specific time <once when="timestamp"/>
    FOREACH axml IN ARRAY xpath('//once',pxml)
    LOOP
        ats = (xpath('@at',axml))[1]::TEXT::TIMESTAMP WITHOUT TIME ZONE;
        -- Don't schedule in the past otherwise we may run stuff we don't want to
        IF ats>=now()
        THEN
            timeout = (xpath('@timeout',axml))[1]::TEXT::INTERVAL;
            IF timeout IS NULL THEN
                timeout = '1 day';
            END IF;

            INSERT INTO runonce (
                id,jobid,type,
                valid, timeout,
                retry,maxretry,
                runat, nextrun
            ) VALUES (
                nextval('scheduleid'), pnameid, 1,
                (xpath('@between',axml))[1]::TEXT::TIMERANGE,
                timeout,
                (xpath('@retry',axml))[1]::TEXT::INTERVAL,
                xtInt(axml,'@max',2147483647),
                ats, ats
            );
        END IF;
    END LOOP;

    -- repeating schedules <repeat next="timestamp" step="interval"/>
    FOREACH axml IN ARRAY xpath('//repeat',pxml)
    LOOP
        ats = (xpath('@next',axml))[1]::TEXT::TIMESTAMP WITHOUT TIME ZONE;

        timeout = (xpath('@timeout',axml))[1]::TEXT::INTERVAL;
        IF timeout IS NULL THEN
            timeout = '1 day';
        END IF;

        INSERT INTO runevery (
            id,jobid,type,
            valid, timeout,
            retry,maxretry,
            runat,nextrun,
            step
        ) VALUES (
            nextval('scheduleid'), pnameid, 2,
            (xpath('@between',axml))[1]::TEXT::TIMERANGE,
            timeout,
            (xpath('@retry',axml))[1]::TEXT::INTERVAL,
            xtInt(axml,'@max',2147483647),
            ats, ats,
            (xpath('@step',axml))[1]::TEXT::INTERVAL
        );
    END LOOP;

END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Persist a job script
CREATE OR REPLACE FUNCTION storeJob( pnode NAME, pname NAME, pjob TEXT, pxml XML )
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    anameid INTEGER;
BEGIN
    anameid = getJobName(pnode,pname);
    LOOP
        UPDATE jobdef SET text=pjob WHERE id=anameid;
        IF FOUND THEN
            PERFORM scheduleJob(anameid,pxml);
            RETURN ;
        END IF;
        BEGIN
            INSERT INTO jobdef (id,text) VALUES (anameid,pjob);
            PERFORM scheduleJob(anameid,pxml);
            RETURN;
        EXCEPTION WHEN unique_violation THEN
            -- Do nothing, loop & try again
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Delete a job script
CREATE OR REPLACE FUNCTION deleteJob( pnode NAME, pname NAME, pjob TEXT )
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
BEGIN
    SELECT INTO rec * FROM jobname d
            INNER JOIN jobnode n ON d.nodeid=n.id
        WHERE n.name=UPPER(pnode) AND d.name=UPPER(pname);
    IF FOUND THEN
        DELETE FROM schedule WHERE jobid=rec.id;
        DELETE FROM jobdependency WHERE id=rec.id;
        DELETE FROM jobdef WHERE id=rec.id;
        DELETE FROM jobname WHERE id=rec.id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Return the next set of available jobs
--
-- This returns a simple table of jid, node and name to run. Internally the job's will be set to PENDING
-- Unless they have hit their max retries.
--
CREATE OR REPLACE FUNCTION nextJobs()
RETURNS TABLE (jid BIGINT, node NAME, name NAME, timeout INTERVAL ) AS $$
DECLARE
    rec         RECORD;
    nt          TIMESTAMP WITHOUT TIME ZONE = now();
    t           TIME = now()::TIME;
    runnable    BOOLEAN;
    drec        RECORD;
    srec        RECORD;
BEGIN
    -- Housekeeping - find any running jobs that have timed out and fail them
    FOR rec IN SELECT s.id FROM schedule s WHERE s.state=2 AND s.lastrun+s.timeout < now()
    LOOP
        PERFORM jobfail(rec.id);
    END LOOP;

    -- More housekeeping, any run once jobs in COMPLETED or FAIL states older than 24 hours remove them
    FOR rec IN SELECT id, jobid
        FROM schedule
        WHERE state IN (3,4)
          AND nextrun IS NULL
          AND (lastrun IS NULL OR lastrun < now()-'1 day'::INTERVAL)
    LOOP
        -- Remove the run once job schedule
        DELETE FROM schedule WHERE id=rec.id;
        -- Remove the job if there's no more schedules of ANY type for it
        SELECT INTO drec * FROM schedule WHERE jobid=rec.jobid LIMIT 1;
        IF NOT FOUND THEN
            DELETE FROM jobdef WHERE id=rec.jobid;
            DELETE FROM jobname WHERE id=rec.jobid;
        END IF;
    END LOOP;

    -- Now get the next set of jobs to be run, effectively all states other than running
    FOR rec IN SELECT s.id AS jid, n.name AS name, j.name AS node, s.timeout, s.attempt, s.maxretry, s.jobid
         FROM schedule s
            INNER JOIN jobname n ON s.jobid=n.id
            INNER JOIN jobnode j ON n.nodeid=j.id
        WHERE nextrun IS NOT NULL AND nextrun < nt
          AND state IN (0,1,3,4)
          AND t === s.valid
          AND attempt < maxretry
    LOOP
        IF rec.attempt < rec.maxretry THEN
            -- Look at last run for the dependencies
            runnable = true;
            FOR drec IN SELECT * FROM jobdependency WHERE id=rec.jobid
            LOOP
                SELECT INTO srec *
                    FROM schedule
                    WHERE jobid = drec.depends
                    ORDER BY lastrun DESC
                    LIMIT 1;
                IF FOUND THEN
                    runnable = srec.state = 3;
                END IF;
            END LOOP;
            
            IF runnable THEN
                -- Mark as running and return this entry
                UPDATE schedule
                    SET state=2, attempt=attempt+1, lastrun=now()
                    WHERE id=rec.jid;
                RETURN QUERY SELECT rec.jid, rec.node, rec.name, rec.timeout ;
            ELSE
                -- Mark as pending as it's waiting on another job
                UPDATE schedule SET state=1 WHERE id=rec.jid;
            END IF;
        ELSE
            -- Max attempts hit so fail the job
            PERFORM jobfail(rec.jid);
        END IF;
    END LOOP;
    RETURN;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Reschedule a job to it's next schedule time
CREATE OR REPLACE FUNCTION jobnextrun(pid BIGINT,ps INTEGER)
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    nt      TIMESTAMP WITHOUT TIME ZONE;
    nt0     TIMESTAMP WITHOUT TIME ZONE = date_trunc('minute',now());
BEGIN
    SELECT INTO rec * FROM schedule WHERE id=pid;
    IF FOUND THEN
        IF rec.type = 2 THEN
            -- Move schedule to next run time
            SELECT INTO rec * FROM runevery WHERE id=pid;
            nt = rec.runat;
            -- in case nt is that far behind ensure it's in the future
            WHILE nt <= nt0 LOOP
                nt = nt + rec.step;
            END LOOP;
            UPDATE schedule SET runat=nt, nextrun=nt, attempt=0, state=ps WHERE id=pid;
        ELSE
            -- Just mark the unknown type as completed.
            UPDATE schedule SET attempt=0, state=ps WHERE id=pid;
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Mark a job as successful
CREATE OR REPLACE FUNCTION jobsuccess(pid BIGINT)
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    nt      TIMESTAMP WITHOUT TIME ZONE;
BEGIN
    SELECT INTO rec * FROM schedule WHERE id=pid;
    IF FOUND THEN
        IF rec.type = 1 THEN
            -- Just mark as completed
            UPDATE schedule SET nextrun=NULL, state=3, error=NULL WHERE id=pid;
        ELSE
            UPDATE schedule SET error=NULL WHERE id=pid;
            PERFORM jobnextrun(pid,3);
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Mark a job a failed.
-- This will if defined apply a retry delay to the job
CREATE OR REPLACE FUNCTION jobfail(pid BIGINT)
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    nt      TIMESTAMP WITHOUT TIME ZONE;
BEGIN
    PERFORM jobfail(pid,'Failed');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION jobfail(pid BIGINT,perr TEXT)
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    nt      TIMESTAMP WITHOUT TIME ZONE;
BEGIN
    SELECT INTO rec * FROM schedule WHERE id=pid;
    IF FOUND THEN
        IF rec.retry IS NOT NULL AND rec.attempt<rec.maxretry THEN 
            -- retry at the next interval
            nt = rec.runat + rec.retry;
            -- in case nt is that far behind ensure it's in the future
            WHILE nt < now() LOOP
                nt = nt + rec.retry;
            END LOOP;
            UPDATE schedule SET nextrun=nt, state=4, error=perr WHERE id=pid;
        ELSE
            -- Try to reschedule at the next time
            UPDATE schedule SET error=perr WHERE id=pid;
            PERFORM jobnextrun(pid,4);
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Schedules a job to run now, regardless of any existing schedules. Normal dependency rules will apply.
CREATE OR REPLACE FUNCTION runnow(pnode NAME, pname NAME)
RETURNS BIGINT AS $$
DECLARE
    ajobid  BIGINT;
BEGIN
    SELECT INTO ajobid n.id
        FROM jobname n
        INNER JOIN jobnode j ON n.nodeid = j.id
        WHERE j.name=UPPER(pnode) and n.name=UPPER(pname);
    IF FOUND THEN
        INSERT INTO runonce (
            id,jobid,type,
            valid,
            retry,maxretry,
            runat, nextrun
        ) VALUES (
            nextval('scheduleid'), ajobid, 1,
            NULL, NULL, 1,
            now(), now()
        );
        RETURN ajobid;
    ELSE
        RETURN 0;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Schedules a job and any dependents to run, regardless of schedules.
-- This is the same as running runnow on a job and each dependent
CREATE OR REPLACE FUNCTION runjob(pnode NAME, pname NAME)
RETURNS VOID AS $$
DECLARE
    ajobid  BIGINT;
    rec     RECORD;
BEGIN
    ajobid = runnow(pnode,pname);
    IF ajobid > 0 THEN
        FOR rec IN SELECT j.name AS node, n.name AS name
            FROM jobname n
            INNER JOIN jobnode j ON n.nodeid=j.id
            INNER JOIN jobdependency d ON n.id=d.id
            WHERE d.depends = ajobid
            GROUP BY j.name, n.name
        LOOP
            PERFORM runnow(rec.node,rec.name);
        END LOOP;
    END IF;
END;
$$ LANGUAGE plpgsql;
