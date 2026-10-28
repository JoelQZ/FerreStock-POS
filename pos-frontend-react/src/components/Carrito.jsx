import { useEffect, useState } from 'react'
import { listarComprobantes, listarMetodosPago } from '../api/ventas'
import {
  aCentimos,
  aSoles,
  calcularTotales,
  calcularVuelto,
  esEfectivo,
  stockDisponible,
  subtotalLinea,
  validarPago,
} from '../utils/carrito'
import { erroresDeApi } from '../utils/errores'
import { formatearCantidad, formatearCentimos } from '../utils/formato'
import Aviso from './Aviso'
import Campo from './Campo'
import Icono from './Icono'
import TituloTarjeta from './TituloTarjeta'

const CLIENTE_VACIO = { numero_documento: '', nombre_razon_social: '' }

export default function Carrito({ items, turnoAbierto, alCambiar, alQuitar, alVaciar, alConfirmar }) {
  const [opciones, setOpciones] = useState({ metodos: [], comprobantes: [] })
  const [idComprobante, setIdComprobante] = useState(null)
  const [idMetodo, setIdMetodo] = useState(null)
  const [recibido, setRecibido] = useState('')
  const [codigo, setCodigo] = useState('')
  const [cliente, setCliente] = useState(CLIENTE_VACIO)
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    let activo = true
    Promise.all([listarMetodosPago(), listarComprobantes()]).then(([metodos, comprobantes]) => {
      if (!activo) return
      setOpciones({ metodos, comprobantes })
      setIdMetodo(metodos[0]?.id_metodo ?? null)
      setIdComprobante(comprobantes[0]?.id_tipo ?? null)
    })
    return () => {
      activo = false
    }
  }, [])

  const { total, opGravada, igv } = calcularTotales(items)
  const metodo = opciones.metodos.find((item) => item.id_metodo === idMetodo)
  const comprobante = opciones.comprobantes.find((item) => item.id_tipo === idComprobante)
  const esFactura = comprobante?.nombre === 'Factura'
  const efectivo = esEfectivo(metodo)

  function elegirMetodo(id) {
    setIdMetodo(id)
    setRecibido('')
    setCodigo('')
    setError('')
  }

  function limpiar() {
    setRecibido('')
    setCodigo('')
    setCliente(CLIENTE_VACIO)
    setError('')
  }

  function vaciar() {
    alVaciar()
    limpiar()
  }

  async function confirmar() {
    if (!turnoAbierto) return setError('Abre un turno de caja antes de vender.')
    if (items.length === 0) return setError('Agrega al menos un producto.')
    if (esFactura && !/^\d{11}$/.test(cliente.numero_documento)) return setError('El RUC debe tener 11 dígitos.')
    if (esFactura && !cliente.nombre_razon_social.trim()) return setError('Ingresa la razón social.')
    const falla = validarPago({ metodo, total, recibido, codigo })
    if (falla) return setError(falla)

    setEnviando(true)
    setError('')
    try {
      await alConfirmar({
        cliente: esFactura ? { tipo_documento: 'RUC', ...cliente } : null,
        tipo_comprobante: idComprobante,
        detalles: items.map((item) => ({ producto: item.id_producto, cantidad: item.cantidad })),
        pagos: [
          {
            metodo: idMetodo,
            monto: aSoles(total),
            monto_recibido: efectivo ? aSoles(aCentimos(recibido)) : undefined,
            codigo_operacion: efectivo ? '' : codigo.trim(),
          },
        ],
      })
      limpiar()
    } catch (falla) {
      const { general, campos } = erroresDeApi(falla)
      setError(general || Object.values(campos)[0] || 'No se pudo registrar la venta.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="tarjeta carrito">
      <TituloTarjeta titulo="Carrito" detalle={`${items.length} ${items.length === 1 ? 'ítem' : 'ítems'}`} />

      {items.length === 0 ? (
        <div className="vacio vacio--chico">
          <Icono nombre="carrito" tamano={32} />
          <p>Agrega productos desde la lista.</p>
        </div>
      ) : (
        <ul className="carrito__lista">
          {items.map((item) => {
            const enTope = item.cantidad >= stockDisponible(item)
            return (
              <li key={item.id_producto} className="carrito__item">
                <div className="carrito__fila">
                  <strong>{item.nombre}</strong>
                  <span className="carrito__subtotal">{formatearCentimos(subtotalLinea(item))}</span>
                </div>
                {enTope && <p className="carrito__tope">Llegaste al stock disponible</p>}
                <div className="carrito__fila">
                  <div className="contador">
                    <button
                      type="button"
                      onClick={() => alCambiar(item.id_producto, item.cantidad - 1)}
                      aria-label={`Quitar una unidad de ${item.nombre}`}
                    >
                      <Icono nombre="menos" tamano={14} />
                    </button>
                    <span>{formatearCantidad(item.cantidad)}</span>
                    <button
                      type="button"
                      onClick={() => alCambiar(item.id_producto, item.cantidad + 1)}
                      disabled={enTope}
                      aria-label={`Agregar una unidad de ${item.nombre}`}
                    >
                      <Icono nombre="mas" tamano={14} />
                    </button>
                  </div>
                  <button type="button" className="enlace" onClick={() => alQuitar(item.id_producto)}>
                    Quitar
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
      )}

      <dl className="totales">
        <div>
          <dt>Op. gravada</dt>
          <dd>{formatearCentimos(opGravada)}</dd>
        </div>
        <div>
          <dt>IGV (18 %)</dt>
          <dd>{formatearCentimos(igv)}</dd>
        </div>
        <div className="totales__total">
          <dt>Total</dt>
          <dd>{formatearCentimos(total)}</dd>
        </div>
      </dl>

      <div className="carrito__seccion">
        <p className="carrito__etiqueta">Comprobante</p>
        <div className="opciones">
          {opciones.comprobantes.map((item) => (
            <button
              key={item.id_tipo}
              type="button"
              className={`opcion${item.id_tipo === idComprobante ? ' opcion--activa' : ''}`}
              onClick={() => setIdComprobante(item.id_tipo)}
              aria-pressed={item.id_tipo === idComprobante}
            >
              {item.nombre}
            </button>
          ))}
        </div>
        {esFactura && (
          <div className="carrito__campos">
            <Campo etiqueta="RUC" obligatorio>
              <input
                className="entrada"
                inputMode="numeric"
                maxLength={11}
                value={cliente.numero_documento}
                onChange={(evento) =>
                  setCliente((previo) => ({ ...previo, numero_documento: evento.target.value.replace(/\D/g, '') }))
                }
              />
            </Campo>
            <Campo etiqueta="Razón social" obligatorio>
              <input
                className="entrada"
                value={cliente.nombre_razon_social}
                onChange={(evento) => setCliente((previo) => ({ ...previo, nombre_razon_social: evento.target.value }))}
              />
            </Campo>
          </div>
        )}
      </div>

      <div className="carrito__seccion">
        <p className="carrito__etiqueta">Tipo de pago</p>
        <div className="opciones opciones--tres">
          {opciones.metodos.map((item) => (
            <button
              key={item.id_metodo}
              type="button"
              className={`opcion${item.id_metodo === idMetodo ? ' opcion--activa' : ''}`}
              onClick={() => elegirMetodo(item.id_metodo)}
              aria-pressed={item.id_metodo === idMetodo}
            >
              {item.nombre}
            </button>
          ))}
        </div>
        {metodo && (
          <div className="carrito__campos">
            {efectivo ? (
              <>
                <Campo etiqueta="Monto recibido" obligatorio>
                  <div className="con-prefijo">
                    <span>S/</span>
                    <input
                      className="entrada"
                      type="number"
                      min="0"
                      step="0.10"
                      inputMode="decimal"
                      value={recibido}
                      onChange={(evento) => setRecibido(evento.target.value)}
                    />
                  </div>
                </Campo>
                <div className="vuelto">
                  <span>Vuelto</span>
                  <strong>{formatearCentimos(calcularVuelto(total, recibido))}</strong>
                </div>
              </>
            ) : (
              <Campo etiqueta={`Código de operación ${metodo.nombre}`} obligatorio ancho>
                <input className="entrada" value={codigo} onChange={(evento) => setCodigo(evento.target.value)} />
              </Campo>
            )}
          </div>
        )}
      </div>

      {error && (
        <div className="carrito__seccion">
          <Aviso tipo="error" alCerrar={() => setError('')}>
            {error}
          </Aviso>
        </div>
      )}

      <div className="carrito__acciones">
        <button type="button" className="boton boton--secundario" onClick={vaciar} disabled={items.length === 0}>
          Vaciar
        </button>
        <button type="button" className="boton boton--principal" onClick={confirmar} disabled={enviando}>
          {enviando ? 'Registrando…' : 'Confirmar venta'}
        </button>
      </div>
    </section>
  )
}
