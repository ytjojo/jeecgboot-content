import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';

const mockGetCategoryTree = vi.fn();
const mockCreateCategory = vi.fn();
const mockUpdateCategory = vi.fn();
const mockDisableCategory = vi.fn();
const mockEnableCategory = vi.fn();
const mockDeleteCategory = vi.fn();
const mockSortCategories = vi.fn();

vi.mock('/@/api/content/channelDiscovery', () => ({
  getCategoryTree: (...args: any[]) => mockGetCategoryTree(...args),
  createCategory: (...args: any[]) => mockCreateCategory(...args),
  updateCategory: (...args: any[]) => mockUpdateCategory(...args),
  disableCategory: (...args: any[]) => mockDisableCategory(...args),
  enableCategory: (...args: any[]) => mockEnableCategory(...args),
  deleteCategory: (...args: any[]) => mockDeleteCategory(...args),
  sortCategories: (...args: any[]) => mockSortCategories(...args),
}));

import { useChannelCategoryStore } from '/@/store/modules/channelCategory';

const mockTree = [
  {
    id: '1',
    name: '科技',
    parentId: '',
    level: 1,
    sortOrder: 1,
    status: 'enabled' as const,
    children: [
      {
        id: '1-1',
        name: '编程',
        parentId: '1',
        level: 2,
        sortOrder: 1,
        status: 'enabled' as const,
        children: [],
      },
    ],
  },
];

describe('useChannelCategoryStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should have correct initial state', () => {
    const store = useChannelCategoryStore();
    expect(store.categoryTree).toEqual([]);
    expect(store.selectedCategory).toBeNull();
    expect(store.loading).toBe(false);
    expect(store.loaded).toBe(false);
  });

  it('should fetch category tree', async () => {
    mockGetCategoryTree.mockResolvedValue(mockTree);

    const store = useChannelCategoryStore();
    await store.fetchCategoryTree();

    expect(store.categoryTree).toHaveLength(1);
    expect(store.categoryTree[0].name).toBe('科技');
    expect(store.loaded).toBe(true);
  });

  it('should not refetch if already loaded (ensureCategoryTree)', async () => {
    mockGetCategoryTree.mockResolvedValue(mockTree);

    const store = useChannelCategoryStore();
    store.categoryTree = mockTree;
    store.loaded = true;

    await store.ensureCategoryTree();
    expect(mockGetCategoryTree).not.toHaveBeenCalled();
  });

  it('should refetch if not loaded yet', async () => {
    mockGetCategoryTree.mockResolvedValue(mockTree);

    const store = useChannelCategoryStore();
    await store.ensureCategoryTree();

    expect(mockGetCategoryTree).toHaveBeenCalledTimes(1);
  });

  it('should set selected category', () => {
    const store = useChannelCategoryStore();
    store.setSelectedCategory(mockTree[0]);
    expect(store.selectedCategory?.name).toBe('科技');
  });

  it('should refresh tree after create category', async () => {
    mockCreateCategory.mockResolvedValue({});
    mockGetCategoryTree.mockResolvedValue(mockTree);

    const store = useChannelCategoryStore();
    await store.createCategoryAction({ name: '新分类' });

    expect(mockCreateCategory).toHaveBeenCalledWith({ name: '新分类' });
    expect(mockGetCategoryTree).toHaveBeenCalled(); // 写后刷新
  });

  it('should refresh tree after update category', async () => {
    mockUpdateCategory.mockResolvedValue({});
    mockGetCategoryTree.mockResolvedValue(mockTree);

    const store = useChannelCategoryStore();
    await store.updateCategoryAction({ id: '1', name: '改名' });

    expect(mockUpdateCategory).toHaveBeenCalled();
    expect(mockGetCategoryTree).toHaveBeenCalled();
  });

  it('should refresh tree after disable category', async () => {
    mockDisableCategory.mockResolvedValue({});
    mockGetCategoryTree.mockResolvedValue(mockTree);

    const store = useChannelCategoryStore();
    await store.disableCategoryAction('1');

    expect(mockDisableCategory).toHaveBeenCalled();
    expect(mockGetCategoryTree).toHaveBeenCalled();
  });

  it('should find category by id', () => {
    const store = useChannelCategoryStore();
    store.categoryTree = mockTree;

    const found = store.findCategoryById('1-1');
    expect(found).not.toBeNull();
    expect(found?.name).toBe('编程');
  });

  it('should return null for non-existent id', () => {
    const store = useChannelCategoryStore();
    store.categoryTree = mockTree;

    const found = store.findCategoryById('non-existent');
    expect(found).toBeNull();
  });

  it('should get category path for breadcrumb', () => {
    const store = useChannelCategoryStore();
    store.categoryTree = mockTree;

    const path = store.getCategoryPath('1-1');
    expect(path).toHaveLength(2);
    expect(path[0].name).toBe('科技');
    expect(path[1].name).toBe('编程');
  });

  it('should clear cache', () => {
    const store = useChannelCategoryStore();
    store.categoryTree = mockTree;
    store.loaded = true;
    store.clearCache();

    expect(store.categoryTree).toEqual([]);
    expect(store.loaded).toBe(false);
  });
});
