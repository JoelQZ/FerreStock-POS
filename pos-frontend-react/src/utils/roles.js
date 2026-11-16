export const ROLES_POS = ['CAJERO', 'ADMINISTRADOR']

export const NOMBRE_ROL = {
  ADMINISTRADOR: 'Admin',
  CAJERO: 'Cajero',
}

export function normalizarRol(rol) {
  const nombre = typeof rol === 'object' ? rol?.nombre : rol
  return (nombre ?? '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toUpperCase()
}
