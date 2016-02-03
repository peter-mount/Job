-- Reference table that holds available cluster's

DROP TABLE jobrepeat;
DROP TABLE jobdef;
DROP TABLE jobname;
DROP TABLE jobnode;

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

-- The job status
DROP TYPE jobstate;
CREATE TYPE jobstate AS ENUM (
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

-- Defines a job to run once in the future
CREATE TABLE jobonce (
    id      SERIAL NOT NULL,
    jobid   BIGINT NOT NULL REFERENCES jobname(id),
    run TIMESTAMP WITHOUT TIME ZONE,
    PRIMARY KEY (id)
);
CREATE INDEX jobonce_ij ON jobrepeat(id,jobid);
CREATE INDEX jobonce_j ON jobrepeat(jobid);
CREATE INDEX jobonce_jn ON jobrepeat(jobid,run);
ALTER TABLE jobonce OWNER TO job;
ALTER SEQUENCE jobonce_id_seq OWNER TO job;

-- Defines a repeating job
CREATE TABLE jobrepeat (
    id      SERIAL NOT NULL,
    jobid   BIGINT NOT NULL REFERENCES jobname(id),
    state   jobstate NOT NULL DEFAULT 'WAITING',
    lastrun TIMESTAMP WITHOUT TIME ZONE,
    nextrun TIMESTAMP WITHOUT TIME ZONE,
    step    INTERVAL NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX jobrepeat_ij ON jobrepeat(id,jobid);
CREATE INDEX jobrepeat_j ON jobrepeat(jobid);
CREATE INDEX jobrepeat_js ON jobrepeat(jobid,state);
CREATE INDEX jobrepeat_jn ON jobrepeat(jobid,nextrun);
CREATE INDEX jobrepeat_jsn ON jobrepeat(jobid,state,nextrun);
ALTER TABLE jobrepeat OWNER TO job;
ALTER SEQUENCE jobrepeat_id_seq OWNER TO job;

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

-- Return the job script by name
CREATE OR REPLACE FUNCTION getJob( pnode NAME, pname NAME )
RETURNS TABLE (text TEXT) AS $$
    SELECT j.text
        FROM jobdef j
            INNER JOIN jobname d ON j.id=d.id
            INNER JOIN jobnode n ON d.nodeid=n.id
        WHERE n.name=UPPER(pnode) AND d.name=UPPER(pname);
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION scheduleJob( pnameid INTEGER, pxml XML )
RETURNS VOID AS $$
DECLARE
    rec     RECORD;
    anameid INTEGER;
    axml    XML;
    ats     TIMESTAMP WITHOUT TIME ZONE;
BEGIN

    -- run once at a specific time <once when="timestamp"/>
    DELETE FROM jobonce WHERE jobid=pnameid;
    FOREACH axml IN ARRAY xpath('//once',pxml)
    LOOP
        ats = (xpath('@when',axml))[1]::TEXT::TIMESTAMP WITHOUT TIME ZONE;
        -- Don't schedule in the past otherwise we may run stuff we don't want to
        IF ats>=now()
        THEN
            INSERT INTO jobonce (jobid,run) VALUES ( pnameid, ats );
        END IF;
    END LOOP;

    -- repeating schedules <repeat next="timestamp" step="interval"/>
    DELETE FROM jobrepeat WHERE jobid=pnameid;
    FOREACH axml IN ARRAY xpath('//repeat',pxml)
    LOOP
        INSERT INTO jobrepeat (jobid,state,lastrun,nextrun,step)
            VALUES (pnameid, 'WAITING',null,
                (xpath('@next',axml))[1]::TEXT::TIMESTAMP WITHOUT TIME ZONE,
                (xpath('@step',axml))[1]::TEXT::INTERVAL
            );
    END LOOP;

END;
$$ LANGUAGE plpgsql;

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
        DELETE FROM jobrepeat WHERE jobid=rec.id;
        -- Remove the job definition
        DELETE FROM jobdef WHERE id=rec.id;
        DELETE FROM jobname WHERE id=rec.id;
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT storeJob('test','test','a test','<schedule>
<once when="2016-02-09 18:00"/>
<once when="2016-02-01 18:00"/>
<repeat next="2016-02-03 18:00" step="1 hour"/>
</schedule>'::xml);

select * from jobname;
select * from jobonce;
select * from jobrepeat;
