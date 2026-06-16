<template>
  <div class="circle-content-card">
    <!-- 置顶/精华标识 -->
    <div v-if="content.isPinned || content.isFeatured" class="content-badges">
      <a-tag v-if="content.isPinned" color="blue">置顶</a-tag>
      <a-tag v-if="content.isFeatured" color="gold">精华</a-tag>
    </div>
    <div class="content-body">
      <div class="content-title">{{ content.title }}</div>
      <div class="content-meta">
        <span>{{ content.author }}</span>
        <span>{{ content.publishTime }}</span>
      </div>
    </div>
    <div class="content-actions" v-if="isAdmin">
      <GovernanceActionMenu
        :is-pinned="!!content.isPinned"
        :is-featured="!!content.isFeatured"
        @action="$emit('governanceAction', $event, content.id)"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import GovernanceActionMenu from '/@/views/channel/components/GovernanceActionMenu.vue';
import { useCircleStoreWithOut } from '/@/store/modules/circle';

export interface CircleContentItem {
  id: string;
  title: string;
  contentType?: string;
  author?: string;
  publishTime?: string;
  isPinned?: boolean;
  isFeatured?: boolean;
}

const props = defineProps<{
  content: CircleContentItem;
}>();

defineEmits<{
  governanceAction: [action: string, contentId: string];
}>();

const circleStore = useCircleStoreWithOut();
const isAdmin = computed(() => circleStore.isCreator || circleStore.isModerator);
</script>

<style lang="less" scoped>
.circle-content-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 10px;
  border: 1px solid #f0f0f0;
  display: flex;
  align-items: flex-start;
  gap: 12px;

  .content-badges {
    display: flex;
    gap: 4px;
    flex-shrink: 0;
  }
  .content-body {
    flex: 1;
    min-width: 0;
  }
  .content-title {
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .content-meta {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #999;
  }
  .content-actions {
    flex-shrink: 0;
  }
}
</style>
