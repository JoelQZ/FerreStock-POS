# FerreStock · POS (frontend)

Interfaz del **punto de venta del cajero**, hecha con React 19 + Vite 8. Sigue la misma estructura y reglas del React de `FerreStock-Admin`, con el estilo **"Acero y madera"**.

> **Estado:** diseño completo de las 4 pantallas, funcionando con **datos de prueba**. Falta conectarlo a Spring Boot (ver la sección 8).

---

## 1. Cómo levantarlo

| Requisito | Para qué |
|---|---|
| Node.js 24 LTS | Correr Vite |
| Django encendido en `127.0.0.1:8000` | El login (Django emite el JWT) |
| MySQL92 encendido | Lo usa Django para el login |

```bat
cd pos-frontend-react
copy .env.example .env
npm install
npm run dev
```

Abrir **http://localhost:5174** e iniciar sesión con un usuario de rol **CAJERO**, **ADMINISTRADOR** o **DUENO** (los mismos usuarios del Admin).

| Comando | Qué hace |
|---|---|
| `npm run dev` | Servidor de desarrollo en el puerto **5174** (fijo: si está ocupado, muestra error) |
| `npm test` | Pruebas Vitest del carrito |
| `npm run lint` | Revisión con ESLint |
| `npm run build` | Compilación de producción en `dist/` |

**Variables de entorno (`.env`):**

| Variable | Valor | Uso |
|---|---|---|
| `VITE_AUTH_URL` | `http://127.0.0.1:8000/api` | Login y renovación del token (Django) |
| `VITE_API_URL` | `http://127.0.0.1:8080/api` | Datos del POS (Spring Boot) |

> El puerto 5174 es obligatorio: Django solo acepta el login desde `localhost:5173` (Admin) y `localhost:5174` (POS).

---

## 2. Pantallas

| Ruta | Pantalla | Qué permite |
|---|---|---|
| `/login` | **Login** | Iniciar sesión contra Django. Solo entran CAJERO, ADMINISTRADOR y DUENO |
| `/nueva-venta` | **Nueva venta** (HU-05) | Buscar productos, armar el carrito, elegir comprobante y pago, y confirmar la venta |
| `/cierre-caja` | **Cierre de caja** | Abrir el turno con un monto inicial, ver el resumen y cerrarlo contando el efectivo |
| `/mis-ventas` | **Mis ventas** | Ver las ventas del turno, su detalle y anularlas con un motivo |

### 2.1 Nueva venta (HU-05)

- **Productos:** buscador por nombre o código (espera 300 ms al escribir), filtro por categoría y etiquetas de stock **Bajo** (stock ≤ stock mínimo) y **Agotado**.
- **Agregar:** el botón se desactiva si el producto está agotado o si ya está en el carrito todo el stock disponible.
- **Carrito:** botones − / +, "Quitar" y el aviso **"Llegaste al stock disponible"** cuando se llega al tope.
- **Totales** (el precio ya incluye IGV):
  - `Total` = suma de las líneas.
  - `Op. gravada` = total ÷ 1.18.
  - `IGV (18 %)` = total − op. gravada.
- **Comprobante:** Boleta o Factura. La Factura pide **RUC (11 dígitos)** y **razón social**.
- **Tipo de pago:**
  - **Efectivo:** pide el monto recibido (debe ser ≥ total) y muestra el vuelto.
  - **Yape / Plin:** piden el código de operación.
- **Vaciar** limpia el carrito; **Confirmar venta** registra la venta y muestra el comprobante generado (`B001-00000001`).
- Si **no hay turno abierto**, aparece un aviso con enlace a Cierre de caja y no se puede vender.
- Indicadores en la franja: **Turno** (Abierto / Cerrado) y **Ventas hoy**.

### 2.2 Cierre de caja

- **Sin turno:** formulario "Abrir turno" con el monto inicial.
- **Con turno:**
  - Resumen con Ventas, Total vendido, Efectivo y Yape y Plin.
  - Efectivo esperado = monto inicial + ventas en efectivo.
  - Al escribir el **efectivo contado** se muestra si hay **Sobrante**, **Faltante** o si está **Cuadrada**.
  - **Cerrar turno** pide confirmación y muestra el resultado.

### 2.3 Mis ventas

- Tabla con comprobante, hora, cliente, pago, total y estado (Completada / Anulada).
- **Ver detalle:** productos, cantidades, op. gravada, IGV, vuelto y código de operación.
- **Anular:** pide el motivo. La venta queda tachada y se devuelve el stock.
- Indicadores: número de ventas y total vendido del turno.

---

## 3. Estructura

```
src/
├── api/
│   ├── cliente.js          ← axios: `auth` (Django) y `api` (Spring Boot), token y renovación automática
│   ├── auth.js             ← login
│   ├── catalogo.js         ← productos y categorías
│   ├── ventas.js           ← métodos de pago, comprobantes, registrar, listar y anular ventas
│   ├── caja.js             ← turno actual, abrir, resumen y cerrar
│   └── datosPrueba.js      ← datos de prueba en memoria (se reemplaza por Spring Boot)
├── components/
│   ├── Cabecera.jsx        ← header carbón con logo, menú en píldoras, usuario y salir
│   ├── Encabezado.jsx      ← franja de acero con kicker, título, subtítulo e indicadores
│   ├── Indicador.jsx       ← cuadro de la franja (Turno, Ventas hoy…)
│   ├── TituloTarjeta.jsx   ← cabecera de tarjeta con hexágono madera
│   ├── Carrito.jsx         ← carrito, totales, comprobante, pago y confirmación
│   ├── EtiquetaStock.jsx   ← etiquetas Bajo / Agotado
│   ├── Aviso.jsx · Campo.jsx · Icono.jsx
├── context/                ← AuthProvider, auth-context, useAuth (sesión)
├── layouts/LayoutPos.jsx   ← Cabecera + página
├── pages/                  ← Login, NuevaVenta, CierreCaja, MisVentas
├── routes/RutaPrivada.jsx  ← redirige a /login si no hay sesión
├── utils/
│   ├── carrito.js          ← cálculo del carrito en céntimos (puro, sin React)
│   ├── carrito.test.js     ← pruebas Vitest
│   ├── formato.js          ← soles, cantidades, horas (America/Lima), comprobante
│   ├── errores.js          ← convierte errores de la API en mensajes
│   ├── roles.js            ← roles permitidos y normalización (DUEÑO → DUENO)
│   ├── menu.js             ← opciones del menú
│   └── useTurno.js         ← hook del turno actual
└── styles/global.css       ← estilo "Acero y madera"
```

