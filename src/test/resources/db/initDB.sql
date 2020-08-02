DROP TABLE IF EXISTS springboottable;
DROP SEQUENCE IF EXISTS jpa_sequence;

CREATE SEQUENCE jpa_sequence START WITH 1;

CREATE TABLE springboottable
(
    id              INTEGER             PRIMARY KEY         DEFAULT         nextval('jpa_sequence'),
    name            VARCHAR             NOT NULL,
    age             INTEGER             NOT NULL,
    weight          INTEGER             NOT NULL,
    height          DOUBLE PRECISION    NOT NULL,
    date_time       TIMESTAMP           NOT NULL
);