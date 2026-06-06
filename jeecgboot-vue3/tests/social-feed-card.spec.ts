import { vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';

// Mock the mutualFollow store to avoid real API calls
vi.mock('/@/store/modules/mutualFollow', () => ({
  useMutualFollowStore: vi.fn(() => ({
    isMutual: vi.fn(() => false),
    fetchAndCache: vi.fn(),
  })),
}));

// Mock PrivateContentGuard to avoid deeper dependency chain
vi.mock('/@/views/content/components/PrivateContentGuard.vue', () => ({
  default: {
    name: 'PrivateContentGuard',
    template: '<div class="private-guard-stub"><slot /></div>',
    props: ['accessible', 'reason'],
  },
}));

function makeFeed(overrides: Record<string, any> = {}) {
  return {
    id: '1',
    userId: 'u-1',
    nickname: 'Test User',
    avatar: 'https://cdn/avatar.png',
    contentId: 'c-1',
    contentTitle: 'Test Post Title',
    contentSummary: 'This is a summary of the post',
    dynamicType: 'post' as const,
    createTime: '2025-06-01',
    isPriority: false,
    ...overrides,
  };
}

describe('FeedCard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  async function mountComponent(feedOverrides: Record<string, any> = {}, props: { isMobile?: boolean } = {}) {
    const Component = (await import('/@/components/social/FeedCard.vue')).default;
    const feed = makeFeed(feedOverrides);
    return mount(Component, {
      props: { feed, ...props },
      global: {
        stubs: {
          Avatar: { template: '<div class="avatar-stub" />', props: ['size', 'src'] },
          Tag: { template: '<span class="tag-stub"><slot /></span>', props: ['color'] },
        },
      },
    });
  }

  describe('rendering', () => {
    it('renders nickname and title', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('.feed-card__nickname').text()).toBe('Test User');
      expect(wrapper.find('.feed-card__title').text()).toBe('Test Post Title');
    });

    it('renders content summary on desktop', async () => {
      const wrapper = await mountComponent({}, { isMobile: false });
      expect(wrapper.find('.feed-card__summary').exists()).toBe(true);
      expect(wrapper.find('.feed-card__summary').text()).toBe('This is a summary of the post');
    });

    it('hides content summary on mobile', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.feed-card__summary').exists()).toBe(false);
    });

    it('renders avatar', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('.avatar-stub').exists()).toBe(true);
    });

    it('renders create time', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('.feed-card__time').text()).toBe('2025-06-01');
    });
  });

  describe('dynamic type tags', () => {
    it('renders "发帖" tag for post type', async () => {
      const wrapper = await mountComponent({ dynamicType: 'post' });
      const tags = wrapper.findAll('.tag-stub');
      expect(tags[0].text()).toBe('发帖');
    });

    it('renders "点赞" tag for like type', async () => {
      const wrapper = await mountComponent({ dynamicType: 'like' });
      const tags = wrapper.findAll('.tag-stub');
      expect(tags[0].text()).toBe('点赞');
    });

    it('renders "收藏" tag for favorite type', async () => {
      const wrapper = await mountComponent({ dynamicType: 'favorite' });
      const tags = wrapper.findAll('.tag-stub');
      expect(tags[0].text()).toBe('收藏');
    });

    it('renders source tag when sourceName exists', async () => {
      const wrapper = await mountComponent({ sourceName: 'Tech News' });
      const tags = wrapper.findAll('.tag-stub');
      expect(tags).toHaveLength(2);
      expect(tags[1].text()).toBe('Tech News');
    });

    it('does not render source tag when sourceName is empty', async () => {
      const wrapper = await mountComponent({ sourceName: '' });
      const tags = wrapper.findAll('.tag-stub');
      expect(tags).toHaveLength(1);
    });
  });

  describe('priority styling', () => {
    it('adds is-priority class when isPriority is true', async () => {
      const wrapper = await mountComponent({ isPriority: true });
      expect(wrapper.find('.is-priority').exists()).toBe(true);
    });

    it('does not add is-priority class when isPriority is false', async () => {
      const wrapper = await mountComponent({ isPriority: false });
      expect(wrapper.find('.is-priority').exists()).toBe(false);
    });
  });

  describe('mobile styling', () => {
    it('adds is-mobile class when isMobile is true', async () => {
      const wrapper = await mountComponent({}, { isMobile: true });
      expect(wrapper.find('.is-mobile').exists()).toBe(true);
    });
  });

  describe('click event', () => {
    it('emits click with feed data when clicked', async () => {
      const wrapper = await mountComponent();
      await wrapper.find('.feed-card').trigger('click');

      const emitted = wrapper.emitted('click');
      expect(emitted).toBeTruthy();
      expect(emitted![0][0]).toMatchObject({
        id: '1',
        nickname: 'Test User',
        contentTitle: 'Test Post Title',
      });
    });
  });
});
