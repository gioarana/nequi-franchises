# API de franquicias — Prueba técnica Nequi

API REST para administrar franquicias, sucursales y productos con stock.

Desarrollada con Java 21, Spring Boot 4.1.1, Spring WebFlux y Spring Data Reactive MongoDB. La persistencia está alojada en MongoDB Atlas y la API se despliega en Render mediante Docker.

## API desplegada

URL base:

https://nequi-franchises.onrender.com

Información de la API:

https://nequi-franchises.onrender.com/api/v1

Estado de salud:

https://nequi-franchises.onrender.com/actuator/health

El servicio utiliza el plan gratuito de Render. Después de un período de inactividad puede suspenderse; la primera solicitud puede tardar mientras vuelve a iniciar.

## Funcionalidades

- Crear franquicias.
- Agregar sucursales a una franquicia.
- Agregar productos a una sucursal.
- Eliminar productos.
- Reemplazar el stock de un producto.
- Consultar el producto con mayor stock de cada sucursal de una franquicia.
- Actualizar nombres de franquicias, sucursales y productos.

La infraestructura de MongoDB Atlas se administra mediante Terraform.

## Requisitos para ejecutar con Docker

- Docker instalado y con su motor en ejecución.
- Un clúster de MongoDB Atlas accesible desde la dirección IP pública donde se esté ejecutando el proyecto.
- Un usuario con permiso de lectura y escritura sobre `nequi_franchises`.

La carpeta `infra/mongodb` contiene la configuración de Terraform para aprovisionar la persistencia.

## Configuración local

Desde la raíz del proyecto, copiar la plantilla:

```sh
cp .env.example .env
```

Editar `.env` y reemplazar los valores de ejemplo:

```dotenv
MONGODB_HOST=tu-cluster.mongodb.net
MONGODB_USERNAME=nequi-api
MONGODB_PASSWORD=REEMPLAZAR_CON_TU_CONTRASEÑA
```

`MONGODB_HOST` debe contener únicamente el host de Atlas, sin protocolo, credenciales ni ruta.

Autorizar la IP pública donde se está ejecutando el proyecto en la lista de acceso de red del proyecto de Atlas.

El archivo `.env` contiene credenciales y está excluido de Git. No debe publicarse.

## Construir y ejecutar con Docker

Desde la raíz del proyecto:

```sh
docker build -t nequi-franchises:local .
```

Ejecutar el contenedor:

```sh
docker run --rm \
  --name nequi-franchises-local \
  -p 8081:8080 \
  --env-file .env \
  nequi-franchises:local
```

La API estará disponible en:

http://localhost:8081

Desde otra terminal, comprobar su funcionamiento:

```sh
curl -i http://localhost:8081/api/v1
curl -i http://localhost:8081/actuator/health
```

Para detener el contenedor, pulsar Control + C en la terminal donde está ejecutándose.

Los datos permanecen en MongoDB Atlas después de detener o eliminar el contenedor.

La construcción de la imagen omite la ejecución de pruebas. La verificación automatizada se ejecuta por separado.

## Ejecutar desde IntelliJ IDEA

Requisitos:

- JDK 21.
- IntelliJ IDEA con soporte para Java y Maven.

Abrir la carpeta del proyecto y completar la importación de Maven.

Configurar el SDK del proyecto y el JDK de ejecución con Java 21.

En la configuración de ejecución de `NequiFranchisesApplication`, agregar las siguientes variables de entorno:

| Variable | Descripción |
|---|---|
| `MONGODB_HOST` | Host del clúster de MongoDB Atlas |
| `MONGODB_USERNAME` | Usuario de base de datos |
| `MONGODB_PASSWORD` | Contraseña del usuario |

El archivo `.env` no se carga automáticamente al ejecutar desde IntelliJ. Las variables deben configurarse en la configuración de ejecución.

Ejecutar el método `main` de `NequiFranchisesApplication`.

La API estará disponible en:

http://localhost:8080

Comprobar su funcionamiento:

