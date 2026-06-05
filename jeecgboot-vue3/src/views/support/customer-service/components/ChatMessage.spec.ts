import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ChatMessage from './ChatMessage.vue';

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
    const wrapper = mount(ChatMessage, { props: { message: userMessage } });
    expect(wrapper.classes()).toContain('message-user');
  });

  it('should render agent message on left side', () => {
    const wrapper = mount(ChatMessage, { props: { message: agentMessage } });
    expect(wrapper.classes()).toContain('message-agent');
  });

  it('should render system message centered', () => {
    const wrapper = mount(ChatMessage, { props: { message: systemMessage } });
    expect(wrapper.classes()).toContain('message-system');
  });

  it('should show sending state with loading', () => {
    const sending = { ...userMessage, status: 'sending' as const };
    const wrapper = mount(ChatMessage, { props: { message: sending } });
    expect(wrapper.find('.sending-indicator').exists()).toBe(true);
  });

  it('should show failed state with retry button', () => {
    const failed = { ...userMessage, status: 'failed' as const };
    const wrapper = mount(ChatMessage, { props: { message: failed } });
    expect(wrapper.find('.retry-btn').exists()).toBe(true);
  });

  it('should emit retry event when retry clicked', async () => {
    const failed = { ...userMessage, status: 'failed' as const };
    const wrapper = mount(ChatMessage, { props: { message: failed } });
    await wrapper.find('.retry-btn').trigger('click');
    expect(wrapper.emitted('retry')).toBeTruthy();
  });
});
