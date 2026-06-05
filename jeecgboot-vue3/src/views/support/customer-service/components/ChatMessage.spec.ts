import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ChatMessage from './ChatMessage.vue';

// Stub ant-design-vue icons so they don't require runtime icon resolution
vi.mock('@ant-design/icons-vue', () => ({
  LoadingOutlined: { template: '<span class="loading-icon" />' },
  ExclamationCircleOutlined: { template: '<span class="exclamation-icon" />' },
}));

const globalStubs = {
  'a-avatar': { template: '<span class="avatar"><slot /></span>', props: ['style'] },
  'a-divider': { template: '<hr />', props: ['style'] },
  'a-image': { template: '<img />', props: ['src', 'width'] },
};

describe('ChatMessage', () => {
  const userMessage = {
    id: '1',
    content: '你好，我想咨询一个问题',
    type: 'text' as const,
    role: 'user' as const,
    status: 'sent' as const,
    timestamp: '2026-06-05 10:30:00',
  };

  const agentMessage = {
    id: '2',
    content: '您好，请问有什么可以帮助您？',
    type: 'text' as const,
    role: 'agent' as const,
    status: 'sent' as const,
    timestamp: '2026-06-05 10:31:00',
  };

  const systemMessage = {
    id: '3',
    content: '人工客服已接入',
    type: 'text' as const,
    role: 'system' as const,
    status: 'sent' as const,
    timestamp: '2026-06-05 10:32:00',
  };

  it('should render user message on right side', () => {
    const wrapper = mount(ChatMessage, { props: { message: userMessage }, global: { stubs: globalStubs } });
    expect(wrapper.classes()).toContain('message-user');
  });

  it('should render agent message on left side', () => {
    const wrapper = mount(ChatMessage, { props: { message: agentMessage }, global: { stubs: globalStubs } });
    expect(wrapper.classes()).toContain('message-agent');
  });

  it('should render system message centered', () => {
    const wrapper = mount(ChatMessage, { props: { message: systemMessage }, global: { stubs: globalStubs } });
    expect(wrapper.classes()).toContain('message-system');
  });

  it('should show sending state with loading', () => {
    const sending = { ...userMessage, status: 'sending' as const };
    const wrapper = mount(ChatMessage, { props: { message: sending }, global: { stubs: globalStubs } });
    expect(wrapper.find('.sending-indicator').exists()).toBe(true);
  });

  it('should show failed state with retry button', () => {
    const failed = { ...userMessage, status: 'failed' as const };
    const wrapper = mount(ChatMessage, { props: { message: failed }, global: { stubs: globalStubs } });
    expect(wrapper.find('.retry-btn').exists()).toBe(true);
  });

  it('should emit retry event when retry clicked', async () => {
    const failed = { ...userMessage, status: 'failed' as const };
    const wrapper = mount(ChatMessage, { props: { message: failed }, global: { stubs: globalStubs } });
    await wrapper.find('.retry-btn').trigger('click');
    expect(wrapper.emitted('retry')).toBeTruthy();
  });

  it('should render image message with a-image', () => {
    const imageMessage = {
      id: '4',
      content: 'https://example.com/photo.jpg',
      type: 'image' as const,
      role: 'user' as const,
      status: 'sent' as const,
      timestamp: '2026-06-05 10:33:00',
    };
    const wrapper = mount(ChatMessage, { props: { message: imageMessage }, global: { stubs: globalStubs } });
    expect(wrapper.find('.image-content').exists()).toBe(true);
    expect(wrapper.find('img').exists()).toBe(true);
  });

  it('should render link with safeHref for https URL', () => {
    const linkMessage = {
      id: '5',
      content: 'https://example.com/article',
      type: 'link' as const,
      role: 'agent' as const,
      status: 'sent' as const,
      timestamp: '2026-06-05 10:34:00',
    };
    const wrapper = mount(ChatMessage, { props: { message: linkMessage }, global: { stubs: globalStubs } });
    const link = wrapper.find('.link-content a');
    expect(link.exists()).toBe(true);
    expect(link.attributes('href')).toBe('https://example.com/article');
  });

  it('should sanitize non-https link to #', () => {
    const linkMessage = {
      id: '6',
      content: 'javascript:alert(1)',
      type: 'link' as const,
      role: 'agent' as const,
      status: 'sent' as const,
      timestamp: '2026-06-05 10:35:00',
    };
    const wrapper = mount(ChatMessage, { props: { message: linkMessage }, global: { stubs: globalStubs } });
    const link = wrapper.find('.link-content a');
    expect(link.attributes('href')).toBe('#');
  });

  it('should render agent avatar for agent role', () => {
    const wrapper = mount(ChatMessage, { props: { message: agentMessage }, global: { stubs: globalStubs } });
    expect(wrapper.find('.avatar-left').exists()).toBe(true);
  });

  it('should render user avatar for user role', () => {
    const wrapper = mount(ChatMessage, { props: { message: userMessage }, global: { stubs: globalStubs } });
    expect(wrapper.find('.avatar-right').exists()).toBe(true);
  });
});
