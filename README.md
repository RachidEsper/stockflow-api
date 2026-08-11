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

## Estado

El proyecto cuenta con la configuración base de Spring Boot y PostgreSQL. El primer módulo funcional será la gestión de productos.
