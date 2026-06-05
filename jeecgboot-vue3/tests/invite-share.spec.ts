import { mount, flushPromises } from '@vue/test-utils';
import { nextTick } from 'vue';

jest.mock('/@/api/content/invite', () => ({
  generateInviteCode: jest.fn().mockResolvedValue({ inviteCode: 'INV123' }),
  listInviteRecords: jest.fn().mockResolvedValue({
    records: [{ id: 'r1', inviteeNickname: '新用户', registeredAt: '2025-06-01', rewardStatus: 'PAID', inviteeAvatar: '' }],
    total: 1,
  }),
  getInviteStats: jest.fn().mockResolvedValue({
    inviteCode: 'INV123',
    totalInvited: 5,
    totalReward: 100,
    pendingReward: 20,
  }),
}));

jest.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({ getUserInfo: { userId: 'u1' } }),
}));

jest.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: jest.fn(), error: jest.fn() } }),
}));

describe('InviteShare.vue', () => {
  async function mountPage() {
    const Component = (await import('/@/views/content/invite/InviteShare.vue')).default;
    return mount(Component, {
      global: {
        stubs: {
          'a-spin': { template: '<div><slot /></div>', props: ['spinning'] },
          'a-card': { template: '<div><slot name="title" /><slot /></div>', props: ['title', 'size'] },
          'a-row': { template: '<div><slot /></div>', props: ['gutter'] },
          'a-col': { template: '<div><slot /></div>', props: ['span'] },
          'a-statistic': { template: '<div>{{ title }}</div>', props: ['title', 'value'] },
          'a-space': { template: '<div><slot /></div>' },
          'a-button': { template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>', props: ['type', 'size', 'disabled'] },
          'a-list': {
            template: '<div class="list-stub"><template v-for="(item, index) in dataSource" :key="item.id || index"><slot name="renderItem" :item="item" /></template><slot /></div>',
            props: ['dataSource', 'pagination'],
            emits: ['change'],
          },
          'a-list-item': { template: '<div><slot /><slot name="actions" /></div>' },
          'a-list-item-meta': { template: '<div><slot name="avatar" /><slot name="title" /><slot name="description" /></div>' },
          'a-avatar': { template: '<span />' },
          'a-tag': { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
          'a-empty': { template: '<div class="empty-stub" />', props: ['description'] },
        },
      },
    });
  }

  it('renders invite code after load', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('INV123');
  });

  it('renders stats cards', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('邀请人数');
    expect(wrapper.html()).toContain('获得积分');
  });

  it('renders invite records', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('新用户');
    expect(wrapper.html()).toContain('已发放');
  });
});
