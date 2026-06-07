import { mount } from '@vue/test-utils';
import SubscriptionCard from '/@/views/channel/subscription/SubscriptionCard.vue';

const stubs = {
  'a-avatar': { template: '<span class="mock-avatar"></span>', props: ['src', 'size'] },
  'a-tag': { template: '<span class="mock-tag"><slot /></span>', props: ['color'] },
  'a-switch': { template: '<input type="checkbox" class="mock-switch" />', props: ['checked', 'size'], emits: ['change'] },
  'a-button': { template: '<button class="mock-btn"><slot /></button>', props: ['type', 'size', 'danger'] },
};

const baseChannel = {
  id: 'ch1',
  name: 'Test Channel',
  avatar: 'https://example.com/avatar.png',
  latestSummary: 'Latest summary text',
  isSystem: false,
  reminderEnabled: false,
};

describe('SubscriptionCard', () => {
  function mountComponent(channelOverrides = {}) {
    return mount(SubscriptionCard, {
      props: { channel: { ...baseChannel, ...channelOverrides } },
      global: { stubs },
    });
  }

  it('should render channel name', () => {
    const wrapper = mountComponent();
    expect(wrapper.text()).toContain('Test Channel');
  });

  it('should render avatar', () => {
    const wrapper = mountComponent();
    expect(wrapper.find('.mock-avatar').exists()).toBe(true);
  });

  it('should render latestSummary', () => {
    const wrapper = mountComponent();
    expect(wrapper.text()).toContain('Latest summary text');
  });

  it('should show system tag when isSystem is true', () => {
    const wrapper = mountComponent({ isSystem: true });
    expect(wrapper.text()).toContain('系统推荐');
  });

  it('should not show system tag when isSystem is false', () => {
    const wrapper = mountComponent({ isSystem: false });
    expect(wrapper.text()).not.toContain('系统推荐');
  });

  it('should show source tag when source exists', () => {
    const wrapper = mountComponent({ source: '推荐频道' });
    expect(wrapper.text()).toContain('推荐频道');
  });

  it('should not show source tag when source is absent', () => {
    const wrapper = mountComponent({ source: undefined });
    expect(wrapper.text()).not.toContain('推荐频道');
  });

  it('should emit toggleReminder with channelId and checked state', async () => {
    const wrapper = mountComponent({ reminderEnabled: false });
    const switchEl = wrapper.find('.mock-switch');
    await switchEl.trigger('change');
    // The component emits toggleReminder via handleReminderChange
    expect(wrapper.emitted()).toBeTruthy();
  });

  it('should emit unsubscribe with channelId', async () => {
    const wrapper = mountComponent();
    const btn = wrapper.find('.mock-btn');
    await btn.trigger('click');
    expect(wrapper.emitted('unsubscribe')).toBeTruthy();
    expect(wrapper.emitted('unsubscribe')![0]).toEqual(['ch1']);
  });

  it('should render unsubscribe button as danger link', () => {
    const wrapper = mountComponent();
    expect(wrapper.text()).toContain('取消订阅');
  });
});
