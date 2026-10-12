# Contrato y diseño — API de franquicias

## Estado

Nueve endpoints implementados.

Persistencia en MongoDB Atlas aprovisionada con Terraform. API empaquetada con Docker y desplegada en Render.

Verificación completa: 83 pruebas exitosas, sin fallos ni omisiones, y generación del JAR ejecutable.

API pública:

https://nequi-franchises.onrender.com

Repositorio:

https://github.com/gioarana/nequi-franchises

## Alcance

Administración de franquicias, sucursales y productos con stock.

Se implementan todos los endpoints solicitados y los extras de:

- Docker.
- Programación reactiva y transformaciones funcionales.
- Cambio de nombre de franquicia, sucursal y producto.
- Aprovisionamiento de persistencia con Terraform.
- Despliegue completo en la nube.

El repositorio público conserva el historial de trabajo con Git.

## Reglas de negocio

- Los nombres son obligatorios.
- Se eliminan los espacios de los extremos.
- Su longitud debe estar entre 1 y 100 caracteres después del recorte.
- Se conservan mayúsculas y acentos.
- Se permiten nombres repetidos.
- Las identidades son UUID.
- El stock es obligatorio al crear o actualizar un producto.
- El stock debe ser un entero entre 0 y 2147483647.
- Los valores decimales se rechazan antes de ejecutar el servicio.
- Actualizar stock reemplaza la cantidad; no la incrementa.
- Los inventarios son independientes por sucursal.
- Se comprueba toda la jerarquía indicada en la URL.
- Una sucursal ajena a la franquicia o un producto ajeno a la sucursal producen 404.
- La eliminación es física.
- Eliminar nuevamente un producto produce 404.
- Renombrar conserva identificadores y contenido.
- Renombrar un producto conserva su stock.

Los identificadores del código y los campos JSON se escriben en inglés. Los mensajes definidos por la aplicación se escriben en español.

## Consulta de máximos

- Devuelve un producto por sucursal con inventario.
- Incluye identificador y nombre de la sucursal.
- Omite sucursales sin productos.
- Considera válido el stock cero.
- En empates selecciona el UUID de producto menor en orden lexicográfico.
- Ordena los resultados por UUID de sucursal en orden lexicográfico.
- Una franquicia existente sin productos devuelve 200 con `[]`.
- Una franquicia inexistente devuelve 404.

## Contrato HTTP

Prefijo: `/api/v1`.

| Método | Ruta | Cuerpo | Éxito |
|---|---|---|---|
| POST | `/franchises` | `{"name":"Café Andino"}` | 201 |
| POST | `/franchises/{franchiseId}/branches` | `{"name":"Chapinero"}` | 201 |
| POST | `/franchises/{franchiseId}/branches/{branchId}/products` | `{"name":"Café","stock":20}` | 201 |
| DELETE | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Sin cuerpo | 204 |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | `{"stock":35}` | 200 |
| GET | `/franchises/{franchiseId}/products/max-stock` | Sin cuerpo | 200 |
| PATCH | `/franchises/{franchiseId}/name` | `{"name":"Nuevo nombre"}` | 200 |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/name` | `{"name":"Nueva sucursal"}` | 200 |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | `{"name":"Nuevo producto"}` | 200 |

Los parámetros de identidad son UUID.

Las creaciones incluyen `Location` como identificador del recurso. No se implementan consultas individuales en esas rutas.

Las respuestas de entidades y ejemplos están documentados en el README.

## Errores

El manejador central utiliza `application/problem+json` para los errores contemplados.

| Estado | Situación | Título |
|---|---|---|
| 400 | Restricciones de validación de campos | Error de validación |
| 400 | JSON o parámetros con formato incorrecto | Solicitud inválida |
| 404 | Recurso inexistente o pertenencia incorrecta | Recurso no encontrado |
| 409 | Conflicto de versión | Modificación concurrente |
| 503 | Excepciones de conexión y disponibilidad contempladas | Persistencia no disponible |

Los errores de validación incluyen una extensión `errors` con el campo y su mensaje.

No se exponen detalles internos de las excepciones de concurrencia o conexión.

Los errores inesperados conservan el manejo predeterminado de Spring Boot; no se implementa un manejador genérico personalizado para todos los fallos posibles.

## Arquitectura

Un módulo Maven y un servicio desplegable.

```text
HTTP / WebFlux
      ↓
Servicios de aplicación
      ↓
FranchiseRepository
      ↓
MongoFranchiseRepositoryAdapter
      ↓
SpringDataFranchiseRepository
      ↓