```sh
curl -i http://localhost:8080/api/v1
curl -i http://localhost:8080/actuator/health
```

La aplicación utiliza el puerto indicado por la variable `PORT`, si existe; en caso contrario, utiliza `8080`.

Las credenciales deben permanecer en la configuración local de ejecución y no deben guardarse en archivos compartidos del proyecto.

## Endpoints

Todas las rutas comienzan con `/api/v1`.

Los parámetros `franchiseId`, `branchId` y `productId` son UUID obtenidos en las respuestas de creación.

| Método | Ruta | Descripción | Respuesta exitosa |
|---|---|---|---|
| POST | `/franchises` | Crear franquicia | `201 Created` |
| POST | `/franchises/{franchiseId}/branches` | Agregar sucursal | `201 Created` |
| POST | `/franchises/{franchiseId}/branches/{branchId}/products` | Agregar producto | `201 Created` |
| DELETE | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar producto | `204 No Content` |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Reemplazar stock | `200 OK` |
| GET | `/franchises/{franchiseId}/products/max-stock` | Consultar máximo por sucursal | `200 OK` |
| PATCH | `/franchises/{franchiseId}/name` | Renombrar franquicia | `200 OK` |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/name` | Renombrar sucursal | `200 OK` |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Renombrar producto | `200 OK` |

### Crear franquicia

```sh
curl -i -X POST http://localhost:8080/api/v1/franchises \
  -H 'Content-Type: application/json' \
  -d '{"name":"Café Andino"}'
