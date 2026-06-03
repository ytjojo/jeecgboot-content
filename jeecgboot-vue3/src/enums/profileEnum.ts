/**
 * 内容社区 - 个人资料相关枚举
 *
 * 设计要点:
 * - 4 种通用 visibility 适用于大部分资料字段
 * - onlineStatusVisibility 是特殊枚举，仅 PUBLIC/HIDDEN/MUTUAL_ONLY（无 PRIVATE）
 * - 每个枚举都提供类型守卫函数和中文 option 列表
 */

export enum PrivacyVisibility {
  PUBLIC = 'PUBLIC',
  FOLLOWERS_ONLY = 'FOLLOWERS_ONLY',
  MUTUAL_ONLY = 'MUTUAL_ONLY',
  PRIVATE = 'PRIVATE',
}

export enum OnlineStatusVisibility {
  PUBLIC = 'PUBLIC',
  HIDDEN = 'HIDDEN',
  MUTUAL_ONLY = 'MUTUAL_ONLY',
}

const PRIVACY_VALUES = new Set<string>([
  PrivacyVisibility.PUBLIC,
  PrivacyVisibility.FOLLOWERS_ONLY,
  PrivacyVisibility.MUTUAL_ONLY,
  PrivacyVisibility.PRIVATE,
]);

const ONLINE_STATUS_VALUES = new Set<string>([
  OnlineStatusVisibility.PUBLIC,
  OnlineStatusVisibility.HIDDEN,
  OnlineStatusVisibility.MUTUAL_ONLY,
]);

export function isPrivacyVisibility(value: unknown): value is PrivacyVisibility {
  return typeof value === 'string' && PRIVACY_VALUES.has(value);
}

export function isOnlineStatusVisibility(value: unknown): value is OnlineStatusVisibility {
  return typeof value === 'string' && ONLINE_STATUS_VALUES.has(value);
}

export const PRIVACY_VISIBILITY_OPTIONS: ReadonlyArray<{ value: PrivacyVisibility; label: string }> = [
  { value: PrivacyVisibility.PUBLIC, label: '公开' },
  { value: PrivacyVisibility.FOLLOWERS_ONLY, label: '仅关注者' },
  { value: PrivacyVisibility.MUTUAL_ONLY, label: '互关可见' },
  { value: PrivacyVisibility.PRIVATE, label: '仅自己' },
];

export const ONLINE_STATUS_VISIBILITY_OPTIONS: ReadonlyArray<{ value: OnlineStatusVisibility; label: string }> = [
  { value: OnlineStatusVisibility.PUBLIC, label: '公开' },
  { value: OnlineStatusVisibility.HIDDEN, label: '完全隐藏' },
  { value: OnlineStatusVisibility.MUTUAL_ONLY, label: '仅互关' },
];
