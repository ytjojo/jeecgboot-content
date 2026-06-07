import { mount } from '@vue/test-utils';
import AnnouncementManage from '../src/views/channel/governance/AnnouncementManage.vue';

const mockAnnouncement = { id: 'a1', title: '频道公告', content: '<p>公告内容</p>', status: 'published', version: 2 };
const mockHistory = [
  { id: 'h1', version: 1, title: '频道公告v1', operator: '管理员', createTime: '2026-06-01 10:00' },
  { id: 'h2', version: 2, title: '频道公告v2', operator: '管理员', createTime: '2026-06-05 14:00' },
];

const mockGetAnnouncement = jest.fn().mockResolvedValue(mockAnnouncement);
const mockSaveAnnouncement = jest.fn().mockResolvedValue({});
const mockDeleteAnnouncement = jest.fn().mockResolvedValue({});
const mockPreviewAnnouncement = jest.fn().mockResolvedValue({ preview: '<p>预览内容</p>' });
const mockGetAnnouncementHistory = jest.fn().mockResolvedValue(mockHistory);
const mockRestoreAnnouncementVersion = jest.fn().mockResolvedValue({});

jest.mock('/@/api/content/channel/announcement', () => ({
  getAnnouncement: (...args: any[]) => mockGetAnnouncement(...args),
  saveAnnouncement: (...args: any[]) => mockSaveAnnouncement(...args),
  deleteAnnouncement: (...args: any[]) => mockDeleteAnnouncement(...args),
  previewAnnouncement: (...args: any[]) => mockPreviewAnnouncement(...args),
  getAnnouncementHistory: (...args: any[]) => mockGetAnnouncementHistory(...args),
  restoreAnnouncementVersion: (...args: any[]) => mockRestoreAnnouncementVersion(...args),
}));

jest.mock('/@/components/Tinymce', () => {
  const { defineComponent, h } = require('vue');
  const Tinymce = defineComponent({
    name: 'Tinymce',
    props: { modelValue: String, height: Number },
    emits: ['update:modelValue'],
    setup(props: any, { emit }: any) {
      return () =>
        h('textarea', {
          class: 'tinymce-mock',
          value: props.modelValue,
          onInput: (e: any) => emit('update:modelValue', e.target.value),
        });
    },
  });
  return { Tinymce };
});

jest.mock('ant-design-vue', () => {
  const { defineComponent, h } = require('vue');
  return {
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
          props: { label: String },
          setup(_p: any, { slots }: any) {
            return () =>
              h('div', { class: 'form-item' }, [_p.label ? h('label', _p.label) : null, slots.default?.()]);
          },
        }),
      },
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
              class: ['btn', _p.type === 'primary' ? 'btn-primary' : '', _p.danger ? 'btn-danger' : '']
                .filter(Boolean)
                .join(' '),
              onClick: () => emit('click'),
            },
            slots.default?.(),
          );
      },
    }),
    Space: defineComponent({
      name: 'Space',
      setup(_p: any, { slots }: any) {
        return () => h('div', { class: 'space' }, slots.default?.());
      },
    }),
    Tag: defineComponent({
      name: 'Tag',
      props: { color: String },
      setup(_p: any, { slots }: any) {
        return () => h('span', { class: 'tag' }, slots.default?.());
      },
    }),
    Divider: defineComponent({
      name: 'Divider',
      setup() {
        return () => h('hr');
      },
    }),
    Input: defineComponent({
      name: 'Input',
      props: { modelValue: String, value: String, placeholder: String, allowClear: Boolean },
      emits: ['update:modelValue', 'update:value'],
      setup() {
        return () => h('input', { class: 'input' });
      },
    }),
    Table: defineComponent({
      name: 'Table',
      props: { dataSource: Array, columns: Array, loading: Boolean, rowKey: [String, Function], size: String },
      setup(props: any, { slots }: any) {
        return () =>
          h('div', { class: 'table' }, [
            h(
              'div',
              { class: 'table-header' },
              (props.columns || []).map((c: any) => h('span', { key: c.key }, c.title)),
            ),
            ...(props.dataSource || []).map((item: any) =>
              h('div', { class: 'table-row', key: item.id }, [
                ...((props.columns || []).map((c: any) => h('span', { key: c.key }, item[c.dataIndex] || ''))),
                slots.bodyCell
                  ? (props.columns || [])
                      .filter((c: any) => c.key === 'action')
                      .map((c: any) => slots.bodyCell({ column: c, record: item }))
                  : null,
              ]),
            ),
          ]);
      },
    }),
    Modal: Object.assign(
      defineComponent({
        name: 'Modal',
        props: { visible: Boolean, title: String, width: Number },
        emits: ['update:visible', 'ok', 'cancel'],
        setup(_p: any, { slots }: any) {
          return () => h('div', { class: 'modal' }, slots.default?.());
        },
      }),
      { confirm: jest.fn(), info: jest.fn() },
    ),
    message: { success: jest.fn(), error: jest.fn(), info: jest.fn(), warning: jest.fn() },
  };
});

jest.mock('@ant-design/icons-vue', () => ({}));

describe('AnnouncementManage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGetAnnouncement.mockResolvedValue(mockAnnouncement);
    mockGetAnnouncementHistory.mockResolvedValue(mockHistory);
  });

  it('应加载当前公告状态', async () => {
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    expect(mockGetAnnouncement).toHaveBeenCalledWith('ch1');
    expect(wrapper.text()).toContain('已发布');
    expect(wrapper.text()).toContain('频道公告');
  });

  it('应展示公告历史列表', async () => {
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    expect(mockGetAnnouncementHistory).toHaveBeenCalledWith('ch1');
    expect(wrapper.text()).toContain('频道公告v1');
    expect(wrapper.text()).toContain('频道公告v2');
    expect(wrapper.text()).toContain('管理员');
  });

  it('点击发布应弹出确认框', async () => {
    const { Modal } = require('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const publishBtn = buttons.find((b) => b.text().includes('发布公告'));
    expect(publishBtn).toBeTruthy();
    await publishBtn!.trigger('click');

    expect(Modal.confirm).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '确认发布',
        onOk: expect.any(Function),
      }),
    );
  });

  it('点击删除应弹出确认框', async () => {
    const { Modal } = require('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const deleteBtn = buttons.find((b) => b.text().includes('删除公告'));
    expect(deleteBtn).toBeTruthy();
    await deleteBtn!.trigger('click');

    expect(Modal.confirm).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '确认删除',
        onOk: expect.any(Function),
      }),
    );
  });
});
