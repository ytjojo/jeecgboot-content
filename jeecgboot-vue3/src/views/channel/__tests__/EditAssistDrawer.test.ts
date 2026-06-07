import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import EditAssistDrawer from '../components/EditAssistDrawer.vue';

vi.mock('/@/api/content/channel/governance', () => ({
  getEditAssistHistory: vi.fn().mockResolvedValue([
    { id: '1', operator: '管理员', field: '标题', time: '2026-06-01', reason: '修正错别字' },
  ]),
}));

describe('EditAssistDrawer', () => {
  let container: HTMLDivElement;

  beforeEach(() => {
    setActivePinia(createPinia());
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    document.body.removeChild(container);
  });

  it('应展示原作者信息', async () => {
    mount(EditAssistDrawer, {
      props: { visible: true, contentId: '1', channelId: 'ch1', content: { title: '测试标题', author: '张三' } },
      attachTo: container,
    });
    await nextTick();
    await nextTick();
    const bodyText = document.body.textContent || '';
    expect(bodyText).toContain('张三');
    expect(bodyText).toContain('测试标题');
  });

  it('应展示修订历史区域', async () => {
    mount(EditAssistDrawer, {
      props: { visible: true, contentId: '1', channelId: 'ch1', content: { title: '测试标题', author: '张三' } },
      attachTo: container,
    });
    await nextTick();
    await nextTick();
    const bodyText = document.body.textContent || '';
    expect(bodyText).toContain('修订历史');
  });
});
