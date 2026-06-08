import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStore } from '/@/store/modules/user';

const { createMessage } = useMessage();

/**
 * 统一 API 错误处理函数
 * 按错误码分类处理，避免各页面重复编写错误处理逻辑
 */
export function handleApiError(error: any, context?: string): void {
  const status = error?.response?.status;
  const code = error?.response?.data?.code;
  const message = extractErrorMessage(error);

  switch (status) {
    case 401:
      // 未登录或 Token 过期，跳转登录页
      useUserStore().logout(true);
      break;

    case 403:
      createMessage.error('权限不足，无法执行此操作');
      break;

    case 404:
      createMessage.error(message || '请求的资源不存在');
      break;

    case 409:
      // 业务冲突类错误
      if (code && isLifecycleConflictCode(code)) {
        createMessage.warning(message || '操作冲突：频道状态已变更，请刷新后重试');
      } else if (code === 40920) {
        createMessage.warning(message || '导出任务冲突：已有相同任务在处理中');
      } else {
        createMessage.warning(message || '资源冲突，请稍后重试');
      }
      break;

    case 429:
      // 限流
      createMessage.warning(message || '操作过于频繁，请稍后重试');
      break;

    case 400:
      // 请求参数错误
      if (code && code >= 40020 && code <= 40021) {
        createMessage.error(message || '导出参数错误');
      } else {
        createMessage.error(message || '请求参数有误');
      }
      break;

    case 500:
      if (code && code >= 50020 && code <= 50021) {
        createMessage.error(message || '导出服务异常，请稍后重试');
      } else {
        createMessage.error(message || '服务器内部错误，请稍后重试');
      }
      break;

    default:
      createMessage.error(message || `操作失败${context ? `：${context}` : ''}`);
  }
}

function extractErrorMessage(error: any): string | null {
  return error?.response?.data?.message
    || error?.response?.data?.msg
    || error?.message
    || null;
}

/** 生命周期操作专用错误码 */
function isLifecycleConflictCode(code: number): boolean {
  // 409xx 错误码段：频道生命周期操作冲突
  return code >= 40910 && code <= 40915;
}

/**
 * 带默认错误处理的 API 调用包装器
 * @param fn API 调用函数
 * @param context 错误上下文描述
 */
export async function safeApiCall<T>(fn: () => Promise<T>, context?: string): Promise<T | null> {
  try {
    return await fn();
  } catch (error: any) {
    handleApiError(error, context);
    return null;
  }
}
