<template>
  <div class="channel-card" :class="[`channel-card--${mode}`]">
    <!-- 排名序号（ranking 模式） -->
    <div v-if="mode === 'ranking' && showRank" class="channel-card__rank">
      <span :class="rankClass">{{ rank }}</span>
    </div>

    <div class="channel-card__body">
      <!-- 频道图标 -->
      <div class="channel-card__icon">
        <img :src="channel.iconUrl" :alt="channel.name" loading="lazy" />
      </div>

      <!-- 频道信息 -->
      <div class="channel-card__info">
        <!-- 第一行：名称 + 类型标签 -->
        <div class="channel-card__header">
          <span class="channel-card__name">
            <span v-if="mode === 'search' && highlightName" v-html="highlightName" />
            <span v-else>{{ channel.name }}</span>
          </span>
          <a-tag v-if="channelTypeLabel" :color="channelTypeColor" size="small">
            {{ channelTypeLabel }}
          </a-tag>
        </div>

        <!-- 第二行：分类 + 简介 -->
        <div class="channel-card__subtitle">
          <span class="channel-card__category">{{ channel.categoryName }}</span>
          <span v-if="channel.description" class="channel-card__desc">
            {{ channel.description }}
          </span>
        </div>

        <!-- 第三行：订阅数 + 推荐理由/匹配原因 -->
        <div class="channel-card__footer">
          <span class="channel-card__subscribers">
            {{ formatCount(channel.subscriberCount) }} 订阅
          </span>
          <span v-if="mode === 'recommend' && showReason && channel.recommendReason" class="channel-card__reason">
            {{ channel.recommendReason }}
          </span>
          <span v-if="mode === 'search' && channel.matchReason" class="channel-card__reason">
            {{ channel.matchReason }}
          </span>
        </div>
      </div>

      <!-- 操作区 -->
      <div v-if="showNotInterested || mode === 'ranking'" class="channel-card__actions">
        <a-button v-if="showNotInterested" type="link" size="small" @click.stop="handleNotInterested">
          不感兴趣
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { ChannelInfo, ChannelRecommendationVO, ChannelSearchResultVO, ChannelRankingItemVO } from '/@/api/content/model/channelDiscoveryModel';
import { formatCount } from '../utils/formatCount';
import { getRankClass } from '../utils/getRankClass';

type CardMode = 'recommend' | 'search' | 'browse' | 'ranking';

interface Props {
  channel: ChannelInfo | ChannelRecommendationVO | ChannelSearchResultVO | ChannelRankingItemVO;
  mode: CardMode;
  showReason?: boolean;
  showNotInterested?: boolean;
  showRank?: boolean;
  rank?: number;
  highlightName?: string;
  matchReason?: string;
}

const props = withDefaults(defineProps<Props>(), {
  showReason: true,
  showNotInterested: false,
  showRank: true,
  rank: 0,
});

const emit = defineEmits<{
  (e: 'not-interested', channelId: string): void;
  (e: 'click', channel: ChannelInfo): void;
}>();

const channel = computed(() => props.channel);

// 频道类型标签
const channelTypeLabel = computed(() => {
  const type = props.channel.channelType;
  const labels: Record<string, string> = {
    system: '官方',
    personal: '个人',
    organization: '组织',
  };
  return labels[type] || '';
});

const channelTypeColor = computed(() => {
  const type = props.channel.channelType;
  const colors: Record<string, string> = {
    system: 'red',
    personal: 'blue',
    organization: 'green',
  };
  return colors[type] || 'default';
});

const rankClass = computed(() => getRankClass(props.rank, 'channel-card__rank'));

function handleNotInterested() {
  emit('not-interested', props.channel.id);
}

function handleClick() {
  emit('click', props.channel as ChannelInfo);
}
</script>

<style lang="less" scoped>
.channel-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  transition: box-shadow 0.2s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  &__body {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }

  &__rank {
    margin-bottom: 8px;

    span {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border-radius: 4px;
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
      background: #f0f0f0;
      color: #333;
      font-weight: 600;
    }

    &--normal {
      color: #999;
    }
  }

  &__icon {
    flex-shrink: 0;

    img {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      object-fit: cover;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
  }

  &__name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__subtitle {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
    font-size: 12px;
    color: #999;
  }

  &__desc {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 200px;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
  }

  &__subscribers {
    color: #666;
  }

  &__reason {
    color: #1677ff;
    font-style: italic;
  }

  &__actions {
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }
}

// Browse 模式：更紧凑的布局
.channel-card--browse {
  .channel-card__body {
    align-items: center;
  }
}

// Ranking 模式：带排名
.channel-card--ranking {
  border-bottom: 1px solid #f0f0f0;
  border-radius: 0;

  &:last-child {
    border-bottom: none;
  }
}
</style>
