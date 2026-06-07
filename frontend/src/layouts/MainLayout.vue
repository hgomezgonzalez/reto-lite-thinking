<template>
  <q-layout view="hHh lpR fFf">
    <!-- Encabezado corporativo -->
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="drawer = !drawer" />
        <q-toolbar-title class="row items-center no-wrap">
          <q-icon name="hub" size="28px" class="q-mr-sm" />
          <span class="text-weight-bold">Lite Thinking</span>
          <span class="text-caption q-ml-sm gt-xs">Reto Tecnico</span>
        </q-toolbar-title>

        <q-chip dense color="white" text-color="primary" :icon="auth.isAdmin ? 'shield_person' : 'person'">
          {{ auth.nombre }} ({{ auth.isAdmin ? 'Admin' : 'Externo' }})
        </q-chip>
        <q-btn flat dense icon="logout" label="Salir" class="q-ml-sm" @click="logout" />
      </q-toolbar>
    </q-header>

    <!-- Menu lateral -->
    <q-drawer v-model="drawer" show-if-above bordered :width="240">
      <q-list padding>
        <q-item-label header class="text-grey-7">Menu</q-item-label>
        <q-item clickable v-ripple :to="{ name: 'empresas' }" exact>
          <q-item-section avatar><q-icon name="business" /></q-item-section>
          <q-item-section>Empresas</q-item-section>
        </q-item>
        <q-item v-if="auth.isAdmin" clickable v-ripple :to="{ name: 'productos' }" exact>
          <q-item-section avatar><q-icon name="inventory_2" /></q-item-section>
          <q-item-section>Productos</q-item-section>
        </q-item>
        <q-item clickable v-ripple :to="{ name: 'inventario' }" exact>
          <q-item-section avatar><q-icon name="warehouse" /></q-item-section>
          <q-item-section>Inventario</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>

    <!-- Pie de pagina con los datos del autor -->
    <q-footer class="bg-dark text-grey-4">
      <q-toolbar class="justify-center text-center">
        <div class="text-caption">
          <q-icon name="badge" size="16px" class="q-mr-xs" />
          <strong>Hugo Ferney Gomez Gonzalez</strong>
          <span class="q-mx-sm">|</span>
          <q-icon name="mail" size="16px" class="q-mr-xs" /> hgomezgonzalez@gmail.com
          <span class="q-mx-sm">|</span>
          <q-icon name="call" size="16px" class="q-mr-xs" /> +57 3168343318
        </div>
      </q-toolbar>
    </q-footer>
  </q-layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const drawer = ref(false)
const auth = useAuthStore()
const router = useRouter()

function logout () {
  auth.logout()
  router.push('/login')
}
</script>
