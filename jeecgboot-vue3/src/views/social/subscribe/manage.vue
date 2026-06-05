<template>
  <div class="subscribe-manage-page">
    <div class="subscribe-manage-page__header">
      <h2 class="subscribe-manage-page__title">订阅管理</h2>
      <span class="subscribe-manage-page__count">共 {{ subscribeStore.totalSubscribes }} 个</span>
    </div>

    <div class="subscribe-manage-page__toolbar">
      <a-input-search
        v-model:value="searchValue"
        placeholder="搜索订阅源"
        :style="{ width: isMobile ? '100%' : '300px' }"
        allow-clear
        @search="handleSearch"
        @change="handleSearchChange"
      />
      <a-select
        v-model:value="selectedSourceType"
        placeholder="全部类型"
        allow-clear
        style="width: 140px"
        @change="handleSourceTypeChange"
      >
        <a-select-option value="">全部类型</a-select-option>
        <a-select-option v-for="t in sourceTypes" :key="t" :value="t">{{ t }}</a-select-option>
      </a-select>
      <a-button @click="toggleBatchMode">{{ batchMode ? '退出批量' : '批量管理' }}</a-button>
    </div>

    <a-spin :spinning="subscribeStore.loading">
      <div v-if="subscribeStore.subscribeList.length > 0" class="subscribe-manage-page__list">
        <div v-for="item in subscribeStore.subscribeList" :key="item.id" class="subscribe-manage-page__item">
          <a-checkbox
            v-if="batchMode"
            :checked="selectedIds.has(item.sourceId)"
            class="subscribe-manage-page__checkbox"
            @change="toggleSelect(item.sourceId)"
          />
          <div class="subscribe-manage-page__card-wrapper" @click="goToDetail(item.sourceId)">
            <SubscriptionCard
              :user-id="currentUserId"
              :source="{
                sourceId: item.sourceId,
                sourceName: item.sourceName,
                sourceIcon: item.sourceIcon,
                sourceType: item.sourceType,
                category: item.category,
                subscriberCount: item.subscriberCount,
                lastUpdateTime: item.lastUpdateTime,
                status: item.status,
              }"
              @unsubscribe="handleUnsubscribe"
              @pause="handlePause"
              @resume="handleResume"
            />
          </div>
          <a-button
            v-if="!batchMode"
            type="link"
            size="small"
            class="subscribe-manage-page__notif-btn"
            @click.stop="goToNotification(item.sourceId)"
          >
            通知设置
          </a-button>
        </div>
      </div>
      <a-empty v-else-if="!subscribeStore.loading" description="还没有订阅任何内容源">
        <a-button type="primary" @click="goToSquare">去订阅广场</a-button>
      </a-empty>
    </a-spin>

    <div v-if="subscribeStore.hasMore && subscribeStore.subscribeList.length > 0" class="subscribe-manage-page__load-more">
      <a-button :loading="subscribeStore.loading" @click="loadMore">加载更多</a-button>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="batchMode" class="subscribe-manage-page__batch-bar">
      <div class="subscribe-manage-page__batch-info">
        已选 {{ selectedIds.size }} 项
      </div>
      <div class="subscribe-manage-page__batch-actions">
        <a-button :disabled="selectedIds.size === 0" @click="handleBatchPause">批量暂停</a-button>
        <a-button :disabled="selectedIds.size === 0" @click="handleBatchResume">批量恢复</a-button>
        <a-popconfirm
          title="确定取消选中的订阅吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="handleBatchCancel"
        >
          <a-button :disabled="selectedIds.size === 0" danger>批量取消</a-button>
        </a-popconfirm>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useSubscribeStore } from '/@/store/modules/subscribe';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import SubscriptionCard from '/@/components/social/SubscriptionCard.vue';

const router = useRouter();
const subscribeStore = useSubscribeStore();
const userStore = useUserStore();
const { createMessage } = useMessage();
const { screenRef } = useBreakpoint();
const isMobile = computed(() => screenRef.value === 'XS' || screenRef.value === 'SM');

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');
const searchValue = ref('');
const selectedSourceType = ref('');
const sourceTypes = ['专题', '话题', '栏目', '频道'];

