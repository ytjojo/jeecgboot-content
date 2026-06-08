<template>
  <div class="pick-manage-page">
    <div class="pick-manage-page__header">
      <h3>编辑精选管理</h3>
      <a-button type="primary" @click="openAddDialog">
        <template #icon><PlusOutlined /></template>
        添加精选频道
      </a-button>
    </div>

    <!-- 状态筛选 -->
    <div class="pick-manage-page__filter">
      <a-radio-group v-model:value="statusFilter" size="small" @change="fetchList">
        <a-radio-button value="">全部</a-radio-button>
        <a-radio-button value="active">生效中</a-radio-button>
        <a-radio-button value="expired">已过期</a-radio-button>
        <a-radio-button value="abnormal">状态异常</a-radio-button>
      </a-radio-group>
    </div>

    <!-- PC 表格 -->
    <a-table
      v-if="!isMobile"
      :columns="columns"
      :data-source="pickList"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      size="small"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'channel'">
          <div class="pick-manage-page__channel">
            <img :src="record.channelInfo?.iconUrl" :alt="record.channelInfo?.name" />
            <span>{{ record.channelInfo?.name }}</span>
          </div>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag v-if="record.status === 'active'" color="green">生效中</a-tag>
          <a-tag v-else-if="record.status === 'expired'" color="default">已过期</a-tag>
          <a-tag v-else color="error">状态异常</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openEditDialog(record)">编辑</a-button>
            <a-popconfirm title="确认取消精选？" @confirm="handleRemove(record)">
              <a-button type="link" size="small" danger>取消精选</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 移动端卡片列表 -->
    <div v-else class="pick-manage-page__cards">
      <a-card v-for="item in pickList" :key="item.id" size="small" class="pick-manage-page__card">
        <div class="pick-manage-page__card-header">
          <img :src="item.channelInfo?.iconUrl" :alt="item.channelInfo?.name" />
          <span>{{ item.channelInfo?.name }}</span>
          <a-tag v-if="item.status === 'abnormal'" color="error" size="small">异常</a-tag>
        </div>
        <div class="pick-manage-page__card-body">
          <p>{{ item.recommendation }}</p>
          <span>{{ item.startTime }} ~ {{ item.endTime }}</span>
        </div>
        <div class="pick-manage-page__card-footer">
          <a-button type="link" size="small" @click="openEditDialog(item)">编辑</a-button>
          <a-popconfirm title="确认取消精选？" @confirm="handleRemove(item)">
            <a-button type="link" size="small" danger>取消精选</a-button>
          </a-popconfirm>
        </div>
      </a-card>
    </div>

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:open="dialogVisible"
      :title="editingPick ? '编辑精选' : '添加精选频道'"
      @ok="handleSubmit"
      :confirm-loading="submitLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="精选频道" required>
          <a-select
            v-if="!editingPick"
            v-model:value="formData.channelId"
            show-search
            placeholder="搜索并选择频道"
            :filter-option="false"
            @search="handleChannelSearch"
          >
            <a-select-option v-for="ch in searchedChannels" :key="ch.id" :value="ch.id">
              {{ ch.name }}
            </a-select-option>
          </a-select>
          <a-input v-else :value="editingPick?.channelInfo?.name" disabled />
        </a-form-item>
        <a-form-item label="推荐语">
          <a-textarea
            v-model:value="formData.recommendation"
            placeholder="请输入推荐语"
            :maxlength="200"
            :rows="3"
            show-count
          />
        </a-form-item>
        <a-form-item label="有效期">
          <a-range-picker
            v-model:value="dateRange"
            show-time
            format="YYYY-MM-DD HH:mm"
            :placeholder="['开始时间', '结束时间']"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import {
  getEditorialPickList,
  createEditorialPick,
  updateEditorialPick,
  removeEditorialPick,
} from '/@/api/content/channelDiscovery';
import { searchChannels } from '/@/api/content/channelDiscovery';
import type { ChannelEditorialPickVO, EditorialPickFormData, ChannelInfo } from '/@/api/content/model/channelDiscoveryModel';
import { useIsMobile } from '../utils/useIsMobile';
import dayjs from 'dayjs';

const pickList = ref<ChannelEditorialPickVO[]>([]);
const loading = ref(false);
const statusFilter = ref('');
const dialogVisible = ref(false);
const submitLoading = ref(false);
const editingPick = ref<ChannelEditorialPickVO | null>(null);
const formData = ref<EditorialPickFormData>({
  channelId: '',
  recommendation: '',
  startTime: '',
  endTime: '',
});
const dateRange = ref<any[]>([]);
const searchedChannels = ref<ChannelInfo[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const { isMobile } = useIsMobile();

const columns = [
  { title: '频道', key: 'channel' },
  { title: '推荐语', dataIndex: 'recommendation', ellipsis: true },
  { title: '有效期', key: 'period' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 150 },
];

onMounted(() => {
  fetchList();
});

async function fetchList() {
  loading.value = true;
  try {
    let data = await getEditorialPickList();
    data = data || [];
    if (statusFilter.value) {
      data = data.filter((item) => item.status === statusFilter.value);
    }
    pickList.value = data;
    pagination.total = data.length;
  } finally {
    loading.value = false;
  }
}

function openAddDialog() {
  editingPick.value = null;
  formData.value = { channelId: '', recommendation: '', startTime: '', endTime: '' };
  dateRange.value = [];
  searchedChannels.value = [];
  dialogVisible.value = true;
}

function openEditDialog(pick: ChannelEditorialPickVO) {
  editingPick.value = pick;
  formData.value = {
    channelId: pick.channelId,
    recommendation: pick.recommendation,
    startTime: pick.startTime,
    endTime: pick.endTime,
  };
  dateRange.value = [
    dayjs(pick.startTime),
    dayjs(pick.endTime),
  ];
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formData.value.channelId) {
    message.warning('请选择频道');
    return;
  }
  if (dateRange.value?.length === 2) {
    formData.value.startTime = dateRange.value[0].format('YYYY-MM-DD HH:mm:ss');
    formData.value.endTime = dateRange.value[1].format('YYYY-MM-DD HH:mm:ss');
  }

  submitLoading.value = true;
  try {
    if (editingPick.value) {
      await updateEditorialPick({ id: editingPick.value.id, ...formData.value });
      message.success('编辑成功');
    } else {
      await createEditorialPick(formData.value);
      message.success('添加成功');
    }
    dialogVisible.value = false;
    await fetchList();
  } catch {
    message.error('操作失败');
  } finally {
    submitLoading.value = false;
  }
}

async function handleRemove(pick: ChannelEditorialPickVO) {
  try {
    await removeEditorialPick(pick.id);
    message.success('已取消精选');
    await fetchList();
  } catch {
    message.error('操作失败');
  }
}

async function handleChannelSearch(keyword: string) {
  if (!keyword.trim()) {
    searchedChannels.value = [];
    return;
  }
  const result = await searchChannels({ keyword, page: 1, pageSize: 10 });
  searchedChannels.value = result.records || [];
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
}
</script>

<style lang="less" scoped>
.pick-manage-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
    }
  }

  &__filter {
    margin-bottom: 16px;
  }

  &__channel {
    display: flex;
    align-items: center;
    gap: 8px;

    img {
      width: 32px;
      height: 32px;
      border-radius: 6px;
    }
  }

  &__cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  &__card-header {
    display: flex;
    align-items: center;
    gap: 8px;

    img {
      width: 32px;
      height: 32px;
      border-radius: 6px;
    }
  }

  &__card-body {
    margin: 8px 0;
    font-size: 13px;
    color: #666;
  }

  &__card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>
