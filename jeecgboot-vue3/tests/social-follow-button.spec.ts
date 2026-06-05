import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';

// Mock follow store
const mockFollow = jest.fn();
const mockUnfollow = jest.fn();

jest.mock('/@/store/modules/follow', () => ({
  useFollowStore: () => ({
    follow: mockFollow,
    unfollow: mockUnfollow,
  }),
}));

jest.mock('/@/store', () => ({ store: {} }));

// Mock ant-design-vue
jest.mock('ant-design-vue', () => ({
  message: { success: jest.fn(), error: jest.fn() },
}));

// Mock ant-design-vue components used in template
jest.mock('ant-design-vue/es/button', () => ({
  default: { name: 'AButton', template: '<button :disabled="disabled"><slot /></button>', props: ['type', 'loading', 'disabled'] },
}));

jest.mock('ant-design-vue/es/popconfirm', () => ({
  default: { name: 'APopconfirm', template: '<div class="popconfirm"><slot /></div>', props: ['title', 'okText', 'cancelText'] },
}));

describe('FollowButton.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockFollow.mockResolvedValue(undefined);
    mockUnfollow.mockResolvedValue(undefined);
  });

  async function mountComponent(props: {
    userId?: string;
    targetUserId?: string;
    isFollowing?: boolean;
    disabled?: boolean;
  } = {}) {
    const Component = (await import('/@/components/social/FollowButton.vue')).default;
    return mount(Component, {
      props: {
        userId: 'u-1',
        targetUserId: 'u-2',
        isFollowing: false,
        ...props,
      },
      global: {
        stubs: {
          Popconfirm: { template: '<div class="popconfirm-stub"><slot /></div>', props: ['title'] },
          Button: { template: '<button class="btn-stub" :disabled="disabled"><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
  }

  describe('button text', () => {
    it('shows "关注" when not following', async () => {
      const wrapper = await mountComponent({ isFollowing: false });
      expect(wrapper.find('.btn-stub').text()).toBe('关注');
    });

    it('shows "已关注" when following', async () => {
      const wrapper = await mountComponent({ isFollowing: true });
      expect(wrapper.find('.btn-stub').text()).toBe('已关注');
    });

    it('shows "自己" when userId === targetUserId', async () => {
      const wrapper = await mountComponent({ userId: 'u-1', targetUserId: 'u-1' });
      expect(wrapper.find('.btn-stub').text()).toBe('自己');
    });
  });

  describe('handleFollow', () => {
    it('calls followStore.follow and emits events', async () => {
      const wrapper = await mountComponent({ isFollowing: false });
      await wrapper.find('.btn-stub').trigger('click');
      await nextTick();

      expect(mockFollow).toHaveBeenCalledWith('u-1', 'u-2');
      expect(wrapper.emitted('update:isFollowing')?.[0]).toEqual([true]);
      expect(wrapper.emitted('follow')).toBeTruthy();
    });

    it('optimistically sets following state', async () => {
      const wrapper = await mountComponent({ isFollowing: false });
      await wrapper.find('.btn-stub').trigger('click');
      await nextTick();

      // Should immediately show "已关注"
      expect(wrapper.find('.btn-stub').text()).toBe('已关注');
    });

    it('rolls back on failure', async () => {
      mockFollow.mockRejectedValue(new Error('fail'));
      const wrapper = await mountComponent({ isFollowing: false });
      await wrapper.find('.btn-stub').trigger('click');
      await nextTick();
      await new Promise((r) => setTimeout(r, 0));
      await nextTick();

      expect(wrapper.find('.btn-stub').text()).toBe('关注');
      expect(wrapper.emitted('update:isFollowing')?.[0]).toEqual([true]);
      // Second emission is the rollback
      expect(wrapper.emitted('update:isFollowing')?.[1]).toEqual([false]);
    });
  });

  describe('handleUnfollow', () => {
    it('calls followStore.unfollow via popconfirm confirm', async () => {
      const wrapper = await mountComponent({ isFollowing: true });
      // Trigger unfollow by calling the component's internal method
      const vm = wrapper.vm as any;
      await vm.handleUnfollow();
      await nextTick();

      expect(mockUnfollow).toHaveBeenCalledWith('u-1', 'u-2');
      expect(wrapper.emitted('update:isFollowing')?.[0]).toEqual([false]);
      expect(wrapper.emitted('unfollow')).toBeTruthy();
    });

    it('rolls back on unfollow failure', async () => {
      mockUnfollow.mockRejectedValue(new Error('fail'));
      const wrapper = await mountComponent({ isFollowing: true });
      const vm = wrapper.vm as any;
      await vm.handleUnfollow();
      await nextTick();
      await new Promise((r) => setTimeout(r, 0));
      await nextTick();

      // Should rollback to following
      expect(wrapper.emitted('update:isFollowing')?.[1]).toEqual([true]);
    });
  });

  describe('disabled state', () => {
    it('disables button when disabled prop is true', async () => {
      const wrapper = await mountComponent({ disabled: true });
      const btn = wrapper.find('.btn-stub');
      expect(btn.attributes('disabled')).toBeDefined();
    });

    it('disables button when isSelf', async () => {
      const wrapper = await mountComponent({ userId: 'u-1', targetUserId: 'u-1' });
      const btn = wrapper.find('.btn-stub');
      expect(btn.attributes('disabled')).toBeDefined();
    });
  });

  describe('watch prop changes', () => {
    it('syncs isFollowing prop to internal state', async () => {
      const wrapper = await mountComponent({ isFollowing: false });
      expect(wrapper.find('.btn-stub').text()).toBe('关注');

      await wrapper.setProps({ isFollowing: true });
      await nextTick();
      expect(wrapper.find('.btn-stub').text()).toBe('已关注');
    });
  });
});
