<template>
  <a-tag :color="badgeConfig.color" class="privacy-badge">
    <component :is="badgeConfig.icon" v-if="badgeConfig.icon" />
    {{ badgeConfig.label }}
  </a-tag>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { PrivacyType } from '/@/api/content/model/circleModel';

const props = defineProps<{
  type: PrivacyType;
}>();

const badgeConfig = computed(() => {
  switch (props.type) {
    case 'PRIVATE':
      return { color: 'orange', label: '私有', icon: null };
    case 'PASSWORD':
      return { color: 'red', label: '密码保护', icon: null };
    case 'PUBLIC':
    default:
      return { color: 'green', label: '公开', icon: null };
  }
});
</script>

<style lang="less" scoped>
.privacy-badge {
  font-size: 11px;
  line-height: 1;
  padding: 1px 6px;
}
</style>
