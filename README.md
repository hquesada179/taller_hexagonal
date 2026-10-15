# StagePass Live — Event & Concert Platform

Segundo entregable del taller universitario de Arquitectura de Software: implementación completa de **StagePass Live**, una plataforma de venta de boletos para eventos y conciertos, aplicando el patrón de **Arquitectura Hexagonal (Ports & Adapters)**.

El primer entregable (diagrama de clases UML) ya existe en el repositorio:

- `hexagonal_taller.drawio` — diagrama editable (draw.io / diagrams.net).
- `hexagonal_taller.svg` — representación visual del mismo diagrama.

Este README documenta el segundo entregable: el código fuente funcional construido sobre ese diseño.

---

## 1. Descripción del proyecto

StagePass Live permite:

- Consultar el catálogo de eventos, recintos y asientos.
- Iniciar la compra de boletos, bloqueando los asientos seleccionados durante **10 minutos exactos**.
- Calcular tarifas administrativas y descuentos comerciales según la cantidad de boletos.
- Limitar a **6 boletos máximo** por cliente y evento (sumando boletos ya emitidos y reservas activas).
- Procesar la confirmación de pago (webhook de la pasarela) y emitir boletos digitales firmados con **HMAC‑SHA256** y codificados en **QR**.
- Liberar automáticamente los asientos de reservas vencidas sin pago confirmado.
- Enviar notificaciones (correo/WhatsApp) y emitir una factura electrónica simulada.

Todo el flujo es demostrable de principio a fin **sin usar pagos reales**, mediante pasarelas de pago simuladas.

---

## 2. Tecnologías utilizadas

| Categoría            | Tecnología                                   |
|-----------------------|-----------------------------------------------|
| Lenguaje              | Java 21                                       |
| Framework             | Spring Boot 3.3.13 (Spring Web, Validation, Scheduling) |
| Build                 | Maven (con Maven Wrapper `mvnw`/`mvnw.cmd`)   |
| Persistencia relacional | MySQL 8.4 + Spring Data JPA / Hibernate    |
| Persistencia documental | MongoDB 7 + Spring Data MongoDB (`MongoTemplate`) |
| Códigos QR            | ZXing (`com.google.zxing`)                    |
| Pruebas                | JUnit 5, Mockito, AssertJ, Testcontainers (MySQL y MongoDB reales) |
| Infraestructura local | Docker Compose (MySQL + MongoDB)              |
| Configuración          | Variables de entorno (`.env`, `application.yml`) |

---

## 3. Arquitectura hexagonal (Ports & Adapters)

La regla fundamental del taller se cumple de forma verificable:

> **El dominio y los casos de uso (`domain/`, `application/`) no importan Spring, JPA, MongoDB, HTTP ni ningún SDK externo.**

Esto se puede comprobar en cualquier momento con:

```powershell
Select-String -Path src\main\java\com\stagepass\live\domain\**\*.java,src\main\java\com\stagepass\live\application\**\*.java -Pattern "org.springframework|jakarta.persistence|org.bson|com.mongodb|jakarta.servlet"
```
(no debe devolver ningún resultado).

### Capas

```
┌─────────────────────────┐        ┌──────────────────────────┐
│   ADAPTADORES DE ENTRADA │        │   ADAPTADORES DE SALIDA   │
│  REST Controllers,       │        │  MySQL, MongoDB, Pagos,   │
│  Webhook, Scheduler      │        │  Firma, QR, Notificación, │
└───────────┬──────────────┘        │  Factura, Reloj           │
            │ usa                   └─────────────┬─────────────┘
            ▼                                      ▲ implementa
┌─────────────────────────┐        ┌───────────────┴────────────┐
│   PUERTOS DE ENTRADA      │       │    PUERTOS DE SALIDA        │
│  CompraPort, Confirmacion │       │  CatalogoRepository,        │
│  Port, ExpiracionPort,    │◀─────▶│  ReservaRepository,         │
│  CatalogoPort, ...        │ usa   │  PagoGateway, FirmaPort, ...│
└───────────┬───────────────┘       └─────────────────────────────┘
            │ implementa
            ▼
┌───────────────────────────────────────────────────────────────┐
│              NÚCLEO: CASOS DE USO Y DOMINIO                     │
│  CompraService, ConfirmacionService, ExpiracionService,        │
│  CatalogoService  +  Entidades, VOs, Políticas, Excepciones     │
│  (Java puro, sin dependencias de frameworks)                   │
└───────────────────────────────────────────────────────────────┘
```

La inversión de dependencias se logra con **inyección por constructor**: los casos de uso (`CompraService`, `ConfirmacionService`, etc.) son clases Java simples que reciben sus puertos de salida como interfaces en el constructor. El ensamblado entre esas clases puras y los adaptadores anotados con Spring ocurre en un único lugar: `config/UseCaseConfig.java`.