**Reglas del equipo aplicadas:** misma estructura que el React del Admin, código sin comentarios, nombres en español y un solo archivo de estilos.

---

## 4. Sesión y seguridad

- El login va a `POST {VITE_AUTH_URL}/auth/login/` (Django) y guarda `access`, `refresh` y `usuario` en `localStorage` (`ferrestock_pos_sesion`).
- Cada petición a Spring Boot lleva `Authorization: Bearer <access>`.
- Si Spring Boot responde **401**, el cliente renueva el token con `POST {VITE_AUTH_URL}/auth/refresh/` y repite la petición. Si no puede, cierra la sesión.
- El rol se normaliza a mayúsculas sin tilde: `Dueño` → `DUENO`.

---

## 5. Cálculo del carrito (`utils/carrito.js`)

Todo se calcula en **céntimos enteros** para evitar errores de decimales:

| Función | Qué hace |
|---|---|
| `subtotalLinea` | `round(precio_centimos × cantidad)`; la cantidad acepta 3 decimales |
| `calcularTotales` | `total`, `opGravada = round(total / 1.18)` e `igv = total − opGravada` |
| `agregarProducto` / `cambiarCantidad` / `quitarProducto` | Manejan el carrito sin pasar el stock |
| `calcularVuelto` | Recibido − total (nunca negativo) |
| `validarPago` | Efectivo ≥ total; Yape y Plin con código de operación |

**Pruebas (`npm test`, 9 casos):** varios ítems con decimales, `op. gravada + IGV = total`, carrito vacío, tope de stock, producto agotado, vuelto y validación de pagos.

Caso de control: **2 × 38.90 + 3.5 × 2.30 = 85.85** → op. gravada **72.75**, IGV **13.10**.

---

## 6. Datos de prueba (`api/datosPrueba.js`)

| Dato | Origen |
|---|---|
| Categorías (7) | Reales, con los `id` de `ferrestock_db` |
| Métodos de pago: Efectivo (7), Yape (8), Plin (9) | Reales |
| Comprobantes: Boleta B001 (4), Factura F001 (5) | Reales |
| Productos (5) | Los de la maqueta del diseño |
| Turno | Empieza **abierto** con S/ 100.00 |

Los campos tienen **la misma forma que devuelve la API de Django** (`id_producto`, `precio_venta`, `stock`, `categoria_nombre`, `serie`, `numero`, `detalles`, `pagos`, etc.).

> Los datos viven en memoria: **al recargar la página (F5) vuelven al estado inicial.**

---

## 7. Diseño: "Acero y madera"

| Elemento | Valor |
|---|---|
| Carbón (header) | `#23282D` |
| Acero (franja) | `#3B434B` → `#2A3036`, con textura de puntos y borde inferior madera |
| Madera (acento y botones) | `#C08A55` · hover `#A87445` |
| Fondo | `#ECEBE7` |
| Tarjetas | Blancas, borde superior madera, cabeceras de tabla `#2A3036` |
| Bajo | Fondo `#FDF1D6`, texto `#7A5200` |
| Agotado | Fondo `#FBE3E1`, texto `#9A2A20` |
| Fuentes | **Oswald** (títulos en MAYÚSCULAS) + **Barlow** (texto) |

Diseño adaptable: en pantallas medianas el carrito pasa debajo de los productos y en celular el menú muestra solo íconos. La barra de desplazamiento queda siempre reservada para que la pantalla no salte al cambiar de página.

---

## 8. Pendiente: conectar a Spring Boot

Cuando el backend tenga listos sus endpoints, solo se cambian **`api/catalogo.js`**, **`api/ventas.js`** y **`api/caja.js`** para que usen `api.get` / `api.post` en vez de `datosPrueba.js`. Las pantallas no cambian.

| Función del front | Endpoint de Spring Boot |
|---|---|
| `listarProductos({ buscar, categoria })` | `GET /api/productos?buscar=&categoria=` |
| `listarCategorias()` | `GET /api/categorias` |
| `listarMetodosPago()` | `GET /api/metodos-pago` |
| `listarComprobantes()` | `GET /api/comprobantes` |
| `registrarVenta(venta)` | `POST /api/ventas` |
| `listarVentasTurno()` | `GET /api/ventas/turno-actual` |
| `anularVenta(id, motivo)` | `POST /api/ventas/{id}/anular` |
| `obtenerTurnoActual()` | `GET /api/turnos/actual` |
| `abrirTurno(montoInicial)` | `POST /api/turnos/abrir` |
| `obtenerResumenTurno(id)` | `GET /api/turnos/{id}/resumen` |
| `cerrarTurno(montoReal)` | `POST /api/turnos/cerrar` |

Después de conectar: probar el flujo completo (abrir turno → vender → ver en Mis ventas → anular → cerrar turno) contra MySQL.
