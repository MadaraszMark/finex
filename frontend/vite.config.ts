/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  // Frontend-tesztek (npm test): Vitest jsdom környezetben
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: false,
  },
  server: {
    port: 5173,
    // A /api kéréseket a Vite az előtag nélkül továbbítja a backendnek, így nincs CORS-hiba
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
        // Nagy könyvtárak külön fájlban: kisebb fő csomag, új kiadás után gyorsítótárból töltődnek
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
