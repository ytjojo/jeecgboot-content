import { defineConfig } from 'vitest/config';
import { resolve } from 'path';

export default defineConfig({
  resolve: {
    alias: [
      { find: /\/@\//, replacement: resolve(__dirname, 'src') + '/' },
      { find: /\/#\//, replacement: resolve(__dirname, 'types') + '/' },
    ],
  },
  test: {
    globals: true,
    environment: 'jsdom',
  },
});
