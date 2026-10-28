import { aCentimos, aSoles, calcularTotales, esEfectivo } from '../utils/carrito'

const categorias = [
  { id_categoria: 13, nombre: 'Construcción' },
  { id_categoria: 10, nombre: 'Electricidad' },
  { id_categoria: 14, nombre: 'Ferretería general' },
  { id_categoria: 11, nombre: 'Gasfitería' },
  { id_categoria: 9, nombre: 'Herramientas eléctricas' },
  { id_categoria: 8, nombre: 'Herramientas manuales' },
  { id_categoria: 12, nombre: 'Pinturas' },
]

const metodosPago = [
  { id_metodo: 7, nombre: 'Efectivo', requiere_codigo: false },
  { id_metodo: 8, nombre: 'Yape', requiere_codigo: true },
  { id_metodo: 9, nombre: 'Plin', requiere_codigo: true },
]

const comprobantes = [
  { id_tipo: 4, nombre: 'Boleta', serie: 'B001', correlativo_actual: 0 },
  { id_tipo: 5, nombre: 'Factura', serie: 'F001', correlativo_actual: 0 },
]

const productos = [
  producto(1, 'HER-0012', 'Martillo de uña 16 oz', '25.90', '38.000', '10.000', 8, 'Unidad'),
  producto(2, 'HER-0031', 'Cinta métrica 5 m', '18.50', '4.000', '5.000', 8, 'Unidad'),
  producto(3, 'ELE-0105', 'Taladro percutor 650 W', '189.00', '12.000', '3.000', 9, 'Unidad'),
  producto(4, 'PIN-0210', 'Pintura látex blanco 1 gal', '42.90', '0.000', '5.000', 12, 'Unidad'),
  producto(5, 'FIJ-0302', 'Tornillo autorroscante (caja)', '12.00', '1.000', '5.000', 14, 'Caja'),
]

const ventas = []
let turnos = [
  {
    id_turno: 1,
    usuario: null,
    usuario_nombre: '',
    apertura: new Date().toISOString(),
    monto_inicial: '100.00',
    cierre: null,
    monto_esperado: '0.00',
    monto_real: null,
    diferencia: null,
    estado: 'ABIERTO',
  },
]

function producto(id, codigo, nombre, precio, stock, minimo, idCategoria, unidad) {
  return {
    id_producto: id,
    codigo,
    nombre,
    precio_venta: precio,
    stock,
    stock_minimo: minimo,
    categoria: idCategoria,
    categoria_nombre: categorias.find((item) => item.id_categoria === idCategoria).nombre,
    unidad_nombre: unidad,
    estado: true,
  }
}

function responder(datos) {
  return new Promise((resolver) => setTimeout(() => resolver(structuredClone(datos)), 250))
}

function rechazar(campos) {
  const error = new Error('Datos inválidos')
  error.response = { status: 400, data: campos }
  return new Promise((_, rechazo) => setTimeout(() => rechazo(error), 250))
}

function normalizar(texto) {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
}

function turnoAbierto() {
  return turnos.find((turno) => turno.estado === 'ABIERTO') ?? null
}

function resumenDe(turno) {
  const delTurno = ventas.filter((venta) => venta.turno === turno.id_turno && venta.estado === 'COMPLETADA')
  const pagos = delTurno.flatMap((venta) => venta.pagos)
  const efectivo = pagos.filter((pago) => pago.metodo_nombre === 'Efectivo').reduce((suma, pago) => suma + aCentimos(pago.monto), 0)
  const otros = pagos.filter((pago) => pago.metodo_nombre !== 'Efectivo').reduce((suma, pago) => suma + aCentimos(pago.monto), 0)
  const total = delTurno.reduce((suma, venta) => suma + aCentimos(venta.total), 0)
  return {
    turno,
    ventas: delTurno.length,
    ventas_total: aSoles(total),
    efectivo_ventas: aSoles(efectivo),
    otros_pagos: aSoles(otros),
    ingresos_caja: '0.00',
    egresos_caja: '0.00',
    monto_esperado: aSoles(aCentimos(turno.monto_inicial) + efectivo),
  }
}

export function listarProductos({ buscar = '', categoria = '' } = {}) {
  const texto = normalizar(buscar.trim())
  const lista = productos.filter(
    (item) =>
      (!texto || normalizar(item.nombre).includes(texto) || normalizar(item.codigo).includes(texto)) &&
      (!categoria || item.categoria === Number(categoria)),
  )
  return responder(lista)
}

export const listarCategorias = () => responder(categorias)
export const listarMetodosPago = () => responder(metodosPago)
export const listarComprobantes = () => responder(comprobantes)

export const obtenerTurnoActual = () => responder(turnoAbierto())

