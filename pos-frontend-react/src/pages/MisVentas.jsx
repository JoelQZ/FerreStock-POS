import { Fragment, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { anularVenta, listarVentasTurno } from '../api/ventas'
import Aviso from '../components/Aviso'
import Encabezado from '../components/Encabezado'
import Indicador from '../components/Indicador'
import TituloTarjeta from '../components/TituloTarjeta'
import { aCentimos } from '../utils/carrito'
import { erroresDeApi } from '../utils/errores'
import {
  formatearCantidad,
  formatearCentimos,
  formatearComprobante,
  formatearHora,
  formatearSoles,
} from '../utils/formato'

export default function MisVentas() {
  const [version, setVersion] = useState(0)
  const [resultado, setResultado] = useState({ version: null, ventas: [], error: '' })
  const [abierta, setAbierta] = useState(null)
  const [mensaje, setMensaje] = useState(null)

  useEffect(() => {
    let activo = true
    listarVentasTurno().then(
      (ventas) => {
        if (activo) setResultado({ version, ventas, error: '' })
      },
      (falla) => {
        if (activo) {
          setResultado({ version, ventas: [], error: erroresDeApi(falla).general || 'No se pudieron cargar las ventas.' })
        }
      },
    )
    return () => {
      activo = false
    }
  }, [version])

  const cargando = resultado.version !== version
  const { ventas, error } = resultado
  const completadas = ventas.filter((venta) => venta.estado === 'COMPLETADA')
  const total = completadas.reduce((suma, venta) => suma + aCentimos(venta.total), 0)

  async function anular(venta) {
    const motivo = window.prompt(`Motivo de la anulación de ${formatearComprobante(venta)}:`)
    if (motivo === null) return
    try {
      await anularVenta(venta.id_venta, motivo)
      setMensaje({ tipo: 'ok', texto: `Venta ${formatearComprobante(venta)} anulada.` })
      setVersion((previa) => previa + 1)
    } catch (falla) {
      const { general, campos } = erroresDeApi(falla)
      setMensaje({ tipo: 'error', texto: general || Object.values(campos)[0] || 'No se pudo anular la venta.' })
    }
  }

  return (
    <>
      <Encabezado kicker="Ventas" titulo="Mis ventas" subtitulo="Ventas que registraste en tu turno actual.">
        <Indicador etiqueta="Ventas" valor={cargando ? '—' : completadas.length} />
        <Indicador etiqueta="Total" valor={cargando ? '—' : formatearCentimos(total)} />
      </Encabezado>

      <main className="contenido">
        {mensaje && (
          <Aviso tipo={mensaje.tipo} alCerrar={() => setMensaje(null)}>
            {mensaje.texto}
          </Aviso>
        )}

        <section className="tarjeta">
          <TituloTarjeta titulo="Ventas del turno" detalle={cargando ? '' : `${ventas.length} ${ventas.length === 1 ? 'registro' : 'registros'}`} />

          {error ? (
            <div className="vacio">
              <Aviso tipo="error">{error}</Aviso>
            </div>
          ) : cargando && ventas.length === 0 ? (
            <p className="vacio">Cargando ventas…</p>
          ) : ventas.length === 0 ? (
            <div className="vacio">
              <h2>Aún no tienes ventas</h2>
              <p>Las ventas que confirmes aparecerán aquí.</p>
              <Link to="/nueva-venta" className="boton boton--principal">
                Ir a nueva venta
              </Link>
            </div>
          ) : (
            <div className="tabla-contenedor">
              <table className="tabla">
                <thead>
                  <tr>
                    <th>Comprobante</th>
                    <th>Hora</th>
                    <th className="col-opcional">Cliente</th>
                    <th>Pago</th>
                    <th className="num">Total</th>
                    <th>Estado</th>
                    <th className="acciones">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {ventas.map((venta) => (
                    <Fragment key={venta.id_venta}>
                      <tr className={venta.estado === 'ANULADA' ? 'inactiva' : ''}>
                        <td>
                          <strong className="codigo">{formatearComprobante(venta)}</strong>
                          <small className="tabla__detalle">{venta.tipo_comprobante_nombre}</small>
                        </td>
                        <td>{formatearHora(venta.fecha_hora)}</td>
                        <td className="col-opcional">{venta.cliente_nombre ?? '—'}</td>
                        <td>{venta.pagos.map((pago) => pago.metodo_nombre).join(', ')}</td>
                        <td className="num">{formatearSoles(venta.total)}</td>
                        <td>
                          <span
                            className={`etiqueta ${venta.estado === 'ANULADA' ? 'etiqueta--agotado' : 'etiqueta--activo'}`}
                          >
                            {venta.estado === 'ANULADA' ? 'Anulada' : 'Completada'}
                          </span>
                        </td>
                        <td className="acciones">
                          <div className="tabla__acciones">
                            <button
                              type="button"
                              className="boton boton--secundario boton--chico"
                              onClick={() => setAbierta(abierta === venta.id_venta ? null : venta.id_venta)}
                              aria-expanded={abierta === venta.id_venta}
                            >
                              {abierta === venta.id_venta ? 'Ocultar' : 'Ver detalle'}
                            </button>
                            {venta.estado === 'COMPLETADA' && (
                              <button
                                type="button"
                                className="boton boton--secundario boton--chico boton--peligro"
                                onClick={() => anular(venta)}
                              >
                                Anular
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                      {abierta === venta.id_venta && (
                        <tr className="detalle">
                          <td colSpan={7}>
                            <ul className="detalle__lista">
                              {venta.detalles.map((detalle) => (
                                <li key={detalle.id_detalle}>
                                  <span>
                                    {formatearCantidad(detalle.cantidad)} × {detalle.producto_nombre}
                                  </span>
                                  <span>{formatearSoles(detalle.subtotal)}</span>
                                </li>
                              ))}
                            </ul>
                            <p className="detalle__pie">
                              Op. gravada {formatearSoles(venta.subtotal)} · IGV {formatearSoles(venta.igv)}
                              {venta.pagos.map((pago) =>
                                Number(pago.vuelto) > 0 ? ` · Vuelto ${formatearSoles(pago.vuelto)}` : '',
                              )}
                              {venta.pagos.map((pago) =>
                                pago.codigo_operacion ? ` · Operación ${pago.codigo_operacion}` : '',
                              )}
                              {venta.motivo_anulacion && ` · Anulada: ${venta.motivo_anulacion}`}
                            </p>
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </>
  )
}
