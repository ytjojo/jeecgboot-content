export const ACTION_COLOR_MAP: Record<string, string> = {
  REMOVE: 'red',
  MUTE: 'orange',
  UNMUTE: 'green',
  BLACKLIST_ADD: 'default',
  BLACKLIST_REMOVE: 'blue',
};

export const ACTION_TEXT_MAP: Record<string, string> = {
  REMOVE: '移除',
  MUTE: '禁言',
  UNMUTE: '解除禁言',
  BLACKLIST_ADD: '加入黑名单',
  BLACKLIST_REMOVE: '移出黑名单',
};

export function getActionColor(action: string) {
  return ACTION_COLOR_MAP[action] || 'default';
}

export function getActionText(action: string) {
  return ACTION_TEXT_MAP[action] || action;
}
