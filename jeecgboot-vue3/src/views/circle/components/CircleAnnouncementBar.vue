<template>
  <a-alert
    v-if="announcement"
    type="info"
    :closable="false"
    class="announcement-bar"
    banner
  >
    <template #message>
      <div class="announcement-content">
        <span class="announcement-label">📢 公告</span>
        <span :class="['announcement-text', { 'is-collapsed': collapsed }]">
          {{ announcement.content }}
        </span>
        <a-button
          v-if="needsToggle"
          type="link"
          size="small"
          @click="collapsed = !collapsed"
        >
          {{ collapsed ? '展开' : '收起' }}
        </a-button>
      </div>
    </template>
  </a-alert>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { getActiveCircleAnnouncement } from '/@/api/content/circle/announcement';
import type { CircleAnnouncementVO } from '/@/api/content/circle/model/circleAnnouncementModel';

const props = defineProps<{
  circleId: string;
}>();

const announcement = ref<CircleAnnouncementVO | null>(null);
const collapsed = ref(true);
let expiryTimer: ReturnType<typeof setInterval> | null = null;

const needsToggle = computed(() => {
  return (announcement.value?.content?.length ?? 0) > 150;
});

function checkExpiry() {
  if (announcement.value?.expireAt) {
    if (Date.now() > new Date(announcement.value.expireAt).getTime()) {
      announcement.value = null;
    }
  }
}

async function loadAnnouncement() {
  try {
    const res = await getActiveCircleAnnouncement(props.circleId);
    if (res) {
      // 检查是否已过期
      if (res.expireAt) {
        const expireTime = new Date(res.expireAt).getTime();
        if (Date.now() > expireTime) {
          announcement.value = null;
          return;
        }
      }
      announcement.value = res;
      collapsed.value = true;
    } else {
      announcement.value = null;
    }
  } catch {
    // 静默失败，不影响页面
    announcement.value = null;
  }
}

// 外部可调用刷新（如发布/删除公告后）
function refresh() {
  loadAnnouncement();
}

defineExpose({ refresh });

onMounted(() => {
  loadAnnouncement();
  // 每 60 秒检查一次是否过期
  expiryTimer = setInterval(checkExpiry, 60000);
});

onUnmounted(() => {
  if (expiryTimer) {
    clearInterval(expiryTimer);
    expiryTimer = null;
  }
});
</script>

<style lang="less" scoped>
.announcement-bar {
  margin-bottom: 12px;
  border-radius: 6px;
}

.announcement-content {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-wrap: wrap;
}

.announcement-label {
  font-weight: 600;
  flex-shrink: 0;
  font-size: 13px;
}

.announcement-text {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  line-height: 1.6;

  &.is-collapsed {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}
</style>
