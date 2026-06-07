import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import AddContentDialog from '../components/AddContentDialog.vue';

vi.mock('/@/api/content/channel/addContent', () => ({
  searchAddableContent: vi.fn().mockResolvedValue([
    { id: '1', title: '可添加文章', contentType: 'article', author: '张三', publishTime: '2026-06-01', addable: true },
  ]),
  addContentToChannel: vi.fn().mockResolvedValue({}),
}));

vi.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: vi.fn().mockResolvedValue({
    list: [
      { id: 'ch1', name: '频道A', type: 'system', userRole: 'admin', publishResult: 'direct', publishable: true },
    ],
    maxChannelCount: 5,
  }),
}));

describe('AddContentDialog', () => {
  let container: HTMLDivElement;

  beforeEach(() => {
    setActivePinia(createPinia());
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    document.body.removeChild(container);
  });

  it('应展示搜索框', async () => {
    mount(AddContentDialog, { props: { visible: true }, attachTo: container });
    await nextTick();
    await nextTick();
    const input = document.body.querySelector('input');
    expect(input).toBeTruthy();
  });

  it('系统入口应展示添加原因输入框', async () => {
    mount(AddContentDialog, { props: { visible: true, entryType: 'system' }, attachTo: container });
    await nextTick();
    await nextTick();
    const bodyText = document.body.textContent || '';
    expect(bodyText).toContain('添加原因');
  });
});
