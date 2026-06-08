<template>
  <div class="ranking-list">
    <!-- 维度切换 -->
    <div v-if="showDimensionSwitch" class="ranking-list__dimension">
      <a-radio-group :value="dimension" size="small" @change="handleDimensionChange">
        <a-radio-button value="day">日榜</a-radio-button>
        <a-radio-button value="week">周榜</a-radio-button>
        <a-radio-button value="month">月榜</a-radio-button>
      </a-radio-group>
    </div>

    <!-- 榜单列表 -->
    <a-spin :spinning="loading">
      <div v-if="data.length === 0 && !loading" class="ranking-list__empty">
        暂无排行数据
      </div>
      <div v-else class="ranking-list__body">
        <div
          v-for="(item, index) in data"
          :key="item.id"
          class="ranking-list__item"
        >
          <div class="ranking-list__rank">
            <span :class="rankClass(item.rank)">{{ item.rank }}</span>
          </div>
          <div class="ranking-list__icon">
            <img :src="item.iconUrl" :alt="item.name" loading="lazy" />
          </div>
          <div class="ranking-list__info">
            <div class="ranking-list__name">{{ item.name }}</div>
            <div class="ranking-list__meta">
              <span>{{ item.categoryName }}</span>
              <span>{{ formatCount(item.subscriberCount) }} 订阅</span>
            </div>
          </div>
          <div class="ranking-list__score">
            {{ item.score?.toFixed(0) || '-' }}
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 榜单更新时间 -->
    <div v-if="updateTime" class="ranking-list__footer">
      更新时间：{{ updateTime }}
      <a-button v-if="showMethodology" type="link" size="small" @click="$emit('methodology')">
        排行口径说明
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { ChannelRankingItemVO } from '/@/api/content/model/channelDiscoveryModel';
import { formatCount } from '../utils/formatCount';
import { getRankClass } from '../utils/getRankClass';

interface Props {
  data: ChannelRankingItemVO[];
  dimension?: string;
  loading?: boolean;
  showDimensionSwitch?: boolean;
  updateTime?: string;
  showMethodology?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  dimension: 'day',
  loading: false,
  showDimensionSwitch: false,
});

const emit = defineEmits<{
  (e: 'dimension-change', dimension: string): void;
  (e: 'methodology'): void;
}>();

function rankClass(rank: number): string {
  return getRankClass(rank, 'ranking-list__rank');
}

function handleDimensionChange(e: any) {
  emit('dimension-change', e.target.value);
}
</script>

<style lang="less" scoped>
.ranking-list {
  &__dimension {
    margin-bottom: 12px;
    text-align: center;
  }

  &__empty {
    text-align: center;
    padding: 48px 0;
    color: #999;
  }

  &__body {
    // 虚拟滚动在需要时启用
    max-height: 600px;
    overflow-y: auto;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-bottom: 1px solid #f5f5f5;
    transition: background 0.2s;

    &:hover {
      background: #fafafa;
    }

    &:last-child {
      border-bottom: none;
    }
  }

  &__rank {
    flex-shrink: 0;
    width: 32px;
    text-align: center;

    span {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 6px;
      font-weight: 700;
      font-size: 14px;
    }

    &--gold {
      background: linear-gradient(135deg, #ffd700, #ffaa00);
      color: #fff;
    }

    &--silver {
      background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
      color: #fff;
    }

    &--bronze {
      background: linear-gradient(135deg, #cd7f32, #a0522d);
      color: #fff;
    }

    &--prominent {
      background: #f5f5f5;
      color: #333;
      font-weight: 600;
    }

    &--normal {
      color: #999;
      font-weight: 400;
    }
  }

  &__icon {
    flex-shrink: 0;

    img {
      width: 36px;
      height: 36px;
      border-radius: 6px;
      object-fit: cover;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__meta {
    font-size: 12px;
    color: #999;
    display: flex;
    gap: 8px;
  }

  &__score {
    flex-shrink: 0;
    font-size: 14px;
    font-weight: 600;
    color: #1677ff;
    min-width: 40px;
    text-align: right;
  }

  &__footer {
    padding: 8px 12px;
    font-size: 12px;
    color: #999;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
}
</style>
