import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [
    vue(),
    {
      name: 'virtual-svg-icons-names',
      resolveId(id) {
        if (id === 'virtual:svg-icons-names') return '\0virtual:svg-icons-names';
      },
      load(id) {
        if (id === '\0virtual:svg-icons-names') return 'export default []';
      },
    },
  ],
  resolve: {
    alias: [
      { find: /\/@\//, replacement: resolve(__dirname, 'src') + '/' },
      { find: /^@\//, replacement: resolve(__dirname, 'src') + '/' },
      { find: /\/#\//, replacement: resolve(__dirname, 'types') + '/' },
    ],
  },
  define: {
    __COLOR_PLUGIN_OUTPUT_FILE_NAME__: '""',
    __COLOR_PLUGIN_OPTIONS__: '{}',
    __PROD__: 'false',
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./tests/setup.ts'],
  },
});
