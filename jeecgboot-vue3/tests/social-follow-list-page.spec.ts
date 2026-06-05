import { mount } from '@vue/test-utils';

// --- Mock functions ---
const mockFetchFollowList = jest.fn().mockResolvedValue(undefined);
const mockFetchFollowGroups = jest.fn().mockResolvedValue(undefined);
const mockSetSearchKeyword = jest.fn();
const mockSetSelectedGroupId = jest.fn();
const mockUnfollow = jest.fn();
const mockSetSpecial = jest.fn();
const mockCancelSpecial = jest.fn();
const mockRouterPush = jest.fn();

// --- Store state (mutable for per-test overrides) ---
let followListState: any[] = [];
let followGroupsState: any[] = [];
let followListLoadingState = false;
let totalFollowsState = 0;
let hasMoreState = false;

jest.mock('/@/store/modules/follow', () => ({
  useFollowStore: () => ({
    get followList() { return followListState; },
    get followGroups() { return followGroupsState; },
    get followListLoading() { return followListLoadingState; },
    get totalFollows() { return totalFollowsState; },
    get hasMore() { return hasMoreState; },
    fetchFollowList: mockFetchFollowList,
    fetchFollowGroups: mockFetchFollowGroups,
    setSearchKeyword: mockSetSearchKeyword,
    setSelectedGroupId: mockSetSelectedGroupId,
    unfollow: mockUnfollow,
    setSpecial: mockSetSpecial,
    cancelSpecial: mockCancelSpecial,
  }),
}));

jest.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({
    getUserInfo: { userId: 'u-1' },
  }),
}));

jest.mock('/@/store', () => ({ store: {} }));

jest.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
}));

jest.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'MD' } }),
}));

// --- Import component after mocks ---
let FollowPage: any;

beforeAll(async () => {
  const mod = await import('/@/views/social/follow/index.vue');
  FollowPage = mod.default;
});

const stubs = {
  'a-input-search': { template: '<input class="search-stub" />', props: ['value', 'placeholder'] },
  'a-radio-group': { template: '<div class="radio-group-stub"><slot /></div>', props: ['value'] },
  'a-radio-button': { template: '<button class="radio-btn-stub"><slot /></button>', props: ['value'] },
  'a-spin': { template: '<div class="spin-stub"><slot /></div>', props: ['spinning'] },
  'a-empty': { template: '<div class="empty-stub"><slot /></div>', props: ['description'] },
  'a-button': { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
  UserCard: { template: '<div class="user-card-stub" />', props: ['userId', 'user', 'isMobile'] },
};

function createWrapper() {
  return mount(FollowPage, {
    global: { stubs },
  });
}

// --- Tests ---
describe('follow/index.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    followListState = [];
    followGroupsState = [];
    followListLoadingState = false;
    totalFollowsState = 0;
    hasMoreState = false;
  });

  it('renders page title "我的关注"', () => {
    const wrapper = createWrapper();
    const title = wrapper.find('.follow-page__title');
    expect(title.exists()).toBe(true);
    expect(title.text()).toBe('我的关注');
  });

  it('renders follow count', () => {
    totalFollowsState = 42;
    const wrapper = createWrapper();
    const count = wrapper.find('.follow-page__count');
    expect(count.exists()).toBe(true);
    expect(count.text()).toContain('42');
  });

  it('calls fetchFollowGroups and fetchFollowList on mount', () => {
    createWrapper();
    expect(mockFetchFollowGroups).toHaveBeenCalledWith('u-1');
    expect(mockFetchFollowList).toHaveBeenCalledWith('u-1', true);
  });

  it('shows UserCard list when followList has items', () => {
    followListState = [
      { id: 'f1', userId: 'u-2', nickname: 'Alice', avatar: '', bio: '', followTime: '', groupId: '', isSpecial: false },
      { id: 'f2', userId: 'u-3', nickname: 'Bob', avatar: '', bio: '', followTime: '', groupId: '', isSpecial: false },
    ];
    const wrapper = createWrapper();
    const cards = wrapper.findAll('.user-card-stub');
    expect(cards.length).toBe(2);
    expect(wrapper.find('.empty-stub').exists()).toBe(false);
  });

  it('shows empty state when list is empty and not loading', () => {
    followListState = [];
    followListLoadingState = false;
    const wrapper = createWrapper();
    const empty = wrapper.find('.empty-stub');
    expect(empty.exists()).toBe(true);
  });

  it('shows "加载更多" button when hasMore is true and list has items', () => {
    followListState = [
      { id: 'f1', userId: 'u-2', nickname: 'Alice', avatar: '', bio: '', followTime: '', groupId: '', isSpecial: false },
    ];
    hasMoreState = true;
    const wrapper = createWrapper();
    const loadMoreBtn = wrapper.find('.follow-page__load-more .btn-stub');
    expect(loadMoreBtn.exists()).toBe(true);
    expect(loadMoreBtn.text()).toBe('加载更多');
  });

  it('hides "加载更多" button when hasMore is false', () => {
    followListState = [
      { id: 'f1', userId: 'u-2', nickname: 'Alice', avatar: '', bio: '', followTime: '', groupId: '', isSpecial: false },
    ];
    hasMoreState = false;
    const wrapper = createWrapper();
    expect(wrapper.find('.follow-page__load-more').exists()).toBe(false);
  });

  it('shows loading state when followListLoading is true', () => {
    followListLoadingState = true;
    const wrapper = createWrapper();
    const spin = wrapper.find('.spin-stub');
    expect(spin.exists()).toBe(true);
  });

  it('has search input', () => {
    const wrapper = createWrapper();
    expect(wrapper.find('.search-stub').exists()).toBe(true);
  });

  it('has group filter radio buttons (default "全部" button)', () => {
    const wrapper = createWrapper();
    const radioGroup = wrapper.find('.radio-group-stub');
    expect(radioGroup.exists()).toBe(true);
    const defaultBtn = wrapper.find('.radio-btn-stub');
    expect(defaultBtn.exists()).toBe(true);
    expect(defaultBtn.text()).toBe('全部');
  });

  it('renders additional radio buttons for follow groups', () => {
    followGroupsState = [
      { id: 'g1', name: '朋友' },
      { id: 'g2', name: '同事' },
    ];
    const wrapper = createWrapper();
    const radioBtns = wrapper.findAll('.radio-btn-stub');
    // 1 default "全部" + 2 group buttons
    expect(radioBtns.length).toBe(3);
    expect(radioBtns[1].text()).toBe('朋友');
    expect(radioBtns[2].text()).toBe('同事');
  });

  it('has action buttons (批量管理, 分组管理, 推荐关注)', () => {
    const wrapper = createWrapper();
    const actions = wrapper.find('.follow-page__actions');
    expect(actions.exists()).toBe(true);
    const buttons = actions.findAll('.btn-stub');
    expect(buttons.length).toBe(3);
    expect(buttons[0].text()).toBe('批量管理');
    expect(buttons[1].text()).toBe('分组管理');
    expect(buttons[2].text()).toBe('推荐关注');
  });
});
