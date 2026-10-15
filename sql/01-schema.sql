-- Esquema MySQL para el catalogo de StagePass Live (recintos, eventos, asientos).
-- Este script NO contiene reglas de negocio: solo estructura de datos.

CREATE TABLE IF NOT EXISTS recintos (
    id      VARCHAR(40)  NOT NULL PRIMARY KEY,
    nombre  VARCHAR(150) NOT NULL,
    aforo   INT          NOT NULL,
    CONSTRAINT chk_recintos_aforo CHECK (aforo > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS eventos (
    id          VARCHAR(40)  NOT NULL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    tipo        VARCHAR(20)  NOT NULL,
    recinto_id  VARCHAR(40)  NOT NULL,
    fecha_hora  DATETIME     NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_eventos_recinto FOREIGN KEY (recinto_id) REFERENCES recintos (id),
    CONSTRAINT chk_eventos_tipo CHECK (tipo IN ('CONCIERTO', 'TEATRO', 'FESTIVAL')),
    CONSTRAINT chk_eventos_estado CHECK (estado IN ('PROGRAMADO', 'AGOTADO', 'CANCELADO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS asientos (
    id           VARCHAR(40)   NOT NULL PRIMARY KEY,
    recinto_id   VARCHAR(40)   NOT NULL,
    codigo       VARCHAR(20)   NOT NULL,
    precio_base  DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_asientos_recinto FOREIGN KEY (recinto_id) REFERENCES recintos (id),
    CONSTRAINT uq_asientos_recinto_codigo UNIQUE (recinto_id, codigo),
    CONSTRAINT chk_asientos_precio CHECK (precio_base >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_eventos_recinto ON eventos (recinto_id);
CREATE INDEX idx_asientos_recinto ON asientos (recinto_id);