MongoDB Atlas
```

Responsabilidades:

- Dominio: modelos inmutables y reglas.
- Aplicación: casos de uso, coordinación y resultados.
- Puerto de salida: contrato de persistencia.
- Adaptador HTTP: rutas, DTO y errores.
- Adaptador MongoDB: documentos, conversión y persistencia.
- Configuración: registro de servicios en Spring.

Tecnologías principales:

- Java 21.
- Spring Boot 4.1.1.
- Spring WebFlux.
- Spring Data Reactive MongoDB.
- Jakarta Validation.
- Spring Boot Actuator.
- Reactor y Reactor Test.
- JUnit, Mockito y Testcontainers.
- Docker y Terraform.

## Persistencia

Colección: `franchises`.

Cada franquicia se guarda como un documento con sus sucursales y productos embebidos.

El documento incluye una versión administrada mediante `@Version`.

El identificador de franquicia se almacena como texto. Los UUID embebidos utilizan representación BSON estándar.

La base `nequi_franchises` y la colección se crean al insertar el primer documento.

### Decisión del modelo

El documento embebido permite leer la jerarquía mediante una consulta y realizar escrituras atómicas dentro de un documento.

Para el alcance de la prueba evita transacciones entre colecciones y mantiene una implementación sencilla.

Sus límites son:

- Tamaño máximo del documento.
- Crecimiento del inventario.
- Contención de escrituras dentro de una misma franquicia.

Para mayor escala convendría evaluar colecciones separadas, índices y actualizaciones más específicas.

## Concurrencia

El adaptador lee el documento y conserva su versión al guardar los cambios.

Si otra escritura modificó la versión, Spring Data rechaza el guardado mediante `OptimisticLockingFailureException`.

La API traduce el conflicto a 409.

No se reintentan automáticamente los casos de uso. Los posibles reintentos de transporte del driver son distintos de repetir una operación de negocio.

La prueba de persistencia reproduce el conflicto leyendo dos copias con la misma versión, guardando una y rechazando la otra. Comprueba además que el primer cambio permanece guardado.

## Programación reactiva y funcional

Los servicios y controladores utilizan `Mono`.

WebFlux realiza la suscripción HTTP. El código de aplicación no utiliza `block()` ni suscripciones manuales.

El acceso MongoDB utiliza el driver reactivo.

Los Streams de Java realizan transformaciones síncronas en memoria. No generan consultas adicionales.

`Mono.defer` retrasa la ejecución hasta la suscripción; no crea un hilo ni transforma acceso bloqueante en no bloqueante.

## Costo de la consulta de máximos

Se realiza una lectura de franquicia.

El cálculo recorre los productos con costo O(P). El ordenamiento agrega O(B log B), donde B es la cantidad de sucursales con productos.

No se consulta MongoDB una vez por sucursal.

## Verificación automatizada

83 pruebas:

- 23 de dominio.
- 50 de servicios.
- 1 de arranque con componentes MongoDB simulados.
- 3 de persistencia real en un contenedor.
- 6 HTTP con servicios simulados.

Las pruebas de persistencia verifican recuperación de datos, incremento de versión y rechazo de escrituras desactualizadas.

Las pruebas HTTP verifican rechazo de decimales y stock ausente, además de respuestas 404, 409 y 503.

Estas pruebas no requieren credenciales ni modifican Atlas.

La suite completa requiere Docker.

## Verificación manual

Se comprobaron:

- Las nueve operaciones de negocio desde la ejecución local.
- Persistencia de los cambios en Atlas.
- Rechazo de entradas inválidas y recursos inexistentes.
- Conservación de identidad e inventario al renombrar.
- Consulta de máximos con inventarios vacíos y con productos.
- Ejecución Docker conectada a Atlas.
- Despliegue en Render.
- Información, salud y consulta de máximos mediante la URL pública.
- Rechazo público de stock decimal y ausente con mensajes en español.

Las pruebas HTTP automatizadas no cubren todos los recorridos exitosos de los nueve endpoints. La indisponibilidad y el conflicto HTTP se verifican mediante errores simulados; el conflicto de persistencia se verifica con MongoDB real.

## Infraestructura

MongoDB Atlas Free:

- Clúster M0.
- AWS.
- Región US_EAST_1.

Render Free:

- Región Ohio.
- Ejecución de la imagen construida desde el Dockerfile.

Terraform administra proyecto, clúster, usuario y reglas de acceso.

El usuario de la API tiene `readWrite` sobre `nequi_franchises`, limitado al clúster.

## Credenciales y red

Las credenciales administrativas de Terraform son distintas de las credenciales de datos de la API.

Los secretos se proporcionan mediante variables de entorno.

Se excluyen de Git:

- `.env`.
- `terraform.tfvars`.
- Estado de Terraform y sus copias.
- Directorio `.terraform`.

Se publican las plantillas `.example` y `.terraform.lock.hcl`.

El estado puede contener contraseñas aunque estén marcadas como sensibles.

La API Access List controla el acceso administrativo de Terraform. La lista del proyecto controla el acceso al clúster.

Los rangos de Render son compartidos. La autenticación de base de datos requiere además credenciales válidas.

## Límites de seguridad y alojamiento

La API no implementa autenticación ni autorización de clientes HTTP.

Las credenciales de Atlas protegen la base, pero no restringen quién puede invocar los endpoints públicos.

El servicio gratuito puede suspenderse por inactividad. La primera solicitud puede tardar mientras arranca.

El entorno está orientado a demostración y evaluación. Las cuotas y condiciones vigentes dependen de cada proveedor.

## Mensajes del entorno de pruebas

Se observaron advertencias de Mockito sobre carga dinámica del agente en Java 21.

En macOS se observó un mensaje de Netty sobre su biblioteca DNS nativa, utilizando la alternativa del sistema.

La verificación completa terminó correctamente con esos mensajes presentes.

## Documentación

El README incluye configuración, ejecución local, Docker, endpoints, Terraform, Render, arquitectura, limitaciones y pruebas.

Este documento registra las decisiones y el alcance de la verificación.