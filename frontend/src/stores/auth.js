import { defineStore } from 'pinia'
import api from '@/services/api'

// Store de autenticacion: guarda el token JWT y los datos del usuario,
// los persiste en localStorage y expone getters de rol.
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.rol === 'ADMIN',
    nombre: (state) => state.user?.nombre || ''
  },

  actions: {
    async login (email, password) {
      const { data } = await api.post('/auth/login', { email, password })
      this.token = data.token
      this.user = { email: data.email, nombre: data.nombre, rol: data.rol }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return data
    },

    logout () {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
