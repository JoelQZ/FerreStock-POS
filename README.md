# FerreStock · POS

Parte de **usuario** de FerreStock: el **punto de venta (POS) del cajero**. Registra ventas con IGV 18 % incluido en el precio, pagos con Efectivo, Yape y Plin, boletas y facturas, turnos y cierre de caja.

Proyecto Integrador · 4.º ciclo · **Tecsup** · 2026-2 · Grupo 3C (C24)

| Integrante | Rol Scrum |
|---|---|
| Juan Diego Ramos Enriquez | Product Owner |
| Manuel Gerardo Salazar Ambrosio | Scrum Master |
| Joel Facundo Quijada Zevallos | Equipo de desarrollo |

Repositorios del sistema: `FerreStock-Admin` (Django + React admin) · **`FerreStock-POS`** (este) · `FerreStock-Movil` (Kotlin).

---

## 1. Arquitectura

```
 ADMINISTRACIÓN
 React admin (5173) ── REST + JWT ──► Django (8000) ─────────┐
                                       (dueño del esquema)   │
                                                             ├──► MySQL: ferrestock_db
 USUARIO                                                     │
 POS React (5174) ──┐                                        │
                    ├── REST + JWT ──► Spring Boot (8080) ───┘
 App Kotlin ────────┘                   (ddl-auto=validate)
```

| Componente | Tecnología | Usuarios | Curso |
|---|---|---|---|
| `pos-backend-springboot` | Spring Boot 4.1.1 + Java 21 + Maven | — | Desarrollo de Aplicaciones Empresariales |
| `pos-frontend-react` | React 19 + Vite 8 | Cajero | Desarrollo de Aplicaciones Empresariales |

**Reglas**

- **Una sola base de datos** (`ferrestock_db`). Django es el dueño del esquema: Spring Boot usa `ddl-auto=validate` y nunca crea tablas.
- **Login único:** Django emite el JWT (`/api/auth/login/`); Spring Boot lo valida con el mismo secreto (HS256) y lee el rol del claim `rol`.
- **IGV incluido:** `total = suma de precios`, `op. gravada = total / 1.18`, `igv = total − op. gravada`.
- **Stock:** todo cambio va en una transacción, con bloqueo de fila y registro en el kárdex.

---

## 2. Estructura del repositorio

```
FerreStock-POS/
├── README.md
├── .gitignore
│
├── pos-backend-springboot/      ← API del POS
│   ├── pom.xml
│   ├── .env.example
│   └── src/main/java/pe/ferrestock/pos/
│
└── pos-frontend-react/          ← POS del cajero (puerto 5174)
    ├── .env.example
    └── src/
        └── styles/global.css    ← colores y fuentes del estilo "Acero y madera"
```

Las subcarpetas internas (`entity/`, `repository/`, `service/`, `controller/`, `security/` en Spring Boot; `api/`, `pages/`, `components/`, `context/` en React) se agregan a medida que se desarrollan las historias de usuario.

---

## 3. Cómo se creó la estructura base

| Proyecto | Cómo se creó |
|---|---|
| `pos-backend-springboot` | [start.spring.io](https://start.spring.io): Maven, Spring Boot 4.1.1, Java 21, paquete `pe.ferrestock.pos`, dependencias Spring Web, Spring Data JPA, MySQL Driver, Spring Security y Validation |
| `pos-frontend-react` | `npm create vite@latest pos-frontend-react -- --template react` (con ESLint), puerto 5174 |

---

## 4. Requisitos

| Herramienta | Versión |
|---|---|
| Git | 2.x |
| JDK | 21 (el de IntelliJ) |
| Node.js | 24 LTS |
| MySQL | 9.2 con la base `ferrestock_db` ya migrada por Django |
| IntelliJ IDEA | Para Spring Boot |

## 5. Cómo levantar cada proyecto

**Spring Boot** (puerto 8080)

1. Copiar `pos-backend-springboot/.env.example` como `.env` y completar `DB_PASSWORD` y `JWT_SECRET` (el mismo `SECRET_KEY` del `.env` de Django).
2. Abrir `pos-backend-springboot` en IntelliJ y ejecutar `PosBackendSpringbootApplication`.

```bat
cd pos-backend-springboot
mvnw test
```

**POS React** (puerto 5174)

```bat
cd pos-frontend-react
npm install
npm run dev
```

Abrir http://localhost:5174

---

## 6. Diseño: estilo "Acero y madera compacto"

| Elemento | Valor |
|---|---|
| Header | Carbón `#23282D`, logo de tuerca hexagonal, menú en píldoras (activo madera `#C08A55`) |
| Franja | Plancha de acero `#3B434B` → `#2A3036` con borde inferior madera |
| Tarjetas | Blancas, borde superior madera, cabeceras de tabla oscuras |
| Botones | Madera con texto carbón |
| Fuentes | Oswald (títulos en mayúsculas) + Barlow (texto) |

Menú: **Nueva venta · Cierre de caja · Mis ventas**.

---

## 7. Estado y siguientes pasos

- [x] Estructura base de Spring Boot conectada a `ferrestock_db` (`ddl-auto=validate`)
- [x] Estructura base del POS React (puerto 5174, fuentes y colores)
- [ ] Spring Boot: validar el JWT de Django y exponer `GET /api/productos`
- [ ] Login del POS contra Django (CAJERO, ADMINISTRADOR y DUENO)
- [ ] HU-05: pantalla "Nueva venta" con carrito, op. gravada, IGV 18 % y total (pruebas con Vitest)
- [ ] Cierre de caja y Mis ventas

## 8. Forma de trabajo

- **Scrum**: 4 sprints (semanas 7 a 15).
- **Un commit por día**: `COMMIT N: descripcion`.
- Código sin comentarios (regla del equipo).
