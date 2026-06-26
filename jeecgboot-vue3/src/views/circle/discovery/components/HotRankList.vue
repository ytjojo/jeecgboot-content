<template>
  <div class="hot-rank-list">
    <a-spin :spinning="store.loading.hot">
      <RankingList v-if="store.hotRankList.length > 0" :list="store.hotRankList" type="hot" @item-click="handleItemClick" />
      <a-empty v-else description="暂无热门圈子" />
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';
import RankingList from './RankingList.vue';
import type { CircleRankingItem } from '/@/api/content/model/circleAnalyticsModel';

const emit = defineEmits<{
  itemClick: [item: CircleRankingItem];
}>();

const store = useCircleRecommendStore();

onMounted(() => {
  store.fetchHotRankList();
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
