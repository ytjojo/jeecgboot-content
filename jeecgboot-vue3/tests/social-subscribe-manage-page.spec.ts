import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';

// -- Mock functions --
const mockFetchSubscribeList = vi.fn().mockResolvedValue(undefined);
const mockSetSearchKeyword = vi.fn();
const mockSetSelectedSourceType = vi.fn();
const mockBatchPause = vi.fn().mockResolvedValue(undefined);
const mockBatchResume = vi.fn().mockResolvedValue(undefined);
const mockBatchCancel = vi.fn().mockResolvedValue(undefined);
const mockRouterPush = vi.fn();

// Mutable store state so individual tests can override values
let storeOverrides: Record<string, any> = {};

function getStoreState() {
  return {
    subscribeList: [],
    totalSubscribes: 0,
    loading: false,
    hasMore: false,
    fetchSubscribeList: mockFetchSubscribeList,
    setSearchKeyword: mockSetSearchKeyword,
    setSelectedSourceType: mockSetSelectedSourceType,
    batchPause: mockBatchPause,
    batchResume: mockBatchResume,
    batchCancel: mockBatchCancel,
    ...storeOverrides,
  };
}

// -- Module mocks --
vi.mock('/@/store/modules/subscribe', () => ({
  useSubscribeStore: () => getStoreState(),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({
    getUserInfo: { userId: 'u-1' },
  }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
}));

vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'MD' } }),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({
    createMessage: { success: vi.fn(), error: vi.fn() },
  }),
}));

// -- Stubs --
const stubs = {
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder'] },
  'a-select': { template: '<select class="select-stub"><slot /></select>', props: ['value', 'placeholder'] },
  'a-select-option': { template: '<option class="option-stub"><slot /></option>', props: ['value'] },
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><span>{{ description }}</span><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading', 'disabled', 'danger'] },
  'a-checkbox': { template: '<input type="checkbox" class="checkbox-stub" />', props: ['checked'] },
  'a-popconfirm': { template: '<div class="popconfirm-stub"><slot /></div>', props: ['title', 'okText', 'cancelText'] },
  SubscriptionCard: { template: '<div class="subscription-card-stub" />', props: ['userId', 'source'] },
};

// -- Helper --
async function mountPage(overrides: Record<string, any> = {}) {
  storeOverrides = overrides;
  const { default: ManagePage } = await import('/@/views/social/subscribe/manage.vue');
  const wrapper = mount(ManagePage, {
    global: { stubs },
  });
  await flushPromises();
  return wrapper;
}

// -- Tests --
describe('subscribe/manage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    storeOverrides = {};
  });

  it('renders page title "订阅管理"', async () => {
    const wrapper = await mountPage();
    const title = wrapper.find('.subscribe-manage-page__title');
    expect(title.exists()).toBe(true);
    expect(title.text()).toBe('订阅管理');
  }, 15000);

  it('calls fetchSubscribeList on mount', async () => {
    await mountPage();
    expect(mockFetchSubscribeList).toHaveBeenCalledWith('u-1', true);
  });

  it('shows search input', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.search-stub').exists()).toBe(true);
  });

  it('shows source type select', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.select-stub').exists()).toBe(true);
  });

  it('has "批量管理" button', async () => {
    const wrapper = await mountPage();
    const buttons = wrapper.findAll('.btn-stub');
    const batchBtn = buttons.find((b) => b.text().includes('批量管理'));
    expect(batchBtn).toBeTruthy();
  });

  it('shows SubscriptionCard list when subscribeList has items', async () => {
    const items = [
      { id: '1', sourceId: 's1', sourceName: 'A', sourceIcon: '', sourceType: '专题', category: '', subscriberCount: 10, lastUpdateTime: '', status: 'active' },
      { id: '2', sourceId: 's2', sourceName: 'B', sourceIcon: '', sourceType: '话题', category: '', subscriberCount: 5, lastUpdateTime: '', status: 'active' },
    ];
    const wrapper = await mountPage({ subscribeList: items });
    expect(wrapper.findAll('.subscription-card-stub')).toHaveLength(2);
  });

  it('shows empty state when list is empty', async () => {
    const wrapper = await mountPage({ subscribeList: [] });
    expect(wrapper.find('.empty-stub').exists()).toBe(true);
    expect(wrapper.find('.empty-stub').text()).toContain('还没有订阅任何内容源');
  });

  it('shows "加载更多" when hasMore is true', async () => {
    const items = [
      { id: '1', sourceId: 's1', sourceName: 'A', sourceIcon: '', sourceType: '专题', category: '', subscriberCount: 10, lastUpdateTime: '', status: 'active' },
    ];
    const wrapper = await mountPage({ subscribeList: items, hasMore: true });
    const loadMoreBtn = wrapper.findAll('.btn-stub').find((b) => b.text().includes('加载更多'));
    expect(loadMoreBtn).toBeTruthy();
  });

  it('toggles batch mode on button click', async () => {
    const items = [
      { id: '1', sourceId: 's1', sourceName: 'A', sourceIcon: '', sourceType: '专题', category: '', subscriberCount: 10, lastUpdateTime: '', status: 'active' },
    ];
    const wrapper = await mountPage({ subscribeList: items });

    // Initially no batch bar
    expect(wrapper.find('.subscribe-manage-page__batch-bar').exists()).toBe(false);

    // Click "批量管理"
    const buttons = wrapper.findAll('.btn-stub');
    const batchBtn = buttons.find((b) => b.text().includes('批量管理'))!;
    await batchBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.find('.subscribe-manage-page__batch-bar').exists()).toBe(true);
  });

  it('shows checkboxes in batch mode', async () => {
    const items = [
      { id: '1', sourceId: 's1', sourceName: 'A', sourceIcon: '', sourceType: '专题', category: '', subscriberCount: 10, lastUpdateTime: '', status: 'active' },
    ];
    const wrapper = await mountPage({ subscribeList: items });

    // Enter batch mode
    const batchBtn = wrapper.findAll('.btn-stub').find((b) => b.text().includes('批量管理'))!;
    await batchBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.findAll('.checkbox-stub')).toHaveLength(1);
  });

  it('hides checkboxes when not in batch mode', async () => {
    const items = [
      { id: '1', sourceId: 's1', sourceName: 'A', sourceIcon: '', sourceType: '专题', category: '', subscriberCount: 10, lastUpdateTime: '', status: 'active' },
    ];
    const wrapper = await mountPage({ subscribeList: items });
    expect(wrapper.findAll('.checkbox-stub')).toHaveLength(0);
  });
});
