<template>
  <div class="subscribe-button-wrapper">
    <Popconfirm
      v-if="isSubscribedState"
      title="确定取消订阅该内容源吗？"
      ok-text="确定"
      cancel-text="取消"
      @confirm="handleUnsubscribe"
    >
      <Button
        :type="isSubscribedState ? 'default' : 'primary'"
        :loading="loading"
        :disabled="disabled"
        @mouseenter="hovering = true"
        @mouseleave="hovering = false"
      >
        {{ buttonText }}
      </Button>
    </Popconfirm>
    <Button
      v-else
      :type="isSubscribedState ? 'default' : 'primary'"
      :loading="loading"
      :disabled="disabled"
      @click="handleSubscribe"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
    >
      {{ buttonText }}
    </Button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { useSubscribeStore } from '/@/store/modules/subscribe';

const props = defineProps<{
  userId: string;
  sourceId: string;
  sourceType: string;
  isSubscribed: boolean;
  isPaused?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:isSubscribed', value: boolean): void;
  (e: 'subscribe'): void;
  (e: 'unsubscribe'): void;
}>();

const subscribeStore = useSubscribeStore();
const loading = ref(false);
const hovering = ref(false);
const isSubscribedState = ref(props.isSubscribed);

watch(
  () => props.isSubscribed,
  (val) => {
    isSubscribedState.value = val;
  }
);

const buttonText = computed(() => {
  if (isSubscribedState.value && hovering.value) return '取消订阅';
  if (isSubscribedState.value) return '已订阅';
  return '订阅';
});

function handleSubscribe() {
  loading.value = true;
  const prev = isSubscribedState.value;
  isSubscribedState.value = true;
  emit('update:isSubscribed', true);
  emit('subscribe');

  subscribeStore
    .subscribe(props.userId, props.sourceId, props.sourceType)
    .then(() => {
      message.success('订阅成功');
    })
    .catch(() => {
      isSubscribedState.value = prev;
      emit('update:isSubscribed', prev);
      message.error('订阅失败');
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleUnsubscribe() {
  loading.value = true;
  const prev = isSubscribedState.value;
  isSubscribedState.value = false;
  emit('update:isSubscribed', false);
  emit('unsubscribe');

  subscribeStore
    .unsubscribe(props.userId, props.sourceId)
    .then(() => {
      message.success('已取消订阅');
    })
    .catch(() => {
      isSubscribedState.value = prev;
      emit('update:isSubscribed', prev);
      message.error('取消订阅失败');
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>

<style scoped lang="less">
.subscribe-button-wrapper {
  display: inline-block;
}
</style>
