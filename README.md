# StockFlow API

API REST para gestionar inventario y ventas, desarrollada como proyecto personal de aprendizaje y portfolio.

## Tecnologías

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA y Hibernate
- Bean Validation
- PostgreSQL
- Maven

## Requisitos

- Java 21
- PostgreSQL en ejecución
- Una base de datos llamada `stockflow`
- Un usuario de PostgreSQL con acceso a esa base

## Configuración local

La aplicación obtiene la conexión mediante variables de entorno:

| Variable | Obligatoria | Valor predeterminado |
| --- | --- | --- |
| `DB_PASSWORD` | Sí | Sin valor |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/stockflow` |
| `DB_USERNAME` | No | `StockFlowApp` |

En PowerShell, definí la contraseña solamente para la terminal actual y ejecutá la aplicación:

```powershell
$env:DB_PASSWORD="tu-contraseña-local"
.\mvnw.cmd spring-boot:run
```

No guardes contraseñas reales en archivos versionados.

## Arquitectura de Productos

Cada petición atraviesa capas con responsabilidades diferentes:

```text
Cliente HTTP / Postman
        ↓
ProductController     recibe y valida JSON
        ↓
ProductService        aplica reglas y transacciones
        ↓
ProductRepository     ejecuta operaciones de persistencia
        ↓
PostgreSQL            almacena la tabla products
```

La API utiliza DTO para que el contrato HTTP no exponga directamente la entidad JPA.

## Endpoints de Productos

| Método | Ruta | Descripción | Respuesta correcta |
| --- | --- | --- | --- |
| `POST` | `/api/products` | Crea un producto activo. | `201 Created` |
| `GET` | `/api/products` | Lista activos e inactivos por nombre. | `200 OK` |
| `GET` | `/api/products/{id}` | Busca un producto por ID. | `200 OK` |
| `PUT` | `/api/products/{id}` | Actualiza nombre, SKU, precio y stock. | `200 OK` |
| `DELETE` | `/api/products/{id}` | Desactiva sin borrar la fila. | `204 No Content` |

El SKU se recorta y convierte a mayúsculas antes de guardarse. Dos SKU que sólo difieren por mayúsculas se consideran duplicados.

### Crear o actualizar

```json
{
  "name": "Mechanical Keyboard",
  "sku": "key-001",
  "price": 129.90,
  "stock": 10
}
```

Una creación correcta devuelve el producto y una cabecera `Location`:

```json
{
  "id": 1,
  "name": "Mechanical Keyboard",
  "sku": "KEY-001",
  "price": 129.90,
  "stock": 10,
  "active": true
}
```

### Errores

La API utiliza la misma estructura para errores de validación (`400`), productos inexistentes (`404`) y SKU duplicados (`409`):

```json
{
  "timestamp": "2026-08-11T18:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La petición contiene valores inválidos",
  "path": "/api/products",
  "fieldErrors": {
    "name": "must not be blank"
  }
}
```

## Pruebas con Postman

1. Iniciá la aplicación con `DB_PASSWORD` definida.
2. Creá una petición `POST` a `http://localhost:8080/api/products`.
3. Seleccioná **Body → raw → JSON** y pegá el ejemplo de creación.
4. Copiá el `id` de la respuesta y consultá `GET http://localhost:8080/api/products/{id}`.
5. Probá `PUT` con nuevos datos y `DELETE` para desactivarlo.

## Pruebas automatizadas

Con las variables de base configuradas, ejecutá:

```powershell
.\mvnw.cmd verify
```

Las pruebas cubren validaciones, consultas JPA, reglas del servicio, contrato HTTP y un recorrido completo hasta PostgreSQL. Los datos creados por las pruebas transaccionales se revierten automáticamente.

## Estado

El módulo de Productos ofrece su primer CRUD REST. Las próximas etapas incorporarán movimientos de stock y ventas.