```

Respuesta de ejemplo:

```json
{
  "id": "00000000-0000-0000-0000-000000000001",
  "name": "Café Andino"
}
```

El identificador del ejemplo es ilustrativo. La aplicación genera un UUID nuevo.

### Crear sucursal

Cuerpo:

```json
{
  "name": "Chapinero"
}
```

Respuesta:

```json
{
  "id": "00000000-0000-0000-0000-000000000002",
  "franchiseId": "00000000-0000-0000-0000-000000000001",
  "name": "Chapinero"
}
```

### Crear producto

Cuerpo:

```json
{
  "name": "Café",
  "stock": 20
}
```

Respuesta:

```json
{
  "id": "00000000-0000-0000-0000-000000000003",
  "branchId": "00000000-0000-0000-0000-000000000002",
  "name": "Café",
  "stock": 20
}
```

### Modificar stock

Cuerpo:

```json
{
  "stock": 35
}
```

La cantidad reemplaza el stock anterior; no representa un incremento. La respuesta contiene el producto actualizado.

### Actualizar nombres

Los tres endpoints de cambio de nombre reciben:

```json
{
  "name": "Nuevo nombre"
}
```

Devuelven la entidad actualizada conservando su identificador. Renombrar una franquicia o sucursal conserva su contenido; renombrar un producto conserva su stock.

### Eliminar producto

La solicitud no lleva cuerpo. Una eliminación exitosa devuelve `204` sin contenido.

Repetir la eliminación del mismo producto devuelve `404`.

### Consultar máximos por sucursal

Respuesta de ejemplo:

```json
[
  {
    "branchId": "00000000-0000-0000-0000-000000000002",
    "branchName": "Chapinero",
    "productId": "00000000-0000-0000-0000-000000000003",
    "productName": "Café",
    "stock": 35
  }
]
```

### Reglas del contrato

- Los nombres son obligatorios y deben tener entre 1 y 100 caracteres después de quitar espacios de los extremos.
- Los nombres pueden repetirse; los recursos se identifican mediante UUID.
- El stock es obligatorio al crear un producto y debe ser un entero entre 0 y 2147483647.
- Se valida la pertenencia del producto a la sucursal y de la sucursal a la franquicia indicada.
- La consulta de máximos devuelve un producto por sucursal con inventario.
- Las sucursales sin productos se omiten.
- El stock cero participa en la consulta de máximos.
- En empates, se selecciona el producto cuyo UUID sea menor en orden lexicográfico.
- Los resultados se ordenan por el UUID de la sucursal en orden lexicográfico.
- Una franquicia existente sin productos devuelve `200` con `[]`.
- Una franquicia inexistente devuelve `404`.
- Las respuestas de creación incluyen la cabecera `Location`. No se implementan endpoints de consulta individual en esas rutas.

## Aprovisionar MongoDB Atlas con Terraform

### Requisitos

- Terraform compatible con la versión declarada en `infra/mongodb/versions.tf`.
- Una cuenta y una organización en MongoDB Atlas.
- Una cuenta de servicio con permiso para crear proyectos y administrar el proyecto creado.
- La IP pública desde la que se ejecuta Terraform autorizada en la API Access List de la cuenta de servicio.

Terraform administra:

- Proyecto de Atlas.
- Clúster gratuito M0 en AWS, región US_EAST_1.
- Usuario de base de datos con permiso `readWrite` sobre `nequi_franchises`.
- Acceso de red desde la IP local configurada.
- Acceso de red desde los rangos de salida de Render configurados.

### Configurar las variables

Desde la raíz del proyecto:

```sh
cd infra/mongodb
cp terraform.tfvars.example terraform.tfvars
```

Editar `terraform.tfvars` y reemplazar los valores de ejemplo.

Si todavía no existe un servicio en Render, utilizar:

```hcl
render_outbound_cidrs = []
```

Después de crear el servicio, obtener sus rangos en `Connect → Outbound`, agregarlos a esta variable y aplicar nuevamente Terraform.

### Autenticación

El proveedor utiliza estas variables de entorno:

- `MONGODB_ATLAS_CLIENT_ID`
- `MONGODB_ATLAS_CLIENT_SECRET`

La contraseña del usuario de base de datos se proporciona mediante:

- `TF_VAR_database_password`

Ejemplo de carga interactiva para zsh en macOS, ejecutando cada comando por separado:

```sh
read -r "MONGODB_ATLAS_CLIENT_ID?Client ID: "
export MONGODB_ATLAS_CLIENT_ID
```

```sh
read -rs "MONGODB_ATLAS_CLIENT_SECRET?Client Secret: "
echo
export MONGODB_ATLAS_CLIENT_SECRET
```

```sh
read -rs "TF_VAR_database_password?Contraseña de base de datos: "
echo
export TF_VAR_database_password
```

Introducir los valores cuando cada comando lo solicite. Los secretos no se muestran al escribirlos.

Las variables permanecen disponibles en esa sesión de terminal. En ejecuciones posteriores debe utilizarse la misma contraseña de base de datos, salvo que se pretenda cambiarla.

Las credenciales administrativas de Terraform son diferentes de las credenciales que utiliza la API para acceder a los datos.

### Crear la infraestructura

```sh
terraform init
terraform fmt
terraform validate
terraform plan
terraform apply
```

Revisar el plan antes de confirmar `apply`.

El nivel gratuito se configura mediante `instance_size = "M0"`. Cambiar el nivel del clúster puede generar costos.

Al crear toda la infraestructura desde cero:

- Sin rangos de Render: se esperan 4 recursos.
- Con los dos rangos configurados: se esperan 6 recursos.

### Archivos locales y estado

No publicar:

- `terraform.tfvars`.
- Archivos `terraform.tfstate` y sus copias.
- La carpeta `.terraform`.
- Credenciales o planes guardados que puedan contener información sensible.

La contraseña marcada como `sensitive` se oculta en la salida habitual, pero puede almacenarse en el estado de Terraform.

Conservar el estado local para administrar los recursos creados. El repositorio excluye estos archivos mediante `.gitignore`.

El archivo `.terraform.lock.hcl` sí debe mantenerse en Git para registrar la versión seleccionada del proveedor.

### Acceso de red

La API Access List de la cuenta de servicio controla el acceso de Terraform a la API administrativa de Atlas.

Las entradas `mongodbatlas_project_ip_access_list` controlan el acceso de la aplicación al clúster.

Si cambia la IP pública local, actualizar `local_ip` y aplicar Terraform. Si cambian los rangos de salida de Render, actualizar `render_outbound_cidrs`.

La base `nequi_franchises` y la colección `franchises` se crean al insertar el primer documento; no son recursos aprovisionados directamente por esta configuración.

## Desplegar la API en Render

### Preparación

- Publicar el proyecto en GitHub, incluyendo `Dockerfile` y `.dockerignore`.
- Aprovisionar MongoDB Atlas mediante Terraform.
- Obtener el host del clúster y las credenciales del usuario de base de datos.

### Crear el servicio

En Render, seleccionar `New → Web Service` y conectar el repositorio.

Configurar:

| Campo | Valor |
|---|---|
| Language | Docker |
| Branch | master |
| Root Directory | Vacío |
| Compute | Free |
| Dockerfile Path | ./Dockerfile |
| Docker Build Context | . |
| Docker Command | Vacío |
| Health Check Path | /actuator/health |

La región utilizada en esta solución es Ohio (US East).

Render construye la imagen desde el Dockerfile del repositorio y ejecuta su `ENTRYPOINT`.

### Variables de entorno

Configurar en el servicio:

| Variable | Valor |
|---|---|
| `MONGODB_HOST` | Host del clúster de Atlas |
| `MONGODB_USERNAME` | Usuario de base de datos |
| `MONGODB_PASSWORD` | Contraseña del usuario |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=60.0` |

