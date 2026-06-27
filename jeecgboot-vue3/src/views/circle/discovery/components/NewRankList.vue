<template>
  <div ref="containerRef" class="new-rank-list">
    <a-spin :spinning="store.loading.new">
      <RankingList v-if="store.newRankList.length > 0" :list="store.newRankList" type="new" @item-click="handleItemClick" />
      <a-empty v-else description="暂无新圈子" />
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';
import { useExposureTracker } from '/@/hooks/web/useExposureTracker';
import RankingList from './RankingList.vue';
import type { CircleRankingItem } from '/@/api/content/model/circleAnalyticsModel';

const emit = defineEmits<{
  itemClick: [item: CircleRankingItem];
}>();

const store = useCircleRecommendStore();

const containerRef = ref<HTMLElement | null>(null);

const listVersion = computed(() => store.newRankList.length);

function getRankingItems(): HTMLElement[] {
  if (!containerRef.value) return [];
  return Array.from(containerRef.value.querySelectorAll('.ranking-item')) as HTMLElement[];
}

const { update: updateExposure } = useExposureTracker({
  containerRef,
  getItems: getRankingItems,
  onExposure(items) {
    const circleIds = items.map((i) => i.id);
    if (circleIds.length > 0) {
      store.reportExposure({ circleIds, source: 'new' });
    }
  },
  thresholdTime: 500,
  trigger: listVersion,
});

onMounted(async () => {
  await store.fetchNewRankList();
  await nextTick();
  updateExposure();
});

function handleItemClick(item: CircleRankingItem) {
  emit('itemClick', item);
}
</script>

<style lang="less" scoped>
.new-rank-list {
  width: 100%;
}
</style>
