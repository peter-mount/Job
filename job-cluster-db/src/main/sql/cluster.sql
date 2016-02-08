-- Reference table that holds available cluster's

DROP TABLE runonce;
DROP TABLE runevery;

DROP FUNCTION nextjobs();
DROP TABLE schedule;
DROP SEQUENCE scheduleid;

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
-- The job type
CREATE TABLE jobtype (
    id      INTEGER NOT NULL,
    name    NAME,
    PRIMARY KEY(id)
);
CREATE INDEX jobtype_n ON (jobstate);
ALTER TABLE jobtype OWNER TO job;
INSERT INTO jobtype VALUES (1,"runonce");
INSERT INTO jobtype VALUES (2,"runevery");
INSERT INTO jobtype VALUES (3,"runcron");

-- ====================================================================================================
-- The job status
CREATE TABLE jobstate (
    id      INTEGER NOT NULL,
    name    NAME,
    PRIMARY KEY(id)
);
CREATE INDEX jobstate_n ON (jobstate);
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
    nextrun     TIMESTAMP WITHOUT TIME ZONE,
    attempt     INTEGER NOT NULL DEFAULT 0,
    -- The time of day this job is valid between
    valid       TIMERANGE,
    -- The retry count
    retry       INTERVAL,
    maxretry    INTEGER NOT NULL DEFAULT (2^31)-1,
    -- The job timeout
    timeout     INTERVAL NOT NULL DEFAULT '24 hours',
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
    run TIMESTAMP WITHOUT TIME ZONE,
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
BEGIN
    -- Wipe down all existing schedules
    DELETE FROM schedule WHERE jobid=pnameid;

    -- run once at a specific time <once when="timestamp"/>
    FOREACH axml IN ARRAY xpath('//once',pxml)
    LOOP
        ats = (xpath('@when',axml))[1]::TEXT::TIMESTAMP WITHOUT TIME ZONE;
        -- Don't schedule in the past otherwise we may run stuff we don't want to
        IF ats>=now()
        THEN
            INSERT INTO runonce (
                id,jobid,type,
                retry,maxretry,
                run
            ) VALUES (
                nextval('scheduleid'), pnameid, 1,
                (xpath('@retry',axml))[1]::TEXT::INTERVAL,
                xtInt(axml,'@max',2147483647),
                ats
            );
        END IF;
    END LOOP;

    -- repeating schedules <repeat next="timestamp" step="interval"/>
    FOREACH axml IN ARRAY xpath('//repeat',pxml)
    LOOP
        INSERT INTO runevery (
            id,jobid,type,
            valid,
            retry,maxretry,
            nextrun,step
        ) VALUES (
            nextval('scheduleid'), pnameid, 2,
            timerange( axml,'@betweenStart','@betweenEnd' ),
            (xpath('@retry',axml))[1]::TEXT::INTERVAL,
            xtInt(axml,'@max',2147483647),
            (xpath('@next',axml))[1]::TEXT::TIMESTAMP WITHOUT TIME ZONE,
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
        -- Remove the 
        DELETE FROM schedule WHERE jobid=rec.id;
        -- Remove the job definition
        DELETE FROM jobdef WHERE id=rec.id;
        DELETE FROM jobname WHERE id=rec.id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
-- Return the next set of available jobs
CREATE OR REPLACE FUNCTION nextJobs()
RETURNS SETOF schedule AS $$
DECLARE
    rec schedule%rowtype;
    n   TIMESTAMP WITHOUT TIME ZONE = now();
    t   TIME = now()::TIME;
BEGIN
    FOR rec IN SELECT *
         FROM schedule
        WHERE nextrun < n
          AND state IN (0,3,4)
          AND t === schedule.valid
    LOOP
        UPDATE schedule SET state=1 WHERE id=rec.id;
        RETURN NEXT rec;
    END LOOP;
    RETURN;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================================
SELECT storeJob('test','test','a test','<schedule>
<once at="2016-02-10 18:24"/>
<once at="2016-02-04 18:34" retry="1 hour" max="3"/>
<once at="2016-02-10 18:44" retry="1 hour"/>
<repeat next="2016-02-04 07:33" step="1 minute"/>
<repeat next="2016-02-04 07:33" step="10 minute"/>
<repeat next="2016-02-04 07:33" step="1 day" retry="1 hour"/>
<repeat next="2016-02-04 07:33" step="1 day" retry="1 hour" max="3"/>
<repeat betweenStart="21:00" betweenEnd="06:00" next="2016-02-04 07:33" step="1 hour"/>
<cron m="0" h="3"/>
<cron m="0" h="3" retry="1 hour" max="4"/>
</schedule>'::xml);

select * from jobname;
select * from schedule;
select * from runonce;
select * from runevery;