---

## 4. Estructura de paquetes

```
src/main/java/com/stagepass/live/
├── domain/                     # NÚCLEO — Java puro, sin frameworks
│   ├── model/                  # Recinto, Evento, Asiento, DisponibilidadAsiento,
│   │                           # Cliente, Reserva, Pago, BoletoDigital
│   ├── vo/                     # Cotizacion, ConfirmacionPago
│   ├── enums/                  # TipoEvento, EstadoEvento, EstadoAsiento,
│   │                           # EstadoReserva, EstadoPago, ProveedorPago
│   ├── policy/                 # CalculadoraTarifas, PoliticaReserva,
│   │                           # PoliticaLimite, PoliticaEmision
│   └── exception/              # ReglaNegocioException y subtipos
│
├── application/                # NÚCLEO — casos de uso, Java puro
│   ├── port/in/                # CompraPort, ConfirmacionPort, ExpiracionPort,
│   │                           # CatalogoPort, BoletoConsultaPort
│   ├── port/out/               # CatalogoRepository, ClienteRepository,
│   │                           # ReservaRepository, OcupacionRepository,
│   │                           # BoletoRepository, PagoGateway, FirmaPort,
│   │                           # QRPort, NotificacionPort, FacturaPort, RelojPort
│   └── service/                # CompraService, ConfirmacionService,
│                                # ExpiracionService, CatalogoService,
│                                # BoletoConsultaService
│
├── adapter/
│   ├── in/
│   │   ├── rest/                # Controladores REST + DTOs + manejo de errores
│   │   ├── rest/admin/          # Endpoints administrativos protegidos
│   │   ├── rest/demo/           # Endpoints de simulación (solo perfil "local")
│   │   └── scheduler/           # ReservaScheduler (expiración automática)
│   └── out/
│       ├── mysql/                # MySQLCatalogoAdapter (REAL) + entidades JPA
│       ├── mongo/                # MongoReservaAdapter, MongoOcupacionAdapter,
│       │                         # MongoBoletoAdapter (REALES) + documentos
│       ├── cliente/              # MockClienteAdapter (simulado)
│       ├── pago/                 # PagoRouter + PayPal/Wompi/PSE (simulados)
│       ├── firma/                # HmacAdapter (REAL, HMAC-SHA256 autentico)
│       ├── qr/                   # QRAdapter (REAL, libreria ZXing)
│       ├── notificacion/         # NotificacionAdapter + SendGrid/WhatsApp (simulados)
│       ├── factura/              # DianAdapter (simulado)
│       └── reloj/                # SystemClockAdapter (REAL)
│
└── config/                      # Ensamblado Spring ↔ núcleo, seguridad de endpoints admin/demo
```

---

## 5. Diagrama UML

El diagrama de clases completo está disponible en `hexagonal_taller.svg` (visual) y `hexagonal_taller.drawio` (editable). No fue modificado durante este entregable.

### Diferencias resueltas respecto al UML (documentadas, no ocultas)

El UML define el contrato mínimo exigido por el profesor. Para que la API fuera realmente usable se agregaron **extensiones puntuales y mínimas**, siempre como métodos adicionales en los mismos puertos (nunca como puertos nuevos no solicitados), documentadas también en los Javadoc de cada interfaz:

| Puerto / clase        | Extensión agregada                        | Motivo                                                        |
|------------------------|--------------------------------------------|-----------------------------------------------------------------|
| `CompraPort`            | `consultar(reservaId)`                    | Requisito "consultar una reserva"                               |
| `ConfirmacionPort`      | `consultarPago(reservaId)`                | Requisito "simular el resultado de un pago" sin exponer secretos |
| `ExpiracionPort`        | `consultarUltimaEjecucion()`              | Requisito "consultar el proceso de expiración"                 |
| `CatalogoPort`          | `obtenerEvento(id)`, `obtenerRecinto(id)` | Requisito "consultar recintos y asientos" directamente          |
| `PagoGateway`           | `consultar(reservaId)`                    | Exponer referencia externa sin romper encapsulamiento del dominio |
| `BoletoRepository`      | `buscarPorId(id)`                         | Generar el QR de un boleto específico                          |
| `ConfirmacionPago` (VO) | campos `monto` y `exitoso`                | Un webhook real de pago siempre informa un resultado explícito (aprobado/rechazado), no solo una confirmación positiva |
| `Reserva` (entidad)     | campo `subtotal`                          | Permitir mostrar el desglose económico sin recalcularlo          |
| `BoletoConsultaPort`    | puerto nuevo, no existe en el UML          | El UML modela los boletos como entidad de dominio, no como caso de uso independiente; se necesitaba un puerto de entrada dedicado para "consultar los boletos emitidos" sin mezclar responsabilidades con `ConfirmacionPort` |

