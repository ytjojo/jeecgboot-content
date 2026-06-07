import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import RejectReasonModal from '../components/RejectReasonModal.vue';

// Suppress ant-design-vue TextArea unmount error in jsdom
const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

describe('RejectReasonModal', () => {
  let container: HTMLDivElement;

  beforeEach(() => {
    setActivePinia(createPinia());
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    document.body.removeChild(container);
  });

  afterAll(() => {
    consoleSpy.mockRestore();
  });

  it('应展示预设原因标签', async () => {
    mount(RejectReasonModal, { props: { visible: true }, attachTo: container });
    await nextTick();
    await nextTick();
    const bodyText = document.body.textContent || '';
    expect(bodyText).toContain('违反社区规范');
    expect(bodyText).toContain('内容重复');
  });

  it('点击预设原因应自动填充', async () => {
    mount(RejectReasonModal, { props: { visible: true }, attachTo: container });
    await nextTick();
    await nextTick();
    const presetTag = document.body.querySelector('.preset-tag') as HTMLElement;
    expect(presetTag).toBeTruthy();
    presetTag.click();
    await nextTick();
    const textarea = document.body.querySelector('textarea') as HTMLTextAreaElement;
    expect(textarea).toBeTruthy();
    expect(textarea.value).toContain('违反社区规范');
  });
});
