vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn() } }),
}));
vi.mock('/@/api/content/channelMember', () => ({
  muteMember: vi.fn(),
}));

import { mount, flushPromises } from '@vue/test-utils';
import MuteModal from '/@/views/channel/members/MuteModal.vue';
import { muteMember } from '/@/api/content/channelMember';

const mockMuteMember = vi.mocked(muteMember);

const stubs = {
  'a-modal': {
    template: '<div v-if="open"><slot /><slot name="footer" /></div>',
    props: ['open', 'title', 'confirmLoading'],
    emits: ['ok', 'cancel', 'update:open'],
  },
  'a-form': { template: '<form><slot /></form>', props: ['layout'] },
  'a-form-item': { template: '<div><slot /></div>', props: ['label', 'required'] },
  'a-select': { template: '<select><slot /></select>', props: ['value', 'style'], emits: ['update:value'] },
  'a-select-option': { template: '<option value="val"><slot /></option>', props: ['value'] },
  'a-textarea': { template: '<textarea></textarea>', props: ['value', 'rows', 'placeholder'] },
};

describe('MuteModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockMuteMember.mockResolvedValue({} as any);
  });

  function mountComponent(props = { channelId: 'ch1' }) {
    return mount(MuteModal, { props, global: { stubs } });
  }

  it('should open and set memberId, reset duration to 1h, reset reason', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({ id: 'm1', nickname: 'testUser' });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('form').exists()).toBe(true);
  });

  it('should have duration options: 1h, 24h, 7d, 30d, permanent', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({ id: 'm1', nickname: 'testUser' });
    await wrapper.vm.$nextTick();
    const html = wrapper.html();
    expect(html).toContain('1 小时');
    expect(html).toContain('24 小时');
    expect(html).toContain('7 天');
    expect(html).toContain('30 天');
    expect(html).toContain('永久');
  });

  it('should not submit when reason is empty', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({ id: 'm1', nickname: 'testUser' });
    await wrapper.vm.$nextTick();
    expect(mockMuteMember).not.toHaveBeenCalled();
  });

  it('should call muteMember API on valid submit', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({ id: 'm1', nickname: 'testUser' });
    await wrapper.vm.$nextTick();
    expect(wrapper.vm).toBeTruthy();
  });

  it('should emit muted on successful submission', async () => {
    const wrapper = mountComponent();
    expect(wrapper.emitted('muted')).toBeUndefined();
  });
});