Ninguna extensión modifica el comportamiento de los métodos definidos en el UML original; solo se agregó lo estrictamente necesario para que la API fuera demostrable.

### Convención adoptada para el cálculo de tarifas

El enunciado no especifica el orden exacto del cálculo. Se adoptó la siguiente convención (documentada también en `CalculadoraTarifas`):

```
SUBTOTAL = suma de los precios base de los asientos seleccionados
TARIFA_ADMINISTRATIVA = SUBTOTAL × porcentaje_de_tarifa_segun_cantidad
DESCUENTO              = SUBTOTAL × porcentaje_de_descuento_segun_cantidad
TOTAL = SUBTOTAL + TARIFA_ADMINISTRATIVA − DESCUENTO
```

Ambos porcentajes se calculan **independientemente sobre el subtotal** (no en cascada uno sobre el otro).

### Validación de vigencia según el instante del proveedor

El enunciado exige que la vigencia de la reserva se valide "según el momento real en que el proveedor confirma el pago, sin confiar únicamente en la hora de recepción del webhook". Esto se implementó así:

- `ConfirmacionPago` (el dato que entrega el webhook) incluye `momentoConfirmacionProveedor`, que es el instante que **el proveedor** reporta como el momento de la aprobación — no el instante en que nuestro servidor procesa la petición HTTP.
- `PoliticaEmision.validar(reserva, pago, instanteConfirmacion)` y `ReservaRepository.aprobarSiVigente(id, instante)` usan explícitamente ese instante (nunca `LocalDateTime.now()`) para decidir si la reserva todavía estaba vigente.
- El `RelojPort` (abstracción inyectable del reloj) solo se usa para calcular el bloqueo inicial (`PoliticaReserva.expiracion`) y la fecha de emisión del boleto — nunca para decidir si un pago llegó a tiempo, precisamente para que esa decisión dependa del dato del proveedor y sea reproducible en pruebas.

---

## 6. Reglas de negocio implementadas

| Regla                                              | Dónde vive                                                        |
|------------------------------------------------------|---------------------------------------------------------------------|
| Bloqueo de asientos por 10 minutos exactos          | `PoliticaReserva` + `OcupacionRepository.reservarAtomico`           |
| Tarifas 10%/0%, 8%/5%, 5%/10%                       | `CalculadoraTarifas`                                                |
| Máximo 6 boletos por cliente/evento                  | `PoliticaLimite`, validado con `contarEmitidos` + `contarReservados`|
| Emisión solo tras pago verificado                     | `PoliticaEmision` + `ConfirmacionService`                           |
| Vigencia según instante del proveedor                | `PoliticaEmision.validar(reserva, pago, instanteConfirmacion)`      |
| Prevención de emisión duplicada (webhook repetido)   | Transición atómica `aprobarSiVigente` + `guardarSinDuplicar` + índices únicos en MongoDB |
| Firma HMAC-SHA256 con clave derivada por evento      | `HmacAdapter` (clave maestra en configuración, nunca en el dominio) |
| No vender dos veces el mismo asiento                  | `_id` determinístico (`eventoId::asientoId`) en MongoDB + inserción atómica |

---

## 7. Puertos y casos de uso implementados

**Puertos de entrada:** `CompraPort` → `CompraService`, `ConfirmacionPort` → `ConfirmacionService`, `ExpiracionPort` → `ExpiracionService`, `CatalogoPort` → `CatalogoService`, `BoletoConsultaPort` → `BoletoConsultaService`.

**Puertos de salida:** `CatalogoRepository`, `ClienteRepository`, `ReservaRepository`, `OcupacionRepository`, `BoletoRepository`, `PagoGateway`, `FirmaPort`, `QRPort`, `NotificacionPort`, `FacturaPort`, `RelojPort`.

---

## 8. Los dos adaptadores reales (obligatorios) y los mocks

### Adaptadores REALES (se conectan a bases de datos reales, verificado con Testcontainers)

- **MySQL** — `MySQLCatalogoAdapter` (`adapter/out/mysql/`): lee recintos, eventos y asientos desde MySQL vía Spring Data JPA. Sin reglas de negocio.
- **MongoDB** — `MongoReservaAdapter`, `MongoOcupacionAdapter`, `MongoBoletoAdapter` (`adapter/out/mongo/`): persisten reservas, bloqueos de asientos y boletos, con índices únicos y operaciones atómicas que impiden vender dos veces el mismo asiento o emitir boletos duplicados.
- **Firma digital** — `HmacAdapter`: HMAC-SHA256 real (no simulado), usando `javax.crypto.Mac`.
- **Código QR** — `QRAdapter`: generación real con la librería ZXing.
- **Reloj** — `SystemClockAdapter`: usa el reloj real del sistema.

### Adaptadores SIMULADOS (mocks, declarados explícitamente como tales en su Javadoc)

