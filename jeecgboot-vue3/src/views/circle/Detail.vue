<template>
  <div v-if="error === 'not-found'" class="circle-detail-page">
    <a-result status="404" title="圈子不存在或已被删除" sub-title="请检查链接是否正确">
      <template #extra>
        <a-button type="primary" @click="goList">返回圈子列表</a-button>
      </template>
    </a-result>
  </div>

  <div v-else-if="error === 'forbidden'" class="circle-detail-page">
    <a-result status="403" title="您没有权限访问此页面" sub-title="该圈子为私有圈子">
      <template #extra>
        <a-button type="primary" @click="goList">返回圈子列表</a-button>
      </template>
    </a-result>
  </div>

  <div v-else class="circle-detail-page">
    <a-spin :spinning="loading">
      <template v-if="circle">
        <!-- 封面图 -->
        <div class="detail-cover">
          <img v-if="circle.coverUrl" :src="circle.coverUrl" :alt="circle.name" class="cover-img" />
          <div v-else class="cover-placeholder" />
        </div>

        <div class="detail-container">
          <!-- 圈子信息头部 -->
          <div class="detail-header">
            <div class="header-main">
              <img :src="circle.iconUrl" :alt="circle.name" class="header-icon" />
              <div class="header-info">
                <div class="header-title-row">
                  <h1 class="header-name">{{ circle.name }}</h1>
                  <PrivacyBadge :type="circle.privacyType" />
                </div>
                <div class="header-meta">
                  <span>{{ circle.memberCount }}/{{ circle.maxMemberCount || '∞' }} 成员</span>
                  <span v-if="circle.category" class="meta-category">{{ circle.category }}</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="header-actions">
              <!-- 创建者操作 -->
              <template v-if="circleStore.isCreator">
                <a-button @click="goEdit">编辑</a-button>
                <a-button @click="goMembers">成员管理</a-button>
                <a-button @click="goGovernanceLog">治理日志</a-button>
              </template>

              <!-- 版主操作 -->
              <template v-else-if="circleStore.isModerator">
                <a-button @click="goMembers">成员管理</a-button>
                <a-button danger @click="handleLeave">退出</a-button>
              </template>

              <!-- 已加入普通成员 -->
              <template v-else-if="circle.joined">
                <a-button @click="goMembers">成员列表</a-button>
                <a-button danger @click="handleLeave">退出</a-button>
              </template>

              <!-- 未加入用户 -->
              <template v-else>
                <JoinStatusButton
                  :circle="circle"
                  @apply="showApplyModal = true"
                  @password-join="showPasswordModal = true"
                  @join-success="handleJoinSuccess"
                />
              </template>
            </div>
          </div>

          <!-- 禁言提示条 -->
          <a-alert
            v-if="isMuted"
            message="您已被禁言"
            type="warning"
            banner
            :closable="false"
            class="muted-banner"
          />

          <!-- 圈子等级区块 -->
          <div class="detail-section circle-level-section">
            <div class="section-title">圈子等级</div>

            <!-- 私有圈子未加入提示 -->
            <div v-if="isPrivateCircle && !circle.joined" class="level-private-tip">
              <LockOutlined class="lock-icon" />
              <span>加入圈子后查看等级信息</span>
            </div>

            <!-- 加载态 -->
            <a-skeleton v-else-if="levelLoading" active :paragraph="{ rows: 3 }" />

            <!-- 失败态 -->
            <div v-else-if="levelError" class="level-error">
              <a-typography-text type="danger">{{ levelError }}</a-typography-text>
              <a-button size="small" type="primary" @click="retryFetchLevel">重试</a-button>
            </div>

            <!-- 成功态 -->
            <template v-else-if="circleLevelInfo">
              <div class="level-header">
                <CircleLevelBadge :level="circleLevelInfo.level" :level-name="circleLevelInfo.levelName" />
              </div>
              <CircleLevelProgress :level-info="circleLevelInfo" />
            </template>
          </div>

          <!-- 圈子简介 -->
          <div class="detail-section">
            <div class="section-title">圈子简介</div>
            <div :class="['description-text', { 'is-collapsed': descCollapsed }]">
              {{ circle.description }}
            </div>
            <a-button
              v-if="descNeedsToggle"
              type="link"
              @click="descCollapsed = !descCollapsed"
            >
              {{ descCollapsed ? '展开' : '收起' }}
            </a-button>
          </div>

          <!-- 内容区 Tab（动态/成员） -->
          <a-tabs class="detail-tabs">
            <a-tab-pane key="feed" tab="动态">
              <!-- 公告栏 -->
              <CircleAnnouncementBar
                v-if="circle"
                :circle-id="circle.id"
                ref="announcementBarRef"
                @manage="showAnnouncementManage = true"
              />
              <a-empty v-if="feedItems.length === 0" description="暂无动态" />
              <div v-else class="feed-list">
                <CircleContentCard
                  v-for="item in feedItems"
                  :key="item.id"
                  :content="item"
                  @action="handleContentAction"
                />
              </div>
            </a-tab-pane>
            <a-tab-pane key="members" tab="成员">
              <div class="member-preview">
                <MemberAvatar
                  v-for="m in previewMembers"
                  :key="m.id"
                  :nickname="m.userId"
                  :role="m.role"
                  size="small"
                />
                <a-button v-if="circle.memberCount > 10" type="link" @click="goMembers">
                  查看全部 {{ circle.memberCount }} 位成员
                </a-button>
                <a-empty v-if="previewMembers.length === 0" description="暂无成员" />
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </template>
    </a-spin>

    <!-- 加入申请确认 Modal -->
    <JoinCircleModal
      v-model:visible="showApplyModal"
      mode="apply"
      @confirm="handleApply"
    />

    <!-- 密码加入 Modal -->
    <JoinCircleModal
      ref="passwordModalRef"
      v-model:visible="showPasswordModal"
      mode="password"
      @confirm="handlePasswordJoin"
    />

    <!-- 退出确认 -->
    <GovernanceConfirmModal
      v-model:visible="showLeaveModal"
      type="remove"
      :member-name="circle?.name"
      @confirm="confirmLeave"
    />

    <!-- 公告管理弹窗 -->
    <CircleAnnouncementManage
      v-model:visible="showAnnouncementManage"
      :circle-id="circle?.id ?? ''"
      @published="handleAnnouncementPublished"
      @deleted="handleAnnouncementDeleted"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LockOutlined } from '@ant-design/icons-vue';
