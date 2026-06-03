import type { ContentUserVerificationBadgeVO } from '/@/api/content/profile/types';

export type BadgeVisualStyleKey =
  | 'OFFICIAL'
  | 'ENTERPRISE'
  | 'CREATOR'
  | 'INDIVIDUAL'
  | 'REAL_NAME'
  | 'MOBILE'
  | 'EMAIL'
  | 'DEFAULT';

export interface BadgeVisualStyle {
  key: BadgeVisualStyleKey;
  iconColor: string;
  backgroundColor: string;
  borderColor: string;
  tooltip: string;
  priority: number;
}

const STYLE_DICTIONARY: Record<BadgeVisualStyleKey, BadgeVisualStyle> = {
  OFFICIAL: {
    key: 'OFFICIAL',
    iconColor: '#ffffff',
    backgroundColor: '#d4380d',
    borderColor: '#a8071a',
    tooltip: '官方认证',
    priority: 100,
  },
  ENTERPRISE: {
    key: 'ENTERPRISE',
    iconColor: '#ffffff',
    backgroundColor: '#1d39c4',
    borderColor: '#061178',
    tooltip: '企业认证',
    priority: 90,
  },
  CREATOR: {
    key: 'CREATOR',
    iconColor: '#ffffff',
    backgroundColor: '#722ed1',
    borderColor: '#531dab',
    tooltip: '创作者认证',
    priority: 80,
  },
  INDIVIDUAL: {
    key: 'INDIVIDUAL',
    iconColor: '#ffffff',
    backgroundColor: '#13a8a8',
    borderColor: '#006d75',
    tooltip: '个人认证',
    priority: 70,
  },
  REAL_NAME: {
    key: 'REAL_NAME',
    iconColor: '#ffffff',
    backgroundColor: '#389e0d',
    borderColor: '#135200',
    tooltip: '实名认证',
    priority: 60,
  },
  MOBILE: {
    key: 'MOBILE',
    iconColor: '#ffffff',
    backgroundColor: '#fa8c16',
    borderColor: '#ad4e00',
    tooltip: '手机绑定',
    priority: 50,
  },
  EMAIL: {
    key: 'EMAIL',
    iconColor: '#ffffff',
    backgroundColor: '#faad14',
    borderColor: '#874d00',
    tooltip: '邮箱绑定',
    priority: 40,
  },
  DEFAULT: {
    key: 'DEFAULT',
    iconColor: '#ffffff',
    backgroundColor: '#bfbfbf',
    borderColor: '#8c8c8c',
    tooltip: '已认证',
    priority: 0,
  },
};

const KNOWN_KEYS: ReadonlySet<BadgeVisualStyleKey> = new Set([
  'OFFICIAL',
  'ENTERPRISE',
  'CREATOR',
  'INDIVIDUAL',
  'REAL_NAME',
  'MOBILE',
  'EMAIL',
]);

export function isKnownBadgeKey(key: string | null | undefined): key is BadgeVisualStyleKey {
  if (!key) return false;
  return (KNOWN_KEYS as Set<string>).has(key);
}

export function getBadgeStyle(key: string | null | undefined): BadgeVisualStyle {
  if (isKnownBadgeKey(key)) {
    return STYLE_DICTIONARY[key];
  }
  return STYLE_DICTIONARY.DEFAULT;
}

/**
 * 在一组 badge 中挑选应当显示的「主徽章」。
 * 优先级：OFFICIAL > ENTERPRISE > CREATOR > INDIVIDUAL > REAL_NAME > MOBILE > EMAIL。
 * 若全部为未知样式，则回退到默认样式（灰对勾）。
 */
export function selectPrimaryBadge(badges: ContentUserVerificationBadgeVO[]): BadgeVisualStyle {
  if (!badges || badges.length === 0) {
    return STYLE_DICTIONARY.DEFAULT;
  }
  let best: BadgeVisualStyle = STYLE_DICTIONARY.DEFAULT;
  for (const b of badges) {
    const style = getBadgeStyle(b.visualStyleKey);
    if (style.priority > best.priority) {
      best = style;
    }
  }
  return best;
}

/**
 * 将 badge 列表按 visualStyleKey 已知/未知分组，
 * 用于折叠展示：已知徽章按优先级降序展示，剩余（未知/DEFAULT）折叠。
 */
export function partitionBadges(badges: ContentUserVerificationBadgeVO[]): {
  known: ContentUserVerificationBadgeVO[];
  unknown: ContentUserVerificationBadgeVO[];
} {
  const known: ContentUserVerificationBadgeVO[] = [];
  const unknown: ContentUserVerificationBadgeVO[] = [];
  for (const b of badges || []) {
    if (isKnownBadgeKey(b.visualStyleKey)) {
      known.push(b);
    } else {
      unknown.push(b);
    }
  }
  known.sort((a, b) => {
    return getBadgeStyle(b.visualStyleKey).priority - getBadgeStyle(a.visualStyleKey).priority;
  });
  return { known, unknown };
}
