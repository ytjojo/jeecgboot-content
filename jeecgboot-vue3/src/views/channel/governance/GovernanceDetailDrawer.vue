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
  import { getActionColor, getActionText } from './constants';

  const visible = ref(false);
  const record = ref<any>(null);

  function open(data: any) {
    record.value = data;
    visible.value = true;
  }

  defineExpose({ open });
</script>
