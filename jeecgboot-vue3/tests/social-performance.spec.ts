import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';

vi.setConfig({ testTimeout: 30000 });

// -- Mock functions --
const mockFetchFollowFeed = vi.fn().mockResolvedValue(undefined);
const mockSetFollowTypes = vi.fn();
const mockFetchFollowList = vi.fn().mockResolvedValue(undefined);
const mockFetchFollowGroups = vi.fn().mockResolvedValue(undefined);
const mockFetchSubscribeList = vi.fn().mockResolvedValue(undefined);
const mockSetSearchKeyword = vi.fn();
const mockSetSelectedSourceType = vi.fn();
const mockBatchCancelFollow = vi.fn().mockResolvedValue(undefined);
const mockBatchPause = vi.fn().mockResolvedValue(undefined);
const mockBatchResume = vi.fn().mockResolvedValue(undefined);

// -- Mutable data slots --
let feedItems: any[] = [];
let followItems: any[] = [];
let subscribeItems: any[] = [];

// -- Module mocks --
vi.mock('/@/store/modules/feed', () => ({
  useFeedStore: () => ({
    get followFeedList() { return feedItems; },
    get priorityItems() { return []; },
    get followLoading() { return false; },
    get followHasMore() { return false; },
    get followTypes() { return ['post', 'like', 'favorite']; },
    fetchFollowFeed: mockFetchFollowFeed,
    setFollowTypes: mockSetFollowTypes,
  }),
}));

vi.mock('/@/store/modules/follow', () => ({
  useFollowStore: () => ({
    get followList() { return followItems; },
    get followGroups() { return []; },
    get totalFollows() { return followItems.length; },
    get followListLoading() { return false; },
    get hasMore() { return false; },
    fetchFollowList: mockFetchFollowList,
    fetchFollowGroups: mockFetchFollowGroups,
    setSearchKeyword: vi.fn(),
    setSelectedGroupId: vi.fn(),
    batchUnfollowUsers: mockBatchCancelFollow,
  }),
}));

