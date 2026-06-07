import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ContentManage from '../src/views/channel/governance/ContentManage.vue';

const mockContentList = [
  { id: '1', title: 'Vue3最佳实践', contentType: 'article', author: '王五', publishTime: '2026-06-01 10:00', status: 'published', isPinned: true, isFeatured: false },
];
const mockFetchList = jest.fn();
const mockSetFilter = jest.fn();
const mockPin = jest.fn().mockResolvedValue({});
const mockFeature = jest.fn().mockResolvedValue({});
const mockDeleteContent = jest.fn().mockResolvedValue({});
const mockMoveContent = jest.fn().mockResolvedValue({});
const mockEditAssist = jest.fn().mockResolvedValue({});

jest.mock('/@/store/modules/channelGovernance', () => ({
  useChannelGovernanceStore: jest.fn(() => ({
    contentList: mockContentList,
    loading: false,
    fetchList: mockFetchList,
    setFilter: mockSetFilter,
    pin: mockPin,
    feature: mockFeature,
    deleteContent: mockDeleteContent,
    moveContent: mockMoveContent,
    editAssist: mockEditAssist,
  })),
}));

jest.mock('pinia', () => {
  const actual = jest.requireActual('pinia');
  const { ref } = require('vue');
  return {
    ...actual,
    storeToRefs: jest.fn((store: any) => {
      const result: any = {};
      Object.keys(store).forEach((key) => {
        if (typeof store[key] === 'function') {
          result[key] = store[key];
        } else {
          result[key] = ref(store[key]);
        }
      });
      return result;
    }),
  };
});

jest.mock('ant-design-vue', () => {
  const { defineComponent, h } = require('vue');
  return {
    Table: defineComponent({
      name: 'Table',
      props: { dataSource: Array, columns: Array, loading: Boolean, rowKey: [String, Function], rowSelection: Object },
      setup(props: any, { slots }: any) {
        return () =>
          h('div', { class: 'table' }, [
            h('div', { class: 'table-header' }, (props.columns || []).map((c: any) => h('span', { key: c.key }, c.title))),
            ...(props.dataSource || []).map((item: any) =>
              h('div', { class: 'table-row', key: item.id }, [
                h('span', { class: 'cell-title' }, item.title),
                h('span', { class: 'cell-author' }, item.author),
                slots.bodyCell
                  ? (props.columns || [])
                      .filter((c: any) => c.key === 'status' || c.key === 'action')
                      .map((c: any) => slots.bodyCell({ column: c, record: item }))
                  : null,
              ]),
            ),
          ]);
      },
    }),
    Button: defineComponent({
      name: 'Button',
      props: { type: String, disabled: Boolean, loading: Boolean, danger: Boolean, size: String },
      emits: ['click'],
      setup(_p: any, { slots, emit }: any) {
        return () => h('button', { class: 'btn', onClick: () => emit('click') }, slots.default?.());
      },
    }),
    Space: defineComponent({
      name: 'Space',
      props: { wrap: Boolean },
      setup(_p: any, { slots }: any) { return () => h('div', { class: 'space' }, slots.default?.()); },
    }),
    Select: Object.assign(
      defineComponent({
        name: 'Select',
        props: { modelValue: [String, undefined], value: [String, undefined], placeholder: String, allowClear: Boolean },
        emits: ['update:modelValue', 'update:value'],
        setup() { return () => h('select', { class: 'select' }); },
      }),
      {
        Option: defineComponent({
          name: 'SelectOption',
          props: { value: String },
          setup(_p: any, { slots }: any) { return () => h('option', { value: _p.value }, slots.default?.()); },
        }),
      }
    ),
    Input: defineComponent({
      name: 'Input',
      props: { modelValue: String, value: String, placeholder: String, allowClear: Boolean },
      emits: ['update:modelValue', 'update:value'],
      setup() { return () => h('input', { class: 'input' }); },
    }),
    Tag: defineComponent({
      name: 'Tag',
      props: { color: String },
      setup(_p: any, { slots }: any) { return () => h('span', { class: 'tag' }, slots.default?.()); },
    }),
    Modal: Object.assign(
      defineComponent({
        name: 'Modal',
        props: { modelValue: Boolean, visible: Boolean, title: String },
        emits: ['update:modelValue', 'ok', 'cancel'],
        setup(_p: any, { slots }: any) { return () => h('div', { class: 'modal' }, slots.default?.()); },
      }),
      { confirm: jest.fn() }
    ),
    message: { success: jest.fn(), error: jest.fn(), info: jest.fn(), warning: jest.fn() },
  };
});

jest.mock('@ant-design/icons-vue', () => ({
  CheckCircleOutlined: { name: 'CheckCircleOutlined', template: '<span />' },
  ClockCircleOutlined: { name: 'ClockCircleOutlined', template: '<span />' },
  CloseCircleOutlined: { name: 'CloseCircleOutlined', template: '<span />' },
}));

jest.mock('../src/views/channel/components/GovernanceActionMenu.vue', () => ({
  name: 'GovernanceActionMenu',
  template: '<div class="governance-action-menu-stub"></div>',
  props: ['isPinned', 'isFeatured'],
}));
jest.mock('../src/views/channel/components/MoveChannelDialog.vue', () => ({
  name: 'MoveChannelDialog',
  template: '<div class="move-channel-dialog-stub"></div>',
  props: ['visible', 'contentId', 'sourceChannelId'],
}));
jest.mock('../src/views/channel/components/EditAssistDrawer.vue', () => ({
  name: 'EditAssistDrawer',
  template: '<div class="edit-assist-drawer-stub"></div>',
  props: ['visible', 'contentId', 'channelId', 'content'],
}));

describe('ContentManage', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    jest.clearAllMocks();
  });

  it('应加载内容列表', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('Vue3最佳实践');
    expect(wrapper.text()).toContain('王五');
  });

  it('应展示置顶标识', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('置顶');
  });

  it('应展示筛选栏', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.filter-bar').exists()).toBe(true);
    expect(wrapper.find('.select').exists()).toBe(true);
    expect(wrapper.find('.input').exists()).toBe(true);
  });

  it('应展示操作按钮', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('回收站');
    expect(wrapper.text()).toContain('日志');
  });
});
