-- Table that holds job definitions

DROP TABLE jobdef;
DROP TABLE job;

CREATE TABLE job (
    id          SERIAL NOT NULL,
    clusterid   BIGINT NOT NULL REFERENCES cluster(id),
    name        NAME NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX job_cn ON job(clusterid,name);
CREATE INDEX job_n ON job(name);
GRANT ALL ON job TO job;

-- The actual job text with versioning
CREATE TABLE jobdef (
    id      BIGINT NOT NULL REFERENCES job(id),
    -- version and timestamp
    version INTEGER NOT NULL DEFAULT 1,
    dt      TIMESTAMP WITHOUT TIME ZONE,
    -- job output section
    outpt     TEXT NOT NULL DEFAULT '',
    -- job declaration section
    decl    TEXT NOT NULL DEFAULT '',
    -- job body
    text    TEXT NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX jobdef_iv ON jobdef(id,version);
CREATE INDEX jobdef_i ON jobdef(id);
CREATE INDEX jobdef_v ON jobdef(id,version);
GRANT ALL ON jobdef TO job;

-- Simple hello world job
INSERT INTO job (clusterid, name, enabled) VALUES (
    (SELECT id FROM cluster WHERE name='test'),
    'HelloWorld',
    true
);

INSERT INTO jobdef (id,version,dt,outpt,decl,text) VALUES (
    (SELECT id FROM job WHERE name='HelloWorld' AND clusterid = (SELECT id FROM cluster WHERE name='test')),
    1, now(),
    '',
    'response="";',
    'log "Hello World";
response="Hello World";
'
);
