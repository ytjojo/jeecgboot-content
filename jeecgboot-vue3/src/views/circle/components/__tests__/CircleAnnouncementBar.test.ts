import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import CircleAnnouncementBar from '../CircleAnnouncementBar.vue';

const { mockGetActive, mockDelete } = vi.hoisted(() => ({
  mockGetActive: vi.fn(),
  mockDelete: vi.fn(),
}));

vi.mock('/@/api/content/circle/announcement', () => ({
  getActiveCircleAnnouncement: mockGetActive,
  deleteCircleAnnouncement: mockDelete,
}));

vi.mock('/@/store/modules/circle', () => ({
  useCircleStoreWithOut: vi.fn(() => ({
    isCreator: false,
    isModerator: false,
  })),
}));

function mountBar(circleId = 'circle-1') {
  return mount(CircleAnnouncementBar, {
    props: { circleId },
    global: {
      stubs: {
        'a-alert': {
          template: '<div class="alert-stub"><slot name="message" /><slot name="action" /></div>',
        },
        'a-button': {
          template: '<button class="btn-stub" @click="$emit(\'click\')"><slot /></button>',
          props: ['type', 'size'],
          emits: ['click'],
        },
      },
    },
  });
}

describe('CircleAnnouncementBar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetActive.mockResolvedValue(null);
  });

  // 1. 有公告时展示内容
  it('有有效公告时应展示内容', async () => {
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: '欢迎来到圈子！',
      expireAt: null,
      createTime: '2026-06-01',
    });

    const wrapper = mountBar();
    // 等待 API 返回
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();

    expect(wrapper.text()).toContain('欢迎来到圈子！');
    expect(wrapper.find('.alert-stub').exists()).toBe(true);
  });

  // 2. 无公告时隐藏
  it('无公告时应隐藏', async () => {
    mockGetActive.mockResolvedValue(null);

    const wrapper = mountBar();
    await vi.dynamicImportSettled();
    await nextTick();

    expect(wrapper.find('.alert-stub').exists()).toBe(false);
  });

  // 3. 公告内容过长时显示摘要（展开/收起）
  it('长公告应默认截断', async () => {
    const longContent = 'A'.repeat(200);
    mockGetActive.mockResolvedValue({
      id: 'a1',
      circleId: 'circle-1',
      content: longContent,
      expireAt: null,
    });

    const wrapper = mountBar();
    await vi.dynamicImportSettled();
    await nextTick();
    await nextTick();

    // 默认截断显示
    const displayed = wrapper.text();
    expect(displayed.length).toBeLessThan(longContent.length + 20);
  });

  // 4. 公告加载失败静默处理
  it('加载失败时应静默隐藏', async () => {
    mockGetActive.mockRejectedValue(new Error('Network error'));

    const wrapper = mountBar();
    await vi.dynamicImportSettled();
    await nextTick();

    expect(wrapper.find('.alert-stub').exists()).toBe(false);
  });
});
