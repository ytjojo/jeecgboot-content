<template>
  <div class="fan-dashboard">
    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <a-tab-pane key="trend" tab="粉丝趋势">
        <FanTrend :user-id="userId" @point-click="handlePointClick" />
      </a-tab-pane>
      <a-tab-pane key="profile" tab="粉丝画像">
        <FanProfile :user-id="userId" />
      </a-tab-pane>
      <a-tab-pane key="list" tab="粉丝列表">
        <FanList />
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="modalVisible"
      :title="`${clickedDate} 新增粉丝`"
      :footer="null"
      width="600px"
    >
      <a-spin :spinning="modalLoading">
        <a-list :data-source="modalFans" size="small">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #avatar>
                  <a-avatar :src="item.avatar" />
                </template>
                <template #title>{{ item.nickname }}</template>
                <template #description>关注于 {{ item.followedAt }}</template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
        <a-empty v-if="!modalLoading && modalFans.length === 0" description="当日无新增粉丝" />
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '/@/store/modules/user';
import { listFans } from '/@/api/content/fan-analytics';
import FanTrend from './FanTrend.vue';
import FanProfile from './FanProfile.vue';
import FanList from './FanList.vue';

const userStore = useUserStore();
const userId = String(userStore.getUserInfo.userId || '');

const activeTab = ref('trend');
const modalVisible = ref(false);
const modalLoading = ref(false);
const clickedDate = ref('');
const modalFans = ref<any[]>([]);

function handleTabChange() {
  // Tab switched
}

async function handlePointClick(data: { date: string; value: number }) {
  clickedDate.value = data.date;
  modalVisible.value = true;
  modalLoading.value = true;
  try {
    const res = await listFans(userId, {
      date: data.date,
      pageNo: 1,
      pageSize: 50,
    });
    modalFans.value = res.records || [];
  } catch {
    modalFans.value = [];
  } finally {
    modalLoading.value = false;
  }
}
</script>

<style scoped>
.fan-dashboard {
  padding: 16px;
}
</style>
