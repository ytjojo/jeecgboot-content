<template>
  <div ref="containerRef" class="hot-rank-list">
    <a-spin :spinning="store.loading.hot">
      <RankingList v-if="store.hotRankList.length > 0" :list="store.hotRankList" type="hot" @item-click="handleItemClick" />
      <a-empty v-else description="暂无热门圈子" />
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

const listVersion = computed(() => store.hotRankList.length);

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
      store.reportExposure({ circleIds, source: 'hot' });
    }
  },
  thresholdTime: 500,
  trigger: listVersion,
});

onMounted(async () => {
  await store.fetchHotRankList();
  await nextTick();
  updateExposure();
});

function handleItemClick(item: CircleRankingItem) {
  emit('itemClick', item);
}
</script>

<style lang="less" scoped>
.hot-rank-list {
  width: 100%;
}
</style>
