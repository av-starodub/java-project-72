DROP TABLE IF EXISTS url_checks;
DROP TABLE IF EXISTS urls;

CREATE TABLE urls
(
    id         bigint GENERATED ALWAYS AS IDENTITY,
    name       varchar(255) UNIQUE,
    created_at timestamp    NOT NULL,
    CONSTRAINT pk_url PRIMARY KEY (id)
);

CREATE TABLE url_checks
(
    id          bigint GENERATED ALWAYS AS IDENTITY,
    url_id      bigint    NOT NULL,
    status_code integer   NOT NULL,
    created_at  timestamp NOT NULL,
    title       varchar(255),
    h1          varchar(255),
    description text,
    CONSTRAINT pk_url_check PRIMARY KEY (id),
    CONSTRAINT fk_url_checks_url_id FOREIGN KEY (url_id) REFERENCES urls(id)
);
