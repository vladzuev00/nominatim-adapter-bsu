CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TYPE city_type AS ENUM ('CAPITAL', 'REGIONAL', 'NOT_DEFINED');

CREATE TABLE city
(
    id           SERIAL PRIMARY KEY,
    type         city_type    NOT NULL,
    name         VARCHAR(256) NOT NULL,
    geometry     GEOMETRY     NOT NULL,
    bounding_box GEOMETRY     NOT NULL
);

ALTER SEQUENCE city_id_seq INCREMENT 50;

CREATE TYPE searching_cities_process_type AS ENUM('HANDLING', 'SUCCESS', 'ERROR');

CREATE TABLE searching_cities_process
(
    id             SERIAL PRIMARY KEY,
    status         searching_cities_process_type NOT NULL,
    search_step    DECIMAL                       NOT NULL,
    total_points   BIGINT                        NOT NULL,
    handled_points BIGINT                        NOT NULL,
    bounds         GEOMETRY                      NOT NULL,
    updated_time   TIMESTAMP                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_time   TIMESTAMP                     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE
OR REPLACE FUNCTION on_update_searching_cities_process() RETURNS TRIGGER AS
'
    BEGIN
        NEW.updated_time = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER tr_on_update_searching_cities_process
    BEFORE UPDATE
    ON searching_cities_process
    FOR EACH ROW
    EXECUTE PROCEDURE on_update_searching_cities_process();

CREATE INDEX ON city using GIST(geometry);
CREATE INDEX ON city using GIST(bounding_box);

