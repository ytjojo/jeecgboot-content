<template>
  <div class="strength-meter">
    <div class="strength-meter__bar">
      <div class="strength-meter__fill" :class="`strength-meter__fill--${strength}`" :style="{ width: percent + '%' }" />
    </div>
    <span class="strength-meter__label" :style="{ color: color }">{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { evaluatePasswordStrength, STRENGTH_LABEL, STRENGTH_COLOR, type PasswordStrength } from './passwordStrength';

const props = defineProps<{ value: string }>();

const strength = computed<PasswordStrength>(() => evaluatePasswordStrength(props.value));
const label = computed(() => STRENGTH_LABEL[strength.value]);
const color = computed(() => STRENGTH_COLOR[strength.value]);
const percent = computed(() => (strength.value === 'weak' ? 33 : strength.value === 'medium' ? 66 : 100));
</script>

<style lang="less" scoped>
.strength-meter {
  display: flex;
  align-items: center;
  gap: 8px;
  &__bar {
    flex: 1;
    height: 4px;
    background: #f0f0f0;
    border-radius: 2px;
    overflow: hidden;
  }
  &__fill {
    height: 100%;
    transition: width 0.2s;
    &--weak { background: #ff4d4f; }
    &--medium { background: #faad14; }
    &--strong { background: #52c41a; }
  }
  &__label {
    font-size: 12px;
    min-width: 24px;
    text-align: right;
  }
}
</style>
