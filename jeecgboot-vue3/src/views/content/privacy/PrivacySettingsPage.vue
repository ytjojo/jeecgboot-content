<template>
  <div class="privacy-settings-page">
    <a-page-header title="隐私设置" @back="router.back()" />

    <div class="settings-cards">
      <a-card hoverable class="settings-card" @click="router.push('/content/privacy/blacklist')">
        <a-card-meta title="黑名单">
          <template #description>
            <span>管理已拉黑的用户</span>
            <a-badge v-if="blockMuteStore.blacklistCount > 0" :count="blockMuteStore.blacklistCount" class="card-badge" />
          </template>
        </a-card-meta>
      </a-card>

      <a-card hoverable class="settings-card" @click="router.push('/content/privacy/mute-list')">
        <a-card-meta title="屏蔽列表">
          <template #description>
            <span>管理屏蔽的用户、话题和内容</span>
            <a-badge v-if="blockMuteStore.muteListCount > 0" :count="blockMuteStore.muteListCount" class="card-badge" />
          </template>
        </a-card-meta>
      </a-card>

      <a-card hoverable class="settings-card" @click="router.push('/content/privacy/keyword-filter')">
        <a-card-meta title="屏蔽词设置">
          <template #description>
            <span>设置关键词和正则屏蔽规则</span>
          </template>
        </a-card-meta>
      </a-card>
    </div>

    <a-collapse class="help-collapse">
      <a-collapse-panel key="help" header="拉黑与屏蔽的区别">
        <a-table :columns="helpColumns" :data-source="helpData" :pagination="false" size="small" bordered />
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useBlockMuteStore } from '/@/store/modules/blockMute';

const router = useRouter();
const blockMuteStore = useBlockMuteStore();

const helpColumns = [
  { title: '对比项', dataIndex: 'aspect', width: 120 },
  { title: '拉黑', dataIndex: 'block' },
  { title: '屏蔽', dataIndex: 'mute' },
];

const helpData = [
  { key: '1', aspect: '主页访问', block: '双方均无法访问', mute: '仍可访问对方主页' },
  { key: '2', aspect: '内容可见', block: '双方内容互不可见', mute: '仅信息流中不展示' },
  { key: '3', aspect: '关注关系', block: '自动解除', mute: '保持不变' },
  { key: '4', aspect: '对方感知', block: '可感知（无法互动）', mute: '无感知' },
  { key: '5', aspect: '可逆性', block: '需手动解除拉黑', mute: '随时取消屏蔽' },
];

async function getCurrentUserId(): Promise<string> {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
}

onMounted(async () => {
  try {
    const userId = await getCurrentUserId();
    await blockMuteStore.refreshCounts(userId);
  } catch {
    // ignore
  }
});
</script>

<style scoped>
.privacy-settings-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}
.settings-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin: 24px 0;
}
.settings-card {
  cursor: pointer;
  transition: box-shadow 0.3s;
}
.settings-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.card-badge {
  margin-left: 8px;
}
.help-collapse {
  margin-top: 24px;
}
@media (max-width: 768px) {
  .settings-cards {
    grid-template-columns: 1fr;
  }
}
</style>