// 批量模式
const batchMode = ref(false);
const selectedIds = ref(new Set<string>());

let debounceTimer: ReturnType<typeof setTimeout> | null = null;

function handleSearch(value: string) {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
  subscribeStore.setSearchKeyword(value);
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

function handleSearchChange() {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    subscribeStore.setSearchKeyword(searchValue.value);
    subscribeStore.fetchSubscribeList(currentUserId.value, true);
  }, 300);
}

function handleSourceTypeChange(value: string) {
  subscribeStore.setSelectedSourceType(value || '');
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

function handleUnsubscribe() {
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

function handlePause(_sourceId: string) {
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

function handleResume(_sourceId: string) {
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

function loadMore() {
  subscribeStore.fetchSubscribeList(currentUserId.value);
}

function goToSquare() {
  router.push({ path: '/social/subscribe/square' });
}

function goToDetail(sourceId: string) {
  router.push({ path: '/social/subscribe/detail', query: { sourceId } });
}

function goToNotification(sourceId: string) {
  router.push({ path: '/social/subscribe/notification', query: { sourceId } });
}

function toggleBatchMode() {
  batchMode.value = !batchMode.value;
  if (!batchMode.value) {
    selectedIds.value = new Set();
  }
}

function toggleSelect(sourceId: string) {
  const newSet = new Set(selectedIds.value);
  if (newSet.has(sourceId)) {
    newSet.delete(sourceId);
  } else {
    newSet.add(sourceId);
  }
  selectedIds.value = newSet;
}

async function handleBatchPause() {
  try {
    await subscribeStore.batchPause(currentUserId.value, Array.from(selectedIds.value));
    createMessage.success('批量暂停成功');
    selectedIds.value = new Set();
  } catch (error) {
    console.error('[SubscribeManage] batch pause failed:', error);
    createMessage.error('批量暂停失败');
  }
}

async function handleBatchResume() {
  try {
    await subscribeStore.batchResume(currentUserId.value, Array.from(selectedIds.value));
    createMessage.success('批量恢复成功');
    selectedIds.value = new Set();
  } catch (error) {
    console.error('[SubscribeManage] batch resume failed:', error);
    createMessage.error('批量恢复失败');
  }
}

async function handleBatchCancel() {
  try {
    await subscribeStore.batchCancel(currentUserId.value, Array.from(selectedIds.value));
    createMessage.success('批量取消成功');
    selectedIds.value = new Set();
  } catch (error) {
    console.error('[SubscribeManage] batch cancel failed:', error);
    createMessage.error('批量取消失败');
  }
}

onMounted(() => {
  subscribeStore.fetchSubscribeList(currentUserId.value, true).catch(console.error);
});

onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
});
</script>

<style scoped lang="less">
.subscribe-manage-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 16px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__count {
    font-size: 14px;
    color: #999;
  }

  &__toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }

  &__list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__item {
    display: flex;
    align-items: flex-start;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  &__checkbox {
    padding: 16px 0 16px 16px;
    flex-shrink: 0;
  }

  &__card-wrapper {
    flex: 1;
    min-width: 0;
    cursor: pointer;
  }

  &__notif-btn {
    flex-shrink: 0;
    align-self: center;
    margin-right: 8px;
  }

  &__load-more {
    text-align: center;
    padding: 16px 0;
  }

  &__batch-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 24px;
    background: #fff;
    border-top: 1px solid #f0f0f0;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
    z-index: 100;
  }

  &__batch-info {
    font-size: 14px;
    color: #666;
  }

  &__batch-actions {
    display: flex;
    gap: 8px;
  }

  @media (max-width: 767px) {
    padding: 16px 12px;

    &__toolbar {
      flex-direction: column;
      align-items: stretch;
    }

    &__batch-bar {
      flex-direction: column;
      gap: 8px;
      padding: 12px 16px;
    }

    &__batch-actions {
      flex-wrap: wrap;
    }
  }
}
</style>
