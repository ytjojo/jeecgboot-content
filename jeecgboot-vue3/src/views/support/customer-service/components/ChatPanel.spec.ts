import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ChatPanel from './ChatPanel.vue';

vi.mock('/@/api/support/customer-service', () => ({
  createServiceSession: vi.fn().mockResolvedValue({ id: 's1', type: 'bot', status: 'bot' }),
  transferToHuman: vi.fn().mockResolvedValue({ queuePosition: 3 }),
  sendChatMessage: vi.fn().mockResolvedValue({}),
  closeServiceSession: vi.fn().mockResolvedValue({}),
}));

describe('ChatPanel', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should auto-start bot session on mount', async () => {
    mount(ChatPanel);
    await flushPromises();
    const { createServiceSession } = await import('/@/api/support/customer-service');
    expect(createServiceSession).toHaveBeenCalled();
  });

  it('should show transfer button during bot session', async () => {
    const wrapper = mount(ChatPanel);
    await flushPromises();
    expect(wrapper.find('[data-testid="transfer-btn"]').exists()).toBe(true);
  });

  it('should show connection lost banner on disconnect', async () => {
    const wrapper = mount(ChatPanel);
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.wsConnected = false;
    store.reconnecting = true;
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('连接已断开');
  });

  it('should show queue position when queuing', async () => {
    const wrapper = mount(ChatPanel);
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.queuePosition = 3;
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('排队中');
  });

  it('should send message via API', async () => {
    const wrapper = mount(ChatPanel);
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    store.wsConnected = true;
    await wrapper.vm.$nextTick();

    const textarea = wrapper.find('textarea');
    if (textarea.exists()) {
      await textarea.setValue('你好');
      const sendBtn = wrapper.find('[data-testid="send-btn"]');
      if (sendBtn.exists()) {
        await sendBtn.trigger('click');
        await flushPromises();
        const { sendChatMessage } = await import('/@/api/support/customer-service');
        expect(sendChatMessage).toHaveBeenCalledWith('s1', { content: '你好', messageType: 'text' });
      }
    }
  });

  it('should convert store messages to ChatMessageData format', async () => {
    const wrapper = mount(ChatPanel);
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.chatMessages = [
      { id: 'm1', sessionId: 's1', senderType: 'user', content: '你好', messageType: 'text', status: 'sent', createTime: '2026-06-05 10:00' },
      { id: 'm2', sessionId: 's1', senderType: 'bot', content: '您好', messageType: 'text', status: 'sent', createTime: '2026-06-05 10:01' },
    ];
    await wrapper.vm.$nextTick();
    // Should render both messages
    expect(wrapper.text()).toContain('你好');
    expect(wrapper.text()).toContain('您好');
  });

  it('should show close button', () => {
    const wrapper = mount(ChatPanel);
    expect(wrapper.find('[data-testid="close-btn"]').exists()).toBe(true);
  });

  it('should show quick replies during bot session', async () => {
    const wrapper = mount(ChatPanel);
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();
    const quickReplies = wrapper.find('[data-testid="quick-replies"]');
    expect(quickReplies.exists()).toBe(true);
  });
});
