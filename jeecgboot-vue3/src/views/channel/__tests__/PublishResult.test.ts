import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PublishResult from '../publish/PublishResult.vue';

describe('PublishResult', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应逐频道展示发布结果', () => {
    const wrapper = mount(PublishResult, {
      props: {
        results: [
          { channelId: '1', channelName: '频道A', status: 'success' },
          { channelId: '2', channelName: '频道B', status: 'review' },
          { channelId: '3', channelName: '频道C', status: 'fail', failReason: '发布限额已达上限' },
        ],
      },
    });
    expect(wrapper.text()).toContain('频道A');
    expect(wrapper.text()).toContain('已发布');
    expect(wrapper.text()).toContain('已提交审核');
    expect(wrapper.text()).toContain('发布限额已达上限');
  });

  it('失败项应展示重试按钮', () => {
    const wrapper = mount(PublishResult, {
      props: {
        results: [
          { channelId: '1', channelName: '频道A', status: 'fail', failReason: '限额' },
        ],
      },
    });
    expect(wrapper.find('.retry-btn').exists()).toBe(true);
  });

  it('定时发布应展示发布时间', () => {
    const wrapper = mount(PublishResult, {
      props: {
        results: [],
        scheduledTime: '2026-06-15 10:00',
      },
    });
    expect(wrapper.text()).toContain('已设定发布时间：2026-06-15 10:00');
  });
});
