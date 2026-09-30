# Prueba API - Catálogo de productos GestoPago

Servicio Spring Boot que sincroniza el catálogo de productos de GestoPago
(API externa) hacia MongoDB y lo expone en JSON vía REST/Swagger.

## Flujo

```
Cron 06:00 AM  ->  Client (Feign, GET getProductList.do)  ->  XML
     |                                                          |
     v                                                          v
 sincronizar()  ->  parseo JAXB + mapeo  ->  MongoDB (colección "productos")
                                                          |
 GET /productos (Swagger)  <-  consultar()  <-------------+  (JSON)
```

- **Escritura:** el cron (o `POST /productos/sincronizar`) llama a GestoPago,
  parsea el XML, mapea a documentos y reemplaza el catálogo en Mongo.
- **Lectura:** `GET /productos` sirve el catálogo desde Mongo, no desde GestoPago.

## Stack

- Java 17, Spring Boot 3.3.6, Gradle
- Spring Cloud OpenFeign (cliente HTTP)
- JAXB (parseo XML), MapStruct (mapeo DTO -> documento)
- Spring Data MongoDB (Atlas) + Spring Data JPA/PostgreSQL (Neon, módulo previo)
- JUnit 5 + Mockito (pruebas)

## Arquitectura (capas)

| Capa            | Clases (feature GestoPago/productos)                          |
|-----------------|--------------------------------------------------------------|
| Controller      | `GestoPagoProductosController`                                |
| Service         | `GestoPagoProductosService`, `CatalogoProductosService`      |
| Client          | `GestoPagoProductosClient` (Feign)                           |
| Config          | `GestoPagoProductosClientConfig`, `GestoPagoProductosErrorDecoder` |
| DTO (XML)       | `GetProductListResponse`, `Mensaje`, `Producto`             |
| Document (Mongo)| `ProductoDocument`                                           |
| Repository      | `ProductoRepository`                                         |
| Mapper          | `ProductoMapper`                                             |
| Job             | `CatalogoProductosSyncJob` (cron)                           |
| Excepciones     | `GestoPagoProductosException` (+ Auth, Communication)       |

## Configuración

Los secretos se cargan desde un archivo `.env` en la raíz (via `spring-dotenv`).
Copia `.env.example` a `.env` y completa los valores:

```
DB_URL, DB_USERNAME, DB_PASSWORD            # PostgreSQL (Neon)
GESTOPAGO_AUTH_PASSWORD                       # auth GestoPago
GESTOPAGO_PRODUCTOS_TOKEN                      # Bearer token getProductList
MONGO_URI                                     # MongoDB Atlas
```

El `.env` está en `.gitignore` (no se sube). El resto de config no sensible
vive en `application.properties`.

## Endpoints

| Método | Ruta                      | Descripción                              |
|--------|---------------------------|------------------------------------------|
| POST   | `/productos/sincronizar`  | Trae de GestoPago y guarda en Mongo      |
| GET    | `/productos`              | Consulta el catálogo desde Mongo         |

Swagger UI: `http://localhost:8081/swagger-ui.html`

## Cron

`CatalogoProductosSyncJob` ejecuta la sincronización diaria a las 06:00 AM.
Configurable con `gestopago.productos.sync-cron` (expresión cron de 6 campos).

## Ejecutar

```
gradlew bootRun      # levanta la app en el puerto 8081
gradlew test         # corre las pruebas unitarias
```

## Decisiones técnicas

- **Feign en vez de RestTemplate:** se sigue la convención existente del proyecto
  (`GestoPagoAuthClient`). Config aislada por cliente (`configuration = ...`) para
  que el interceptor del Bearer no afecte a otros clientes.
- **Token desde configuración, no hardcodeado:** un `RequestInterceptor` arma
  `Authorization: Bearer <token>` leyendo el valor del `.env`.
- **Cliente devuelve String (XML) y el service parsea con JAXB:** evita pelear con
  la negociación de contenido de Feign y facilita las pruebas (mock del String).
- **DTO (XML) separado del Document (Mongo):** cada capa con su modelo; MapStruct
  convierte entre ambos sin código repetitivo.
- **`idProducto` como `_id` de Mongo:** re-sincronizar sobreescribe en vez de
  duplicar.
- **Sincronización = reemplazo completo** (`deleteAll` + `saveAll`): deja una foto
  fresca del catálogo; los productos retirados en GestoPago desaparecen.
- **Manejo de errores:** `ErrorDecoder` mapea 401/403 (auth) y otros HTTP
  (no exitosos); el service atrapa timeouts y fallos de comunicación. Los logs no
  exponen token ni datos sensibles.
