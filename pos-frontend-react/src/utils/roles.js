export const ROLES_POS = ['CAJERO', 'ADMINISTRADOR', 'DUENO']

export const NOMBRE_ROL = {
  ADMINISTRADOR: 'Admin',
  DUENO: 'Dueño',
  CAJERO: 'Cajero',
}

export function normalizarRol(rol) {
  const nombre = typeof rol === 'object' ? rol?.nombre : rol
  return (nombre ?? '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toUpperCase()
}
