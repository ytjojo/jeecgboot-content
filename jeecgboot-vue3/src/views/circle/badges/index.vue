<template>
  <div class="circle-badges-page">
    <div class="badges-container">
      <div class="badges-header">
        <a-button type="link" @click="goDetail">
          <ArrowLeftOutlined /> 返回圈子详情
        </a-button>
        <h2 class="badges-title">{{ circleName || '徽章墙' }}</h2>
        <div v-if="!loading && !error && badges.length > 0" class="badges-summary">
          <a-statistic title="已获得徽章" :value="earnedCount" suffix="/ {{ totalCount }}" :value-style="{ fontSize: '20px', color: earnedCount > 0 ? '#52c41a' : undefined }" />
        </div>
      </div>

      <!-- 骨架屏加载态 -->
      <div v-if="loading" class="skeleton-wrapper">
        <a-skeleton active :paragraph="{ rows: 10 }" />
      </div>

      <!-- 错误态 -->
      <div v-else-if="error" class="error-wrapper">
        <a-result status="error" title="加载失败" sub-title="徽章数据加载失败，请稍后重试">
          <template #extra>
            <a-button type="primary" @click="loadData">重试</a-button>
          </template>
        </a-result>
      </div>

      <!-- 空状态 -->
      <div v-else-if="badges.length === 0" class="empty-wrapper">
        <a-empty description="暂无徽章">
          <template #extra>
            <a-button type="primary" @click="goToPost">去发帖</a-button>
            <a-button style="margin-left: 8px" @click="goDetail">去评论</a-button>
          </template>
        </a-empty>
      </div>

      <!-- 徽章墙 -->
      <template v-else>
        <BadgeWall :badges="badges" :loading="false" @open-detail="openDetailModal" />
      </template>
    </div>

    <!-- 徽章详情弹窗 -->
    <BadgeDetailModal :visible="detailModalVisible" :badge="selectedBadge" @close="closeDetailModal" />
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { ArrowLeftOutlined } from '@ant-design/icons-vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useCircleGrowthStore } from '/@/store/modules/circleGrowth';
  import { getCircleDetail } from '/@/api/content/circle';
  import { BadgeWall, BadgeDetailModal } from '../components/growth';
  import { useGrowthNotification } from '../components/growth/useGrowthNotification';
  import type { AchievementVO } from '/@/api/content/circle/growth';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();
  const circleGrowthStore = useCircleGrowthStore();

  const circleId = computed(() => route.params.id as string);
  const currentUserId = computed(() => {
    const userInfo = userStore.getUserInfo;
    return userInfo?.id || userInfo?.userId || '';
  });

  // 状态
  const loading = ref(false);
  const error = ref(false);
  const badges = ref<AchievementVO[]>([]);
  const circleName = ref<string>('');

  // 详情弹窗状态
  const detailModalVisible = ref(false);
  const selectedBadge = ref<AchievementVO | null>(null);

  // 统计数据（降级通过列表长度计算）
  const earnedCount = computed(() => badges.value.filter((b) => b.earned).length);
  const totalCount = computed(() => badges.value.length);

  // 加载数据
  async function loadData() {
    if (!circleId.value) {
      error.value = true;
      return;
    }

    loading.value = true;
    error.value = false;

    try {
      // 并行加载圈子信息和徽章列表
      const [circleData, achievementsData] = await Promise.all([
        getCircleDetail(circleId.value).catch(() => null),
        currentUserId.value
          ? circleGrowthStore.fetchAchievements(circleId.value, currentUserId.value, true)
          : Promise.resolve([]),
      ]);

      if (circleData) {
        circleName.value = circleData.name || '';
      }

      badges.value = achievementsData || [];
    } catch (err) {
      console.error('加载徽章失败:', err);
      error.value = true;
    } finally {
      loading.value = false;
    }
  }

  // 打开详情弹窗
  function openDetailModal(badge: AchievementVO) {
    selectedBadge.value = badge;
    detailModalVisible.value = true;
  }

  // 关闭详情弹窗
  function closeDetailModal() {
    detailModalVisible.value = false;
    selectedBadge.value = null;
  }

  function goDetail() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  function goToPost() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  // 注册成长通知监听
  useGrowthNotification(
    () => circleId.value,
    () => loadData(),
  );

  onMounted(() => {
    loadData();
  });
</script>

<style lang="less" scoped>
  .circle-badges-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: calc(100vh - 64px);
  }

  .badges-container {
    max-width: 1200px;
    margin: 0 auto;
  }

  .badges-header {
    margin-bottom: 24px;
    background: #fff;
    padding: 20px 24px;
    border-radius: 8px;

    .badges-title {
      margin: 8px 0 12px;
      font-size: 24px;
      font-weight: 600;
    }

    .badges-summary {
      margin-top: 8px;
    }
  }

  .skeleton-wrapper,
  .error-wrapper,
  .empty-wrapper {
    background: #fff;
    border-radius: 8px;
    padding: 48px 24px;
  }

  .error-wrapper {
    display: flex;
    justify-content: center;
  }
</style>
