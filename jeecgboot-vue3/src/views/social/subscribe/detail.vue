<template>
  <div class="subscribe-detail-page">
    <a-spin :spinning="loading">
      <template v-if="detail">
        <div class="subscribe-detail-page__info">
          <div class="subscribe-detail-page__info-header">
            <div class="subscribe-detail-page__icon">
              <img v-if="detail.sourceIcon" :src="detail.sourceIcon" :alt="detail.sourceName" />
              <AppstoreOutlined v-else />
            </div>
            <div class="subscribe-detail-page__info-body">
              <h2 class="subscribe-detail-page__name">{{ detail.sourceName }}</h2>
              <div class="subscribe-detail-page__meta">
                <a-tag v-if="detail.sourceType" color="blue">{{ detail.sourceType }}</a-tag>
                <a-tag v-if="detail.category" color="cyan">{{ detail.category }}</a-tag>
                <span class="subscribe-detail-page__count">
                  <UserOutlined />
                  {{ detail.subscriberCount }} 人订阅
                </span>
              </div>
              <p v-if="detail.description" class="subscribe-detail-page__desc">{{ detail.description }}</p>
            </div>
          </div>
          <div class="subscribe-detail-page__actions">
            <SubscribeButton
              :user-id="currentUserId"
              :source-id="detail.sourceId"
              :source-type="detail.sourceType"
              :is-subscribed="detail.isSubscribed"
              @subscribe="handleSubscribed"
              @unsubscribe="handleUnsubscribed"
            />
            <a-button @click="goToNotification">通知设置</a-button>
          </div>
        </div>

        <div class="subscribe-detail-page__content">
          <h3 class="subscribe-detail-page__section-title">最近内容</h3>
          <div v-if="detail.recentContent && detail.recentContent.length > 0" class="subscribe-detail-page__content-list">
            <div
              v-for="item in pagedContent"
              :key="item.contentId"
              class="subscribe-detail-page__content-item"
              @click="goToContent(item.contentId)"
            >
              <div class="subscribe-detail-page__content-title">{{ item.title }}</div>
              <div v-if="item.summary" class="subscribe-detail-page__content-summary">{{ item.summary }}</div>
              <div class="subscribe-detail-page__content-time">{{ item.createTime }}</div>
            </div>
            <div v-if="contentTotal > contentPagination.pageSize" class="subscribe-detail-page__pagination">
              <a-pagination
                v-model:current="contentPagination.current"
                :page-size="contentPagination.pageSize"
                :total="contentTotal"
                size="small"
                show-less-items
              />
            </div>
          </div>
          <a-empty v-else description="暂无内容" />
        </div>
      </template>
      <a-empty v-else-if="!loading" description="未找到该订阅源" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { AppstoreOutlined, UserOutlined } from '@ant-design/icons-vue';
import { useSubscribeStore } from '/@/store/modules/subscribe';
import { useUserStore } from '/@/store/modules/user';
import SubscribeButton from '/@/components/social/SubscribeButton.vue';

interface ContentItem {
  contentId: string;
  title: string;
  summary: string;
  createTime: string;
}

interface SourceDetail {
  sourceId: string;
  sourceName: string;
  sourceIcon: string;
  sourceType: string;
  category: string;
  subscriberCount: number;
  description: string;
  isSubscribed: boolean;
  recentContent: ContentItem[];
}

const route = useRoute();
const router = useRouter();
const subscribeStore = useSubscribeStore();
const userStore = useUserStore();

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');
const sourceId = computed(() => (route.query.sourceId as string) || (route.params.sourceId as string) || '');
const detail = ref<SourceDetail | null>(null);
const loading = ref(false);

// 内容列表分页
const contentPagination = reactive({ current: 1, pageSize: 10 });
const pagedContent = computed(() => {
  const list = detail.value?.recentContent ?? [];
  const start = (contentPagination.current - 1) * contentPagination.pageSize;
  return list.slice(start, start + contentPagination.pageSize);
});
const contentTotal = computed(() => detail.value?.recentContent?.length ?? 0);

function handleSubscribed() {
  if (detail.value) {
    detail.value.isSubscribed = true;
    detail.value.subscriberCount++;
  }
}

function handleUnsubscribed() {
  if (detail.value) {
    detail.value.isSubscribed = false;
    detail.value.subscriberCount = Math.max(0, detail.value.subscriberCount - 1);
  }
}

function goToNotification() {
  router.push({ path: '/social/subscribe/notification', query: { sourceId: sourceId.value } });
}

function goToContent(contentId: string) {
  window.open(`/social/content/detail?contentId=${contentId}`, '_blank', 'noopener,noreferrer');
}

onMounted(async () => {
  if (!sourceId.value) return;
  loading.value = true;
  try {
    const res = await subscribeStore.fetchSourceDetail(sourceId.value);
    detail.value = res;
  } catch (error) {
    console.error('[SubscribeDetail] fetch detail failed:', error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.subscribe-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__info {
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 24px;
  }

  &__info-header {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16px;
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 16px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .anticon {
      font-size: 28px;
      color: #999;
    }
  }

  &__info-body {
    flex: 1;
    min-width: 0;
  }

  &__name {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  &__count {
    font-size: 13px;
    color: #666;

    .anticon {
      margin-right: 4px;
    }
  }

  &__desc {
    font-size: 13px;
    color: #666;
    margin-top: 10px;
    margin-bottom: 0;
    line-height: 1.6;
  }

  &__actions {
    display: flex;
    gap: 8px;
  }

  &__content {
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    padding: 20px;
  }

  &__section-title {
    margin: 0 0 16px;
    font-size: 16px;
    font-weight: 600;
  }

  &__content-list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__content-item {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #fafafa;
    }
  }

  &__content-title {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    margin-bottom: 4px;
  }

  &__content-summary {
    font-size: 13px;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__content-time {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }

  &__pagination {
    display: flex;
    justify-content: center;
    padding: 16px 0 0;
  }
}
</style>
