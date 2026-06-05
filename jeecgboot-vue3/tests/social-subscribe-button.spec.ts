import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';

// Mock subscribe store
const mockSubscribe = jest.fn();
const mockUnsubscribe = jest.fn();

jest.mock('/@/store/modules/subscribe', () => ({
  useSubscribeStore: () => ({
    subscribe: mockSubscribe,
    unsubscribe: mockUnsubscribe,
  }),
}));

jest.mock('/@/store', () => ({ store: {} }));

jest.mock('ant-design-vue', () => ({
  message: { success: jest.fn(), error: jest.fn() },
}));

describe('SubscribeButton.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSubscribe.mockResolvedValue(undefined);
    mockUnsubscribe.mockResolvedValue(undefined);
  });

  async function mountComponent(props: {
    userId?: string;
    sourceId?: string;
    sourceType?: string;
    isSubscribed?: boolean;
    isPaused?: boolean;
    disabled?: boolean;
  } = {}) {
    const Component = (await import('/@/components/social/SubscribeButton.vue')).default;
    return mount(Component, {
      props: {
        userId: 'u-1',
        sourceId: 'src-1',
        sourceType: 'RSS',
        isSubscribed: false,
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
    it('shows "订阅" when not subscribed', async () => {
      const wrapper = await mountComponent({ isSubscribed: false });
      expect(wrapper.find('.btn-stub').text()).toBe('订阅');
    });

    it('shows "已订阅" when subscribed', async () => {
      const wrapper = await mountComponent({ isSubscribed: true });
      expect(wrapper.find('.btn-stub').text()).toBe('已订阅');
    });
  });

  describe('handleSubscribe', () => {
    it('calls subscribeStore.subscribe and emits events', async () => {
      const wrapper = await mountComponent({ isSubscribed: false });
      await wrapper.find('.btn-stub').trigger('click');
      await nextTick();

      expect(mockSubscribe).toHaveBeenCalledWith('u-1', 'src-1', 'RSS');
      expect(wrapper.emitted('update:isSubscribed')?.[0]).toEqual([true]);
      expect(wrapper.emitted('subscribe')).toBeTruthy();
    });

    it('optimistically sets subscribed state', async () => {
      const wrapper = await mountComponent({ isSubscribed: false });
      await wrapper.find('.btn-stub').trigger('click');
      await nextTick();

      expect(wrapper.find('.btn-stub').text()).toBe('已订阅');
    });

    it('rolls back on failure', async () => {
      mockSubscribe.mockRejectedValue(new Error('fail'));
      const wrapper = await mountComponent({ isSubscribed: false });
      await wrapper.find('.btn-stub').trigger('click');
      await nextTick();
      await new Promise((r) => setTimeout(r, 0));
      await nextTick();

      expect(wrapper.find('.btn-stub').text()).toBe('订阅');
    });
  });

  describe('handleUnsubscribe', () => {
    it('calls subscribeStore.unsubscribe and emits events', async () => {
      const wrapper = await mountComponent({ isSubscribed: true });
      const vm = wrapper.vm as any;
      await vm.handleUnsubscribe();
      await nextTick();

      expect(mockUnsubscribe).toHaveBeenCalledWith('u-1', 'src-1');
      expect(wrapper.emitted('update:isSubscribed')?.[0]).toEqual([false]);
      expect(wrapper.emitted('unsubscribe')).toBeTruthy();
    });

    it('rolls back on unsub failure', async () => {
      mockUnsubscribe.mockRejectedValue(new Error('fail'));
      const wrapper = await mountComponent({ isSubscribed: true });
      const vm = wrapper.vm as any;
      await vm.handleUnsubscribe();
      await nextTick();
      await new Promise((r) => setTimeout(r, 0));
      await nextTick();

      expect(wrapper.emitted('update:isSubscribed')?.[1]).toEqual([true]);
    });
  });

  describe('disabled state', () => {
    it('disables button when disabled prop is true', async () => {
      const wrapper = await mountComponent({ disabled: true });
      expect(wrapper.html()).toContain('disabled');
    });
  });

  describe('watch prop changes', () => {
    it('syncs isSubscribed prop to internal state', async () => {
      const wrapper = await mountComponent({ isSubscribed: false });
      expect(wrapper.find('.btn-stub').text()).toBe('订阅');

      await wrapper.setProps({ isSubscribed: true });
      await nextTick();
      expect(wrapper.find('.btn-stub').text()).toBe('已订阅');
    });
  });
});
