<template>
  <span class="verification-badge-wrapper" v-if="visibleBadges.length > 0">
    <!-- Visible badges (top 2) -->
    <a-tooltip v-for="badge in visibleBadges" :key="badge.badgeId" :title="getBadgeStyle(badge.visualStyleKey).tooltip">
      <span
        class="verification-badge"
        :style="getBadgeInlineStyle(badge)"
        @click="openDetail(badge)"
      >
        <Icon icon="mdi:check-decagram" :size="14" />
      </span>
    </a-tooltip>

    <!-- Overflow "+N" pill -->
    <a-popover
      v-if="overflowCount > 0"
      :open="popoverOpen"
      trigger="click"
      placement="bottom"
    >
      <template #content>
        <div class="badge-popover-content">
          <a-tooltip v-for="badge in allKnownBadges" :key="badge.badgeId" :title="getBadgeStyle(badge.visualStyleKey).tooltip">
            <span
              class="verification-badge"
              :style="getBadgeInlineStyle(badge)"
              @click="openDetail(badge)"
            >
              <Icon icon="mdi:check-decagram" :size="14" />
            </span>
          </a-tooltip>
        </div>
      </template>
      <span
        class="badge-overflow"
        @click="togglePopover"
      >+{{ overflowCount }}</span>
    </a-popover>

    <!-- Detail: PC modal -->
    <a-modal
      v-if="!isMobile"
      :open="detailVisible"
      :width="400"
      :title="detailData?.badgeLabel ?? ''"
      :footer="null"
      @cancel="closeDetail"
    >
      <div v-if="detailLoading" class="detail-loading">
        <a-spin />
      </div>
      <div v-else-if="detailError" class="detail-error">
        {{ detailError }}
      </div>
      <div v-else-if="detailData" class="detail-content">
        <p v-if="detailData.description && !isStructuredDescription(detailData.badgeType)">
          {{ detailData.description }}
        </p>
        <template v-if="parsedDescription">
          <p v-if="parsedDescription.companyName">
            企业名称：{{ parsedDescription.companyName }}
          </p>
          <p v-if="parsedDescription.field">
            认证领域：{{ parsedDescription.field }}
          </p>
        </template>
        <p v-if="detailData.verifiedAt">
          认证时间：{{ formatDate(detailData.verifiedAt) }}
        </p>
      </div>
    </a-modal>

    <!-- Detail: Mobile drawer -->
    <a-drawer
      v-if="isMobile"
      :open="detailVisible"
      placement="bottom"
      :width="'100%'"
      :title="detailData?.badgeLabel ?? ''"
      :closable="true"
      @close="closeDetail"
    >
      <div v-if="detailLoading" class="detail-loading">
        <a-spin />
      </div>
      <div v-else-if="detailError" class="detail-error">
        {{ detailError }}
      </div>
      <div v-else-if="detailData" class="detail-content">
        <p v-if="detailData.description && !isStructuredDescription(detailData.badgeType)">
          {{ detailData.description }}
        </p>
        <template v-if="parsedDescription">
          <p v-if="parsedDescription.companyName">
            企业名称：{{ parsedDescription.companyName }}
          </p>
          <p v-if="parsedDescription.field">
            认证领域：{{ parsedDescription.field }}
          </p>
        </template>
        <p v-if="detailData.verifiedAt">
          认证时间：{{ formatDate(detailData.verifiedAt) }}
        </p>
      </div>
    </a-drawer>
  </span>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Icon } from '/@/components/Icon';
import type { ContentUserVerificationBadgeVO } from '/@/api/content/profile/types';
import { getBadgeStyle, partitionBadges } from './badgeStyle';
import { getBadgeDetail } from '/@/api/content/profile';

const MAX_VISIBLE = 2;

const props = defineProps<{
  badges: ContentUserVerificationBadgeVO[];
}>();

// --- C4: partition + top-N ---
const partitioned = computed(() => partitionBadges(props.badges));
const allKnownBadges = computed(() => partitioned.value.known);
const visibleBadges = computed(() => allKnownBadges.value.slice(0, MAX_VISIBLE));
const overflowCount = computed(() => Math.max(0, allKnownBadges.value.length - MAX_VISIBLE));

const popoverOpen = ref(false);
function togglePopover() {
  popoverOpen.value = !popoverOpen.value;
}

// --- Responsive: PC vs Mobile ---
const isMobile = ref(window.innerWidth < 768);
// Listen for resize
if (typeof window !== 'undefined') {
  const onResize = () => { isMobile.value = window.innerWidth < 768; };
  // Use passive listener; cleanup not needed for root-level component
  window.addEventListener('resize', onResize, { passive: true });
}

// --- C3: badge detail modal/drawer ---
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailError = ref<string | null>(null);
const detailData = ref<ContentUserVerificationBadgeVO | null>(null);

async function openDetail(badge: ContentUserVerificationBadgeVO) {
  detailVisible.value = true;
  detailLoading.value = true;
  detailError.value = null;
  detailData.value = null;
  // Close popover when opening detail
  popoverOpen.value = false;
  try {
    detailData.value = await getBadgeDetail(badge.badgeId);
  } catch (e: any) {
    detailError.value = e?.message ?? '加载失败';
  } finally {
    detailLoading.value = false;
  }
}

function closeDetail() {
  detailVisible.value = false;
  detailData.value = null;
  detailError.value = null;
}

// --- Helpers ---
function getBadgeInlineStyle(badge: ContentUserVerificationBadgeVO) {
  const style = getBadgeStyle(badge.visualStyleKey);
  return {
    backgroundColor: style.backgroundColor,
    borderColor: style.borderColor,
    color: style.iconColor,
  };
}

function isStructuredDescription(badgeType?: string): boolean {
  return badgeType === 'ENTERPRISE' || badgeType === 'CREATOR';
}

const parsedDescription = computed(() => {
  if (!detailData.value?.description) return null;
  if (!isStructuredDescription(detailData.value.badgeType)) return null;
  try {
    return JSON.parse(detailData.value.description);
  } catch {
    return null;
  }
});

function formatDate(iso: string): string {
  return iso.slice(0, 10);
}
</script>

<style scoped>
.verification-badge-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  vertical-align: middle;
}
.verification-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1.5px solid;
  vertical-align: middle;
  cursor: pointer;
}
.badge-overflow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 18px;
  padding: 0 4px;
  border-radius: 9px;
  background-color: #f0f0f0;
  border: 1px solid #d9d9d9;
  color: #666;
  font-size: 11px;
  cursor: pointer;
  vertical-align: middle;
  user-select: none;
}
.badge-popover-content {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 4px 0;
}
.detail-loading,
.detail-error,
.detail-content {
  min-height: 60px;
}
.detail-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}
.detail-error {
  color: #ff4d4f;
}
</style>
