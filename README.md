# StockFlow API

API REST creada como proyecto de portfolio para administrar productos, inventario y clientes. StockFlow permite mantener el catálogo, controlar entradas y salidas de stock sin permitir existencias negativas y gestionar clientes con emails únicos.

**Versión actual:** `1.1.0`

**Estado:** funcional y lista para demostración local.

## Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA e Hibernate
- Bean Validation
- PostgreSQL
- Maven

## Funcionalidades

### Productos e inventario

- Crear, listar, consultar y actualizar productos.
- Desactivar productos mediante borrado lógico.
- Aumentar y disminuir stock.
- Rechazar cantidades inválidas y stock insuficiente.
- Mantener SKU únicos normalizados en mayúsculas.

Los productos contienen `id`, `name`, `description` opcional, `sku`, `price`, `stock` y `active`.

### Clientes

- Crear, listar, consultar y actualizar clientes.
- Desactivar clientes mediante borrado lógico.
- Validar nombres obligatorios y formato de email.
- Mantener emails únicos sin distinguir mayúsculas y minúsculas.
- Normalizar espacios en nombres y emails en minúsculas.

Los clientes contienen `id`, `firstName`, `lastName`, `email` y `active`. Al desactivar un cliente su fila permanece en PostgreSQL con `active=false`.

### Contrato HTTP

- Validación centralizada de cuerpos JSON.
- Respuestas de error uniformes.
- `400 Bad Request` para datos o JSON inválidos.
- `404 Not Found` para identificadores inexistentes.
- `409 Conflict` para SKU o email duplicados y stock insuficiente.

La versión actual no incluye autenticación, ventas ni frontend.

## Arquitectura

```text
Cliente HTTP
    -> Controller
    -> Service
    -> Repository
    -> Hibernate / JDBC
    -> PostgreSQL
```

Los Controllers gestionan HTTP y validan los DTO de entrada. Los Services concentran reglas de negocio y transacciones. Los Repositories usan Spring Data JPA para persistir entidades. Los DTO de respuesta evitan exponer directamente las entidades JPA y `GlobalExceptionHandler` transforma excepciones de negocio en errores HTTP uniformes.

## Requisitos

- JDK 21
- PostgreSQL en ejecución en el puerto `5432`
- Maven Wrapper incluido en el repositorio

## Preparar PostgreSQL

Desde pgAdmin o `psql`, conectado como un usuario administrador, crea primero el rol de la aplicación y después la base:

```sql
CREATE ROLE stockflow_app
WITH LOGIN
PASSWORD 'tu-contraseña-local';

CREATE DATABASE stockflow
OWNER stockflow_app;
```

Si la base ya existe, se puede asignar el propietario con:

```sql
ALTER DATABASE stockflow OWNER TO stockflow_app;
```

Comprueba las credenciales desde una terminal:

```bash
psql -h 127.0.0.1 -p 5432 -U stockflow_app -d stockflow -W
```

Hibernate crea y actualiza las tablas locales mediante `spring.jpa.hibernate.ddl-auto=update` al iniciar la aplicación.

## Variables de entorno

| Variable | Obligatoria | Valor predeterminado |
| --- | --- | --- |
| `DB_PASSWORD` | Sí | Sin valor |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/stockflow` |
| `DB_USERNAME` | No | `stockflow_app` |

El repositorio incluye `.env.example` como referencia. Copia sus nombres a un `.env` local y reemplaza la contraseña, pero no versiones ese archivo. Spring Boot no carga `.env` automáticamente.

En Linux o macOS:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/stockflow"
export DB_USERNAME="stockflow_app"
export DB_PASSWORD="tu-contraseña-local"
./mvnw spring-boot:run
```

En PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/stockflow"
$env:DB_USERNAME="stockflow_app"
$env:DB_PASSWORD="tu-contraseña-local"
.\mvnw.cmd spring-boot:run
```

En IntelliJ IDEA agrega las mismas variables en **Run > Edit Configurations > Environment variables**. Las contraseñas reales no deben guardarse en `application.properties` ni enviarse a Git.

## Endpoints de productos

| Método | Ruta | Descripción | Estado exitoso |
| --- | --- | --- | --- |
| `POST` | `/api/products` | Crear producto | `201 Created` |
| `GET` | `/api/products` | Listar productos | `200 OK` |
| `GET` | `/api/products/{id}` | Consultar por ID | `200 OK` |
| `PUT` | `/api/products/{id}` | Actualizar producto | `200 OK` |
| `DELETE` | `/api/products/{id}` | Desactivar producto | `204 No Content` |
| `PATCH` | `/api/products/{id}/stock/increase` | Aumentar stock | `200 OK` |
| `PATCH` | `/api/products/{id}/stock/decrease` | Disminuir stock | `200 OK` |

Crear o actualizar un producto:

```json
{
  "name": "Mechanical Keyboard",
  "description": "Teclado mecánico compacto",
  "sku": "key-001",
  "price": 129.90,
  "stock": 10
}
```

Mover stock:

```json
{
  "quantity": 5
}
```

## Endpoints de clientes

| Método | Ruta | Descripción | Estado exitoso |
| --- | --- | --- | --- |
| `POST` | `/api/customers` | Crear cliente | `201 Created` |
| `GET` | `/api/customers` | Listar clientes | `200 OK` |
| `GET` | `/api/customers/{id}` | Consultar por ID | `200 OK` |
| `PUT` | `/api/customers/{id}` | Actualizar cliente | `200 OK` |
| `DELETE` | `/api/customers/{id}` | Desactivar cliente | `204 No Content` |

Crear o actualizar un cliente:

```json
{
  "firstName": "Ada",
  "lastName": "Lovelace",
  "email": "ada@example.com"
}
```

Una creación exitosa devuelve el cliente, la cabecera `Location` y estado `201 Created`. El listado incluye clientes activos e inactivos ordenados por apellido y nombre.

## Tests

La suite incluye validaciones, tests unitarios de Service, contrato MVC, Repository contra PostgreSQL y recorridos de integración desde HTTP hasta la base.

Con PostgreSQL y las variables de entorno configuradas:

```bash
./mvnw verify
```

En Windows:

```powershell
.\mvnw.cmd verify
```
