<template>
  <div class="invite-share">
    <div class="page-header">
      <h3>邀请好友</h3>
    </div>

    <a-spin :spinning="loading">
      <!-- Invite Code Card -->
      <a-card title="我的邀请码" size="small" style="margin-bottom: 16px">
        <div class="invite-code-row">
          <span class="invite-code">{{ inviteCode || '---' }}</span>
          <a-space>
            <a-button size="small" @click="handleCopyCode" :disabled="!inviteCode">
              复制邀请码
            </a-button>
            <a-button size="small" type="primary" @click="handleCopyLink" :disabled="!inviteCode">
              复制邀请链接
            </a-button>
          </a-space>
        </div>
        <a-button
          v-if="!inviteCode"
          type="primary"
          @click="handleGenerate"
          style="margin-top: 8px"
        >
          生成邀请码
        </a-button>
      </a-card>

      <!-- Stats Card -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="8">
          <a-statistic title="邀请人数" :value="stats.totalInvited || 0" />
        </a-col>
        <a-col :span="8">
          <a-statistic title="获得积分" :value="stats.totalReward || 0" />
        </a-col>
        <a-col :span="8">
          <a-statistic title="待发放积分" :value="stats.pendingReward || 0" />
        </a-col>
      </a-row>

      <!-- Invite Records -->
      <a-card title="邀请记录" size="small">
        <a-list
          :data-source="records"
          :pagination="pagination"
          @change="handlePageChange"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #avatar>
                  <a-avatar :src="item.inviteeAvatar" />
                </template>
                <template #title>{{ item.inviteeNickname || '新用户' }}</template>
                <template #description>注册于 {{ item.registeredAt }}</template>
              </a-list-item-meta>
              <template #actions>
                <a-tag :color="item.rewardStatus === 'PAID' ? 'green' : 'orange'">
                  {{ item.rewardStatus === 'PAID' ? '已发放' : '待发放' }}
                </a-tag>
              </template>
            </a-list-item>
          </template>
        </a-list>
        <a-empty v-if="!loading && records.length === 0" description="暂无邀请记录" />
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { generateInviteCode, listInviteRecords, getInviteStats } from '/@/api/content/invite';
import { useMessage } from '/@/hooks/web/useMessage';

const { createMessage } = useMessage();
const loading = ref(false);
const inviteCode = ref('');
const stats = reactive<any>({ totalInvited: 0, totalReward: 0, pendingReward: 0 });
const records = ref<any[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
});

const userId = ''; // TODO: get from user store

const fetchData = async () => {
  loading.value = true;
  try {
    const [statsRes, recordsRes] = await Promise.all([
      getInviteStats(userId),
      listInviteRecords(userId, {
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      }),
    ]);
    if (statsRes) {
      inviteCode.value = statsRes.inviteCode || '';
      stats.totalInvited = statsRes.totalInvited || 0;
      stats.totalReward = statsRes.totalReward || 0;
      stats.pendingReward = statsRes.pendingReward || 0;
    }
    records.value = recordsRes?.records || [];
    pagination.total = recordsRes?.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleGenerate = async () => {
  try {
    const res = await generateInviteCode(userId);
    inviteCode.value = res?.inviteCode || '';
    createMessage.success('邀请码生成成功');
  } catch {
    createMessage.error('邀请码生成失败');
  }
};

const handleCopyCode = () => {
  navigator.clipboard.writeText(inviteCode.value).then(() => {
    createMessage.success('邀请码已复制');
  });
};

const handleCopyLink = () => {
  const link = `${window.location.origin}/register?inviteCode=${inviteCode.value}`;
  navigator.clipboard.writeText(link).then(() => {
    createMessage.success('邀请链接已复制');
  });
};

const handlePageChange = (page: any) => {
  pagination.current = page.current;
  pagination.pageSize = page.pageSize;
  fetchData();
};

onMounted(fetchData);
</script>

<style scoped>
.invite-share {
  padding: 16px;
}
.page-header {
  margin-bottom: 16px;
}
.invite-code-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.invite-code {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 4px;
  color: #1890ff;
}
</style>
