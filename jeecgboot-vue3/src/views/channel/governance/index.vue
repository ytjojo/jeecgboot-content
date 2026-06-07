<template>
  <PageWrapper dense contentFullHeight>
    <Tabs v-model:activeKey="activeTab">
      <Tabs.TabPane key="review" tab="待审区">
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
import { ref, computed } from 'vue';
import { Tabs } from 'ant-design-vue';
import { PageWrapper } from '/@/components/Page';
import { useRoute } from 'vue-router';
import ReviewQueue from './ReviewQueue.vue';
import ContentManage from './ContentManage.vue';
import RecycleBin from './RecycleBin.vue';
import GovernanceLog from './GovernanceLog.vue';
import AnnouncementManage from './AnnouncementManage.vue';

const route = useRoute();
const channelId = computed(() => (route.params.id as string) || (route.params.channelId as string) || (route.query.channelId as string) || '');
const activeTab = ref('review');
</script>
