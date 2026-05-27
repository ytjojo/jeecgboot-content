<template>
  <div :class="[prefixCls]">
    <div class="auth-title">第三方授权管理</div>
    <div class="auth-desc">管理你已授权的第三方应用，可以随时撤销不需要的授权。</div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <a-spin />
    </div>

    <!-- 空状态 -->
    <a-empty v-else-if="authList.length === 0" description="暂无第三方授权记录" />

    <!-- 授权列表 -->
    <div v-else class="auth-list">
      <div v-for="item in authList" :key="item.id" class="auth-item">
        <div class="item-info">
          <div class="item-icon">
            <Icon icon="ant-design:appstore-outlined" />
          </div>
          <div class="item-text">
            <div class="item-name">{{ item.appName }}</div>
            <div class="item-meta">
              <span class="meta-label">授权时间：</span>{{ item.authTime }}
            </div>
            <div class="item-meta">
              <span class="meta-label">权限范围：</span>{{ item.scopes || '基础信息' }}
            </div>
          </div>
        </div>
        <div class="item-action">
          <a-badge :status="item.status === 'active' ? 'success' : 'default'" />
          <span class="status-text">{{ item.status === 'active' ? '已授权' : '已过期' }}</span>
          <a-popconfirm
            title="确定撤销该应用的授权吗？撤销后该应用将无法访问你的数据。"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleRevoke(item.id)"
          >
            <a-button type="link" danger :loading="revokingId === item.id">撤销授权</a-button>
          </a-popconfirm>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { listThirdPartyAuths, revokeThirdPartyAuth } from '/@/api/content/settings';

  // 授权记录类型
  interface ThirdPartyAuth {
    id: string;
    appName: string;
    authTime: string;
    scopes: string;
    status: string;
  }

  const { prefixCls } = useDesign('third-party-auth-container');
  const userStore = useUserStore();
  const { createMessage } = useMessage();

  const loading = ref(false);
  const revokingId = ref<string>('');
  const authList = ref<ThirdPartyAuth[]>([]);

  /**
   * 获取当前用户ID
   */
  function getUserId(): string {
    return userStore.getUserInfo?.userId || '';
  }

  /**
   * 加载第三方授权列表
   */
  async function fetchAuthList() {
    const userId = getUserId();
    if (!userId) return;
    loading.value = true;
    try {
      const res = await listThirdPartyAuths(userId);
      if (res && Array.isArray(res)) {
        authList.value = res;
      } else if (res && res.result && Array.isArray(res.result)) {
        authList.value = res.result;
      } else {
        authList.value = [];
      }
    } catch {
      createMessage.error('加载授权列表失败');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 撤销第三方授权
   */
  async function handleRevoke(authId: string) {
    const userId = getUserId();
    if (!userId) return;
    revokingId.value = authId;
    try {
      await revokeThirdPartyAuth(userId, authId);
      createMessage.success('已撤销授权');
      // 撤销后自动刷新列表
      await fetchAuthList();
    } catch {
      createMessage.error('撤销失败，请重试');
    } finally {
      revokingId.value = '';
    }
  }

  onMounted(() => {
    fetchAuthList();
  });
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-third-party-auth-container';

  .@{prefix-cls} {
    padding: 30px 40px 0 20px;

    .auth-title {
      font-size: 17px;
      font-weight: 700;
      color: @text-color;
      margin-bottom: 8px;
    }

    .auth-desc {
      color: @text-color-secondary;
      margin-bottom: 24px;
    }

    .loading-wrap {
      display: flex;
      justify-content: center;
      padding: 60px 0;
    }

    .auth-list {
      .auth-item {
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

            .item-meta {
              font-size: 13px;
              color: @text-color-secondary;
              margin-bottom: 2px;

              .meta-label {
                color: @text-color-secondary;
              }
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
