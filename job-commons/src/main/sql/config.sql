-- ======================================================================
-- Configuration Schema
-- ======================================================================

CREATE SCHEMA config;
SET search_path = config;

-- The primary config table. All entries are public
CREATE TABLE config (
    -- Configuration name
    name    NAME NOT NULL,
    -- Configuration key, unique within name
    key     NAME NOT NULL,
    -- Configuration value
    value   TEXT NOT NULL,
    PRIMARY KEY (name,key)
);

CREATE INDEX config_n ON config(name);
CREATE INDEX config_k ON config(key);

-- Private config. Similar to the public one but restricted so only Java code can access this table
CREATE TABLE prconfig (
    -- Configuration name
    name    NAME NOT NULL,
    -- Configuration key, unique within name
    key     NAME NOT NULL,
    -- Configuration value
    value   TEXT NOT NULL,
    PRIMARY KEY (name,key)
);

CREATE INDEX prconfig_n ON prconfig(name);
CREATE INDEX prconfig_k ON prconfig(key);
