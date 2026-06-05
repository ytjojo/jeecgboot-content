import { mount, flushPromises } from '@vue/test-utils';

const mockFetchFollowFeed = jest.fn().mockResolvedValue(undefined);
const mockSetFollowTypes = jest.fn();

let feedStoreState: Record<string, any> = {
  followFeedList: [],
  priorityItems: [],
  followLoading: false,
  followHasMore: false,
  followTypes: [],
};

jest.mock('/@/store/modules/feed', () => ({
  useFeedStore: () => ({
    ...feedStoreState,
    fetchFollowFeed: mockFetchFollowFeed,
    setFollowTypes: mockSetFollowTypes,
  }),
}));

jest.mock('/@/store', () => ({ store: {} }));

jest.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'MD' } }),
}));

const stubs = {
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
  FeedCard: { template: '<div class="feed-card-stub" />', props: ['feed', 'isMobile'] },
  FeedFilter: { template: '<div class="feed-filter-stub" />', props: ['types', 'modelValue'] },
  SpecialFeed: { template: '<div class="special-feed-stub" />', props: ['feeds', 'loading', 'hasMore'] },
};

function makeFeed(overrides: Record<string, any> = {}) {
  return {
    id: '1',
    userId: 'u-1',
    nickname: 'Test User',
    avatar: 'https://cdn/avatar.png',
    contentId: 'c-1',
    contentTitle: 'Test Post Title',
    contentSummary: 'Summary',
    dynamicType: 'post',
    createTime: '2025-06-01',
    isPriority: false,
    ...overrides,
  };
}

describe('FeedPage (src/views/social/feed/index.vue)', () => {
  async function mountPage(storeOverrides: Record<string, any> = {}) {
    feedStoreState = {
      followFeedList: [],
      priorityItems: [],
      followLoading: false,
      followHasMore: false,
      followTypes: [],
      ...storeOverrides,
    };
    const Component = (await import('/@/views/social/feed/index.vue')).default;
    const wrapper = mount(Component, {
      global: { stubs },
    });
    await flushPromises();
    return wrapper;
  }

  beforeEach(() => {
    jest.clearAllMocks();
    feedStoreState = {
      followFeedList: [],
      priorityItems: [],
      followLoading: false,
      followHasMore: false,
      followTypes: [],
    };
  });

  it('renders page title "关注动态"', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.feed-page__title').text()).toBe('关注动态');
  });

  it('calls fetchFollowFeed(true) on mount', async () => {
    await mountPage();
    expect(mockFetchFollowFeed).toHaveBeenCalledWith(true);
  });

  it('shows FeedFilter component', async () => {
    const wrapper = await mountPage();
    const filter = wrapper.find('.feed-filter-stub');
    expect(filter.exists()).toBe(true);
  });

  it('shows SpecialFeed when priorityItems has items', async () => {
    const wrapper = await mountPage({ priorityItems: [makeFeed({ id: 'p1', isPriority: true })] });
    expect(wrapper.find('.special-feed-stub').exists()).toBe(true);
  });

  it('hides SpecialFeed when priorityItems is empty', async () => {
    const wrapper = await mountPage({ priorityItems: [] });
    expect(wrapper.find('.special-feed-stub').exists()).toBe(false);
  });

  it('shows FeedCard list when followFeedList has items', async () => {
    const wrapper = await mountPage({
      followFeedList: [makeFeed({ id: 'f1' }), makeFeed({ id: 'f2' })],
    });
    const cards = wrapper.findAll('.feed-card-stub');
    expect(cards).toHaveLength(2);
    expect(wrapper.find('.feed-page__list').exists()).toBe(true);
  });

  it('shows empty state when list is empty and not loading', async () => {
    const wrapper = await mountPage({ followFeedList: [], followLoading: false });
    expect(wrapper.find('.empty-stub').exists()).toBe(true);
  });

  it('shows loading state when followLoading is true and list is empty', async () => {
    const wrapper = await mountPage({ followFeedList: [], followLoading: true });
    const spin = wrapper.find('.spin-stub');
    expect(spin.exists()).toBe(true);
    expect(wrapper.find('.empty-stub').exists()).toBe(false);
  });

  it('has "刷新" button that calls fetchFollowFeed(true)', async () => {
    const wrapper = await mountPage();
    const btn = wrapper.find('.btn-stub');
    expect(btn.exists()).toBe(true);
    expect(btn.text()).toBe('刷新');
    await btn.trigger('click');
    expect(mockFetchFollowFeed).toHaveBeenCalledWith(true);
  });

  it('shows loading indicator at bottom when following loading with items', async () => {
    const wrapper = await mountPage({
      followFeedList: [makeFeed({ id: 'f1' })],
      followLoading: true,
    });
    expect(wrapper.find('.feed-page__loading').exists()).toBe(true);
  });
});
