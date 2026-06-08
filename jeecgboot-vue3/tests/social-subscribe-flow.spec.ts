import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import SquareView from '/@/views/social/subscribe/square.vue';
import NotificationView from '/@/views/social/subscribe/notification.vue';
import ManageView from '/@/views/social/subscribe/manage.vue';

// --- Mutable state for subscribe store ---
let subscribeListState: any[] = [];
let totalSubscribesState = 0;
let loadingState = false;
let hasMoreState = false;

// --- Mock functions ---
const mockFetchSubscribeList = vi.fn();
const mockFetchPlaza = vi.fn();
const mockSetSearchKeyword = vi.fn();
const mockSetSelectedSourceType = vi.fn();
const mockBatchPause = vi.fn();
const mockBatchResume = vi.fn();
const mockBatchCancel = vi.fn();
const mockFetchNotificationConfig = vi.fn();
const mockFetchGlobalNotificationDefault = vi.fn();
const mockSaveConfig = vi.fn();
const mockRouterPush = vi.fn();

// --- Store mocks ---
vi.mock('/@/store/modules/subscribe', () => ({
  useSubscribeStore: () => ({
    get subscribeList() { return subscribeListState; },
    get plazaData() { return { records: [], total: 0 }; },
    get totalSubscribes() { return totalSubscribesState; },
    get loading() { return loadingState; },
    get hasMore() { return hasMoreState; },
    get globalNotificationDefault() { return null; },
    get currentNotificationConfig() { return null; },
    fetchSubscribeList: mockFetchSubscribeList,
    fetchPlaza: mockFetchPlaza,
    setSearchKeyword: mockSetSearchKeyword,
    setSelectedSourceType: mockSetSelectedSourceType,
    batchPause: mockBatchPause,
    batchResume: mockBatchResume,
    batchCancel: mockBatchCancel,
    fetchNotificationConfig: mockFetchNotificationConfig,
    fetchGlobalNotificationDefault: mockFetchGlobalNotificationDefault,
    saveConfig: mockSaveConfig,
  }),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({
    getUserInfo: { userId: 'u-1' },
  }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
  useRoute: () => ({ query: {} }),
}));

vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'MD' } }),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn() } }),
}));