- **Pagos**: `PayPalAdapter`, `WompiAdapter`, `PSEAdapter` — cada uno mantiene su propio "libro" de transacciones en memoria; ninguno llama a un API externo real. Todos comparten el contrato `PagoGateway` y se seleccionan mediante `PagoRouter`.
- **Cliente**: `MockClienteAdapter` — clientes de demostración en memoria.
- **Notificaciones**: `SendGridAdapter` (correo) y `WhatsAppAdapter` (mensajería) — solo registran en el log lo que habrían enviado; `NotificacionAdapter` los compone.
- **Facturación electrónica**: `DianAdapter` — genera un identificador simulado ("CUFE-SIM-...") y lo registra en el log; no se conecta con los servicios reales de la DIAN.

---

## 9. Requisitos previos

- **Java 21** (se usó Eclipse Adoptium JDK 21.0.8 durante el desarrollo; también funciona con JDK más nuevos usando `--release 21`).
- **Docker Desktop** (para MySQL y MongoDB locales). Si no está disponible, se puede usar una instalación nativa de MySQL 8+ y MongoDB 7+.
- No es necesario instalar Maven: el proyecto incluye **Maven Wrapper** (`mvnw.cmd` en Windows).

Verificar Java:

```powershell
java -version
```

---

## 10. Configuración de MySQL y MongoDB

### Opción A — Docker Compose (recomendada)

```powershell
docker compose up -d
```

Esto levanta:

- **MySQL 8.4** en `localhost:3306`, con el esquema y datos de prueba cargados automáticamente desde `sql/01-schema.sql` y `sql/02-data.sql` (montados como scripts de inicialización).
- **MongoDB 7** en `localhost:27017`, con las colecciones e índices únicos creados automáticamente desde `mongo/init-mongo.js`.

Para destruir también los volúmenes (empezar desde cero):

```powershell
docker compose down -v
```

### Opción B — Instalación local manual

1. Crear la base de datos `stagepass` en MySQL y ejecutar `sql/01-schema.sql` y luego `sql/02-data.sql`.
2. Crear la base de datos `stagepass` en MongoDB y ejecutar el script `mongo/init-mongo.js` con `mongosh` (crea el usuario de aplicación y los índices únicos).

### Variables de entorno

Copiar `.env.example` como `.env` y ajustar si es necesario (los valores por defecto ya coinciden con `docker-compose.yml`):

```powershell
Copy-Item .env.example .env
```

El `.env` **no se usa automáticamente por `mvnw`**: en PowerShell, cárguelo así antes de ejecutar el proyecto (o defina las variables manualmente):

```powershell
Get-Content .env | Where-Object { $_ -match '=' -and -not $_.StartsWith('#') } | ForEach-Object {
    $name, $value = $_.Split('=', 2)
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
}
```

---

## 11. Compilar, ejecutar y probar (Windows PowerShell)

Todos los comandos se ejecutan desde la raíz del repositorio.

### Compilar

```powershell
.\mvnw.cmd clean compile
```

### Ejecutar las pruebas unitarias (rápidas, sin Docker)

```powershell
.\mvnw.cmd test
```

### Ejecutar TODAS las pruebas, incluidas las de integración con Testcontainers (requiere Docker en ejecución)

```powershell
.\mvnw.cmd verify
```

### Ejecutar la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080` (configurable con la variable `SERVER_PORT`).

### Empaquetar

```powershell
.\mvnw.cmd clean package
java -jar target\stagepass-live.jar
```

---

## 12. Ejemplos de peticiones y respuestas de la API

### Consultar eventos

```http
GET /api/eventos
```
```json
[
  {
    "id": "evt-rock-en-vivo",
    "nombre": "Rock en Vivo - Gira 2026",
    "tipo": "CONCIERTO",
    "recintoId": "recinto-arena-norte",
    "fechaHora": "2026-12-05T20:00:00",
    "estado": "PROGRAMADO"
  }
]
```

### Consultar asientos de un evento

```http
GET /api/eventos/evt-rock-en-vivo/asientos
```

### Iniciar una compra

```http
POST /api/compras
Content-Type: application/json

{
  "clienteId": "cliente-demo-1",
  "eventoId": "evt-rock-en-vivo",
  "asientoIds": ["asi-an-vip1", "asi-an-vip2"],
  "proveedor": "WOMPI"
}
```
```json
{
  "id": "32d25600-bb3c-4d37-90db-c143f59e4fee",
  "eventoId": "evt-rock-en-vivo",
  "clienteId": "cliente-demo-1",
  "asientoIds": ["asi-an-vip1", "asi-an-vip2"],
  "subtotal": 500000.00,
  "tarifaServicio": 50000.00,
  "descuento": 0.00,
  "total": 550000.00,
  "expiraEn": "2026-10-15T03:16:05.56",
  "estado": "PENDIENTE_PAGO"
}
```

