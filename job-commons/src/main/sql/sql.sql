-- ============================================================
-- Provides control over what SQL functions are possible from
-- a script
-- ============================================================

CREATE SCHEMA config;
SET search_path = config;

--
-- 
DROP TABLE sqlextension;
CREATE TABLE sqlextension (
    id          SERIAL,
    -- Name of extension
    name        NAME,
    -- The datasource to use (for SQL permissions etc)
    datasource  NAME,
    -- The number of arguments
    argc        INTEGER,
    -- A result set expected
    resultset   BOOLEAN DEFAULT FALSE,
    -- Returns an object value?
    singlevalue BOOLEAN DEFAULT FALSE,
    -- JDBC snippet, eg 'myschema.myfunc(?,?)'
    sqlcall     TEXT,
    -- Description of what this extension performs
    description TEXT,
    PRIMARY KEY(id)
);
GRANT SELECT ON sqlextension TO PUBLIC;

CREATE UNIQUE INDEX sqlextension_n on sqlextension(name);

-- Cleanup Darwin
-- darwin_cleanup();
INSERT INTO sqlextension (name,datasource,argc,sqlcall,description)
    VALUES ('darwin_cleanup','rail',0,'darwin.darwin_cleanup()','Cleans up the live darwin database of any old trains');

-- Return the database versiom
-- var = postgresql_version();
INSERT INTO sqlextension (name,datasource,argc,singlevalue,sqlcall,description)
    VALUES ('postgresql_version','rail',0,true,'version()','Debug, returns the postgresql version');
