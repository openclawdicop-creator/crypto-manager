import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: './', // Garante que assets sejam importados usando caminhos relativos
  server: {
    host: 'localhost',
    port: 5173,
    strictPort: true,
    allowedHosts: ['localhost', '127.0.0.1', 'openclaw-dicop.duckdns.org']
  }
})
