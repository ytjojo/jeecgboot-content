import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RejectReasonModal from '../src/views/channel/components/RejectReasonModal.vue';

vi.mock('ant-design-vue', async () => {
  const { defineComponent, h } = await import('vue');
  return {
    Modal: defineComponent({
      name: 'Modal',
      props: { modelValue: Boolean, visible: Boolean, title: String, width: Number },
      emits: ['update:modelValue', 'cancel', 'ok'],
      setup(_p: any, { slots }: any) {
        return () => h('div', { class: 'modal' }, [
          _p.title ? h('div', { class: 'modal-title' }, _p.title) : null,
          slots.default?.(),
          slots.footer?.(),
        ]);
      },
    }),
    Input: Object.assign(
      defineComponent({ name: 'Input', setup() { return () => h('input'); } }),
      {
        TextArea: defineComponent({
          name: 'TextArea',
          props: { modelValue: String, value: String, rows: Number, placeholder: String, status: String },
          emits: ['update:modelValue', 'update:value'],
          setup(_p: any, { emit }: any) {
            return () => h('textarea', {
              value: _p.value || _p.modelValue || '',
              placeholder: _p.placeholder,
              onInput: (e: any) => emit('update:value', e.target.value),
            });
          },
        }),
      }
    ),
    Tag: defineComponent({
      name: 'Tag',
      props: { color: String, closable: Boolean },
      emits: ['close', 'click'],
      setup(_p: any, { slots, emit }: any) {
        return () => h('span', { class: 'tag', onClick: () => emit('click') }, slots.default?.());
      },
    }),
    Space: defineComponent({
      name: 'Space',
      props: { wrap: Boolean, direction: String },
      setup(_p: any, { slots }: any) { return () => h('div', { class: 'space' }, slots.default?.()); },
    }),
    Button: defineComponent({
      name: 'Button',
      props: { type: String, disabled: Boolean, loading: Boolean, danger: Boolean, size: String },
      emits: ['click'],
      setup(_p: any, { slots, emit }: any) {
        return () => h('button', { class: ['btn', _p.type === 'primary' ? 'btn-primary' : '', _p.danger ? 'btn-danger' : ''].join(' '), onClick: () => emit('click') }, slots.default?.());
      },
    }),
    message: { success: vi.fn(), error: vi.fn(), info: vi.fn(), warning: vi.fn() },
  };
});

vi.mock('@ant-design/icons-vue', () => ({
  CheckCircleOutlined: { name: 'CheckCircleOutlined', template: '<span />' },
  ClockCircleOutlined: { name: 'ClockCircleOutlined', template: '<span />' },
  CloseCircleOutlined: { name: 'CloseCircleOutlined', template: '<span />' },
  InfoCircleOutlined: { name: 'InfoCircleOutlined', template: '<span />' },
}));

describe('RejectReasonModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('应展示预设原因标签', () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    expect(wrapper.text()).toContain('违反社区规范');
    expect(wrapper.text()).toContain('内容重复');
  });

  it('拒绝原因不足10字应校验失败', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    const textarea = wrapper.find('textarea');
    await textarea.setValue('太短');
    await wrapper.find('.confirm-btn').trigger('click');
    expect(wrapper.text()).toContain('拒绝原因至少需要10个字');
  });

  it('点击预设原因应自动填充', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    await wrapper.find('.preset-tag').trigger('click');
    const textarea = wrapper.find('textarea');
    expect((textarea.element as HTMLTextAreaElement).value).toContain('违反社区规范');
  });

  it('点击预设原因后确认应跳过长度校验并触发confirm事件', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    await wrapper.find('.preset-tag').trigger('click');
    await wrapper.find('.confirm-btn').trigger('click');
    expect(wrapper.emitted('confirm')).toBeTruthy();
    expect(wrapper.emitted('confirm')![0]).toEqual(['违反社区规范']);
  });

  it('输入足够字数后确认应触发confirm事件', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    const textarea = wrapper.find('textarea');
    await textarea.setValue('这条内容严重违反了社区规范，需要拒绝处理');
    await wrapper.find('.confirm-btn').trigger('click');
    expect(wrapper.emitted('confirm')).toBeTruthy();
    expect(wrapper.emitted('confirm')![0]).toEqual(['这条内容严重违反了社区规范，需要拒绝处理']);
  });

  it('点击取消应关闭弹窗', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    const buttons = wrapper.findAll('button');
    const cancelBtn = buttons.find(b => b.text() === '取消');
    expect(cancelBtn).toBeTruthy();
    await cancelBtn!.trigger('click');
    expect(wrapper.emitted('update:visible')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });
});
