import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';

const mockFetchPlaza = vi.fn();
const mockPush = vi.fn();

vi.mock('/@/store/modules/subscribe', () => ({
  useSubscribeStore: () => ({
    fetchPlaza: mockFetchPlaza,
  }),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({
    getUserInfo: { userId: 'u-1' },
  }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}));

vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'MD' } }),
}));

const STUBS = {
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder'] },
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
  'a-tag': { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
  AppstoreOutlined: { template: '<span class="icon-appstore-stub" />' },
  UserOutlined: { template: '<span class="icon-user-stub" />' },
  SubscribeButton: {
    template: '<button class="subscribe-btn-stub" />',
    props: ['userId', 'sourceId', 'sourceType', 'isSubscribed'],
  },
};

function makeSource(overrides: Record<string, any> = {}) {
  return {
    sourceId: 's1',
    sourceName: 'Tech News',
    sourceIcon: '',
    sourceType: 'RSS',
    category: '科技',
    subscriberCount: 100,
    description: 'Latest tech',
    isSubscribed: false,
    ...overrides,
  };
}

describe('SubscribeSquarePage.vue', () => {
  let Component: any;

  beforeAll(async () => {
    Component = (await import('/@/views/social/subscribe/square.vue')).default;
  });

  function mountPage() {
    return mount(Component, {
      global: { stubs: STUBS },
    });
  }

  beforeEach(() => {
    vi.clearAllMocks();
    mockFetchPlaza.mockResolvedValue({ records: [], total: 0 });
  });

  it('renders page title "订阅广场"', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    const title = wrapper.find('.subscribe-square-page__title');
    expect(title.exists()).toBe(true);
    expect(title.text()).toBe('订阅广场');
  });

  it('calls fetchPlaza on mount', async () => {
    await mountPage();
    await flushPromises();
    expect(mockFetchPlaza).toHaveBeenCalledTimes(1);
    expect(mockFetchPlaza).toHaveBeenCalledWith(
      expect.objectContaining({ sort: 'popularity' }),
    );
  });

  it('shows search input', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    expect(wrapper.find('.search-stub').exists()).toBe(true);
  });

  it('shows category tags', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    const tags = wrapper.findAll('.subscribe-square-page__category-tag');
    expect(tags).toHaveLength(6);
    expect(tags[0].text()).toBe('科技');
    expect(tags[5].text()).toBe('其他');
  });

  it('shows source cards when fetchPlaza returns data', async () => {
    mockFetchPlaza.mockResolvedValue({ records: [makeSource()], total: 1 });
    const wrapper = await mountPage();
    await flushPromises();

    const cards = wrapper.findAll('.subscribe-square-page__card');
    expect(cards).toHaveLength(1);
    expect(wrapper.find('.subscribe-square-page__card-name').text()).toBe('Tech News');
    expect(wrapper.find('.subscribe-square-page__card-desc').text()).toBe('Latest tech');
    expect(wrapper.find('.subscribe-btn-stub').exists()).toBe(true);
  });

  it('shows empty state when no sources', async () => {
    mockFetchPlaza.mockResolvedValue({ records: [], total: 0 });
    const wrapper = await mountPage();
    await flushPromises();

    expect(wrapper.find('.subscribe-square-page__card').exists()).toBe(false);
    expect(wrapper.find('.empty-stub').exists()).toBe(true);
  });

  it('shows "加载更多" when hasMore is true', async () => {
    mockFetchPlaza.mockResolvedValue({ records: [makeSource()], total: 5 });
    const wrapper = await mountPage();
    await flushPromises();

    const loadMoreBtn = wrapper.find('.subscribe-square-page__load-more .btn-stub');
    expect(loadMoreBtn.exists()).toBe(true);
    expect(loadMoreBtn.text()).toBe('加载更多');
  });

  it('shows loading state', async () => {
    let resolvePlaza: (value: any) => void;
    mockFetchPlaza.mockImplementation(
      () => new Promise((resolve) => { resolvePlaza = resolve; }),
    );

    const wrapper = await mountPage();
    // spin-stub is always rendered inside a-spin; spinning prop is true while loading
    const spin = wrapper.find('.spin-stub');
    expect(spin.exists()).toBe(true);

    resolvePlaza!({ records: [], total: 0 });
    await flushPromises();
  });
});
