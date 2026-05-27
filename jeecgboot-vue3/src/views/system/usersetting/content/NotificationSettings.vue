<template>
  <div :class="[`${prefixCls}`]">
    <div class="settings-title">通知设置</div>

    <!-- 通知类型开关 -->
    <div class="section">
      <div class="section-title">通知类型</div>
      <div v-for="item in notificationTypes" :key="item.key" class="notification-row">
        <div class="row-left">
          <span class="type-label">{{ item.label }}</span>
          <a-switch
            :checked="settings[item.key]"
            @change="(val: boolean) => onToggleChange(item.key, val)"
          />
        </div>
        <div class="row-right">
          <a-checkbox-group
            :value="channels[item.key]"
            :options="channelOptions"
            @change="(vals: string[]) => onChannelChange(item.key, vals)"
          />
        </div>
      </div>
    </div>

    <!-- 免打扰设置 -->
    <div class="section">
      <div class="section-title">
        免打扰时段
        <a-button type="link" size="small" @click="addDndRule">+ 添加时段</a-button>
      </div>
      <div v-if="dndRules.length === 0" class="empty-tip">暂无免打扰时段</div>
      <div v-for="(rule, index) in dndRules" :key="index" class="dnd-row">
        <a-form
          :model="rule"
          :rules="dndRules_formRules"
          :ref="(el: any) => setDndFormRef(el, index)"
          layout="inline"
        >
          <a-form-item name="startTime" label="开始">
            <a-time-picker
              v-model:value="rule.startTime"
              format="HH:mm"
              :allowClear="false"
              :disabled="!rule.enabled"
            />
          </a-form-item>
          <a-form-item name="endTime" label="结束">
            <a-time-picker
              v-model:value="rule.endTime"
              format="HH:mm"
              :allowClear="false"
              :disabled="!rule.enabled"
            />
          </a-form-item>
          <a-form-item label="重复">
            <a-select
              v-model:value="rule.dayType"
              style="width: 120px"
              :disabled="!rule.enabled"
              :options="dayTypeOptions"
            />
          </a-form-item>
          <a-form-item>
            <a-switch
              v-model:checked="rule.enabled"
              checked-children="启用"
              un-checked-children="关闭"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="link" danger size="small" @click="removeDndRule(index)">删除</a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <a-button type="primary" :loading="saving" @click="handleSave">保存设置</a-button>
      <a-button style="margin-left: 12px" @click="handleTempDisable">
        暂时关闭免打扰1小时
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    getNotificationSetting,
    updateNotificationSetting,
    updateDndRule,
  } from '/@/api/content/settings';
  import dayjs, { type Dayjs } from 'dayjs';

  const { prefixCls } = useDesign('notification-settings');
  const { createMessage } = useMessage();
  const userStore = useUserStore();

  // 通知类型定义
  const notificationTypes = [
    { key: 'like', label: '点赞' },
    { key: 'comment', label: '评论' },
    { key: 'follow', label: '关注' },
    { key: 'favorite', label: '收藏' },
    { key: 'at', label: '@我' },
    { key: 'message', label: '私信' },
  ];

  // 渠道选项
  const channelOptions = [
    { label: 'App内', value: 'app' },
    { label: '推送', value: 'push' },
    { label: '短信', value: 'sms' },
    { label: '邮件', value: 'email' },
  ];

  // 日期类型选项
  const dayTypeOptions = [
    { label: '每天', value: 'everyday' },
    { label: '工作日', value: 'workday' },
    { label: '周末', value: 'weekend' },
  ];

  // 通知开关状态
  const settings = reactive<Record<string, boolean>>({
    like: true,
    comment: true,
    follow: true,
    favorite: true,
    at: true,
    message: true,
  });

  // 渠道选择状态
  const channels = reactive<Record<string, string[]>>({
    like: ['app'],
    comment: ['app'],
    follow: ['app'],
    favorite: ['app'],
    at: ['app'],
    message: ['app'],
  });

  // 免打扰规则
  interface DndRule {
    startTime: Dayjs | null;
    endTime: Dayjs | null;
    dayType: string;
    enabled: boolean;
  }

  const dndRules = ref<DndRule[]>([]);

  // 免打扰表单校验规则
  const dndRules_formRules = {
    startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
    endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  };

  // 表单ref集合
  const dndFormRefs = ref<any[]>([]);
  function setDndFormRef(el: any, index: number) {
    if (el) {
      dndFormRefs.value[index] = el;
    }
  }

  const saving = ref(false);
  const userId = ref('');

  /**
   * 切换通知类型开关
   */
  function onToggleChange(key: string, val: boolean) {
    settings[key] = val;
  }

  /**
   * 修改通知渠道
   */
  function onChannelChange(key: string, vals: string[]) {
    channels[key] = vals;
  }

  /**
   * 添加免打扰时段
   */
  function addDndRule() {
    dndRules.value.push({
      startTime: dayjs('22:00', 'HH:mm'),
      endTime: dayjs('08:00', 'HH:mm'),
      dayType: 'everyday',
      enabled: true,
    });
  }

  /**
   * 删除免打扰时段
   */
  function removeDndRule(index: number) {
    dndRules.value.splice(index, 1);
    dndFormRefs.value.splice(index, 1);
  }

  /**
   * 加载通知设置
   */
  async function loadSettings() {
    try {
      const res = await getNotificationSetting(userId.value);
      if (res) {
        // 填充通知开关
        notificationTypes.forEach((item) => {
          if (res[item.key] !== undefined) {
            settings[item.key] = res[item.key];
          }
        });
        // 填充渠道选择
        notificationTypes.forEach((item) => {
          if (res[`${item.key}Channels`]) {
            channels[item.key] = res[`${item.key}Channels`];
          }
        });
        // 填充免打扰规则
        if (res.dndRules && Array.isArray(res.dndRules)) {
          dndRules.value = res.dndRules.map((rule: any) => ({
            startTime: rule.startTime ? dayjs(rule.startTime, 'HH:mm') : null,
            endTime: rule.endTime ? dayjs(rule.endTime, 'HH:mm') : null,
            dayType: rule.dayType || 'everyday',
            enabled: rule.enabled !== false,
          }));
        }
      }
    } catch {
      createMessage.error('加载通知设置失败');
    }
  }

  /**
   * 保存设置
   */
  async function handleSave() {
    // 校验免打扰表单
    for (let i = 0; i < dndRules.value.length; i++) {
      const rule = dndRules.value[i];
      if (rule.enabled) {
        const formRef = dndFormRefs.value[i];
        if (formRef) {
          try {
            await formRef.validate();
          } catch {
            createMessage.warning('请检查免打扰时段配置');
            return;
          }
        }
      }
    }

    saving.value = true;
    try {
      // 构建通知设置数据
      const notificationData: Record<string, any> = {};
      notificationTypes.forEach((item) => {
        notificationData[item.key] = settings[item.key];
        notificationData[`${item.key}Channels`] = channels[item.key];
      });
      await updateNotificationSetting(userId.value, notificationData);

      // 构建免打扰数据
      const dndData = dndRules.value.map((rule) => ({
        startTime: rule.startTime ? rule.startTime.format('HH:mm') : null,
        endTime: rule.endTime ? rule.endTime.format('HH:mm') : null,
        dayType: rule.dayType,
        enabled: rule.enabled,
      }));
      await updateDndRule(userId.value, dndData);

      createMessage.success('保存成功');
    } catch {
      createMessage.error('保存失败');
    } finally {
      saving.value = false;
    }
  }

  /**
   * 临时关闭免打扰1小时
   */
  async function handleTempDisable() {
    try {
      const dndData = dndRules.value.map((rule) => ({
        ...rule,
        enabled: false,
        startTime: rule.startTime ? rule.startTime.format('HH:mm') : null,
        endTime: rule.endTime ? rule.endTime.format('HH:mm') : null,
      }));
      await updateDndRule(userId.value, dndData);
      dndRules.value.forEach((rule) => (rule.enabled = false));
      createMessage.success('免打扰已临时关闭1小时');
    } catch {
      createMessage.error('操作失败');
    }
  }

  onMounted(() => {
    const userInfo = userStore.getUserInfo;
    userId.value = userInfo?.id || '';
    if (userId.value) {
      loadSettings();
    }
  });
</script>

<style lang="less">
  @prefix-cls: ~'@{namespace}-notification-settings';

  .@{prefix-cls} {
    padding: 30px 40px 0 20px;

    .settings-title {
      font-size: 17px;
      font-weight: 700 !important;
      color: @text-color;
      margin-bottom: 24px;
    }

    .section {
      margin-bottom: 32px;
    }

    .section-title {
      font-size: 15px;
      font-weight: 600;
      color: @text-color;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
    }

    .notification-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid @border-color-base;
    }

    .row-left {
      display: flex;
      align-items: center;
      gap: 16px;
      min-width: 140px;
    }

    .type-label {
      color: @text-color;
      font-size: 14px;
    }

    .row-right {
      flex: 1;
      text-align: right;
    }

    .dnd-row {
      margin-bottom: 12px;
      padding: 12px;
      background: @background-color-light;
      border-radius: 4px;
    }

    .empty-tip {
      color: @text-color-secondary;
      font-size: 13px;
      padding: 12px 0;
    }

    .actions {
      padding-top: 16px;
      border-top: 1px solid @border-color-base;
    }
  }
</style>
