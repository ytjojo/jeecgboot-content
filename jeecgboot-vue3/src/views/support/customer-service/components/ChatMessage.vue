<template>
  <div :class="['message-wrapper', `message-${message.role}`]">
    <!-- 系统消息居中 -->
    <div v-if="message.role === 'system'" class="system-message">
      <a-divider />
      <span class="system-text">{{ message.content }}</span>
      <a-divider />
    </div>

    <!-- 用户/客服消息气泡 -->
    <template v-else>
      <div v-if="message.role === 'agent'" class="avatar-left">
        <a-avatar style="background-color: #1890ff">客服</a-avatar>
      </div>
      <div class="bubble-container">
        <div :class="['bubble', `bubble-${message.role}`, { 'bubble-sending': message.status === 'sending' }]">
          <div v-if="message.type === 'text'" class="text-content">{{ message.content }}</div>
          <div v-else-if="message.type === 'image'" class="image-content">
            <a-image :src="message.content" :width="200" />
          </div>
          <div v-else-if="message.type === 'link'" class="link-content">
            <a :href="safeHref" target="_blank">{{ message.content }}</a>
          </div>
        </div>
        <div class="message-meta">
          <span class="timestamp">{{ message.timestamp }}</span>
          <span v-if="message.status === 'sending'" class="sending-indicator">
            <loading-outlined spin />
          </span>
          <span v-if="message.status === 'failed'" class="failed-indicator">
            <exclamation-circle-outlined style="color: #ff4d4f" />
            <a class="retry-btn" @click="$emit('retry')">重试</a>
          </span>
        </div>
      </div>
      <div v-if="message.role === 'user'" class="avatar-right">
        <a-avatar style="background-color: #52c41a">我</a-avatar>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { LoadingOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue';

export interface ChatMessageData {
  id: string;
  content: string;
  type: 'text' | 'image' | 'link';
  role: 'user' | 'agent' | 'system';
  status: 'sending' | 'sent' | 'failed';
  timestamp: string;
}

const props = defineProps<{
  message: ChatMessageData;
}>();

const safeHref = computed(() => {
  const url = props.message.content;
  if (/^https?:\/\//i.test(url)) return url;
  return '#';
});

defineEmits<{
  (e: 'retry'): void;
}>();
</script>

<style scoped lang="less">
.message-wrapper {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  padding: 0 16px;

  &.message-user {
    flex-direction: row-reverse;
  }

  &.message-agent {
    flex-direction: row;
  }

  &.message-system {
    justify-content: center;
  }
}

.system-message {
  text-align: center;
  width: 100%;

  .system-text {
    color: #999;
    font-size: 12px;
    padding: 0 12px;
  }
}

.avatar-left,
.avatar-right {
  flex-shrink: 0;
  margin: 0 8px;
}

.bubble-container {
  max-width: 70%;

  .bubble {
    padding: 10px 14px;
    border-radius: 8px;
    word-break: break-word;
    line-height: 1.6;

    &.bubble-user {
      background: #1890ff;
      color: #fff;
      border-top-right-radius: 2px;
    }

    &.bubble-agent {
      background: #f5f5f5;
      color: #333;
      border-top-left-radius: 2px;
    }

    &.bubble-sending {
      opacity: 0.6;
    }
  }

  .message-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
    font-size: 12px;
    color: #999;

    .message-user & {
      flex-direction: row-reverse;
    }

    .retry-btn {
      color: #1890ff;
      cursor: pointer;
    }
  }
}

@media (max-width: 768px) {
  .bubble-container {
    max-width: 85%;
  }
}
</style>
