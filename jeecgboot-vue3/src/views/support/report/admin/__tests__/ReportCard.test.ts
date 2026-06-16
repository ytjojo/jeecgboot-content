import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ReportCard from '../ReportCard.vue';
import type { CircleReportVO } from '/@/api/content/circle/report';

// Mock ant-design-vue: replace Button and Tag with simple stubs since
// global.stubs don't override locally-imported components in vue-test-utils
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    Tag: {
      name: 'Tag',
      template: '<span class="ant-tag" :data-color="color"><slot /></span>',
      props: ['color'],
    },
    Button: {
      name: 'Button',
      template: '<button class="ant-btn"><slot /></button>',
      props: ['size', 'type', 'danger'],
    },
    message: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn(),
    },
  };
});

const mockReport: CircleReportVO = {
  id: 'report-001-abc-def-ghi',
  circleId: 'circle-123',
  contentId: 'content-456',
  reporterId: 'user-789',
  reason: '违规内容',
  status: 'PENDING',
  handleAction: 'DELETE_CONTENT',
  createTime: '2024-01-15 10:30:00',
};

describe('ReportCard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  function createWrapper(props = {}) {
    return mount(ReportCard, {
      props: { report: mockReport, ...props },
    });
  }

  it('渲染举报 ID 前缀和截断的 ID', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('#report-0');
    expect(wrapper.find('.report-card-no').exists()).toBe(true);
  });

  it('渲染被举报内容 ID', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('content-456');
  });

  it('渲染举报原因 Tag', () => {
    const wrapper = createWrapper();
    const tags = wrapper.findAll('.ant-tag');
    const reasonTag = tags.find(t => t.text() === '违规内容');
    expect(reasonTag).toBeTruthy();
  });

  it('PENDING 状态显示橙色状态标签和"待处理"文字', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('待处理');
    const headerTags = wrapper.find('.report-card-header').findAll('.ant-tag');
    expect(headerTags.length).toBeGreaterThan(0);
    expect(headerTags[0].attributes('data-color')).toBe('orange');
  });

  it('RESOLVED 状态显示"已处理"', () => {
    const wrapper = createWrapper({
      report: { ...mockReport, status: 'RESOLVED' },
    });
    expect(wrapper.text()).toContain('已处理');
  });

  it('IGNORED 状态显示"已忽略"', () => {
    const wrapper = createWrapper({
      report: { ...mockReport, status: 'IGNORED' },
    });
    expect(wrapper.text()).toContain('已忽略');
  });

  it('PENDING 状态显示操作按钮（查看/删除内容/忽略/禁言）', () => {
    const wrapper = createWrapper();
    const buttons = wrapper.findAll('.ant-btn');
    const texts = buttons.map(b => b.text());
    expect(texts).toContain('查看');
    expect(texts).toContain('删除内容');
    expect(texts).toContain('忽略');
    expect(texts).toContain('禁言');
  });

  it('非 PENDING 状态下仅显示"查看"按钮', () => {
    const wrapper = createWrapper({
      report: { ...mockReport, status: 'RESOLVED' },
    });
    const buttons = wrapper.findAll('.ant-btn');
    const texts = buttons.map(b => b.text());
    expect(texts).toEqual(['查看']);
  });

  it('点击"查看"按钮触发 detail 事件', async () => {
    const wrapper = createWrapper();
    const viewBtn = wrapper.findAll('.ant-btn').find(b => b.text() === '查看');
    await viewBtn!.trigger('click');
    expect(wrapper.emitted('detail')).toBeTruthy();
    expect(wrapper.emitted('detail')![0]).toEqual([mockReport]);
  });

  it('点击"删除内容"按钮触发 deleteContent 事件', async () => {
    const wrapper = createWrapper();
    const deleteBtn = wrapper.findAll('.ant-btn').find(b => b.text() === '删除内容');
    await deleteBtn!.trigger('click');
    expect(wrapper.emitted('deleteContent')).toBeTruthy();
  });

  it('点击"忽略"按钮触发 ignore 事件', async () => {
    const wrapper = createWrapper();
    const ignoreBtn = wrapper.findAll('.ant-btn').find(b => b.text() === '忽略');
    await ignoreBtn!.trigger('click');
    expect(wrapper.emitted('ignore')).toBeTruthy();
  });

  it('点击"禁言"按钮触发 mute 事件', async () => {
    const wrapper = createWrapper();
    const muteBtn = wrapper.findAll('.ant-btn').find(b => b.text() === '禁言');
    await muteBtn!.trigger('click');
    expect(wrapper.emitted('mute')).toBeTruthy();
  });
});
