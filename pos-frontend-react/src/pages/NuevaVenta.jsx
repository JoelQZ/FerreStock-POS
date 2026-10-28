import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarCategorias, listarProductos } from '../api/catalogo'
import { listarVentasTurno, registrarVenta } from '../api/ventas'
import Aviso from '../components/Aviso'
import Carrito from '../components/Carrito'
import Encabezado from '../components/Encabezado'
import EtiquetaStock from '../components/EtiquetaStock'
import Icono from '../components/Icono'
import Indicador from '../components/Indicador'
import TituloTarjeta from '../components/TituloTarjeta'
import { useAuth } from '../context/useAuth'
import { agregarProducto, cambiarCantidad, puedeAgregar, quitarProducto } from '../utils/carrito'
import { erroresDeApi } from '../utils/errores'
import { formatearComprobante, formatearSoles, tieneStockBajo } from '../utils/formato'
import { useTurno } from '../utils/useTurno'

export default function NuevaVenta() {
  const { usuario } = useAuth()
  const { turno, cargado, recargar } = useTurno()
  const [categorias, setCategorias] = useState([])
  const [texto, setTexto] = useState('')
  const [buscar, setBuscar] = useState('')
  const [categoria, setCategoria] = useState('')
  const [version, setVersion] = useState(0)
  const [resultado, setResultado] = useState({ clave: null, productos: [], error: '' })
  const [items, setItems] = useState([])
  const [ventasHoy, setVentasHoy] = useState(0)
  const [mensaje, setMensaje] = useState(null)

  useEffect(() => {
    listarCategorias().then(setCategorias, () => setCategorias([]))
  }, [])

  useEffect(() => {
    let activo = true
    listarVentasTurno().then((ventas) => {
      if (activo) setVentasHoy(ventas.filter((venta) => venta.estado === 'COMPLETADA').length)
    })
    return () => {
      activo = false
    }
  }, [version])

  useEffect(() => {
    const espera = setTimeout(() => setBuscar(texto.trim()), 300)
    return () => clearTimeout(espera)
  }, [texto])

  const clave = `${buscar}|${categoria}|${version}`

  useEffect(() => {
    let activo = true
    listarProductos({ buscar, categoria }).then(
      (productos) => {
        if (activo) setResultado({ clave, productos, error: '' })
      },
      (falla) => {
        if (activo) {
          setResultado({
            clave,
            productos: [],
            error: erroresDeApi(falla).general || 'No se pudo cargar la lista de productos.',
          })
        }
      },
    )
    return () => {
      activo = false
    }
  }, [buscar, categoria, clave])

  const cargando = resultado.clave !== clave
  const { productos, error } = resultado
  const hayFiltros = Boolean(buscar || categoria)

  async function confirmarVenta(venta) {
    const registrada = await registrarVenta(venta, usuario)
    setItems([])
    setVersion((previa) => previa + 1)
    setMensaje({
      tipo: 'ok',
      texto: `Venta ${formatearComprobante(registrada)} registrada por ${formatearSoles(registrada.total)}.`,
    })
  }

  function limpiarFiltros() {
    setTexto('')
    setBuscar('')
    setCategoria('')
  }

  return (
    <>
      <Encabezado
        kicker="HU-05 · Punto de venta"
        titulo="Nueva venta"
        subtitulo="Busca productos, agrégalos al carrito y confirma el cobro."
      >
        <Indicador etiqueta="Turno" valor={!cargado ? '—' : turno ? 'Abierto' : 'Cerrado'} />
        <Indicador etiqueta="Ventas hoy" valor={ventasHoy} />
      </Encabezado>

      <main className="contenido">
        {cargado && !turno && (
          <Aviso tipo="error">
            No tienes un turno abierto. <Link to="/cierre-caja">Abre tu turno de caja</Link> para empezar a vender.
          </Aviso>
        )}
        {mensaje && (
          <Aviso tipo={mensaje.tipo} alCerrar={() => setMensaje(null)}>
            {mensaje.texto}
          </Aviso>
        )}

        <div className="venta">
          <section className="tarjeta">
            <TituloTarjeta titulo="Productos" detalle={cargando ? '' : `${productos.length} ${productos.length === 1 ? 'producto' : 'productos'}`} />
            <div className="herramientas">
              <div className="buscador">
                <Icono nombre="buscar" />
                <input
                  className="entrada"
                  type="search"
                  placeholder="Buscar por nombre o código…"
                  value={texto}
                  onChange={(evento) => setTexto(evento.target.value)}
                  aria-label="Buscar productos"
                />
              </div>
              <select
                className="entrada herramientas__filtro"
                value={categoria}
                onChange={(evento) => setCategoria(evento.target.value)}
                aria-label="Filtrar por categoría"
              >
                <option value="">Todas las categorías</option>
                {categorias.map((item) => (
                  <option key={item.id_categoria} value={item.id_categoria}>
                    {item.nombre}
                  </option>
                ))}
              </select>
            </div>

            {error ? (
              <div className="vacio">
                <Aviso tipo="error">{error}</Aviso>
              </div>
            ) : cargando && productos.length === 0 ? (
              <p className="vacio">Cargando productos…</p>
            ) : productos.length === 0 ? (
              <div className="vacio">
                <h2>{hayFiltros ? 'Sin resultados' : 'No hay productos'}</h2>
                <p>
                  {hayFiltros
                    ? 'Ningún producto coincide con la búsqueda o la categoría elegida.'
                    : 'El catálogo todavía no tiene productos activos.'}
                </p>
                {hayFiltros && (
                  <button type="button" className="boton boton--secundario" onClick={limpiarFiltros}>
                    Limpiar filtros
                  </button>
                )}
              </div>
            ) : (
              <div className={`tabla-contenedor${cargando ? ' tabla-contenedor--cargando' : ''}`}>
                <table className="tabla">
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Producto</th>
                      <th className="num">Precio</th>
                      <th>Stock</th>
                      <th className="acciones">
                        <span className="sr-only">Acciones</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {productos.map((producto) => (
                      <tr key={producto.id_producto}>
                        <td>
                          <span className="codigo">{producto.codigo}</span>
                        </td>
                        <td>
                          <strong className="tabla__nombre">{producto.nombre}</strong>
                          <small className="tabla__detalle">
                            {producto.categoria_nombre} · {producto.unidad_nombre}
                          </small>
                        </td>
                        <td className="num">{formatearSoles(producto.precio_venta)}</td>
                        <td>
                          <EtiquetaStock stock={producto.stock} stockBajo={tieneStockBajo(producto)} />
                        </td>
                        <td className="acciones">
                          <button
                            type="button"
                            className="boton boton--principal boton--chico"
                            onClick={() => setItems((previos) => agregarProducto(previos, producto))}
                            disabled={!puedeAgregar(items, producto)}
                          >
                            Agregar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <Carrito
            items={items}
            turnoAbierto={Boolean(turno)}
            alCambiar={(id, cantidad) => setItems((previos) => cambiarCantidad(previos, id, cantidad))}
            alQuitar={(id) => setItems((previos) => quitarProducto(previos, id))}
            alVaciar={() => setItems([])}
            alConfirmar={async (venta) => {
              await confirmarVenta(venta)
              recargar()
            }}
          />
        </div>
      </main>
    </>
  )
}
