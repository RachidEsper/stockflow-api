# StockFlow API

API REST de inventario básico creada como proyecto de portfolio. StockFlow permite registrar productos, mantener sus datos y controlar entradas y salidas de stock sin permitir existencias negativas.

**Versión actual:** `1.0.0`

**Estado:** funcional y lista para demostración como proyecto de portfolio.

## Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA e Hibernate
- Bean Validation
- PostgreSQL
- Maven

## Funcionalidades v1

- Crear, listar, consultar y actualizar productos.
- Desactivar productos mediante borrado lógico.
- Aumentar y disminuir stock.
- Rechazar cantidades inválidas y stock insuficiente.
- Validar el contrato HTTP y devolver errores uniformes.
- Mantener SKU únicos normalizados en mayúsculas.

Los productos contienen `id`, `name`, `description` opcional, `sku`, `price`, `stock` y `active`.

La versión 1.0.0 está enfocada exclusivamente en productos e inventario. No incluye autenticación, ventas, clientes ni frontend.

## Arquitectura

```text
Cliente HTTP -> ProductController -> ProductService -> ProductRepository -> PostgreSQL
```

El Controller gestiona HTTP y validación de entrada; el Service concentra las reglas de negocio y transacciones; el Repository usa Spring Data JPA para persistir entidades. Los DTO evitan exponer directamente la entidad JPA.

## Requisitos y PostgreSQL

- JDK 21
- PostgreSQL en ejecución
- Base de datos `stockflow`
- Usuario con acceso a esa base

La conexión se configura mediante variables de entorno:

| Variable | Obligatoria | Predeterminado |
| --- | --- | --- |
| `DB_PASSWORD` | Sí | Sin valor |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/stockflow` |
| `DB_USERNAME` | No | `StockFlowApp` |

Ejemplo mínimo de preparación ejecutado con un usuario administrador de PostgreSQL:

```sql
CREATE DATABASE stockflow;
CREATE USER "StockFlowApp" WITH PASSWORD 'tu-contraseña';
GRANT ALL PRIVILEGES ON DATABASE stockflow TO "StockFlowApp";
```

El usuario también debe poder crear y modificar objetos en el esquema usado por la aplicación. En un entorno local con PostgreSQL moderno puede ser necesario:

```sql
\c stockflow
GRANT USAGE, CREATE ON SCHEMA public TO "StockFlowApp";
```

No guardes contraseñas reales en archivos versionados. En PowerShell:

```powershell
$env:DB_PASSWORD="tu-contraseña"
$env:DB_USERNAME="StockFlowApp"
$env:DB_URL="jdbc:postgresql://localhost:5432/stockflow"
.\mvnw.cmd spring-boot:run
```

En IntelliJ IDEA, agrega las mismas variables en **Run > Edit Configurations > Environment variables**.

## Endpoints

| Método | Ruta | Descripción | Estado exitoso |
| --- | --- | --- | --- |
| `POST` | `/api/products` | Crear producto | `201 Created` |
| `GET` | `/api/products` | Listar productos | `200 OK` |
| `GET` | `/api/products/{id}` | Consultar por ID | `200 OK` |
| `PUT` | `/api/products/{id}` | Actualizar producto | `200 OK` |
| `DELETE` | `/api/products/{id}` | Desactivar producto | `204 No Content` |
| `PATCH` | `/api/products/{id}/stock/increase` | Aumentar stock | `200 OK` |
| `PATCH` | `/api/products/{id}/stock/decrease` | Disminuir stock | `200 OK` |

Crear o actualizar:

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

Las validaciones devuelven `400 Bad Request`, un ID inexistente devuelve `404 Not Found` y un SKU duplicado o una salida con stock insuficiente devuelve `409 Conflict`.

## Tests

Con PostgreSQL y las variables de entorno configuradas:

```powershell
.\mvnw.cmd verify
```

La suite incluye tests unitarios del Service, validaciones, contrato MVC y pruebas de integración contra PostgreSQL.
