<template>
  <div ref="containerRef" class="recommend-list">
    <a-alert
      v-if="!store.personalizationEnabled && !store.fallbackMode && store.recommendList.length > 0"
      message="个性化推荐已关闭，为您展示热门圈子"
      type="info"
      show-icon
      class="personalization-tip"
    />
    <HotRankList v-if="store.fallbackMode" />
    <template v-else>
      <a-spin :spinning="store.loading.recommend">
        <a-empty v-if="store.recommendList.length === 0 && !store.loading.recommend" description="暂无推荐圈子" />
        <div v-else ref="listRef" class="recommend-grid">
          <div
            v-for="item in store.recommendList"
            :key="item.sourceId || item.circleId"
            class="recommend-card"
            :data-expose-id="item.circleId"
            data-expose-type="CIRCLE"
            @click="handleClick(item)"
          >
            <div class="recommend-card-body">
              <div class="recommend-card-header">
                <h4 class="recommend-card-name">{{ item.circleName }}</h4>
                <a-tag v-if="item.category" size="small" color="blue">{{ item.category }}</a-tag>
              </div>
              <p class="recommend-card-desc">{{ item.description }}</p>
              <p v-if="item.why" class="recommend-why">{{ item.why }}</p>
              <div class="recommend-card-footer">
                <span class="member-count">{{ item.memberCount }} 成员</span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';
import { useExposureTracker } from '/@/hooks/web/useExposureTracker';
import HotRankList from './HotRankList.vue';
import type { CircleRecommendItem } from '/@/api/content/model/circleAnalyticsModel';

const router = useRouter();
const store = useCircleRecommendStore();

const containerRef = ref<HTMLElement | null>(null);
const listRef = ref<HTMLElement | null>(null);

const listVersion = computed(() => store.recommendList.length);

function getCardItems(): HTMLElement[] {
  if (!listRef.value) return [];
  return Array.from(listRef.value.querySelectorAll('.recommend-card')) as HTMLElement[];
}

const { update: updateExposure } = useExposureTracker({
  containerRef,
  getItems: getCardItems,
  onExposure(items) {
    const circleIds = items.map((i) => i.id);
    if (circleIds.length > 0) {
      store.reportExposure({ circleIds, source: 'recommend' });
    }
  },
  thresholdTime: 500,
  trigger: listVersion,
});

onMounted(async () => {
  await store.fetchRecommendList();
  await nextTick();
  updateExposure();
});

function handleClick(item: CircleRecommendItem) {
  store.reportClick(item.sourceId);
  router.push(`/circle/${item.circleId}`);
}
</script>

<style lang="less" scoped>
.recommend-list {
  width: 100%;
}

.personalization-tip {
  margin-bottom: 16px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.recommend-card {
  background: var(--component-background);
  border-radius: 12px;
  border: 1px solid var(--border-color-base, #f0f0f0);
  cursor: pointer;
  transition: box-shadow 0.3s, transform 0.2s;
  overflow: hidden;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  &-body {
    padding: 16px;
  }

  &-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  &-name {
    font-size: 15px;
    font-weight: 600;
    margin: 0;
    color: var(--text-color, #333);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    min-width: 0;
  }

  &-desc {
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    margin: 0 0 6px;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 39px;
  }
}

.recommend-why {
  font-size: 12px;
  color: var(--primary-color, #1890ff);
  margin: 0 0 8px;
  line-height: 1.4;
}

.recommend-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-color-tertiary, #999);
}

.member-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .recommend-grid {
    grid-template-columns: 1fr;
  }
}
</style>
