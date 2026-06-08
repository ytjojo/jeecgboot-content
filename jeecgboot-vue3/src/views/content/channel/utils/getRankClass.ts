/**
 * 根据排名返回对应的 CSS class
 * 1: 金色, 2: 银色, 3: 铜色, 4-10: 突出, 11+: 普通
 */
export function getRankClass(rank: number, prefix: string): string {
  if (rank === 1) return `${prefix}--gold`;
  if (rank === 2) return `${prefix}--silver`;
  if (rank === 3) return `${prefix}--bronze`;
  if (rank <= 10) return `${prefix}--prominent`;
  return `${prefix}--normal`;
}
