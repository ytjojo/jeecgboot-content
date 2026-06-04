<template>
  <div class="my-level">
    <a-page-header title="我的等级" :back-icon="true" @back="$router.back()" />

    <a-spin :spinning="loading">
      <!-- 顶部两栏：等级卡片 + 积分余额 -->
      <a-row :gutter="16" class="my-level__top">
        <a-col :xs="24" :sm="12">
          <LevelCard
            :level="summary?.level ?? 0"
            :level-name="summary?.levelName"
            :growth-value="summary?.growthValue ?? 0"
            :next-level-gap="summary?.nextLevelGap"
            :level-progress="summary?.levelProgress"
          />
        </a-col>
        <a-col :xs="24" :sm="12">
          <PointBalance :balance="summary?.pointBalance ?? 0" />
        </a-col>
      </a-row>

      <!-- 升级进度 -->
      <a-card title="升级进度" :bordered="false" class="my-level__section">
        <GrowthProgress
          :current="summary?.growthValue ?? 0"
          :target="(summary?.growthValue ?? 0) + (summary?.nextLevelGap ?? 0)"
        />
      </a-card>

      <!-- 衰减预警 -->
      <a-card v-if="decayRule?.enabled" title="衰减状态" :bordered="false" class="my-level__section">
        <DecayWarning :decay-status="decayStatus" :decay-rule="decayRule" />
      </a-card>

      <!-- 当前等级权益 -->
      <a-card title="当前等级权益" :bordered="false" class="my-level__section">
        <LevelBenefitList :benefits="levelBenefit?.benefits ?? []" :loading="benefitLoading" />
      </a-card>

      <!-- 等级体系说明（可折叠） -->
      <a-card title="等级体系说明" :bordered="false" class="my-level__section">
        <a-collapse v-if="levelConfigs.length">
          <a-collapse-panel
            v-for="cfg in levelConfigs"
            :key="cfg.level"
            :header="`Lv.${cfg.level} ${cfg.levelName}`"
          >
            <p><strong>所需成长值：</strong>{{ cfg.requiredGrowth }}</p>
            <p v-if="cfg.description">{{ cfg.description }}</p>
          </a-collapse-panel>
        </a-collapse>
        <a-empty v-else description="暂无等级配置" />
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import LevelCard from '/@/components/content/LevelCard/index.vue';
  import PointBalance from '/@/components/content/PointBalance/index.vue';
  import LevelBenefitList from '/@/components/content/LevelBenefitList/index.vue';
  import GrowthProgress from '/@/components/content/GrowthProgress/index.vue';
  import DecayWarning from '/@/components/content/DecayWarning/index.vue';
  import { useGrowthStore } from '/@/store/modules/growth';

  const growthStore = useGrowthStore();

  const loading = ref(false);
  const benefitLoading = ref(false);

  const summary = computed(() => growthStore.getSummary);
  const levelBenefit = computed(() => growthStore.getLevelBenefit);
  const decayRule = computed(() => growthStore.getDecayRule);
  const decayStatus = computed(() => growthStore.getDecayStatus);
  const levelConfigs = computed(() => growthStore.getLevelConfigs);

  onMounted(async () => {
    loading.value = true;
    try {
      await Promise.all([
        growthStore.loadSummary(),
        growthStore.loadLevelConfigs(),
        growthStore.loadDecayRule(),
        growthStore.loadDecayStatus(),
      ]);
    } finally {
      loading.value = false;
    }

    benefitLoading.value = true;
    try {
      await growthStore.loadLevelBenefit();
    } finally {
      benefitLoading.value = false;
    }
  });
</script>

<style scoped lang="less">
  .my-level {
    padding: 16px;

    &__top {
      margin-bottom: 16px;

      .ant-col {
        margin-bottom: 16px;
      }
    }

    &__section {
      margin-bottom: 16px;
    }
  }
</style>