import { getCircleDetail, joinCircle, leaveCircle } from '/@/api/content/circle';
import { getMemberList } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import type { CircleVO, CircleMemberVO } from '/@/api/content/model/circleModel';
import type { CircleLevelVO } from '/@/api/content/circle/growth';
import { useCircleStoreWithOut } from '/@/store/modules/circle';
import { useCircleGrowthStoreWithOut } from '/@/store/modules/circleGrowth';
import PrivacyBadge from './components/PrivacyBadge.vue';
import JoinStatusButton from './components/JoinStatusButton.vue';
import MemberAvatar from './components/MemberAvatar.vue';
import JoinCircleModal from './components/JoinCircleModal.vue';
import GovernanceConfirmModal from './components/GovernanceConfirmModal.vue';
import CircleContentCard from './components/CircleContentCard.vue';
import type { CircleContentItem } from './components/CircleContentCard.vue';
import CircleAnnouncementBar from './components/CircleAnnouncementBar.vue';
import CircleAnnouncementManage from './components/CircleAnnouncementManage.vue';
import { CircleLevelBadge, CircleLevelProgress } from './components/growth';
import { useGrowthNotification } from './components/growth/useGrowthNotification';
import { togglePin, toggleFeatured } from '/@/api/content/circle/content';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();
const circleStore = useCircleStoreWithOut();
const circleGrowthStore = useCircleGrowthStoreWithOut();

const circle = ref<CircleVO | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const previewMembers = ref<CircleMemberVO[]>([]);

// 圈子等级相关状态
const levelLoading = ref(false);
const levelError = ref<string | null>(null);
const circleLevelInfo = ref<CircleLevelVO | null>(null);
const isPrivateCircle = computed(() => circle.value?.privacyType === 'PRIVATE' || circle.value?.privacyType === 'PASSWORD');

// 弹出层状态
const showApplyModal = ref(false);
const showPasswordModal = ref(false);
const showLeaveModal = ref(false);
const showAnnouncementManage = ref(false);
const feedItems = ref<CircleContentItem[]>([]);

