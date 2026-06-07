import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import MainLayout from '@/layouts/MainLayout.vue'
import LoginPage from '@/pages/LoginPage.vue'
import EmpresasPage from '@/pages/EmpresasPage.vue'
import ProductosPage from '@/pages/ProductosPage.vue'
import InventarioPage from '@/pages/InventarioPage.vue'

const routes = [
  { path: '/login', name: 'login', component: LoginPage },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/empresas' },
      { path: 'empresas', name: 'empresas', component: EmpresasPage },
      // Productos e Inventario son funciones del administrador.
      { path: 'productos', name: 'productos', component: ProductosPage, meta: { adminOnly: true } },
      { path: 'inventario', name: 'inventario', component: InventarioPage }
    ],
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Guard global: protege rutas autenticadas y las exclusivas de administrador.
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isAuthenticated) {
    return { path: '/login' }
  }
  if (to.meta.adminOnly && !auth.isAdmin) {
    return { path: '/empresas' }
  }
  if (to.path === '/login' && auth.isAuthenticated) {
    return { path: '/empresas' }
  }
  return true
})

export default router
