import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置：/user 与 /upload 代理到本地 SpringBoot 后端
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
