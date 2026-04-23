import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      // 所有 /api 请求转发到后端 8080
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})