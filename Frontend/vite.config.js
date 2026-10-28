import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// El backend del módulo de trámites corre en el puerto 8080; durante las pruebas locales del
// portal se levanta en el 8100 y se puede indicar con la variable SIM_BACKEND.
const backend = process.env.SIM_BACKEND ?? "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    proxy: {
      "/api": {
        target: backend,
        changeOrigin: true,
      },
    },
  },
});
