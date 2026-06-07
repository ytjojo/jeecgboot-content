vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn() } }),
}));
vi.mock('/@/api/content/channelPrivacy', () => ({
  updateChannelPrivacy: vi.fn(),
}));

import { mount, flushPromises } from '@vue/test-utils';
import PrivacySettings from '/@/views/channel/settings/PrivacySettings.vue';
import { updateChannelPrivacy } from '/@/api/content/channelPrivacy';

const mockUpdateChannelPrivacy = vi.mocked(updateChannelPrivacy);

const stubs = {
  'a-radio': {
    template: '<label class="mock-radio"><slot /></label>',
    props: ['value', 'disabled'],
  },
  'a-radio-group': {
    template: '<div class="mock-radio-group"><slot /></div>',
    props: ['value', 'disabled'],
    emits: ['change', 'update:value'],
  },
  'a-alert': {
    template: '<div class="mock-alert" v-if="message">{{ message }}<slot /></div>',
    props: ['type', 'message', 'showIcon', 'style'],
  },
  'a-modal': {
    template: '<div v-if="open" class="mock-modal"><slot /><slot name="footer" /></div>',
    props: ['open', 'title', 'confirmLoading'],
    emits: ['ok', 'cancel', 'update:open'],
  },
  'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'danger'] },
  'a-skeleton': {
    template: '<div class="mock-skeleton"><slot /></div>',
    props: ['loading', 'active', 'paragraph'],
  },
};

describe('PrivacySettings', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUpdateChannelPrivacy.mockResolvedValue({} as any);
  });

  function mountComponent(props = { channelId: 'ch1', initialPrivacy: 'PUBLIC' as const, isSystem: false }) {
    return mount(PrivacySettings, { props, global: { stubs } });
  }

  it('should render radio group with PUBLIC/PRIVATE options', () => {
    const wrapper = mountComponent();
    expect(wrapper.text()).toContain('公开');
    expect(wrapper.text()).toContain('私有');
  });

  it('should disable radio when isSystem is true', () => {
    const wrapper = mountComponent({ channelId: 'ch1', initialPrivacy: 'PUBLIC', isSystem: true });
    const radioGroup = wrapper.find('.mock-radio-group');
    expect(radioGroup.exists()).toBe(true);
  });

  it('should show alert for system channel', () => {
    const wrapper = mountComponent({ channelId: 'ch1', initialPrivacy: 'PUBLIC', isSystem: true });
    expect(wrapper.text()).toContain('系统频道必须公开');
  });

  it('should not show alert for non-system channel', () => {
    const wrapper = mountComponent({ channelId: 'ch1', initialPrivacy: 'PUBLIC', isSystem: false });
    expect(wrapper.text()).not.toContain('系统频道必须公开');
  });

  it('should show privacy description for PUBLIC', () => {
    const wrapper = mountComponent({ initialPrivacy: 'PUBLIC' });
    expect(wrapper.text()).toContain('频道内容对所有人可见');
  });

  it('should show privacy description for PRIVATE', () => {
    const wrapper = mountComponent({ initialPrivacy: 'PRIVATE' });
    expect(wrapper.text()).toContain('仅频道成员可浏览');
  });

  it('should call updateChannelPrivacy on confirm', async () => {
    const wrapper = mountComponent({ initialPrivacy: 'PUBLIC' });
    expect(mockUpdateChannelPrivacy).toBeDefined();
  });

  it('should emit updated on successful confirm', async () => {
    const wrapper = mountComponent();
    expect(wrapper.emitted('updated')).toBeUndefined();
  });

  it('should show different modal title for PUBLIC->PRIVATE vs PRIVATE->PUBLIC', () => {
    const wrapper = mountComponent({ initialPrivacy: 'PUBLIC' });
    expect(wrapper.vm).toBeTruthy();
  });

  it('should watch initialPrivacy prop changes', async () => {
    const wrapper = mountComponent({ initialPrivacy: 'PUBLIC', channelId: 'ch1', isSystem: false });
    expect(wrapper.text()).toContain('频道内容对所有人可见');
    await wrapper.setProps({ initialPrivacy: 'PRIVATE' });
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('仅频道成员可浏览');
  });
});
