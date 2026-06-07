import axios from 'axios'

// Cliente HTTP centralizado. La baseURL '/api' se resuelve por el proxy de
// Vite en desarrollo y por la variable VITE_API_URL en produccion.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api'
})

// Interceptor de peticion: adjunta el token JWT si existe.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
