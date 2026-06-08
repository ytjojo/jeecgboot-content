<template>
  <div class="category-manage-tree">
    <!-- 搜索框 -->
    <div class="category-manage-tree__search">
      <a-input
        v-model:value="searchKeyword"
        placeholder="搜索分类名称"
        allow-clear
        size="small"
        @input="handleSearch"
      >
        <template #prefix><SearchOutlined /></template>
      </a-input>
    </div>

    <!-- 新增一级分类按钮 -->
    <div class="category-manage-tree__toolbar">
      <a-button type="primary" size="small" block @click="$emit('onAdd')">
        <template #icon><PlusOutlined /></template>
        新增一级分类
      </a-button>
    </div>

    <!-- 可编辑分类树 -->
    <div class="category-manage-tree__body">
      <a-tree
        :tree-data="filteredTreeData"
        :selected-keys="selectedKeys"
        :expanded-keys="expandedKeys"
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
        :draggable="true"
        :block-node="true"
        show-line
        @select="handleSelect"
        @drop="handleDrop"
        @expand="handleExpand"
      >
        <template #title="slotProps">
          <a-dropdown :trigger="['contextmenu']">
            <span
              class="category-manage-tree__node"
              :class="{ 'category-manage-tree__node--disabled': slotProps.status === 'disabled' }"
            >
              <span v-if="searchKeyword && slotProps.name.includes(searchKeyword)" v-html="highlightName(slotProps.name)" />
              <span v-else>{{ slotProps.name }}</span>
              <a-tag v-if="slotProps.status === 'disabled'" color="default" size="small">停用</a-tag>
              <span v-if="slotProps.channelCount !== undefined" class="category-manage-tree__count">
                {{ slotProps.channelCount }}
              </span>
            </span>
            <template #overlay>
              <a-menu @click="handleContextMenu($event, slotProps)">
                <a-menu-item key="addChild" :disabled="slotProps.level >= maxLevel">
                  <PlusOutlined /> 新增子分类
                </a-menu-item>
                <a-menu-item key="edit">
                  <EditOutlined /> 编辑
                </a-menu-item>
                <a-menu-item v-if="slotProps.status === 'enabled'" key="disable">
                  <StopOutlined /> 停用
                </a-menu-item>
                <a-menu-item v-else key="enable">
                  <CheckCircleOutlined /> 启用
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="delete" danger :disabled="(slotProps.channelCount || 0) > 0">
                  <DeleteOutlined /> 删除
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
      </a-tree>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  StopOutlined,
  CheckCircleOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue';
import type { CategoryTreeVO } from '/@/api/content/model/channelDiscoveryModel';
import { transformCategoryTree, findNodeById } from '../utils/transformCategoryTree';

interface Props {
  categories: CategoryTreeVO[];
  maxLevel?: number;
}

const props = withDefaults(defineProps<Props>(), {
  maxLevel: 4,
});

const emit = defineEmits<{
  (e: 'select', category: CategoryTreeVO): void;
  (e: 'add', parentId?: string): void;
  (e: 'edit', category: CategoryTreeVO): void;
  (e: 'toggleStatus', category: CategoryTreeVO): void;
  (e: 'delete', category: CategoryTreeVO): void;
  (e: 'drop', info: { dragNode: CategoryTreeVO; dropNode: CategoryTreeVO; dropPosition: number }): void;
}>();

const searchKeyword = ref('');
const selectedKeys = ref<string[]>([]);
const expandedKeys = ref<string[]>([]);

const filteredTreeData = computed(() => {
  if (!searchKeyword.value.trim()) {
    return transformCategoryTree(props.categories, { enabledOnly: false });
  }
  return filterTree(props.categories, searchKeyword.value.trim().toLowerCase());
});

function filterTree(nodes: CategoryTreeVO[], keyword: string): any[] {
  const result: any[] = [];
  for (const node of nodes) {
    const nameMatch = node.name.toLowerCase().includes(keyword);
    const filteredChildren = node.children?.length ? filterTree(node.children, keyword) : [];
    if (nameMatch || filteredChildren.length > 0) {
      result.push({
        ...node,
        key: node.id,
        children: filteredChildren.length > 0 ? filteredChildren : node.children?.length ? transformCategoryTree(node.children, { enabledOnly: false }) : undefined,
      });
    }
  }
  return result;
}

/** HTML 转义，防 XSS（纯字符串实现，SSR/jsdom 可用） */
function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function highlightName(name: string): string {
  const escaped = escapeHtml(name);
  const escapedKeyword = escapeHtml(searchKeyword.value);
  return escaped.replace(
    new RegExp(`(${escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi'),
    '<mark>$1</mark>',
  );
}

function handleSelect(_keys: string[]) {
  selectedKeys.value = _keys;
  const node = findNodeById(props.categories, _keys[0]);
  if (node) {
    emit('select', node);
  }
}

function handleExpand(keys: string[]) {
  expandedKeys.value = keys;
}

function handleContextMenu(event: any, nodeData: any) {
  const key = event.key;
  switch (key) {
    case 'addChild':
      emit('add', nodeData.id);
      break;
    case 'edit':
      emit('edit', nodeData as CategoryTreeVO);
      break;
    case 'disable':
    case 'enable':
      emit('toggleStatus', nodeData as CategoryTreeVO);
      break;
    case 'delete':
      emit('delete', nodeData as CategoryTreeVO);
      break;
  }
}

function handleDrop(info: any) {
  const dragNode = info.dragNode?.dataRef as CategoryTreeVO;
  const dropNode = info.node?.dataRef as CategoryTreeVO;
  if (dragNode && dropNode) {
    if (dragNode.parentId === dropNode.parentId) {
      emit('drop', {
        dragNode,
        dropNode,
        dropPosition: info.dropPosition,
      });
    }
  }
}
</script>

<style lang="less" scoped>
.category-manage-tree {
  &__search {
    margin-bottom: 8px;
  }

  &__toolbar {
    margin-bottom: 8px;
  }

  &__body {
    max-height: calc(100vh - 240px);
    overflow-y: auto;
  }

  &__node {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;

    &--disabled {
      color: #999;
    }

    mark {
      background: #ffd666;
      padding: 0 2px;
      border-radius: 2px;
    }
  }

  &__count {
    font-size: 11px;
    color: #1677ff;
    background: #e6f4ff;
    padding: 0 6px;
    border-radius: 10px;
  }

  :deep(.ant-tree) {
    .ant-tree-treenode {
      padding: 3px 0;
      width: 100%;
    }

    .ant-tree-node-content-wrapper {
      flex: 1;
    }
  }
}
</style>
