<template>
  <div class="circle-members-page">
    <div class="members-container">
      <!-- 页面头 -->
      <div class="members-header">
        <a-button type="link" @click="goDetail">
          <ArrowLeftOutlined /> 返回圈子详情
        </a-button>
        <h2 class="members-title">成员管理</h2>
      </div>

      <!-- 筛选区 -->
      <div class="members-filters">
        <a-space wrap>
          <a-select v-model:value="filters.role" placeholder="全部角色" allow-clear style="width: 120px" @change="handleFilter">
            <a-select-option value="">全部角色</a-select-option>
            <a-select-option value="CREATOR">创建者</a-select-option>
            <a-select-option value="MODERATOR">版主</a-select-option>
            <a-select-option value="MEMBER">成员</a-select-option>
          </a-select>
          <a-select v-model:value="filters.status" placeholder="全部状态" allow-clear style="width: 120px" @change="handleFilter">
            <a-select-option value="">全部状态</a-select-option>
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="MUTED">禁言中</a-select-option>
            <a-select-option value="REMOVED">已移除</a-select-option>
          </a-select>
          <a-input-search
            v-model:value="filters.keyword"
            placeholder="搜索成员昵称..."
            style="width: 200px"
            @search="handleFilter"
          />
        </a-space>
      </div>

      <!-- 成员表格 -->
      <a-table
        :columns="columns"
        :data-source="members"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 成员信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'member'">
            <MemberAvatar :nickname="record.userId" :role="record.role" />
          </template>
          <template v-if="column.key === 'status'">
            <a-tag v-if="record.status === 'ACTIVE'" color="green">正常</a-tag>
            <a-tag v-else-if="record.status === 'MUTED'" color="orange">禁言中</a-tag>
            <a-tag v-else-if="record.status === 'REMOVED'" color="red">已移除</a-tag>
          </template>
          <template v-if="column.key === 'actions'">
            <div v-if="getActions(record).length > 0" class="action-btns">
              <template v-for="action in getActions(record)" :key="action.key">
                <a-button v-if="action.key === 'set-moderator'" type="link" size="small" @click="handleSetModerator(record)">
                  设为版主
                </a-button>
                <a-button v-else-if="action.key === 'unset-moderator'" type="link" size="small" @click="handleUnsetModerator(record)">
                  取消版主
                </a-button>
                <a-button v-else-if="action.key === 'mute'" type="link" size="small" @click="handleMute(record)">
                  禁言
                </a-button>
                <a-button v-else-if="action.key === 'unmute'" type="link" size="small" @click="handleUnmute(record)">
                  解除禁言
                </a-button>
                <a-button v-else-if="action.key === 'remove'" type="link" size="small" danger @click="handleRemove(record)">
                  移除
                </a-button>
              </template>
            </div>
            <span v-else class="no-actions">-</span>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && members.length === 0" description="暂无成员" />
    </div>

    <!-- 禁言 Modal -->
    <MuteMemberModal
      ref="muteModalRef"
      v-model:visible="showMuteModal"
      :member-name="selectedMember?.userId"
      @confirm="confirmMute"
    />

    <!-- 确认 Modal -->
    <GovernanceConfirmModal
      ref="confirmModalRef"
      v-model:visible="showConfirmModal"
      :type="confirmType"
      :member-name="selectedMember?.userId"
      @confirm="confirmAction"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeftOutlined } from '@ant-design/icons-vue';
import { getMemberList, changeMemberRole, muteMember, unmuteMember, removeMember } from '/@/api/content/circle';
import { getCircleDetail } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import { useCircleStoreWithOut } from '/@/store/modules/circle';
import type { CircleMemberVO, MemberRole } from '/@/api/content/model/circleModel';
import MemberAvatar from './components/MemberAvatar.vue';
import MuteMemberModal from './components/MuteMemberModal.vue';
import GovernanceConfirmModal from './components/GovernanceConfirmModal.vue';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();
const circleStore = useCircleStoreWithOut();

// 数据
const members = ref<CircleMemberVO[]>([]);
const loading = ref(false);
const circleId = computed(() => route.params.id as string);

// 筛选
const filters = reactive({
  role: '',
  status: '',
  keyword: '',
});

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 50,
  total: 0,
  showSizeChanger: false,
});

// 表格列
const columns = [
  { key: 'member', title: '成员', dataIndex: 'userId' },
  { key: 'status', title: '状态', dataIndex: 'status', width: 100 },
  { key: 'createTime', title: '加入时间', dataIndex: 'createTime', width: 180 },
  { key: 'actions', title: '操作', width: 200 },
];

// 当前操作
const showMuteModal = ref(false);
const showConfirmModal = ref(false);
const selectedMember = ref<CircleMemberVO | null>(null);
const confirmType = ref<'set-moderator' | 'unset-moderator' | 'remove'>('remove');
const muteModalRef = ref();
const confirmModalRef = ref();

