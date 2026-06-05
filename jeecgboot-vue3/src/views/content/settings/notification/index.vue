<template>
  <div :class="prefixCls">
    <a-page-header title="通知设置" :back-icon="true" @back="$router.back()" />

    <a-spin :spinning="loading">
      <!-- 通知类型设置 -->
      <a-card title="通知类型" :bordered="false" :class="`${prefixCls}__card`">
        <div
          v-for="item in notificationTypes"
          :key="item.key"
          :class="`${prefixCls}__row`"
        >
          <div :class="`${prefixCls}__row-header`">
            <span :class="`${prefixCls}__row-label`">{{ item.label }}</span>
            <a-switch
              :checked="settings[item.key]"
              @change="(val: boolean) => onSwitchChange(item.key, val)"
            />
          </div>
          <a-checkbox-group
            :class="`${prefixCls}__channels`"
            :value="channels[item.key]"
            :disabled="!settings[item.key]"
            :options="channelOptions"
            @change="(vals: string[]) => onChannelChange(item.key, vals)"
          />
        </div>
      </a-card>

      <!-- 免打扰规则 -->
      <a-card title="免打扰规则" :bordered="false" :class="`${prefixCls}__card`">
        <div :class="`${prefixCls}__dnd-header`">
          <a-switch v-model:checked="dndEnabled" />
          <span :class="`${prefixCls}__dnd-label`">开启免打扰</span>
        </div>

        <div v-if="dndEnabled" :class="`${prefixCls}__dnd-list`">
          <div
            v-for="(rule, index) in dndRules"
            :key="index"
            :class="`${prefixCls}__dnd-rule`"
          >
            <div :class="`${prefixCls}__dnd-rule-row`">
              <a-switch
                :checked="rule.enabled"
                size="small"
                @change="(val: boolean) => (rule.enabled = val)"
              />
              <a-time-picker
                :value="parseTime(rule.startTime)"
                format="HH:mm"
                :minute-step="15"
                :allow-clear="false"
                :disabled="!rule.enabled"
                :class="`${prefixCls}__time-picker`"
                @change="(_: any, val: string) => (rule.startTime = val)"
              />
              <span>至</span>
              <a-time-picker
                :value="parseTime(rule.endTime)"
                format="HH:mm"
                :minute-step="15"
                :allow-clear="false"
                :disabled="!rule.enabled"
                :class="`${prefixCls}__time-picker`"
                @change="(_: any, val: string) => (rule.endTime = val)"
              />
              <a-select
                v-model:value="rule.dayType"
                :options="dayTypeOptions"
                :disabled="!rule.enabled"
                :class="`${prefixCls}__day-select`"
              />
              <div :class="`${prefixCls}__dnd-rule-actions`">
                <a-switch
                  v-model:checked="rule.summaryMode"
                  :disabled="!rule.enabled"
                  size="small"
                />
                <span :class="`${prefixCls}__summary-label`">汇总模式</span>
                <a-button
                  type="link"
                  danger
                  size="small"
                  @click="removeDndRule(index)"
                >
                  删除
                </a-button>
              </div>
            </div>
          </div>

          <a-button
            v-if="dndRules.length < 5"
            type="dashed"
            block
            :class="`${prefixCls}__add-rule`"
            @click="addDndRule"
          >
            + 添加规则
          </a-button>
          <a-alert
            v-if="dndRules.length >= 5"
            message="最多添加5条免打扰规则"
            type="info"
            show-icon
            :class="`${prefixCls}__tip`"
          />
        </div>

        <!-- 暂时关闭免打扰 -->
        <div v-if="dndEnabled" :class="`${prefixCls}__temp-disable`">
          <template v-if="tempDisableRemain > 0">
            <a-alert
              type="warning"
              :message="`免打扰已暂时关闭，剩余 ${formatRemain(tempDisableRemain)}`"
              show-icon
            />
          </template>
          <template v-else>
            <a-button @click="onTempDisable">暂时关闭免打扰（1小时）</a-button>
          </template>
        </div>
      </a-card>

      <!-- 保存按钮 -->
      <div :class="`${prefixCls}__footer`">
        <a-button type="primary" :loading="saving" @click="onSave">
          保存设置
        </a-button>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue';
