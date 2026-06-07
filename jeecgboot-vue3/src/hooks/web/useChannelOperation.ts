import { ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';

interface OptimisticOperationOptions<T> {
  /** 实际执行的 API 调用 */
  apiCall: () => Promise<T>;
  /** 乐观更新：立即执行的状态变更 */
  onOptimistic: () => void;
  /** 成功回调 */
  onSuccess?: (result: T) => void;
  /** 失败回滚 */
  onRollback: () => void;
  /** 成功消息 */
  successMessage?: string;
  /** 失败消息 */
  errorMessage?: string;
}

export function useChannelOperation() {
  const { createMessage } = useMessage();
  const operating = ref(false);

  async function optimisticExecute<T>(options: OptimisticOperationOptions<T>): Promise<boolean> {
    if (operating.value) return false;
    operating.value = true;

    // 乐观更新：立即执行
    options.onOptimistic();

    try {
      const result = await options.apiCall();
      if (options.successMessage) {
        createMessage.success(options.successMessage);
      }
      options.onSuccess?.(result);
      return true;
    } catch (error) {
      options.onRollback();
      createMessage.error(options.errorMessage || '操作失败，请重试');
      throw error;
    } finally {
      operating.value = false;
    }
  }

  return { operating, optimisticExecute };
}
