# Spec-Driven Development (SDD) — Backend Spring Boot POS

## 1. Contexto y Visión General
Este proyecto representa el backend del Punto de Venta (POS) para la ferretería **FerreStock**.
El objetivo es proveer una API REST transaccional, segura y concurrente en Java 21 / Spring Boot 4.1.1 para la gestión de catálogo, aperturas/cierres de caja, emisión de ventas y control de stock en tiempo real.

---

## 2. Reglas Inquebrantables del Sistema (Hard Rules)

1. **Esquema de BD Inmutable:**
    * La base de datos MySQL `ferrestock_db` pertenece al backend de Django.
    * `spring.jpa.hibernate.ddl-auto` DEBE estar en `validate`.
    * **NUNCA** se deben crear, alterar ni eliminar tablas o columnas mediante Hibernate.
2. **Autenticación Centralizada:**
    * El login se procesa en Django (emite JWT firmado con algoritmo `HS256`).
    * Spring Boot únicamente **valida** la firma del token usando `JWT_SECRET`.
    * El ID del usuario y su rol para transacciones (ventas/turnos) **NUNCA** se reciben en el body; se extraen directamente del token validado (`SecurityContextHolder`).
3. **Cálculos Financieros (Regla del IGV Perú):**
    * Precios de venta guardados en BD **ya incluyen IGV** (18% por defecto desde la tabla `negocio`).
    * `total` = suma de subtotales de cada línea de detalle.
    * `operacion_gravada` = `total / (1 + (igv_porcentaje / 100))` redondeado a 2 decimales (`RoundingMode.HALF_UP`).
    * `igv` = `total - operacion_gravada`.
    * Todos los cálculos monetarios deben usar obligatoriamente `BigDecimal`.
4. **Concurrencia y Control de Stock Seguro:**
    * Toda modificación de stock debe ejecutarse dentro de una transacción `@Transactional`.
    * Los registros de la tabla `producto` consumidos en una venta deben bloquearse usando **bloqueo pesimista** (`PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`).
    * Para evitar *deadlocks*, los productos involucrados en una venta deben ordenarse y bloquearse en la BD **siempre ordenados por `id_producto` ascendente**.
    * Cada salida o entrada de stock DEBE generar su correspondiente registro en la tabla `movimiento_inventario` (Kárdex).
5. **Zona Horaria y Formato de Respuesta:**
    * Las fechas se procesan e insertan en la zona horaria `America/Lima`.
    * Las respuestas JSON deben respetar el estándar `snake_case` y los contratos definidos para el frontend en React.

---

## 3. Arquitectura de Paquetes (`pe.ferrestock.pos`)

pe.ferrestock.pos
├── config/        # SecurityConfig, CorsConfig
├── security/      # JwtFilter, JwtProvider, UsuarioAutenticado
├── entity/        # Entidades JPA (mapeo directo a la BD de Django)
├── repository/    # JpaRepository con consultas personalizadas y bloqueos
├── dto/           # Records/DTOs de entrada y salida (snake_case)
├── service/       # Interfaces y ServiceImpl (Lógica de negocio y transacciones)
├── controller/    # Endpoints REST (@RestController)
└── exception/     # ErrorApi, CustomExceptions, @ControllerAdvice Global

---

## 4. Mapeo de Entidades Clave (JPA Read-Only Scheme)

| Entidad | Tabla BD | Campos Clave / Requisitos |
|---|---|---|
| `Producto` | `producto` | `idProducto` (Long), `codigo`, `nombre`, `precioVenta` (BigDecimal), `stock` (BigDecimal), `stockMinimo`, `estado` (Boolean). Mapear relaciones `@ManyToOne` con Categoria, Marca, UnidadMedida. |
| `Categoria` | `categoria` | `id`, `nombre`, `estado`. |
| `MetodoPago` | `metodo_pago` | `idMetodo`, `nombre`, `requiereCodigo`, `estado`. |
| `TipoComprobante` | `tipo_comprobante` | `idTipo`, `nombre`, `serie`, `correlativoActual` (Long). |
| `Cliente` | `cliente` | `idCliente`, `tipoDocumento`, `numeroDocumento`, `nombreRazonSocial`, `direccion`. |
| `Turno` | `turno` | `idTurno`, `idUsuario`, `apertura`, `montoInicial`, `cierre`, `montoEsperado`, `montoReal`, `diferencia`, `estado` (Enum: `ABIERTO`, `CERRADO`). |
| `MovimientoCaja`| `movimiento_caja`| `idMovCaja`, `idTurno`, `idUsuario`, `tipo` (`INGRESO`/`EGRESO`), `monto`, `motivo`, `fecha`. |
| `Venta` | `venta` | `idVenta`, `fechaHora`, `subtotal`, `igv`, `total`, `estado` (`COMPLETADA`/`ANULADA`), `idUsuario`, `idTurno`, `idCliente`, `idTipoComprobante`, `serie`, `numero`, `motivoAnulacion`, `fechaAnulacion`, `idUsuarioAnula`. |
| `DetalleVenta` | `detalle_venta` | `idDetalle`, `idVenta`, `idProducto`, `cantidad`, `precioUnitario`, `subtotal`. |
| `Pago` | `pago` | `idPago`, `idVenta`, `idMetodo`, `monto`, `montoRecibido`, `vuelto`, `codigoOperacion`. |
| `MovimientoInventario`| `movimiento_inventario`| `idMovimiento`, `idProducto`, `idUsuario`, `idVenta`, `tipo` (`ENTRADA`/`SALIDA`/`AJUSTE`), `cantidad`, `stockAnterior`, `stockNuevo`, `fecha`. |
| `Negocio` | `negocio` | `id`, `igvPorcentaje` (BigDecimal). |

