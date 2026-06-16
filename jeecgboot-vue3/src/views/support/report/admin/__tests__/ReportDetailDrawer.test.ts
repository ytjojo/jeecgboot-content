import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ReportDetailDrawer from '../ReportDetailDrawer.vue';
import type { CircleReportVO } from '/@/api/content/circle/report';

const mockReport: CircleReportVO = {
  id: 'report-001',
  circleId: 'circle-123',
  contentId: 'content-456',
  reporterId: 'user-789',
  reason: '违规内容',
  status: 'PENDING',
  handleAction: 'DELETE_CONTENT',
  createTime: '2024-01-15 10:30:00',
};

describe('ReportDetailDrawer', () => {
  function createWrapper(props = {}) {
    return mount(ReportDetailDrawer, {
      props: {
        visible: true,
        report: mockReport,
        ...props,
      },
      global: {
        stubs: {
          'a-drawer': {
            template: '<div class="ant-drawer" @click="$emit(\'close\')"><slot /></div>',
            props: ['open', 'title', 'placement', 'width'],
          },
          'a-descriptions': {
            template: '<div class="ant-descriptions"><slot /></div>',
            props: ['column', 'size', 'bordered'],
          },
          'a-descriptions-item': {
            template: '<div class="ant-descriptions-item"><span class="ant-descriptions-item-label">{{ label }}</span>: <span class="ant-descriptions-item-content"><slot /></span></div>',
            props: ['label'],
          },
          'a-tag': {
            template: '<span class="ant-tag" :data-color="color"><slot /></span>',
            props: ['color'],
          },
          'a-empty': {
            template: '<div class="ant-empty"><slot name="description" /></div>',
            props: ['description'],
          },
        },
      },
    });
  }

  it('有 report 数据时显示详情', () => {
    const wrapper = createWrapper();
    expect(wrapper.find('.ant-descriptions').exists()).toBe(true);
    expect(wrapper.find('.ant-empty').exists()).toBe(false);
  });

  it('显示举报 ID', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('report-001');
  });

  it('显示圈子 ID、内容 ID、举报人', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('circle-123');
    expect(wrapper.text()).toContain('content-456');
    expect(wrapper.text()).toContain('user-789');
  });

  it('显示举报原因 Tag', () => {
    const wrapper = createWrapper();
    const tags = wrapper.findAll('.ant-tag');
    const reasonTag = tags.find(t => t.text() === '违规内容');
    expect(reasonTag).toBeTruthy();
  });

  it('显示处理动作和时间', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('DELETE_CONTENT');
    expect(wrapper.text()).toContain('2024-01-15 10:30:00');
  });

  it('无 report 时显示空状态', () => {
    const wrapper = createWrapper({ report: null });
    expect(wrapper.find('.ant-empty').exists()).toBe(true);
    expect(wrapper.find('.ant-descriptions').exists()).toBe(false);
  });

  it('关闭抽屉时触发 update:visible 事件', async () => {
    const wrapper = createWrapper();
    const drawer = wrapper.find('.ant-drawer');
    await drawer.trigger('click');
    expect(wrapper.emitted('update:visible')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });

  it('处理动作为空时显示"-"', () => {
    const wrapper = createWrapper({
      report: { ...mockReport, handleAction: undefined },
    });
    expect(wrapper.text()).toContain('-');
  });
});