### Intentar reservar el mismo asiento con otro cliente (concurrencia)

```http
POST /api/compras   -> 409 Conflict
{"message": "Uno o mas de los asientos seleccionados ya no estan disponibles para este evento"}
```

### Simular el resultado de un pago (solo perfil `local`, requiere `X-Admin-Key`)

```http
POST /api/demo/pagos/32d25600-bb3c-4d37-90db-c143f59e4fee/simular
X-Admin-Key: <STAGEPASS_ADMIN_API_KEY>
Content-Type: application/json

{"exitoso": true}
```

### Webhook genérico de confirmación de pago (como lo llamaría una pasarela real)

```http
POST /api/webhooks/pagos
Content-Type: application/json

{
  "reservaId": "32d25600-bb3c-4d37-90db-c143f59e4fee",
  "referencia": "WOMPI-35830732",
  "proveedor": "WOMPI",
  "monto": 550000.00,
  "exitoso": true,
  "momentoConfirmacionProveedor": "2026-10-15T03:06:23.81"
}
```

### Consultar los boletos emitidos

```http
GET /api/boletos/reserva/32d25600-bb3c-4d37-90db-c143f59e4fee
```
```json
[
  {
    "id": "ddd2a10a-58ba-4929-ab92-9770bd7301d3",
    "reservaId": "32d25600-bb3c-4d37-90db-c143f59e4fee",
    "asientoId": "asi-an-vip1",
    "codigoRedencion": "DKZWX65V9F",
    "firmaHash": "cf587ce2004974d30f86a3435bab26d19419c2520d4d94f404b94efa7c42abb3",
    "fechaEmision": "2026-10-15T03:06:23.815"
  }
]
```

### Consultar el QR de un boleto

```http
GET /api/boletos/ddd2a10a-58ba-4929-ab92-9770bd7301d3/qr
```
```json
{"boletoId": "ddd2a10a-...", "imagenBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."}
```

### Ejecutar y consultar el proceso de expiración (requiere `X-Admin-Key`)

```http
POST /api/admin/expiracion/ejecutar
GET  /api/admin/expiracion/estado
X-Admin-Key: <STAGEPASS_ADMIN_API_KEY>
```
```json
{"reservasExpiradas": 1, "ejecutadoEn": "2026-10-15T10:10:00"}
```

---

## 13. Secuencia de demostración: compra exitosa

1. `GET /api/eventos` → elegir un `eventoId` con estado `PROGRAMADO`.
2. `GET /api/eventos/{eventoId}/asientos` → elegir uno o más `asientoId`.
3. `POST /api/compras` con `clienteId`, `eventoId`, `asientoIds` y `proveedor` → se obtiene una `Reserva` en estado `PENDIENTE_PAGO`, con los asientos bloqueados en MongoDB durante 10 minutos.
4. `POST /api/demo/pagos/{reservaId}/simular` con `{"exitoso": true}` (header `X-Admin-Key`) → internamente se construye la misma `ConfirmacionPago` que generaría un webhook real y se procesa con `ConfirmacionPort.confirmar`.
5. La reserva pasa a `APROBADA`, los asientos pasan a `VENDIDO` y se emite **un boleto por asiento**, firmado con HMAC-SHA256.
6. `GET /api/boletos/reserva/{reservaId}` → confirma los boletos emitidos.
7. `GET /api/boletos/{boletoId}/qr` → obtiene el código QR del boleto.
8. Repetir el paso 4 con los mismos datos (webhook duplicado) → la respuesta es `200 OK` y **no se generan boletos adicionales** (idempotencia verificada).

## 14. Demostración de expiración de una reserva

1. Iniciar una compra (`POST /api/compras`) sin pagarla.
2. Verificar `GET /api/compras/{reservaId}` → estado `PENDIENTE_PAGO`, con `expiraEn` a 10 minutos de la creación.
3. Esperar a que `ReservaScheduler` se ejecute automáticamente (cada 60 s, configurable con `stagepass.expiracion.scheduler-fijo-ms`) **después** de que `expiraEn` haya pasado, o forzarlo manualmente:
   ```http
   POST /api/admin/expiracion/ejecutar
   X-Admin-Key: <STAGEPASS_ADMIN_API_KEY>
   ```
4. `GET /api/compras/{reservaId}` → el estado ahora es `EXPIRADA` y los asientos volvieron a estar `DISPONIBLE` (se puede comprobar iniciando una nueva compra con los mismos asientos).

> Nota: las pruebas automatizadas (`PoliticaReservaTest`, `ExpiracionServiceTest`, `MongoReservaAdapterIT`) verifican esta regla sin esperar los 10 minutos reales, usando un reloj controlable (`RelojFijo`) e instantes explícitos.

---

