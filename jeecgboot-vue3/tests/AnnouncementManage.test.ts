import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import AnnouncementManage from '../src/views/channel/governance/AnnouncementManage.vue';

const mockAnnouncement = { id: 'a1', title: '频道公告', content: '<p>公告内容</p>', status: 'published', version: 2 };
const mockHistory = [
  { id: 'h1', version: 1, title: '频道公告v1', operator: '管理员', createTime: '2026-06-01 10:00' },
  { id: 'h2', version: 2, title: '频道公告v2', operator: '管理员', createTime: '2026-06-05 14:00' },
];

const mockGetAnnouncement = vi.fn().mockResolvedValue(mockAnnouncement);
const mockSaveAnnouncement = vi.fn().mockResolvedValue({});
const mockDeleteAnnouncement = vi.fn().mockResolvedValue({});
const mockPreviewAnnouncement = vi.fn().mockResolvedValue({ preview: '<p>预览内容</p>' });
const mockGetAnnouncementHistory = vi.fn().mockResolvedValue(mockHistory);
const mockRestoreAnnouncementVersion = vi.fn().mockResolvedValue({});

vi.mock('/@/api/content/channel/announcement', () => ({
  getAnnouncement: (...args: any[]) => mockGetAnnouncement(...args),
  saveAnnouncement: (...args: any[]) => mockSaveAnnouncement(...args),
  deleteAnnouncement: (...args: any[]) => mockDeleteAnnouncement(...args),
  previewAnnouncement: (...args: any[]) => mockPreviewAnnouncement(...args),
  getAnnouncementHistory: (...args: any[]) => mockGetAnnouncementHistory(...args),
  restoreAnnouncementVersion: (...args: any[]) => mockRestoreAnnouncementVersion(...args),
}));

