<template>
  <q-page class="q-pa-md">
    <div class="row items-center q-mb-md">
      <div class="text-h5 text-primary text-weight-bold">
        <q-icon name="warehouse" class="q-mr-sm" />Inventario
      </div>
      <q-space />
      <q-btn color="secondary" icon="picture_as_pdf" label="Descargar PDF"
        unelevated class="q-mr-sm" :loading="downloading" @click="downloadPdf" />
      <q-btn color="primary" icon="mail" label="Enviar PDF a correo" unelevated @click="emailDialog = true" />
    </div>

    <q-select
      v-model="empresaFiltro"
      :options="empresaOptions"
      label="Filtrar por empresa"
      outlined dense emit-value map-options clearable
      class="q-mb-md"
      style="max-width: 360px"
      @update:model-value="load"
    />

    <q-table
      :rows="inventario"
      :columns="columns"
      row-key="id"
      :loading="loading"
      flat
      bordered
    >
      <template #body-cell-precios="props">
        <q-td :props="props">
          <q-chip v-for="(valor, moneda) in props.row.precios" :key="moneda"
            dense size="sm" color="blue-1" text-color="primary">
            {{ moneda }} {{ formatNumber(valor) }}
          </q-chip>
        </q-td>
      </template>
    </q-table>

    <!-- Dialogo de envio por correo -->
    <q-dialog v-model="emailDialog">
      <q-card style="min-width: 400px">
        <q-card-section class="text-h6 text-primary">Enviar inventario por correo</q-card-section>
        <q-separator />
        <q-card-section>
          <q-form @submit="sendEmail">
            <q-input v-model="email" type="email" label="Correo destino" outlined dense
              :rules="[v => !!v || 'El correo es obligatorio']">
              <template #prepend><q-icon name="mail" /></template>
            </q-input>
            <div class="text-caption text-grey-7 q-mt-sm">
              Se enviara el PDF del inventario {{ empresaFiltro ? 'de la empresa seleccionada' : 'de todas las empresas' }}.
            </div>
            <div class="row justify-end q-mt-md q-gutter-sm">
              <q-btn flat label="Cancelar" v-close-popup />
              <q-btn type="submit" color="primary" label="Enviar" :loading="sending" unelevated />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import api from '@/services/api'

const $q = useQuasar()

const inventario = ref([])
const empresaOptions = ref([])
const empresaFiltro = ref(null)
const loading = ref(false)
const downloading = ref(false)
const emailDialog = ref(false)
const email = ref('')
const sending = ref(false)

const columns = [
  { name: 'empresaNombre', label: 'Empresa', field: 'empresaNombre', align: 'left', sortable: true },
  { name: 'productoCodigo', label: 'Codigo', field: 'productoCodigo', align: 'left' },
  { name: 'productoNombre', label: 'Producto', field: 'productoNombre', align: 'left', sortable: true },
  { name: 'cantidad', label: 'Cantidad', field: 'cantidad', align: 'center', sortable: true },
  { name: 'precios', label: 'Precios', field: 'precios', align: 'left' }
]

function formatNumber (v) {
  return new Intl.NumberFormat('es-CO').format(v)
}

function queryParams () {
  return empresaFiltro.value ? { params: { empresaNit: empresaFiltro.value } } : {}
}

async function load () {
  loading.value = true
  try {
    const [inv, emp] = await Promise.all([
      api.get('/inventario', queryParams()),
      empresaOptions.value.length ? Promise.resolve({ data: null }) : api.get('/empresas')
    ])
    inventario.value = inv.data
    if (emp.data) {
      empresaOptions.value = emp.data.map(e => ({ label: `${e.nombre} (${e.nit})`, value: e.nit }))
    }
  } catch (err) {
    $q.notify({ type: 'negative', message: 'No se pudo cargar el inventario' })
  } finally {
    loading.value = false
  }
}

async function downloadPdf () {
  downloading.value = true
  try {
    const response = await api.get('/inventario/pdf', { ...queryParams(), responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'inventario.pdf')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    $q.notify({ type: 'negative', message: 'No se pudo descargar el PDF' })
  } finally {
    downloading.value = false
  }
}

async function sendEmail () {
  sending.value = true
  try {
    await api.post('/inventario/pdf/email', { to: email.value }, queryParams())
    $q.notify({ type: 'positive', message: 'Correo enviado a ' + email.value })
    emailDialog.value = false
    email.value = ''
  } catch (err) {
    $q.notify({ type: 'negative', message: err.response?.data?.message || 'No se pudo enviar el correo' })
  } finally {
    sending.value = false
  }
}

onMounted(load)
</script>
