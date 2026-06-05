import { mount, flushPromises } from '@vue/test-utils';
import { nextTick } from 'vue';

// Mock API
jest.mock('/@/api/content/relation', () => ({
  getMutualFollowList: jest.fn().mockResolvedValue({
    records: [
      { id: '1', nickname: 'Alice', avatar: '' },
      { id: '2', nickname: 'Bob', avatar: '' },
    ],
    total: 2,
  }),
  unfollowUser: jest.fn().mockResolvedValue(undefined),
}));

// Mock user store
jest.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({
    getUserInfo: { userId: 'u1' },
  }),
}));

// Mock message hook
jest.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({
    createMessage: { success: jest.fn(), error: jest.fn() },
    createConfirm: jest.fn().mockResolvedValue(true),
  }),
}));

describe('MutualFollowList.vue', () => {
  async function mountPage() {
    const Component = (await import('/@/views/content/mutual-follow/MutualFollowList.vue')).default;
    return mount(Component, {
      global: {
        stubs: {
          'a-spin': { template: '<div><slot /></div>', props: ['spinning'] },
          'a-list': {
            template: '<div><slot name="renderItem" v-for="item in dataSource" :item="item" /><slot /></div>',
            props: ['dataSource', 'pagination'],
          },
          'a-list-item': { template: '<div><slot /></div>' },
          'a-list-item-meta': { template: '<div><slot name="avatar" /><slot name="title" /><slot /></div>' },
          'a-avatar': { template: '<span />' },
          'a-input-search': {
            template: '<input class="search-input" :value="value" @input="$emit(\'update:value\', $event.target.value)" />',
            props: ['value', 'placeholder'],
            emits: ['update:value', 'search'],
          },
          'a-empty': { template: '<div class="empty-stub" />', props: ['description'] },
          'a-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          'a-popconfirm': {
            template: '<div><slot /><slot name="cancelButtonProps" /><slot name="okButtonProps" /></div>',
            props: ['title', 'okText', 'cancelText'],
          },
        },
      },
    });
  }

  it('renders mutual follow list with items', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('Alice');
    expect(wrapper.html()).toContain('Bob');
  });

  it('renders empty state when no items', async () => {
    const { getMutualFollowList } = await import('/@/api/content/relation');
    (getMutualFollowList as jest.Mock).mockResolvedValueOnce({ records: [], total: 0 });
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.find('.empty-stub').exists()).toBe(true);
  });

  it('shows search input', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.search-input').exists()).toBe(true);
  });
});
