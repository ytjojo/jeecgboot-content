<!-- jeecgboot-vue3/src/views/channel/subscription/SubscriptionList.vue -->
<template>
  <div class="subscription-list">
    <div class="page-header">
      <h3>我的订阅</h3>
      <Input.Search v-model:value="searchKeyword" placeholder="搜索频道" style="width: 240px" />
    </div>

    <div class="group-tabs">
      <Tabs v-model:activeKey="activeGroup" @change="loadData">
        <Tabs.TabPane key="all" tab="全部" />
        <Tabs.TabPane key="default" tab="默认分组" />
        <Tabs.TabPane v-for="group in groups" :key="group.id" :tab="group.name" />
      </Tabs>
      <Button type="link" size="small" @click="showCreateGroupModal = true">新建分组</Button>
    </div>

    <Skeleton :loading="loading" active :paragraph="{ rows: 5 }">
      <div v-if="filteredChannels.length === 0" class="empty-state">
        <Empty description="暂无订阅频道">
          <Button type="primary">去发现频道</Button>
        </Empty>
      </div>
      <div v-else class="channel-list">
        <SubscriptionCard
          v-for="channel in filteredChannels"
          :key="channel.id"
          :channel="channel"
          @toggleReminder="handleToggleReminder"
          @unsubscribe="handleUnsubscribe"
        />
      </div>
    </Skeleton>

    <!-- 新建分组 Modal -->
    <Modal v-model:open="showCreateGroupModal" title="新建分组" @ok="handleCreateGroup">
      <Input v-model:value="newGroupName" placeholder="请输入分组名称" />
    </Modal>

    <!-- 取消订阅确认 Modal -->
    <Modal v-model:open="unsubscribeModalVisible" title="确认取消订阅？" @ok="handleConfirmUnsubscribe">
      <p>取消后您将不再收到该频道的更新推送。</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { Tabs, Button, Input, Empty, Modal, Skeleton } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getSubscriptionList, getSubscriptionGroupList, createSubscriptionGroup, updateSubscriptionReminder, unsubscribeChannel } from '/@/api/content/channelSubscription';
  import SubscriptionCard from './SubscriptionCard.vue';

  const { createMessage } = useMessage();
  const loading = ref(false);
  const channels = ref<any[]>([]);
  const groups = ref<any[]>([]);
  const searchKeyword = ref('');
  const activeGroup = ref('all');

  const showCreateGroupModal = ref(false);
  const newGroupName = ref('');

  const unsubscribeModalVisible = ref(false);
  const unsubscribeTarget = ref('');

  const filteredChannels = computed(() => {
    let list = channels.value;
    if (activeGroup.value !== 'all') {
      list = list.filter((c) => c.groupId === activeGroup.value);
    }
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase();
      list = list.filter((c) => c.name.toLowerCase().includes(keyword));
    }
    return list;
  });

  async function loadData() {
    loading.value = true;
    try {
      const [subs, grps] = await Promise.all([
        getSubscriptionList({ groupId: activeGroup.value === 'all' ? undefined : activeGroup.value }),
        getSubscriptionGroupList(),
      ]);
      channels.value = subs;
      groups.value = grps;
    } finally {
      loading.value = false;
    }
  }

  async function handleToggleReminder(channelId: string, enabled: boolean) {
    await updateSubscriptionReminder({ channelId, enabled });
    createMessage.success(enabled ? '已开启提醒' : '已关闭提醒');
  }

  function handleUnsubscribe(channelId: string) {
    unsubscribeTarget.value = channelId;
    unsubscribeModalVisible.value = true;
  }

  async function handleConfirmUnsubscribe() {
    await unsubscribeChannel(unsubscribeTarget.value);
    createMessage.success('已取消订阅');
    unsubscribeModalVisible.value = false;
    loadData();
  }

  async function handleCreateGroup() {
    if (!newGroupName.value.trim()) return;
    await createSubscriptionGroup({ name: newGroupName.value });
    createMessage.success('分组已创建');
    showCreateGroupModal.value = false;
    newGroupName.value = '';
    loadData();
  }

  onMounted(loadData);
</script>

<style scoped>
.subscription-list { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.group-tabs { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.empty-state { padding: 48px 0; text-align: center; }
</style>
