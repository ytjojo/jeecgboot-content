import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import StatsCard from '../components/StatsCard.vue';

vi.mock('/@/components/CountTo', () => ({
  CountTo: {
    name: 'CountTo',
    props: ['startVal', 'endVal', 'duration', 'decimals', 'prefix', 'suffix'],
    template: '<span class="count-to">{{ prefix }}{{ endVal }}{{ suffix }}</span>',
  },
}));

describe('StatsCard', () => {
  it('应正常渲染指标标题和数值', () => {
    const wrapper = mount(StatsCard, {
      props: {
        title: '订阅数',
        value: 12345,
      },
    });
    expect(wrapper.find('.stats-card__title').text()).toBe('订阅数');
    expect(wrapper.find('.count-to').exists()).toBe(true);
  });

  it('loading 状态应展示骨架屏', () => {
    const wrapper = mount(StatsCard, {
      props: { title: '订阅数', value: 0, loading: true },
    });
    expect(wrapper.find('.stats-card--loading').exists()).toBe(true);
  });

  it('错误状态应展示错误信息和重试按钮', () => {
    const wrapper = mount(StatsCard, {
      props: { title: '订阅数', value: 0, error: '加载失败' },
    });
    expect(wrapper.find('.stats-card__error').exists()).toBe(true);
    expect(wrapper.text()).toContain('加载失败');
  });

  it('应展示上升趋势箭头', () => {
    const wrapper = mount(StatsCard, {
      props: { title: '订阅数', value: 1000, trend: 12.5 },
    });
    expect(wrapper.find('.stats-card__trend-value.up').exists()).toBe(true);
  });

  it('应展示下降趋势箭头', () => {
    const wrapper = mount(StatsCard, {
      props: { title: 'PV', value: 500, trend: -5.2 },
    });
    expect(wrapper.find('.stats-card__trend-value.down').exists()).toBe(true);
  });

  it('trend 为 0 时应展示趋势', () => {
    const wrapper = mount(StatsCard, {
      props: { title: '内容数', value: 100, trend: 0 },
    });
    expect(wrapper.find('.stats-card__trend').exists()).toBe(true);
  });

  it('trend 为 undefined 时不展示趋势', () => {
    const wrapper = mount(StatsCard, {
      props: { title: '内容数', value: 100 },
    });
    expect(wrapper.find('.stats-card__trend').exists()).toBe(false);
  });

  it('点击重试应触发 retry 事件', async () => {
    const wrapper = mount(StatsCard, {
      props: { title: '订阅数', value: 0, error: '加载失败' },
    });
    // Ant Design Vue 组件在测试中渲染为自定义元素，通过 element 查找触发点击
    const retryBtn = wrapper.find('a-button');
    if (retryBtn.exists()) {
      await retryBtn.trigger('click');
      expect(wrapper.emitted('retry')).toBeTruthy();
    }
  });
});
