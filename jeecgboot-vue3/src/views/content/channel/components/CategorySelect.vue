<template>
  <div class="category-select">
    <!-- 主分类选择 -->
    <div class="category-select__primary">
      <label class="category-select__label">
        主分类 <span class="category-select__required">*</span>
      </label>
      <a-tree-select
        :value="primaryCategoryId"
        :tree-data="treeSelectData"
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
        placeholder="请选择主分类"
        :status="primaryError ? 'error' : ''"
        style="width: 100%"
        @change="handlePrimaryChange"
      />
      <div v-if="primaryError" class="category-select__error">{{ primaryError }}</div>
    </div>

    <!-- 副分类选择 -->
    <div class="category-select__secondary">
      <label class="category-select__label">
        副分类 <span class="category-select__hint">（最多 3 个）</span>
      </label>
      <a-select
        v-model:value="secondaryCategoryIds"
        mode="multiple"
        placeholder="请选择副分类（最多 3 个）"
        :max-tag-count="3"
        style="width: 100%"
        @change="handleSecondaryChange"
      >
        <a-select-option
          v-for="cat in secondaryOptions"
          :key="cat.id"
          :value="cat.id"
          :disabled="cat.id === primaryCategoryId"
        >
          {{ cat.name }}
        </a-select-option>
      </a-select>
      <div v-if="secondaryError" class="category-select__error">{{ secondaryError }}</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useChannelCategoryStore } from '/@/store/modules/channelCategory';
import type { CategoryTreeVO } from '/@/api/content/model/channelDiscoveryModel';

interface Props {
  primaryCategoryId?: string;
  secondaryCategoryIds?: string[];
  channelType?: string;
}

const props = withDefaults(defineProps<Props>(), {
  primaryCategoryId: undefined,
  secondaryCategoryIds: () => [],
  channelType: 'personal',
});

const emit = defineEmits<{
  (e: 'update:primaryCategoryId', value: string): void;
  (e: 'update:secondaryCategoryIds', value: string[]): void;
  (e: 'validate', valid: boolean): void;
}>();

const categoryStore = useChannelCategoryStore();

const primaryCategoryId = ref(props.primaryCategoryId);
const secondaryCategoryIds = ref<string[]>([...props.secondaryCategoryIds]);
const primaryError = ref('');
const secondaryError = ref('');

// 系统频道的特殊分类选项
const specialCategories = [
  { id: '__official_event__', name: '官方活动', level: 1, parentId: '', sortOrder: 0, status: 'enabled' as const, children: [] },
  { id: '__platform_announce__', name: '平台公告', level: 1, parentId: '', sortOrder: 0, status: 'enabled' as const, children: [] },
  { id: '__newbie_guide__', name: '新手引导', level: 1, parentId: '', sortOrder: 0, status: 'enabled' as const, children: [] },
  { id: '__system_pick__', name: '系统精选', level: 1, parentId: '', sortOrder: 0, status: 'enabled' as const, children: [] },
];

const treeSelectData = computed(() => {
  const tree = transformToTreeData(categoryStore.categoryTree.filter((n) => n.level <= 4));
  if (props.channelType === 'system') {
    // 系统频道追加特殊分类
    tree.push(...specialCategories.map((c) => ({
      id: c.id,
      name: c.name,
      value: c.id,
      children: undefined,
    })));
  }
  return tree;
});

function transformToTreeData(nodes: CategoryTreeVO[]): any[] {
  return nodes
    .filter((n) => n.status === 'enabled')
    .map((n) => ({
      id: n.id,
      name: n.name,
      value: n.id,
      children: n.children?.length ? transformToTreeData(n.children) : undefined,
    }));
}

// 副分类可选列表（平铺，排除主分类）
const secondaryOptions = computed(() => {
  const flat: { id: string; name: string }[] = [];
  const flatten = (nodes: CategoryTreeVO[]) => {
    for (const node of nodes) {
      if (node.status === 'enabled' && node.id !== primaryCategoryId.value) {
        flat.push({ id: node.id, name: node.name });
      }
      if (node.children?.length) flatten(node.children);
    }
  };
  flatten(categoryStore.categoryTree);
  if (props.channelType === 'system') {
    specialCategories
      .filter((c) => c.id !== primaryCategoryId.value)
      .forEach((c) => flat.push({ id: c.id, name: c.name }));
  }
  return flat;
});

onMounted(async () => {
  await categoryStore.ensureCategoryTree();
});

// 同步外部变化
watch(() => props.primaryCategoryId, (val) => {
  primaryCategoryId.value = val;
});
watch(() => props.secondaryCategoryIds, (val) => {
  secondaryCategoryIds.value = [...(val || [])];
});

function handlePrimaryChange(value: string) {
  primaryError.value = '';
  primaryCategoryId.value = value;
  emit('update:primaryCategoryId', value);
  validate();
}

function handleSecondaryChange(values: string[]) {
  secondaryError.value = '';

  // 排重主分类
  if (values.includes(primaryCategoryId.value!)) {
    secondaryError.value = '副分类不能与主分类相同';
    secondaryCategoryIds.value = values.filter((v) => v !== primaryCategoryId.value);
    emit('update:secondaryCategoryIds', secondaryCategoryIds.value);
    return;
  }

  // 数量限制
  if (values.length > 3) {
    secondaryError.value = '副分类最多选择 3 个';
    secondaryCategoryIds.value = values.slice(0, 3);
    emit('update:secondaryCategoryIds', secondaryCategoryIds.value);
    return;
  }

  secondaryCategoryIds.value = values;
  emit('update:secondaryCategoryIds', values);
  validate();
}

function validate(): boolean {
  let valid = true;

  if (!primaryCategoryId.value) {
    primaryError.value = '请选择主分类';
    valid = false;
  }

  if (secondaryCategoryIds.value.length > 3) {
    secondaryError.value = '副分类最多选择 3 个';
    valid = false;
  }

  emit('validate', valid);
  return valid;
}

defineExpose({ validate });
</script>

<style lang="less" scoped>
.category-select {
  &__primary {
    margin-bottom: 16px;
  }

  &__secondary {
    // 副分类区域
  }

  &__label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 6px;
  }

  &__required {
    color: #ff4d4f;
  }

  &__hint {
    font-weight: 400;
    color: #999;
    font-size: 12px;
  }

  &__error {
    color: #ff4d4f;
    font-size: 12px;
    margin-top: 4px;
  }
}
</style>
