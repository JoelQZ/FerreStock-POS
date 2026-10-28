import { useCallback, useEffect, useState } from 'react'
import { obtenerTurnoActual } from '../api/caja'

export function useTurno() {
  const [estado, setEstado] = useState({ turno: null, cargado: false })
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let activo = true
    obtenerTurnoActual().then(
      (turno) => {
        if (activo) setEstado({ turno, cargado: true })
      },
      () => {
        if (activo) setEstado({ turno: null, cargado: true })
      },
    )
    return () => {
      activo = false
    }
  }, [version])

  const recargar = useCallback(() => setVersion((previa) => previa + 1), [])

  return { ...estado, recargar }
}
