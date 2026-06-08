import type { CategoryTreeVO } from '/@/api/content/model/channelDiscoveryModel';

interface TransformOptions {
  /** 是否只包含启用状态的节点，默认 true */
  enabledOnly?: boolean;
}

/**
 * 将 CategoryTreeVO[] 转为 a-tree / a-tree-select 需要的 tree-data 格式
 * 附加 key/value 字段，可选过滤已停用的节点
 */
export function transformCategoryTree(
  nodes: CategoryTreeVO[],
  options: TransformOptions = {},
): any[] {
  const { enabledOnly = true } = options;
  const filtered = enabledOnly ? nodes.filter((n) => n.status === 'enabled') : nodes;
  return filtered.map((node) => ({
    ...node,
    key: node.id,
    value: node.id,
    children: node.children?.length ? transformCategoryTree(node.children, options) : undefined,
  }));
}

/**
 * 在分类树中按 ID 查找节点
 */
export function findNodeById(nodes: CategoryTreeVO[], id: string): CategoryTreeVO | null {
  for (const node of nodes) {
    if (node.id === id) return node;
    if (node.children?.length) {
      const found = findNodeById(node.children, id);
      if (found) return found;
    }
  }
  return null;
}
