<template>
  <div class="leaderboard-list">
    <!-- 加载骨架屏 -->
    <div v-if="loading" class="skeleton-wrapper">
      <a-skeleton active :paragraph="{ rows: 10 }" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="!entries || entries.length === 0" class="empty-wrapper">
      <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无排行数据">
        <div class="empty-actions">
          <a-button type="primary" @click="$emit('go-post')">去发帖</a-button>
          <a-button style="margin-left: 8px" @click="$emit('go-comment')">去评论</a-button>
        </div>
      </a-empty>
    </div>

    <!-- 排行榜列表 -->
    <div v-else class="list-content">
      <div class="list-header">
        <span class="rank-col">排名</span>
        <span class="user-col">用户</span>
        <span class="score-col">{{ scoreLabel }}</span>
      </div>

      <div class="list-body">
        <div
          v-for="(entry, index) in displayedEntries"
          :key="entry.userId"
          :class="['list-item', { 'is-highlighted': entry.highlighted, 'is-top-three': index < 3 }]"
        >
          <span class="rank-col">
            <span :class="['rank-badge', `rank-${index + 1}`]" v-if="index < 3">
              {{ index + 1 }}
            </span>
            <span v-else class="rank-num">{{ entry.rankNum }}</span>
          </span>
          <span class="user-col" @click="handleUserClick(entry.userId)">
            <a-avatar :src="entry.avatar" :size="36" class="user-avatar">
              {{ entry.username?.charAt(0) }}
            </a-avatar>
            <span class="username">{{ entry.username }}</span>
          </span>
          <span class="score-col">{{ entry.score }}</span>
        </div>
      </div>

      <!-- 我的排名（不在 Top 50 时显示） -->
      <div v-if="showCurrentUserOutside" class="current-user-section">
        <div class="divider"></div>
        <div class="current-user-item is-highlighted">
          <span class="rank-col">
            <span class="rank-num">{{ currentUser?.rankNum }}</span>
          </span>
          <span class="user-col" @click="handleUserClick(currentUser!.userId)">
            <a-avatar :src="currentUser?.avatar" :size="36" class="user-avatar">
              {{ currentUser?.username?.charAt(0) }}
            </a-avatar>
            <span class="username">{{ currentUser?.username }}</span>
          </span>
          <span class="score-col">
            <div class="score-wrapper">
              <span>{{ currentUser?.score }}</span>
              <span class="gap-text" v-if="currentUser?.gap && currentUser.gap > 0">
                距上一名差 {{ currentUser.gap }}
              </span>
            </div>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { Empty } from 'ant-design-vue';
  import type { LeaderboardEntryVO } from '/@/api/content/circle/growth';

  const props = defineProps<{
    entries: LeaderboardEntryVO[];
    currentUser: LeaderboardEntryVO | null;
    loading: boolean;
    dimension: 'experience' | 'contribution' | 'posts';
  }>();

  const emit = defineEmits<{
    userClick: [userId: string];
    'go-post': [];
    'go-comment': [];
  }>();

  const MAX_DISPLAY = 50;

  const displayedEntries = computed(() => {
    return (props.entries || []).slice(0, MAX_DISPLAY);
  });

  const hasHighlightedInList = computed(() => {
    return displayedEntries.value.some((entry) => entry.highlighted);
  });

  const showCurrentUserOutside = computed(() => {
    return props.currentUser && !hasHighlightedInList.value;
  });

  const scoreLabel = computed(() => {
    const labels: Record<string, string> = {
      experience: '经验值',
      contribution: '贡献值',
      posts: '发帖数',
    };
    return labels[props.dimension] || '分数';
  });

  function handleUserClick(userId: string) {
    emit('userClick', userId);
  }
</script>

<style lang="less" scoped>
  .leaderboard-list {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
  }

  .skeleton-wrapper {
    padding: 24px;
  }

  .empty-wrapper {
    padding: 48px 24px;
    text-align: center;

    .empty-actions {
      margin-top: 16px;
    }
  }

  .list-content {
    .list-header {
      display: flex;
      align-items: center;
      padding: 12px 24px;
      background: #fafafa;
      border-bottom: 1px solid #f0f0f0;
      font-size: 13px;
      color: #999;
      font-weight: 500;
    }

    .list-body {
      max-height: 600px;
      overflow-y: auto;
    }

    .list-item {
      display: flex;
      align-items: center;
      padding: 12px 24px;
      border-bottom: 1px solid #f5f5f5;
      transition: background-color 0.2s;

      &:hover {
        background: #fafafa;
      }

      &.is-highlighted {
        background: #e6f7ff;

        &:hover {
          background: #bae7ff;
        }
      }
    }

    .rank-col {
      width: 60px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .user-col {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      min-width: 0;
    }

    .score-col {
      width: 120px;
      flex-shrink: 0;
      text-align: right;
      font-weight: 600;
    }

    .rank-badge {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-weight: bold;
      font-size: 14px;

      &.rank-1 {
        background: linear-gradient(135deg, #ffd700, #ffb800);
        box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
      }

      &.rank-2 {
        background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
        box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
      }

      &.rank-3 {
        background: linear-gradient(135deg, #cd7f32, #b87333);
        box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
      }
    }

    .rank-num {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }

    .user-avatar {
      flex-shrink: 0;
    }

    .username {
      font-size: 14px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .current-user-section {
      .divider {
        height: 1px;
        background: #e8e8e8;
        margin: 0 24px;
      }

      .current-user-item {
        display: flex;
        align-items: center;
        padding: 12px 24px;
        margin-top: 8px;

        .score-wrapper {
          display: flex;
          flex-direction: column;
          align-items: flex-end;

          .gap-text {
            font-size: 12px;
            color: #999;
            font-weight: normal;
            margin-top: 2px;
          }
        }
      }
    }
  }

  @media (max-width: 767px) {
    .list-content {
      .list-header {
        padding: 10px 12px;
        font-size: 12px;
      }

      .list-body .list-item,
      .current-user-section .current-user-item {
        padding: 10px 12px;
      }

      .rank-col {
        width: 44px;
      }

      .user-col {
        gap: 8px;

        .user-avatar {
          width: 32px !important;
          height: 32px !important;
        }

        .username {
          font-size: 13px;
        }
      }

      .score-col {
        width: 80px;
        font-size: 13px;
      }

      .rank-badge {
        width: 24px;
        height: 24px;
        font-size: 12px;
      }
    }

    .skeleton-wrapper,
    .empty-wrapper {
      padding: 32px 16px;
    }
  }
</style>