Render proporciona `PORT`. La aplicación utiliza ese valor mediante:

```properties
server.port=${PORT:8080}
```

`JAVA_TOOL_OPTIONS` limita el máximo del heap de Java al 60 % de la memoria disponible. No limita toda la memoria del proceso.

No configurar las credenciales administrativas de Terraform en el servicio.

### Autorizar la conexión a Atlas

Después de crear el servicio:

1. Consultar todos los rangos de salida en `Connect → Outbound`.
2. Agregarlos a `render_outbound_cidrs` en el archivo local `terraform.tfvars`.
3. Ejecutar `terraform plan` y revisar los cambios.
4. Ejecutar `terraform apply`.

La comprobación de salud puede fallar mientras Atlas no permita la conexión. Si el despliegue falla por ese motivo, iniciarlo nuevamente después de aplicar las reglas de red.

### Verificar el despliegue

```sh
curl -i https://nequi-franchises.onrender.com/api/v1
curl -i https://nequi-franchises.onrender.com/actuator/health
```

Ambas solicitudes deben responder `200`. El estado de salud debe indicar `UP`.

Verificar además una operación de negocio para comprobar el acceso a los datos.

La dirección anterior corresponde al servicio desplegado para esta entrega. En un despliegue independiente debe utilizarse la URL asignada por Render.

### Límites del entorno gratuito

El servicio puede suspenderse por inactividad y tardar en responder mientras vuelve a iniciar.

La API utiliza persistencia externa en Atlas; detener o reemplazar el contenedor no elimina los datos.

Las cuotas y condiciones de los planes gratuitos deben consultarse en la documentación de cada proveedor.

## Arquitectura

La aplicación separa el dominio, los casos de uso y los adaptadores mediante puertos e interfaces.

```text
Controladores HTTP
        ↓
Servicios de aplicación
        ↓
FranchiseRepository — puerto de salida
        ↓
MongoFranchiseRepositoryAdapter
        ↓
SpringDataFranchiseRepository
        ↓
MongoDB Atlas
```

### Organización del código

| Paquete | Responsabilidad |
|---|---|
| `domain.model` | Modelos inmutables y validaciones del dominio |
| `domain.exception` | Excepciones de negocio |
| `application.service` | Casos de uso |
| `application.port.out` | Contrato de persistencia |
| `application.model` | Resultados de los casos de uso |
| `adapter.in.web` | Controladores y manejo de errores HTTP |
| `adapter.in.web.dto` | Solicitudes y respuestas de la API |
| `adapter.out.persistence` | Adaptador MongoDB |
| `adapter.out.persistence.document` | Documento de persistencia |
| `adapter.out.persistence.repository` | Repositorio reactivo de Spring Data |
| `configuration` | Registro de los servicios en Spring |

