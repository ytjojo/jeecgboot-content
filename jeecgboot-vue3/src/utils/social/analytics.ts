/** 社交扩展模块埋点事件定义 */

export const SOCIAL_EVENTS = {
  // 互关相关
  MUTUAL_FOLLOW_BADGE_SHOW: 'mutual_follow_badge_show',
  MUTUAL_FOLLOW_BADGE_CLICK: 'mutual_follow_badge_click',
  MUTUAL_FOLLOW_CANCEL: 'mutual_follow_cancel',

  // 粉丝管理
  FAN_LIST_VIEW: 'fan_list_view',
  FAN_TREND_VIEW: 'fan_trend_view',
  FAN_TREND_POINT_CLICK: 'fan_trend_point_click',

  // 邀请相关
  INVITE_CODE_COPY: 'invite_code_copy',
  INVITE_LANDING_PAGE_VIEW: 'invite_landing_page_view',
  INVITE_REGISTER_CLICK: 'invite_register_click',
  INVITE_REGISTER_COMPLETE: 'invite_register_complete',
  INVITE_REWARD_TRIGGER: 'invite_reward_trigger',

  // 角色标签
  COMMUNITY_ROLE_BADGE_SHOW: 'community_role_badge_show',
  COMMUNITY_ROLE_BADGE_CLICK: 'community_role_badge_click',

  // 管理操作
  MODERATOR_ACTION_EXECUTE: 'moderator_action_execute',

  // 私密内容
  PRIVATE_CONTENT_PUBLISH: 'private_content_publish',
  PRIVATE_CONTENT_ACCESS_DENIED: 'private_content_access_denied',
} as const;

export type SocialEventName = (typeof SOCIAL_EVENTS)[keyof typeof SOCIAL_EVENTS];

/** 埋点事件上报 */
export function trackSocialEvent(name: SocialEventName, payload?: Record<string, unknown>) {
  if (typeof window === 'undefined') return;
  try {
    const w = window as any;
    if (typeof w.trackEvent === 'function') {
      w.trackEvent(name, payload);
    } else if (typeof w.dataLayer?.push === 'function') {
      w.dataLayer.push({ event: name, ...payload });
    } else {
      console.debug('[social-analytics]', name, payload);
    }
  } catch {
    // 静默失败
  }
}
