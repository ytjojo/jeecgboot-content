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
            @click="onRestore(item.historyId)"
          >
            恢复
          </a-button>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getHistoryList, restoreHistory } from '/@/api/content/profile';
import type { ContentUserProfileHistoryVO, HistoryType } from '/@/api/content/profile/types';

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

async function onRestore(historyId: string) {
  if (!userId.value) return;
  restoringId.value = historyId;
  try {
    await restoreHistory(userId.value, historyId);
    message.success('已恢复');
    await load();
  } catch (e: any) {
    message.error(e?.message || '恢复失败');
  } finally {
    restoringId.value = '';
  }
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
</style>
