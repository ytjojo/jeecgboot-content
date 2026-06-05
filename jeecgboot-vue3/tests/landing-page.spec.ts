import { mount, flushPromises } from '@vue/test-utils';
import { nextTick } from 'vue';

const mockPush = jest.fn();
const mockReplace = jest.fn();
let mockToken = '';

jest.mock('vue-router', () => ({
  useRoute: () => ({ params: { inviteCode: 'CODE123' } }),
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
}));

const mockValidateInviteCode = jest.fn().mockResolvedValue({ valid: true });
jest.mock('/@/api/content/invite', () => ({
  validateInviteCode: (...args: any[]) => mockValidateInviteCode(...args),
}));

jest.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({ getToken: mockToken }),
}));

describe('LandingPage.vue', () => {
  async function mountPage() {
    const Component = (await import('/@/views/content/invite/LandingPage.vue')).default;
    return mount(Component, {
      global: {
        stubs: {
          'a-spin': { template: '<div><slot /></div>', props: ['spinning'] },
          'a-result': { template: '<div class="result-stub">{{ title }}<slot name="extra" /></div>', props: ['status', 'title', 'subTitle'] },
          'a-button': { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'size', 'block'] },
        },
      },
    });
  }

  beforeEach(() => {
    jest.clearAllMocks();
    mockToken = '';
  });

  it('shows value points when invite code is valid', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('发现优质内容');
    expect(wrapper.html()).toContain('立即注册');
  });

  it('navigates to register with invite code', async () => {
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    const buttons = wrapper.findAll('button');
    await buttons[0].trigger('click');
    expect(mockPush).toHaveBeenCalledWith({ path: '/register', query: { inviteCode: 'CODE123' } });
  });

  it('shows error when invite code is invalid', async () => {
    mockValidateInviteCode.mockResolvedValueOnce({ valid: false, reason: '邀请码已过期' });
    const wrapper = await mountPage();
    await flushPromises();
    await nextTick();
    expect(wrapper.html()).toContain('邀请无效');
  });

  it('redirects logged-in users', async () => {
    mockToken = 'some-token';
    await mountPage();
    await flushPromises();
    expect(mockReplace).toHaveBeenCalledWith('/');
  });
});
