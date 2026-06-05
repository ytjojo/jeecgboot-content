import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';

interface ReportItem {
  id: string;
  reportNo: string;
  targetType: string;
  targetId: string;
  targetSummary: string;
  reportType: string;
  reportTypeLabel: string;
  description: string;
  evidenceUrls: string[];
  status: string;
  statusLabel: string;
  result: string;
  createTime: string;
}

interface AppealItem {
  id: string;
  appealNo: string;
  appealType: string;
  appealTypeLabel: string;
  relatedId: string;
  relatedSummary: string;
  reason: string;
  attachmentUrls: string[];
  status: string;
  statusLabel: string;
  auditResult: string;
  auditTime: string;
  appealCount: number;
  maxAppealCount: number;
  estimatedTime: string;
  createTime: string;
}

interface ServiceSession {
  id: string;
  type: string;
  status: string;
  agentName: string;
  queuePosition: number | null;
  estimatedWaitTime: number | null;
  rating?: number;
  ratingComment?: string;
  startTime?: string;
  endTime?: string;
  expired?: boolean;
  createTime: string;
}

interface ChatMessage {
  id: string;
  sessionId: string;
  senderType: string;
  content: string;
  messageType: string;
  status: string;
  createTime: string;
}

export const useFeedbackStore = defineStore('feedback', () => {
  // ===== Report State =====
  const reportList = ref<ReportItem[]>([]);
  const reportTotal = ref(0);
  const pendingReportCount = ref(0);

  // ===== Appeal State =====
  const appealList = ref<AppealItem[]>([]);
  const appealTotal = ref(0);
  const pendingAppealCount = ref(0);

  // ===== Customer Service State =====
  const currentSession = ref<ServiceSession | null>(null);
  const chatMessages = ref<ChatMessage[]>([]);
  const queuePosition = ref<number | null>(null);
  const wsConnected = ref(false);
  const reconnecting = ref(false);

  // ===== Chat Methods =====
  function addMessage(msg: ChatMessage) {
    chatMessages.value.push(msg);
  }

  function updateMessageStatus(id: string, status: string) {
    const msg = chatMessages.value.find((m) => m.id === id);
    if (msg) {
      msg.status = status;
    }
  }

  function clearSession() {
    currentSession.value = null;
    chatMessages.value = [];
    queuePosition.value = null;
    wsConnected.value = false;
    reconnecting.value = false;
  }

  return {
    // Report
    reportList,
    reportTotal,
    pendingReportCount,
    // Appeal
    appealList,
    appealTotal,
    pendingAppealCount,
    // Customer Service
    currentSession,
    chatMessages,
    queuePosition,
    wsConnected,
    reconnecting,
    // Methods
    addMessage,
    updateMessageStatus,
    clearSession,
  };
});

// Support use outside of setup
export function useFeedbackStoreWithOut() {
  return useFeedbackStore(store);
}
