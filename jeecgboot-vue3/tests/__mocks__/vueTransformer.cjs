// Minimal Vue 3 SFC transformer for Jest tests.
// Compiles <script setup> + <template> using @vue/compiler-sfc,
// then converts ESM output to CommonJS using Babel.
const babel = require('@babel/core');
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc');

module.exports = {
  process(src, filename) {
    if (!filename.endsWith('.vue')) return { code: src };

    const { descriptor, errors } = parse(src, { filename });
    if (errors && errors.length) {
      throw new Error(
        '[jest-vue-transformer] parse error in ' + filename + ':\n' + errors.map(e => e.message).join('\n')
      );
    }

    const id = 'data-v-' + filename.replace(/[\\/.:]/g, '_');

    // Compile <script setup>
    let scriptCode = '';
    if (descriptor.scriptSetup || descriptor.script) {
      const scriptResult = compileScript(descriptor, {
        id,
        sourceMap: false,
        genDefaultAs: '_sfc_main',
      });
      if (scriptResult.errors && scriptResult.errors.length) {
        throw new Error(
          '[jest-vue-transformer] script error in ' + filename + ':\n' +
            scriptResult.errors.map(e => e.message || String(e)).join('\n')
        );
      }
      scriptCode = scriptResult.content;
    }

    // Compile <template>
    let templateCode = '';
    if (descriptor.template) {
      const templateResult = compileTemplate({
        source: descriptor.template.content,
        filename,
        id,
        scoped: descriptor.styles.some(s => s.scoped),
      });
      if (templateResult.errors && templateResult.errors.length) {
        throw new Error(
          '[jest-vue-transformer] template error in ' + filename + ':\n' +
            templateResult.errors.map(e => e.message || String(e)).join('\n')
        );
      }
      templateCode = templateResult.code;
    }

    // Babel: ESM → CJS. Strip TypeScript types first.
    const babelResult = babel.transformSync(scriptCode + '\n' + templateCode, {
      filename,
      sourceType: 'module',
      babelrc: false,
      configFile: false,
      presets: [
        ['@babel/preset-typescript', { isTSX: false, allExtensions: false, allowDeclareFields: true }],
      ],
      plugins: [
        '@babel/plugin-transform-modules-commonjs',
      ],
    });

    return { code: babelResult.code };
  },
  getCacheKey(src, filename) {
    return filename + '_' + (typeof src === 'string' ? src.length : 0);
  },
};
