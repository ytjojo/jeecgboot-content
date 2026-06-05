<template>
  <div :class="prefixCls">
    <a-page-header title="第三方授权管理" sub-title="管理已授权的第三方应用" @back="router.back()" />

    <a-table
      v-if="dataSource.length > 0 || loading"
      :class="`${prefixCls}__table`"
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      row-key="authId"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'appName'">
          {{ record.appName || '未知应用' }}
        </template>
        <template v-if="column.dataIndex === 'scopes'">
          <template v-if="record.scopes && record.scopes.length > 0">
            <a-tag v-for="scope in record.scopes" :key="scope" color="blue">{{ scope }}</a-tag>
          </template>
          <a-tag v-else color="red">未知权限</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleDetail(record)">查看详情</a-button>
            <a-popconfirm
              v-if="record.status === 'ACTIVE'"
              title="撤销后该应用将无法访问你的数据，是否确认？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="handleRevoke(record)"
            >
              <a-button type="link" danger size="small">撤销授权</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 移动端卡片列表 -->
    <div v-if="dataSource.length > 0 && !loading" :class="`${prefixCls}__cards`">
      <a-card v-for="item in dataSource" :key="item.authId" :class="`${prefixCls}__card`" size="small">
        <div :class="`${prefixCls}__card-row`">
          <span :class="`${prefixCls}__card-label`">应用名称</span>
          <span>{{ item.appName || '未知应用' }}</span>
        </div>
        <div :class="`${prefixCls}__card-row`">
          <span :class="`${prefixCls}__card-label`">授权时间</span>
          <span>{{ item.authTime }}</span>
        </div>
        <div :class="`${prefixCls}__card-row`">
          <span :class="`${prefixCls}__card-label`">授权范围</span>
          <span>
            <template v-if="item.scopes && item.scopes.length > 0">
              <a-tag v-for="scope in item.scopes" :key="scope" color="blue">{{ scope }}</a-tag>
            </template>
            <a-tag v-else color="red">未知权限</a-tag>
          </span>
        </div>
        <div :class="`${prefixCls}__card-actions`">
          <a-button type="link" size="small" @click="handleDetail(item)">查看详情</a-button>
          <a-popconfirm
            v-if="item.status === 'ACTIVE'"
            title="撤销后该应用将无法访问你的数据，是否确认？"
            ok-text="确认"
            cancel-text="取消"
            @confirm="handleRevoke(item)"
          >
            <a-button type="link" danger size="small">撤销授权</a-button>
          </a-popconfirm>
        </div>
      </a-card>
    </div>

    <!-- 空状态 -->
    <a-empty v-if="!loading && dataSource.length === 0" description="暂无已授权的第三方应用" />

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="授权详情" :footer="null" width="480px">
      <template v-if="detailLoading">
        <a-spin style="display: block; text-align: center; padding: 40px 0" />
      </template>
      <template v-else-if="detailData">
        <div :class="`${prefixCls}__detail`">
          <div :class="`${prefixCls}__detail-row`">
            <span :class="`${prefixCls}__detail-label`">应用名称</span>
            <span>{{ detailData.appName || '未知应用' }}</span>
          </div>
          <div :class="`${prefixCls}__detail-row`">
            <span :class="`${prefixCls}__detail-label`">授权时间</span>
            <span>{{ detailData.authTime }}</span>
          </div>
          <div :class="`${prefixCls}__detail-row`">
            <span :class="`${prefixCls}__detail-label`">状态</span>
            <a-tag :color="detailData.status === 'ACTIVE' ? 'green' : 'red'">
              {{ detailData.status === 'ACTIVE' ? '已授权' : '已撤销' }}
            </a-tag>
          </div>
          <div :class="`${prefixCls}__detail-row`">
            <span :class="`${prefixCls}__detail-label`">撤销时间</span>
            <span>{{ detailData.revokedAt || '-' }}</span>
          </div>
          <div :class="`${prefixCls}__detail-row`">
            <span :class="`${prefixCls}__detail-label`">授权范围</span>
            <span>
              <template v-if="detailData.scopes && detailData.scopes.length > 0">
                <a-tag v-for="scope in detailData.scopes" :key="scope" color="blue">{{ scope }}</a-tag>
              </template>
              <a-tag v-else color="red">未知权限</a-tag>
            </span>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { listThirdPartyAuths, getThirdPartyAuthDetail, revokeThirdPartyAuth } from '/@/api/content/settings';
  import type { ThirdPartyAuthVO, ThirdPartyAuthDetailVO } from '/@/api/content/settings-types';

  const router = useRouter();
  const { prefixCls } = useDesign('third-party-auth');
  const { createMessage } = useMessage();

  const loading = ref(false);
  const dataSource = ref<ThirdPartyAuthVO[]>([]);

  // 详情弹窗
  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const detailData = ref<ThirdPartyAuthDetailVO | null>(null);

  // 表格列定义
  const columns = [
    { title: '应用名称', dataIndex: 'appName', width: 200 },
    { title: '授权时间', dataIndex: 'authTime', width: 180 },
    { title: '授权范围', dataIndex: 'scopes', width: 260 },
    { title: '操作', dataIndex: 'action', width: 180, fixed: 'right' as const },
  ];

  /** 获取当前用户ID */
  async function getCurrentUserId(): Promise<string> {
    const { useUserStore } = await import('/@/store/modules/user');
    const userStore = useUserStore();
    return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
  }

  /** 加载授权列表 */
  async function loadList() {
    loading.value = true;
    try {
      const userId = await getCurrentUserId();
      dataSource.value = await listThirdPartyAuths(userId);
    } catch (e: any) {
      handleError(e);
    } finally {
      loading.value = false;
    }
  }

  /** 查看详情 */
  async function handleDetail(record: ThirdPartyAuthVO) {
    detailVisible.value = true;
    detailLoading.value = true;
    detailData.value = null;
    try {
      const userId = await getCurrentUserId();
      detailData.value = await getThirdPartyAuthDetail(userId, record.authId);
    } catch (e: any) {
      handleError(e);
      detailVisible.value = false;
    } finally {
      detailLoading.value = false;
    }
  }

  /** 撤销授权 */
  async function handleRevoke(record: ThirdPartyAuthVO) {
    try {
      const userId = await getCurrentUserId();
      await revokeThirdPartyAuth(userId, record.authId);
      dataSource.value = dataSource.value.filter((item) => item.authId !== record.authId);
      createMessage.success('授权已撤销');
    } catch (e: any) {
      handleError(e);
    }
  }

  /** 统一错误处理 */
  function handleError(e: any) {
    const status = e?.response?.status;
    if (status === 404) {
      createMessage.error('授权记录不存在');
    } else if (status === 403) {
      createMessage.error('权限不足');
    } else {
      createMessage.error(e?.message || '操作失败');
    }
  }

  onMounted(loadList);
</script>

<style lang="less" scoped>
  @prefix-cls: ~'jeecg-third-party-auth';

  .@{prefix-cls} {
    max-width: 900px;
    margin: 0 auto;
    padding: 16px;

    &__table {
      // 桌面端显示表格，隐藏卡片
    }

    &__cards {
      // 移动端显示卡片，隐藏表格
      display: none;
    }

    &__card {
      margin-bottom: 12px;
    }

    &__card-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-of-type {
        border-bottom: none;
      }
    }

    &__card-label {
      color: #999;
      flex-shrink: 0;
      margin-right: 12px;
    }

    &__card-actions {
      display: flex;
      justify-content: flex-end;
      padding-top: 8px;
    }

    &__detail-row {
      display: flex;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    &__detail-label {
      width: 80px;
      flex-shrink: 0;
      color: #999;
    }

    // 响应式：移动端隐藏表格，显示卡片
    @media (max-width: 768px) {
      &__table {
        display: none;
      }

      &__cards {
        display: block;
      }
    }
  }
</style>