import dayjs, { type Dayjs } from 'dayjs';
import { useDesign } from '/@/hooks/web/useDesign';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import {
  getNotificationSetting,
  updateNotificationSetting,
  updateDndRule,
} from '/@/api/content/settings';
import type { DndRuleItem, NotificationChannelConfig } from '/@/api/content/settings-types';

const { prefixCls } = useDesign('notification-settings');
const userStore = useUserStore();
const { createMessage } = useMessage();

const userInfo = userStore.getUserInfo;
const userId = computed(() => userInfo?.id || '');

// ---- 状态 ----
const loading = ref(false);
const saving = ref(false);

// 通知开关
const settings = reactive<Record<string, boolean>>({
  like: true,
  comment: true,
  follow: true,
  favorite: true,
  mention: true,
  privateMessage: true,
  subscription: true,
});

// 通知渠道
const channels = reactive<Record<string, string[]>>({
  like: ['IN_APP', 'PUSH'],
  comment: ['IN_APP', 'PUSH'],
  follow: ['IN_APP', 'PUSH'],
  favorite: ['IN_APP', 'PUSH'],
  mention: ['IN_APP', 'PUSH'],
  privateMessage: ['IN_APP', 'PUSH'],
  subscription: ['IN_APP', 'PUSH'],
});

// 免打扰
const dndEnabled = ref(false);
const dndRules = ref<DndRuleItem[]>([]);
const temporaryDisableUntil = ref<number | null>(null);
const tempDisableRemain = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

// ---- 常量 ----
const notificationTypes = [
  { key: 'like', label: '点赞通知' },
  { key: 'comment', label: '评论通知' },
  { key: 'follow', label: '关注通知' },
  { key: 'favorite', label: '收藏通知' },
  { key: 'mention', label: '@我通知' },
  { key: 'privateMessage', label: '私信通知' },
  { key: 'subscription', label: '订阅更新' },
] as const;

const channelOptions = [
  { label: '站内信', value: 'IN_APP' },
  { label: '推送', value: 'PUSH' },
  { label: '邮件', value: 'EMAIL' },
  { label: '短信', value: 'SMS' },
];

const dayTypeOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '工作日', value: 'WORKDAY' },
  { label: '周末', value: 'WEEKEND' },
  { label: '自定义', value: 'CUSTOM' },
];

// API VO 字段映射
const keyToField: Record<string, string> = {
  like: 'likeNoticeEnabled',
  comment: 'commentNoticeEnabled',
  follow: 'followNoticeEnabled',
  favorite: 'favoriteNoticeEnabled',
  mention: 'mentionNoticeEnabled',
  privateMessage: 'privateMessageNoticeEnabled',
  subscription: 'subscriptionNoticeEnabled',
};

const keyToChannel: Record<string, string> = {
  like: 'likeChannels',
  comment: 'commentChannels',
  follow: 'followChannels',
  favorite: 'favoriteChannels',
  mention: 'mentionChannels',
  privateMessage: 'privateMessageChannels',
  subscription: 'subscriptionChannels',
};

// ---- 方法 ----
function parseTime(timeStr: string): Dayjs | null {
  if (!timeStr) return null;
  return dayjs(timeStr, 'HH:mm');
}

