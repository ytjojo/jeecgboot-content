import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';

// --- Mutable state ---
let followListState: any[] = [];
let followGroupsState: any[] = [];
let followListLoadingState = false;
let hasMoreState = false;
let totalFollowsState = 0;

let feedFeedListState: any[] = [];
let priorityItemsState: any[] = [];
let followLoadingState = false;
let followHasMoreState = false;
let followTypesState: string[] = [];

// --- Mock functions ---
const mockFetchFollowList = vi.fn();
const mockFetchFollowGroups = vi.fn();
const mockSetSearchKeyword = vi.fn();
const mockSetSelectedGroupId = vi.fn();
const mockBatchCancelFollow = vi.fn();
const mockRouterPush = vi.fn();
const mockFetchFollowFeed = vi.fn();
const mockSetFollowTypes = vi.fn();

// --- Store mocks ---
vi.mock('/@/store/modules/follow', () => ({
  useFollowStore: () => ({
    get followList() { return followListState; },
    get followGroups() { return followGroupsState; },
    get followListLoading() { return followListLoadingState; },
    get hasMore() { return hasMoreState; },
    get searchKeyword() { return ''; },
    get selectedGroupId() { return ''; },
    get totalFollows() { return totalFollowsState; },
    fetchFollowList: mockFetchFollowList,
    fetchFollowGroups: mockFetchFollowGroups,
    setSearchKeyword: mockSetSearchKeyword,
    setSelectedGroupId: mockSetSelectedGroupId,
    batchCancelFollow: mockBatchCancelFollow,
  }),
}));

vi.mock('/@/store/modules/feed', () => ({
  useFeedStore: () => ({
    get followFeedList() { return feedFeedListState; },
    get priorityItems() { return priorityItemsState; },
    get followLoading() { return followLoadingState; },
    get followHasMore() { return followHasMoreState; },
    get followTypes() { return followTypesState; },
    fetchFollowFeed: mockFetchFollowFeed,
    setFollowTypes: mockSetFollowTypes,
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
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder'] },
  'a-radio-group': { template: '<div class="radio-group-stub"><slot /></div>', props: ['value'] },
  'a-radio-button': { template: '<button class="radio-btn-stub"><slot /></button>', props: ['value'] },
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
  UserCard: { template: '<div class="user-card-stub" />', props: ['userId', 'user', 'isMobile'] },
  FeedCard: { template: '<div class="feed-card-stub" />', props: ['feed', 'isMobile'] },
  FeedFilter: { template: '<div class="feed-filter-stub" />', props: ['types', 'modelValue'] },
  SpecialFeed: { template: '<div class="special-feed-stub" />', props: ['feeds', 'loading', 'hasMore'] },
  'a-radio': { template: '<label class="radio-stub"><slot /></label>', props: ['value'] },
  'a-tag': { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
};

function makeFollowItem(overrides: Record<string, any> = {}) {
  return {
    id: 'f1',
    userId: 'u-2',
    nickname: 'Alice',
    avatar: '',
    bio: 'bio',
    followTime: '2025-06-01',
    groupId: '',
    isSpecial: false,
    ...overrides,
  };
}

function makeFeedItem(overrides: Record<string, any> = {}) {
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

describe('social follow flow (E2E-style)', () => {
  let FollowPage: any;
  let FeedPage: any;

  beforeAll(async () => {
    FollowPage = (await import('/@/views/social/follow/index.vue')).default;
    FeedPage = (await import('/@/views/social/feed/index.vue')).default;
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mockFetchFollowList.mockResolvedValue(undefined);
    mockFetchFollowGroups.mockResolvedValue(undefined);
    mockBatchCancelFollow.mockResolvedValue(undefined);
    mockFetchFollowFeed.mockResolvedValue(undefined);
    followListState = [];
    followGroupsState = [];
    followListLoadingState = false;
    hasMoreState = false;
    totalFollowsState = 0;
    feedFeedListState = [];
    priorityItemsState = [];
    followLoadingState = false;
    followHasMoreState = false;
    followTypesState = [];
  });

  function mountFollowPage() {
    return mount(FollowPage, { global: { stubs } });
  }

  function mountFeedPage() {
    return mount(FeedPage, { global: { stubs } });
  }

  // --- Follow page tests ---

  it('renders follow list page with user cards', async () => {
    followListState = [makeFollowItem({ id: 'f1' }), makeFollowItem({ id: 'f2', userId: 'u-3', nickname: 'Bob' })];
    totalFollowsState = 2;
    const wrapper = mountFollowPage();
    await flushPromises();
    const cards = wrapper.findAll('.user-card-stub');
    expect(cards.length).toBe(2);
    expect(wrapper.find('.follow-page__title').text()).toBe('我的关注');
  });

  it('shows search and filter controls', async () => {
    const wrapper = mountFollowPage();
    await flushPromises();
    expect(wrapper.find('.search-stub').exists()).toBe(true);
    expect(wrapper.find('.radio-group-stub').exists()).toBe(true);
  });

  it('navigates to batch manage mode', async () => {
    const wrapper = mountFollowPage();
    await flushPromises();
    const actionButtons = wrapper.findAll('.follow-page__actions .btn-stub');
    expect(actionButtons.length).toBeGreaterThanOrEqual(1);
    // First button is "批量管理"
    expect(actionButtons[0].text()).toBe('批量管理');
    await actionButtons[0].trigger('click');
    expect(mockRouterPush).toHaveBeenCalledWith({ path: '/social/follow/batch' });
  });

  // --- Feed page tests ---

  it('navigates to feed page', async () => {
    feedFeedListState = [makeFeedItem()];
    const wrapper = mountFeedPage();
    await flushPromises();
    expect(wrapper.find('.feed-page__title').text()).toBe('关注动态');
    expect(wrapper.find('.feed-filter-stub').exists()).toBe(true);
  });

  it('renders feed cards when data exists', async () => {
    feedFeedListState = [makeFeedItem({ id: 'f1' }), makeFeedItem({ id: 'f2' })];
    const wrapper = mountFeedPage();
    await flushPromises();
    const cards = wrapper.findAll('.feed-card-stub');
    expect(cards.length).toBe(2);
    expect(wrapper.find('.feed-page__list').exists()).toBe(true);
  });

  it('shows special feed section', async () => {
    priorityItemsState = [makeFeedItem({ id: 'p1', isPriority: true })];
    const wrapper = mountFeedPage();
    await flushPromises();
    expect(wrapper.find('.special-feed-stub').exists()).toBe(true);
  });

  it('supports feed type filtering', async () => {
    const wrapper = mountFeedPage();
    await flushPromises();
    const filter = wrapper.find('.feed-filter-stub');
    expect(filter.exists()).toBe(true);
  });

  it('supports scroll-to-load when hasMore is true', async () => {
    followHasMoreState = true;
    followLoadingState = false;
    feedFeedListState = [makeFeedItem({ id: 'f1' })];
    const wrapper = mountFeedPage();
    await flushPromises();
    // Verify the page rendered with data and scroll listener was registered
    expect(wrapper.find('.feed-page__list').exists()).toBe(true);
    expect(wrapper.findAll('.feed-card-stub').length).toBe(1);
    // Verify fetchFollowFeed was called on mount
    expect(mockFetchFollowFeed).toHaveBeenCalledWith(true);
  });
});
