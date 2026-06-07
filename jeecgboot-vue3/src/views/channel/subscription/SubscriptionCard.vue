<!-- jeecgboot-vue3/src/views/channel/subscription/SubscriptionCard.vue -->
<template>
  <div class="subscription-card">
    <div class="card-main">
      <Avatar :src="channel.avatar" :size="48" />
      <div class="card-info">
        <div class="card-name">
          {{ channel.name }}
          <Tag v-if="channel.isSystem" color="blue">系统推荐</Tag>
          <Tag v-if="channel.source" color="default">{{ channel.source }}</Tag>
        </div>
        <div class="card-summary">{{ channel.latestSummary }}</div>
      </div>
    </div>
    <div class="card-actions">
      <Switch :checked="channel.reminderEnabled" size="small" @change="handleReminderChange" />
      <Button type="link" size="small" danger @click="handleUnsubscribe">取消订阅</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Avatar, Tag, Switch, Button } from 'ant-design-vue';

  const props = defineProps<{
    channel: {
      id: string;
      name: string;
      avatar: string;
      latestSummary: string;
      source?: string;
      isSystem: boolean;
      reminderEnabled: boolean;
    };
  }>();

  const emit = defineEmits<{
    (e: 'toggleReminder', channelId: string, enabled: boolean): void;
    (e: 'unsubscribe', channelId: string): void;
  }>();

  function handleReminderChange(checked: boolean) {
    emit('toggleReminder', props.channel.id, checked);
  }

  function handleUnsubscribe() {
    emit('unsubscribe', props.channel.id);
  }
</script>

<style scoped>
.subscription-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 8px;
}
.card-main { display: flex; align-items: center; gap: 12px; flex: 1; }
.card-info { flex: 1; }
.card-name { font-weight: 500; }
.card-summary { color: #999; font-size: 13px; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-actions { display: flex; align-items: center; gap: 8px; }
</style>
