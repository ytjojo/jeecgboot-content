/**
 * 内容社区 - 个人资料 VO / Req 类型定义
 *
 * 对接后端 `ContentUserProfileController`，路径前缀 `/content/user/profile/*`。
 * 字段约束（来自后端 DTO `@NotBlank` / `@Size` / `@Pattern` 注解）以 JSDoc 形式标注，
 * 供前端表单校验复用。
 */
// ============================================================
// 枚举字符串字面量类型（与 /@/enums/profileEnum 保持一致，供类型签名使用）
// ============================================================
export type PrivacyVisibilityLiteral =
  | 'PUBLIC'
  | 'FOLLOWERS_ONLY'
  | 'MUTUAL_ONLY'
  | 'PRIVATE';

export type OnlineStatusVisibilityLiteral =
  | 'PUBLIC'
  | 'HIDDEN'
  | 'MUTUAL_ONLY';

/**
 * 性别枚举（后端 ContentUserProfileUpdateReq.gender 为 Integer）
 * 0=未设置, 1=男, 2=女
 */
export type Gender = 0 | 1 | 2;
export type ProfileReviewStatus = 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';
export type HistoryType = 'NICKNAME' | 'AVATAR';
export type BadgeType =
  | 'INDIVIDUAL'
  | 'ENTERPRISE'
  | 'CREATOR'
  | 'OFFICIAL'
  | 'REAL_NAME'
  | 'MOBILE'
  | 'EMAIL';

// ============================================================
// 内嵌 VO
// ============================================================

/** 认证标识 Badge VO — `ContentUserVerificationBadgeVO` */
export interface ContentUserVerificationBadgeVO {
  /** 标识 ID */
  badgeId: string;
  /** 认证类型 */
  badgeType: BadgeType;
  /** 展示标签 */
  badgeLabel: string;
  /** 前端图标+颜色字典 key（未知 key 落入 DEFAULT 兜底） */
  visualStyleKey: string;
  /** 认证时间 */
  verifiedAt?: string;
  /** 过期时间，过期后不在公开列表中显示 */
  expiresAt?: string;
  /** 认证说明 */
  description?: string;
}

/** 主页模块配置 VO — `ContentUserHomepageModuleVO` */
export interface ContentUserHomepageModuleVO {
  moduleKey: string;
  moduleName: string;
  visible: boolean;
  sortOrder: number;
}

/** 资料历史记录 VO — `ContentUserProfileHistoryVO` */
export interface ContentUserProfileHistoryVO {
  id: string;
  historyType: HistoryType;
  historyValue: string;
  createTime: string;
  /** 过期时间（180 天 TTL） */
  expiresAt: string;
}

// ============================================================
// 核心 VO
// ============================================================

/** 资料详情 VO（已按 viewer 视角裁剪） — `ContentUserProfileVO` */
export interface ContentUserProfileVO {
  userId: string;
  nickname: string;
  avatar: string;
  bio?: string;
  gender?: Gender;
  birthday?: string;
  region?: string;
  profession?: string;
  personalLink?: string;
  homepageBackground?: string;
  themeColor?: string;
  /** 模块排序的 JSON 字符串，由前端解析后展示 */
  moduleOrderJson?: string;
  certificationType?: string;
  certificationLabel?: string;
  certificationDescription?: string;
  /** 资料完善率 0-100 */
  profileCompletionRate?: number;
  /** 资料审核状态 */
  profileReviewStatus?: ProfileReviewStatus;
  /** 审核拒绝原因 */
  profileReviewReason?: string;
  lastUpdatedAt?: string;
  /** 认证标识列表（已按 privacy 过滤） */
  verificationBadges?: ContentUserVerificationBadgeVO[];
  /** 主页模块列表（已按 privacy 过滤） */
  homepageModules?: ContentUserHomepageModuleVO[];
}

/** 主页配置更新 Req — `ContentUserHomepageUpdateReq` */
export interface ContentUserHomepageUpdateReq {
  homepageBackground?: string;
  themeColor?: string;
  moduleOrderJson?: string;
}

/** 统一资料更新 Req — `ContentUserProfileUpdateReq`
 *
 * 字段约束（与后端 DTO 一致）:
 * - nickname: 必填，≤30 字符
 * - avatar: 必填，≤512 字符（OSS 上传后回填的 CDN URL）
 * - bio: ≤500 字符
 * - gender: MALE|FEMALE|OTHER|UNKNOWN
 * - birthday: ISO 日期，必须为过去日期
 * - region: ≤64 字符
 * - profession: ≤64 字符
 * - personalLink: ≤256 字符，符合 `^https?://.*$`
 * - homepageBackground: ≤512 字符
 * - themeColor: ≤16 字符，符合 `^#[0-9A-Fa-f]{6}$`
 * - certificationType/Label/Description: ≤32/64/512 字符
 */
export interface ContentUserProfileUpdateReq {
  nickname: string;
  avatar: string;
  bio?: string;
  gender?: Gender;
  birthday?: string;
  region?: string;
  profession?: string;
  personalLink?: string;
  homepageBackground?: string;
  themeColor?: string;
  moduleOrderJson?: string;
  certificationType?: string;
  certificationLabel?: string;
  certificationDescription?: string;
}

/** 隐私设置更新 Req — `ContentUserPrivacyUpdateReq`
 *
 * 对齐后端实际 16 字段：14 个 *Visibility + onlineStatusVisible(Boolean) + onlineStatusVisibility(特殊枚举)
 * + allowSearchEngineIndex(Boolean) + allowUserSearch(Boolean)。
 */
export interface ContentUserPrivacyUpdateReq {
  // 基础资料 (4)
  birthdayVisibility?: PrivacyVisibilityLiteral;
  genderVisibility?: PrivacyVisibilityLiteral;
  regionVisibility?: PrivacyVisibilityLiteral;
  professionVisibility?: PrivacyVisibilityLiteral;
  // 扩展资料 (1)
  personalLinkVisibility?: PrivacyVisibilityLiteral;
  // 认证标识 (2)
  verificationBadgeVisibility?: PrivacyVisibilityLiteral;
  contactBadgeVisibility?: PrivacyVisibilityLiteral;
  // 主页 (2)
  homepageVisibility?: PrivacyVisibilityLiteral;
  dynamicVisibility?: PrivacyVisibilityLiteral;
  // 在线状态 (2) — onlineStatusVisible 为旧字段兼容保留
  onlineStatusVisible?: boolean;
  onlineStatusVisibility?: OnlineStatusVisibilityLiteral;
  // 活动 (3)
  browseHistoryVisibility?: PrivacyVisibilityLiteral;
  likeActivityVisibility?: PrivacyVisibilityLiteral;
  favoriteVisibility?: PrivacyVisibilityLiteral;
  // 搜索与发现 (2)
  allowSearchEngineIndex?: boolean;
  allowUserSearch?: boolean;
}
