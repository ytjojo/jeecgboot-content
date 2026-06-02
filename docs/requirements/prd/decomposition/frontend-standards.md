# 前端规范与组件清单

> 生成前端 PRD 时必须参考本文档，优先复用现有组件

## 技术栈

- **框架**: Vue 3 + TypeScript + Vite 6
- **UI 库**: Ant Design Vue 4（自动导入，无需手动 import）
- **状态管理**: Pinia
- **包管理器**: pnpm
- **路径别名**: `/@/` → `src/`

## 前端硬规则

1. 优先复用现有组件、hooks、store 和工具函数，避免平行造轮子
2. 统一使用 `src/api/` 目录的 API 封装（`defHttp`）
3. 涉及菜单、权限、动态路由时，先确认是否依赖后端返回
4. 路径别名优先使用 `/@/` 指向 `src/`

---

## 核心业务组件

### JVxeTable（增强表格）
- **路径**: `src/components/jeecg/JVxeTable/`
- **用途**: 业务数据表格，支持行编辑、拖拽、批量操作
- **特性**: 列配置、插槽自定义、分页、排序、筛选
- **适用场景**: 列表页、数据管理页

### OnLine（在线表单）
- **路径**: `src/components/jeecg/OnLine/`
- **用途**: 动态表单渲染，对接后端 online 模块
- **适用场景**: 动态配置的表单、低代码表单

### Form（基础表单）
- **路径**: `src/components/Form/`
- **用途**: 配置式表单，schema 驱动
- **核心 Props**: `schemas`（表单配置）、`model`（数据模型）、`labelWidth`
- **适用场景**: 新增/编辑表单、查询表单

### Table（基础表格）
- **路径**: `src/components/Table/`
- **用途**: 通用数据表格
- **核心 Props**: `api`（数据接口）、`columns`（列配置）、`pagination`
- **适用场景**: 简单列表展示

### Modal（弹窗）
- **路径**: `src/components/Modal/`
- **用途**: 模态对话框
- **适用场景**: 确认框、表单弹窗、详情弹窗

### Drawer（抽屉）
- **路径**: `src/components/Drawer/`
- **用途**: 侧边抽屉面板
- **适用场景**: 详情查看、辅助表单

---

## 基础 UI 组件

| 组件 | 路径 | 用途 |
|------|------|------|
| Button | `src/components/Button/` | 按钮，支持权限控制 |
| Icon | `src/components/Icon/` | 图标（Iconify + SVG） |
| Page | `src/components/Page/` | 页面容器 |
| Container | `src/components/Container/` | 布局容器 |
| CardList | `src/components/CardList/` | 卡片列表 |
| Description | `src/components/Description/` | 描述列表 |
| Upload | `src/components/Upload/` | 文件上传 |
| Tree | `src/components/Tree/` | 树形控件 |
| Dropdown | `src/components/Dropdown/` | 下拉菜单 |
| Loading | `src/components/Loading/` | 加载状态 |
| Markdown | `src/components/Markdown/` | Markdown 渲染 |
| CodeEditor | `src/components/CodeEditor/` | 代码编辑器 |
| Tinymce | `src/components/Tinymce/` | 富文本编辑器 |
| Qrcode | `src/components/Qrcode/` | 二维码 |
| Cropper | `src/components/Cropper/` | 图片裁剪 |
| CountTo | `src/components/CountTo/` | 数字动画 |
| StrengthMeter | `src/components/StrengthMeter/` | 密码强度 |

---

## Hooks（组合式函数）

| Hook | 路径 | 用途 |
|------|------|------|
| `useTable` | `src/hooks/component/` | 表格逻辑封装 |
| `useForm` | `src/hooks/component/` | 表单逻辑封装 |
| `useModal` | `src/hooks/component/` | 弹窗逻辑封装 |
| `useDrawer` | `src/hooks/component/` | 抽屉逻辑封装 |
| `useMessage` | `src/hooks/web/` | 消息提示 |
| `usePermission` | `src/hooks/web/` | 权限判断 |
| `useI18n` | `src/hooks/web/` | 国际化 |

---

## Store（状态管理）

| Store | 文件 | 用途 |
|-------|------|------|
| `useUserStore` | `src/store/modules/user.ts` | 用户信息、Token、角色 |
| `usePermissionStore` | `src/store/modules/permission.ts` | 路由、权限码 |
| `useAppStore` | `src/store/modules/app.ts` | 应用配置、主题 |
| `useMultipleTabStore` | `src/store/modules/multipleTab.ts` | 标签页状态 |

---

## API 封装

```typescript
// 使用方式
import { defHttp } from '/@/utils/http/axios';

// GET 请求
defHttp.get({ url: '/api/xxx', params: {} });

// POST 请求
defHttp.post({ url: '/api/xxx', data: {} });
```

**响应格式**: `{ code: 200, result: any, message: string, success: boolean }`

---

## 设计规范

### CSS 前缀
- 自定义组件: `jeecg`

### 主题色
- 主色: `--j-global-primary-color`（动态设置）
- 预设色板: `#0960bd`, `#1890ff`, `#009688`, `#536dfe` 等

### 布局
- 左侧菜单 + 顶部导航 + 多标签页
- 配置文件: `src/settings/projectSetting.ts`

---

## 典型页面模式

### 列表页模式
```
Page
  └── 查询表单 (Form + schemas)
  └── 操作按钮 (Button)
  └── 数据表格 (JVxeTable / Table)
  └── 新增/编辑弹窗 (Modal + Form)
```

### 详情页模式
```
Page
  └── 描述信息 (Description)
  └── 关联列表 (Table / CardList)
  └── 操作按钮 (Button)
```

### 表单页模式
```
Page
  └── 步骤条 (Steps) [可选]
  └── 表单 (Form + schemas)
  └── 提交/取消按钮 (Button)
```

---

## PRD 组件选型指引

| 需求场景 | 推荐组件 | 备选 |
|---------|---------|------|
| 数据列表 | JVxeTable | Table |
| 新增/编辑 | Modal + Form | Drawer + Form |
| 详情查看 | Description | Modal + 只读 Form |
| 树形选择 | Tree | TreeSelect |
| 文件上传 | Upload | - |
| 富文本 | Tinymce | Markdown |
| 代码编辑 | CodeEditor | - |
| 卡片展示 | CardList | 自定义 |
| 权限控制 | Button (auth) | usePermission |
