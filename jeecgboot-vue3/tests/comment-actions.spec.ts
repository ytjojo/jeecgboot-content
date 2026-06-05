const mockPush = jest.fn();
const mockHasPermission = jest.fn().mockReturnValue(false);

jest.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}));

jest.mock('/@/hooks/web/usePermission', () => ({
  usePermission: () => ({ hasPermission: mockHasPermission }),
}));

import { mount } from '@vue/test-utils';

describe('CommentActions.vue', () => {
  const baseComment = {
    id: 'c1',
    fromUserId: 'u1',
    communityRole: 'CREATOR',
    roleVerified: true,
  };

  async function mountComponent(comment: Record<string, any> = baseComment, isMod = false) {
    mockHasPermission.mockImplementation((perm: string) => {
      if (perm === 'content:moderator') return isMod;
      if (perm === 'content:admin') return isMod;
      return false;
    });
    const Component = (await import('/@/views/content/components/CommentActions.vue')).default;
    return mount(Component, {
      props: { comment },
      global: {
        stubs: {
          CommunityRoleBadge: { template: '<span class="badge-stub" />', props: ['role', 'verified', 'size', 'inline'] },
          'a-divider': { template: '<hr />', props: ['type'] },
          'a-dropdown': { template: '<div><slot /><slot name="overlay" /></div>', props: ['trigger'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'size'] },
          'a-menu': { template: '<div><slot /></div>', props: ['onClick'] },
          'a-menu-item': { template: '<div class="menu-item" @click="$emit(\'click\')"><slot /></div>', props: ['key'] },
        },
      },
    });
  }

  it('renders CommunityRoleBadge when role is not NORMAL', async () => {
    const wrapper = await mountComponent();
    expect(wrapper.find('.badge-stub').exists()).toBe(true);
  });

  it('does not render moderation menu for non-moderators', async () => {
    const wrapper = await mountComponent(baseComment, false);
    expect(wrapper.find('button').exists()).toBe(false);
  });

  it('renders moderation menu for moderators', async () => {
    const wrapper = await mountComponent(baseComment, true);
    expect(wrapper.find('button').exists()).toBe(true);
  });

  it('emits deleteComment event', async () => {
    const wrapper = await mountComponent(baseComment, true);
    const vm = wrapper.vm as any;
    vm.handleMenuClick({ key: 'deleteComment' });
    expect(wrapper.emitted('deleteComment')?.[0]).toEqual(['c1']);
  });

  it('emits warnUser event', async () => {
    const wrapper = await mountComponent(baseComment, true);
    const vm = wrapper.vm as any;
    vm.handleMenuClick({ key: 'warnUser' });
    expect(wrapper.emitted('warnUser')?.[0]).toEqual(['u1']);
  });

  it('navigates to user management for admin', async () => {
    const wrapper = await mountComponent(baseComment, true);
    const vm = wrapper.vm as any;
    vm.handleMenuClick({ key: 'manageUser' });
    expect(mockPush).toHaveBeenCalledWith({ path: '/system/user/u1' });
  });
});
