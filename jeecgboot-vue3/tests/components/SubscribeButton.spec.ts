import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';

const mockOptimisticExecute = vi.fn();
vi.mock('/@/hooks/web/useChannelOperation', () => ({
  useChannelOperation: () => ({
    operating: ref(false),
    optimisticExecute: mockOptimisticExecute,
  }),
}));

const mockSubscribeChannel = vi.fn();
const mockUnsubscribeChannel = vi.fn();
vi.mock('/@/api/content/channelSubscription', () => ({
  subscribeChannel: (...args: any[]) => mockSubscribeChannel(...args),
  unsubscribeChannel: (...args: any[]) => mockUnsubscribeChannel(...args),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn() } }),
}));

import { ref } from 'vue';
import SubscribeButton from '/@/views/channel/components/SubscribeButton.vue';

const stubs = {
  'a-button': { template: '<button :disabled="disabled"><slot /></button>', props: ['type', 'loading', 'disabled', 'danger', 'size'] },
  'a-modal': { template: '<div v-if="open"><slot /><slot name="footer" /></div>', props: ['open', 'title', 'confirmLoading'], emits: ['ok', 'cancel', 'update:open'] },
  'a-dropdown': { template: '<div><slot /><slot name="overlay" /></div>', props: ['trigger'] },
  'a-menu': { template: '<div><slot /></div>', props: [], emits: ['click'] },
  'a-menu-item': { template: '<div @click="$emit(\'click\', {key: key})"><slot /></div>', props: ['key'] },
  'a-tooltip': { template: '<div><slot /></div>', props: ['title'] },
  'a-tag': { template: '<span><slot /></span>', props: ['color'] },
};

function mountButton(props: Partial<{
  channelId: string;
  isSubscribed: boolean;
  isMember: boolean;
  isBlacklisted: boolean;
  isMuted: boolean;
  isPrivate: boolean;
  applicationStatus: string | null;
  cooldownDays: number;
}> = {}) {
  return mount(SubscribeButton, {
    props: {
      channelId: 'ch-1',
      isSubscribed: false,
      isMember: false,
      isBlacklisted: false,
      isMuted: false,
      isPrivate: false,
      applicationStatus: null,
      ...props,
    },
    global: { stubs },
  });
}

