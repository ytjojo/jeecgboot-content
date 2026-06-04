<template>
  <div class="notification-config-page">
    <div class="notification-config-page__header">
      <h2 class="notification-config-page__title">
        {{ isGlobal ? '全局通知设置' : '订阅通知设置' }}
      </h2>
    </div>

    <template v-if="isGlobal && globalDefault">
      <div class="notification-config-page__global-hint">
        <span class="notification-config-page__global-label">当前全局默认：</span>
        <a-tag color="blue">{{ globalDefault.frequency === 'realtime' ? '实时推送' : '每日摘要' }}</a-tag>
        <span v-if="globalDefault.channelInApp" class="notification-config-page__global-tag">站内通知</span>
        <span v-if="globalDefault.channelPush" class="notification-config-page__global-tag">推送通知</span>
        <span v-if="globalDefault.channelEmail" class="notification-config-page__global-tag">邮件通知</span>
      </div>
    </template>

    <a-spin :spinning="fetchLoading">
      <a-form
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        class="notification-config-page__form"
      >
        <a-form-item label="站内通知">
          <a-switch v-model:checked="form.channelInApp" />
        </a-form-item>
        <a-form-item label="推送通知">
          <a-switch v-model:checked="form.channelPush" />
        </a-form-item>
        <a-form-item label="邮件通知">
          <a-switch v-model:checked="form.channelEmail" />
        </a-form-item>
        <a-form-item label="推送频率">
          <a-radio-group v-model:value="form.frequency">
            <a-radio value="realtime">实时推送</a-radio>
            <a-radio value="daily">每日摘要</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="免打扰时段">
          <div class="notification-config-page__quiet-time">
            <a-time-picker
              v-model:value="quietStartValue"
              format="HH:mm"
              :allow-clear="true"
              placeholder="开始时间"
              @change="handleQuietStartChange"
            />
            <span class="notification-config-page__quiet-sep">至</span>
            <a-time-picker
              v-model:value="quietEndValue"
              format="HH:mm"
              :allow-clear="true"
              placeholder="结束时间"
              @change="handleQuietEndChange"
            />
          </div>
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
          <a-button type="primary" :loading="saving" :disabled="saving" @click="handleSave">
            保存
          </a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useSubscribeStore } from '/@/store/modules/subscribe';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import type { NotificationConfig } from '/@/store/modules/subscribe';
import type { Dayjs } from 'dayjs';

const route = useRoute();
const subscribeStore = useSubscribeStore();
const userStore = useUserStore();
const { createMessage } = useMessage();

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');
const sourceId = computed(() => (route.query.sourceId as string) || '');
const isGlobal = computed(() => !sourceId.value);

const fetchLoading = ref(false);
const saving = ref(false);
const globalDefault = ref<NotificationConfig | null>(null);

const quietStartValue = ref<Dayjs | null>(null);
const quietEndValue = ref<Dayjs | null>(null);

const form = reactive<NotificationConfig>({
  channelInApp: true,
  channelPush: true,
  channelEmail: false,
  frequency: 'realtime',
  quietStart: '',
  quietEnd: '',
});

function handleQuietStartChange(val: Dayjs | null) {
  form.quietStart = val ? val.format('HH:mm') : '';
}

function handleQuietEndChange(val: Dayjs | null) {
  form.quietEnd = val ? val.format('HH:mm') : '';
}

function applyConfig(config: NotificationConfig | null) {
  if (!config) return;
  form.channelInApp = config.channelInApp;
  form.channelPush = config.channelPush;
  form.channelEmail = config.channelEmail;
  form.frequency = config.frequency;
  form.quietStart = config.quietStart || '';
  form.quietEnd = config.quietEnd || '';
}

async function handleSave() {
  saving.value = true;
  try {
    const userId = isGlobal.value ? 'global' : currentUserId.value;
    const sid = isGlobal.value ? 'default' : sourceId.value;
    await subscribeStore.saveConfig(userId, sid, { ...form });
    createMessage.success('保存成功');
  } catch (error) {
    console.error('[NotificationConfig] save failed:', error);
    createMessage.error('保存失败，请重试');
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  fetchLoading.value = true;
  try {
    if (isGlobal.value) {
      await subscribeStore.fetchGlobalNotificationDefault().catch(console.error);
      globalDefault.value = subscribeStore.globalNotificationDefault;
      applyConfig(subscribeStore.globalNotificationDefault);
    } else {
      await subscribeStore.fetchNotificationConfig(currentUserId.value, sourceId.value).catch(console.error);
      applyConfig(subscribeStore.currentNotificationConfig);
    }
  } finally {
    fetchLoading.value = false;
  }
});
</script>

<style scoped lang="less">
.notification-config-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    margin-bottom: 24px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__global-hint {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f6f8fa;
    border-radius: 4px;
    margin-bottom: 20px;
    font-size: 13px;
    flex-wrap: wrap;
  }

  &__global-label {
    color: #666;
  }

  &__global-tag {
    font-size: 12px;
    color: #666;
  }

  &__form {
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    padding: 24px 16px;
  }

  &__quiet-time {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__quiet-sep {
    color: #999;
  }
}
</style>
