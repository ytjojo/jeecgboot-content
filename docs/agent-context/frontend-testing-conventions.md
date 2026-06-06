# 前端单元测试规范

## 测试框架

- **唯一框架：Vitest**（`vitest` + `@vitest/coverage-v8`）
- Jest 为遗留配置，**禁止在新测试中使用** `jest.mock` / `jest.fn` / `jest.clearAllMocks`，统一使用 `vi.mock` / `vi.fn` / `vi.clearAllMocks`
- 运行命令：`npx vitest run`（单次）或 `npx vitest`（watch 模式）

## 测试覆盖率要求

- **行覆盖率 ≥ 90%**，低于此标准的代码不允许提交
- 重点覆盖：Pinia store actions/getters、composable 逻辑、API 封装层、组件交互逻辑
- 不计入覆盖率：纯类型定义文件（`.d.ts`）、路由配置、常量/枚举声明

## 测试文件位置与命名

```
jeecgboot-vue3/
├── tests/                          # 主测试目录
│   ├── api/                        # API 封装层测试
│   ├── store/                      # Pinia store 测试
│   ├── composables/                # composable 函数测试
│   ├── components/                 # 组件测试
│   └── *.spec.ts                   # 工具函数/枚举等测试
└── src/                            # 也可与源码同目录放置
    └── views/xxx/Component.spec.ts # 组件就近测试
```

- 命名统一使用 `.spec.ts` 后缀（项目既有惯例）
- 测试文件名与被测模块同名：`userStatus.ts` → `userStatus.spec.ts`

## 测试必须执行，不能只写不跑

- 写完测试后，**必须执行该测试文件**确认通过，不能仅凭代码逻辑推断正确
- 代码变更涉及的模块，必须执行**模块级全量测试**（`npx vitest run`），不能只跑修改的测试文件
- 原因：单个测试通过不代表与其他测试无冲突，全量测试能发现 mock 泄漏、桩冲突等问题

## 四类测试模式

### 1. API 封装层测试

Mock `defHttp`，验证请求 URL、参数、方法是否正确。

```typescript
import { vi } from 'vitest';

// Mock 必须在 import 被测模块之前
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';
import { getUserStatus } from '/@/api/content/userStatus';

describe('getUserStatus', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should call GET with correct URL and params', async () => {
    const mockResult = { code: 200, result: { userId: 'u1', status: 'NORMAL' } };
    vi.mocked(defHttp.get).mockResolvedValue(mockResult);

    const result = await getUserStatus('u1');

    expect(defHttp.get).toHaveBeenCalledWith({
      url: '/api/content/user-status/u1',
    });
    expect(result).toEqual(mockResult);
  });
});
```

**要点**：
- `vi.mock()` 必须放在文件顶层、`import` 被测模块之前
- 使用 `vi.mocked(fn)` 替代类型断言 `(fn as vi.Mock)`
- 验证请求 URL 和参数，而非仅验证返回值

### 2. Pinia Store 测试

Mock API 模块，使用 `setActivePinia(createPinia())` 初始化。

```typescript
import { vi } from 'vitest';

const mockGetCurrentStatus = vi.fn();
vi.mock('/@/api/content/userStatus', () => ({
  getCurrentStatus: (...args: any[]) => mockGetCurrentStatus(...args),
}));

import { setActivePinia, createPinia } from 'pinia';
import { useUserStatusStore } from '/@/store/modules/userStatus';

describe('UserStatusStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should fetch and update current status', async () => {
    const mockDetail = { userId: 'u1', status: 'NORMAL', statusName: '正常' };
    mockGetCurrentStatus.mockResolvedValue(mockDetail);

    const store = useUserStatusStore();
    await store.fetchCurrentStatus('u1');

    expect(store.currentStatus).toBe('NORMAL');
    expect(store.statusDetail).toEqual(mockDetail);
  });
});
```

**要点**：
- 每个测试用 `setActivePinia(createPinia())` 重置 store 状态
- Mock API 时使用闭包变量（`mockGetCurrentStatus`）便于在测试中控制返回值和断言调用次数
- 测试 getters 时直接修改 store 状态再读取计算属性

### 3. Composable 测试

Mock 依赖的 store 和 hooks，验证返回的函数行为。

