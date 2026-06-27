<template>
  <div class="circle-growth-page">
    <div class="growth-container">
      <div class="growth-header">
        <a-button type="link" @click="goDetail">
          <ArrowLeftOutlined /> 返回圈子详情
        </a-button>
        <h2 class="growth-title">我的成长</h2>
      </div>

      <!-- 骨架屏加载态 -->
      <div v-if="loading && !memberGrowth" class="skeleton-wrapper">
        <a-skeleton active :paragraph="{ rows: 4 }" class="skeleton-overview" />
        <a-row :gutter="16" class="skeleton-cards">
          <a-col :xs="24" :lg="12">
            <a-skeleton active :paragraph="{ rows: 6 }" />
          </a-col>
          <a-col :xs="24" :lg="12">
            <a-skeleton active :paragraph="{ rows: 3 }" class="skeleton-daily" />
            <a-skeleton active :paragraph="{ rows: 4 }" />
          </a-col>
        </a-row>
      </div>

      <!-- 错误态 -->
      <div v-else-if="error" class="error-state">
        <a-result status="error" title="加载失败" sub-title="成长数据加载失败，请重试">
          <template #extra>
            <a-button type="primary" @click="fetchData">重新加载</a-button>
          </template>
        </a-result>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!memberGrowth" class="empty-state">
        <a-empty description="暂无成长数据">
          <template #extra>
            <a-button type="primary" @click="goToPost">去发帖</a-button>
            <a-button style="margin-left: 8px" @click="goToDetail">去评论</a-button>
          </template>
        </a-empty>
      </div>

      <!-- 内容区 -->
      <div v-else class="growth-content">
          <GrowthOverviewCard
            :exp-points="memberGrowth?.expPoints ?? 0"
            :contribution-points="memberGrowth?.contributionPoints ?? 0"
            :rank="memberGrowth?.rank ?? 0"
          />

          <a-row :gutter="16">
            <a-col :xs="24" :lg="12">
              <a-card class="level-card" :bordered="false">
                <template #title>
                  <div class="card-title">
                    <MedalOutlined class="title-icon" />
                    我的等级
                  </div>
                </template>

                <div class="level-content">
                  <div class="level-header">
                    <div class="level-badge" :class="`level-${memberGrowth?.level ?? 1}`">
                      L{{ memberGrowth?.level ?? 1 }}
                    </div>
                    <div class="level-info">
                      <div class="level-name">{{ memberGrowth?.levelName ?? '萌新成员' }}</div>
                      <div class="level-exp">
                        当前经验：{{ memberGrowth?.expPoints ?? 0 }}
                      </div>
                    </div>
                  </div>

                  <div v-if="isMaxLevel" class="max-level-tip">
                    <CrownOutlined />
                    已达最高等级
                  </div>

                  <div v-else class="level-progress-section">
                    <div class="progress-header">
                      <span>距离下一等级</span>
                      <span>{{ memberGrowth?.expPoints ?? 0 }} / {{ memberGrowth?.nextLevelThreshold ?? 0 }}</span>
                    </div>
                    <a-progress
                      :percent="memberGrowth?.progressPercent ?? 0"
                      :stroke-color="levelColor"
                      :show-info="false"
                      :stroke-width="10"
                    />
                    <div class="progress-tip">
                      还需 {{ expToNextLevel }} 经验值升级
                    </div>
                  </div>
                </div>
              </a-card>
            </a-col>

            <a-col :xs="24" :lg="12">
              <DailyExpBar
                :today-exp="memberGrowth?.todayExp ?? 0"
                :daily-exp-limit="memberGrowth?.dailyExpLimit ?? 100"
              />

              <ParticipationStreak :participation-days="participationDays" />
            </a-col>
          </a-row>

          <a-card class="badges-card" :bordered="false">
            <template #title>
              <div class="card-title-between">
                <div class="card-title">
                  <TrophyOutlined class="title-icon" />
                  最近徽章
                </div>
                <a-button type="link" @click="goBadges">查看全部徽章</a-button>
              </div>
            </template>

            <div v-if="(!recentBadges || recentBadges.length === 0)" class="badges-empty">
              <a-empty description="暂无获得的徽章" :image="undefined">
                <template #extra>
                  <a-button type="primary" size="small" @click="goToPost">去发帖</a-button>
                  <a-button size="small" style="margin-left: 8px" @click="goToDetail">去评论</a-button>
                </template>
              </a-empty>
            </div>

            <div v-else class="badges-list">
              <div
                v-for="badge in recentBadges.slice(0, 3)"
                :key="badge.achievementType"
                class="badge-item"
              >
                <div class="badge-icon">
                  <img :src="badge.iconUrl" :alt="badge.name" v-if="badge.iconUrl" />
                  <div v-else class="badge-placeholder">
                    <StarOutlined />
                  </div>
                </div>
                <div class="badge-info">
                  <div class="badge-name">{{ badge.name }}</div>
                  <div class="badge-desc">{{ badge.description }}</div>
                  <div class="badge-date" v-if="badge.earnedDate">
                    {{ formatDate(badge.earnedDate) }} 获得
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import {
    ArrowLeftOutlined,
    MedalOutlined,
    CrownOutlined,
    TrophyOutlined,
    StarOutlined,
  } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { useCircleGrowthStore } from '/@/store/modules/circleGrowth';
  import type { MemberGrowthVO, AchievementVO } from '/@/api/content/circle/growth';
  import GrowthOverviewCard from '../components/growth/GrowthOverviewCard.vue';
  import DailyExpBar from '../components/growth/DailyExpBar.vue';
  import ParticipationStreak from '../components/growth/ParticipationStreak.vue';
  import { useGrowthNotification } from '../components/growth/useGrowthNotification';

  const router = useRouter();
  const route = useRoute();
  const { createMessage } = useMessage();
  const userStore = useUserStore();
  const circleGrowthStore = useCircleGrowthStore();

  const circleId = computed(() => route.params.id as string);
  const userId = computed(() => {
    const userInfo = userStore.getUserInfo;
    return userInfo?.id || userInfo?.userId || '';
  });

  const loading = ref(false);
  const error = ref(false);
  const memberGrowth = ref<MemberGrowthVO | null>(null);
  const participationDays = ref(0);
  const recentBadges = ref<AchievementVO[]>([]);

  const LEVEL_COLORS: Record<number, string> = {
    1: '#bfbfbf',
    2: '#52c41a',
    3: '#1890ff',
    4: '#fa8c16',
    5: '#faad14',
  };

  const levelColor = computed(() => {
    const level = memberGrowth.value?.level ?? 1;
    return LEVEL_COLORS[level] || '#bfbfbf';
  });

  const isMaxLevel = computed(() => {
    return memberGrowth.value?.nextLevelThreshold === null;
  });

  const expToNextLevel = computed(() => {
    if (!memberGrowth.value || isMaxLevel.value) return 0;
    const current = memberGrowth.value.expPoints;
    const threshold = memberGrowth.value.nextLevelThreshold ?? 0;
    return Math.max(threshold - current, 0);
  });

  async function fetchData() {
    if (!circleId.value || !userId.value) {
      error.value = true;
      return;
    }

    loading.value = true;
    error.value = false;

    try {
      const [growthData, participationData, achievementsData] = await Promise.all([
        circleGrowthStore.fetchMemberGrowth(circleId.value, userId.value),
        circleGrowthStore.fetchParticipationDays(circleId.value, userId.value),
        circleGrowthStore.fetchAchievements(circleId.value, userId.value),
      ]);

      memberGrowth.value = growthData;
      participationDays.value = participationData?.days ?? 0;
      recentBadges.value = (achievementsData || []).filter((a) => a.earned).slice(0, 3);
    } catch (err) {
      console.error('加载成长数据失败:', err);
      error.value = true;
      createMessage.error('加载成长数据失败');
    } finally {
      loading.value = false;
    }
  }

  function formatDate(dateStr: string | null) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  function goDetail() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  function goToPost() {
    router.push({ name: 'CircleDetail', params: { id: circleId.value } });
  }

  function goBadges() {
    router.push({ name: 'CircleBadges', params: { id: circleId.value } });
  }

  // 注册成长通知监听
  useGrowthNotification(
    () => circleId.value,
    () => fetchData(),
  );

  onMounted(() => {
    fetchData();
  });
