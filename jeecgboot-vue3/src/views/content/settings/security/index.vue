<template>
  <div :class="prefixCls">
    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]">
        <!-- 设备管理 -->
        <a-col :xs="24" :md="12">
          <a-card hoverable :class="`${prefixCls}__card`" @click="goTo('/content/account-security/devices')">
            <div :class="`${prefixCls}__card-content`">
              <div :class="`${prefixCls}__card-icon`">
                <Icon icon="ant-design:desktop-outlined" :size="28" />
              </div>
              <div :class="`${prefixCls}__card-info`">
                <div :class="`${prefixCls}__card-title`">设备管理</div>
                <div :class="`${prefixCls}__card-desc`">
                  <a-badge
                    :status="deviceManagementEnabled ? 'success' : 'default'"
                    :text="deviceManagementEnabled ? '已启用' : '未启用'"
                  />
                </div>
              </div>
              <Icon icon="mdi:chevron-right" :size="20" :class="`${prefixCls}__card-arrow`" />
            </div>
          </a-card>
        </a-col>

        <!-- 密码修改 -->
        <a-col :xs="24" :md="12">
          <a-card hoverable :class="`${prefixCls}__card`" @click="goTo('/content/account-security/password')">
            <div :class="`${prefixCls}__card-content`">
              <div :class="`${prefixCls}__card-icon`">
                <Icon icon="ant-design:lock-outlined" :size="28" />
              </div>
              <div :class="`${prefixCls}__card-info`">
                <div :class="`${prefixCls}__card-title`">密码修改</div>
                <div :class="`${prefixCls}__card-desc`">
                  <a-badge
                    :status="passwordChangeEnabled ? 'success' : 'default'"
                    :text="passwordChangeEnabled ? '已启用' : '未启用'"
                  />
                </div>
              </div>
              <Icon icon="mdi:chevron-right" :size="20" :class="`${prefixCls}__card-arrow`" />
            </div>
          </a-card>
        </a-col>

        <!-- 两步验证 -->
        <a-col :xs="24" :md="12">
          <a-card hoverable :class="`${prefixCls}__card`" @click="goTo('/content/account-security/two-factor')">
            <div :class="`${prefixCls}__card-content`">
              <div :class="`${prefixCls}__card-icon`">
                <Icon icon="ant-design:shield-outlined" :size="28" />
              </div>
              <div :class="`${prefixCls}__card-info`">
                <div :class="`${prefixCls}__card-title`">两步验证</div>
                <div :class="`${prefixCls}__card-desc`">
                  <a-badge
                    :status="twoFactorEnabled ? 'success' : 'default'"
                    :text="twoFactorEnabled ? '已启用' : '未启用'"
                  />
                </div>
              </div>
              <Icon icon="mdi:chevron-right" :size="20" :class="`${prefixCls}__card-arrow`" />
            </div>
          </a-card>
        </a-col>

        <!-- 登录提醒 -->
        <a-col :xs="24" :md="12">
          <a-card :class="`${prefixCls}__card`">
            <div :class="`${prefixCls}__card-content`">
              <div :class="`${prefixCls}__card-icon`">
                <Icon icon="ant-design:bell-outlined" :size="28" />
              </div>
              <div :class="`${prefixCls}__card-info`">
                <div :class="`${prefixCls}__card-title`">登录提醒</div>
                <div :class="`${prefixCls}__card-desc`">
                  <a-badge
                    :status="loginAlertEnabled ? 'success' : 'default'"
                    :text="loginAlertEnabled ? '已开启' : '已关闭'"
                  />
                </div>
              </div>
              <a-switch
                :checked="loginAlertEnabled"
                :loading="switchLoading"
                @change="onLoginAlertChange"
                @click.stop
              />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Icon } from '/@/components/Icon';
  import { getSecuritySetting, updateSecuritySetting } from '/@/api/content/settings';
  import type { SecuritySettingVO } from '/@/api/content/settings-types';

  const { prefixCls } = useDesign('account-security');
  const router = useRouter();
  const userStore = useUserStore();
  const { createMessage } = useMessage();

  const loading = ref(false);
  const switchLoading = ref(false);

  const deviceManagementEnabled = ref(true);
  const passwordChangeEnabled = ref(true);
  const twoFactorEnabled = ref(false);
  const loginAlertEnabled = ref(true);

  /** 加载安全设置 */
  async function loadSettings() {
    const userId = (userStore.getUserInfo as any)?.id || (userStore.getUserInfo as any)?.userId || '';
    if (!userId) {
      createMessage.error('未识别当前用户');
      return;
    }

    loading.value = true;
    try {
      const data: SecuritySettingVO = await getSecuritySetting(userId);
      deviceManagementEnabled.value = data.deviceManagementEnabled ?? true;
      passwordChangeEnabled.value = data.passwordChangeEnabled ?? true;
      twoFactorEnabled.value = data.twoFactorEnabled ?? false;
      loginAlertEnabled.value = data.loginAlertEnabled ?? true;
    } catch {
      createMessage.error('加载安全设置失败');
    } finally {
      loading.value = false;
    }
  }

  /** 登录提醒开关切换 */
  async function onLoginAlertChange(checked: boolean) {
    const userId = (userStore.getUserInfo as any)?.id || (userStore.getUserInfo as any)?.userId || '';
    if (!userId) {
      createMessage.error('未识别当前用户');
      return;
    }

    const prev = loginAlertEnabled.value;
    loginAlertEnabled.value = checked;
    switchLoading.value = true;

    try {
      await updateSecuritySetting(userId, { loginAlertEnabled: checked });
      createMessage.success(checked ? '已开启登录提醒' : '已关闭登录提醒');
    } catch {
      // 回滚开关状态
      loginAlertEnabled.value = prev;
      createMessage.error('更新失败，请稍后重试');
    } finally {
      switchLoading.value = false;
    }
  }

  /** 跳转到子页面 */
  function goTo(path: string) {
    router.push(path);
  }

  onMounted(() => {
    loadSettings();
  });
</script>

<style lang="less" scoped>
  @prefix-cls: ~'jeecg-account-security';

  .@{prefix-cls} {
    max-width: 800px;
    margin: 0 auto;
    padding: 16px;

    &__card {
      cursor: pointer;
      transition: box-shadow 0.3s;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
      }
    }

    &__card-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    &__card-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: #f0f5ff;
      color: #1890ff;
      flex-shrink: 0;
    }

    &__card-info {
      flex: 1;
      min-width: 0;
    }

    &__card-title {
      font-size: 16px;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.85);
      line-height: 1.4;
    }

    &__card-desc {
      margin-top: 4px;
      color: rgba(0, 0, 0, 0.45);
      font-size: 13px;
    }

    &__card-arrow {
      color: rgba(0, 0, 0, 0.25);
      flex-shrink: 0;
    }

    @media (max-width: 768px) {
      padding: 8px;
    }
  }
</style>