const announcementBarRef = ref<InstanceType<typeof CircleAnnouncementBar>>();
const actionLoading = ref<Record<string, boolean>>({});

async function handleContentAction(action: string, contentId: string) {
  const circleId = circle.value!.id;
  const loadingKey = `${action}-${contentId}`;
  actionLoading.value[loadingKey] = true;

  try {
    switch (action) {
      case 'pin':
        await togglePin(contentId, circleId);
        createMessage.success('已置顶');
        break;
      case 'unpin':
        await togglePin(contentId, circleId);
        createMessage.success('已取消置顶');
        break;
      case 'feature':
        await toggleFeatured(contentId, circleId);
        createMessage.success('已标记精华');
        break;
      case 'unfeature':
        await toggleFeatured(contentId, circleId);
        createMessage.success('已取消精华');
        break;
      case 'report':
        // 举报逻辑——打开 ReportModal（Phase 6 W3 实现）
        createMessage.info('举报功能开发中');
        break;
      case 'delete':
        createMessage.info('删除功能开发中');
        break;
      default:
        createMessage.warning('未知操作');
    }
    // 刷新内容列表
    await fetchFeedItems();
  } catch (e) {
    const err = e as Error;
    createMessage.error(err?.message || '操作失败，请重试');
  } finally {
    delete actionLoading.value[loadingKey];
  }
}

function handleAnnouncementPublished() {
  announcementBarRef.value?.refresh();
}

function handleAnnouncementDeleted() {
  announcementBarRef.value?.refresh();
}

async function fetchFeedItems() {
  try {
    // 降级为空列表，后续对接 GET /api/v1/content/circle/{circleId}/posts
    feedItems.value = [];
  } catch {
    feedItems.value = [];
  }
}

const passwordModalRef = ref();

// 简介展开
const descCollapsed = ref(true);
const descNeedsToggle = computed(() => {
  return (circle.value?.description?.length ?? 0) > 150;
});

// 禁言（后端 CircleMemberVO.status 驱动，当前由成员管理页处理）
const isMuted = computed(() => false);

async function fetchDetail() {
  const id = route.params.id as string;
  if (!id) {
    error.value = 'not-found';
    loading.value = false;
    return;
  }

  loading.value = true;
  error.value = null;
  try {
    const detail = await getCircleDetail(id);
    if (!detail) {
      error.value = 'not-found';
    } else {
      circle.value = detail;
      circleStore.setCurrentCircle(detail);
      // 加载预览成员
      fetchPreviewMembers(id);
      // 加载圈子等级信息
      fetchCircleLevelInfo(id);
    }
  } catch {
    error.value = 'not-found';
  } finally {
    loading.value = false;
  }
}

async function fetchPreviewMembers(circleId: string) {
  try {
    const result = await getMemberList({ circleId, pageNum: 1, pageSize: 10 });
    previewMembers.value = result?.records || [];
  } catch {
    previewMembers.value = [];
  }
}

async function fetchCircleLevelInfo(circleId: string) {
  if (!circleId) return;

  levelLoading.value = true;
  levelError.value = null;
  try {
    const data = await circleGrowthStore.fetchCircleLevel(circleId);
    circleLevelInfo.value = data;
  } catch (e) {
    const err = e as Error;
    levelError.value = err?.message || '获取等级信息失败';
    circleLevelInfo.value = null;
  } finally {
    levelLoading.value = false;
  }
}

function retryFetchLevel() {
  if (circle.value) {
    fetchCircleLevelInfo(circle.value.id);
  }
}

// 加入成功回调
function handleJoinSuccess() {
  fetchDetail();
}

// 申请加入
async function handleApply() {
  try {
    await joinCircle({ circleId: circle.value!.id });
    createMessage.success('申请已提交，请等待审核');
    showApplyModal.value = false;
    fetchDetail();
  } catch (error: any) {
    createMessage.error(error?.message || '申请失败');
  }
}

