import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { nextTick } from 'vue';
import ReportList from '../ReportList.vue';

// ---- 权限状态变量 ----
// 由 vi.mock 通过闭包捕获，每次 useCircleStore() 调用时读取
let _hasPermission = false;

// ---- Mock useCircleStore / useCircleStoreWithOut ----
vi.mock('/@/store/modules/circle', () => ({
  useCircleStore: vi.fn(() => ({
    isCreator: _hasPermission,
    isModerator: _hasPermission,
    canManageMember: _hasPermission,
  })),
  useCircleStoreWithOut: vi.fn(() => ({
    isCreator: _hasPermission,
    isModerator: _hasPermission,
    canManageMember: _hasPermission,
  })),
}));

// ---- Mock API ----
vi.mock('/@/api/content/circle/report', () => ({
  getCircleReportList: vi.fn(),
  deleteReportContent: vi.fn(),
  ignoreReport: vi.fn(),
  muteReportUser: vi.fn(),
}));

// ---- Mock ant-design-vue 组件 ----
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();

  const TabPaneStub = {
    name: 'ATabPane',
    template: '<div class="ant-tabs-tabpane"><slot /></div>',
    props: ['tab'],
  };

  const TabsStub = Object.assign(
    {
      name: 'ATabs',
      template: '<div class="ant-tabs"><slot /></div>',
      props: ['activeKey'],
    },
    { TabPane: TabPaneStub },
  );

  return {
    ...actual,
    Tabs: TabsStub,
    Table: {
      name: 'ATable',
      template: '<div class="ant-table" :data-loading="loading"><template v-for="item in dataSource" :key="item.id"><template v-for="col in columns" :key="col.key"><slot name="bodyCell" :record="item" :column="col" /></template></template></div>',
      props: ['dataSource', 'columns', 'loading', 'rowKey'],
    },
    Button: {
      name: 'AButton',
      template: '<button class="ant-btn"><slot /></button>',
      props: ['size', 'type', 'danger'],
    },
    Space: {
      name: 'ASpace',
      template: '<div class="ant-space"><slot /></div>',
    },
    Tag: {
      name: 'ATag',
      template: '<span class="ant-tag" :data-color="color"><slot /></span>',
      props: ['color'],
    },
    Modal: {
      confirm: vi.fn(),
    },
    message: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn(),
    },
  };
});

const defaultStubs = {
  ReportCard: {
    template: '<div class="report-card-stub"><slot /></div>',
    props: ['report'],
  },
  ReportDetailDrawer: {
    template: '<div class="drawer-stub" />',
    props: ['visible', 'report'],
  },
  'a-spin': {
    template: '<div class="ant-spin"><slot /></div>',
    props: ['spinning'],
  },
  'a-empty': {
    template: '<div class="ant-empty-empty" />',
    props: ['description'],
  },
  'a-result': {
    template: '<div class="ant-result"><div class="ant-result-title">{{ title }}</div><div class="ant-result-subtitle">{{ subTitle }}</div></div>',
    props: ['status', 'title', 'subTitle'],
  },
};

const mockReportList = [
  {
    id: 'report-001',
    circleId: 'circle-123',
    contentId: 'content-456',
    reporterId: 'user-789',
    reason: '违规内容',
    status: 'PENDING',
    handleAction: 'DELETE_CONTENT',
    createTime: '2024-01-15 10:30:00',
  },
  {
    id: 'report-002',
    circleId: 'circle-123',
    contentId: 'content-789',
    reporterId: 'user-012',
    reason: '垃圾信息',
    status: 'RESOLVED',
    handleAction: 'DELETE_CONTENT',
    createTime: '2024-01-15 11:00:00',
  },
];

