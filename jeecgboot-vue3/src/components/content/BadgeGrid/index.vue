<template>
  <div class="badge-grid">
    <!-- 加载骨架屏 -->
    <template v-if="loading">
      <a-row :gutter="[16, 16]">
        <a-col v-for="i in skeletonCount" :key="i" :xs="12" :sm="8" :md="6">
          <a-card :body-style="{ padding: '12px', textAlign: 'center' }">
            <a-skeleton active :paragraph="false" />
            <a-skeleton-input style="width: 60%; margin: 8px auto" size="small" active />
          </a-card>
        </a-col>
      </a-row>
    </template>

    <!-- 空状态 -->
    <template v-else-if="!badges || badges.length === 0">
      <div class="badge-grid__empty">
        <a-empty description="暂无勋章" />
      </div>
    </template>

    <!-- 勋章网格 -->
    <template v-else>
      <!-- 选择计数器 -->
      <div v-if="selectable" class="badge-grid__counter">
        已选 {{ selectedIds.length }}/{{ maxSelect }}
      </div>

      <a-row :gutter="[16, 16]">
        <a-col v-for="badge in badges" :key="badge.badgeId" :xs="12" :sm="8" :md="6">
          <BadgeCard
            :badge="badge"
            :selectable="selectable"
            :selected="selectedIds.includes(badge.badgeId)"
            :size="sizeForGrid"
            @click="handleBadgeClick"
            @select="handleBadgeSelect"
          />
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { BadgeDetailVO } from '/@/api/content/growth/badge-types';
  import BadgeCard from '/@/components/content/BadgeCard/index.vue';

  const props = withDefaults(
    defineProps<{
      badges: BadgeDetailVO[];
      selectable?: boolean;
      selectedIds?: string[];
      maxSelect?: number;
      loading?: boolean;
    }>(),
    {
      selectable: false,
      selectedIds: () => [],
      maxSelect: 5,
      loading: false,
    }
  );

  const emit = defineEmits<{
    (e: 'update:selectedIds', ids: string[]): void;
    (e: 'click:badge', badge: BadgeDetailVO): void;
  }>();

  /** 骨架屏数量 */
  const skeletonCount = 8;

  /** 网格内卡片尺寸：数量多时用 small */
  const sizeForGrid = computed<'small' | 'medium'>(() => {
    return props.badges.length > 8 ? 'small' : 'medium';
  });

  /** 点击勋章（查看详情） */
  function handleBadgeClick(badge: BadgeDetailVO) {
    emit('click:badge', badge);
  }

  /** 选中/取消选中勋章 */
  function handleBadgeSelect(badgeId: string, selected: boolean) {
    let newIds = [...props.selectedIds];
    if (selected) {
      if (newIds.length >= props.maxSelect) return;
      newIds.push(badgeId);
    } else {
      newIds = newIds.filter((id) => id !== badgeId);
    }
    emit('update:selectedIds', newIds);
  }
</script>

<style scoped lang="less">
  .badge-grid {
    &__counter {
      text-align: right;
      margin-bottom: 12px;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    &__empty {
      padding: 48px 0;
    }
  }
</style>
