// Script de inicializacion de MongoDB para el entorno local de StagePass Live.
// Se ejecuta automaticamente la primera vez que se crea el volumen del contenedor.
// Crea un usuario de aplicacion con permisos sobre la base "stagepass", separado del usuario root.

const appDbName = process.env.MONGO_INITDB_DATABASE || "stagepass";
const appUser = "stagepass_app";
const appPassword = "stagepass_app_pwd";

db = db.getSiblingDB(appDbName);

db.createUser({
  user: appUser,
  pwd: appPassword,
  roles: [{ role: "readWrite", db: appDbName }],
});

db.createCollection("reservas");
db.createCollection("disponibilidad_asientos");
db.createCollection("boletos");

// Indice unico: un asiento solo puede tener un documento de ocupacion por evento.
// Esta es la garantia tecnica que impide vender dos veces el mismo asiento de una funcion.
db.disponibilidad_asientos.createIndex(
  { eventoId: 1, asientoId: 1 },
  { unique: true, name: "uq_evento_asiento" }
);

// Indice unico: un boleto por asiento y reserva (evita emisiones duplicadas del mismo asiento).
db.boletos.createIndex(
  { reservaId: 1, asientoId: 1 },
  { unique: true, name: "uq_reserva_asiento_boleto" }
);

db.boletos.createIndex(
  { codigoRedencion: 1 },
  { unique: true, name: "uq_codigo_redencion" }
);

db.reservas.createIndex({ clienteId: 1, eventoId: 1 }, { name: "ix_cliente_evento" });
db.reservas.createIndex({ estado: 1, expiraEn: 1 }, { name: "ix_estado_expira" });

print("Inicializacion de MongoDB para StagePass Live completada.");
