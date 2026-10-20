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
  build: {
    rolldownOptions: {
      output: {
        // A nagy külső könyvtárak külön fájlokba kerülnek: kisebb a fő csomag, és egy új kiadás után
        // a böngésző a változatlan könyvtárakat a gyorsítótárból tölti be
        codeSplitting: {
          groups: [
            { name: 'react', test: /[\\/]node_modules[\\/](react|react-dom|scheduler)[\\/]/ },
            { name: 'router', test: /[\\/]node_modules[\\/]react-router[\\/]/ },
            { name: 'data', test: /[\\/]node_modules[\\/](@tanstack|axios)[\\/]/ },
            { name: 'motion', test: /[\\/]node_modules[\\/](motion|framer-motion|motion-dom|motion-utils)[\\/]/ },
            { name: 'forms', test: /[\\/]node_modules[\\/](zod|react-hook-form|@hookform)[\\/]/ },
            // A grafikonkönyvtár és a függőségei csak a grafikonos oldalakon töltődnek le
            {
              name: 'charts',
              test: /[\\/]node_modules[\\/](recharts|victory-vendor|d3-[^\\/]+|internmap|@reduxjs|react-redux|redux|immer|reselect|decimal\.js-light|eventemitter3|es-toolkit|tiny-invariant|use-sync-external-store)[\\/]/,
            },
          ],
        },
      },
    },
  },
})