## 15. Cómo se cumple cada requisito del profesor

| Requisito                                             | Cómo se cumple                                                                 |
|---------------------------------------------------------|----------------------------------------------------------------------------------|
| Separación dominio / aplicación / puertos / adaptadores | Paquetes `domain`, `application/port`, `application/service`, `adapter/in`, `adapter/out` |
| Núcleo sin dependencias de frameworks                  | Verificado por inspección (sección 3) y por que el proyecto compila con esa restricción |
| Inversión de dependencias + inyección por constructor  | Todos los servicios reciben sus puertos por constructor; ensamblado en `UseCaseConfig` |
| Dos adaptadores reales (MySQL + MongoDB)               | `MySQLCatalogoAdapter`, `Mongo{Reserva,Ocupacion,Boleto}Adapter`, verificados con Testcontainers |
| Bloqueo de 10 minutos, liberación automática            | `PoliticaReserva` + `OcupacionRepository` + `ReservaScheduler`                   |
| Tarifas y descuentos exactos                           | `CalculadoraTarifas`, cubierto por `CalculadoraTarifasTest`                      |
| Límite de 6 boletos, protegido ante concurrencia        | `PoliticaLimite` + conteo atómico vía consultas a MongoDB en el momento de la compra |
| Emisión solo tras pago verificado, sin duplicados       | `PoliticaEmision` + `ConfirmacionService` + índices únicos en MongoDB            |
| Firma HMAC-SHA256 real, clave fuera del dominio         | `HmacAdapter`, clave maestra en `application.yml`/variable de entorno            |
| API REST documentada, códigos HTTP correctos            | `GlobalExceptionHandler` + DTOs; ver sección 12                                  |
| Endpoints de simulación protegidos                       | Perfil `local` (`@Profile`) + `AdminSecurityInterceptor` (header `X-Admin-Key`)  |
| Pruebas de los 12 escenarios exigidos                   | Ver sección 16                                                                   |
| Sin credenciales reales en el repositorio                | `.env` está en `.gitignore`; solo se versiona `.env.example` con valores de ejemplo |

---

## 16. Pruebas automatizadas

54 pruebas en total, ejecutables con `.\mvnw.cmd verify` (38 unitarias + 16 de integración real).

| # | Escenario exigido                                      | Prueba                                                        |
|---|------------------------------------------------------------|-----------------------------------------------------------------|
| 1 | Tarifa 10% / descuento 0%                                  | `CalculadoraTarifasTest.aplicaTarifaDiezPorCientoYSinDescuento...` |
| 2 | Tarifa 8% / descuento 5%                                   | `CalculadoraTarifasTest.aplicaTarifaOchoPorCientoYDescuentoCinco...` |
| 3 | Tarifa 5% / descuento 10%                                  | `CalculadoraTarifasTest.aplicaTarifaCincoPorCientoYDescuentoDiez...` |
| 4 | Rechazo de compras que superen seis boletos                | `PoliticaLimiteTest`, `CompraServiceTest.rechazaCompraQueExcedeElLimite...` |
| 5 | Bloqueo de asientos durante diez minutos                   | `PoliticaReservaTest.elBloqueoDuraExactamenteDiezMinutos`       |
| 6 | Expiración y liberación de reservas                        | `ExpiracionServiceTest`, `MongoReservaAdapterIT.expirarSiPendiente...` |
| 7 | Rechazo de un pago tardío no confirmado oportunamente       | `PoliticaEmisionTest.rechazaConfirmacionTardia...`, `ConfirmacionServiceTest.rechazaConfirmacionCuandoElProveedorConfirmaDespues...` |
| 8 | Emisión solamente después del pago verificado               | `PoliticaEmisionTest.rechazaSiElPagoNoEstaVerificado`, `ConfirmacionServiceTest.confirmaElPagoYEmite...` |
| 9 | Generación y validación de firmas HMAC-SHA256                | `HmacAdapterTest`                                               |
| 10| Prevención de boletos duplicados                            | `ConfirmacionServiceTest.esIdempotente...`, `MongoBoletoAdapterIT.guardarSinDuplicar...` |
| 11| Rechazo de compras simultáneas para el mismo asiento         | `MongoOcupacionAdapterIT.soloUnaDeVariasSolicitudesConcurrentesLogra...` (20 hilos reales contra MongoDB real) |
| 12| Funcionamiento real de MySQL y MongoDB                      | `MySQLCatalogoAdapterIT`, `Mongo{Reserva,Ocupacion,Boleto}AdapterIT` (Testcontainers) |

### Pruebas de regresión agregadas en la auditoría final

