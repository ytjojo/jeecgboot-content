import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AddContentDialog from '../src/views/channel/components/AddContentDialog.vue';

jest.mock('/@/api/content/channel/addContent', () => ({
  searchAddableContent: jest.fn().mockResolvedValue([
    { id: '1', title: '测试文章', contentType: 'article', author: '张三', publishTime: '2026-06-01', addable: true },
    { id: '2', title: '不可添加', contentType: 'article', author: '李四', publishTime: '2026-06-02', addable: false },
  ]),
  addContentToChannel: jest.fn().mockResolvedValue({}),
}));

jest.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: jest.fn().mockResolvedValue([
    { id: 'ch1', name: '频道A', publishResult: 'direct' },
    { id: 'ch2', name: '频道B', publishResult: 'review' },
  ]),
}));

jest.mock('ant-design-vue', () => {
  const { defineComponent, h } = require('vue');
  return {
    Modal: Object.assign(
      defineComponent({
        name: 'Modal',
        props: { visible: Boolean, title: String, width: Number },
        emits: ['update:visible', 'ok', 'cancel'],
        setup(_p: any, { slots }: any) {
          return () => h('div', { class: 'modal' }, [
            _p.title ? h('div', _p.title) : null,
            slots.default?.(),
            slots.footer?.(),
          ]);
        },
      }),
      { confirm: jest.fn(), info: jest.fn() }
    ),
    Button: defineComponent({
      name: 'Button',
      props: { type: String, disabled: Boolean, loading: Boolean, danger: Boolean, size: String },
      emits: ['click'],
      setup(_p: any, { slots, emit }: any) {
        return () =>
          h(
            'button',
            {
              class: [
                'btn',
                _p.type === 'primary' ? 'btn-primary' : '',
                _p.danger ? 'btn-danger' : '',
                _p.disabled ? 'btn-disabled' : '',
              ].join(' '),
              onClick: () => !_p.disabled && emit('click'),
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
    Form: Object.assign(
      defineComponent({
        name: 'Form',
        props: { layout: String },
        setup(_p: any, { slots }: any) {
          return () => h('div', { class: 'form' }, slots.default?.());
        },
      }),
      {
        Item: defineComponent({
          name: 'FormItem',
          props: { label: String, required: Boolean },
          setup(_p: any, { slots }: any) {
            return () => h('div', { class: 'form-item' }, [_p.label ? h('label', _p.label) : null, slots.default?.()]);
          },
        }),
      }
    ),
    Input: Object.assign(
      defineComponent({
        name: 'Input',
        props: { value: String, placeholder: String },
        emits: ['update:value'],
        setup() {
          return () => h('input');
        },
      }),
      {
        TextArea: defineComponent({
          name: 'TextArea',
          props: { value: String, placeholder: String },
          emits: ['update:value'],
          setup() {
            return () => h('textarea');
          },
        }),
        Search: defineComponent({
          name: 'InputSearch',
          props: { value: String, placeholder: String },
          emits: ['update:value', 'search'],
          setup(_p: any, { emit }: any) {
            return () => h('input', {
              class: 'input-search',
              onKeydown: (e: any) => { if (e.key === 'Enter') emit('search'); },
            });
          },
        }),
      }
    ),
    Select: Object.assign(
      defineComponent({
        name: 'Select',
        props: { value: [Array, String], mode: String, placeholder: String, options: Array },
        emits: ['update:value', 'change'],
        setup() {
          return () => h('select', { class: 'select' });
        },
      }),
      {
        Option: defineComponent({
          name: 'SelectOption',
          props: { value: String },
          setup(_p: any, { slots }: any) {
            return () => h('option', { value: _p.value }, slots.default?.());
          },
        }),
      }
    ),
    Tag: defineComponent({
      name: 'Tag',
      props: { color: String },
      setup(_p: any, { slots }: any) {
        return () => h('span', { class: 'tag' }, slots.default?.());
      },
    }),
    message: { success: jest.fn(), error: jest.fn(), info: jest.fn(), warning: jest.fn() },
  };
});

jest.mock('@ant-design/icons-vue', () => ({
  InfoCircleOutlined: { name: 'InfoCircleOutlined', template: '<span />' },
}));

const mockSearchResults = [
  { id: '1', title: '测试文章', contentType: 'article', author: '张三', publishTime: '2026-06-01', addable: true },
  { id: '2', title: '不可添加', contentType: 'article', author: '李四', publishTime: '2026-06-02', addable: false },
];

describe('AddContentDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    jest.clearAllMocks();
  });

  it('打开弹窗应展示搜索框', () => {
    const wrapper = mount(AddContentDialog, {
      props: { visible: true, entryType: 'system' },
    });
    expect(wrapper.find('.input-search').exists()).toBe(true);
  });

  it('选择不可添加内容应无效', async () => {
    const wrapper = mount(AddContentDialog, {
      props: { visible: true, entryType: 'system' },
    });

    // 直接设置搜索结果
    const vm = wrapper.vm as any;
    vm.searchResults = [...mockSearchResults];
    await wrapper.vm.$nextTick();

    // 点击不可添加项
    const items = wrapper.findAll('.result-item');
    expect(items.length).toBe(2);

    const disabledItem = items[1]; // 第二个是不可添加的
    await disabledItem.trigger('click');
    await wrapper.vm.$nextTick();

    // 验证未选中 — disabled 项不触发 handleSelectContent
    expect(wrapper.text()).not.toContain('已选内容');
  });

  it('选择可添加内容应展示预览', async () => {
    const wrapper = mount(AddContentDialog, {
      props: { visible: true, entryType: 'system' },
    });

    // 直接设置搜索结果
    const vm = wrapper.vm as any;
    vm.searchResults = [...mockSearchResults];
    await wrapper.vm.$nextTick();

    // 点击可添加项
    const items = wrapper.findAll('.result-item');
    expect(items.length).toBe(2);

    const addableItem = items[0]; // 第一个是可添加的
    await addableItem.trigger('click');
    await wrapper.vm.$nextTick();

    // 验证显示预览
    expect(wrapper.text()).toContain('已选内容');
    expect(wrapper.text()).toContain('测试文章');
  });

  it('system 入口应展示添加原因字段', () => {
    const wrapper = mount(AddContentDialog, {
      props: { visible: true, entryType: 'system' },
    });
    expect(wrapper.text()).toContain('添加原因（必填）');
    expect(wrapper.find('textarea').exists()).toBe(true);
  });

  it('非 system 入口不应展示添加原因字段', () => {
    const wrapper = mount(AddContentDialog, {
      props: { visible: true, entryType: 'author' },
    });
    expect(wrapper.text()).not.toContain('添加原因（必填）');
    expect(wrapper.find('textarea').exists()).toBe(false);
  });

  it('点击取消应关闭弹窗', async () => {
    const wrapper = mount(AddContentDialog, {
      props: { visible: true, entryType: 'system' },
    });
    const buttons = wrapper.findAll('button');
    const cancelBtn = buttons.find((b) => b.text() === '取消');
    expect(cancelBtn).toBeTruthy();
    await cancelBtn!.trigger('click');
    expect(wrapper.emitted('update:visible')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });
});
