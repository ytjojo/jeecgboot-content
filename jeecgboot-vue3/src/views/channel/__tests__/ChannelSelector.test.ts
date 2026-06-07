import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import ChannelSelector from '../publish/ChannelSelector.vue';

vi.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: vi.fn().mockResolvedValue({
    list: [
      { id: '1', name: '频道A', type: 'system', userRole: 'admin', publishResult: 'direct', publishable: true },
      { id: '2', name: '频道B', type: 'personal', userRole: 'member', publishResult: 'review', publishable: true },
      { id: '3', name: '频道C', type: 'org', userRole: 'member', publishResult: 'blocked', publishable: false, reason: '仅管理员可发布' },
    ],
    maxChannelCount: 5,
  }),
}));

describe('ChannelSelector', () => {
  let container: HTMLDivElement;

  beforeEach(() => {
    setActivePinia(createPinia());
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    document.body.removeChild(container);
  });

  it('应加载频道列表并按分组展示', async () => {
    const wrapper = mount(ChannelSelector, { props: { modelValue: true }, attachTo: container });
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();
    const bodyText = document.body.textContent || '';
    expect(bodyText).toContain('频道A');
    expect(bodyText).toContain('频道B');
    wrapper.unmount();
  });

  it('不可发布频道应置灰展示', async () => {
    const wrapper = mount(ChannelSelector, { props: { modelValue: true }, attachTo: container });
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();
    const blockedChannel = document.body.querySelector('[data-channel-id="3"]');
    expect(blockedChannel).toBeTruthy();
    expect(blockedChannel!.classList.contains('disabled')).toBe(true);
    wrapper.unmount();
  });

  it('达到上限时应阻止继续选择', async () => {
    const wrapper = mount(ChannelSelector, { props: { modelValue: true, maxChannelCount: 1 }, attachTo: container });
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();
    const firstChannel = document.body.querySelector('[data-channel-id="1"]') as HTMLElement;
    expect(firstChannel).toBeTruthy();
    firstChannel.click();
    await nextTick();
    const secondChannel = document.body.querySelector('[data-channel-id="2"]') as HTMLElement;
    expect(secondChannel).toBeTruthy();
    expect(secondChannel.classList.contains('disabled')).toBe(true);
    wrapper.unmount();
  });

  it('已选频道应支持移除', async () => {
    const wrapper = mount(ChannelSelector, { props: { modelValue: true }, attachTo: container });
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();
    const firstChannel = document.body.querySelector('[data-channel-id="1"]') as HTMLElement;
    expect(firstChannel).toBeTruthy();
    firstChannel.click();
    await nextTick();
    const tags = document.body.querySelectorAll('.ant-tag');
    expect(tags.length).toBeGreaterThanOrEqual(1);
    const closeIcon = document.body.querySelector('.ant-tag .ant-tag-close-icon') as HTMLElement;
    if (closeIcon) {
      closeIcon.click();
      await nextTick();
      const tagsAfter = document.body.querySelectorAll('.ant-tag');
      expect(tagsAfter.length).toBeLessThan(tags.length);
    }
    wrapper.unmount();
  });
});
