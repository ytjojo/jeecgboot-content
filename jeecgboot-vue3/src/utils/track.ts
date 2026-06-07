/**
 * 埋点上报工具
 * 上报至后端分析接口，生产环境可替换为第三方 SDK
 */
export function track(event: string, params?: Record<string, any>) {
  try {
    const payload = {
      event,
      params,
      timestamp: Date.now(),
      url: typeof window !== 'undefined' ? window.location.href : '',
    };
    // 使用 sendBeacon 上报，不阻塞页面；不支持时降级为 fetch
    if (typeof navigator !== 'undefined' && navigator.sendBeacon) {
      navigator.sendBeacon('/api/v1/content/track', JSON.stringify(payload));
    } else if (typeof fetch !== 'undefined') {
      fetch('/api/v1/content/track', { method: 'POST', body: JSON.stringify(payload), keepalive: true }).catch(() => {});
    }
  } catch {
    // 埋点失败静默处理
  }
}
