<template>
  <div class="new-rank-list">
    <a-spin :spinning="store.loading.new">
      <RankingList v-if="store.newRankList.length > 0" :list="store.newRankList" type="new" @item-click="handleItemClick" />
      <a-empty v-else description="暂无新圈子" />
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
  store.fetchNewRankList();
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
