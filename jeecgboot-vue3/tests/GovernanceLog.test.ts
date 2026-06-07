import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import GovernanceLog from '../src/views/channel/governance/GovernanceLog.vue';

const mockGovernanceLogList = [
  { id: '1', time: '2026-06-01 10:00', operator: '管理员', actionType: 'pin', targetTitle: 'Vue3最佳实践', contentId: 'c1', result: '成功', reason: '优质内容' },
];
const mockFetchGovernanceLog = jest.fn();

jest.mock('/@/store/modules/channelGovernance', () => ({
  useChannelGovernanceStore: jest.fn(() => ({
    governanceLogList: mockGovernanceLogList,
    loading: false,
    fetchGovernanceLog: mockFetchGovernanceLog,
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
      props: { dataSource: Array, columns: Array, loading: Boolean, rowKey: [String, Function] },
      setup(props: any, { slots }: any) {
        return () =>
          h('div', { class: 'table' }, [
            h('div', { class: 'table-header' }, (props.columns || []).map((c: any) => h('span', { key: c.key }, c.title))),
            ...(props.dataSource || []).map((item: any) =>
              h('div', { class: 'table-row', key: item.id }, [
                h('span', { class: 'cell-time' }, item.time),
                h('span', { class: 'cell-operator' }, item.operator),
                h('span', { class: 'cell-action-type' }, item.actionType),
                h('span', { class: 'cell-result' }, item.result),
                h('span', { class: 'cell-remark' }, item.remark),
                slots.bodyCell
                  ? (props.columns || [])
                      .filter((c: any) => c.key === 'target')
                      .map((c: any) => slots.bodyCell({ column: c, record: item }))
                  : null,
              ]),
            ),
          ]);
      },
    }),
    Space: defineComponent({
      name: 'Space',
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
  };
});

jest.mock('@ant-design/icons-vue', () => ({}));

describe('GovernanceLog', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    jest.clearAllMocks();
  });

  it('应加载治理日志列表', async () => {
    const wrapper = mount(GovernanceLog, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('Vue3最佳实践');
    expect(wrapper.text()).toContain('管理员');
  });

  it('应展示筛选栏', async () => {
    const wrapper = mount(GovernanceLog, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.filter-bar').exists()).toBe(true);
    expect(wrapper.find('.select').exists()).toBe(true);
    expect(wrapper.find('.input').exists()).toBe(true);
  });

  it('应展示操作对象链接', async () => {
    const wrapper = mount(GovernanceLog, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    const link = wrapper.find('.table-row a');
    expect(link.exists()).toBe(true);
    expect(link.text()).toBe('Vue3最佳实践');
  });
});
