import { mount } from '@vue/test-utils';

describe('PrivateContentGuard.vue', () => {
  async function mountComponent(props: Record<string, any> = {}) {
    const Component = (await import('/@/views/content/components/PrivateContentGuard.vue')).default;
    return mount(Component, {
      props,
      slots: { default: '<div class="guarded-content">Secret</div>' },
      global: {
        stubs: {
          'a-button': { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'size'] },
        },
      },
    });
  }

  it('renders slot content when accessible', async () => {
    const wrapper = await mountComponent({ accessible: true });
    expect(wrapper.find('.guarded-content').exists()).toBe(true);
    expect(wrapper.find('.private-content-guard').exists()).toBe(false);
  });

  it('shows guard message when not accessible', async () => {
    const wrapper = await mountComponent({ accessible: false, reason: 'not_mutual_follow' });
    expect(wrapper.find('.guarded-content').exists()).toBe(false);
    expect(wrapper.text()).toContain('该内容仅互关好友可见');
  });

  it('shows unfollowed message', async () => {
    const wrapper = await mountComponent({ accessible: false, reason: 'unfollowed' });
    expect(wrapper.text()).toContain('内容已不可见');
  });

  it('shows follow button when enabled', async () => {
    const wrapper = await mountComponent({ accessible: false, showFollowButton: true });
    expect(wrapper.find('button').exists()).toBe(true);
  });

  it('emits follow event', async () => {
    const wrapper = await mountComponent({ accessible: false, showFollowButton: true });
    await wrapper.find('button').trigger('click');
    expect(wrapper.emitted('follow')).toBeTruthy();
  });
});
