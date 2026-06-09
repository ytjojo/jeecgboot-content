<template>
  <div class="circle-governance-log-page">
    <!-- 403 -->
    <a-result v-if="forbidden" status="403" title="您没有权限访问此页面" sub-title="仅圈子创建者可以查看治理日志">
      <template #extra>
        <a-button type="primary" @click="goDetail">返回圈子详情</a-button>
      </template>
    </a-result>

    <div v-else class="log-container">
      <!-- 页面头 -->
      <div class="log-header">
        <a-button type="link" @click="goDetail">
          <ArrowLeftOutlined /> 返回圈子详情
        </a-button>
        <h2 class="log-title">治理日志</h2>
      </div>

      <!-- 筛选区 -->
      <div class="log-filters">
        <a-space wrap>
          <a-select v-model:value="filters.operationType" placeholder="全部操作类型" allow-clear style="width: 140px" @change="handleFilter">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="MUTE">禁言</a-select-option>
            <a-select-option value="UNMUTE">解除禁言</a-select-option>
            <a-select-option value="REMOVE">移除</a-select-option>
            <a-select-option value="ROLE_CHANGE">角色变更</a-select-option>
          </a-select>
          <a-input-search
            v-model:value="filters.targetUser"
            placeholder="操作对象昵称..."
            style="width: 200px"
            @search="handleFilter"
          />
          <a-range-picker
            v-model:value="filters.dateRange"
            :placeholder="['开始日期', '结束日期']"
            @change="handleFilter"
          />
        </a-space>
      </div>

      <!-- 日志表格 -->
      <a-table
        :columns="columns"
        :data-source="logs"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operationType'">
            <a-tag v-if="record.operationType === 'MUTE'" color="orange">禁言</a-tag>
            <a-tag v-else-if="record.operationType === 'UNMUTE'" color="green">解除禁言</a-tag>
            <a-tag v-else-if="record.operationType === 'REMOVE'" color="red">移除</a-tag>
            <a-tag v-else-if="record.operationType === 'ROLE_CHANGE'" color="blue">角色变更</a-tag>
            <span v-else>{{ record.operationType }}</span>
          </template>
          <template v-if="column.key === 'detail'">
            <span v-if="record.operationType === 'MUTE'">
              禁言时长: {{ record.detail || '-' }}
            </span>
            <span v-else-if="record.operationType === 'ROLE_CHANGE'">
              {{ record.detail || '-' }}
            </span>
            <span v-else>
              {{ record.reason || record.detail || '-' }}
            </span>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && logs.length === 0" description="暂无治理记录" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeftOutlined } from '@ant-design/icons-vue';
import { getGovernanceLogList, getCircleDetail } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import { useCircleStoreWithOut } from '/@/store/modules/circle';
import type { CircleGovernanceLogVO } from '/@/api/content/model/circleModel';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();
const circleStore = useCircleStoreWithOut();

const circleId = computed(() => route.params.id as string);
const forbidden = ref(false);
const loading = ref(false);
const logs = ref<CircleGovernanceLogVO[]>([]);

// 筛选
const filters = reactive({
  operationType: '',
  targetUser: '',
  dateRange: null as any,
});

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: false,
});

// 表格列
const columns = [
  { key: 'createTime', title: '操作时间', dataIndex: 'createTime', width: 170 },
  { key: 'operatorId', title: '操作者', dataIndex: 'operatorId', width: 120 },
  { key: 'targetUserId', title: '操作对象', dataIndex: 'targetUserId', width: 120 },
  { key: 'operationType', title: '操作类型', width: 100 },
  { key: 'detail', title: '详情' },
];

// 权限校验
onMounted(async () => {
  try {
    const detail = await getCircleDetail(circleId.value);
    if (detail?.myRole !== 'CREATOR') {
      forbidden.value = true;
      return;
    }
    circleStore.setCurrentCircle(detail);
    fetchData();
  } catch {
    forbidden.value = true;
  }
});

async function fetchData() {
  loading.value = true;
  try {
    const params: any = {
      circleId: circleId.value,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    const result = await getGovernanceLogList(params);
    logs.value = result?.records || [];
    pagination.total = result?.total || 0;
  } catch {
    createMessage.error('加载失败，请重试');
  } finally {
    loading.value = false;
  }
}

function handleFilter() {
  pagination.current = 1;
  fetchData();
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  fetchData();
}

function goDetail() {
  router.push(`/circle/${circleId.value}`);
}
</script>

<style lang="less" scoped>
.circle-governance-log-page {
  min-height: calc(100vh - 64px);
  padding: 16px 24px;
  background: var(--background-color-base, #f5f5f5);
}

.log-container {
  max-width: 1000px;
  margin: 0 auto;
  background: var(--component-background, #fff);
  border-radius: 12px;
  padding: 24px;
}

.log-header {
  margin-bottom: 20px;

  .log-title {
    font-size: 20px;
    font-weight: 600;
    margin: 8px 0 0;
  }
}

.log-filters {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .circle-governance-log-page {
    padding: 8px;
  }

  .log-container {
    padding: 16px;
    border-radius: 8px;
  }

  :deep(.ant-table) {
    font-size: 13px;
  }
}
</style>
