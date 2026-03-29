import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: './', // Garante que assets sejam importados usando caminhos relativos
  server: {
    allowedHosts: ['openclaw-dicop.duckdns.org']
  }
})