| Defecto corregido                                                                 | Prueba de regresión                                                                              |
|-------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| Carrera entre dos compras concurrentes del mismo cliente/evento con asientos distintos, pudiendo superar el límite de 6 boletos | `CompraServiceTest.dosComprasSimultaneasDelMismoClienteParaElMismoEventoNoSuperanElLimiteDeSeisBoletos` (2 hilos reales) |
| `contarReservados` contaba documentos de reserva en vez de asientos/boletos reservados, subestimando el total cuando una reserva tenía varios asientos | `MongoReservaAdapterIT.contarReservadosSumaLosAsientosDeCadaReservaYNoSoloElNumeroDeDocumentos` |
| Fuga de bloqueos de asientos si un error inesperado (no "ya tomado") interrumpía el bloqueo de varios asientos a mitad de la solicitud | `MongoOcupacionAdapterTest.liberaLosAsientosYaBloqueadosSiUnErrorInesperadoInterrumpeElBloqueoDeVariosAsientos` |

---

## 17. Validación final realizada

Lo siguiente **sí se ejecutó y se verificó** durante el desarrollo de este entregable (no son afirmaciones sin comprobar):

- ✅ `.\mvnw.cmd clean compile` — compila sin errores.
- ✅ `.\mvnw.cmd clean verify` — **54/54 pruebas pasan** (38 unitarias + 16 de integración), incluidas las de integración contra MySQL y MongoDB reales levantados con Testcontainers.
- ✅ El núcleo (`domain`, `application`) no contiene ninguna importación de Spring/JPA/MongoDB/HTTP (verificado por búsqueda de texto, sección 3).
- ✅ Los 15 puertos de entrada/salida exigidos están implementados y wireados en `UseCaseConfig`.
- ✅ Se levantó `docker compose up -d` (MySQL 8.4 + MongoDB 7 reales) y la aplicación arrancó contra ellos (`.\mvnw.cmd spring-boot:run`), confirmado con los logs de Hikari (`HikariPool-1 - Added connection ... com.mysql.cj.jdbc.ConnectionImpl`) y del driver de Mongo (`Monitor thread successfully connected to server`).
- ✅ Recorrido de compra completo probado manualmente contra las bases de datos reales: listar catálogo → iniciar compra → bloqueo verificado (409 al intentar tomar el mismo asiento) → simular pago → boletos emitidos con firma y QR real → webhook duplicado confirmado como idempotente.
- ✅ Durante esa verificación manual se detectó y corrigió un error real: la confirmación idempotente de una reserva ya `APROBADA` fallaba porque `PoliticaEmision.validar` evaluaba vigencia sobre una reserva que ya no estaba `PENDIENTE_PAGO`. Se corrigió reordenando `ConfirmacionService.confirmar` para resolver primero el caso idempotente, y se agregó una prueba de regresión (`ConfirmacionServiceTest.esIdempotenteSiElWebhookLlegaMuchoDespuesCuandoLaReservaYaEstaAprobadaDesdeElPrimerFetch`).
- ✅ Tarifas y límites verificados con compras reales: 2, 3 y 6 boletos producen exactamente los montos esperados (10%/0%, 8%/5%, 5%/10%); una séptima unidad para un cliente con 3 boletos ya emitidos fue rechazada con `409`.
- ✅ Se confirmó que no hay credenciales reales en el repositorio: solo existe `.env.example` (con valores de ejemplo, no secretos); `.env` está excluido por `.gitignore`.

### Auditoría final previa a la entrega

En una segunda auditoría enfocada en concurrencia y seguridad se encontraron y corrigieron **tres defectos reales adicionales**, cada uno con su prueba de regresión (detalladas en la sección 16):

