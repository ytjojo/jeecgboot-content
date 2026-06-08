/**
 * 格式化计数（订阅数、关注数等）
 * @example formatCount(1234) => "1.2k"
 * @example formatCount(12345) => "1.2万"
 */
export function formatCount(count: number): string {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万';
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k';
  }
  return String(count);
}
