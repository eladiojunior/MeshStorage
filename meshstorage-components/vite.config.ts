import { defineConfig } from 'vite';

export default defineConfig({
    build: {
        lib: {
            entry: 'src/upload/upload.ts',
            name: 'MeshStorageUpload',
            formats: ['es'],
            fileName: () => 'meshstorage-upload.js'
        },
        rollupOptions: {
            // sem dependências externas
        },
        target: 'es2020'
    },
    server: {
        port: 5173,
        open: true
    }
});