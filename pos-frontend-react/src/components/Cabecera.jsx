import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { iniciales } from '../utils/formato'
import { MENU } from '../utils/menu'
import { NOMBRE_ROL } from '../utils/roles'
import Icono from './Icono'

export default function Cabecera() {
  const { usuario, cerrarSesion } = useAuth()
  const nombre = usuario?.nombre || usuario?.usuario

  return (
    <header className="cabecera">
      <div className="marca">
        <span className="marca__logo">
          <Icono nombre="tuerca" tamano={22} />
        </span>
        <strong>
          Ferre<span>Stock</span>
        </strong>
      </div>

      <nav className="cabecera__menu" aria-label="Menú principal">
        {MENU.map((item) => (
          <NavLink key={item.ruta} to={item.ruta} className="cabecera__item">
            <Icono nombre={item.icono} tamano={16} />
            <span>{item.texto}</span>
          </NavLink>
        ))}
      </nav>

      <div className="cabecera__usuario">
        <div className="cabecera__datos">
          <strong>{nombre}</strong>
          <span>{NOMBRE_ROL[usuario?.rol] ?? usuario?.rol}</span>
        </div>
        <span className="avatar">{iniciales(nombre)}</span>
        <button type="button" className="cabecera__salir" onClick={cerrarSesion} aria-label="Cerrar sesión">
          <Icono nombre="salir" />
        </button>
      </div>
    </header>
  )
}