</script>

<style lang="less" scoped>
  .circle-growth-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: calc(100vh - 64px);
  }

  .growth-container {
    max-width: 1200px;
    margin: 0 auto;
  }

  .growth-header {
    margin-bottom: 24px;

    .growth-title {
      margin: 8px 0 0;
      font-size: 24px;
      font-weight: 600;
    }
  }

  .error-state,
  .empty-state {
    background: #fff;
    border-radius: 12px;
    padding: 48px 24px;
  }

  .skeleton-wrapper {
    background: #fff;
    border-radius: 12px;
    padding: 24px;

    .skeleton-overview {
      margin-bottom: 24px;
    }

    .skeleton-cards {
      .skeleton-daily {
        margin-bottom: 16px;
      }
    }
  }

  .badges-empty {
    padding: 24px 0;
    text-align: center;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;

    .title-icon {
      font-size: 18px;
      color: #1890ff;
    }
  }

  .card-title-between {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .level-card {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
  }

  .level-content {
    padding: 8px 0;
  }

  .level-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
  }

  .level-badge {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 700;
    color: #fff;
    flex-shrink: 0;

    &.level-1 {
      background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
    }
    &.level-2 {
      background: linear-gradient(135deg, #95de64 0%, #52c41a 100%);
    }
    &.level-3 {
      background: linear-gradient(135deg, #69c0ff 0%, #1890ff 100%);
    }
    &.level-4 {
      background: linear-gradient(135deg, #ffc069 0%, #fa8c16 100%);
    }
    &.level-5 {
      background: linear-gradient(135deg, #ffe58f 0%, #faad14 100%);
      color: #d48806;
    }
  }

  .level-info {
    flex: 1;
  }

  .level-name {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color, rgba(0, 0, 0, 0.85));
    margin-bottom: 4px;
  }

  .level-exp {
    font-size: 14px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
  }

  .max-level-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 16px;
    background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
    border-radius: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #d46b08;
  }

  .level-progress-section {
    margin-top: 8px;
  }

  .progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
  }

  .progress-tip {
    margin-top: 8px;
    font-size: 13px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
    text-align: right;
  }

  .badges-card {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .badges-empty {
    padding: 24px 0;
  }

  .badges-list {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .badge-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: var(--background-color-light, #fafafa);
    border-radius: 12px;
    flex: 1;
    min-width: 240px;
    transition: all 0.2s ease;

    &:hover {
      background: var(--background-color-base, #f5f5f5);
      transform: translateY(-2px);
    }
  }

  .badge-icon {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
    background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .badge-placeholder {
    font-size: 28px;
    color: #fa8c16;
  }

  .badge-info {
    flex: 1;
    min-width: 0;
  }

  .badge-name {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-color, rgba(0, 0, 0, 0.85));
    margin-bottom: 4px;
  }

  .badge-desc {
    font-size: 13px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .badge-date {
    font-size: 12px;
    color: var(--text-color-tertiary, rgba(0, 0, 0, 0.25));
  }

  @media (max-width: 768px) {
    .circle-growth-page {
      padding: 8px;
    }

    .growth-container {
      padding: 0;
    }

    .level-header {
      gap: 12px;
    }

    .level-badge {
      width: 52px;
      height: 52px;
      font-size: 16px;
    }

    .level-name {
      font-size: 18px;
    }

    .badge-item {
      min-width: 100%;
    }
  }
</style>