export function abrirTurno(montoInicial, usuario) {
  if (turnoAbierto()) return rechazar({ detail: 'Ya tienes un turno abierto.' })
  const turno = {
    id_turno: turnos.length + 1,
    usuario: usuario?.id ?? null,
    usuario_nombre: usuario?.nombre ?? '',
    apertura: new Date().toISOString(),
    monto_inicial: aSoles(aCentimos(montoInicial)),
    cierre: null,
    monto_esperado: '0.00',
    monto_real: null,
    diferencia: null,
    estado: 'ABIERTO',
  }
  turnos = [turno, ...turnos]
  return responder(turno)
}

export function obtenerResumenTurno(idTurno) {
  const turno = turnos.find((item) => item.id_turno === idTurno)
  return responder(resumenDe(turno))
}

export function cerrarTurno(montoReal) {
  const turno = turnoAbierto()
  if (!turno) return rechazar({ detail: 'No tienes un turno de caja abierto.' })
  const resumen = resumenDe(turno)
  Object.assign(turno, {
    cierre: new Date().toISOString(),
    monto_esperado: resumen.monto_esperado,
    monto_real: aSoles(aCentimos(montoReal)),
    diferencia: aSoles(aCentimos(montoReal) - aCentimos(resumen.monto_esperado)),
    estado: 'CERRADO',
  })
  return responder({ ...resumen, turno })
}

export function listarVentasTurno() {
  const turno = turnoAbierto() ?? turnos[0]
  return responder(ventas.filter((venta) => venta.turno === turno?.id_turno))
}

export function registrarVenta({ cliente, tipo_comprobante, detalles, pagos }, usuario) {
  const turno = turnoAbierto()
  if (!turno) return rechazar({ turno: 'Debes abrir un turno de caja antes de registrar ventas.' })

  for (const detalle of detalles) {
    const item = productos.find((prod) => prod.id_producto === detalle.producto)
    if (Number(detalle.cantidad) > Number(item.stock)) {
      return rechazar({ detalles: `Stock insuficiente para ${item.nombre}.` })
    }
  }

  const lineas = detalles.map((detalle) => {
    const item = productos.find((prod) => prod.id_producto === detalle.producto)
    item.stock = (Number(item.stock) - Number(detalle.cantidad)).toFixed(3)
    return { ...item, cantidad: detalle.cantidad }
  })
  const { total, opGravada, igv } = calcularTotales(lineas)
  const comprobante = comprobantes.find((item) => item.id_tipo === tipo_comprobante)
  comprobante.correlativo_actual += 1

  const venta = {
    id_venta: ventas.length + 1,
    fecha_hora: new Date().toISOString(),
    subtotal: aSoles(opGravada),
    igv: aSoles(igv),
    total: aSoles(total),
    estado: 'COMPLETADA',
    usuario: usuario?.id ?? null,
    usuario_nombre: usuario?.nombre ?? '',
    turno: turno.id_turno,
    cliente_nombre: cliente?.nombre_razon_social ?? null,
    tipo_comprobante: comprobante.id_tipo,
    tipo_comprobante_nombre: comprobante.nombre,
    serie: comprobante.serie,
    numero: comprobante.correlativo_actual,
    motivo_anulacion: null,
    fecha_anulacion: null,
    detalles: lineas.map((linea, indice) => ({
      id_detalle: indice + 1,
      producto: linea.id_producto,
      producto_codigo: linea.codigo,
      producto_nombre: linea.nombre,
      cantidad: Number(linea.cantidad).toFixed(3),
      precio_unitario: linea.precio_venta,
      subtotal: aSoles(Math.round(aCentimos(linea.precio_venta) * Number(linea.cantidad))),
    })),
    pagos: pagos.map((pago, indice) => {
      const metodo = metodosPago.find((item) => item.id_metodo === pago.metodo)
      const recibido = esEfectivo(metodo) ? pago.monto_recibido : pago.monto
      return {
        id_pago: indice + 1,
        metodo: metodo.id_metodo,
        metodo_nombre: metodo.nombre,
        monto: pago.monto,
        monto_recibido: recibido,
        vuelto: aSoles(aCentimos(recibido) - aCentimos(pago.monto)),
        codigo_operacion: pago.codigo_operacion ?? '',
      }
    }),
  }
  ventas.unshift(venta)
  return responder(venta)
}

export function anularVenta(idVenta, motivo) {
  const venta = ventas.find((item) => item.id_venta === idVenta)
  if (!venta || venta.estado === 'ANULADA') return rechazar({ detail: 'La venta ya está anulada.' })
  if (!motivo?.trim()) return rechazar({ motivo: 'Indica el motivo de la anulación.' })
  for (const detalle of venta.detalles) {
    const item = productos.find((prod) => prod.id_producto === detalle.producto)
    item.stock = (Number(item.stock) + Number(detalle.cantidad)).toFixed(3)
  }
  Object.assign(venta, { estado: 'ANULADA', motivo_anulacion: motivo.trim(), fecha_anulacion: new Date().toISOString() })
  return responder(venta)
}
