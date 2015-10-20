-- ============================================================
-- Provides control over what SQL functions are possible from
-- a script
-- ============================================================

CREATE SCHEMA config;
SET search_path = config;

DROP TABLE sqlextension;
DROP TYPE SQLFUNCTYPE;

-- Enum that defines the type of function
CREATE TYPE SQLFUNCTYPE AS ENUM (
    -- Exists as a statement in the language
    'STATEMENT',
    -- Function returns a single result
    'SINGLE',
    -- Function returns a list of single results
    'LIST',
    -- Function returns a table
    'TABLE'
);

-- 
CREATE TABLE sqlextension (
    id          SERIAL NOT NULL,
    -- Name of extension
    name        NAME NOT NULL,
    -- The number of arguments
    argc        INTEGER NOT NULL,
    -- Type of function
    type        SQLFUNCTYPE NOT NULL,
    -- The datasource to use (for SQL permissions etc)
    datasource  NAME NOT NULL,
    -- JDBC snippet, eg 'myschema.myfunc(?,?)'
    sqlcall     TEXT NOT NULL,
    -- Description of what this extension performs
    description TEXT NOT NULL DEFAULT '',
    PRIMARY KEY(id)
);
GRANT SELECT ON sqlextension TO PUBLIC;

-- Index is unique by name and result type
CREATE UNIQUE INDEX sqlextension_n on sqlextension(name,argc,type);

-- Cleanup Darwin
-- darwin_cleanup();
INSERT INTO sqlextension (name,type,datasource,argc,sqlcall,description)
    VALUES ('darwin_cleanup', 'STATEMENT', 'rail',0,'darwin.darwin_cleanup()','Cleans up the live darwin database of any old trains');

-- Return the database versiom
-- var = postgresql_version();
INSERT INTO sqlextension (name,type,datasource,argc,sqlcall,description)
    VALUES ('postgresql_version', 'SINGLE', 'rail',0,'version()','Debug, returns the postgresql version');

INSERT INTO sqlextension (name,type,datasource,argc,sqlcall,description)
    VALUES ('departureboard', 'TABLE', 'rail', 1,'darwin.departureboard(?)','The current departureboard for a CRS. CRS MUST exist');
