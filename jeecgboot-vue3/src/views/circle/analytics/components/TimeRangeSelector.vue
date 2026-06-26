<template>
  <div class="time-range-selector">
    <a-space>
      <a-button
        v-for="preset in presets"
        :key="preset.days"
        :type="activePreset === preset.days ? 'primary' : 'default'"
        size="small"
        @click="handlePreset(preset.days)"
      >
        {{ preset.label }}
      </a-button>
      <a-range-picker
        v-model:value="pickerValue"
        size="small"
        format="YYYY-MM-DD"
        :disabled-date="disabledDate"
        @change="handlePickerChange"
      />
    </a-space>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import dayjs, { type Dayjs } from 'dayjs';
import type { DateRange } from '/@/api/content/model/circleAnalyticsModel';

const props = withDefaults(defineProps<{
  value: DateRange;
  maxRangeDays?: number;
}>(), {
  maxRangeDays: 90,
});

const emit = defineEmits<{
  change: [range: DateRange];
}>();

const presets = [
  { label: '近7天', days: 7 },
  { label: '近30天', days: 30 },
];

const pickerValue = ref<[Dayjs, Dayjs] | null>(null);

const activePreset = computed<number | null>(() => {
  if (!props.value.startDate || !props.value.endDate) return null;
  const start = dayjs(props.value.startDate);
  const end = dayjs(props.value.endDate);
  const diffDays = end.diff(start, 'day') + 1;
  const today = dayjs().format('YYYY-MM-DD');
  if (props.value.endDate === today) {
    const matched = presets.find((p) => p.days === diffDays);
    return matched ? matched.days : null;
  }
  return null;
});

watch(
  () => props.value,
  (val) => {
    if (val.startDate && val.endDate) {
      pickerValue.value = [dayjs(val.startDate), dayjs(val.endDate)];
    } else {
      pickerValue.value = null;
    }
  },
  { immediate: true },
);

function handlePreset(days: number) {
  const end = dayjs();
  const start = end.subtract(days - 1, 'day');
  pickerValue.value = [start, end];
  emit('change', {
    startDate: start.format('YYYY-MM-DD'),
    endDate: end.format('YYYY-MM-DD'),
  });
}

function disabledDate(current: Dayjs) {
  if (!pickerValue.value || !pickerValue.value[0]) {
    return current && current > dayjs().endOf('day');
  }
  const start = pickerValue.value[0];
  const tooEarly = current && current < start.subtract(props.maxRangeDays - 1, 'day');
  const tooLate = current && current > start.add(props.maxRangeDays - 1, 'day');
  const afterToday = current && current > dayjs().endOf('day');
  return !!(tooEarly || tooLate || afterToday);
}

function handlePickerChange(dates: [Dayjs, Dayjs] | null) {
  if (!dates || !dates[0] || !dates[1]) {
    return;
  }
  emit('change', {
    startDate: dates[0].format('YYYY-MM-DD'),
    endDate: dates[1].format('YYYY-MM-DD'),
  });
}
</script>

<style lang="less" scoped>
.time-range-selector {
  display: flex;
  align-items: center;
}
</style>
