import { useCallback, useEffect, useMemo, useState } from 'react'
import { iniciarSesion } from '../api/auth'
import { guardarSesion, leerSesion, registrarExpiracion } from '../api/cliente'
import { normalizarRol, ROLES_POS } from '../utils/roles'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(() => leerSesion())

  const cerrarSesion = useCallback(() => {
    guardarSesion(null)
    setSesion(null)
  }, [])

  useEffect(() => {
    registrarExpiracion(cerrarSesion)
  }, [cerrarSesion])

  const entrar = useCallback(async (usuario, password) => {
    const datos = await iniciarSesion(usuario, password)
    const rol = normalizarRol(datos.usuario.rol)

    if (!ROLES_POS.includes(rol)) {
      const error = new Error('Tu usuario no tiene acceso al punto de venta.')
      error.codigo = 'ROL'
      throw error
    }

    const nueva = { access: datos.access, refresh: datos.refresh, usuario: { ...datos.usuario, rol } }
    guardarSesion(nueva)
    setSesion(nueva)
  }, [])

  const valor = useMemo(
    () => ({ usuario: sesion?.usuario ?? null, entrar, cerrarSesion }),
    [sesion, entrar, cerrarSesion],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
