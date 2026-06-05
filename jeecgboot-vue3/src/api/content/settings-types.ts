/** 通知渠道配置 */
export interface NotificationChannelConfig {
  likeChannels: string[];
  commentChannels: string[];
  followChannels: string[];
  favoriteChannels: string[];
  mentionChannels: string[];
  privateMessageChannels: string[];
  subscriptionChannels: string[];
}

/** 免打扰规则条目 */
export interface DndRuleItem {
  enabled: boolean;
  startTime: string;
  endTime: string;
  dayType: 'DAILY' | 'WORKDAY' | 'WEEKEND' | 'CUSTOM';
  summaryMode: boolean;
}

/** 免打扰规则 */
export interface NotificationDndRule {
  enabled: boolean;
  startTime: string;
  endTime: string;
  dndRules: DndRuleItem[];
  temporaryDisableUntil: number | null;
}

/** 通知设置 */
export interface NotificationSettingVO {
  userId: string;
  likeNoticeEnabled: boolean | null;
  commentNoticeEnabled: boolean | null;
  followNoticeEnabled: boolean | null;
  favoriteNoticeEnabled: boolean | null;
  mentionNoticeEnabled: boolean | null;
  privateMessageNoticeEnabled: boolean | null;
  subscriptionNoticeEnabled: boolean | null;
  channelConfig: NotificationChannelConfig;
  dndRule: NotificationDndRule;
}

/** 通知设置更新请求 */
export interface NotificationUpdateReq {
  likeNoticeEnabled?: boolean | null;
  commentNoticeEnabled?: boolean | null;
  followNoticeEnabled?: boolean | null;
  favoriteNoticeEnabled?: boolean | null;
  mentionNoticeEnabled?: boolean | null;
  privateMessageNoticeEnabled?: boolean | null;
  subscriptionNoticeEnabled?: boolean | null;
  channelConfig?: Partial<NotificationChannelConfig>;
}

/** 免打扰规则更新请求 */
export interface DndRuleUpdateReq {
  enabled?: boolean;
  dndRules?: DndRuleItem[];
  temporaryDisable?: boolean;
}

/** 隐私设置（对应 ContentUserPrivacySetting 实体） */
export interface PrivacySettingVO {
  userId: string;
  browseHistoryVisibility: string | null;
  likeActivityVisibility: string | null;
  favoriteVisibility: string | null;
  onlineStatusVisibility: string | null;
  allowSearchEngineIndex: boolean | null;
}

/** 隐私设置更新请求 */
export interface PrivacyUpdateReq {
  browseHistoryVisibility?: string;
  likeActivityVisibility?: string;
  favoritesVisibility?: string;
  onlineStatusVisibility?: string;
  allowSearchEngineIndex?: boolean;
}

/** 安全设置 */
export interface SecuritySettingVO {
  deviceManagementEnabled: boolean;
  passwordChangeEnabled: boolean;
  twoFactorEnabled: boolean;
  loginAlertEnabled: boolean;
}

/** 安全设置更新请求 */
export interface SecurityUpdateReq {
  loginAlertEnabled?: boolean;
}

/** 第三方授权记录 */
export interface ThirdPartyAuthVO {
  authId: string;
  appName: string | null;
  authTime: string;
  scopes: string[];
  status: 'ACTIVE' | 'REVOKED';
}

/** 第三方授权详情 */
export interface ThirdPartyAuthDetailVO extends ThirdPartyAuthVO {
  revokedAt: string | null;
}
