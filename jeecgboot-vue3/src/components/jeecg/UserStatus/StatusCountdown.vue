<template>
  <span v-if="remaining > 0" class="status-countdown">
    {{ formatted }}
  </span>
  <span v-else class="status-countdown expired">已到期</span>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';

dayjs.extend(duration);

const props = defineProps({
  endTime: {
    type: String,
    required: true,
  },
  onExpired: {
    type: Function as PropType<() => void>,
    default: undefined,
  },
});

const remaining = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

const formatted = computed(() => {
  const d = dayjs.duration(remaining.value);
  const days = Math.floor(d.asDays());
  const hours = d.hours();
  const minutes = d.minutes();
  const seconds = d.seconds();

  if (days > 0) {
    return `${days}天${hours}时${minutes}分${seconds}秒`;
  }
  if (hours > 0) {
    return `${hours}时${minutes}分${seconds}秒`;
  }
  return `${minutes}分${seconds}秒`;
});

function updateRemaining() {
  const end = dayjs(props.endTime);
  const now = dayjs();
  remaining.value = Math.max(0, end.diff(now, 'millisecond'));
  if (remaining.value <= 0) {
    stopTimer();
    props.onExpired?.();
  }
}

function stopTimer() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    updateRemaining();
  }
}

onMounted(() => {
  updateRemaining();
  timer = setInterval(updateRemaining, 1000);
  document.addEventListener('visibilitychange', handleVisibilityChange);
});

onUnmounted(() => {
  stopTimer();
  document.removeEventListener('visibilitychange', handleVisibilityChange);
});
</script>

<style scoped>
.status-countdown {
  font-variant-numeric: tabular-nums;
}
.expired {
  color: #999;
}
</style>
