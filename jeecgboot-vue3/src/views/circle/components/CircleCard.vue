<template>
  <div class="circle-card" @click="$emit('click')">
    <div class="circle-card-cover">
      <img v-lazy="circle.coverUrl || circle.iconUrl" :alt="circle.name" class="circle-card-cover-img" />
    </div>
    <div class="circle-card-body">
      <div class="circle-card-header">
        <img :src="circle.iconUrl" :alt="circle.name" class="circle-card-icon" />
        <div class="circle-card-info">
          <h3 class="circle-card-name">{{ circle.name }}</h3>
          <span class="circle-card-badges">
            <PrivacyBadge :type="circle.privacyType" />
            <a-tag v-if="isGovernor" color="blue" class="governance-badge" @click.stop="handleGovernance">治理</a-tag>
          </span>
        </div>
      </div>
      <p class="circle-card-desc">{{ circle.description }}</p>
      <div class="circle-card-footer">
        <span class="circle-card-meta">
          <span class="circle-card-members">{{ circle.memberCount }} 成员</span>
          <span v-if="circle.category" class="circle-card-category">{{ circle.category }}</span>
        </span>
        <JoinStatusButton :circle="circle" @join-success="$emit('join-success')" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { CircleVO } from '/@/api/content/model/circleModel';
import PrivacyBadge from './PrivacyBadge.vue';
import JoinStatusButton from './JoinStatusButton.vue';

const props = defineProps<{
  circle: CircleVO;
}>();

const emit = defineEmits<{
  click: [];
  'join-success': [];
  governance: [circleId: string];
}>();

const isGovernor = computed(() => {
  return props.circle.myRole === 'CREATOR' || props.circle.myRole === 'MODERATOR';
});

function handleGovernance() {
  emit('governance', props.circle.id);
}
</script>

<style lang="less" scoped>
.circle-card {
  background: var(--component-background);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.3s, transform 0.2s;
  border: 1px solid var(--border-color-base, #f0f0f0);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  &-cover {
    width: 100%;
    height: 120px;
    overflow: hidden;
    background: var(--background-color-base, #f5f5f5);

    &-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &-body {
    padding: 12px 16px 16px;
  }

  &-header {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin-top: -28px;
  }

  &-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    border: 2px solid var(--component-background, #fff);
    background: var(--component-background, #fff);
    object-fit: cover;
    flex-shrink: 0;
  }

  &-info {
    flex: 1;
    min-width: 0;
    padding-top: 28px;
  }

  &-name {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 4px;
    line-height: 1.3;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &-badges {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
  }

  &-desc {
    margin: 10px 0 12px;
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 39px;
  }

  &-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    color: var(--text-color-tertiary, #999);
  }

  &-category {
    padding: 1px 6px;
    background: var(--background-color-base, #f5f5f5);
    border-radius: 4px;
  }
}

.governance-badge {
  cursor: pointer;
  font-size: 11px;
  line-height: 1;
  padding: 1px 6px;

  &:hover {
    opacity: 0.8;
  }
}

// 响应式
@media (max-width: 768px) {
  .circle-card {
    &-cover {
      height: 100px;
    }

    &-icon {
      width: 40px;
      height: 40px;
    }

    &-name {
      font-size: 14px;
    }
  }
}
</style>
