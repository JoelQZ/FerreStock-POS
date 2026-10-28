export const IGV = 0.18

export function aCentimos(valor) {
  return Math.round(Number(valor || 0) * 100)
}

export function aSoles(centimos) {
  return (centimos / 100).toFixed(2)
}

export function subtotalLinea(item) {
  return Math.round(aCentimos(item.precio_venta) * Number(item.cantidad))
}

export function calcularTotales(items) {
  const total = items.reduce((suma, item) => suma + subtotalLinea(item), 0)
  const opGravada = Math.round(total / (1 + IGV))
  return { total, opGravada, igv: total - opGravada }
}

export function stockDisponible(producto) {
  return Math.max(Number(producto.stock) || 0, 0)
}

export function cantidadEnCarrito(items, idProducto) {
  return items.find((item) => item.id_producto === idProducto)?.cantidad ?? 0
}

export function puedeAgregar(items, producto, cantidad = 1) {
  return cantidadEnCarrito(items, producto.id_producto) + cantidad <= stockDisponible(producto)
}

export function agregarProducto(items, producto, cantidad = 1) {
  if (!puedeAgregar(items, producto, cantidad)) return items
  if (items.some((item) => item.id_producto === producto.id_producto)) {
    return items.map((item) =>
      item.id_producto === producto.id_producto ? { ...item, cantidad: redondearCantidad(item.cantidad + cantidad) } : item,
    )
  }
  return [...items, { ...producto, cantidad: redondearCantidad(cantidad) }]
}

export function cambiarCantidad(items, idProducto, cantidad) {
  const valor = redondearCantidad(cantidad)
  if (valor <= 0) return quitarProducto(items, idProducto)
  return items.map((item) =>
    item.id_producto === idProducto ? { ...item, cantidad: Math.min(valor, stockDisponible(item)) } : item,
  )
}

export function quitarProducto(items, idProducto) {
  return items.filter((item) => item.id_producto !== idProducto)
}

export function redondearCantidad(cantidad) {
  return Math.round(Number(cantidad) * 1000) / 1000
}

export function esEfectivo(metodo) {
  return metodo?.nombre?.trim().toLowerCase() === 'efectivo'
}

export function calcularVuelto(total, recibido) {
  return Math.max(aCentimos(recibido) - total, 0)
}

export function validarPago({ metodo, total, recibido, codigo }) {
  if (!metodo) return 'Elige el tipo de pago.'
  if (esEfectivo(metodo)) {
    if (aCentimos(recibido) < total) return 'El efectivo recibido no puede ser menor al total.'
  } else if (metodo.requiere_codigo && !codigo?.trim()) {
    return `Ingresa el código de operación de ${metodo.nombre}.`
  }
  return ''
}
