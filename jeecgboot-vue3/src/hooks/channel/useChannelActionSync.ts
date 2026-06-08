import { useMessage } from '/@/hooks/web/useMessage';
import { safeApiCall } from '/@/utils/handleApiError';

const { createMessage } = useMessage();

/**
 * 跨 Store 联动 composable
 * 用于在频道生命周期操作后同步更新相关 Store 的状态
 */
export function useChannelActionSync() {
  /**
   * 生命周期操作后统一回调
   * @param callbacks 各 Store 的刷新回调
   */
  async function afterLifecycleAction(callbacks: {
    refreshGovernance?: () => Promise<void>;
    refreshAuditLog?: () => Promise<void>;
    refreshStats?: () => Promise<void>;
    onSuccess?: () => void;
  }) {
    const { refreshGovernance, refreshAuditLog, refreshStats, onSuccess } = callbacks;

    try {
      // 并行刷新相关模块数据
      await Promise.allSettled([
        safeApiCall(async () => { if (refreshGovernance) await refreshGovernance(); }),
        safeApiCall(async () => { if (refreshAuditLog) await refreshAuditLog(); }),
        safeApiCall(async () => { if (refreshStats) await refreshStats(); }),
      ]);
    } finally {
      onSuccess?.();
    }
  }

  /**
   * 审核操作后回调
   */
  async function afterReviewAction(callbacks: {
    refreshReviewList?: () => Promise<void>;
    refreshReviewStats?: () => Promise<void>;
    onSuccess?: () => void;
  }) {
    const { refreshReviewList, refreshReviewStats, onSuccess } = callbacks;

    try {
      await Promise.allSettled([
        safeApiCall(async () => { if (refreshReviewList) await refreshReviewList(); }),
        safeApiCall(async () => { if (refreshReviewStats) await refreshReviewStats(); }),
      ]);
    } finally {
      onSuccess?.();
    }
  }

  /**
   * 申诉操作后回调
   */
  async function afterAppealAction(callbacks: {
    refreshAppealList?: () => Promise<void>;
    refreshGovernance?: () => Promise<void>;
    onSuccess?: () => void;
  }) {
    const { refreshAppealList, refreshGovernance, onSuccess } = callbacks;

    try {
      await Promise.allSettled([
        safeApiCall(async () => { if (refreshAppealList) await refreshAppealList(); }),
        safeApiCall(async () => { if (refreshGovernance) await refreshGovernance(); }),
      ]);
    } finally {
      onSuccess?.();
    }
  }

  /**
   * 操作确认弹窗通用逻辑
   */
  function confirmLifecycleAction(
    actionName: string,
    impactDescription: string,
    isHighRisk: boolean,
    callback: (reason: string, channelNameConfirm?: string) => Promise<void>,
  ) {
    // 注：实际弹窗逻辑在 LifecycleActionModal 组件中实现
    // 此函数提供统一的 action 执行入口
    return { actionName, impactDescription, isHighRisk, callback };
  }

  return {
    afterLifecycleAction,
    afterReviewAction,
    afterAppealAction,
    confirmLifecycleAction,
  };
}
