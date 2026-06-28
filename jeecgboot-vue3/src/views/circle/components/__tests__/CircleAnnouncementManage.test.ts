import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';

const { mockGetActive, mockPublish, mockDelete, mockCreateMessage, mockModalConfirm } = vi.hoisted(() => ({
  mockGetActive: vi.fn(),
  mockPublish: vi.fn(),
  mockDelete: vi.fn(),
  mockCreateMessage: {
    success: vi.fn(),
    error: vi.fn(),
  },
  mockModalConfirm: vi.fn(),
}));

vi.mock('/@/api/content/circle/announcement', () => ({
  getActiveCircleAnnouncement: mockGetActive,
  publishCircleAnnouncement: mockPublish,
  deleteCircleAnnouncement: mockDelete,
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({
    createMessage: mockCreateMessage,
  }),
}));

vi.mock('ant-design-vue', () => ({
  Modal: {
    confirm: mockModalConfirm,
  },
}));

import CircleAnnouncementManage from '../CircleAnnouncementManage.vue';

function mountManage(circleId = 'circle-1') {
  return mount(CircleAnnouncementManage, {
    props: {
      circleId,
      visible: false,
    },
    global: {
      stubs: {
        'a-modal': {
          template: '<div class="modal-stub" v-if="visible"><slot /></div>',
          props: ['visible', 'title', 'maskClosable', 'footer', 'destroyOnClose'],
        },
        'a-form': {
          template: '<form class="form-stub"><slot /></form>',
          props: ['model', 'layout'],
        },
        'a-form-item': {
          template: '<div class="form-item-stub" :data-status="validateStatus"><slot /><div v-if="help" class="help-text">{{ help }}</div></div>',
          props: ['label', 'required', 'validateStatus', 'help'],
        },
        'a-textarea': {
          template: '<textarea class="textarea-stub" :value="value" @input="$emit(\'update:value\', $event.target.value)" :rows="rows" :maxlength="maxlength"></textarea>',
          props: ['value', 'rows', 'maxlength', 'showCount', 'placeholder'],
        },
        'a-date-picker': {
          template: '<input class="date-picker-stub" :value="value" @change="$emit(\'update:value\', $event.target.value)" />',
          props: ['value', 'showTime', 'valueFormat', 'placeholder'],
        },
        'a-button': {
          template: '<button class="btn-stub" :class="type" :disabled="loading" @click="$emit(\'click\')"><slot /></button>',
          props: ['type', 'danger', 'loading'],
          emits: ['click'],
        },
      },
    },
  });
}

