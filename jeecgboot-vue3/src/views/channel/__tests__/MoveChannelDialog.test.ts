import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import MoveChannelDialog from '../components/MoveChannelDialog.vue';

vi.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: vi.fn().mockResolvedValue([
    { id: 'ch1', name: '频道A', publishResult: 'direct' },
    { id: 'ch2', name: '频道B', publishResult: 'review' },
  ]),
}));

describe('MoveChannelDialog', () => {
  let container: HTMLDivElement;

  beforeEach(() => {
    setActivePinia(createPinia());
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    document.body.removeChild(container);
  });

  it('应展示目标频道选择器', async () => {
    mount(MoveChannelDialog, { props: { visible: true, contentId: '1', sourceChannelId: 'ch1' }, attachTo: container });
    await nextTick();
    await nextTick();
    const bodyText = document.body.textContent || '';
    expect(bodyText).toContain('选择目标频道');
  });

  it('未选择目标时确认按钮应禁用', async () => {
    mount(MoveChannelDialog, { props: { visible: true, contentId: '1', sourceChannelId: 'ch1' }, attachTo: container });
    await nextTick();
    await nextTick();
    const confirmBtn = document.body.querySelector('.ant-btn-primary') as HTMLButtonElement;
    expect(confirmBtn).toBeTruthy();
    expect(confirmBtn.disabled || confirmBtn.hasAttribute('disabled')).toBe(true);
  });
});