```typescript
import { vi } from 'vitest';

vi.mock('/@/store/modules/userStatus', () => ({
  useUserStatusStore: vi.fn(),
}));

import { useUserStatusStore } from '/@/store/modules/userStatus';
import { useStatusGuard } from '/@/composables/useStatusGuard';

const mockStore = {
  currentStatus: null as string | null,
  fetchCurrentStatus: vi.fn(),
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(useUserStatusStore).mockReturnValue(mockStore as any);
});

describe('useStatusGuard', () => {
  it('should return true when status is NORMAL', () => {
    mockStore.currentStatus = 'NORMAL';
    const { canPerformAction } = useStatusGuard();
    expect(canPerformAction('comment')).toBe(true);
  });
});
```

**要点**：
- Mock 返回值使用闭包对象，便于在 `beforeEach` 或测试中修改
- 通过 `vi.mocked(useUserStatusStore).mockReturnValue()` 控制 mock 返回

### 4. Vue 组件测试

使用 `@vue/test-utils` 的 `mount`，Mock 第三方组件。

```typescript
import { mount } from '@vue/test-utils';
import StatusTag from '/@/components/jeecg/UserStatus/StatusTag.vue';

// Mock ant-design-vue 组件为简易 DOM
const mockComponents = {
  'a-tag': {
    template: '<span class="mock-tag"><slot /></span>',
    props: ['color'],
  },
  'a-tooltip': {
    template: '<span class="mock-tooltip"><slot /></span>',
    props: ['title'],
  },
};

describe('StatusTag', () => {
  const mountTag = (props: Record<string, any> = {}) => {
    return mount(StatusTag, {
      props: { status: 'NORMAL', ...props },
      global: {
        components: mockComponents,
      },
    });
  };

  it('should render status label', () => {
    const wrapper = mountTag({ status: 'NORMAL' });
    expect(wrapper.text()).toContain('正常');
  });
});
```

**要点**：
- Ant Design Vue 组件通过 `global.components` 注入简易 mock，不要导入真实组件
- 使用工厂函数（`mountTag`）封装挂载逻辑，减少重复代码
- 优先测试用户可见的行为（文本、可见性、事件触发），而非内部实现细节

## Mock 规范

### Mock 顺序
1. `vi.mock()` 声明在文件最顶层
2. `import` 被测模块
3. `import` 测试工具（`describe`, `it`, `expect` 等由 globals 提供，无需导入）

### 清理
- 每个 `describe` 或顶层 `beforeEach` 中调用 `vi.clearAllMocks()`
- 有定时器的测试使用 `vi.useFakeTimers()` + `vi.useRealTimers()` 配对

### 路径别名
- Mock 路径使用 `/@/` 前缀（与 `vitest.config.ts` 中的 alias 一致）
- 示例：`vi.mock('/@/utils/http/axios', ...)`

## 断言规范

- 优先使用语义化断言：`toBe` > `toEqual` > `toBeTruthy`
- 异步测试使用 `async/await`，不要使用 `done` 回调
- 测试错误场景用 `expect(...).rejects.toThrow()`
- 组件断言优先检查 DOM 输出（`wrapper.text()`、`wrapper.find()`），而非组件内部状态

## 常见陷阱

### Mock 必须在 import 之前
`vi.mock()` 会被 Vitest 提升到文件顶部，但为了可读性和避免变量未初始化，**始终将 `vi.mock()` 写在 `import` 之前**。

### 不要混用 Jest 和 Vitest API
旧测试中残留的 `jest.mock` / `jest.fn` 必须迁移为 `vi.mock` / `vi.fn`。两者 API 几乎一致，但 Vitest 的 `vi` 提供了更好的 TypeScript 支持和 `vi.mocked()` 辅助函数。

### 组件测试中的 Ant Design Vue
Ant Design Vue 组件在 jsdom 环境下无法正常渲染，**必须 mock**。不要在测试中引入完整的 Ant Design Vue。

### Store 测试中的 `$reset`
Pinia store 测试中，如需完全重置状态，可在 `createStore()` 工厂函数中调用 `store.$reset()`。但注意 `$reset` 仅对 Options Store 有效，Setup Store 需要手动重置。
