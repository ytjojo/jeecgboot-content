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

vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    Modal: {
      confirm: vi.fn(({ onOk }) => { onOk?.(); }),
    },
    message: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn(),
    },
  };
});

const globalStubs = {
  'a-textarea': {
    template: '<textarea :value="value" @input="$emit(\'update:value\', $event.target.value)" />',
    props: ['value', 'autoSize', 'placeholder'],
    emits: ['update:value'],
  },
  'a-button': {
    template: '<button :disabled="disabled" :data-testid="$attrs[\'data-testid\']"><slot /></button>',
    props: ['type', 'size', 'disabled'],
    inheritAttrs: false,
  },
  'a-avatar': { template: '<span><slot /></span>', props: ['style'] },
  'a-divider': { template: '<hr />' },
  'a-image': { template: '<img />', props: ['src', 'width'] },
  'a-badge': { template: '<div><slot /></div>', props: ['count', 'offset'] },
};

describe('ChatPanel', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should auto-start bot session on mount', async () => {
    mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { createServiceSession } = await import('/@/api/support/customer-service');
    expect(createServiceSession).toHaveBeenCalled();
  });

  it('should show transfer button during bot session', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    expect(wrapper.find('[data-testid="transfer-btn"]').exists()).toBe(true);
  });

  it('should show connection lost banner on disconnect', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.wsConnected = false;
    store.reconnecting = true;
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('连接已断开');
  });

  it('should show queue position when queuing', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.queuePosition = 3;
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('排队中');
  });

  it('should send message via API', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    store.wsConnected = true;
    await wrapper.vm.$nextTick();

    // Call handleSend directly since stub v-model doesn't propagate reliably
    await (wrapper.vm as any).handleSend('你好');
    await flushPromises();
    const { sendChatMessage } = await import('/@/api/support/customer-service');
    expect(sendChatMessage).toHaveBeenCalledWith('s1', { content: '你好', messageType: 'text' });
  });

  it('should convert store messages to ChatMessageData format', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
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
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    expect(wrapper.find('[data-testid="close-btn"]').exists()).toBe(true);
  });

  it('should show quick replies during bot session', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();
    const quickReplies = wrapper.find('[data-testid="quick-replies"]');
    expect(quickReplies.exists()).toBe(true);
  });

  it('should call transferToHuman when handleTransfer called', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();

    await (wrapper.vm as any).handleTransfer();
    await flushPromises();
    const { transferToHuman } = await import('/@/api/support/customer-service');
    expect(transferToHuman).toHaveBeenCalledWith('s1');
    expect(store.queuePosition).toBe(3);
    expect(store.currentSession.status).toBe('queuing');
  });

  it('should call Modal.confirm when handleClose called', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { Modal } = await import('ant-design-vue');
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();

    (wrapper.vm as any).handleClose();
    expect(Modal.confirm).toHaveBeenCalled();
  });

  it('should retry failed message via sendChatMessage', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    store.chatMessages = [
      { id: 'm1', sessionId: 's1', senderType: 'user', content: '失败消息', messageType: 'text', status: 'failed', createTime: '2026-06-05 10:00' },
    ];
    await wrapper.vm.$nextTick();

    await (wrapper.vm as any).handleRetry('m1');
    await flushPromises();
    const { sendChatMessage } = await import('/@/api/support/customer-service');
    expect(sendChatMessage).toHaveBeenCalledWith('s1', { content: '失败消息', messageType: 'text' });
    expect(store.chatMessages[0].status).toBe('sent');
  });

  it('should send message on Enter (non-shift)', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();

    const preventDefault = vi.fn();
    (wrapper.vm as any).inputText = '测试消息';
    (wrapper.vm as any).handleEnter({ shiftKey: false, preventDefault });
    await flushPromises();
    expect(preventDefault).toHaveBeenCalled();
    const { sendChatMessage } = await import('/@/api/support/customer-service');
    expect(sendChatMessage).toHaveBeenCalledWith('s1', { content: '测试消息', messageType: 'text' });
  });

  it('should not send message on shift+Enter', async () => {
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { sendChatMessage } = await import('/@/api/support/customer-service');
    vi.mocked(sendChatMessage).mockClear();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();

    const preventDefault = vi.fn();
    (wrapper.vm as any).inputText = '换行消息';
    (wrapper.vm as any).handleEnter({ shiftKey: true, preventDefault });
    await flushPromises();
    expect(preventDefault).not.toHaveBeenCalled();
    expect(sendChatMessage).not.toHaveBeenCalled();
  });

  it('should set isUnmounted flag on component unmount', async () => {
    // Verify that unmounting prevents error message after async createServiceSession
    const { createServiceSession } = await import('/@/api/support/customer-service');
    const { message } = await import('ant-design-vue');
    // Create a deferred promise to control when createServiceSession resolves
    let resolveSession: (v: any) => void;
    vi.mocked(createServiceSession).mockReturnValueOnce(
      new Promise((resolve) => {
        resolveSession = resolve;
      })
    );
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    // Unmount before session creation completes — sets isUnmounted = true
    wrapper.unmount();
    // Now resolve with an error to trigger the catch path
    resolveSession!(Promise.reject(new Error('late error')));
    await flushPromises();
    // Because isUnmounted is true, message.error should NOT be called
    expect(message.error).not.toHaveBeenCalled();
  });

  it('should show error message when createServiceSession fails', async () => {
    const { createServiceSession } = await import('/@/api/support/customer-service');
    const { message } = await import('ant-design-vue');
    vi.mocked(createServiceSession).mockRejectedValueOnce(new Error('network error'));
    mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    expect(message.error).toHaveBeenCalledWith('创建会话失败');
  });

  it('should mark message as failed when sendChatMessage rejects', async () => {
    const { sendChatMessage } = await import('/@/api/support/customer-service');
    vi.mocked(sendChatMessage).mockRejectedValueOnce(new Error('send failed'));
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();

    await (wrapper.vm as any).handleSend('测试失败');
    await flushPromises();
    const failedMsg = store.chatMessages.find((m) => m.content === '测试失败');
    expect(failedMsg?.status).toBe('failed');
  });

  it('should mark message as failed when retry sendChatMessage rejects', async () => {
    const { sendChatMessage } = await import('/@/api/support/customer-service');
    vi.mocked(sendChatMessage).mockRejectedValueOnce(new Error('retry failed'));
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    store.chatMessages = [
      { id: 'm1', sessionId: 's1', senderType: 'user', content: '重试消息', messageType: 'text', status: 'failed', createTime: '2026-06-05 10:00' },
    ];
    await wrapper.vm.$nextTick();

    await (wrapper.vm as any).handleRetry('m1');
    await flushPromises();
    expect(store.chatMessages[0].status).toBe('failed');
  });

  it('should show error when transferToHuman fails', async () => {
    const { transferToHuman } = await import('/@/api/support/customer-service');
    const { message } = await import('ant-design-vue');
    vi.mocked(transferToHuman).mockRejectedValueOnce(new Error('transfer failed'));
    const wrapper = mount(ChatPanel, { global: { stubs: globalStubs } });
    await flushPromises();
    const { useFeedbackStore } = await import('/@/store/modules/feedback');
    const store = useFeedbackStore();
    store.currentSession = { id: 's1', type: 'bot', status: 'bot', agentName: '', queuePosition: null, estimatedWaitTime: null, createTime: '2026-06-05' };
    await wrapper.vm.$nextTick();

    await (wrapper.vm as any).handleTransfer();
    await flushPromises();
    expect(message.error).toHaveBeenCalledWith('转接失败，请稍后重试');
  });
});
