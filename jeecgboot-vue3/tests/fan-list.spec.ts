import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { nextTick } from 'vue';

vi.mock('/@/api/content/fan-analytics', () => ({
  listFans: vi.fn().mockResolvedValue({ records: [{ id: 'f1', nickname: 'FanA', avatar: '', followedAt: '2025-01-01' }], total: 1 }),
  exportFansCsv: vi.fn().mockResolvedValue(new Blob(['csv'])),
  getFanTrend: vi.fn().mockResolvedValue([{ newFollowerCount: 3 }]),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({ getUserInfo: { userId: 'u1' } }),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn() } }),
}));

describe('FanList.vue', () => {
  async function mountPage() {
    const Component = (await import('/@/views/content/fan/FanList.vue')).default;
    return mount(Component, {
      global: {
        stubs: {
          'a-spin': { template: '<div><slot /></div>', props: ['spinning'] },
          'a-row': { template: '<div><slot /></div>', props: ['gutter'] },
          'a-col': { template: '<div><slot /></div>', props: ['span'] },
          'a-card': { template: '<div><slot /></div>', props: ['size'] },
          'a-statistic': { template: '<div>{{ title }}</div>', props: ['title', 'value'] },
          'a-list': {
            template: '<div><slot name="renderItem" v-for="item in dataSource" :item="item" /><slot /></div>',
            props: ['dataSource', 'pagination'],
          },
          'a-list-item': { template: '<div><slot /></div>' },
          'a-list-item-meta': { template: '<div><slot name="avatar" /><slot name="title" /><slot /></div>' },
          'a-avatar': { template: '<span />' },
          'a-input-search': {
            template: '<input class="search-input" :value="value" @input="$emit(\'update:value\', $event.target.value)" />',
            props: ['value', 'placeholder', 'style'],
            emits: ['update:value', 'search'],
          },
          'a-empty': { template: '<div class="empty-stub" />', props: ['description'] },
          'a-button': { template: '<button><slot /></button>', props: ['type'] },
          'a-space': { template: '<div><slot /></div>' },
        },
      },
    });
  }

  it('renders fan list with items', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('FanA');
  });

  it('renders stats overview after fetch', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    // Stats section should be visible after statsLoaded
    expect(wrapper.html()).toContain('总粉丝数');
    expect(wrapper.html()).toContain('今日新增');
  });

  it('shows search input', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.search-input').exists()).toBe(true);
  });
});
