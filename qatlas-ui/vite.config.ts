import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      // Dev-time only: forwards /rs/* to the backend running in docker so the
      // browser only ever talks to one origin (avoids CORS entirely in dev).
      '/rs': {
        target: process.env.VITE_DEV_BACKEND_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
      // The backend serves attachment files itself as static resources
      // (spring.web.resources.static-locations -> app.data.root-path), so
      // this points at the same backend as /rs, not a separate service.
      '/attachments': {
        target: process.env.VITE_DEV_BACKEND_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    sourcemap: false,
    // keep vendor chunks separate so a UI code change doesn't bust the cache
    // for the (large, rarely-changing) library bundle
    rollupOptions: {
      output: {
        manualChunks(id: string) {
          if (id.includes('node_modules')) {
            if (id.includes('react-dom') || id.includes('/react/') || id.includes('react-router')) return 'vendor';
            if (id.includes('@tanstack/react-query')) return 'query';
            if (id.includes('recharts')) return 'charts';
          }
        },
      },
    },
  },
});
