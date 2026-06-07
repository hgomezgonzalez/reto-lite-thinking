<template>
  <q-page class="q-pa-md">
    <div class="row items-center q-mb-md">
      <div class="text-h5 text-primary text-weight-bold">
        <q-icon name="business" class="q-mr-sm" />Empresas
      </div>
      <q-space />
      <q-btn
        v-if="auth.isAdmin"
        color="primary"
        icon="add"
        label="Nueva empresa"
        unelevated
        @click="openCreate"
      />
    </div>

    <q-banner v-if="!auth.isAdmin" dense class="bg-blue-1 text-grey-8 q-mb-md rounded-borders">
      <template #avatar><q-icon name="visibility" color="primary" /></template>
      Vista de visitante: puede consultar las empresas en modo solo lectura.
    </q-banner>

    <q-table
      :rows="empresas"
      :columns="columns"
      row-key="nit"
      :loading="loading"
      flat
      bordered
      :rows-per-page-options="[10, 25, 50]"
    >
      <template #body-cell-acciones="props">
        <q-td :props="props" class="text-right">
          <q-btn dense flat round color="secondary" icon="edit" @click="openEdit(props.row)" />
          <q-btn dense flat round color="negative" icon="delete" @click="confirmDelete(props.row)" />
        </q-td>
      </template>
    </q-table>

    <!-- Dialogo de creacion / edicion (solo admin) -->
    <q-dialog v-model="dialog">
      <q-card style="min-width: 420px">
        <q-card-section class="text-h6 text-primary">
          {{ editing ? 'Editar empresa' : 'Nueva empresa' }}
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-form @submit="save">
            <q-input
              v-model="form.nit"
              label="NIT (llave primaria)"
              outlined
              dense
              :disable="editing"
              :rules="[v => !!v || 'El NIT es obligatorio']"
            />
            <q-input v-model="form.nombre" label="Nombre" outlined dense class="q-mt-sm"
              :rules="[v => !!v || 'El nombre es obligatorio']" />
            <q-input v-model="form.direccion" label="Direccion" outlined dense class="q-mt-sm" />
            <q-input v-model="form.telefono" label="Telefono" outlined dense class="q-mt-sm" />
            <div class="row justify-end q-mt-md q-gutter-sm">
              <q-btn flat label="Cancelar" v-close-popup />
              <q-btn type="submit" color="primary" label="Guardar" :loading="saving" unelevated />
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
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const $q = useQuasar()

const empresas = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
const form = ref({ nit: '', nombre: '', direccion: '', telefono: '' })

const baseColumns = [
  { name: 'nit', label: 'NIT', field: 'nit', align: 'left', sortable: true },
  { name: 'nombre', label: 'Nombre', field: 'nombre', align: 'left', sortable: true },
  { name: 'direccion', label: 'Direccion', field: 'direccion', align: 'left' },
  { name: 'telefono', label: 'Telefono', field: 'telefono', align: 'left' }
]
const columns = auth.isAdmin
  ? [...baseColumns, { name: 'acciones', label: 'Acciones', field: 'acciones', align: 'right' }]
  : baseColumns

async function load () {
  loading.value = true
  try {
    const { data } = await api.get('/empresas')
    empresas.value = data
  } catch (err) {
    $q.notify({ type: 'negative', message: 'No se pudieron cargar las empresas' })
  } finally {
    loading.value = false
  }
}

function openCreate () {
  editing.value = false
  form.value = { nit: '', nombre: '', direccion: '', telefono: '' }
  dialog.value = true
}

function openEdit (row) {
  editing.value = true
  form.value = { ...row }
  dialog.value = true
}

async function save () {
  saving.value = true
  try {
    if (editing.value) {
      await api.put(`/empresas/${form.value.nit}`, form.value)
    } else {
      await api.post('/empresas', form.value)
    }
    $q.notify({ type: 'positive', message: 'Empresa guardada' })
    dialog.value = false
    await load()
  } catch (err) {
    $q.notify({ type: 'negative', message: err.response?.data?.message || 'Error al guardar' })
  } finally {
    saving.value = false
  }
}

function confirmDelete (row) {
  $q.dialog({
    title: 'Eliminar empresa',
    message: `Eliminar la empresa ${row.nombre} (${row.nit})?`,
    cancel: true,
    persistent: true
  }).onOk(async () => {
    try {
      await api.delete(`/empresas/${row.nit}`)
      $q.notify({ type: 'positive', message: 'Empresa eliminada' })
      await load()
    } catch (err) {
      $q.notify({ type: 'negative', message: err.response?.data?.message || 'Error al eliminar' })
    }
  })
}

onMounted(load)
</script>
