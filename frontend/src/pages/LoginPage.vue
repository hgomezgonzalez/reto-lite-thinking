<template>
  <q-layout view="lHh lpr lFf">
    <q-page-container>
      <q-page class="flex flex-center bg-gradient">
        <q-card class="login-card q-pa-lg shadow-10">
          <q-card-section class="text-center">
            <q-icon name="hub" size="48px" color="primary" />
            <div class="text-h5 text-weight-bold text-primary q-mt-sm">Lite Thinking</div>
            <div class="text-subtitle2 text-grey-7">Reto Tecnico - Iniciar Sesion</div>
          </q-card-section>

          <q-card-section>
            <q-form @submit="onSubmit">
              <q-input
                v-model="email"
                type="email"
                label="Correo"
                outlined
                lazy-rules
                :rules="[v => !!v || 'El correo es obligatorio']"
              >
                <template #prepend><q-icon name="mail" /></template>
              </q-input>

              <q-input
                v-model="password"
                :type="showPwd ? 'text' : 'password'"
                label="Contrasena"
                outlined
                class="q-mt-md"
                lazy-rules
                :rules="[v => !!v || 'La contrasena es obligatoria']"
              >
                <template #prepend><q-icon name="lock" /></template>
                <template #append>
                  <q-icon
                    :name="showPwd ? 'visibility_off' : 'visibility'"
                    class="cursor-pointer"
                    @click="showPwd = !showPwd"
                  />
                </template>
              </q-input>

              <q-btn
                type="submit"
                label="Ingresar"
                color="primary"
                class="full-width q-mt-lg"
                size="md"
                :loading="loading"
                unelevated
              />
            </q-form>
          </q-card-section>

          <q-card-section class="q-pt-none">
            <q-banner dense class="bg-blue-1 text-grey-8 rounded-borders">
              <div class="text-caption">
                <div><strong>Demo Admin:</strong> admin@litethinking.com / Admin123*</div>
                <div><strong>Demo Externo:</strong> externo@litethinking.com / Externo123*</div>
              </div>
            </q-banner>
          </q-card-section>

          <q-separator />
          <q-card-section class="text-center text-caption text-grey-6">
            Hugo Ferney Gomez Gonzalez &middot; hgomezgonzalez@gmail.com &middot; +57 3168343318
          </q-card-section>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import { useAuthStore } from '@/stores/auth'

const email = ref('admin@litethinking.com')
const password = ref('Admin123*')
const showPwd = ref(false)
const loading = ref(false)

const auth = useAuthStore()
const router = useRouter()
const $q = useQuasar()

async function onSubmit () {
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    $q.notify({ type: 'positive', message: 'Bienvenido ' + auth.nombre })
    router.push('/empresas')
  } catch (err) {
    $q.notify({ type: 'negative', message: err.response?.data?.message || 'Credenciales invalidas' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.bg-gradient {
  background: linear-gradient(135deg, #0D47A1 0%, #1565C0 50%, #00ACC1 100%);
}
.login-card {
  width: 420px;
  max-width: 92vw;
  border-radius: 16px;
}
</style>
