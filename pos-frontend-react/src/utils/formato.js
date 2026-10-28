const soles = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const cantidad = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 3 })
const hora = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit', timeZone: 'America/Lima' })
const fechaHora = new Intl.DateTimeFormat('es-PE', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: 'America/Lima',
})

export function formatearSoles(valor) {
  return soles.format(Number(valor) || 0)
}

export function formatearCentimos(centimos) {
  return soles.format((centimos || 0) / 100)
}

export function formatearCantidad(valor) {
  return cantidad.format(Number(valor) || 0)
}

export function formatearHora(fecha) {
  return fecha ? hora.format(new Date(fecha)) : '—'
}

export function formatearFechaHora(fecha) {
  return fecha ? fechaHora.format(new Date(fecha)) : '—'
}

export function formatearComprobante(venta) {
  return `${venta.serie}-${String(venta.numero).padStart(8, '0')}`
}

export function tieneStockBajo(producto) {
  return Number(producto.stock) <= Number(producto.stock_minimo)
}

export function iniciales(nombre = '') {
  const partes = nombre.trim().split(/\s+/).filter(Boolean)
  return partes.slice(0, 2).map((parte) => parte[0].toUpperCase()).join('') || '?'
}
