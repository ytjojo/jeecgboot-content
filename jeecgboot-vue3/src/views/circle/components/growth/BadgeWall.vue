<template>
  <div class="badge-wall">
    <a-skeleton :loading="loading" active :paragraph="{ rows: 8 }">
      <template v-if="earnedBadges.length > 0 || unearnedBadges.length > 0">
        <div v-if="earnedBadges.length > 0" class="badge-section">
          <div class="section-header">
            <h3 class="section-title">
              <TrophyOutlined class="title-icon earned-icon" />
              已获得徽章
              <a-tag color="success" class="count-tag">{{ earnedBadges.length }}</a-tag>
            </h3>
          </div>
          <a-row :gutter="[16, 16]">
            <a-col v-for="badge in earnedBadges" :key="badge.achievementType" :xs="12" :sm="12" :md="8" :lg="8" :xl="6">
              <BadgeCard :badge="badge" @click="handleBadgeClick" />
            </a-col>
          </a-row>
        </div>

        <a-divider v-if="earnedBadges.length > 0 && unearnedBadges.length > 0" />

        <div v-if="unearnedBadges.length > 0" class="badge-section">
          <div class="section-header">
            <h3 class="section-title">
              <LockOutlined class="title-icon unearned-icon" />
              未获得徽章
              <a-tag color="default" class="count-tag">{{ unearnedBadges.length }}</a-tag>
            </h3>
          </div>
          <a-row :gutter="[16, 16]">
            <a-col v-for="badge in unearnedBadges" :key="badge.achievementType" :xs="12" :sm="12" :md="8" :lg="8" :xl="6">
              <BadgeCard :badge="badge" @click="handleBadgeClick" />
            </a-col>
          </a-row>
        </div>
      </template>

      <a-empty v-else description="暂无徽章" />
    </a-skeleton>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { TrophyOutlined, LockOutlined } from '@ant-design/icons-vue';
  import BadgeCard from './BadgeCard.vue';
  import type { AchievementVO } from '/@/api/content/circle/growth';

  type BadgeItem = AchievementVO & { revoked?: boolean };

  interface BadgeWallProps {
    badges: BadgeItem[];
    loading?: boolean;
  }

  const props = withDefaults(defineProps<BadgeWallProps>(), {
    loading: false,
  });

  const emit = defineEmits<{
    (e: 'open-detail', badge: BadgeItem): void;
  }>();

  const earnedBadges = computed(() => {
    return props.badges.filter((b) => b.earned && !b.revoked);
  });

  const unearnedBadges = computed(() => {
    return props.badges.filter((b) => !b.earned && !b.revoked);
  });

  function handleBadgeClick(badge: BadgeItem) {
    emit('open-detail', badge);
  }
</script>

<style lang="less" scoped>
  .badge-wall {
    .badge-section {
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .section-header {
      margin-bottom: 16px;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);

      .title-icon {
        font-size: 20px;

        &.earned-icon {
          color: #faad14;
        }

        &.unearned-icon {
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .count-tag {
        margin-left: 4px;
      }
    }
  }
</style>
