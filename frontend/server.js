// Servidor estatico para desplegar el SPA de Quasar en Heroku.
// Sirve la carpeta dist/ y redirige todas las rutas al index.html (SPA).
import express from 'express'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const app = express()
const port = process.env.PORT || 9000
const dist = join(__dirname, 'dist')

app.use(express.static(dist))

// Fallback SPA: cualquier ruta no encontrada devuelve index.html.
app.get('*', (req, res) => {
  res.sendFile(join(dist, 'index.html'))
})

app.listen(port, () => {
  console.log(`Frontend escuchando en el puerto ${port}`)
})
