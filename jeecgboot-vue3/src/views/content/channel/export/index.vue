<template>
  <div class="channel-export-page">
    <h2 class="channel-export-page__title">数据导出</h2>
    <a-row :gutter="16">
      <!-- 导出配置 -->
      <a-col :xs="24" :md="12">
        <a-card title="新建导出" size="small">
          <a-form layout="vertical">
            <a-form-item label="导出格式" required>
              <a-radio-group v-model:value="exportFormat">
                <a-radio value="EXCEL">Excel (.xlsx)</a-radio>
                <a-radio value="CSV">CSV (.csv)</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="时间范围">
              <a-range-picker v-model:value="dateRange" style="width: 100%" />
            </a-form-item>
            <a-form-item label="导出字段">
              <a-checkbox-group v-model:value="selectedFields">
                <a-row :gutter="[8, 8]">
                  <a-col :span="8" v-for="f in exportFields" :key="f.value">
                    <a-checkbox :value="f.value">{{ f.label }}</a-checkbox>
                  </a-col>
                </a-row>
              </a-checkbox-group>
              <div class="channel-export-page__field-actions">
                <a-button size="small" type="link" @click="selectAll">全选</a-button>
                <a-button size="small" type="link" @click="clearAll">清空</a-button>
              </div>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="store.submitting" @click="handleExport" :disabled="selectedFields.length === 0">
                开始导出
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <!-- 导出历史 -->
      <a-col :xs="24" :md="12">
        <a-card title="导出历史" size="small">
          <a-table
            :columns="historyColumns"
            :dataSource="store.exportHistory"
            :loading="store.loading"
            :pagination="{ total: store.historyTotal, pageSize: 20, onChange: handlePageChange }"
            size="small"
            rowKey="taskId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag v-if="record.status === 'completed'" color="success">已完成</a-tag>
                <a-tag v-else-if="record.status === 'processing'" color="processing">
                  <LoadingOutlined spin /> 处理中
                </a-tag>
                <a-tag v-else-if="record.status === 'failed'" color="error">失败</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <template v-if="record.status === 'completed'">
                  <a-button
                    v-if="!isExpired(record.expiredAt)"
                    size="small"
                    type="link"
                    @click="handleDownload(record)"
                  >
                    下载
                  </a-button>
                  <a-tooltip v-else title="文件已过期">
                    <a-button size="small" type="link" disabled>已过期</a-button>
                  </a-tooltip>
                </template>
                <a-button
                  v-else-if="record.status === 'failed'"
                  size="small"
                  type="link"
                  @click="handleRetry(record)"
                >
                  重试
                </a-button>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { useChannelExportStore } from '/@/store/modules/channelExport';
import dayjs from 'dayjs';

const route = useRoute();
const store = useChannelExportStore();
const { createMessage } = useMessage();

const exportFormat = ref<'EXCEL' | 'CSV'>('EXCEL');
const dateRange = ref<any>(null);
const selectedFields = ref<string[]>([]);

const exportFields = [
  { label: '频道名称', value: 'channelName' },
  { label: '订阅数', value: 'subscriberCount' },
  { label: '内容数', value: 'contentCount' },
  { label: 'PV', value: 'pv' },
  { label: 'UV', value: 'uv' },
  { label: '互动数', value: 'interactionCount' },
  { label: '发布时间', value: 'publishTime' },
  { label: '内容类型', value: 'contentType' },
  { label: '创建时间', value: 'createTime' },
];

const historyColumns = [
  { title: '导出时间', dataIndex: 'exportTime', width: 160 },
  { title: '格式', dataIndex: 'format', width: 60 },
  { title: '行数', dataIndex: 'rowCount', width: 80 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 80 },
];

function selectAll() { selectedFields.value = exportFields.map((f) => f.value); }
function clearAll() { selectedFields.value = []; }

function isExpired(expiredAt?: string): boolean {
  if (!expiredAt) return false;
  return dayjs(expiredAt).isBefore(dayjs());
}

async function handleExport() {
  const task = await store.submitExport({
    channelId: (route.query.channelId as string) || '',
    fields: selectedFields.value,
    format: exportFormat.value,
    timeRange: {
      start: dateRange.value?.[0]?.toISOString?.() || '',
      end: dateRange.value?.[1]?.toISOString?.() || '',
    },
  });
  if (task) {
    createMessage.success('导出任务已提交');
  }
  // 防重复提交：按钮在 store.submitting 期间已禁用
}

async function handleDownload(record: any) {
  const blob = await store.downloadFile(record.taskId);
  if (blob) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `export-${record.taskId}.${record.format === 'EXCEL' ? 'xlsx' : 'csv'}`;
    a.click();
    URL.revokeObjectURL(url);
  }
}

async function handleRetry(record: any) {
  // 重新导入相同的配置
  await store.submitExport({
    channelId: (route.query.channelId as string) || '',
    fields: selectedFields.value,
    format: record.format,
    timeRange: record.timeRange,
  });
}

function handlePageChange(page: number) {
  store.fetchHistory({ current: page });
}

onMounted(() => {
  store.setChannelId((route.query.channelId as string) || '');
  store.fetchHistory();
  // 启动轮询
  store.startPolling();
  // 默认全选
  selectedFields.value = exportFields.map((f) => f.value);
});

onUnmounted(() => {
  store.clearPolling();
});
</script>

<style lang="less" scoped>
.channel-export-page {
  padding: 16px;

  &__title {
    margin: 0 0 16px;
    font-size: 20px;
    font-weight: 600;
  }

  &__field-actions {
    margin-top: 4px;
  }
}
</style>