### Modelo de persistencia

Cada franquicia se almacena en un documento de la colección `franchises`, con sus sucursales y productos embebidos.

Este modelo permite leer la jerarquía completa mediante una consulta y guardar cada modificación dentro de un documento.

Los modelos de dominio son records inmutables. Las modificaciones construyen nuevas instancias y conservan los datos no afectados.

El documento MongoDB se mantiene separado del modelo de dominio e incluye una versión administrada por Spring Data.

### Concurrencia

Las actualizaciones utilizan control optimista mediante `@Version`.

El adaptador lee el documento, aplica la transformación y lo guarda conservando la versión leída. Si otra escritura modificó esa versión, Spring Data rechaza el guardado y la API devuelve `409 Conflict`.

No se realizan reintentos automáticos de las operaciones de negocio.

### Programación reactiva y funcional

Los controladores y servicios utilizan `Mono` de Project Reactor. El acceso a MongoDB utiliza el driver reactivo mediante Spring Data.

WebFlux realiza la suscripción de las solicitudes HTTP. El flujo de la aplicación no utiliza `block()` ni suscripciones manuales.

Las transformaciones de las listas utilizan Streams de Java. Estas operaciones son síncronas en memoria; no realizan llamadas adicionales a la base de datos.

### Consulta de máximos

La consulta lee una franquicia y calcula un máximo por cada sucursal con productos.

El recorrido de productos es lineal respecto al total de productos. El ordenamiento final de las sucursales resultantes tiene un costo adicional de O(B log B), donde B es la cantidad de sucursales con productos.

### Límites y decisiones

- Las identidades utilizan UUID para permitir nombres repetidos y cambios de nombre.
- El documento embebido favorece la simplicidad para el alcance de esta prueba.
- El crecimiento del documento está sujeto al límite de tamaño de documentos de MongoDB.
- Las escrituras sobre sucursales diferentes de una misma franquicia pueden entrar en conflicto, porque comparten la versión del documento.
- Para inventarios grandes o alta concurrencia convendría evaluar colecciones separadas y operaciones más específicas.
- La API no implementa autenticación ni autorización de clientes HTTP. Las credenciales de Atlas protegen el acceso a la base de datos, pero no restringen quién puede invocar los endpoints públicos.
- El despliegue gratuito está orientado a demostración y evaluación.

## Pruebas automatizadas

Requisitos:

- JDK 21.
- Docker instalado y con su motor en ejecución.
- Acceso a Internet en la primera ejecución para descargar dependencias e imágenes.

Desde la raíz del proyecto:

```sh
./mvnw test
```

En Windows:

```powershell
.\mvnw.cmd test
```

La suite contiene 77 pruebas:

- 23 pruebas de modelos de dominio.
- 50 pruebas de servicios de aplicación.
- 1 prueba de arranque del contexto de Spring con componentes MongoDB simulados.
- 3 pruebas de integración con MongoDB temporal mediante Testcontainers.

Las pruebas de integración verifican:

- Persistencia y recuperación de la jerarquía completa.
- Actualización con conservación del inventario e incremento de versión.
- Rechazo de una escritura con versión desactualizada, sin sobrescribir el cambio anterior.

Testcontainers administra una base temporal en Docker. Estas pruebas no requieren credenciales de Atlas ni modifican sus datos.

Para ejecutar únicamente las pruebas de persistencia:

```sh
./mvnw -Dtest=MongoPersistenceTest test
```

Las pruebas verifican el conflicto de versión en persistencia. La traducción HTTP a `409` no está cubierta por estas tres pruebas.

Los endpoints y la conexión a Atlas se han comprobado manualmente desde la ejecución local, Docker y Render.

Mockito puede emitir advertencias sobre la carga dinámica de su agente en Java 21. Estas advertencias no impiden la ejecución de las pruebas.