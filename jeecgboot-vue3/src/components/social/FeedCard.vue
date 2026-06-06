<template>
  <div
    :class="['feed-card', { 'is-mobile': isMobile, 'is-priority': feed.isPriority }]"
    @click="handleClick"
  >
    <div class="feed-card__header">
      <Avatar :size="isMobile ? 32 : 36" :src="feed.avatar" />
      <div class="feed-card__user-info">
        <span class="feed-card__nickname">{{ feed.nickname }}</span>
        <div class="feed-card__tags">
          <Tag :color="dynamicTypeColor" class="feed-card__type-tag">
            {{ dynamicTypeLabel }}
          </Tag>
          <Tag v-if="feed.sourceName" color="default" class="feed-card__source-tag">
            {{ feed.sourceName }}
          </Tag>
        </div>
      </div>
      <span v-if="feed.visibility === 'MUTUAL_FOLLOW'" class="feed-card__private-badge">
        <eye-invisible-outlined /> 仅互关可见
      </span>
      <span class="feed-card__time">{{ feed.createTime }}</span>
    </div>
    <PrivateContentGuard
      :accessible="isAccessible"
      reason="not_mutual_follow"
    >
      <div class="feed-card__content">
        <h3 class="feed-card__title">{{ feed.contentTitle }}</h3>
        <p v-if="!isMobile && feed.contentSummary" class="feed-card__summary">
          {{ feed.contentSummary }}
        </p>
      </div>
    </PrivateContentGuard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { EyeInvisibleOutlined } from '@ant-design/icons-vue';
import PrivateContentGuard from '/@/views/content/components/PrivateContentGuard.vue';
import { useMutualFollowStore } from '/@/store/modules/mutualFollow';

interface FeedProp {
  id: string;
  userId: string;
  nickname: string;
  avatar: string;
  contentId: string;
  contentTitle: string;
  contentSummary: string;
  dynamicType: 'post' | 'like' | 'favorite';
  sourceType?: string;
  sourceName?: string;
  createTime: string;
  isPriority: boolean;
  visibility?: 'PUBLIC' | 'MUTUAL_FOLLOW';
}

const props = defineProps<{
  feed: FeedProp;
  isMobile?: boolean;
}>();

const emit = defineEmits<{
  (e: 'click', feed: FeedProp): void;
}>();

const mutualFollowStore = useMutualFollowStore();

const isAccessible = computed(() => {
  if (props.feed.visibility !== 'MUTUAL_FOLLOW') return true;
  return mutualFollowStore.isMutual(props.feed.userId);
});

onMounted(() => {
  if (props.feed.visibility === 'MUTUAL_FOLLOW') {
    mutualFollowStore.fetchAndCache([props.feed.userId]);
  }
});

const dynamicTypeMap: Record<string, { label: string; color: string }> = {
  post: { label: '发帖', color: 'blue' },
  like: { label: '点赞', color: 'orange' },
  favorite: { label: '收藏', color: 'green' },
};

const dynamicTypeLabel = computed(() => dynamicTypeMap[props.feed.dynamicType]?.label || '动态');
const dynamicTypeColor = computed(() => dynamicTypeMap[props.feed.dynamicType]?.color || 'default');

function handleClick() {
  emit('click', props.feed);
}
</script>

<style scoped lang="less">
.feed-card {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  border-left: 3px solid transparent;

  &:hover {
    background-color: #fafafa;
  }

  &.is-priority {
    border-left-color: #faad14;
    background-color: #fffbe6;

    &:hover {
      background-color: #fff7cc;
    }
  }

  &__header {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }

  &__user-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__nickname {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__tags {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
  }

  &__type-tag,
  &__source-tag {
    font-size: 12px;
    line-height: 18px;
    padding: 0 4px;
  }

  &__private-badge {
    font-size: 12px;
    color: #faad14;
    display: inline-flex;
    align-items: center;
    gap: 2px;
    flex-shrink: 0;
    white-space: nowrap;
  }

  &__time {
    font-size: 12px;
    color: #999;
    flex-shrink: 0;
    white-space: nowrap;
  }

  &__content {
    margin-top: 8px;
    padding-left: 46px;
  }

  &__title {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__summary {
    font-size: 13px;
    color: #666;
    margin: 4px 0 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  &.is-mobile {
    padding: 10px 12px;

    .feed-card__header {
      gap: 8px;
    }

    .feed-card__nickname {
      font-size: 13px;
    }

    .feed-card__content {
      padding-left: 40px;
    }

    .feed-card__title {
      font-size: 13px;
    }
  }
}
</style>
