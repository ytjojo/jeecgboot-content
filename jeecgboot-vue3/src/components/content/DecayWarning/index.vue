<template>
  <div class="decay-warning">
    <template v-if="decayStatus">
      <!-- 衰减中：距离衰减 <= 3 天 -->
      <a-alert
        v-if="decayStatus.daysUntilDecay !== undefined && decayStatus.daysUntilDecay <= 3"
        :message="`距离衰减仅剩 ${decayStatus.daysUntilDecay} 天`"
        :description="decayRule?.description"
        type="error"
        show-icon
      />
      <!-- 保护期：本周期活跃次数达标 -->
      <a-alert
        v-else-if="isProtected"
        message="本周期已达标，免受衰减"
        type="success"
        show-icon
      />
      <!-- 正常状态 -->
      <a-alert
        v-else
        :message="normalMessage"
        type="info"
        show-icon
      />
    </template>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { PropType } from 'vue';
  import type { DecayStatusVO, DecayRuleVO } from '/@/api/content/growth/types';

  const props = defineProps({
    decayStatus: {
      type: Object as PropType<DecayStatusVO | null>,
      required: true,
    },
    decayRule: {
      type: Object as PropType<DecayRuleVO | null>,
      default: null,
    },
  });

  const isProtected = computed(() => {
    if (!props.decayStatus) return false;
    const { currentActivityCount = 0, requiredActivityCount = 0 } = props.decayStatus;
    return requiredActivityCount > 0 && currentActivityCount >= requiredActivityCount;
  });

  const normalMessage = computed(() => {
    if (!props.decayStatus) return '';
    const { daysUntilDecay, currentActivityCount } = props.decayStatus;
    const days = daysUntilDecay ?? 0;
    const count = currentActivityCount ?? 0;
    return `距下次衰减还有 ${days} 天，本周期活跃 ${count} 次`;
  });
</script>

<style scoped lang="less">
  .decay-warning {
    width: 100%;
  }
</style>