describe('ReportList', () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    _hasPermission = false;
    const { getCircleReportList } = await import('/@/api/content/circle/report');
    vi.mocked(getCircleReportList).mockResolvedValue(mockReportList as any);
  });

  async function mountWithPermission(hasPermission: boolean, extraProps = {}) {
    _hasPermission = hasPermission;
    const wrapper = mount(ReportList, {
      props: { circleId: 'circle-123', ...extraProps },
      global: { stubs: defaultStubs },
    });
    await flushPromises();
    return wrapper;
  }

  // ======================== 基础渲染测试 ========================

  it('渲染页面标题"举报处理"', async () => {
    const wrapper = await mountWithPermission(true);
    expect(wrapper.text()).toContain('举报处理');
  });

  it('渲染 3 个 Tab（待处理/已处理/已忽略）', async () => {
    const wrapper = await mountWithPermission(true);
    const panes = wrapper.findAll('.ant-tabs-tabpane');
    expect(panes.length).toBe(3);
  });

  it('默认请求数据', async () => {
    const { getCircleReportList } = await import('/@/api/content/circle/report');
    await mountWithPermission(true);
    // 默认 activeTab 为 PENDING，不带 status 参数
    expect(getCircleReportList).toHaveBeenCalledWith('circle-123', undefined);
  });

  it('加载中显示 loading 状态', async () => {
    const { getCircleReportList } = await import('/@/api/content/circle/report');
    let resolvePromise: any;
    vi.mocked(getCircleReportList).mockReturnValue(
      new Promise((resolve) => { resolvePromise = resolve; }),
    );
    _hasPermission = true;
    const wrapper = mount(ReportList, {
      props: { circleId: 'circle-123' },
      global: { stubs: defaultStubs },
    });
    await nextTick();
    expect(wrapper.find('.ant-table').attributes('data-loading')).toBe('true');
    resolvePromise(mockReportList);
  });

  it('列表数据渲染（含举报原因、状态）', async () => {
    const wrapper = await mountWithPermission(true);
    expect(wrapper.text()).toContain('违规内容');
    expect(wrapper.text()).toContain('垃圾信息');
    expect(wrapper.text()).toContain('待处理');
    expect(wrapper.text()).toContain('已处理');
  });

  it('状态 Tag 颜色正确（Pending=orange, Resolved=green）', async () => {
    const wrapper = await mountWithPermission(true);
    const tags = wrapper.findAll('.ant-tag');
    const pendingTag = tags.find(t => t.text() === '待处理');
    const resolvedTag = tags.find(t => t.text() === '已处理');
    expect(pendingTag?.attributes('data-color')).toBe('orange');
    expect(resolvedTag?.attributes('data-color')).toBe('green');
  });

  // ======================== 操作测试 ========================

  it('点击"查看"按钮显示详情抽屉', async () => {
    const wrapper = await mountWithPermission(true);
    const viewBtn = wrapper.findAll('.ant-btn').find(b => b.text() === '查看');
    await viewBtn!.trigger('click');
    await nextTick();
    expect(wrapper.find('.drawer-stub').exists()).toBe(true);
  });

  it('点击"删除内容"弹出确认框（danger 类型）', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = await mountWithPermission(true);
    const deleteBtn = wrapper.findAll('.ant-btn').find(b => b.text() === '删除内容');
    await deleteBtn!.trigger('click');
    await nextTick();
    expect(Modal.confirm).toHaveBeenCalled();
    const callArgs = vi.mocked(Modal.confirm).mock.calls[0]?.[0] as any;
    expect(callArgs.title).toBe('确认删除');
    expect(callArgs.okType).toBe('danger');
  });

  // ======================== 权限测试 ========================

  it('无权限时显示"无权限访问"', async () => {
    const wrapper = await mountWithPermission(false);
    expect(wrapper.text()).toContain('无权限访问');
  });

  it('有权限时正常渲染内容，不显示"无权限访问"', async () => {
    const wrapper = await mountWithPermission(true);
    expect(wrapper.find('.ant-tabs').exists()).toBe(true);
    expect(wrapper.text()).not.toContain('无权限访问');
  });

  // ======================== 错误处理测试 ========================

  it('API 调用失败时不崩溃', async () => {
    const { getCircleReportList } = await import('/@/api/content/circle/report');
    vi.mocked(getCircleReportList).mockRejectedValue(new Error('Network error'));
    _hasPermission = true;
    const wrapper = mount(ReportList, {
      props: { circleId: 'circle-123' },
      global: { stubs: defaultStubs },
    });
    await flushPromises();
    expect(wrapper.html()).toBeTruthy();
  });

  it('API 调用失败时显示错误消息', async () => {
    const { getCircleReportList } = await import('/@/api/content/circle/report');
    const { message } = await import('ant-design-vue');
    vi.mocked(getCircleReportList).mockRejectedValue(new Error('Network error'));
    _hasPermission = true;
    mount(ReportList, {
      props: { circleId: 'circle-123' },
      global: { stubs: defaultStubs },
    });
    await flushPromises();
    expect(message.error).toHaveBeenCalledWith('加载举报列表失败');
  });
});