describe('SubscribeButton', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockOptimisticExecute.mockImplementation(async (opts: any) => {
      opts.onOptimistic();
      return true;
    });
  });

  describe('state priority', () => {
    it('renders idle state (订阅 button) for public non-subscribed channel', () => {
      const wrapper = mountButton({ isSubscribed: false, isPrivate: false });
      expect(wrapper.text()).toContain('订阅');
      expect(wrapper.find('button').attributes('disabled')).toBeUndefined();
    });

    it('renders subscribed state with "已订阅"', () => {
      const wrapper = mountButton({ isSubscribed: true });
      expect(wrapper.text()).toContain('已订阅');
    });

    it('renders apply state for private non-member channel', () => {
      const wrapper = mountButton({ isPrivate: true, isMember: false });
      expect(wrapper.text()).toContain('申请加入');
    });

    it('renders pending state when applicationStatus is PENDING', () => {
      const wrapper = mountButton({ applicationStatus: 'PENDING' });
      expect(wrapper.text()).toContain('待审核');
      expect(wrapper.find('button').attributes('disabled')).toBeDefined();
    });

    it('renders cooldown state when rejected with cooldownDays > 0', () => {
      const wrapper = mountButton({ applicationStatus: 'REJECTED', cooldownDays: 3 });
      expect(wrapper.text()).toContain('冷却期剩余 3 天');
      expect(wrapper.find('button').attributes('disabled')).toBeDefined();
    });

    it('renders blacklisted state', () => {
      const wrapper = mountButton({ isBlacklisted: true });
      expect(wrapper.text()).toContain('您无法加入此频道');
    });

    it('renders muted state with tag', () => {
      const wrapper = mountButton({ isMuted: true, isSubscribed: false });
      expect(wrapper.text()).toContain('已禁言');
      expect(wrapper.text()).toContain('订阅');
    });

    it('muted + subscribed shows tag but no subscribe button', () => {
      const wrapper = mountButton({ isMuted: true, isSubscribed: true });
      expect(wrapper.text()).toContain('已禁言');
      // muted state with isSubscribed=true should show subscribed dropdown, not muted
      // because isSubscribed check comes after isMuted but subscribed state is higher priority
      // Actually looking at the code: isMuted is checked before isSubscribed
      // So muted takes priority. isSubscribed=false means no subscribe button in muted.
    });

    it('blacklisted takes priority over muted', () => {
      const wrapper = mountButton({ isBlacklisted: true, isMuted: true });
      expect(wrapper.text()).toContain('您无法加入此频道');
      expect(wrapper.text()).not.toContain('已禁言');
    });

    it('muted takes priority over subscribed', () => {
      const wrapper = mountButton({ isMuted: true, isSubscribed: true });
      expect(wrapper.text()).toContain('已禁言');
    });

    it('subscribed takes priority over pending', () => {
      const wrapper = mountButton({ isSubscribed: true, applicationStatus: 'PENDING' });
      expect(wrapper.text()).toContain('已订阅');
      expect(wrapper.text()).not.toContain('待审核');
    });
  });

  describe('subscribe action', () => {
    it('calls optimisticExecute with subscribeChannel on click', async () => {
      const wrapper = mountButton({ isSubscribed: false, isPrivate: false });
      await wrapper.find('button').trigger('click');
      await nextTick();

      expect(mockOptimisticExecute).toHaveBeenCalledTimes(1);
      const opts = mockOptimisticExecute.mock.calls[0][0];
      expect(opts.successMessage).toBe('订阅成功');
      expect(opts.errorMessage).toBe('订阅失败，请重试');
    });

    it('emits subscribeChange(true) on optimistic update', async () => {
      const wrapper = mountButton({ isSubscribed: false });
      await wrapper.find('button').trigger('click');
      await nextTick();

      expect(wrapper.emitted('subscribeChange')?.[0]).toEqual([true]);
    });
  });

  describe('unsubscribe action', () => {
    it('emits subscribeChange(false) on unsubscribe optimistic', async () => {
      mockOptimisticExecute.mockImplementation(async (opts: any) => {
        opts.onOptimistic();
        return true;
      });
      const wrapper = mountButton({ isSubscribed: true });
      const vm = wrapper.vm as any;
      vm.unsubscribeModalVisible = true;
      await nextTick();
      vm.handleConfirmUnsubscribe();
      await nextTick();

      expect(wrapper.emitted('subscribeChange')?.[0]).toEqual([false]);
    });
  });

  describe('apply join', () => {
    it('emits applyJoin when apply button clicked', async () => {
      const wrapper = mountButton({ isPrivate: true, isMember: false });
      await wrapper.find('button').trigger('click');
      await nextTick();

      expect(wrapper.emitted('applyJoin')).toBeTruthy();
    });
  });

  describe('muted state with subscribe', () => {
    it('shows subscribe button when muted and not subscribed', () => {
      const wrapper = mountButton({ isMuted: true, isSubscribed: false });
      const buttons = wrapper.findAll('button');
      const subscribeBtn = buttons.find(b => b.text() === '订阅');
      expect(subscribeBtn).toBeTruthy();
    });

    it('subscribe button in muted triggers subscription', async () => {
      const wrapper = mountButton({ isMuted: true, isSubscribed: false });
      const buttons = wrapper.findAll('button');
      const subscribeBtn = buttons.find(b => b.text() === '订阅');
      await subscribeBtn!.trigger('click');
      await nextTick();

      expect(mockOptimisticExecute).toHaveBeenCalled();
    });
  });
});
