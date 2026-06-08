# 前端编码规范

## 路径别名

| 别名 | 映射目标 | 说明 |
|------|----------|------|
| `/@/` | `src/` | 项目惯例，优先使用带前导斜杠的写法 |
| `@/` | `src/` | 同上，但 `/@/` 更常用 |
| `/#/` | `types/` | TypeScript 类型声明目录 |
| `~/icons/{collection}/{name}` | unplugin-icons | 编译时图标导入，如 `~icons/mdi/home` |

## 代码格式化

### Prettier

```yaml
printWidth: 150
singleQuote: true
trailingComma: 'es5'
tabWidth: 2
endOfLine: 'auto'
vueIndentScriptAndStyle: true   # <script> 和 <style> 内缩进
htmlWhitespaceSensitivity: 'strict'
```

运行方式：
```bash
pnpm batch:prettier   # 格式化 src/ 下所有文件
```

### ESLint

- 配置：Vue3 recommended + TypeScript recommended + Prettier
- `any` 类型允许使用
- 以下划线 `_` 开头的未使用变量会被忽略
- `prettier/prettier` 规则为 `'off'`，Prettier 不通过 ESLint 强制执行，需单独运行

运行方式：
```bash
npx eslint src/path/to/file.vue    # 检查单个文件
npx stylelint "src/**/*.{vue,less,css}"  # 样式检查
```

## Git 提交规范

- 使用 Conventional Commits，由 commitlint 强制执行
- 提交类型：`feat`, `fix`, `perf`, `style`, `docs`, `test`, `refactor`, `build`, `ci`, `chore`, `revert`, `wip`, `workflow`, `types`, `release`
- 标题最大长度：108 字符

## 国际化（i18n）

- 支持语言：中文（zh-CN）、英文（en）
- 语言文件位置：`src/locales/lang/`
