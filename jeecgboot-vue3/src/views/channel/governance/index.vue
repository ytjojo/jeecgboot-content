<template>
  <PageWrapper dense contentFullHeight>
    <Tabs v-model:activeKey="activeTab">
      <Tabs.TabPane key="review">
        <template #tab>
          <a-badge :count="pendingCount" :overflow-count="99" :offset="[6, 0]">
            <span>待审区</span>
          </a-badge>
        </template>
        <ReviewQueue v-if="activeTab === 'review'" :channel-id="channelId" />
      </Tabs.TabPane>
      <Tabs.TabPane key="content" tab="内容管理">
        <ContentManage v-if="activeTab === 'content'" :channel-id="channelId" @go-recycle-bin="activeTab = 'recycle'" @go-log="activeTab = 'log'" />
      </Tabs.TabPane>
      <Tabs.TabPane key="recycle" tab="回收站">
        <RecycleBin v-if="activeTab === 'recycle'" :channel-id="channelId" />
      </Tabs.TabPane>
      <Tabs.TabPane key="log" tab="治理日志">
        <GovernanceLog v-if="activeTab === 'log'" :channel-id="channelId" />
      </Tabs.TabPane>
      <Tabs.TabPane key="announcement" tab="公告管理">
        <AnnouncementManage v-if="activeTab === 'announcement'" :channel-id="channelId" />
      </Tabs.TabPane>
    </Tabs>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue';
import { Tabs, Badge } from 'ant-design-vue';
import { PageWrapper } from '/@/components/Page';
import { useRoute } from 'vue-router';
import { useChannelReviewStore } from '/@/store/modules/channelReview';
import ReviewQueue from './ReviewQueue.vue';
import ContentManage from './ContentManage.vue';
import RecycleBin from './RecycleBin.vue';
import GovernanceLog from './GovernanceLog.vue';
import AnnouncementManage from './AnnouncementManage.vue';

const route = useRoute();
const reviewStore = useChannelReviewStore();
const channelId = computed(() => (route.params.id as string) || (route.params.channelId as string) || (route.query.channelId as string) || '');
const activeTab = ref('review');
const pendingCount = ref(0);

async function refreshPendingCount() {
  if (!channelId.value) return;
  try {
    await reviewStore.fetchStats(channelId.value);
    pendingCount.value = reviewStore.stats.total ?? 0;
  } catch {
    pendingCount.value = 0;
  }
}

watch(channelId, () => {
  if (channelId.value) refreshPendingCount();
});

onMounted(() => {
  if (channelId.value) refreshPendingCount();
});
</script>
