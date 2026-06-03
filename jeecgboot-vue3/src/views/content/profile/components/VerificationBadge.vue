<template>
  <a-tooltip :title="primary.tooltip">
    <span
      class="verification-badge"
      :style="{
        backgroundColor: primary.backgroundColor,
        borderColor: primary.borderColor,
        color: primary.iconColor,
      }"
    >
      <Icon icon="mdi:check-decagram" :size="14" />
    </span>
  </a-tooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Icon } from '/@/components/Icon';
import type { ContentUserVerificationBadgeVO } from '/@/api/content/profile/types';
import { selectPrimaryBadge } from './badgeStyle';

const props = defineProps<{
  badges: ContentUserVerificationBadgeVO[];
}>();

const primary = computed(() => selectPrimaryBadge(props.badges));
</script>

<style scoped>
.verification-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1.5px solid;
  vertical-align: middle;
}
</style>
