<template>
  <div class="category-manage-page">
    <h3>分类管理</h3>
    <div class="category-manage-page__layout">
      <!-- 左侧分类树 -->
      <aside class="category-manage-page__sidebar">
        <CategoryManageTree
          :categories="categoryStore.categoryTree"
          @select="handleSelect"
          @add="handleAdd"
          @edit="handleEdit"
          @toggle-status="handleToggleStatus"
          @delete="handleDelete"
          @drop="handleDrop"
        />
      </aside>

      <!-- 右侧详情 -->
      <main class="category-manage-page__detail">
        <a-card v-if="selectedCategory" title="分类详情" size="small">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="名称">{{ selectedCategory.name }}</a-descriptions-item>
            <a-descriptions-item label="层级">{{ selectedCategory.level }} 级</a-descriptions-item>
            <a-descriptions-item label="排序号">{{ selectedCategory.sortOrder }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="selectedCategory.status === 'enabled' ? 'green' : 'default'">
                {{ selectedCategory.status === 'enabled' ? '启用' : '停用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="关联频道数">
              {{ selectedCategory.channelCount ?? '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
        <a-empty v-else description="请选择左侧分类查看详情" />
      </main>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="editingCategory ? '编辑分类' : '新增分类'"
      @ok="handleFormSubmit"
      :confirm-loading="formLoading"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="分类名称" required>
          <a-input
            v-model:value="formData.name"
            placeholder="请输入分类名称"
            :maxlength="50"
            :status="nameError ? 'error' : ''"
          />
          <div v-if="nameError" style="color: #ff4d4f; font-size: 12px">{{ nameError }}</div>
        </a-form-item>
        <a-form-item label="排序号">
          <a-input-number v-model:value="formData.sortOrder" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 停用确认弹窗 -->
    <a-modal
      v-model:open="disableVisible"
      title="停用分类"
      @ok="confirmDisable"
    >
      <p v-if="selectedCategory">
        确认停用分类「{{ selectedCategory.name }}」？
      </p>
      <p v-if="hasChannelCount">
        该分类下有 {{ selectedCategory?.channelCount }} 个关联频道，停用后将影响这些频道的分类展示。
      </p>
      <a-radio-group v-if="hasChannelCount" v-model:value="disableAction">
        <a-radio value="keep">保留历史归属</a-radio>
        <a-radio value="migrate">迁移到其他分类</a-radio>
      </a-radio-group>
      <a-tree-select
        v-if="hasChannelCount && disableAction === 'migrate'"
        v-model:value="migrateTargetId"
        :tree-data="migrateTreeData"
        placeholder="选择目标分类"
        style="width: 100%; margin-top: 8px"
      />
    </a-modal>

    <!-- 删除确认 -->
    <a-modal
      v-model:open="deleteVisible"
      title="删除分类"
      @ok="confirmDelete"
      ok-type="danger"
    >
      <p>确认删除分类「{{ selectedCategory?.name }}」？此操作不可撤销。</p>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useChannelCategoryStore } from '/@/store/modules/channelCategory';
import type { CategoryTreeVO, CategoryFormData } from '/@/api/content/model/channelDiscoveryModel';
import CategoryManageTree from '../components/CategoryManageTree.vue';

const categoryStore = useChannelCategoryStore();

const selectedCategory = ref<CategoryTreeVO | null>(null);
const formVisible = ref(false);
const formLoading = ref(false);
const editingCategory = ref<CategoryTreeVO | null>(null);
const formData = ref<CategoryFormData>({ name: '', sortOrder: 0 });
const nameError = ref('');
const disableVisible = ref(false);
const disableAction = ref<'keep' | 'migrate'>('keep');
const migrateTargetId = ref<string | undefined>(undefined);
const deleteVisible = ref(false);

const hasChannelCount = computed(() => (selectedCategory.value?.channelCount ?? 0) > 0);

const migrateTreeData = computed(() => {
  return categoryStore.categoryTree
    .filter((n) => n.id !== selectedCategory.value?.id)
    .map((n) => ({ id: n.id, value: n.id, title: n.name }));
});

onMounted(async () => {
  await categoryStore.ensureCategoryTree();
});

function handleSelect(category: CategoryTreeVO) {
  selectedCategory.value = category;
}

function handleAdd(parentId?: string) {
  editingCategory.value = null;
  formData.value = { name: '', sortOrder: 0, parentId };
  formVisible.value = true;
  nameError.value = '';
}

function handleEdit(category: CategoryTreeVO) {
  editingCategory.value = category;
  formData.value = { name: category.name, sortOrder: category.sortOrder };
  formVisible.value = true;
  nameError.value = '';
}

async function handleFormSubmit() {
  if (!formData.value.name.trim()) {
    nameError.value = '分类名称不能为空';
    return;
  }
  if (formData.value.name.length > 50) {
    nameError.value = '分类名称不能超过 50 个字符';
    return;
  }

  formLoading.value = true;
  try {
    if (editingCategory.value) {
      await categoryStore.updateCategoryAction({
        id: editingCategory.value.id,
        ...formData.value,
      });
      message.success('编辑成功');
    } else {
      await categoryStore.createCategoryAction(formData.value);
      message.success('新增成功');
    }
    formVisible.value = false;
    selectedCategory.value = null;
  } catch {
    message.error('操作失败');
  } finally {
    formLoading.value = false;
  }
}

async function handleToggleStatus(category: CategoryTreeVO) {
  selectedCategory.value = category;
  if (category.status === 'enabled') {
    // 停用
    if (hasChannelCount.value) {
      disableVisible.value = true;
      return;
    }
    try {
      await categoryStore.disableCategoryAction(category.id);
      message.success('已停用');
      selectedCategory.value = null;
    } catch {
      message.error('停用失败');
    }
  } else {
    try {
      await categoryStore.enableCategoryAction(category.id);
      message.success('已启用');
      selectedCategory.value = null;
    } catch {
      message.error('启用失败');
    }
  }
}

async function confirmDisable() {
  try {
    await categoryStore.disableCategoryAction(selectedCategory.value!.id, {
      action: disableAction.value,
      targetCategoryId: migrateTargetId.value,
    });
    message.success('已停用');
    disableVisible.value = false;
    selectedCategory.value = null;
  } catch {
    message.error('停用失败');
  }
}

function handleDelete(category: CategoryTreeVO) {
  if ((category.channelCount ?? 0) > 0) {
    message.warning(`该分类下有 ${category.channelCount} 个频道，请先迁移或停用`);
    return;
  }
  selectedCategory.value = category;
  deleteVisible.value = true;
}

async function confirmDelete() {
  try {
    await categoryStore.deleteCategoryAction(selectedCategory.value!.id);
    message.success('已删除');
    deleteVisible.value = false;
    selectedCategory.value = null;
  } catch {
    message.error('删除失败');
  }
}

async function handleDrop(info: { dragNode: CategoryTreeVO; dropNode: CategoryTreeVO; dropPosition: number }) {
  // 重新计算排序号
  const siblings = categoryStore.categoryTree.filter(
    (n) => n.parentId === info.dragNode.parentId,
  );
  const newOrder = siblings.map((n, i) => ({ id: n.id, sortOrder: i + 1 }));
  try {
    await categoryStore.sortCategoriesAction(newOrder);
    message.success('排序已更新');
  } catch {
    message.error('排序失败');
  }
}
</script>

<style lang="less" scoped>
.category-manage-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;

  h3 {
    margin-bottom: 16px;
  }

  &__layout {
    display: flex;
    gap: 24px;
  }

  &__sidebar {
    width: 280px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 8px;
    padding: 12px;
    max-height: calc(100vh - 120px);
    overflow-y: auto;
  }

  &__detail {
    flex: 1;
  }
}

@media (max-width: 767px) {
  .category-manage-page {
    &__layout {
      flex-direction: column;
    }

    &__sidebar {
      width: 100%;
    }
  }
}
</style>
