<template>
  <div class="chat-panel">
    <!-- 连接状态横幅 -->
    <div v-if="!store.wsConnected" class="connection-banner" :class="{ reconnecting: store.reconnecting }">
      <div v-if="store.reconnecting" class="banner-warning">连接已断开，正在重连...</div>
      <div v-else class="banner-error">连接已断开，请刷新页面</div>
    </div>

    <!-- 头部 -->
    <div class="chat-header">
      <span class="session-type">{{ sessionTypeLabel }}</span>
      <span v-if="store.queuePosition != null" class="queue-info">
        排队中 (第 {{ store.queuePosition }} 位)
      </span>
      <div class="header-actions">
        <a-button data-testid="close-btn" size="small" @click="handleClose">结束会话</a-button>
      </div>
    </div>

    <!-- 消息区域 -->
    <div ref="messagesRef" class="chat-messages">
      <ChatMessage
        v-for="msg in displayMessages"
        :key="msg.id"
        :message="msg"
        @retry="handleRetry(msg.id)"
      />
    </div>

    <!-- 快捷回复 -->
    <div v-if="isBotSession" data-testid="quick-replies" class="quick-replies">
      <a-button
        v-for="reply in quickReplies"
        :key="reply"
        size="small"
        @click="handleSend(reply)"
      >
        {{ reply }}
      </a-button>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <a-textarea
        v-model:value="inputText"
        placeholder="输入消息..."
        :auto-size="{ minRows: 1, maxRows: 4 }"
        @press-enter="handleEnter"
      />
      <div class="input-actions">
        <a-button
          v-if="isBotSession"
          data-testid="transfer-btn"
          @click="handleTransfer"
        >
          转人工
        </a-button>
        <a-button
          data-testid="send-btn"
          type="primary"
          :disabled="!inputText.trim()"
          @click="handleSend(inputText)"
        >
          发送
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { Modal, message } from 'ant-design-vue';
import ChatMessage from './ChatMessage.vue';
import type { ChatMessageData } from './ChatMessage.vue';
import { useFeedbackStore } from '/@/store/modules/feedback';
import {
  createServiceSession,
  transferToHuman,
  sendChatMessage,
  closeServiceSession,
} from '/@/api/support/customer-service';

const store = useFeedbackStore();
const inputText = ref('');
const messagesRef = ref<HTMLElement | null>(null);

const quickReplies = ['常见问题', '转人工客服', '意见反馈'];

const isBotSession = computed(() => store.currentSession?.type === 'bot' || store.currentSession?.status === 'bot');

const sessionTypeLabel = computed(() => {
  if (!store.currentSession) return '客服';
  return store.currentSession.type === 'bot' || store.currentSession.status === 'bot'
    ? '智能客服'
    : '人工客服';
});

// 将 store 的 ChatMessage 转换为 ChatMessageData
const displayMessages = computed<ChatMessageData[]>(() =>
  store.chatMessages.map((msg) => ({
    id: msg.id,
    content: msg.content,
    type: (msg.messageType as ChatMessageData['type']) || 'text',
    role: mapSenderType(msg.senderType),
    status: (msg.status as ChatMessageData['status']) || 'sent',
    timestamp: msg.createTime,
  }))
);

function mapSenderType(senderType: string): ChatMessageData['role'] {
  if (senderType === 'user') return 'user';
  if (senderType === 'bot' || senderType === 'agent') return 'agent';
  if (senderType === 'system') return 'system';
  return 'agent';
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
    }
  });
}

// 自动滚动
watch(() => store.chatMessages.length, scrollToBottom);

let isUnmounted = false;
onMounted(async () => {
  try {
    const res = await createServiceSession();
    if (isUnmounted) return;
    store.currentSession = res;
    store.wsConnected = true;
  } catch {
    if (!isUnmounted) message.error('创建会话失败');
  }
});

onUnmounted(() => {
  isUnmounted = true;
});

async function handleSend(text: string) {
  const content = text.trim();
  if (!content || !store.currentSession) return;

  const msgId = `msg-${Date.now()}`;
  store.addMessage({
    id: msgId,
    sessionId: store.currentSession.id,
    senderType: 'user',
    content,
    messageType: 'text',
    status: 'sending',
    createTime: new Date().toLocaleString(),
  });
  inputText.value = '';
  scrollToBottom();

  try {
    await sendChatMessage(store.currentSession.id, { content, messageType: 'text' });
    store.updateMessageStatus(msgId, 'sent');
  } catch {
    store.updateMessageStatus(msgId, 'failed');
  }
}

function handleEnter(e: KeyboardEvent) {
  if (!e.shiftKey) {
    e.preventDefault();
    handleSend(inputText.value);
  }
}

function handleRetry(msgId: string) {
  const msg = store.chatMessages.find((m) => m.id === msgId);
  if (msg && store.currentSession) {
    store.updateMessageStatus(msgId, 'sending');
    sendChatMessage(store.currentSession.id, { content: msg.content, messageType: msg.messageType })
      .then(() => store.updateMessageStatus(msgId, 'sent'))
      .catch(() => store.updateMessageStatus(msgId, 'failed'));
  }
}

async function handleTransfer() {
  if (!store.currentSession) return;
  try {
    const res = await transferToHuman(store.currentSession.id);
    if (res?.queuePosition != null) {
      store.queuePosition = res.queuePosition;
    }
    store.currentSession.status = 'queuing';
    store.addMessage({
      id: `sys-${Date.now()}`,
      sessionId: store.currentSession.id,
      senderType: 'system',
      content: '正在为您转接人工客服...',
      messageType: 'text',
      status: 'sent',
      createTime: new Date().toLocaleString(),
    });
  } catch {
    message.error('转接失败，请稍后重试');
  }
}

function handleClose() {
  Modal.confirm({
    title: '确认结束会话？',
    content: '结束后将无法继续对话',
    onOk: async () => {
      if (store.currentSession) {
        try {
          await closeServiceSession(store.currentSession.id);
        } catch {
          // ignore
        }
      }
      store.clearSession();
    },
  });
}
</script>

<style scoped lang="less">
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.connection-banner {
  flex-shrink: 0;

  .banner-warning {
    padding: 8px 16px;
    background: #fffbe6;
    border-bottom: 1px solid #ffe58f;
    color: #d48806;
    font-size: 13px;
    text-align: center;
  }

  .banner-error {
    padding: 8px 16px;
    background: #fff2f0;
    border-bottom: 1px solid #ffccc7;
    color: #cf1322;
    font-size: 13px;
    text-align: center;
  }
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;

  .session-type {
    font-weight: 600;
    font-size: 15px;
    color: #333;
  }

  .queue-info {
    margin-left: 12px;
    font-size: 13px;
    color: #fa8c16;
  }

  .header-actions {
    margin-left: auto;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  min-height: 0;
}

.quick-replies {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;

  .input-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
  }
}
</style>
