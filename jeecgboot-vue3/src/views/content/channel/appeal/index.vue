<template>
  <div class="appeal-page">
    <h2 class="appeal-page__title">申诉管理</h2>

    <!-- 筛选条件 -->
    <a-card size="small" class="appeal-page__filter">
      <a-form layout="inline">
        <a-form-item label="申诉状态">
          <a-select v-model:value="filters.status" allowClear placeholder="全部" style="width: 140px">
            <a-select-option value="submitted">处理中</a-select-option>
            <a-select-option value="resolved_restore">已恢复</a-select-option>
            <a-select-option value="resolved_maintain">维持原判</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="频道名称">
          <a-input v-model:value="filters.channelName" allowClear placeholder="搜索频道" />
        </a-form-item>
        <a-form-item label="提交时间">
          <a-range-picker v-model:value="dateRange" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 申诉列表 -->
    <a-card size="small">
      <a-table
        :columns="columns"
        :dataSource="appealList"
        :loading="loading"
        :pagination="{ total, pageSize, current: currentPage, onChange: handlePageChange }"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <StatusTag :status="record.status" size="small" />
            <a-tag v-if="record.isTimeout" color="error" size="small" style="margin-left: 4px">超时</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="handleViewDetail(record)">查看</a-button>
              <a-button
                v-if="record.status === 'submitted'"
                size="small"
                type="primary"
                @click="handleOpenProcess(record)"
              >
                处理
              </a-button>
            </a-space>
          </template>
        </template>
        <template #emptyText>
          <a-empty description="暂无申诉记录" />
        </template>
      </a-table>
    </a-card>

    <!-- 申诉详情 Drawer -->
    <a-drawer
      v-model:open="detailVisible"
      title="申诉详情"
      :width="600"
    >
      <a-descriptions :column="1" bordered size="small" v-if="currentDetail">
        <a-descriptions-item label="申诉编号">{{ currentDetail.appealNo }}</a-descriptions-item>
        <a-descriptions-item label="频道名称">{{ currentDetail.channelName }}</a-descriptions-item>
        <a-descriptions-item label="处罚类型">{{ currentDetail.penaltyType }}</a-descriptions-item>
        <a-descriptions-item label="处罚说明">{{ currentDetail.penaltyInfo }}</a-descriptions-item>
        <a-descriptions-item label="申诉说明">{{ currentDetail.appealExplain }}</a-descriptions-item>
        <a-descriptions-item label="申诉人">{{ currentDetail.appellant }}</a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ currentDetail.submitTime }}</a-descriptions-item>
      </a-descriptions>

      <!-- 补充材料 -->
      <div class="appeal-page__materials" v-if="currentDetail?.supplementaryMaterial?.length">
        <h4>补充材料</h4>
        <a-space>
          <a-image
            v-for="(url, idx) in currentDetail.supplementaryMaterial"
            :key="idx"
            :src="url"
            width="120"
          />
        </a-space>
      </div>

      <!-- 历史处理记录 -->
      <div class="appeal-page__history" v-if="currentDetail?.historyRecords?.length">
        <h4>处理记录</h4>
        <a-timeline>
          <a-timeline-item v-for="record in currentDetail.historyRecords" :key="record.time">
            {{ record.time }} - {{ record.action }}（{{ record.operator }}）: {{ record.result }}
          </a-timeline-item>
        </a-timeline>
      </div>
    </a-drawer>

    <!-- 处理弹窗 -->
    <a-modal
      v-model:open="processVisible"
      title="处理申诉"
      :confirmLoading="processLoading"
      @ok="handleProcessConfirm"
    >
      <a-form-item label="处理结果" required>
        <a-radio-group v-model:value="processResult">
          <a-radio value="RESTORE">恢复状态</a-radio>
          <a-radio value="MAINTAIN">维持原处理</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="处理说明" required>
        <a-textarea
          v-model:value="processNote"
          placeholder="请输入处理说明"
          :rows="3"
          :maxlength="300"
        />
      </a-form-item>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import StatusTag from '/@/views/content/channel/components/StatusTag.vue';
import {
  getAppealList,
  getAppealDetail,
  handleAppeal,
} from '/@/api/content/channel/appeal';
import type { AppealVO, AppealDetailVO, AppealListQuery } from '/@/api/content/channel/appeal';
import { useChannelActionSync } from '/@/hooks/channel/useChannelActionSync';

const { createMessage } = useMessage();
const { afterAppealAction } = useChannelActionSync();

const appealList = ref<AppealVO[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(20);
const currentPage = ref(1);
const dateRange = ref<any>(null);

const detailVisible = ref(false);
const currentDetail = ref<AppealDetailVO | null>(null);

const processVisible = ref(false);
const processLoading = ref(false);
const processResult = ref<'RESTORE' | 'MAINTAIN'>('RESTORE');
const processNote = ref('');
const processingAppeal = ref<AppealVO | null>(null);

const filters = reactive<AppealListQuery>({
  status: undefined,
  channelName: undefined,
});

const columns = [
  { title: '申诉编号', dataIndex: 'appealNo', width: 130 },
  { title: '频道名称', dataIndex: 'channelName', width: 160, ellipsis: true },
  { title: '处罚类型', dataIndex: 'penaltyType', width: 100 },
  { title: '申诉人', dataIndex: 'appellant', width: 120 },
  { title: '提交时间', dataIndex: 'submitTime', width: 170 },
  { title: '状态', key: 'status', width: 140 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const },
];

async function fetchList() {
  loading.value = true;
  try {
    const res = await getAppealList({
      ...filters,
      submitTimeStart: dateRange.value?.[0],
      submitTimeEnd: dateRange.value?.[1],
      current: currentPage.value,
      size: pageSize.value,
    });
    appealList.value = res?.records || [];
    total.value = res?.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  currentPage.value = 1;
  fetchList();
}

function handlePageChange(page: number) {
  currentPage.value = page;
  fetchList();
}

async function handleViewDetail(record: AppealVO) {
  detailVisible.value = true;
  try {
    currentDetail.value = await getAppealDetail(record.id);
  } catch {
    // 错误由 handleApiError 统一处理
  }
}

function handleOpenProcess(record: AppealVO) {
  processingAppeal.value = record;
  processResult.value = 'RESTORE';
  processNote.value = '';
  processVisible.value = true;
}

async function handleProcessConfirm() {
  if (!processingAppeal.value) return;
  processLoading.value = true;
  try {
    await handleAppeal({
      appealId: processingAppeal.value.id,
      result: processResult.value,
      note: processNote.value,
    });
    createMessage.success('处理完成');
    processVisible.value = false;
    await afterAppealAction({
      refreshAppealList: fetchList,
    });
  } catch {
    // 错误由 handleApiError 统一处理
  } finally {
    processLoading.value = false;
  }
}

onMounted(() => {
  fetchList();
});
</script>

<style lang="less" scoped>
.appeal-page {
  padding: 16px;

  &__title {
    margin: 0 0 16px;
    font-size: 20px;
    font-weight: 600;
  }

  &__filter {
    margin-bottom: 16px;
  }

  &__materials {
    margin-top: 16px;
  }

  &__history {
    margin-top: 16px;
  }
}
</style>
