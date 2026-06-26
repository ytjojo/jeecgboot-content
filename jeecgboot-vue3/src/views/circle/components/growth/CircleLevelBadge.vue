<template>
  <div :class="['circle-level-badge', `level-${level}`, { 'is-top': level === 5 }]" :style="badgeStyle">
    <span class="level-label">L{{ level }}</span>
    <span class="level-name">{{ displayName }}</span>
    <span v-if="level === 5" class="top-badge">标杆圈</span>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    level: number;
    levelName?: string;
  }>(),
  {
    levelName: '',
  },
);

const LEVEL_COLORS: Record<number, string> = {
  1: '#bfbfbf',
  2: '#52c41a',
  3: '#1890ff',
  4: '#fa8c16',
  5: '#faad14',
};

const LEVEL_BG_COLORS: Record<number, string> = {
  1: '#f5f5f5',
  2: '#f6ffed',
  3: '#e6f7ff',
  4: '#fff7e6',
  5: '#fffbe6',
};

const LEVEL_NAMES: Record<number, string> = {
  1: '新生圈',
  2: '成长圈',
  3: '优质圈',
  4: '活跃圈',
  5: '标杆圈',
};

const displayName = computed(() => {
  return props.levelName || LEVEL_NAMES[props.level] || `L${props.level}`;
});

const levelColor = computed(() => LEVEL_COLORS[props.level] || '#bfbfbf');
const levelBgColor = computed(() => LEVEL_BG_COLORS[props.level] || '#f5f5f5');

const badgeStyle = computed(() => {
  if (props.level === 5) {
    return {
      color: '#d48806',
      background: 'linear-gradient(135deg, #fffbe6 0%, #fff7cc 100%)',
      border: '1px solid #faad14',
    };
  }
  return {
    color: levelColor.value,
    background: levelBgColor.value,
    border: `1px solid ${levelColor.value}4d`,
  };
});
</script>

<style lang="less" scoped>
.circle-level-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  transition: all 0.3s;

  .level-label {
    font-weight: 700;
    font-size: 12px;
  }

  .level-name {
    white-space: nowrap;
  }

  .top-badge {
    display: inline-flex;
    align-items: center;
    padding: 1px 6px;
    font-size: 11px;
    border-radius: 8px;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.6);
  }

  &.level-5 {
    box-shadow: 0 2px 8px rgba(250, 173, 20, 0.25);

    .level-label {
      background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
}
</style>
