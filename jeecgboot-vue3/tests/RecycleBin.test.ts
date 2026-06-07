import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RecycleBin from '../src/views/channel/governance/RecycleBin.vue';

const mockRecycleBinList = [
  { id: '1', title: '已删文章', contentType: 'article', originalAuthor: '张三', deletedBy: '管理员', deleteTime: '2026-06-01', deleteReason: '违规', remainingDays: 25 },
  { id: '2', title: '过期内容', contentType: 'post', originalAuthor: '李四', deletedBy: '管理员', deleteTime: '2026-05-01', deleteReason: '重复', remainingDays: 0 },
];
const mockFetchRecycleBin = vi.fn();
const mockRestore = vi.fn().mockResolvedValue({});

vi.mock('/@/store/modules/channelGovernance', () => ({
  useChannelGovernanceStore: vi.fn(() => ({
    recycleBinList: mockRecycleBinList,
    loading: false,
    fetchRecycleBin: mockFetchRecycleBin,
    restore: mockRestore,
  })),
}));

vi.mock('pinia', async (importOriginal) => {
  const actual = await importOriginal<typeof import('pinia')>();
  const { ref } = await import('vue');
  return {
    ...actual,
    storeToRefs: vi.fn((store: any) => {
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

vi.mock('ant-design-vue', async () => {
  const { defineComponent, h } = await import('vue');
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
                h('span', { class: 'cell-author' }, item.originalAuthor),
                slots.bodyCell
                  ? (props.columns || [])
                      .filter((c: any) => c.key === 'remaining' || c.key === 'action')
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
      setup(props: any, { slots, emit }: any) {
        return () =>
          h(
            'button',
            {
              class: 'btn',
              disabled: props.disabled,
              onClick: () => emit('click'),
            },
            slots.default?.(),
          );
      },
    }),
    Space: defineComponent({
      name: 'Space',
      props: { wrap: Boolean },
      setup(_p: any, { slots }: any) {
        return () => h('div', { class: 'space' }, slots.default?.());
      },
    }),
    Modal: Object.assign(
      defineComponent({
        name: 'Modal',
        props: { modelValue: Boolean, visible: Boolean, title: String },
        emits: ['update:modelValue', 'ok', 'cancel'],
        setup(_p: any, { slots }: any) {
          return () => h('div', { class: 'modal' }, slots.default?.());
        },
      }),
      { confirm: vi.fn() },
    ),
    message: { success: vi.fn(), error: vi.fn(), info: vi.fn(), warning: vi.fn() },
  };
});

vi.mock('@ant-design/icons-vue', () => ({
  UndoOutlined: { name: 'UndoOutlined', template: '<span />' },
}));

describe('RecycleBin', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('应加载回收站列表', async () => {
    const wrapper = mount(RecycleBin, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('已删文章');
    expect(wrapper.text()).toContain('张三');
  });

  it('应展示剩余天数', async () => {
    const wrapper = mount(RecycleBin, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('25天');
  });

  it('过期内容应显示已过保留期', async () => {
    const wrapper = mount(RecycleBin, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('已过保留期');
  });

  it('过期内容恢复按钮应禁用', async () => {
    const wrapper = mount(RecycleBin, { props: { channelId: '1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();
    const rows = wrapper.findAll('.table-row');
    const expiredRow = rows[1];
    const expiredButton = expiredRow.find('button.btn');
    expect(expiredButton.attributes('disabled')).toBeDefined();
  });
});
