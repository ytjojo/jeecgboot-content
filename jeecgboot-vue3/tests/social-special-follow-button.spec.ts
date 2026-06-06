import { vi } from 'vitest';
import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';

// Mock follow store
const mockSetSpecial = vi.fn();
const mockCancelSpecial = vi.fn();

vi.mock('/@/store/modules/follow', () => ({
  useFollowStore: () => ({
    setSpecial: mockSetSpecial,
    cancelSpecial: mockCancelSpecial,
  }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('ant-design-vue', () => ({
  message: { success: vi.fn(), error: vi.fn() },
}));

describe('SpecialFollowButton.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSetSpecial.mockResolvedValue(undefined);
    mockCancelSpecial.mockResolvedValue(undefined);
  });

  async function mountComponent(props: {
    userId?: string;
    targetUserId?: string;
    isFollowing?: boolean;
    isSpecial?: boolean;
  } = {}) {
    const Component = (await import('/@/components/social/SpecialFollowButton.vue')).default;
    return mount(Component, {
      props: {
        userId: 'u-1',
        targetUserId: 'u-2',
        isFollowing: true,
        isSpecial: false,
        ...props,
      },
      global: {
        stubs: {
          Popconfirm: { template: '<div class="popconfirm-stub"><slot /></div>', props: ['title', 'okText', 'cancelText'] },
          Button: { template: '<button class="btn-stub" :disabled="disabled"><slot /></button>', props: ['type', 'loading', 'disabled'] },
          Tooltip: { template: '<div class="tooltip-stub"><slot /></div>', props: ['title'] },
        },
      },
    });
  }

  describe('when not following', () => {
    it('shows disabled button with tooltip', async () => {
      const wrapper = await mountComponent({ isFollowing: false });
      expect(wrapper.find('.tooltip-stub').exists()).toBe(true);
      expect(wrapper.find('.btn-stub').attributes('disabled')).toBeDefined();
    });
  });

  describe('when following', () => {
    it('shows star button without tooltip', async () => {
      const wrapper = await mountComponent({ isFollowing: true });
      expect(wrapper.find('.tooltip-stub').exists()).toBe(false);
      expect(wrapper.find('.popconfirm-stub').exists()).toBe(true);
    });

    it('shows filled star when isSpecial is true', async () => {
      const wrapper = await mountComponent({ isFollowing: true, isSpecial: true });
      expect(wrapper.find('.special-star.filled').exists()).toBe(true);
    });

    it('shows outlined star when isSpecial is false', async () => {
      const wrapper = await mountComponent({ isFollowing: true, isSpecial: false });
      expect(wrapper.find('.special-star').exists()).toBe(true);
      expect(wrapper.find('.special-star.filled').exists()).toBe(false);
    });
  });

  describe('handleToggle', () => {
    it('calls setSpecial and emits events when not special', async () => {
      const wrapper = await mountComponent({ isSpecial: false });
      const vm = wrapper.vm as any;
      await vm.handleToggle();
      await nextTick();

      expect(mockSetSpecial).toHaveBeenCalledWith('u-1', 'u-2');
      expect(wrapper.emitted('update:isSpecial')?.[0]).toEqual([true]);
      expect(wrapper.emitted('special')).toBeTruthy();
    });

    it('calls cancelSpecial and emits events when special', async () => {
      const wrapper = await mountComponent({ isSpecial: true });
      const vm = wrapper.vm as any;
      await vm.handleToggle();
      await nextTick();

      expect(mockCancelSpecial).toHaveBeenCalledWith('u-1', 'u-2');
      expect(wrapper.emitted('update:isSpecial')?.[0]).toEqual([false]);
      expect(wrapper.emitted('cancelSpecial')).toBeTruthy();
    });

    it('optimistically updates state', async () => {
      const wrapper = await mountComponent({ isSpecial: false });
      const vm = wrapper.vm as any;
      await vm.handleToggle();
      await nextTick();

      expect(wrapper.find('.special-star.filled').exists()).toBe(true);
    });

    it('rolls back on failure', async () => {
      mockSetSpecial.mockRejectedValue(new Error('fail'));
      const wrapper = await mountComponent({ isSpecial: false });
      const vm = wrapper.vm as any;
      await vm.handleToggle();
      await nextTick();
      await new Promise((r) => setTimeout(r, 0));
      await nextTick();

      expect(wrapper.emitted('update:isSpecial')?.[0]).toEqual([true]);
      expect(wrapper.emitted('update:isSpecial')?.[1]).toEqual([false]);
    });
  });

  describe('watch prop changes', () => {
    it('syncs isSpecial prop to internal state', async () => {
      const wrapper = await mountComponent({ isSpecial: false });
      expect(wrapper.find('.special-star.filled').exists()).toBe(false);

      await wrapper.setProps({ isSpecial: true });
      await nextTick();
      expect(wrapper.find('.special-star.filled').exists()).toBe(true);
    });
  });
});
