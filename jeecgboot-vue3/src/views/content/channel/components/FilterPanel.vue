<template>
  <div class="filter-panel" :class="{ 'filter-panel--collapsed': collapsed && isMobile }">
    <!-- 移动端折叠按钮 -->
    <a-button v-if="isMobile" type="default" size="small" @click="collapsed = !collapsed">
      <template #icon><FilterOutlined /></template>
      筛选
      <span v-if="activeFilterCount > 0" class="filter-panel__count">{{ activeFilterCount }}</span>
    </a-button>

    <div v-show="!collapsed || !isMobile" class="filter-panel__content">
      <!-- 频道类型多选 -->
      <div v-if="filters.channelType" class="filter-panel__group">
        <span class="filter-panel__label">频道类型</span>
        <a-checkbox-group
          :value="values.channelType || []"
          :options="channelTypeOptions"
          @change="handleChannelTypeChange"
        />
      </div>

      <!-- 分类树选择 -->
      <div v-if="filters.category" class="filter-panel__group">
        <span class="filter-panel__label">分类</span>
        <a-tree-select
          :value="values.categoryId"
          :tree-data="categoryTreeData"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          placeholder="选择分类"
          allow-clear
          style="min-width: 160px"
          @change="handleCategoryChange"
        />
      </div>

      <!-- 排序单选 -->
      <div v-if="filters.sortBy" class="filter-panel__group">
        <span class="filter-panel__label">排序</span>
        <a-radio-group :value="values.sortBy" size="small" @change="handleSortChange">
          <a-radio-button v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </a-radio-button>
        </a-radio-group>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { FilterOutlined } from '@ant-design/icons-vue';
import type { CategoryTreeVO } from '/@/api/content/model/channelDiscoveryModel';

interface FilterConfig {
  channelType?: boolean;
  category?: boolean;
  sortBy?: boolean;
}

interface FilterValues {
  channelType?: string[];
  categoryId?: string;
  sortBy?: string;
}

interface SortOption {
  label: string;
  value: string;
}

interface Props {
  filters?: FilterConfig;
  values?: FilterValues;
  categories?: CategoryTreeVO[];
  sortOptions?: SortOption[];
  collapsible?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  filters: () => ({ channelType: true, category: true, sortBy: true }),
  values: () => ({}),
  categories: () => [],
  sortOptions: () => [
    { label: '订阅数', value: 'subscriber' },
    { label: '活跃度', value: 'active' },
    { label: '创建时间', value: 'created' },
  ],
  collapsible: true,
});

const emit = defineEmits<{
  (e: 'change', values: FilterValues): void;
  (e: 'update:values', values: FilterValues): void;
}>();

const collapsed = ref(true);

const channelTypeOptions = [
  { label: '个人', value: 'personal' },
  { label: '组织', value: 'organization' },
  { label: '官方', value: 'system' },
];

const isMobile = ref(false);

// 检测是否为移动端
if (typeof window !== 'undefined') {
  isMobile.value = window.innerWidth < 768;
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth < 768;
  });
}

// 将分类树转为 a-tree-select 需要的格式
const categoryTreeData = computed(() => {
  return transformToTreeData(props.categories);
});

function transformToTreeData(nodes: CategoryTreeVO[]): any[] {
  return nodes
    .filter((n) => n.status === 'enabled')
    .map((node) => ({
      id: node.id,
      name: node.name,
      value: node.id,
      children: node.children?.length ? transformToTreeData(node.children) : undefined,
    }));
}

const activeFilterCount = computed(() => {
  let count = 0;
  if (props.values.channelType?.length) count++;
  if (props.values.categoryId) count++;
  if (props.values.sortBy && props.values.sortBy !== 'relevance') count++;
  return count;
});

function emitChange() {
  emit('change', { ...props.values });
  emit('update:values', { ...props.values });
}

function handleChannelTypeChange(checkedValues: string[]) {
  const newValues = { ...props.values, channelType: checkedValues };
  emit('change', newValues);
  emit('update:values', newValues);
}

function handleCategoryChange(value: string | undefined) {
  const newValues = { ...props.values, categoryId: value };
  emit('change', newValues);
  emit('update:values', newValues);
}

function handleSortChange(e: any) {
  const newValues = { ...props.values, sortBy: e.target.value };
  emit('change', newValues);
  emit('update:values', newValues);
}
</script>

<style lang="less" scoped>
.filter-panel {
  &__content {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    align-items: center;
    padding: 8px 0;
  }

  &__group {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__label {
    font-size: 13px;
    color: #666;
    white-space: nowrap;
  }

  &__count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    background: #1677ff;
    color: #fff;
    border-radius: 50%;
    font-size: 10px;
    margin-left: 4px;
  }
}

// 移动端折叠
@media (max-width: 767px) {
  .filter-panel--collapsed {
    .filter-panel__content {
      display: none;
    }
  }

  .filter-panel:not(.filter-panel--collapsed) {
    .filter-panel__content {
      flex-direction: column;
      align-items: stretch;
      background: #fff;
      border-radius: 8px;
      padding: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .filter-panel__group {
      flex-wrap: wrap;
    }
  }
}
</style>