---

## 5. Especificación de Endpoints y Contratos REST

### 5.1 Catálogo
* `GET /api/productos?buscar={query}&categoria={id}` -> Devuelve lista de productos activos (`estado = true`).
* `GET /api/categorias` -> Devuelve categorías activas.
* `GET /api/metodos-pago` -> Devuelve métodos de pago activos (`Efectivo`, `Yape`, `Plin`, etc.).
* `GET /api/comprobantes` -> Devuelve los tipos de comprobantes activos (`Boleta`, `Factura`).

### 5.2 Gestión de Turnos (Caja)
* `GET /api/turnos/actual`:
    * Busca el turno con `estado = 'ABIERTO'` para el `idUsuario` autenticado. Devuelve `null` si no existe.
* `POST /api/turnos/abrir`:
    * **Payload:** `{ "monto_inicial": "100.00" }`
    * Validar que el usuario NO tenga otro turno en estado `ABIERTO`.
* `GET /api/turnos/{id}/resumen`:
    * Calcula acumulados del turno: ventas en efectivo, otros pagos, ingresos/egresos de caja, y `monto_esperado` = `monto_inicial + efectivo_ventas + ingresos_caja - egresos_caja`.
* `POST /api/turnos/cerrar`:
    * **Payload:** `{ "monto_real": "190.00" }`
    * Calcula `diferencia = monto_real - monto_esperado`, actualiza el turno a `CERRADO` y guarda la fecha de cierre.

### 5.3 Procesamiento de Ventas (Transacción Crítica)
* `POST /api/ventas`:
    * **Payload de Ejemplo:**
      {
      "cliente": null,
      "tipo_comprobante": 4,
      "detalles": [
      { "producto": 1, "cantidad": 2.000 }
      ],
      "pagos": [
      { "metodo": 7, "monto": "51.80", "monto_recibido": "60.00", "codigo_operacion": "" }
      ]
      }
    * **Pasos de Ejecución Obligatorios:**
        1. Validar que el usuario tenga un turno `ABIERTO` (aplicar bloqueo sobre la fila del turno).
        2. Bloquear en la base de datos la fila correspondiente a `tipo_comprobante`.
        3. Extraer los IDs de producto del detalle, ordenarlos de menor a mayor (`ORDER BY id_producto ASC`) y aplicar bloqueo pesimista (`FOR UPDATE`).
        4. Validar existencia, estado activo y que `stock_disponible >= cantidad_solicitada`. Caso contrario, arrojar excepción con mensaje descriptivo de stock insuficiente.
        5. Recalcular precios, totales, operación gravada e IGV desde el backend (no confiar en montos enviados por el cliente).
        6. Validar pagos: la suma de los montos debe cubrir el `total` de la venta. Si es Yape/Plin, verificar que `codigo_operacion` no venga vacío.
        7. Si es Factura (`tipo_comprobante` correspondiente), buscar el `Cliente` por RUC (11 dígitos) o crearlo si no existe.
        8. Generar número de comprobante incrementando `correlativo_actual + 1`.
        9. Insertar registros en `venta`, `detalle_venta` y `pago`.
        10. Restar stock del producto e insertar registro en `movimiento_inventario` con tipo `SALIDA`.

* `POST /api/ventas/{id}/anular`:
    * **Payload:** `{ "motivo": "Cliente canceló la compra" }`
    * Verificar que la venta esté en estado `COMPLETADA`.
    * Revertir stock de los productos con bloqueo pesimista.
    * Crear registros en `movimiento_inventario` con tipo `ENTRADA`.
    * Cambiar estado de la venta a `ANULADA`, registrando fecha, motivo e ID del usuario que anula.

---

## 6. Formato de Control de Errores
Cualquier excepción debe ser capturada por `@ControllerAdvice` y retornada con la siguiente estructura idéntica a Django:

* **Error General (HTTP 400 Bad Request):**
  {
  "detail": "Stock insuficiente para CODIGO: HER-0012. Disponible: 3.000, Solicitado: 5.000"
  }
* **Error de Validación de Campo (HTTP 400 Bad Request):**
  {
  "monto_inicial": "El monto inicial debe ser mayor o igual a 0.00"
  }
* **No Autorizado (HTTP 401 Unauthorized):**
  {
  "detail": "Token JWT inválido o expirado"
  }
* **Acceso Prohibido (HTTP 403 Forbidden):**
  {
  "detail": "No tienes permisos para realizar esta acción"
  }

---

## 7. Instrucciones para la IA (Prompt de Ejecución)
Al implementar código bajo esta especificación:
* Genera el código **sin comentarios innecesarios**, priorizando limpieza y nombres descriptivos en español.
* Garantiza que cada entidad use la anotación `@Table(name = "nombre_exacto")` y `@Column(name = "columna_exacta")`.
* Asegúrate de incluir pruebas unitarias/integración con JUnit 5 para validar el cálculo de IGV, la contención de transacciones sin turno abierto y los *rollbacks* por stock insuficiente.