// --- Stubs ---
const stubs = {
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder', 'allowClear'] },
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading', 'disabled', 'danger'] },
  'a-tag': { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
  'a-checkbox': { template: '<input type="checkbox" class="checkbox-stub" />', props: ['checked'] },
  'a-popconfirm': { template: '<div class="popconfirm-stub"><slot /></div>', props: ['title', 'okText', 'cancelText'] },
  'a-form': { template: '<form class="form-stub"><slot /></form>', props: ['labelCol', 'wrapperCol'] },
  'a-form-item': { template: '<div class="form-item-stub"><slot /></div>', props: ['label', 'wrapperCol'] },
  'a-switch': { template: '<input type="checkbox" class="switch-stub" />', props: ['checked'] },
  'a-radio-group': { template: '<div class="radio-group-stub"><slot /></div>', props: ['value'] },
  'a-radio': { template: '<label class="radio-stub"><slot /></label>', props: ['value'] },
  'a-time-picker': { template: '<input class="time-picker-stub" />', props: ['value', 'format', 'placeholder', 'allowClear'] },
  'a-select': { template: '<select class="select-stub"><slot /></select>', props: ['value', 'placeholder', 'allowClear'] },
  'a-select-option': { template: '<option class="select-option-stub"><slot /></option>', props: ['value'] },
  AppstoreOutlined: { template: '<span class="icon-appstore-stub" />' },
  UserOutlined: { template: '<span class="icon-user-stub" />' },
  SubscribeButton: { template: '<button class="subscribe-btn-stub"><slot /></button>', props: ['userId', 'sourceId', 'sourceType', 'isSubscribed'] },
  SubscriptionCard: { template: '<div class="subscription-card-stub" />', props: ['userId', 'source'] },
};

function makePlazaItem(overrides: Record<string, any> = {}) {
  return {
    sourceId: 's-1',
    sourceName: 'Tech Blog',
    sourceIcon: '',
    sourceType: '专题',
    category: '科技',
    subscriberCount: 100,
    description: 'A tech blog',
    isSubscribed: false,
    ...overrides,
  };
}

function makeSubscribeItem(overrides: Record<string, any> = {}) {
  return {
    id: 'sub-1',
    sourceId: 's-1',
    sourceName: 'Tech Blog',
    sourceIcon: '',
    sourceType: '专题',
    category: '科技',
    subscriberCount: 100,
    lastUpdateTime: '2025-06-01',
    status: 'active',
    ...overrides,
  };
}

describe('social subscribe flow (E2E-style)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockFetchSubscribeList.mockResolvedValue(undefined);
    mockFetchPlaza.mockResolvedValue({ records: [], total: 0 });
    mockBatchPause.mockResolvedValue(undefined);
    mockBatchResume.mockResolvedValue(undefined);
    mockBatchCancel.mockResolvedValue(undefined);
    mockFetchNotificationConfig.mockResolvedValue(undefined);
    mockFetchGlobalNotificationDefault.mockResolvedValue(undefined);
    mockSaveConfig.mockResolvedValue(undefined);
    subscribeListState = [];
    totalSubscribesState = 0;
    loadingState = false;
    hasMoreState = false;
  });

  function mountSquare() {
    return mount(SquareView, { global: { stubs } });
  }

  function mountNotification() {
    return mount(NotificationView, { global: { stubs } });
  }

  function mountManage() {
    return mount(ManageView, { global: { stubs } });
  }

  // --- Square page tests ---

  it('renders subscribe square with categories', async () => {
    const wrapper = mountSquare();
    await flushPromises();
    expect(wrapper.find('.subscribe-square-page__title').text()).toBe('订阅广场');
    const categoryTags = wrapper.findAll('.subscribe-square-page__category-tag');
    expect(categoryTags.length).toBe(6); // 科技 娱乐 体育 生活 教育 其他
    expect(categoryTags[0].text()).toBe('科技');
  });

  it('shows source cards on square', async () => {
    const records = [makePlazaItem({ sourceId: 's-1' }), makePlazaItem({ sourceId: 's-2', sourceName: 'Sport News' })];
    mockFetchPlaza.mockResolvedValue({ records, total: 2 });
    const wrapper = mountSquare();
    await flushPromises();
    const cards = wrapper.findAll('.subscribe-square-page__card');
    expect(cards.length).toBe(2);
    expect(cards[0].find('.subscribe-square-page__card-name').text()).toBe('Tech Blog');
  });

  // --- Notification config page tests ---

  it('renders notification config page', async () => {
    const wrapper = mountNotification();
    await flushPromises();
    expect(wrapper.find('.notification-config-page').exists()).toBe(true);
    expect(wrapper.find('.notification-config-page__title').text()).toContain('通知设置');
  });

  it('shows channel switches', async () => {
    const wrapper = mountNotification();
    await flushPromises();
    const formItems = wrapper.findAll('.form-item-stub');
    // form items: 站内通知, 推送通知, 邮件通知, 推送频率, 免打扰时段, 保存按钮
    expect(formItems.length).toBeGreaterThanOrEqual(3);
    const switches = wrapper.findAll('.switch-stub');
    expect(switches.length).toBe(3); // channelInApp, channelPush, channelEmail
  });

  // --- Manage page tests ---

  it('navigates to subscribe management', async () => {
    const wrapper = mountManage();
    await flushPromises();
    expect(wrapper.find('.subscribe-manage-page__title').text()).toBe('订阅管理');
    expect(mockFetchSubscribeList).toHaveBeenCalled();
  });

  it('shows subscription list', async () => {
    subscribeListState = [
      makeSubscribeItem({ id: 'sub-1', sourceId: 's-1' }),
      makeSubscribeItem({ id: 'sub-2', sourceId: 's-2', sourceName: 'Sport News' }),
    ];
    totalSubscribesState = 2;
    const wrapper = mountManage();
    await flushPromises();
    const cards = wrapper.findAll('.subscription-card-stub');
    expect(cards.length).toBe(2);
    expect(wrapper.find('.subscribe-manage-page__count').text()).toContain('2');
  });

  it('supports batch operations', async () => {
    subscribeListState = [makeSubscribeItem({ id: 'sub-1', sourceId: 's-1' })];
    const wrapper = mountManage();
    await flushPromises();
    // Click "批量管理" button
    const buttons = wrapper.findAll('.subscribe-manage-page__toolbar .btn-stub');
    const batchBtn = buttons.find((b) => b.text() === '批量管理');
    expect(batchBtn).toBeTruthy();
    await batchBtn!.trigger('click');
    await flushPromises();
    // Verify batch bar appears
    expect(wrapper.find('.subscribe-manage-page__batch-bar').exists()).toBe(true);
    // Verify checkboxes appear
    expect(wrapper.find('.checkbox-stub').exists()).toBe(true);
    // Verify batch info text
    expect(wrapper.find('.subscribe-manage-page__batch-info').text()).toContain('已选');
  });

  it('supports search on manage page', async () => {
    const wrapper = mountManage();
    await flushPromises();
    const searchInput = wrapper.find('.subscribe-manage-page__toolbar .search-stub');
    expect(searchInput.exists()).toBe(true);
  });
});
