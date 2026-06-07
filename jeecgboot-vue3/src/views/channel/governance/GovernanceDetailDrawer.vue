<!-- jeecgboot-vue3/src/views/channel/governance/GovernanceDetailDrawer.vue -->
<template>
  <Drawer v-model:open="visible" title="治理详情" :width="400">
    <Descriptions :column="1" bordered>
      <Descriptions.Item label="操作类型">
        <Tag :color="getActionColor(record?.action)">{{ getActionText(record?.action) }}</Tag>
      </Descriptions.Item>
      <Descriptions.Item label="操作者">{{ record?.operatorName }}</Descriptions.Item>
      <Descriptions.Item label="目标用户">{{ record?.targetUserName }}</Descriptions.Item>
      <Descriptions.Item label="操作时间">{{ record?.createTime }}</Descriptions.Item>
      <Descriptions.Item label="原因">{{ record?.reason || '无' }}</Descriptions.Item>
      <Descriptions.Item label="操作前状态">{{ record?.beforeState }}</Descriptions.Item>
      <Descriptions.Item label="操作后状态">{{ record?.afterState }}</Descriptions.Item>
    </Descriptions>
  </Drawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Drawer, Descriptions, Tag } from 'ant-design-vue';

  const visible = ref(false);
  const record = ref<any>(null);

  function getActionColor(action: string) {
    const map: Record<string, string> = { REMOVE: 'red', MUTE: 'orange', UNMUTE: 'green', BLACKLIST_ADD: 'default', BLACKLIST_REMOVE: 'blue' };
    return map[action] || 'default';
  }

  function getActionText(action: string) {
    const map: Record<string, string> = { REMOVE: '移除', MUTE: '禁言', UNMUTE: '解除禁言', BLACKLIST_ADD: '加入黑名单', BLACKLIST_REMOVE: '移出黑名单' };
    return map[action] || action;
  }

  function open(data: any) {
    record.value = data;
    visible.value = true;
  }

  defineExpose({ open });
</script>
