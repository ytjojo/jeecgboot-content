import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';

const mockGetSubscriptionList = vi.fn();
const mockGetSubscriptionGroupList = vi.fn();
const mockCreateSubscriptionGroup = vi.fn();
const mockUpdateSubscriptionReminder = vi.fn();
const mockUnsubscribeChannel = vi.fn();
vi.mock('/@/api/content/channelSubscription', () => ({
  getSubscriptionList: (...args: any[]) => mockGetSubscriptionList(...args),
  getSubscriptionGroupList: (...args: any[]) => mockGetSubscriptionGroupList(...args),
  createSubscriptionGroup: (...args: any[]) => mockCreateSubscriptionGroup(...args),
  updateSubscriptionReminder: (...args: any[]) => mockUpdateSubscriptionReminder(...args),
  unsubscribeChannel: (...args: any[]) => mockUnsubscribeChannel(...args),
}));

const mockSuccess = vi.fn();
const mockError = vi.fn();
vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: mockSuccess, error: mockError } }),
}));

import SubscriptionList from '/@/views/channel/subscription/SubscriptionList.vue';

const SubscriptionCardStub = {
  template: '<div class="subscription-card-stub">{{ channel.name }}</div>',
  props: ['channel'],
  emits: ['toggleReminder', 'unsubscribe'],
};

const stubs = {
  'a-tabs': { template: '<div><slot /></div>', props: ['activeKey'] },
  'a-tab-pane': { template: '<div><slot /></div>', props: ['key', 'tab'] },
  'a-button': { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'loading', 'disabled', 'danger', 'size'] },
  'a-input': { template: '<input />', props: ['value', 'placeholder', 'maxlength', 'minlength'] },
  'a-input-search': { template: '<input />', props: ['value', 'placeholder'] },
  'a-empty': { template: '<div>{{ description }}<slot /></div>', props: ['description'] },
  'a-skeleton': { template: '<div><slot /></div>', props: ['loading', 'active'] },
  'a-modal': {
    template: '<div v-if="open"><slot /><slot name="footer" /></div>',
    props: ['open', 'title', 'confirmLoading'],
    emits: ['ok', 'cancel', 'update:open'],
  },
  SubscriptionCard: SubscriptionCardStub,
};

const sampleChannels = [
  { id: 'ch-1', name: '技术频道', groupId: 'default', avatar: '', latestSummary: '', isSystem: false, reminderEnabled: true },
  { id: 'ch-2', name: '设计频道', groupId: 'g-1', avatar: '', latestSummary: '', isSystem: false, reminderEnabled: false },
  { id: 'ch-3', name: '产品频道', groupId: 'default', avatar: '', latestSummary: '', isSystem: true, reminderEnabled: true },
];

const sampleGroups = [
  { id: 'g-1', name: '自定义分组' },
];

function mountList() {
  return mount(SubscriptionList, { global: { stubs } });
}

describe('SubscriptionList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetSubscriptionList.mockResolvedValue([...sampleChannels]);
    mockGetSubscriptionGroupList.mockResolvedValue([...sampleGroups]);
    mockCreateSubscriptionGroup.mockResolvedValue(undefined);
    mockUpdateSubscriptionReminder.mockResolvedValue(undefined);
    mockUnsubscribeChannel.mockResolvedValue(undefined);
  });

  describe('data loading on mount', () => {
    it('calls getSubscriptionList and getSubscriptionGroupList on mount', async () => {
      mountList();
      await nextTick();
      await nextTick();

      expect(mockGetSubscriptionList).toHaveBeenCalled();
      expect(mockGetSubscriptionGroupList).toHaveBeenCalled();
    });
  });

  describe('tab switching', () => {
    it('filters channels by groupId when tab is not "all"', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.activeGroup = 'default';
      await nextTick();

      const filtered = vm.filteredChannels;
      expect(filtered.every((c: any) => c.groupId === 'default')).toBe(true);
    });

    it('shows all channels when tab is "all"', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.activeGroup = 'all';
      await nextTick();

      expect(vm.filteredChannels.length).toBe(3);
    });
  });

  describe('search filtering', () => {
    it('filters channels by name case-insensitively', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.searchKeyword = '技术';
      await nextTick();

      expect(vm.filteredChannels.length).toBe(1);
      expect(vm.filteredChannels[0].name).toBe('技术频道');
    });

    it('shows no results for non-matching search', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.searchKeyword = '不存在的频道';
      await nextTick();

      expect(vm.filteredChannels.length).toBe(0);
    });
  });

  describe('empty state', () => {
    it('shows empty state when no channels', async () => {
      mockGetSubscriptionList.mockResolvedValue([]);
      const wrapper = mountList();
      await nextTick();
      await nextTick();
      await nextTick();

      expect(wrapper.text()).toContain('暂无订阅频道');
      expect(wrapper.text()).toContain('去发现频道');
    });
  });

  describe('create group', () => {
    it('calls createSubscriptionGroup API and refreshes list', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.showCreateGroupModal = true;
      vm.newGroupName = '新分组';
      await nextTick();
      await vm.handleCreateGroup();

      expect(mockCreateSubscriptionGroup).toHaveBeenCalledWith({ name: '新分组' });
      expect(mockSuccess).toHaveBeenCalledWith('分组已创建');
      expect(vm.showCreateGroupModal).toBe(false);
    });

    it('does not call API when name is empty', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.newGroupName = '   ';
      await vm.handleCreateGroup();

      expect(mockCreateSubscriptionGroup).not.toHaveBeenCalled();
    });
  });

  describe('unsubscribe', () => {
    it('opens confirmation modal on unsubscribe', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.handleUnsubscribe('ch-1');
      await nextTick();

      expect(vm.unsubscribeModalVisible).toBe(true);
      expect(vm.unsubscribeTarget).toBe('ch-1');
    });

    it('calls unsubscribeChannel API on confirm', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      vm.handleUnsubscribe('ch-1');
      await nextTick();
      await vm.handleConfirmUnsubscribe();

      expect(mockUnsubscribeChannel).toHaveBeenCalledWith('ch-1');
      expect(mockSuccess).toHaveBeenCalledWith('已取消订阅');
      expect(vm.unsubscribeModalVisible).toBe(false);
    });
  });

  describe('toggle reminder', () => {
    it('calls updateSubscriptionReminder API', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      await vm.handleToggleReminder('ch-1', true);

      expect(mockUpdateSubscriptionReminder).toHaveBeenCalledWith({ channelId: 'ch-1', enabled: true });
      expect(mockSuccess).toHaveBeenCalledWith('已开启提醒');
    });

    it('shows "已关闭提醒" when disabling', async () => {
      const wrapper = mountList();
      await nextTick();
      await nextTick();

      const vm = wrapper.vm as any;
      await vm.handleToggleReminder('ch-1', false);

      expect(mockSuccess).toHaveBeenCalledWith('已关闭提醒');
    });
  });
});