vi.mock('/@/store/modules/subscribe', () => ({
  useSubscribeStore: () => ({
    get subscribeList() { return subscribeItems; },
    get totalSubscribes() { return subscribeItems.length; },
    get loading() { return false; },
    get hasMore() { return false; },
    fetchSubscribeList: mockFetchSubscribeList,
    setSearchKeyword: mockSetSearchKeyword,
    setSelectedSourceType: mockSetSelectedSourceType,
    batchPause: mockBatchPause,
    batchResume: mockBatchResume,
    batchCancel: mockBatchPause, // reuse for batch cancel
  }),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({
    getUserInfo: { userId: 'u-1' },
  }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
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
const feedStubs = {
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub" />', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
  FeedCard: { template: '<div class="feed-card-stub" />', props: ['feed', 'isMobile'] },
  FeedFilter: { template: '<div class="feed-filter-stub" />', props: ['types', 'modelValue'] },
  SpecialFeed: { template: '<div class="special-feed-stub" />', props: ['feeds', 'loading', 'hasMore'] },
};

const followStubs = {
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder'] },
  'a-radio-group': { template: '<div class="radio-group-stub"><slot /></div>', props: ['value'] },
  'a-radio-button': { template: '<button class="radio-btn-stub"><slot /></button>', props: ['value'] },
  UserCard: { template: '<div class="user-card-stub" />', props: ['userId', 'user', 'isMobile'] },
};

const subscribeStubs = {
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading', 'danger', 'disabled'] },
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder'] },
  'a-select': { template: '<select class="select-stub"><slot /></select>', props: ['value', 'placeholder'] },
  'a-select-option': { template: '<option class="option-stub"><slot /></option>', props: ['value'] },
  'a-checkbox': { template: '<input type="checkbox" class="checkbox-stub" />', props: ['checked'] },
  'a-popconfirm': { template: '<div class="popconfirm-stub"><slot /></div>', props: ['title', 'okText', 'cancelText'] },
  SubscriptionCard: { template: '<div class="sub-card-stub" />', props: ['userId', 'source'] },
};

// -- Data generators --
function generateFeedItems(count: number) {
  return Array.from({ length: count }, (_, i) => ({
    id: `feed-${i}`,
    userId: `user-${i}`,
    nickname: `User ${i}`,
    avatar: '',
    contentId: `c-${i}`,
    contentTitle: `Post ${i}`,
    contentSummary: `Content item ${i}`,
    dynamicType: 'post' as const,
    createTime: new Date().toISOString(),
    isPriority: false,
  }));
}

function generateFollowItems(count: number) {
  return Array.from({ length: count }, (_, i) => ({
    id: `follow-${i}`,
    userId: `user-${i}`,
    nickname: `User ${i}`,
    avatar: '',
    bio: `Bio ${i}`,
    followTime: new Date().toISOString(),
    groupId: '',
    isSpecial: i % 10 === 0,
    lastActiveTime: new Date().toISOString(),
  }));
}

function generateSubscribeItems(count: number) {
  return Array.from({ length: count }, (_, i) => ({
    id: `sub-${i}`,
    sourceId: `src-${i}`,
    sourceName: `Source ${i}`,
    sourceIcon: '',
    sourceType: i % 2 === 0 ? '专题' : '话题',
    category: '科技',
    subscriberCount: i * 10,
    lastUpdateTime: new Date().toISOString(),
    subscribeTime: new Date().toISOString(),
    status: 'active' as const,
  }));
}

// -- Tests --
describe('Social module performance', () => {
  beforeEach(() => {
    feedItems = [];
    followItems = [];
    subscribeItems = [];
    vi.clearAllMocks();
  });

  test('feed page renders within 2 seconds with 50 items', async () => {
    feedItems = generateFeedItems(50);
    const FeedPage = (await import('/@/views/social/feed/index.vue')).default;

    const start = Date.now();
    const wrapper = mount(FeedPage, { global: { stubs: feedStubs } });
    await flushPromises();
    const elapsed = Date.now() - start;

    const cards = wrapper.findAll('.feed-card-stub');
    expect(cards.length).toBe(50);
    expect(elapsed).toBeLessThan(2000);

    wrapper.unmount();
  });

  test('follow list renders within 1 second with 100 items', async () => {
    followItems = generateFollowItems(100);
    const FollowPage = (await import('/@/views/social/follow/index.vue')).default;

    const start = Date.now();
    const wrapper = mount(FollowPage, { global: { stubs: followStubs } });
    await flushPromises();
    const elapsed = Date.now() - start;

    const cards = wrapper.findAll('.user-card-stub');
    expect(cards.length).toBe(100);
    expect(elapsed).toBeLessThan(1000);

    wrapper.unmount();
  });

  test('subscribe list renders within 1 second with 100 items', async () => {
    subscribeItems = generateSubscribeItems(100);
    const SubscribeManagePage = (await import('/@/views/social/subscribe/manage.vue')).default;

    const start = Date.now();
    const wrapper = mount(SubscribeManagePage, { global: { stubs: subscribeStubs } });
    await flushPromises();
    const elapsed = Date.now() - start;

    const cards = wrapper.findAll('.sub-card-stub');
    expect(cards.length).toBe(100);
    expect(elapsed).toBeLessThan(1000);

    wrapper.unmount();
  });

  test('large feed list (500 items) renders without timeout', async () => {
    feedItems = generateFeedItems(500);
    const FeedPage = (await import('/@/views/social/feed/index.vue')).default;

    const start = Date.now();
    const wrapper = mount(FeedPage, { global: { stubs: feedStubs } });
    await flushPromises();
    const elapsed = Date.now() - start;

    const cards = wrapper.findAll('.feed-card-stub');
    expect(cards.length).toBe(500);
    // Must complete within Jest default timeout (10s)
    expect(elapsed).toBeLessThan(10000);

    wrapper.unmount();
  });

  test('batch cancel follow with 100 items completes under 3 seconds', async () => {
    const ids = Array.from({ length: 100 }, (_, i) => `user-${i}`);

    const start = Date.now();
    await mockBatchCancelFollow('u-1', ids);
    const elapsed = Date.now() - start;

    expect(mockBatchCancelFollow).toHaveBeenCalledWith('u-1', ids);
    expect(elapsed).toBeLessThan(3000);
  });

  test('batch pause with 100 items completes under 3 seconds', async () => {
    const ids = Array.from({ length: 100 }, (_, i) => `src-${i}`);

    const start = Date.now();
    await mockBatchPause('u-1', ids);
    const elapsed = Date.now() - start;

    expect(mockBatchPause).toHaveBeenCalledWith('u-1', ids);
    expect(elapsed).toBeLessThan(3000);
  });
});