// 密码加入
async function handlePasswordJoin(data: { password?: string }) {
  try {
    await joinCircle({ circleId: circle.value!.id, password: data.password });
    createMessage.success('加入成功');
    showPasswordModal.value = false;
    fetchDetail();
  } catch (error: any) {
    const msg = error?.message || '加入失败';
    if (msg.includes('密码错误')) {
      passwordModalRef.value?.setError(msg);
    } else if (msg.includes('次数过多')) {
      passwordModalRef.value?.setError(msg);
      passwordModalRef.value?.setLocked(true);
    } else {
      createMessage.error(msg);
    }
  }
}

// 退出
function handleLeave() {
  if (circleStore.isCreator) {
    createMessage.warning('创建者不可退出圈子');
    return;
  }
  showLeaveModal.value = true;
}

async function confirmLeave() {
  try {
    await leaveCircle({ circleId: circle.value!.id });
    createMessage.success('已退出圈子');
    showLeaveModal.value = false;
    circleStore.clearCurrentCircle();
    router.push('/circle/list');
  } catch (error: any) {
    createMessage.error(error?.message || '退出失败');
  }
}

// 导航
function goEdit() {
  router.push(`/circle/${circle.value!.id}/edit`);
}
function goMembers() {
  router.push(`/circle/${circle.value!.id}/members`);
}
function goGovernanceLog() {
  router.push(`/circle/${circle.value!.id}/governance-log`);
}
function goList() {
  router.push('/circle/list');
}

// 获取当前圈子ID
function getCurrentCircleId(): string | undefined {
  return circle.value?.id || (route.params.id as string);
}

// 刷新圈子等级信息
function refreshCircleData() {
  const id = getCurrentCircleId();
  if (id) {
    fetchCircleLevelInfo(id);
  }
}

// 注册成长通知监听
useGrowthNotification(getCurrentCircleId, refreshCircleData);

// 监听路由参数变化
watch(() => route.params.id, () => {
  if (route.params.id) fetchDetail();
});

onMounted(() => fetchDetail());
</script>

<style lang="less" scoped>
.circle-detail-page {
  min-height: calc(100vh - 64px);
  background: var(--background-color-base, #f5f5f5);
}

.detail-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
  }
}

.detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px 40px;
  margin-top: -24px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;

  .header-main {
    display: flex;
    gap: 12px;
    flex: 1;
    min-width: 0;
  }

  .header-icon {
    width: 72px;
    height: 72px;
    border-radius: 14px;
    border: 3px solid var(--component-background, #fff);
    background: var(--component-background, #fff);
    object-fit: cover;
    flex-shrink: 0;
    margin-top: -20px;
  }

  .header-info {
    flex: 1;
    min-width: 0;
    padding-top: 4px;
  }

  .header-title-row {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .header-name {
    font-size: 22px;
    font-weight: 700;
    margin: 0;
    line-height: 1.3;
  }

  .header-meta {
    display: flex;
    gap: 12px;
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    margin-top: 6px;

    .meta-category {
      padding: 1px 8px;
      background: var(--background-color-base, #f5f5f5);
      border-radius: 4px;
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
    padding-top: 12px;
    flex-wrap: wrap;
  }
}

.muted-banner {
  margin-bottom: 16px;
}

.circle-level-section {
  .level-header {
    margin-bottom: 16px;
  }

  .level-private-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 32px 16px;
    color: var(--text-color-secondary, #999);
    font-size: 14px;
    background: var(--background-color-base, #f5f5f5);
    border-radius: 8px;

    .lock-icon {
      font-size: 18px;
      color: var(--text-color-tertiary, #ccc);
    }
  }

  .level-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 24px;
  }
}

.detail-section {
  background: var(--component-background, #fff);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 10px;
  }
}

.description-text {
  font-size: 14px;
  color: var(--text-color-secondary, #666);
  line-height: 1.7;
  white-space: pre-wrap;

  &.is-collapsed {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.detail-tabs {
  background: var(--component-background, #fff);
  border-radius: 12px;
  padding: 0 20px 20px;

  :deep(.ant-tabs-nav) {
    margin-bottom: 16px;
  }
}

.feed-list { display: flex; flex-direction: column; }
.member-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

// 响应式
@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;

    .header-icon {
      width: 56px;
      height: 56px;
    }

    .header-name {
      font-size: 18px;
    }

    .header-actions {
      padding-top: 8px;
      width: 100%;
    }
  }

  .detail-container {
    padding: 0 12px 24px;
  }
}
</style>
