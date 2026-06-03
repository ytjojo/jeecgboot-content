<template>
  <div class="profile-history">
    <a-page-header title="历史记录" :back-icon="true" @back="$router.back()" />

    <a-tabs v-model:active-key="activeTab" @change="onTabChange">
      <a-tab-pane key="NICKNAME" tab="昵称历史" />
      <a-tab-pane key="AVATAR" tab="头像历史" />
    </a-tabs>

    <a-list
      :data-source="records"
      :loading="loading"
      :pagination="{ pageSize: 10, showSizeChanger: false }"
      :locale="{ emptyText: '暂无记录' }"
    >
      <template #renderItem="{ item }">
        <a-list-item class="profile-history__item">
          <a-list-item-meta>
            <template #title>
              <template v-if="item.historyType === 'AVATAR'">
                <a-avatar :size="48" :src="item.historyValue" />
              </template>
              <template v-else>
                <span class="profile-history__nickname">{{ item.historyValue }}</span>
              </template>
            </template>
            <template #description>
              修改于 {{ formatTime(item.changedAt) }} · 过期于 {{ formatTime(item.expiresAt) }}
            </template>
          </a-list-item-meta>
          <a-button
            size="small"
            type="link"
            :loading="restoringId === item.historyId"
            @click="onRestore(item)"
          >
            恢复
          </a-button>
        </a-list-item>
      </template>
    </a-list>

    <p class="profile-history__footer">最多保留 20 条记录，保留期限 180 天</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { getHistoryList, restoreHistory } from '/@/api/content/profile';
import type { ContentUserProfileHistoryVO, HistoryType } from '/@/api/content/profile/types';
import { buildRestoreConfirmOptions } from '/@/views/content/profile/components/restoreConfirm';

const activeTab = ref<HistoryType>('NICKNAME');
const records = ref<ContentUserProfileHistoryVO[]>([]);
const loading = ref(false);
const restoringId = ref<string>('');
const userId = ref<string>('');

onMounted(async () => {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  const uid = (userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '';
  if (!uid) {
    message.error('未识别当前用户');
    return;
  }
  userId.value = uid;
  await load();
});

async function load() {
  if (!userId.value) return;
  loading.value = true;
  try {
    const res = await getHistoryList(userId.value, activeTab.value);
    records.value = res || [];
  } catch (e: any) {
    message.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

async function onTabChange() {
  await load();
}

function formatTime(iso?: string): string {
  if (!iso) return '-';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleString('zh-CN', { hour12: false });
}

async function onRestore(item: ContentUserProfileHistoryVO) {
  if (!userId.value) return;
  const opts = buildRestoreConfirmOptions(activeTab.value);
  Modal.confirm({
    ...opts,
    onOk: async () => {
      restoringId.value = item.historyId;
      try {
        await restoreHistory(userId.value, item.historyId);
        message.success('已恢复');
        await load();
      } catch (e: any) {
        message.error(e?.message || '恢复失败');
      } finally {
        restoringId.value = '';
      }
    },
  });
}
</script>

<style scoped>
.profile-history__item {
  padding: 12px 0;
}
.profile-history__nickname {
  font-size: 16px;
  font-weight: 500;
}
.profile-history__footer {
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
  margin-top: 16px;
  padding: 8px 0;
}
@media (max-width: 768px) {
  .profile-history :deep(.ant-list-item) {
    flex-wrap: wrap;
  }
  .profile-history :deep(.ant-list-item-action) {
    margin-left: 0;
    margin-top: 8px;
  }
}
</style>
