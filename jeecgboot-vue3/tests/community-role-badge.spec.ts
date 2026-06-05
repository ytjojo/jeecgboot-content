import { mount } from '@vue/test-utils';

describe('CommunityRoleBadge.vue', () => {
  async function mountComponent(props = {}) {
    const Component = (await import('/@/views/content/components/CommunityRoleBadge.vue')).default;
    return mount(Component, {
      props: { role: 'MODERATOR', ...props },
      global: {
        stubs: {
          'a-tag': { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
          'a-popover': {
            template: '<span class="popover-stub"><slot /><slot name="content" /></span>',
            props: ['title', 'overlayClassName'],
          },
        },
      },
    });
  }

  it('renders role label for MODERATOR', async () => {
    const wrapper = await mountComponent({ role: 'MODERATOR' });
    expect(wrapper.find('.community-role-badge').exists()).toBe(true);
    expect(wrapper.text()).toContain('版主');
  });

  it('does not render for NORMAL role', async () => {
    const wrapper = await mountComponent({ role: 'NORMAL' });
    expect(wrapper.find('.community-role-badge').exists()).toBe(false);
  });

  it('renders verification icon when verified is true', async () => {
    const wrapper = await mountComponent({ role: 'CREATOR', verified: true });
    expect(wrapper.find('.community-role-badge__verified').exists()).toBe(true);
  });

  it('does not render verification icon when verified is false', async () => {
    const wrapper = await mountComponent({ role: 'CREATOR', verified: false });
    expect(wrapper.find('.community-role-badge__verified').exists()).toBe(false);
  });

  it('wraps in popover when showDetail is true', async () => {
    const wrapper = await mountComponent({ role: 'ADMIN', showDetail: true });
    expect(wrapper.find('.popover-stub').exists()).toBe(true);
  });
});
