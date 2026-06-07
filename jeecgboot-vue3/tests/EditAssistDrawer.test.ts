import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import EditAssistDrawer from '../src/views/channel/components/EditAssistDrawer.vue';

const mockEditAssist = vi.fn().mockResolvedValue({});
const mockGetEditAssistHistory = vi.fn().mockResolvedValue([
  { id: '1', operator: '管理员', field: '标题', time: '2026-06-01 10:00', reason: '修正错别字' },
]);

vi.mock('/@/api/content/channel/governance', () => ({
  getEditAssistHistory: (...args: any[]) => mockGetEditAssistHistory(...args),
}));

vi.mock('/@/store/modules/channelGovernance', () => ({
  useChannelGovernanceStore: vi.fn(() => ({
    editAssist: (...args: any[]) => mockEditAssist(...args),
  })),
}));

vi.mock('ant-design-vue', async () => {
  const { defineComponent, h } = await import('vue');
  return {
    Drawer: defineComponent({
      name: 'Drawer',
      props: { visible: Boolean, title: String, width: Number },
      emits: ['close', 'update:visible'],
      setup(_p: any, { slots }: any) {
        return () =>
          _p.visible
            ? h('div', { class: 'drawer' }, [
                h('div', { class: 'drawer-title' }, _p.title),
                h('div', { class: 'drawer-body' }, slots.default?.()),
                h('div', { class: 'drawer-footer' }, slots.footer?.()),
              ])
            : null;
      },
    }),
    Form: Object.assign(
      defineComponent({
        name: 'Form',
        props: { model: Object, layout: String },
        setup(_p: any, { slots }: any) {
          return () => h('form', { class: 'form' }, slots.default?.());
        },
      }),
      {
        Item: defineComponent({
          name: 'FormItem',
          props: { label: String, name: String, required: Boolean },
          setup(_p: any, { slots }: any) {
            return () =>
              h('div', { class: 'form-item' }, [
                _p.label ? h('label', { class: 'form-label' }, _p.label) : null,
                slots.default?.(),
              ]);
          },
        }),
      }
    ),
    Input: Object.assign(
      defineComponent({
        name: 'Input',
        props: { value: String, placeholder: String },
        emits: ['update:value'],
        setup(_p: any, { emit }: any) {
          return () =>
            h('input', {
              class: 'input',
              value: _p.value || '',
              onInput: (e: any) => emit('update:value', e.target.value),
            });
        },
      }),
      {
        TextArea: defineComponent({
          name: 'TextArea',
          props: { value: String, rows: Number, placeholder: String },
          emits: ['update:value'],
          setup(_p: any, { emit }: any) {
            return () =>
              h('textarea', {
                class: 'textarea',
                value: _p.value || '',
                rows: _p.rows,
                placeholder: _p.placeholder,
                onInput: (e: any) => emit('update:value', e.target.value),
              });
          },
        }),
      }
    ),
    Select: defineComponent({
      name: 'Select',
      props: { value: Array, mode: String, placeholder: String },
      emits: ['update:value'],
      setup(_p: any, { emit }: any) {
        return () =>
          h('select', {
            class: 'select',
            onChange: (e: any) => emit('update:value', [e.target.value]),
          });
      },
    }),
    Button: defineComponent({
      name: 'Button',
      props: { type: String, loading: Boolean },
      emits: ['click'],
      setup(_p: any, { slots, emit }: any) {
        return () =>
          h(
            'button',
            {
              class: ['btn', _p.type === 'primary' ? 'btn-primary' : ''].join(' '),
              onClick: () => emit('click'),
            },
            slots.default?.()
          );
      },
    }),
    Space: defineComponent({
      name: 'Space',
      setup(_p: any, { slots }: any) {
        return () => h('div', { class: 'space' }, slots.default?.());
      },
    }),
    Divider: defineComponent({
      name: 'Divider',
      setup() {
        return () => h('hr', { class: 'divider' });
      },
    }),
    Timeline: Object.assign(
      defineComponent({
        name: 'Timeline',
        setup(_p: any, { slots }: any) {
          return () => h('div', { class: 'timeline' }, slots.default?.());
        },
      }),
      {
        Item: defineComponent({
          name: 'TimelineItem',
          setup(_p: any, { slots }: any) {
            return () => h('div', { class: 'timeline-item' }, slots.default?.());
          },
        }),
      }
    ),
    Empty: defineComponent({
      name: 'Empty',
      props: { description: String },
      setup(_p: any) {
        return () => h('div', { class: 'empty' }, _p.description || '暂无数据');
      },
    }),
    message: { success: vi.fn(), error: vi.fn(), info: vi.fn(), warning: vi.fn() },
  };
});

vi.mock('@ant-design/icons-vue', () => ({}));

describe('EditAssistDrawer', () => {
  const defaultProps = {
    visible: true,
    contentId: 'content-1',
    channelId: 'channel-1',
    content: { title: '测试标题', author: '测试作者' },
  };

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('应展示原作者信息', () => {
    const wrapper = mount(EditAssistDrawer, { props: defaultProps });
    expect(wrapper.text()).toContain('原作者：测试作者');
    expect(wrapper.text()).toContain('原标题：测试标题');
  });

  it('应展示修订历史', async () => {
    mockGetEditAssistHistory.mockResolvedValue([
      { id: '1', operator: '管理员', field: '标题', time: '2026-06-01 10:00', reason: '修正错别字' },
    ]);
    const wrapper = mount(EditAssistDrawer, { props: { ...defaultProps, visible: false } });
    await wrapper.setProps({ visible: true });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('管理员 修改了 标题');
    expect(wrapper.text()).toContain('2026-06-01 10:00');
    expect(wrapper.text()).toContain('修正错别字');
  });

  it('未填写修改原因应提示', async () => {
    const wrapper = mount(EditAssistDrawer, { props: defaultProps });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    const buttons = wrapper.findAll('button');
    const saveBtn = buttons.find((b) => b.text() === '保存');
    expect(saveBtn).toBeTruthy();
    await saveBtn!.trigger('click');
    const { message } = await import('ant-design-vue');
    expect(message.warning).toHaveBeenCalledWith('请填写修改原因');
  });

  it('填写完整信息后保存应触发saved事件', async () => {
    const wrapper = mount(EditAssistDrawer, { props: defaultProps });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    const textareas = wrapper.findAll('textarea');
    const reasonTextarea = textareas[textareas.length - 1];
    await reasonTextarea.setValue('修正内容错误');
    const buttons = wrapper.findAll('button');
    const saveBtn = buttons.find((b) => b.text() === '保存');
    await saveBtn!.trigger('click');
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(mockEditAssist).toHaveBeenCalledWith(
      expect.objectContaining({
        contentId: 'content-1',
        channelId: 'channel-1',
        reason: '修正内容错误',
      })
    );
    expect(wrapper.emitted('saved')).toBeTruthy();
  });

  it('点击取消应关闭抽屉', async () => {
    const wrapper = mount(EditAssistDrawer, { props: defaultProps });
    const buttons = wrapper.findAll('button');
    const cancelBtn = buttons.find((b) => b.text() === '取消');
    expect(cancelBtn).toBeTruthy();
    await cancelBtn!.trigger('click');
    expect(wrapper.emitted('update:visible')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });
});
