<template>
  <div class="my-badges">
    <a-page-header title="我的勋章" :back-icon="true" @back="$router.back()" />

    <!-- 统计栏 -->
    <div class="my-badges__stats" v-if="!loading">
      <a-space :size="24">
        <a-statistic title="已获得勋章" :value="earnedBadges.length" />
        <a-statistic title="佩戴中" :value="wornBadgeIds.length" />
      </a-space>
    </div>

    <!-- 操作栏 -->
    <div class="my-badges__actions" v-if="!loading">
      <a-button v-if="!editMode" type="primary" @click="enterEditMode">编辑佩戴</a-button>
      <a-space v-else>
        <a-button type="primary" :loading="saving" @click="onSaveWorn">保存</a-button>
        <a-button @click="cancelEditMode">取消</a-button>
      </a-space>
    </div>

    <!-- 加载骨架屏 -->
    <template v-if="loading">
      <a-row :gutter="[16, 16]">
        <a-col v-for="i in 8" :key="i" :xs="12" :sm="8" :md="6">
          <a-card :body-style="{ padding: '12px', textAlign: 'center' }">
            <a-skeleton active :paragraph="false" />
            <a-skeleton-input style="width: 60%; margin: 8px auto" size="small" active />
          </a-card>
        </a-col>
      </a-row>
    </template>

    <!-- 分类标签页 + 勋章网格 -->
    <template v-else>
      <a-tabs v-model:activeKey="activeCategory" class="my-badges__tabs">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane
          v-for="cat in catalog"
          :key="cat.categoryCode"
          :tab="cat.categoryName"
        />
      </a-tabs>

      <BadgeGrid
        :badges="filteredBadges"
        :selectable="editMode"
        v-model:selectedIds="editSelectedIds"
        :max-select="5"
        @click:badge="onBadgeClick"
      />
    </template>

    <!-- 勋章详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      :footer="null"
      :width="420"
      :title="detailBadge?.badgeName"
    >
      <BadgeDetail v-if="detailBadge" :badge="detailBadge" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import type { BadgeCatalogVO, BadgeDetailVO } from '/@/api/content/growth/badge-types';
  import { useBadgeStore } from '/@/store/modules/badge';
  import BadgeGrid from '/@/components/content/BadgeGrid/index.vue';
  import BadgeDetail from '/@/components/content/BadgeDetail/index.vue';

  const badgeStore = useBadgeStore();

  const loading = ref(true);
  const saving = ref(false);
  const editMode = ref(false);
  const catalog = ref<BadgeCatalogVO[]>([]);
  const activeCategory = ref('all');
  const detailVisible = ref(false);
  const detailBadge = ref<BadgeDetailVO | null>(null);
  const editSelectedIds = ref<string[]>([]);

  /** 当前用户 ID */
  const userId = ref('');

  /** 已获得的勋章（扁平化所有分类） */
  const earnedBadges = computed<BadgeDetailVO[]>(() => {
    const all: BadgeDetailVO[] = [];
    for (const cat of catalog.value) {
      if (cat.badges) {
        all.push(...cat.badges.filter((b) => b.earned));
      }
    }
    return all;
  });

  /** 佩戴中的勋章 ID */
  const wornBadgeIds = computed(() => badgeStore.getWornBadges.map((b) => b.badgeId));

  /** 按当前选中分类过滤勋章 */
  const filteredBadges = computed<BadgeDetailVO[]>(() => {
    if (activeCategory.value === 'all') {
      const all: BadgeDetailVO[] = [];
      for (const cat of catalog.value) {
        if (cat.badges) all.push(...cat.badges);
      }
      return all;
    }
    const cat = catalog.value.find((c) => c.categoryCode === activeCategory.value);
    return cat?.badges || [];
  });

  onMounted(async () => {
    try {
      const { useUserStore } = await import('/@/store/modules/user');
      const userStore = useUserStore();
      const uid = (userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '';
      if (!uid) {
        message.error('未识别当前用户');
        return;
      }
      userId.value = String(uid);
      const [catData] = await Promise.all([
        badgeStore.loadCatalog(),
        badgeStore.loadWornBadges(userId.value),
      ]);
      catalog.value = catData;
    } catch (e: any) {
      message.error(e?.message || '加载勋章数据失败');
    } finally {
      loading.value = false;
    }
  });

  function onBadgeClick(badge: BadgeDetailVO) {
    detailBadge.value = badge;
    detailVisible.value = true;
  }

  function enterEditMode() {
    editSelectedIds.value = [...wornBadgeIds.value];
    editMode.value = true;
  }

  function cancelEditMode() {
    editMode.value = false;
    editSelectedIds.value = [];
  }

  async function onSaveWorn() {
    if (editSelectedIds.value.length > 5) {
      message.warning('最多佩戴 5 枚勋章');
      return;
    }
    saving.value = true;
    try {
      await badgeStore.saveWorn({ badgeIds: editSelectedIds.value });
      // 刷新佩戴缓存
      await badgeStore.loadWornBadges(userId.value, true);
      editMode.value = false;
      message.success('佩戴设置已保存');
    } catch (e: any) {
      message.error(e?.message || '保存失败');
    } finally {
      saving.value = false;
    }
  }
</script>

<style scoped>
  .my-badges {
    padding: 0 16px 24px;
  }
  .my-badges__stats {
    margin-bottom: 16px;
  }
  .my-badges__actions {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
  }
  .my-badges__tabs {
    margin-bottom: 16px;
  }
</style>
