import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PublishPermission from '../settings/PublishPermission.vue';

vi.mock('/@/api/content/channel/publish', () => ({
  getPublishPermission: vi.fn().mockResolvedValue({
    publishModel: 'all_members',
    hourlyLimit: 0,
    dailyLimit: 0,
    minWordCount: 0,
  }),
  savePublishPermission: vi.fn().mockResolvedValue({}),
}));

describe('PublishPermission', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应展示四种权限模型选项', () => {
    const wrapper = mount(PublishPermission, { props: { channelId: '1' } });
    expect(wrapper.text()).toContain('仅管理员可发布');
    expect(wrapper.text()).toContain('所有成员可发布');
    expect(wrapper.text()).toContain('公开投稿');
    expect(wrapper.text()).toContain('先审后发');
  });

  it('应展示限额配置表单', () => {
    const wrapper = mount(PublishPermission, { props: { channelId: '1' } });
    expect(wrapper.text()).toContain('每小时发布上限');
    expect(wrapper.text()).toContain('每日发布上限');
    expect(wrapper.text()).toContain('内容字数下限');
  });

  it('切换权限模型应弹出影响说明', async () => {
    const wrapper = mount(PublishPermission, { props: { channelId: '1' } });
    const radio = wrapper.find('input[value="open_submission"]');
    await radio.trigger('change');
    expect(wrapper.find('.impact-description').exists()).toBe(true);
  });
});
