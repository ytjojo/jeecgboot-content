import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useFeedbackStore } from '/@/store/modules/feedback';

// Mock defHttp
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('useFeedbackStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should have correct initial state', () => {
    const store = useFeedbackStore();
    expect(store.reportList).toEqual([]);
    expect(store.reportTotal).toBe(0);
    expect(store.appealList).toEqual([]);
    expect(store.appealTotal).toBe(0);
    expect(store.currentSession).toBeNull();
    expect(store.chatMessages).toEqual([]);
    expect(store.queuePosition).toBeNull();
    expect(store.wsConnected).toBe(false);
    expect(store.reconnecting).toBe(false);
    expect(store.pendingReportCount).toBe(0);
    expect(store.pendingAppealCount).toBe(0);
  });

  it('should update chatMessages when addMessage is called', () => {
    const store = useFeedbackStore();
    const msg = { id: '1', content: 'hello', senderType: 'user', status: 'sent' };
    store.addMessage(msg as any);
    expect(store.chatMessages).toHaveLength(1);
    expect(store.chatMessages[0].content).toBe('hello');
  });

  it('should update message status', () => {
    const store = useFeedbackStore();
    store.addMessage({ id: '1', content: 'hello', senderType: 'user', status: 'sending' } as any);
    store.updateMessageStatus('1', 'sent');
    expect(store.chatMessages[0].status).toBe('sent');
  });

  it('should clear session state', () => {
    const store = useFeedbackStore();
    store.currentSession = { id: '1' } as any;
    store.chatMessages = [{ id: '1' }] as any;
    store.queuePosition = 3;
    store.clearSession();
    expect(store.currentSession).toBeNull();
    expect(store.chatMessages).toEqual([]);
    expect(store.queuePosition).toBeNull();
  });
});