vi.mock('/@/components/Tinymce', async () => {
  const { defineComponent, h } = await import('vue');
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

vi.mock('ant-design-vue', async () => {
  const { defineComponent, h } = await import('vue');
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
    DatePicker: defineComponent({
      name: 'DatePicker',
      props: { value: String, showTime: Boolean, valueFormat: String, placeholder: String, style: [String, Object] },
      emits: ['update:value'],
      setup() {
        return () => h('input', { class: 'date-picker' });
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
      { confirm: vi.fn(), info: vi.fn() },
    ),
    message: { success: vi.fn(), error: vi.fn(), info: vi.fn(), warning: vi.fn() },
  };
});

vi.mock('@ant-design/icons-vue', () => ({}));

describe('AnnouncementManage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
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
    const { Modal } = await import('ant-design-vue');
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
    const { Modal } = await import('ant-design-vue');
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

  // ======================== onOk 回调执行测试 ========================

  it('发布 onOk 应调用 saveAnnouncement 并刷新历史', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const publishBtn = buttons.find((b) => b.text().includes('发布公告'));
    await publishBtn!.trigger('click');

    // 重置 mount 阶段的调用计数，确保断言只测 onOk 触发的调用
    mockGetAnnouncementHistory.mockClear();

    const callArgs = vi.mocked(Modal.confirm).mock.calls[0][0] as any;
    await callArgs.onOk();
    await new Promise((r) => setTimeout(r, 0));

    expect(mockSaveAnnouncement).toHaveBeenCalledWith(
      expect.objectContaining({
        channelId: 'ch1',
        title: '频道公告',
        content: '<p>公告内容</p>',
        expireAt: undefined,
        version: 2,
      }),
    );
    expect(mockGetAnnouncementHistory).toHaveBeenCalled();
    expect((await import('ant-design-vue')).message.success).toHaveBeenCalledWith('公告已发布');
  });

  it('删除 onOk 应调用 deleteAnnouncement 并显示成功消息', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const deleteBtn = buttons.find((b) => b.text().includes('删除公告'));
    await deleteBtn!.trigger('click');

    // 重置 mount 阶段的调用计数
    mockGetAnnouncementHistory.mockClear();

    const callArgs = vi.mocked(Modal.confirm).mock.calls[0][0] as any;
    await callArgs.onOk();
    await new Promise((r) => setTimeout(r, 0));

    expect(mockDeleteAnnouncement).toHaveBeenCalledWith('a1');
    expect(mockGetAnnouncementHistory).toHaveBeenCalled();
    expect((await import('ant-design-vue')).message.success).toHaveBeenCalledWith('公告已删除');
  });

  // ======================== 保存草稿 / 预览 / 恢复按钮测试 ========================

  it('点击保存草稿应调用 saveAnnouncement', async () => {
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const saveBtn = buttons.find((b) => b.text().includes('保存草稿'));
    await saveBtn!.trigger('click');
    await new Promise((r) => setTimeout(r, 0));

    expect(mockSaveAnnouncement).toHaveBeenCalledWith(
      expect.objectContaining({ channelId: 'ch1' }),
    );
    expect((await import('ant-design-vue')).message.success).toHaveBeenCalledWith('草稿已保存');
  });

  it('点击预览应调用 previewAnnouncement 并弹出 Modal.info', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const previewBtn = buttons.find((b) => b.text().includes('预览'));
    await previewBtn!.trigger('click');
    await new Promise((r) => setTimeout(r, 0));

    expect(mockPreviewAnnouncement).toHaveBeenCalledWith({ content: '<p>公告内容</p>' });
    expect(Modal.info).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '公告预览',
        width: 600,
      }),
    );
  });

  it('点击恢复应弹出确认框', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const restoreButtons = wrapper.findAll('button').filter((b) => b.text() === '恢复');
    expect(restoreButtons.length).toBeGreaterThanOrEqual(1);
    await restoreButtons[0].trigger('click');

    expect(Modal.confirm).toHaveBeenCalledWith(
      expect.objectContaining({
        title: '确认恢复',
        onOk: expect.any(Function),
      }),
    );
  });

  it('恢复 onOk 应调用 restoreAnnouncementVersion 并刷新', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const restoreButtons = wrapper.findAll('button').filter((b) => b.text() === '恢复');
    await restoreButtons[0].trigger('click');

    // 重置 mount 阶段的调用计数
    mockGetAnnouncementHistory.mockClear();

    const callArgs = vi.mocked(Modal.confirm).mock.calls[0][0] as any;
    await callArgs.onOk();
    await new Promise((r) => setTimeout(r, 0));

    expect(mockRestoreAnnouncementVersion).toHaveBeenCalledWith('h1');
    expect(mockGetAnnouncementHistory).toHaveBeenCalled();
    expect((await import('ant-design-vue')).message.success).toHaveBeenCalledWith('版本已恢复');
  });

  // ======================== 状态与错误处理测试 ========================

  it('无公告时显示"未发布"Tag', async () => {
    mockGetAnnouncement.mockResolvedValue(null);
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('未发布');
  });

  it('加载历史失败时显示错误消息', async () => {
    mockGetAnnouncementHistory.mockRejectedValue(new Error('Network error'));
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    expect((await import('ant-design-vue')).message.error).toHaveBeenCalledWith('加载历史版本失败');
  });

  it('发布失败时显示错误消息', async () => {
    const { Modal } = await import('ant-design-vue');
    mockSaveAnnouncement.mockRejectedValue(new Error('Network error'));
    const wrapper = mount(AnnouncementManage, { props: { channelId: 'ch1' } });
    await new Promise((r) => setTimeout(r, 0));
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const publishBtn = buttons.find((b) => b.text().includes('发布公告'));
    await publishBtn!.trigger('click');

    const callArgs = vi.mocked(Modal.confirm).mock.calls[0][0] as any;
    await callArgs.onOk();
    await new Promise((r) => setTimeout(r, 0));

    expect((await import('ant-design-vue')).message.error).toHaveBeenCalledWith('发布失败，请重试');
  });
});
