# STG Inventory Control System

Sistema web centralizado de **gestión y control de inventarios** para la empresa **STG Technology & Logistics S.A.C**

## 📋 Descripción

Aplicación desarrollada en **Spring Boot** y **PostgreSQL** que optimiza el proceso de control de inventarios, asegurando:

✅ **Trazabilidad completa** de entradas y salidas de mercadería  
✅ **Registro inalterable** en Kardex para auditoría  
✅ **Precisión del stock** en tiempo real  
✅ **Alertas automáticas** de reabastecimiento  
✅ **Gestión de usuarios** con roles específicos (Admin, Almacenero, Vendedor)  

---

## 🎯 Requisitos Funcionales (15 RF + 8 RNF)

### Funcionalidades Clave

| Módulo | Características |
|--------|-----------------|
| **🔐 Seguridad** | Login con BCrypt • Bloqueo por 3 intentos fallidos • Control de acceso por roles |
| **📦 Inventario** | Registro de productos con SKU único • Categorías y marcas • Estados activo/inactivo |
| **📥 Operaciones** | Entradas de mercadería • Salidas validadas • Registro automático en Kardex |
| **📊 Reportes** | Dashboard ejecutivo • Historial de movimientos • Alertas de stock bajo |

---

## 🛠️ Stack Tecnológico

- **Backend:** Spring Boot 3.x, Spring Security
- **Base de Datos:** PostgreSQL 14+
- **Frontend:** Thymeleaf, Bootstrap 5, JavaScript
- **Build:** Maven
- **Patrones:** MVC, Repository Pattern, Event Listeners

---

## 📦 Instalación

### Prerequisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Git

### Pasos

1. **Clonar el repositorio**
```bash
git clone https://github.com/swiftdeskk/stg-inventory-control.git
cd stg-inventory-control
```

2. **Configurar PostgreSQL**
```sql
CREATE DATABASE techstore_db;
CREATE USER admin_techstore WITH PASSWORD 'password123';
GRANT ALL PRIVILEGES ON DATABASE techstore_db TO admin_techstore;
```

3. **Configurar aplicación** (`src/main/resources/application.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/techstore_db
spring.datasource.username=admin_techstore
spring.datasource.password=password123
```

4. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Acceder a la aplicación**
```
http://localhost:8080
```

---

## 👥 Usuarios de Prueba

| Usuario | Contraseña | Rol | Acceso |
|---------|-----------|-----|--------|
| admin | admin123 | ADMIN | Todo el sistema |
| almacenero | almacen123 | ALMACENERO | Inventario, entradas |
| vendedor | venta123 | VENDEDOR | Salidas de inventario |

---

## 📂 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/techstore/tech_store_project/
│   │   ├── controller/        # Controladores (RF-01 al RF-15)
│   │   ├── model/             # Entidades JPA
│   │   ├── respository/       # Acceso a datos
│   │   ├── service/           # Lógica de negocio
│   │   └── config/            # Configuraciones (Security, etc)
│   └── resources/
│       ├── templates/         # Vistas Thymeleaf
│       ├── static/            # CSS, JS, imágenes
│       └── application.properties
└── test/
    └── java/                  # Tests unitarios
```

---

## 🔑 Características Destacadas

### Seguridad (RNF-02, RNF-06)
- ✅ Contraseñas cifradas con **BCrypt**
- ✅ Bloqueo de cuenta tras **3 intentos fallidos**
- ✅ Validación de credenciales contra BD
- ✅ Control de acceso por roles

### Rendimiento (RNF-01, RNF-07)
- ✅ Búsqueda en catálogo **< 2 segundos**
- ✅ Soporte para **50+ conexiones concurrentes**
- ✅ Filtros avanzados por SKU, categoría y estado

### Kardex (RF-12, RF-13)
- ✅ Registro **inalterable** de movimientos
- ✅ Trazabilidad: fecha, hora, usuario, cantidad
- ✅ Consulta con filtros por rango de fechas
- ✅ Historial completo de transacciones

---

## 📈 Roadmap

- [ ] Exportación a PDF/Excel de reportes
- [ ] Autenticación OAuth2
- [ ] API REST para integraciones
- [ ] App móvil (React Native)
- [ ] Dashboard analytics avanzado

---

## 👨‍💻 Autores

**Equipo de Desarrollo:**
- Quispe Sánchez Juan André
- Mauricio Lopez Sebastian Alessandro
- Salazar Bustamante Angelo Gianpiero
- Cerna Martinez Arian
- Sullcapuma Bustamante Jaren John

---

## 📄 Licencia

Proyecto académico - UTP 2026

---

## 📞 Soporte

Para reportar issues o sugerencias: [GitHub Issues](https://github.com/swiftdeskk/stg-inventory-control/issues)

---

**Última actualización:** Junio 2026
