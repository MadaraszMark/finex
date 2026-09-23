import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // Fejlesztés közben a /api/... kéréseket a Vite továbbítja a Spring Boot backendnek
    // (a /api előtag nélkül), így a böngésző csak az 5173-as porttal beszél, és nincs CORS-hiba.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
