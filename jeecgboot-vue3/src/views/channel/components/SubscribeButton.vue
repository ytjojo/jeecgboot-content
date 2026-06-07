<!-- jeecgboot-vue3/src/views/channel/components/SubscribeButton.vue -->
<template>
  <div class="subscribe-button-wrapper">
    <!-- 未订阅公开频道 -->
    <Button v-if="state === 'idle'" type="primary" :loading="operating" @click="handleSubscribe">
      订阅
    </Button>

    <!-- 已订阅 -->
    <Dropdown v-else-if="state === 'subscribed'" :trigger="['hover']">
      <Button :loading="operating">已订阅</Button>
      <template #overlay>
        <Menu @click="handleUnsubscribeMenu">
          <Menu.Item key="unsubscribe">取消订阅</Menu.Item>
        </Menu>
      </template>
    </Dropdown>

    <!-- 私有频道非成员：申请加入 -->
    <Button v-else-if="state === 'apply'" type="primary" @click="$emit('applyJoin')">
      申请加入
    </Button>

    <!-- 待审核 -->
    <Tooltip v-else-if="state === 'pending'" title="您的申请正在审核中">
      <Button disabled>待审核</Button>
    </Tooltip>

    <!-- 冷却期 -->
    <Tooltip v-else-if="state === 'cooldown'" :title="`冷却期剩余 ${cooldownDays} 天`">
      <Button disabled>冷却期剩余 {{ cooldownDays }} 天</Button>
    </Tooltip>

    <!-- 被黑名单 -->
    <span v-else-if="state === 'blacklisted'" class="blacklisted-text">您无法加入此频道</span>

    <!-- 已禁言 -->
    <div v-else-if="state === 'muted'" class="muted-state">
      <Tag color="orange">已禁言</Tag>
      <Button v-if="!isSubscribed" type="primary" size="small" @click="handleSubscribe">订阅</Button>
    </div>

    <!-- 取消订阅确认 Modal -->
    <Modal
      v-model:open="unsubscribeModalVisible"
      title="确认取消订阅？"
      @ok="handleConfirmUnsubscribe"
      :confirmLoading="operating"
    >
      <p>取消后您将不再收到该频道的更新推送。</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { Button, Dropdown, Menu, Tooltip, Tag, Modal } from 'ant-design-vue';
  import { useChannelOperation } from '/@/hooks/web/useChannelOperation';
  import { subscribeChannel, unsubscribeChannel } from '/@/api/content/channelSubscription';

  type ButtonState = 'idle' | 'subscribed' | 'apply' | 'pending' | 'cooldown' | 'blacklisted' | 'muted';

  const props = defineProps<{
    channelId: string;
    isSubscribed: boolean;
    isMember: boolean;
    isBlacklisted: boolean;
    isMuted: boolean;
    isPrivate: boolean;
    applicationStatus?: string | null;
    cooldownDays?: number;
  }>();

  const emit = defineEmits<{
    (e: 'subscribeChange', subscribed: boolean): void;
    (e: 'applyJoin'): void;
  }>();

  const { operating, optimisticExecute } = useChannelOperation();
  const unsubscribeModalVisible = ref(false);

  const state = computed<ButtonState>(() => {
    if (props.isBlacklisted) return 'blacklisted';
    if (props.isMuted) return 'muted';
    if (props.isSubscribed) return 'subscribed';
    if (props.applicationStatus === 'PENDING') return 'pending';
    if (props.applicationStatus === 'REJECTED' && props.cooldownDays && props.cooldownDays > 0) return 'cooldown';
    if (props.isPrivate && !props.isMember) return 'apply';
    return 'idle';
  });

  function handleSubscribe() {
    optimisticExecute({
      apiCall: () => subscribeChannel(props.channelId),
      onOptimistic: () => emit('subscribeChange', true),
      onRollback: () => emit('subscribeChange', false),
      successMessage: '订阅成功',
      errorMessage: '订阅失败，请重试',
    });
  }

  function handleUnsubscribeMenu({ key }: { key: string }) {
    if (key === 'unsubscribe') {
      unsubscribeModalVisible.value = true;
    }
  }

  function handleConfirmUnsubscribe() {
    optimisticExecute({
      apiCall: () => unsubscribeChannel(props.channelId),
      onOptimistic: () => emit('subscribeChange', false),
      onRollback: () => emit('subscribeChange', true),
      successMessage: '已取消订阅',
      errorMessage: '取消订阅失败，请重试',
    });
    unsubscribeModalVisible.value = false;
  }
</script>

<style scoped>
.subscribe-button-wrapper { display: inline-flex; align-items: center; }
.blacklisted-text { color: #999; font-size: 13px; }
.muted-state { display: flex; align-items: center; gap: 8px; }
</style>
