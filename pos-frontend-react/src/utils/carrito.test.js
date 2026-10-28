import { describe, expect, it } from 'vitest'
import {
  agregarProducto,
  calcularTotales,
  calcularVuelto,
  cambiarCantidad,
  quitarProducto,
  validarPago,
} from './carrito'

const martillo = { id_producto: 1, precio_venta: '38.90', stock: '10.000' }
const cable = { id_producto: 2, precio_venta: '2.30', stock: '50.000' }
const tornillo = { id_producto: 3, precio_venta: '12.00', stock: '1.000' }
const pintura = { id_producto: 4, precio_venta: '42.90', stock: '0.000' }
const efectivo = { id_metodo: 7, nombre: 'Efectivo', requiere_codigo: false }
const yape = { id_metodo: 8, nombre: 'Yape', requiere_codigo: true }

describe('calcularTotales', () => {
  it('suma varios ítems con cantidades decimales y separa el IGV incluido', () => {
    const items = [
      { ...martillo, cantidad: 2 },
      { ...cable, cantidad: 3.5 },
    ]
    expect(calcularTotales(items)).toEqual({ total: 8585, opGravada: 7275, igv: 1310 })
  })

  it('cumple que op. gravada más IGV es igual al total', () => {
    const items = [{ ...martillo, cantidad: 1 }, { ...cable, cantidad: 0.333 }, { ...tornillo, cantidad: 1 }]
    const { total, opGravada, igv } = calcularTotales(items)
    expect(opGravada + igv).toBe(total)
  })

  it('devuelve cero con el carrito vacío', () => {
    expect(calcularTotales([])).toEqual({ total: 0, opGravada: 0, igv: 0 })
  })
})

describe('stock', () => {
  it('no deja agregar más que el stock disponible', () => {
    const items = agregarProducto([], tornillo)
    expect(agregarProducto(items, tornillo)).toBe(items)
    expect(items[0].cantidad).toBe(1)
  })

  it('no deja agregar un producto agotado', () => {
    expect(agregarProducto([], pintura)).toEqual([])
  })

  it('limita la cantidad al stock y quita el ítem si llega a cero', () => {
    const items = agregarProducto([], martillo)
    expect(cambiarCantidad(items, 1, 25)[0].cantidad).toBe(10)
    expect(cambiarCantidad(items, 1, 0)).toEqual([])
    expect(quitarProducto(items, 1)).toEqual([])
  })
})

describe('pago', () => {
  it('calcula el vuelto en efectivo', () => {
    expect(calcularVuelto(8585, '100')).toBe(1415)
  })

  it('rechaza efectivo menor al total', () => {
    expect(validarPago({ metodo: efectivo, total: 8585, recibido: '80' })).not.toBe('')
    expect(validarPago({ metodo: efectivo, total: 8585, recibido: '85.85' })).toBe('')
  })

  it('pide el código de operación en Yape', () => {
    expect(validarPago({ metodo: yape, total: 8585, codigo: ' ' })).not.toBe('')
    expect(validarPago({ metodo: yape, total: 8585, codigo: '123456' })).toBe('')
  })
})
