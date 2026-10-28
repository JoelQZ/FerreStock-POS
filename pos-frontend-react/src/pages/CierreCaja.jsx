import { useEffect, useState } from 'react'
import { abrirTurno, cerrarTurno, obtenerResumenTurno } from '../api/caja'
import Aviso from '../components/Aviso'
import Campo from '../components/Campo'
import Encabezado from '../components/Encabezado'
import Indicador from '../components/Indicador'
import TituloTarjeta from '../components/TituloTarjeta'
import { useAuth } from '../context/useAuth'
import { aCentimos } from '../utils/carrito'
import { erroresDeApi } from '../utils/errores'
import { formatearCentimos, formatearHora, formatearSoles } from '../utils/formato'
import { useTurno } from '../utils/useTurno'

function textoDiferencia(centimos) {
  if (centimos === 0) return 'Cuadrada'
  return centimos > 0 ? 'Sobrante' : 'Faltante'
}

export default function CierreCaja() {
  const { usuario } = useAuth()
  const { turno, cargado, recargar } = useTurno()
  const [resumen, setResumen] = useState(null)
  const [cerrado, setCerrado] = useState(null)
  const [montoInicial, setMontoInicial] = useState('')
  const [montoReal, setMontoReal] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!turno) return
    let activo = true
    obtenerResumenTurno(turno.id_turno).then((datos) => {
      if (activo) setResumen(datos)
    })
    return () => {
      activo = false
    }
  }, [turno])

  async function abrir(evento) {
    evento.preventDefault()
    if (montoInicial === '' || Number(montoInicial) < 0) return setError('Ingresa el monto inicial de la caja.')
    setEnviando(true)
    setError('')
    try {
      await abrirTurno(montoInicial, usuario)
      setMontoInicial('')
      setCerrado(null)
      recargar()
    } catch (falla) {
      setError(erroresDeApi(falla).general || 'No se pudo abrir el turno.')
    } finally {
      setEnviando(false)
    }
  }

  async function cerrar(evento) {
    evento.preventDefault()
    if (montoReal === '' || Number(montoReal) < 0) return setError('Ingresa el efectivo contado en caja.')
    if (!window.confirm('¿Cerrar el turno? Ya no podrás registrar ventas en él.')) return
    setEnviando(true)
    setError('')
    try {
      const datos = await cerrarTurno(montoReal)
      setCerrado(datos)
      setResumen(null)
      setMontoReal('')
      recargar()
    } catch (falla) {
      setError(erroresDeApi(falla).general || 'No se pudo cerrar el turno.')
    } finally {
      setEnviando(false)
    }
  }

  const datos = turno ? resumen : cerrado
  const diferencia = turno && montoReal !== '' && resumen ? aCentimos(montoReal) - aCentimos(resumen.monto_esperado) : null

  return (
    <>
      <Encabezado
        kicker="Caja"
        titulo="Cierre de caja"
        subtitulo="Abre tu turno al empezar y ciérralo contando el efectivo al terminar."
      >
        <Indicador etiqueta="Turno" valor={!cargado ? '—' : turno ? 'Abierto' : 'Cerrado'} />
        <Indicador etiqueta="Apertura" valor={turno ? formatearHora(turno.apertura) : '—'} />
      </Encabezado>

      <main className="contenido">
        {error && (
          <Aviso tipo="error" alCerrar={() => setError('')}>
            {error}
          </Aviso>
        )}
        {cerrado && (
          <Aviso tipo={Number(cerrado.turno.diferencia) === 0 ? 'ok' : 'error'}>
            Turno cerrado. {textoDiferencia(aCentimos(cerrado.turno.diferencia))}:{' '}
            {formatearSoles(Math.abs(Number(cerrado.turno.diferencia)))}.
          </Aviso>
        )}

        {datos && (
          <section className="kpis">
            <div className="tarjeta kpi">
              <span>Ventas</span>
              <strong>{datos.ventas}</strong>
            </div>
            <div className="tarjeta kpi">
              <span>Total vendido</span>
              <strong>{formatearSoles(datos.ventas_total)}</strong>
            </div>
            <div className="tarjeta kpi">
              <span>Efectivo</span>
              <strong>{formatearSoles(datos.efectivo_ventas)}</strong>
            </div>
            <div className="tarjeta kpi">
              <span>Yape y Plin</span>
              <strong>{formatearSoles(datos.otros_pagos)}</strong>
            </div>
          </section>
        )}

        {cargado && !turno && (
          <form className="tarjeta caja" onSubmit={abrir} noValidate>
            <TituloTarjeta titulo="Abrir turno" />
            <div className="caja__cuerpo">
              <Campo etiqueta="Monto inicial en caja" obligatorio ayuda="Efectivo con el que empiezas el turno.">
                <div className="con-prefijo">
                  <span>S/</span>
                  <input
                    className="entrada"
                    type="number"
                    min="0"
                    step="0.10"
                    inputMode="decimal"
                    value={montoInicial}
                    onChange={(evento) => setMontoInicial(evento.target.value)}
                  />
                </div>
              </Campo>
            </div>
            <div className="formulario__acciones">
              <button type="submit" className="boton boton--principal" disabled={enviando}>
                {enviando ? 'Abriendo…' : 'Abrir turno'}
              </button>
            </div>
          </form>
        )}

        {turno && resumen && (
          <form className="tarjeta caja" onSubmit={cerrar} noValidate>
            <TituloTarjeta titulo="Cerrar turno" detalle={`Turno #${turno.id_turno}`} />
            <div className="caja__cuerpo">
              <dl className="totales totales--caja">
                <div>
                  <dt>Monto inicial</dt>
                  <dd>{formatearSoles(turno.monto_inicial)}</dd>
                </div>
                <div>
                  <dt>Ventas en efectivo</dt>
                  <dd>{formatearSoles(resumen.efectivo_ventas)}</dd>
                </div>
                <div className="totales__total">
                  <dt>Efectivo esperado</dt>
                  <dd>{formatearSoles(resumen.monto_esperado)}</dd>
                </div>
              </dl>
              <Campo etiqueta="Efectivo contado" obligatorio>
                <div className="con-prefijo">
                  <span>S/</span>
                  <input
                    className="entrada"
                    type="number"
                    min="0"
                    step="0.10"
                    inputMode="decimal"
                    value={montoReal}
                    onChange={(evento) => setMontoReal(evento.target.value)}
                  />
                </div>
              </Campo>
              {diferencia !== null && (
                <div className={`vuelto${diferencia < 0 ? ' vuelto--faltante' : ''}`}>
                  <span>{textoDiferencia(diferencia)}</span>
                  <strong>{formatearCentimos(Math.abs(diferencia))}</strong>
                </div>
              )}
            </div>
            <div className="formulario__acciones">
              <button type="submit" className="boton boton--principal" disabled={enviando}>
                {enviando ? 'Cerrando…' : 'Cerrar turno'}
              </button>
            </div>
          </form>
        )}
      </main>
    </>
  )
}
