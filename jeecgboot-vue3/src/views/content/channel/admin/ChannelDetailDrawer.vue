<template>
  <a-drawer
    v-model:open="visible"
    title="频道详情"
    :width="520"
    :footer="null"
  >
    <a-descriptions :column="1" bordered size="small" v-if="channel">
      <a-descriptions-item label="频道名称">{{ channel.name }}</a-descriptions-item>
      <a-descriptions-item label="频道类型">
        <ChannelTypeTag :type="channel.channelType" />
      </a-descriptions-item>
      <a-descriptions-item label="审核状态">
        <ChannelStatusTag :status="channel.status" />
      </a-descriptions-item>
      <a-descriptions-item label="简介">{{ channel.description }}</a-descriptions-item>
      <a-descriptions-item label="分类">{{ channel.categoryName }}</a-descriptions-item>
      <a-descriptions-item label="归属">{{ channel.ownerName }}</a-descriptions-item>
      <a-descriptions-item label="组织" v-if="channel.orgName">{{ channel.orgName }}</a-descriptions-item>
      <a-descriptions-item label="置顶权重">{{ channel.topWeight }}</a-descriptions-item>
      <a-descriptions-item label="频道图标">
        <a-avatar :src="channel.iconUrl" :size="48">{{ channel.name?.charAt(0) }}</a-avatar>
      </a-descriptions-item>
      <a-descriptions-item label="频道封面" v-if="channel.coverUrl">
        <img :src="channel.coverUrl" style="max-width: 200px; border-radius: 4px" />
      </a-descriptions-item>
      <a-descriptions-item label="创建时间">{{ channel.createdTime }}</a-descriptions-item>
      <a-descriptions-item label="更新时间">{{ channel.updatedTime }}</a-descriptions-item>
    </a-descriptions>
  </a-drawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import ChannelTypeTag from '/@/components/jeecg/channel/ChannelTypeTag.vue';
  import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';
  import type { ChannelVO } from '/@/api/content/channel/model/channelModel';

  const visible = ref(false);
  const channel = ref<ChannelVO | null>(null);

  function open(record: ChannelVO) {
    channel.value = record;
    visible.value = true;
  }

  defineExpose({ open });
</script>
