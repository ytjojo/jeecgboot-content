<template>
  <div class="category-tree-nav" :class="[`category-tree-nav--${mode}`]">
    <a-tree
      :tree-data="treeData"
      :selected-keys="selectedKeys"
      :expanded-keys="expandedKeys"
      :field-names="{ title: 'name', key: 'id', children: 'children' }"
      :show-line="mode !== 'select'"
      :block-node="true"
      @select="handleSelect"
      @expand="handleExpand"
    >
      <template #title="{ name, status }">
        <span class="category-tree-nav__node">
          <span class="category-tree-nav__label">{{ name }}</span>
          <a-tag v-if="status === 'disabled'" color="default" size="small" class="category-tree-nav__status">
            已停用
          </a-tag>
        </span>
      </template>
    </a-tree>

    <!-- 最大层级提示 -->
    <div v-if="maxLevel && currentMaxLevel >= maxLevel" class="category-tree-nav__max-level">
      最多支持 {{ maxLevel }} 级分类
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import type { CategoryTreeVO } from '/@/api/content/model/channelDiscoveryModel';

type TreeMode = 'browse' | 'select' | 'manage';

interface Props {
  categories: CategoryTreeVO[];
  selectedKey?: string;
  mode?: TreeMode;
  maxLevel?: number;
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'browse',
  maxLevel: 4,
});

const emit = defineEmits<{
  (e: 'select', category: CategoryTreeVO): void;
  (e: 'change', keys: string[]): void;
}>();

const expandedKeys = ref<string[]>([]);
const selectedKeys = computed(() => (props.selectedKey ? [props.selectedKey] : []));

// 将 CategoryTreeVO[] 转为 a-tree 的 tree-data 格式
const treeData = computed(() => {
  return transformTree(props.categories);
});

function transformTree(nodes: CategoryTreeVO[]): any[] {
  return nodes.map((node) => ({
    ...node,
    key: node.id,
    children: node.children?.length ? transformTree(node.children) : undefined,
  }));
}

const currentMaxLevel = computed(() => {
  return getMaxLevel(props.categories, 1);
});

function getMaxLevel(nodes: CategoryTreeVO[], currentLevel: number): number {
  let max = currentLevel;
  for (const node of nodes) {
    if (node.children?.length) {
      max = Math.max(max, getMaxLevel(node.children, currentLevel + 1));
    }
  }
  return max;
}

function handleSelect(_keys: string[], info: any) {
  const node = info.node?.dataRef as CategoryTreeVO;
  if (node) {
    emit('select', node);
  }
  emit('change', _keys);
}

function handleExpand(keys: string[]) {
  expandedKeys.value = keys;
}

// 监听 selectedKey 变化，自动展开父节点
watch(
  () => props.selectedKey,
  (newKey) => {
    if (newKey) {
      expandToNode(newKey);
    }
  },
  { immediate: true },
);

function expandToNode(key: string) {
  const path = findPath(props.categories, key);
  if (path.length > 0) {
    const parentIds = path.slice(0, -1).map((n) => n.id);
    expandedKeys.value = [...new Set([...expandedKeys.value, ...parentIds])];
  }
}

function findPath(nodes: CategoryTreeVO[], targetId: string): CategoryTreeVO[] {
  for (const node of nodes) {
    if (node.id === targetId) return [node];
    if (node.children?.length) {
      const childPath = findPath(node.children, targetId);
      if (childPath.length) return [node, ...childPath];
    }
  }
  return [];
}
</script>

<style lang="less" scoped>
.category-tree-nav {
  padding: 8px 0;

  &__node {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__label {
    font-size: 14px;
  }

  &__status {
    font-size: 10px;
  }

  &__max-level {
    padding: 8px 12px;
    color: #999;
    font-size: 12px;
    text-align: center;
    border-top: 1px solid #f0f0f0;
    margin-top: 8px;
  }
}

// Select 模式：紧凑
.category-tree-nav--select {
  :deep(.ant-tree) {
    .ant-tree-treenode {
      padding: 2px 0;
    }
  }
}

// Manage 模式：更宽间距
.category-tree-nav--manage {
  :deep(.ant-tree) {
    .ant-tree-treenode {
      padding: 4px 0;
    }
  }
}
</style>
