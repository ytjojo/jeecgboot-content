import { vi } from 'vitest';
import { mount } from '@vue/test-utils';

// Mock follow store
const mockSetSpecial = vi.fn();
const mockCancelSpecial = vi.fn();
const mockUnfollow = vi.fn();

vi.mock('/@/store/modules/follow', () => ({
  useFollowStore: () => ({
    setSpecial: mockSetSpecial,
    cancelSpecial: mockCancelSpecial,
    unfollow: mockUnfollow,
  }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('ant-design-vue', () => ({
  message: { success: vi.fn(), error: vi.fn() },
}));

function makeUser(overrides: Record<string, any> = {}) {
  return {
    userId: 'u-2',
    nickname: 'Test User',
    avatar: 'https://cdn/avatar.png',
    bio: 'A short bio',
    followTime: '2025-06-01',
    groupName: 'Friends',
    isSpecial: false,
    ...overrides,
  };
}

describe('UserCard.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSetSpecial.mockResolvedValue(undefined);
    mockCancelSpecial.mockResolvedValue(undefined);
    mockUnfollow.mockResolvedValue(undefined);
  });

  async function mountComponent(
    userOverrides: Record<string, any> = {},
    props: { isMobile?: boolean } = {},
  ) {
    const Component = (await import('/@/components/social/UserCard.vue')).default;
    const user = makeUser(userOverrides);
    return mount(Component, {
      props: {
        userId: 'u-1',
        user,
        ...props,
      },
      global: {
        stubs: {
          Avatar: { template: '<div class="avatar-stub" />', props: ['size', 'src'] },
          Tag: { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
          SpecialFollowButton: {
            template: '<button class="special-btn-stub" />',
            props: ['userId', 'targetUserId', 'isFollowing', 'isSpecial'],
          },
          FollowButton: {
            template: '<button class="follow-btn-stub" />',
            props: ['userId', 'targetUserId', 'isFollowing'],
          },
          Dropdown: { template: '<div class="dropdown-stub"><slot /><slot name="overlay" /></div>', props: ['trigger'] },
          Menu: {
            template: '<div class="menu-stub"><slot /></div>',
            props: [],
          },
          'Menu.Item': {
            template: '<div class="menu-item-stub"><slot /></div>',
            props: ['key'],
          },
          Button: { template: '<button class="btn-stub"><slot /></button>', props: ['type', 'loading'] },
        },
      },
    });
  }

  describe('desktop rendering', () => {
    it('renders nickname', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('.user-card__nickname').text()).toBe('Test User');
    });

    it('renders avatar', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('.avatar-stub').exists()).toBe(true);
    });

    it('renders bio on desktop', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      expect(wrapper.find('.user-card__bio').text()).toBe('A short bio');
    });

    it('renders follow time on desktop', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      expect(wrapper.find('.user-card__time').text()).toContain('2025-06-01');
    });

    it('renders group tag when groupName exists', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('.tag-stub').text()).toBe('Friends');
    });

    it('does not render group tag when groupName is empty', async () => {
      const wrapper = await mountComponent({ groupName: '' });
      expect(wrapper.find('.tag-stub').exists()).toBe(false);
    });

    it('shows special badge when isSpecial is true', async () => {
      const wrapper = await mountComponent({ isSpecial: true });
      expect(wrapper.find('.user-card__special-badge').exists()).toBe(true);
    });

    it('does not show special badge when isSpecial is false', async () => {
      const wrapper = await mountComponent({ isSpecial: false });
      expect(wrapper.find('.user-card__special-badge').exists()).toBe(false);
    });
  });

  describe('mobile rendering', () => {
    it('adds is-mobile class', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.is-mobile').exists()).toBe(true);
    });

    it('hides bio on mobile', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.user-card__bio').exists()).toBe(false);
    });

    it('hides follow time on mobile', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.user-card__time').exists()).toBe(false);
    });

    it('shows dropdown menu on mobile', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.dropdown-stub').exists()).toBe(true);
    });

    it('hides follow/special buttons on mobile', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.special-btn-stub').exists()).toBe(false);
      expect(wrapper.find('.follow-btn-stub').exists()).toBe(false);
    });
  });

  describe('desktop actions', () => {
    it('shows FollowButton on desktop', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      expect(wrapper.find('.follow-btn-stub').exists()).toBe(true);
    });

    it('shows SpecialFollowButton on desktop', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      expect(wrapper.find('.special-btn-stub').exists()).toBe(true);
    });
  });

  describe('events', () => {
    it('emits unfollow when FollowButton triggers unfollow', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      const vm = wrapper.vm as any;
      vm.handleUnfollow();
      expect(wrapper.emitted('unfollow')?.[0]).toEqual(['u-2']);
    });

    it('emits specialChange with correct args', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      const vm = wrapper.vm as any;
      vm.handleSpecialChange(true);
      expect(wrapper.emitted('specialChange')?.[0]).toEqual(['u-2', true]);
    });

    it('emits specialChange with false', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      const vm = wrapper.vm as any;
      vm.handleSpecialChange(false);
      expect(wrapper.emitted('specialChange')?.[0]).toEqual(['u-2', false]);
    });
  });

  describe('mobile menu actions', () => {
    it('emits groupChange when menu key is group', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      const vm = wrapper.vm as any;
      vm.handleMenuClick({ key: 'group' });
      expect(wrapper.emitted('groupChange')?.[0]).toEqual(['u-2']);
    });

    it('calls followStore.unfollow when menu key is unfollow', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      const vm = wrapper.vm as any;
      await vm.handleMenuClick({ key: 'unfollow' });
      expect(mockUnfollow).toHaveBeenCalledWith('u-1', 'u-2');
    });

    it('emits unfollow after successful unfollow via menu', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      const vm = wrapper.vm as any;
      await vm.handleMenuClick({ key: 'unfollow' });
      // Wait for promise
      await new Promise((r) => setTimeout(r, 0));
      expect(wrapper.emitted('unfollow')?.[0]).toEqual(['u-2']);
    });

    it('calls followStore.setSpecial when menu key is special and not currently special', async () => {
      const wrapper = await mountComponent({ isSpecial: false }, { isMobile: true });
      const vm = wrapper.vm as any;
      await vm.handleMenuClick({ key: 'special' });
      expect(mockSetSpecial).toHaveBeenCalledWith('u-1', 'u-2');
    });

    it('calls followStore.cancelSpecial when menu key is special and currently special', async () => {
      const wrapper = await mountComponent({ isSpecial: true }, { isMobile: true });
      const vm = wrapper.vm as any;
      await vm.handleMenuClick({ key: 'special' });
      expect(mockCancelSpecial).toHaveBeenCalledWith('u-1', 'u-2');
    });
  });
});
