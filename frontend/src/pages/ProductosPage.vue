<template>
  <q-page class="q-pa-md">
    <div class="row items-center q-mb-md">
      <div class="text-h5 text-primary text-weight-bold">
        <q-icon name="inventory_2" class="q-mr-sm" />Productos
      </div>
      <q-space />
      <q-btn color="primary" icon="add" label="Nuevo producto" unelevated @click="openCreate" />
    </div>

    <q-table
      :rows="productos"
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
      <template #body-cell-categorias="props">
        <q-td :props="props">
          <q-chip v-for="c in props.row.categorias" :key="c.id" dense size="sm"
            color="grey-3" text-color="grey-9">{{ c.nombre }}</q-chip>
        </q-td>
      </template>
      <template #body-cell-acciones="props">
        <q-td :props="props" class="text-right">
          <q-btn dense flat round color="secondary" icon="edit" @click="openEdit(props.row)" />
          <q-btn dense flat round color="negative" icon="delete" @click="confirmDelete(props.row)" />
        </q-td>
      </template>
    </q-table>

    <q-dialog v-model="dialog">
      <q-card style="min-width: 480px">
        <q-card-section class="text-h6 text-primary">
          {{ editing ? 'Editar producto' : 'Nuevo producto' }}
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-form @submit="save">
            <q-input v-model="form.codigo" label="Codigo" outlined dense
              :rules="[v => !!v || 'El codigo es obligatorio']" />
            <q-input v-model="form.nombre" label="Nombre del producto" outlined dense class="q-mt-sm"
              :rules="[v => !!v || 'El nombre es obligatorio']" />
            <q-input v-model="form.caracteristicas" label="Caracteristicas" outlined dense
              type="textarea" autogrow class="q-mt-sm" />
            <div class="row q-col-gutter-sm q-mt-xs">
              <div class="col">
                <q-input v-model.number="form.precioBase" label="Precio base" type="number" step="0.01"
                  outlined dense :rules="[v => v > 0 || 'Precio mayor a cero']" />
              </div>
              <div class="col">
                <q-select v-model="form.monedaBase" :options="['COP', 'USD', 'EUR']"
                  label="Moneda base" outlined dense />
              </div>
            </div>
            <q-select v-model="form.empresaNit" :options="empresaOptions" label="Empresa"
              outlined dense emit-value map-options class="q-mt-sm"
              :rules="[v => !!v || 'Seleccione una empresa']" />
            <q-select v-model="form.categoriaIds" :options="categoriaOptions" label="Categorias"
              outlined dense multiple emit-value map-options use-chips class="q-mt-sm" />
            <q-input v-model.number="form.cantidadInventario" label="Cantidad inicial en inventario"
              type="number" outlined dense class="q-mt-sm" hint="Se guarda en la tabla inventario" />
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

const $q = useQuasar()

const productos = ref([])
const empresaOptions = ref([])
const categoriaOptions = ref([])
const loading = ref(false)
const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
const form = ref(emptyForm())

const columns = [
  { name: 'codigo', label: 'Codigo', field: 'codigo', align: 'left', sortable: true },
  { name: 'nombre', label: 'Producto', field: 'nombre', align: 'left', sortable: true },
  { name: 'empresaNombre', label: 'Empresa', field: 'empresaNombre', align: 'left' },
  { name: 'categorias', label: 'Categorias', field: 'categorias', align: 'left' },
  { name: 'precios', label: 'Precios', field: 'precios', align: 'left' },
  { name: 'acciones', label: 'Acciones', field: 'acciones', align: 'right' }
]

function emptyForm () {
  return {
    id: null, codigo: '', nombre: '', caracteristicas: '',
    precioBase: null, monedaBase: 'COP', empresaNit: null,
    categoriaIds: [], cantidadInventario: null
  }
}

function formatNumber (v) {
  return new Intl.NumberFormat('es-CO').format(v)
}

async function load () {
  loading.value = true
  try {
    const [prod, emp, cat] = await Promise.all([
      api.get('/productos'),
      api.get('/empresas'),
      api.get('/categorias')
    ])
    productos.value = prod.data
    empresaOptions.value = emp.data.map(e => ({ label: `${e.nombre} (${e.nit})`, value: e.nit }))
    categoriaOptions.value = cat.data.map(c => ({ label: c.nombre, value: c.id }))
  } catch (err) {
    $q.notify({ type: 'negative', message: 'No se pudieron cargar los datos' })
  } finally {
    loading.value = false
  }
}

function openCreate () {
  editing.value = false
  form.value = emptyForm()
  dialog.value = true
}

function openEdit (row) {
  editing.value = true
  form.value = {
    id: row.id,
    codigo: row.codigo,
    nombre: row.nombre,
    caracteristicas: row.caracteristicas,
    precioBase: row.precioBase,
    monedaBase: row.monedaBase,
    empresaNit: row.empresaNit,
    categoriaIds: row.categorias.map(c => c.id),
    cantidadInventario: null
  }
  dialog.value = true
}

async function save () {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (editing.value) {
      await api.put(`/productos/${form.value.id}`, payload)
    } else {
      await api.post('/productos', payload)
    }
    $q.notify({ type: 'positive', message: 'Producto guardado' })
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
    title: 'Eliminar producto',
    message: `Eliminar el producto ${row.nombre}?`,
    cancel: true,
    persistent: true
  }).onOk(async () => {
    try {
      await api.delete(`/productos/${row.id}`)
      $q.notify({ type: 'positive', message: 'Producto eliminado' })
      await load()
    } catch (err) {
      $q.notify({ type: 'negative', message: err.response?.data?.message || 'Error al eliminar' })
    }
  })
}

onMounted(load)
</script>
