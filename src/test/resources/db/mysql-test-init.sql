CREATE TABLE IF NOT EXISTS recintos (
    id      VARCHAR(40)  NOT NULL PRIMARY KEY,
    nombre  VARCHAR(150) NOT NULL,
    aforo   INT          NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS eventos (
    id          VARCHAR(40)  NOT NULL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    tipo        VARCHAR(20)  NOT NULL,
    recinto_id  VARCHAR(40)  NOT NULL,
    fecha_hora  DATETIME     NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_eventos_recinto FOREIGN KEY (recinto_id) REFERENCES recintos (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS asientos (
    id           VARCHAR(40)   NOT NULL PRIMARY KEY,
    recinto_id   VARCHAR(40)   NOT NULL,
    codigo       VARCHAR(20)   NOT NULL,
    precio_base  DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_asientos_recinto FOREIGN KEY (recinto_id) REFERENCES recintos (id),
    CONSTRAINT uq_asientos_recinto_codigo UNIQUE (recinto_id, codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO recintos (id, nombre, aforo) VALUES ('recinto-test', 'Recinto de Prueba', 100);

INSERT INTO eventos (id, nombre, tipo, recinto_id, fecha_hora, estado) VALUES
    ('evento-test', 'Evento de Prueba', 'CONCIERTO', 'recinto-test', '2026-12-01 20:00:00', 'PROGRAMADO');

INSERT INTO asientos (id, recinto_id, codigo, precio_base) VALUES
    ('asiento-test-1', 'recinto-test', 'A1', 50000.00),
    ('asiento-test-2', 'recinto-test', 'A2', 50000.00);