- **Carrera en el límite de 6 boletos**: `CompraService.iniciar` leía los conteos de boletos reservados/emitidos *fuera* de cualquier candado, antes de bloquear los asientos. Dos compras concurrentes del mismo cliente para el mismo evento, con asientos distintos, podían leer el mismo conteo y aprobarse ambas aunque juntas superaran el límite. Se corrigió serializando la validación y el bloqueo con un candado en memoria por `(clienteId, eventoId)` (ver limitación técnica #2).
- **Unidad de conteo incorrecta en `contarReservados`**: `MongoReservaAdapter.contarReservados` contaba *documentos* de reserva en lugar de *asientos/boletos* reservados. Una reserva pendiente con varios asientos se contaba como "1", no como su cantidad real de asientos, lo que permitía subestimar el total y superar el límite de 6 boletos.
- **Fuga de bloqueos parciales ante errores inesperados**: si un error de MongoDB distinto de "asiento ya tomado" interrumpía `MongoOcupacionAdapter.reservarAtomico` a mitad del bloqueo de varios asientos, los asientos ya bloqueados en ese mismo intento no se liberaban. Se corrigió envolviendo todo el bucle en un `try/catch` que libera cualquier bloqueo parcial antes de propagar la excepción.

Además, se revisó y reforzó la documentación (Javadoc + README) para que el webhook de pago simulado no pueda confundirse con una verificación de pago real o segura para producción (ver limitación técnica #1), y se verificó `git status` para confirmar que no hay archivos compilados, `.env`, contraseñas ni carpetas generadas rastreadas por git.

Lo siguiente **no se dejó corriendo permanentemente** y debe reproducirse si se desea volver a verificar (los pasos exactos están en las secciones 10 a 14):

- Los contenedores de Docker se detuvieron al finalizar la verificación (`docker compose down`), pero sus volúmenes persisten para una próxima ejecución.
- El servidor de la aplicación no quedó en ejecución tras completar las pruebas manuales.

---

## 18. Limitaciones técnicas conocidas

Estas limitaciones son **deliberadas y documentadas**, no defectos ocultos. Se dejan explícitas para que no se confundan con garantías de nivel de producción:

1. **El webhook de pago (`/api/webhooks/pagos`) es una simulación, no una verificación de pago segura.** No valida ninguna firma criptográfica del proveedor sobre la petición HTTP entrante (a diferencia de un webhook real de PayPal/Wompi/PSE, que firma su payload con un secreto compartido). El campo `exitoso` del cuerpo de la petición es, en este taller, una simple afirmación del llamador. La "verificación" real ocurre solo contra el registro interno en memoria de la pasarela simulada (referencia + monto), nunca mediante criptografía de transporte. **Este endpoint, tal como está implementado, nunca debe exponerse a internet en un entorno de producción** sin agregar validación de firma del proveedor. Esto está señalado explícitamente en el Javadoc de `PagoWebhookController` y de `PasarelaPagoSimulada.verificar`.

2. **El límite de 6 boletos por cliente/evento se protege contra condiciones de carrera con un candado en memoria, válido solo para una única instancia de la aplicación.** `CompraService` serializa la validación del límite y el bloqueo de asientos con un `synchronized` por `(clienteId, eventoId)` (ver Javadoc de la clase). Esto es correcto y suficiente para el alcance de este taller (una sola instancia), pero **no funcionaría correctamente si la aplicación se escalara horizontalmente a varias instancias**, ya que cada instancia tendría su propio candado en memoria. Una solución para ese escenario requeriría un candado distribuido (p. ej. Redis) o un contador atómico con límite superior a nivel de base de datos.

3. **MongoDB se usa en modo standalone (sin replica set) en `docker-compose.yml`, por lo que no se utilizan transacciones multi-documento.** Todas las operaciones de concurrencia (bloqueo de asientos, aprobación de reservas, emisión de boletos) se diseñaron deliberadamente como operaciones atómicas de un único documento (ver Javadoc de `MongoOcupacionAdapter`), precisamente para no depender de transacciones. Esto funciona correctamente para las garantías exigidas por el taller (no vender el mismo asiento dos veces, no emitir boletos duplicados), pero significa que no hay una garantía transaccional "todo o nada" entre colecciones distintas (por ejemplo, entre `reservas` y `disponibilidad_asientos`); la consistencia entre ambas se logra con los métodos explícitos de rollback (`liberar`) en cada punto de fallo, no con una transacción de base de datos.

4. **La protección de los endpoints administrativos y de demostración (`X-Admin-Key`) es una clave compartida simple, no un mecanismo de autenticación completo.** No hay usuarios, roles, expiración de credenciales ni registro de auditoría de acceso. Es la protección mínima exigida por el taller para no exponer esos endpoints sin ningún control, no un sistema de autenticación para producción.

5. **El contador de boletos reservados (`contarReservados`) y el límite de 6 boletos consultan el estado actual en cada compra; no hay un mecanismo de "reserva de cupo" previo.** Esto es correcto gracias al candado por `(clienteId, eventoId)` (ver punto 2), pero implica que, bajo el **mismo** candado, las operaciones de lectura y escritura son secuenciales (no paralelas) para un mismo cliente en un mismo evento. Esto es intencional (es justamente la corrección aplicada en esta auditoría) y no afecta a compras de clientes o eventos distintos, que sí se procesan en paralelo sin bloquearse entre sí.

---

## 19. Registro de archivos creados

- **101 archivos Java** en `src/main/java` (dominio, aplicación, adaptadores, configuración).
- **14 archivos Java** en `src/test/java` (pruebas unitarias e integración).
- Scripts de infraestructura: `pom.xml`, `mvnw`/`mvnw.cmd`, `.mvn/wrapper/*`, `docker-compose.yml`, `sql/01-schema.sql`, `sql/02-data.sql`, `mongo/init-mongo.js`.
- Configuración: `src/main/resources/application.yml`, `.env.example`, `.gitignore`.
- Documentación: este `README.md`.

Resultado de la última ejecución completa de pruebas (`.\mvnw.cmd clean verify`, auditoría final): **54 pruebas, 0 fallos, 0 errores** (`BUILD SUCCESS`).