// 获取数据
async function fetchData() {
  loading.value = true;
  try {
    const params: any = {
      circleId: circleId.value,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    if (filters.role) params.role = filters.role;
    if (filters.status) params.status = filters.status;
    if (filters.keyword) params.keyword = filters.keyword;

    const result = await getMemberList(params);
    members.value = result?.records || [];
    pagination.total = result?.total || 0;
  } catch {
    createMessage.error('加载失败，请重试');
  } finally {
    loading.value = false;
  }
}

// 筛选
function handleFilter() {
  pagination.current = 1;
  fetchData();
}

function handleTableChange(pag: any) {
  pagination.current = pag.current;
  fetchData();
}

// 权限判断
function getActions(record: CircleMemberVO) {
  const actions: { key: string; label: string }[] = [];
  const myRole = circleStore.currentRole;

  if (!myRole) return actions;

  // 创建者：所有权限
  if (myRole === 'CREATOR') {
    if (record.role === 'MEMBER') {
      actions.push({ key: 'set-moderator', label: '设为版主' });
    }
    if (record.role === 'MODERATOR') {
      actions.push({ key: 'unset-moderator', label: '取消版主' });
    }
    if (record.role !== 'CREATOR') {
      if (record.status === 'MUTED') {
        actions.push({ key: 'unmute', label: '解除禁言' });
      } else if (record.status === 'ACTIVE') {
        actions.push({ key: 'mute', label: '禁言' });
      }
      actions.push({ key: 'remove', label: '移除' });
    }
  }

  // 版主：有限权限
  if (myRole === 'MODERATOR') {
    if (record.role === 'MEMBER') {
      if (record.status === 'MUTED') {
        actions.push({ key: 'unmute', label: '解除禁言' });
      } else if (record.status === 'ACTIVE') {
        actions.push({ key: 'mute', label: '禁言' });
      }
      actions.push({ key: 'remove', label: '移除' });
    }
  }

  return actions;
}

// 禁言
function handleMute(record: CircleMemberVO) {
  selectedMember.value = record;
  showMuteModal.value = true;
}

async function confirmMute(data: { duration: string; reason: string }) {
  try {
    await muteMember({
      circleId: circleId.value,
      targetUserId: selectedMember.value!.userId,
      muteDuration: data.duration,
      reason: data.reason,
    });
    createMessage.success('已禁言');
    showMuteModal.value = false;
    fetchData();
  } catch (error: any) {
    createMessage.error(error?.message || '操作失败，该成员状态已变更');
    fetchData();
  }
}

// 解除禁言
async function handleUnmute(record: CircleMemberVO) {
  try {
    await unmuteMember({ circleId: circleId.value, targetUserId: record.userId });
    createMessage.success('已解除禁言');
    fetchData();
  } catch (error: any) {
    createMessage.error(error?.message || '操作失败');
  }
}

// 角色变更
function handleSetModerator(record: CircleMemberVO) {
  selectedMember.value = record;
  confirmType.value = 'set-moderator';
  showConfirmModal.value = true;
}

function handleUnsetModerator(record: CircleMemberVO) {
  selectedMember.value = record;
  confirmType.value = 'unset-moderator';
  showConfirmModal.value = true;
}

// 移除
function handleRemove(record: CircleMemberVO) {
  selectedMember.value = record;
  confirmType.value = 'remove';
  showConfirmModal.value = true;
}

async function confirmAction() {
  const record = selectedMember.value!;
  const role = confirmType.value === 'set-moderator' ? 'MODERATOR' : 'MEMBER';

  try {
    if (confirmType.value === 'remove') {
      await removeMember({ circleId: circleId.value, targetUserId: record.userId });
      createMessage.success('已移除');
    } else {
      await changeMemberRole({ circleId: circleId.value, targetUserId: record.userId, targetRole: role });
      createMessage.success(confirmType.value === 'set-moderator' ? '已设置为版主' : '已取消版主');
    }
    showConfirmModal.value = false;
    fetchData();
  } catch (error: any) {
    createMessage.error(error?.message || '操作失败，该成员状态已变更');
    fetchData();
  }
}

function goDetail() {
  router.push(`/circle/${circleId.value}`);
}

onMounted(() => fetchData());
</script>

<style lang="less" scoped>
.circle-members-page {
  min-height: calc(100vh - 64px);
  padding: 16px 24px;
  background: var(--background-color-base, #f5f5f5);
}

.members-container {
  max-width: 1000px;
  margin: 0 auto;
  background: var(--component-background, #fff);
  border-radius: 12px;
  padding: 24px;
}

.members-header {
  margin-bottom: 20px;

  .members-title {
    font-size: 20px;
    font-weight: 600;
    margin: 8px 0 0;
  }
}

.members-filters {
  margin-bottom: 16px;
}

.action-btns {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.no-actions {
  color: var(--text-color-tertiary, #999);
}

@media (max-width: 768px) {
  .circle-members-page {
    padding: 8px;
  }

  .members-container {
    padding: 16px;
    border-radius: 8px;
  }

  :deep(.ant-table) {
    font-size: 13px;
  }
}
</style>
