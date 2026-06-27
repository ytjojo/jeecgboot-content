<template>
  <div class="ranking-list">
    <a-empty v-if="list.length === 0" description="暂无数据" />
    <a-list v-else item-layout="horizontal" :data-source="list">
      <template #renderItem="{ item, index }">
        <a-list-item
          class="ranking-item"
          :data-expose-id="item.circleId"
          data-expose-type="CIRCLE"
          @click="handleClick(item)"
        >
          <div class="ranking-badge" :class="getRankClass(index)">
            <span v-if="index < 3" class="medal">{{ ['🥇', '🥈', '🥉'][index] }}</span>
            <span v-else class="rank-num">{{ index + 1 }}</span>
          </div>
          <div class="ranking-content">
            <div class="ranking-header">
              <h4 class="ranking-name">{{ item.circleName }}</h4>
              <a-tag v-if="item.category" size="small" color="blue">{{ item.category }}</a-tag>
            </div>
            <p class="ranking-desc">{{ item.description }}</p>
            <div class="ranking-meta">
              <span class="member-count">{{ item.memberCount }} 成员</span>
              <span v-if="type === 'new'" class="create-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';
import type { CircleRankingItem } from '/@/api/content/model/circleAnalyticsModel';

const props = defineProps<{
  list: CircleRankingItem[];
  type: 'hot' | 'new';
}>();

const emit = defineEmits<{
  itemClick: [item: CircleRankingItem];
}>();

const router = useRouter();

function getRankClass(index: number) {
  if (index === 0) return 'rank-gold';
  if (index === 1) return 'rank-silver';
  if (index === 2) return 'rank-bronze';
  return 'rank-normal';
}

function formatTime(time: string) {
  if (!time) return '';
  return time.slice(0, 10);
}

function handleClick(item: CircleRankingItem) {
  emit('itemClick', item);
  router.push(`/circle/${item.circleId}`);
}
</script>

<style lang="less" scoped>
.ranking-list {
  background: var(--component-background);
  border-radius: 12px;
  overflow: hidden;
}

.ranking-item {
  cursor: pointer;
  padding: 12px 16px;
  transition: background 0.2s;
  display: flex;
  align-items: flex-start;
  gap: 12px;

  &:hover {
    background: var(--item-hover-bg, #f5f5f5);
  }
}

.ranking-badge {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 600;

  &.rank-gold {
    .medal {
      font-size: 24px;
    }
  }

  &.rank-silver {
    .medal {
      font-size: 24px;
    }
  }

  &.rank-bronze {
    .medal {
      font-size: 24px;
    }
  }

  &.rank-normal {
    .rank-num {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: var(--background-color-base, #f0f0f0);
      color: var(--text-color-secondary, #999);
      font-size: 13px;
    }
  }
}

.ranking-content {
  flex: 1;
  min-width: 0;
}

.ranking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.ranking-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: var(--text-color, #333);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-desc {
  font-size: 13px;
  color: var(--text-color-secondary, #666);
  margin: 0 0 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ranking-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-color-tertiary, #999);
}
</style>
