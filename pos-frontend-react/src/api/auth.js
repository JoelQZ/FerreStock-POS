import { auth } from './cliente'

export async function iniciarSesion(usuario, password) {
  const { data } = await auth.post('/auth/login/', { usuario, password })
  return data
}
