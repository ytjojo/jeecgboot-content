import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  disableCategory,
  enableCategory,
  deleteCategory,
  sortCategories,
} from '/@/api/content/channelDiscovery';
import type { CategoryTreeVO, CategoryFormData } from '/@/api/content/model/channelDiscoveryModel';

export const useChannelCategoryStore = defineStore('channelCategory', () => {
  // ===== State =====
  const categoryTree = ref<CategoryTreeVO[]>([]);
  const selectedCategory = ref<CategoryTreeVO | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  /** 标记分类树是否已首次加载 */
  const loaded = ref(false);

  // ===== Actions =====

  /** 加载分类树 */
  async function fetchCategoryTree() {
    loading.value = true;
    error.value = null;

    try {
      const data = await getCategoryTree();
      categoryTree.value = data || [];
      loaded.value = true;
    } catch (e) {
      error.value = '分类树加载失败';
    } finally {
      loading.value = false;
    }
  }

  /** 确保分类树已加载（会话级缓存） */
  async function ensureCategoryTree() {
    if (!loaded.value || categoryTree.value.length === 0) {
      await fetchCategoryTree();
    }
  }

  /** 设置选中分类 */
  function setSelectedCategory(category: CategoryTreeVO | null) {
    selectedCategory.value = category;
  }

  /** 创建分类 */
  async function createCategoryAction(data: CategoryFormData) {
    await createCategory(data);
    await fetchCategoryTree(); // 写操作后刷新
  }

  /** 更新分类 */
  async function updateCategoryAction(data: CategoryFormData & { id: string }) {
    await updateCategory(data);
    await fetchCategoryTree();
  }

  /** 停用分类 */
  async function disableCategoryAction(categoryId: string, params?: { action?: string; targetCategoryId?: string }) {
    await disableCategory(categoryId, params);
    await fetchCategoryTree();
  }

  /** 启用分类 */
  async function enableCategoryAction(categoryId: string) {
    await enableCategory(categoryId);
    await fetchCategoryTree();
  }

  /** 删除分类 */
  async function deleteCategoryAction(categoryId: string) {
    await deleteCategory(categoryId);
    await fetchCategoryTree();
  }

  /** 分类排序 */
  async function sortCategoriesAction(data: { id: string; sortOrder: number }[]) {
    await sortCategories(data);
    await fetchCategoryTree();
  }

  /** 根据 ID 查找分类节点 */
  function findCategoryById(id: string): CategoryTreeVO | null {
    const search = (nodes: CategoryTreeVO[]): CategoryTreeVO | null => {
      for (const node of nodes) {
        if (node.id === id) return node;
        if (node.children?.length) {
          const found = search(node.children);
          if (found) return found;
        }
      }
      return null;
    };
    return search(categoryTree.value);
  }

  /** 获取分类路径（面包屑） */
  function getCategoryPath(id: string): CategoryTreeVO[] {
    const path: CategoryTreeVO[] = [];
    const search = (nodes: CategoryTreeVO[], ancestors: CategoryTreeVO[]): boolean => {
      for (const node of nodes) {
        const currentPath = [...ancestors, node];
        if (node.id === id) {
          path.push(...currentPath);
          return true;
        }
        if (node.children?.length && search(node.children, currentPath)) {
          return true;
        }
      }
      return false;
    };
    search(categoryTree.value, []);
    return path;
  }

  /** 清除分类树缓存 */
  function clearCache() {
    loaded.value = false;
    categoryTree.value = [];
  }

  return {
    // State
    categoryTree,
    selectedCategory,
    loading,
    error,
    loaded,
    // Actions
    fetchCategoryTree,
    ensureCategoryTree,
    setSelectedCategory,
    createCategoryAction,
    updateCategoryAction,
    disableCategoryAction,
    enableCategoryAction,
    deleteCategoryAction,
    sortCategoriesAction,
    findCategoryById,
    getCategoryPath,
    clearCache,
  };
});

// Support use outside of setup
export function useChannelCategoryStoreWithOut() {
  return useChannelCategoryStore(store);
}
