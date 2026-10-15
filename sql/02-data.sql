-- Datos de prueba para el catalogo de StagePass Live.

INSERT INTO recintos (id, nombre, aforo) VALUES
    ('recinto-teatro-mayor', 'Teatro Mayor', 500),
    ('recinto-arena-norte', 'Arena Norte', 2000);

INSERT INTO eventos (id, nombre, tipo, recinto_id, fecha_hora, estado) VALUES
    ('evt-rock-en-vivo', 'Rock en Vivo - Gira 2026', 'CONCIERTO', 'recinto-arena-norte', '2026-12-05 20:00:00', 'PROGRAMADO'),
    ('evt-hamlet', 'Hamlet, el Principe', 'TEATRO', 'recinto-teatro-mayor', '2026-11-20 19:30:00', 'PROGRAMADO'),
    ('evt-festival-verano', 'Festival de Verano', 'FESTIVAL', 'recinto-arena-norte', '2027-01-15 16:00:00', 'PROGRAMADO'),
    ('evt-opera-cancelada', 'Opera de Invierno (cancelada)', 'TEATRO', 'recinto-teatro-mayor', '2026-10-01 19:00:00', 'CANCELADO');

-- Asientos del Teatro Mayor (usados por evt-hamlet y evt-opera-cancelada, ya que ambos comparten recinto).
INSERT INTO asientos (id, recinto_id, codigo, precio_base) VALUES
    ('asi-tm-a1', 'recinto-teatro-mayor', 'A1', 80000.00),
    ('asi-tm-a2', 'recinto-teatro-mayor', 'A2', 80000.00),
    ('asi-tm-a3', 'recinto-teatro-mayor', 'A3', 80000.00),
    ('asi-tm-a4', 'recinto-teatro-mayor', 'A4', 80000.00),
    ('asi-tm-a5', 'recinto-teatro-mayor', 'A5', 80000.00),
    ('asi-tm-b1', 'recinto-teatro-mayor', 'B1', 60000.00),
    ('asi-tm-b2', 'recinto-teatro-mayor', 'B2', 60000.00),
    ('asi-tm-b3', 'recinto-teatro-mayor', 'B3', 60000.00),
    ('asi-tm-b4', 'recinto-teatro-mayor', 'B4', 60000.00),
    ('asi-tm-b5', 'recinto-teatro-mayor', 'B5', 60000.00);

-- Asientos de Arena Norte (usados por evt-rock-en-vivo y evt-festival-verano).
INSERT INTO asientos (id, recinto_id, codigo, precio_base) VALUES
    ('asi-an-vip1', 'recinto-arena-norte', 'VIP-1', 250000.00),
    ('asi-an-vip2', 'recinto-arena-norte', 'VIP-2', 250000.00),
    ('asi-an-vip3', 'recinto-arena-norte', 'VIP-3', 250000.00),
    ('asi-an-gen1', 'recinto-arena-norte', 'GEN-1', 120000.00),
    ('asi-an-gen2', 'recinto-arena-norte', 'GEN-2', 120000.00),
    ('asi-an-gen3', 'recinto-arena-norte', 'GEN-3', 120000.00),
    ('asi-an-gen4', 'recinto-arena-norte', 'GEN-4', 120000.00),
    ('asi-an-gen5', 'recinto-arena-norte', 'GEN-5', 120000.00),
    ('asi-an-gen6', 'recinto-arena-norte', 'GEN-6', 120000.00),
    ('asi-an-gen7', 'recinto-arena-norte', 'GEN-7', 120000.00),
    ('asi-an-gen8', 'recinto-arena-norte', 'GEN-8', 120000.00),
    ('asi-an-gen9', 'recinto-arena-norte', 'GEN-9', 120000.00);