describe('CircleAnnouncementManage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetActive.mockResolvedValue(null);
    mockPublish.mockResolvedValue(undefined);
    mockDelete.mockResolvedValue(undefined);
    mockModalConfirm.mockImplementation(({ onOk }) => {
      onOk?.();
    });
  });

  async function openModal(wrapper: ReturnType<typeof mountManage>) {
    await wrapper.setProps({ visible: true });
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();
  }

  function getFutureTimeStr(days = 1) {
    return new Date(Date.now() + days * 86400000).toISOString();
  }

  function getPastTimeStr() {
    return new Date(Date.now() - 86400000).toISOString();
  }

  it('打开弹窗时应调用 getActiveCircleAnnouncement 加载公告', async () => {
    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    expect(mockGetActive).toHaveBeenCalledWith('circle-1');
  });

  it('有有效公告时应回填表单并显示更新/删除按钮', async () => {
    const futureTime = getFutureTimeStr();
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '测试公告内容',
      expireAt: futureTime,
      createTime: '2026-06-01T00:00:00.000Z',
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    expect(wrapper.text()).toContain('更新公告');
    expect(wrapper.text()).toContain('删除公告');
    expect((wrapper.find('.textarea-stub').element as HTMLTextAreaElement).value).toBe('测试公告内容');
  });

  it('已过期公告应清空表单且不显示删除按钮', async () => {
    const pastTime = getPastTimeStr();
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '过期公告',
      expireAt: pastTime,
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    expect(wrapper.text()).toContain('发布公告');
    expect(wrapper.text()).not.toContain('删除公告');
    expect(wrapper.text()).not.toContain('更新公告');
  });

  it('无公告时表单为空且显示发布按钮', async () => {
    mockGetActive.mockResolvedValue(null);

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    expect(wrapper.text()).toContain('发布公告');
    expect(wrapper.text()).not.toContain('删除公告');
    expect(wrapper.text()).not.toContain('更新公告');
  });

  it('空内容提交应显示错误提示', async () => {
    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const publishBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('发布公告'));
    expect(publishBtn).toBeTruthy();
    await publishBtn!.trigger('click');
    await nextTick();

    expect(wrapper.text()).toContain('请输入公告内容');
    expect(mockPublish).not.toHaveBeenCalled();
  });

  it('未选择有效期提交应显示错误提示', async () => {
    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const textarea = wrapper.find('.textarea-stub');
    await textarea.setValue('新公告内容');
    await nextTick();

    const publishBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('发布公告'));
    await publishBtn!.trigger('click');
    await nextTick();

    expect(wrapper.text()).toContain('请选择有效期');
    expect(mockPublish).not.toHaveBeenCalled();
  });

  it('有效期为过去时间应显示错误提示', async () => {
    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const textarea = wrapper.find('.textarea-stub');
    await textarea.setValue('新公告内容');
    await nextTick();

    const datePicker = wrapper.find('.date-picker-stub');
    const pastTime = getPastTimeStr();
    await datePicker.setValue(pastTime);
    await nextTick();

    const publishBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('发布公告'));
    await publishBtn!.trigger('click');
    await nextTick();

    expect(wrapper.text()).toContain('有效期不得早于当前时间');
    expect(mockPublish).not.toHaveBeenCalled();
  });

  it('无已有公告时填写有效内容直接发布成功', async () => {
    mockGetActive.mockResolvedValue(null);
    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const futureTime = getFutureTimeStr();
    const textarea = wrapper.find('.textarea-stub');
    await textarea.setValue('新公告内容');
    const datePicker = wrapper.find('.date-picker-stub');
    await datePicker.setValue(futureTime);
    await nextTick();

    const publishBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('发布公告'));
    await publishBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockPublish).toHaveBeenCalledWith({
      circleId: 'circle-1',
      content: '新公告内容',
      expireAt: futureTime,
    });
    expect(mockCreateMessage.success).toHaveBeenCalledWith('公告已发布');
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
    expect(wrapper.emitted('published')).toBeTruthy();
  });

  it('有已有公告时发布需弹出确认框，确认后发布', async () => {
    const existingFutureTime = getFutureTimeStr(1);
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '旧公告',
      expireAt: existingFutureTime,
    });
    mockModalConfirm.mockImplementation(({ onOk }) => {
      onOk?.();
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const newFutureTime = getFutureTimeStr(2);
    const textarea = wrapper.find('.textarea-stub');
    await textarea.setValue('更新后的公告');
    const datePicker = wrapper.find('.date-picker-stub');
    await datePicker.setValue(newFutureTime);
    await nextTick();

    const updateBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('更新公告'));
    expect(updateBtn).toBeTruthy();
    await updateBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockModalConfirm).toHaveBeenCalled();
    expect(mockPublish).toHaveBeenCalledWith({
      circleId: 'circle-1',
      content: '更新后的公告',
      expireAt: newFutureTime,
    });
    expect(mockCreateMessage.success).toHaveBeenCalledWith('公告已发布');
    expect(wrapper.emitted('published')).toBeTruthy();
  });

  it('有已有公告时发布取消确认则不发布', async () => {
    const existingFutureTime = getFutureTimeStr(1);
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '旧公告',
      expireAt: existingFutureTime,
    });
    mockModalConfirm.mockImplementation(({ onCancel }) => {
      onCancel?.();
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const newFutureTime = getFutureTimeStr(2);
    const textarea = wrapper.find('.textarea-stub');
    await textarea.setValue('更新后的公告');
    const datePicker = wrapper.find('.date-picker-stub');
    await datePicker.setValue(newFutureTime);
    await nextTick();

    const updateBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('更新公告'));
    expect(updateBtn).toBeTruthy();
    await updateBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockModalConfirm).toHaveBeenCalled();
    expect(mockPublish).not.toHaveBeenCalled();
    expect(wrapper.emitted('published')).toBeFalsy();
  });

  it('发布失败应显示错误提示', async () => {
    mockGetActive.mockResolvedValue(null);
    mockPublish.mockRejectedValue(new Error('Network error'));

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const futureTime = getFutureTimeStr();
    const textarea = wrapper.find('.textarea-stub');
    await textarea.setValue('新公告内容');
    const datePicker = wrapper.find('.date-picker-stub');
    await datePicker.setValue(futureTime);
    await nextTick();

    const publishBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('发布公告'));
    await publishBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockCreateMessage.error).toHaveBeenCalledWith('发布失败，请重试');
    expect(wrapper.emitted('published')).toBeFalsy();
  });

  it('点击删除按钮弹出确认框，确认后删除成功', async () => {
    const futureTime = getFutureTimeStr();
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '测试公告',
      expireAt: futureTime,
    });
    mockModalConfirm.mockImplementation(({ onOk }) => {
      onOk?.();
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const deleteBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('删除公告'));
    expect(deleteBtn).toBeTruthy();
    await deleteBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockModalConfirm).toHaveBeenCalled();
    expect(mockDelete).toHaveBeenCalledWith('a1');
    expect(mockCreateMessage.success).toHaveBeenCalledWith('公告已删除');
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
    expect(wrapper.emitted('deleted')).toBeTruthy();
  });

  it('删除取消确认则不删除', async () => {
    const futureTime = getFutureTimeStr();
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '测试公告',
      expireAt: futureTime,
    });
    mockModalConfirm.mockImplementation(({ onCancel }) => {
      onCancel?.();
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const deleteBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('删除公告'));
    expect(deleteBtn).toBeTruthy();
    await deleteBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockDelete).not.toHaveBeenCalled();
    expect(wrapper.emitted('deleted')).toBeFalsy();
  });

  it('删除失败应显示错误提示', async () => {
    const futureTime = getFutureTimeStr();
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '测试公告',
      expireAt: futureTime,
    });
    mockDelete.mockRejectedValue(new Error('Network error'));
    mockModalConfirm.mockImplementation(({ onOk }) => {
      onOk?.();
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const deleteBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text().includes('删除公告'));
    expect(deleteBtn).toBeTruthy();
    await deleteBtn!.trigger('click');
    await vi.dynamicImportSettled();
    await nextTick();

    expect(mockCreateMessage.error).toHaveBeenCalledWith('删除失败，请重试');
    expect(wrapper.emitted('deleted')).toBeFalsy();
  });

  it('点击取消按钮关闭弹窗', async () => {
    const wrapper = mountManage('circle-1');
    await openModal(wrapper);

    const cancelBtn = wrapper.findAll('.btn-stub').find((btn) => btn.text() === '取消');
    expect(cancelBtn).toBeTruthy();
    await cancelBtn!.trigger('click');
    await nextTick();

    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });

  it('visible 变为 false 时清空表单', async () => {
    const futureTime = getFutureTimeStr();
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '测试公告',
      expireAt: futureTime,
    });

    const wrapper = mountManage('circle-1');
    await openModal(wrapper);
    expect((wrapper.find('.textarea-stub').element as HTMLTextAreaElement).value).toBe('测试公告');

    await wrapper.setProps({ visible: false });
    await nextTick();
  });
});
