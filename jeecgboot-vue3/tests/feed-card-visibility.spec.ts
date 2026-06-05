import { mount } from '@vue/test-utils';

jest.mock('@ant-design/icons-vue', () => ({
  EyeInvisibleOutlined: { template: '<span class="eye-icon" />' },
}));

describe('FeedCard visibility badge', () => {
  const baseFeed = {
    id: '1',
    userId: 'u1',
    nickname: 'Alice',
    avatar: '',
    contentId: 'c1',
    contentTitle: 'Test Post',
    contentSummary: 'Summary',
    dynamicType: 'post' as const,
    createTime: '2025-06-01',
    isPriority: false,
  };

  async function mountCard(feed: Record<string, any>) {
    const Component = (await import('/@/components/social/FeedCard.vue')).default;
    return mount(Component, {
      props: { feed },
      global: {
        stubs: {
          Avatar: { template: '<span />' },
          Tag: { template: '<span><slot /></span>', props: ['color'] },
        },
      },
    });
  }

  it('does not show private badge for PUBLIC content', async () => {
    const wrapper = await mountCard({ ...baseFeed, visibility: 'PUBLIC' });
    expect(wrapper.find('.feed-card__private-badge').exists()).toBe(false);
  });

  it('shows private badge for MUTUAL_FOLLOW content', async () => {
    const wrapper = await mountCard({ ...baseFeed, visibility: 'MUTUAL_FOLLOW' });
    expect(wrapper.find('.feed-card__private-badge').exists()).toBe(true);
    expect(wrapper.text()).toContain('仅互关可见');
  });

  it('does not show private badge when visibility is undefined', async () => {
    const wrapper = await mountCard(baseFeed);
    expect(wrapper.find('.feed-card__private-badge').exists()).toBe(false);
  });
});
