-- Reference table that holds available cluster's

CREATE TABLE cluster (
    id SERIAL NOT NULL,
    name NAME NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX cluster_n ON cluster(name);
GRANT ALL ON cluster TO job;

INSERT INTO cluster (name) VALUES ('test');
