<template>
  <div :class="[prefixCls]">
    <div class="security-title">账户安全</div>
    <div class="security-desc">管理你的账户安全设置，保护账户免受未经授权的访问。</div>

    <div class="security-list">
      <!-- 设备管理 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <Icon icon="ant-design:desktop-outlined" />
          </div>
          <div class="item-text">
            <div class="item-name">设备管理</div>
            <div class="item-desc">查看和管理已登录的设备，移除不信任的设备</div>
          </div>
        </div>
        <div class="item-action">
          <a-badge :status="securityData.deviceManageEnabled ? 'success' : 'default'" />
          <span class="status-text">{{ securityData.deviceManageEnabled ? '已启用' : '未启用' }}</span>
          <a-button type="link" @click="goTo('/system/usersetting/device')">管理</a-button>
        </div>
      </div>

      <!-- 密码修改 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <Icon icon="ant-design:lock-outlined" />
          </div>
          <div class="item-text">
            <div class="item-name">密码修改</div>
            <div class="item-desc">定期修改密码可以提高账户安全性</div>
          </div>
        </div>
        <div class="item-action">
          <a-badge :status="securityData.passwordSet ? 'success' : 'warning'" />
          <span class="status-text">{{ securityData.passwordSet ? '已设置' : '未设置' }}</span>
          <a-button type="link" @click="goTo('/system/usersetting/password')">修改</a-button>
        </div>
      </div>

      <!-- 两步验证 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <Icon icon="ant-design:shield-outlined" />
          </div>
          <div class="item-text">
            <div class="item-name">两步验证</div>
            <div class="item-desc">登录时需要额外验证，大幅提升账户安全性</div>
          </div>
        </div>
        <div class="item-action">
          <a-badge :status="securityData.twoFactorEnabled ? 'success' : 'default'" />
          <span class="status-text">{{ securityData.twoFactorEnabled ? '已开启' : '未开启' }}</span>
          <a-button type="link" @click="goTo('/system/usersetting/two-factor')">设置</a-button>
        </div>
      </div>

      <!-- 登录提醒 -->
      <div class="security-item">
        <div class="item-info">
          <div class="item-icon">
            <Icon icon="ant-design:notification-outlined" />
          </div>
          <div class="item-text">
            <div class="item-name">登录提醒</div>
            <div class="item-desc">新设备登录时发送通知提醒</div>
          </div>
        </div>
        <div class="item-action">
          <a-switch
            :checked="securityData.loginAlertEnabled"
            :loading="loginAlertLoading"
            @change="handleLoginAlertChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { getSecuritySetting, updateLoginAlert } from '/@/api/content/settings';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { prefixCls } = useDesign('account-security-container');
  const router = useRouter();
  const userStore = useUserStore();
  const { createMessage } = useMessage();

  // 安全设置数据
  const securityData = ref({
    deviceManageEnabled: false,
    passwordSet: false,
    twoFactorEnabled: false,
    loginAlertEnabled: false,
  });

  // 登录提醒开关加载状态
  const loginAlertLoading = ref(false);

  /**
   * 获取安全设置数据
   */
  async function fetchSecuritySetting() {
    try {
      const userId = userStore.getUserInfo?.userId;
      if (!userId) return;
      const res = await getSecuritySetting(userId);
      if (res && res.result) {
        securityData.value = {
          deviceManageEnabled: !!res.result.deviceManageEnabled,
          passwordSet: !!res.result.passwordSet,
          twoFactorEnabled: !!res.result.twoFactorEnabled,
          loginAlertEnabled: !!res.result.loginAlertEnabled,
        };
      }
    } catch {
      // 获取失败时使用默认值
    }
  }

  /**
   * 路由跳转
   */
  function goTo(path: string) {
    router.push(path);
  }

  /**
   * 登录提醒开关切换
   */
  async function handleLoginAlertChange(checked: boolean) {
    loginAlertLoading.value = true;
    try {
      const userId = userStore.getUserInfo?.userId;
      if (!userId) return;
      await updateLoginAlert({ userId, enabled: checked });
      securityData.value.loginAlertEnabled = checked;
      createMessage.success(checked ? '已开启登录提醒' : '已关闭登录提醒');
    } catch {
      createMessage.error('操作失败，请重试');
    } finally {
      loginAlertLoading.value = false;
    }
  }

  onMounted(() => {
    fetchSecuritySetting();
  });
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-account-security-container';

  .@{prefix-cls} {
    padding: 30px 40px 0 20px;

    .security-title {
      font-size: 17px;
      font-weight: 700;
      color: @text-color;
      margin-bottom: 8px;
    }

    .security-desc {
      color: @text-color-secondary;
      margin-bottom: 24px;
    }

    .security-list {
      .security-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px 0;
        border-bottom: 1px solid @border-color-base;

        &:last-child {
          border-bottom: none;
        }

        .item-info {
          display: flex;
          align-items: center;

          .item-icon {
            font-size: 24px;
            color: @primary-color;
            margin-right: 16px;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: @primary-1;
            border-radius: 8px;
          }

          .item-text {
            .item-name {
              font-size: 15px;
              font-weight: 500;
              color: @text-color;
              margin-bottom: 4px;
            }

            .item-desc {
              font-size: 13px;
              color: @text-color-secondary;
            }
          }
        }

        .item-action {
          display: flex;
          align-items: center;
          gap: 8px;

          .status-text {
            font-size: 13px;
            color: @text-color-secondary;
            margin-right: 4px;
          }
        }
      }
    }
  }
</style>
