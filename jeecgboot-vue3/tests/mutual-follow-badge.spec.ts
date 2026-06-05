import { mount } from '@vue/test-utils';

describe('MutualFollowBadge.vue', () => {
  async function mountComponent(props = {}) {
    const Component = (await import('/@/views/content/components/MutualFollowBadge.vue')).default;
    return mount(Component, {
      props: { mutualFollow: true, ...props },
      global: {
        stubs: {
          'a-tag': { template: '<span class="tag-stub"><slot /></span>', props: ['color', 'size'] },
        },
      },
    });
  }

  it('renders badge when mutualFollow is true', async () => {
    const wrapper = await mountComponent({ mutualFollow: true });
    expect(wrapper.find('.mutual-follow-badge').exists()).toBe(true);
    expect(wrapper.text()).toContain('互关');
  });

  it('does not render when mutualFollow is false', async () => {
    const wrapper = await mountComponent({ mutualFollow: false });
    expect(wrapper.find('.mutual-follow-badge').exists()).toBe(false);
  });

  it('applies small size class when size is small', async () => {
    const wrapper = await mountComponent({ size: 'small' });
    expect(wrapper.find('.mutual-follow-badge--small').exists()).toBe(true);
  });

  it('applies inline mode class when inline is true', async () => {
    const wrapper = await mountComponent({ inline: true });
    expect(wrapper.find('.mutual-follow-badge--inline').exists()).toBe(true);
  });
});
