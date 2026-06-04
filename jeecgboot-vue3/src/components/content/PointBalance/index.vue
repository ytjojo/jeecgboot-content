<template>
  <a-card class="point-balance" :bordered="false">
    <a-skeleton v-if="loading" active :paragraph="{ rows: 3 }" />
    <template v-else>
      <!-- 积分余额 -->
      <div class="point-balance__main">
        <a-statistic title="积分余额" :value="balance" :value-style="{ color: 'var(--j-global-primary-color, #1890ff)', fontSize: '36px', fontWeight: 700 }">
          <template #suffix>
            <span style="font-size: 14px; color: #999; margin-left: 4px">分</span>
          </template>
        </a-statistic>
      </div>

      <!-- 今日获取 / 今日消耗 -->
      <a-row class="point-balance__today" :gutter="16">
        <a-col :span="12">
          <a-statistic title="今日获取" :value="todayEarn" :value-style="{ color: '#52c41a', fontSize: '18px' }">
            <template #prefix>
              <span>+</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="12">
          <a-statistic title="今日消耗" :value="todaySpend" :value-style="{ color: '#f5222d', fontSize: '18px' }">
            <template #prefix>
              <span>-</span>
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </template>
  </a-card>
</template>

<script setup lang="ts">
  import type { PropType } from 'vue';

  defineProps({
    balance: {
      type: Number as PropType<number>,
      required: true,
    },
    todayEarn: {
      type: Number as PropType<number>,
      default: 0,
    },
    todaySpend: {
      type: Number as PropType<number>,
      default: 0,
    },
    loading: {
      type: Boolean as PropType<boolean>,
      default: false,
    },
  });
</script>

<style scoped lang="less">
  .point-balance {
    text-align: center;

    &__main {
      margin-bottom: 16px;
    }

    &__today {
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
    }
  }
</style>