function formatRemain(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}分${s < 10 ? '0' : ''}${s}秒`;
}

function startTempCountdown() {
  stopTempCountdown();
  timer = setInterval(() => {
    if (!temporaryDisableUntil.value) {
      stopTempCountdown();
      return;
    }
    const remain = Math.floor((temporaryDisableUntil.value - Date.now()) / 1000);
    tempDisableRemain.value = remain > 0 ? remain : 0;
    if (remain <= 0) {
      stopTempCountdown();
    }
  }, 1000);
}

function stopTempCountdown() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

function onSwitchChange(key: string, val: boolean) {
  settings[key] = val;
}

function onChannelChange(key: string, vals: string[]) {
  channels[key] = vals;
}

function addDndRule() {
  dndRules.value.push({
    enabled: true,
    startTime: '22:00',
    endTime: '07:00',
    dayType: 'DAILY',
    summaryMode: false,
  });
}

function removeDndRule(index: number) {
  dndRules.value.splice(index, 1);
}

async function onTempDisable() {
  if (!userId.value) return;
  try {
    await updateDndRule(userId.value, { temporaryDisable: true });
    // 设置1小时后的时间戳
    temporaryDisableUntil.value = Date.now() + 60 * 60 * 1000;
    startTempCountdown();
    createMessage.success('免打扰已暂时关闭1小时');
  } catch {
    createMessage.error('操作失败');
  }
}

async function loadData() {
  if (!userId.value) return;
  loading.value = true;
  try {
    const res = await getNotificationSetting(userId.value);
    // 加载通知开关，null 默认为 true
    for (const item of notificationTypes) {
      const field = keyToField[item.key];
      const val = res[field as keyof typeof res];
      settings[item.key] = val ?? true;
    }
    // 加载通知渠道，null 默认为 IN_APP + PUSH
    if (res.channelConfig) {
      for (const item of notificationTypes) {
        const field = keyToChannel[item.key];
        const val = res.channelConfig[field as keyof NotificationChannelConfig];
        channels[item.key] = val && val.length > 0 ? [...val] : ['IN_APP', 'PUSH'];
      }
    }
    // 加载免打扰规则
    if (res.dndRule) {
      dndEnabled.value = res.dndRule.enabled;
      dndRules.value = (res.dndRule.dndRules || []).map((r) => ({ ...r }));
      temporaryDisableUntil.value = res.dndRule.temporaryDisableUntil;
      if (temporaryDisableUntil.value && temporaryDisableUntil.value > Date.now()) {
        startTempCountdown();
      }
    }
  } catch {
    createMessage.error('加载通知设置失败');
  } finally {
    loading.value = false;
  }
}

async function onSave() {
  if (!userId.value) return;
  saving.value = true;
  try {
    // 构造通知设置请求
    const notificationData: Record<string, any> = {};
    for (const item of notificationTypes) {
      notificationData[keyToField[item.key]] = settings[item.key];
    }
    const channelConfig: Record<string, string[]> = {};
    for (const item of notificationTypes) {
      channelConfig[keyToChannel[item.key]] = channels[item.key];
    }
    notificationData.channelConfig = channelConfig;

    // 构造免打扰规则请求
    const dndData = {
      enabled: dndEnabled.value,
      dndRules: dndRules.value,
    };

    await Promise.all([
      updateNotificationSetting(userId.value, notificationData as any),
      updateDndRule(userId.value, dndData),
    ]);
    createMessage.success('保存成功');
  } catch {
    createMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
}

// ---- 生命周期 ----
onMounted(() => {
  loadData();
});

onBeforeUnmount(() => {
  stopTempCountdown();
});
</script>

<style lang="less" scoped>
@prefix-cls: ~'jeecg-notification-settings';

.@{prefix-cls} {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;

  &__card {
    margin-bottom: 16px;
  }

  &__row {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  &__row-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  &__row-label {
    font-size: 14px;
    font-weight: 500;
  }

  &__channels {
    padding-left: 4px;
  }

  &__dnd-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
  }

  &__dnd-label {
    font-size: 14px;
    font-weight: 500;
  }

  &__dnd-list {
    padding-left: 8px;
  }

  &__dnd-rule {
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-of-type {
      border-bottom: none;
    }
  }

  &__dnd-rule-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  &__dnd-rule-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-left: auto;
  }

  &__summary-label {
    font-size: 12px;
    color: #666;
  }

  &__time-picker {
    width: 100px;
  }

  &__day-select {
    width: 100px;
  }

  &__add-rule {
    margin-top: 8px;
  }

  &__tip {
    margin-top: 8px;
  }

  &__temp-disable {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }

  &__footer {
    margin-top: 24px;
    text-align: right;
  }

  // 移动端响应式
  @media (max-width: 768px) {
    &__row-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    &__dnd-rule-row {
      flex-direction: column;
      align-items: flex-start;
    }

    &__dnd-rule-actions {
      margin-left: 0;
      width: 100%;
      justify-content: space-between;
    }

    &__time-picker,
    &__day-select {
      width: 100%;
    }
  }
}
</style>
