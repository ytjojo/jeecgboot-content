<template>
  <div class="circle-leaderboard-page">
    <div class="leaderboard-container">
      <div class="leaderboard-header">
        <a-button type="link" @click="goDetail">
          <ArrowLeftOutlined /> 返回圈子详情
        </a-button>
        <h2 class="leaderboard-title">排行榜</h2>
      </div>

      <!-- 错误态 -->
      <div v-if="error" class="error-wrapper">
        <a-result status="error" title="加载失败" sub-title="排行榜数据加载失败，请稍后重试">
          <template #extra>
            <a-button type="primary" @click="loadLeaderboard">重试</a-button>
          </template>
        </a-result>
      </div>

      <template v-else>
        <!-- 维度和周期切换 -->
        <LeaderboardTabs
          v-model:dimension="dimension"
          v-model:period="period"
        />

        <!-- 排行榜列表 -->
        <LeaderboardList
          :entries="leaderboardData?.entries || []"
          :current-user="leaderboardData?.currentUser || null"
          :loading="loading"
          :dimension="dimension"
          @user-click="handleUserClick"
          @go-post="goToPost"
          @go-comment="goToDetail"
        />

        <!-- 底部提示 -->
        <div class="footer-tip">
          <a-alert type="info" show-icon>
            <template #message>
              <span v-if="snapshotTime">榜单快照时间：{{ snapshotTime }}</span>
              <span v-else>榜单每小时更新一次</span>
            </template>
            <template #description v-if="snapshotTime">榜单每小时更新一次</template>
          </a-alert>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, computed, onMounted } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { ArrowLeftOutlined } from '@ant-design/icons-vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useCircleGrowthStore } from '/@/store/modules/circleGrowth';
  import { LeaderboardTabs, LeaderboardList } from '../components/growth';
  import { useGrowthNotification } from '../components/growth/useGrowthNotification';
  import type { LeaderboardResponse } from '/@/api/content/circle/growth';

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
  const dimension = ref<'experience' | 'contribution' | 'posts'>('experience');
  const period = ref<'week' | 'month' | 'all'>('week');
  const loading = ref(false);
  const error = ref(false);
  const snapshotTime = ref<string | null>(null);

  // 排行榜数据
  const leaderboardData = ref<LeaderboardResponse | null>(null);

  // 加载排行榜
  async function loadLeaderboard() {
    if (!circleId.value || !currentUserId.value) {
      return;
    }

    loading.value = true;
    error.value = false;

    try {
      const data = await circleGrowthStore.fetchLeaderboard(
        circleId.value,
        dimension.value,
        period.value,
        currentUserId.value,
        true,
      );
      leaderboardData.value = data;
    } catch (err) {
      console.error('加载排行榜失败:', err);
      error.value = true;
    } finally {
      loading.value = false;
    }
  }

  // 监听维度和周期变化
  watch([dimension, period], () => {
    loadLeaderboard();
  });

  // 页面初始化
  onMounted(() => {
    loadLeaderboard();
  });

  function goDetail() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  function goToPost() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  function goToDetail() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  function handleUserClick(userId: string) {
    console.log('跳转至用户资料页:', userId);
  }

  // 注册成长通知监听
  useGrowthNotification(
    () => circleId.value,
    () => loadLeaderboard(),
  );
</script>

<style lang="less" scoped>
  .circle-leaderboard-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: calc(100vh - 64px);
  }

  .leaderboard-container {
    max-width: 800px;
    margin: 0 auto;
  }

  .leaderboard-header {
    margin-bottom: 24px;

    .leaderboard-title {
      margin: 8px 0 0;
      font-size: 24px;
      font-weight: 600;
    }
  }

  .error-wrapper {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
  }

  .footer-tip {
    margin-top: 16px;
  }
</style>
