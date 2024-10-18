DROP TABLE IF EXISTS url_checks;

CREATE TABLE url_checks
(
    id          bigint GENERATED ALWAYS AS IDENTITY,
    url_id      bigint    NOT NULL,
    status_code integer   NOT NULL,
    created_at  timestamp NOT NULL,
    title       varchar(255),
    h1          varchar(255),
    description text,
    FOREIGN KEY (url_id) REFERENCES urls (id)
);
