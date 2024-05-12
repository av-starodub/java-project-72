DROP TABLE IF EXISTS urls;

CREATE TABLE urls
(
    id         bigint GENERATED ALWAYS AS IDENTITY,
    name       varchar(255) NOT NULL,
    created_at timestamp    NOT NULL,
    CONSTRAINT pk_url PRIMARY KEY (id)
);
