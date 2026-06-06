import { useUserStatusStore } from '/@/store/modules/userStatus';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';

const MUTED_STATUSES = ['MUTED'];
const BLOCKED_STATUSES = ['FROZEN', 'BANNED'];

export function useStatusGuard() {
  const userStatusStore = useUserStatusStore();
  const userStore = useUserStore();
  const { createWarningModal } = useMessage();

  function canPerformAction(action: 'comment' | 'message' | 'post' | 'recommend'): boolean {
    const status = userStatusStore.currentStatus;
    if (!status || status === 'NORMAL') return true;

    if (BLOCKED_STATUSES.includes(status)) return false;

    if (MUTED_STATUSES.includes(status)) {
      if (['comment', 'message', 'post'].includes(action)) {
        return false;
      }
    }
    if (status === 'RESTRICTED_RECOMMEND' && action === 'recommend') {
      return false;
    }
    return true;
  }

  function showBlockModal(action: string) {
    const status = userStatusStore.currentStatus;
    const messages: Record<string, string> = {
      MUTED: '您当前处于禁言状态，无法执行此操作。',
      RESTRICTED_RECOMMEND: '您的推荐功能已被限制。',
      FROZEN: '您的账号已被冻结，请先完成安全核验。',
      BANNED: '您的账号已被封禁，无法执行此操作。',
    };
    createWarningModal({
      title: '操作受限',
      content: messages[status || ''] || `您当前状态（${status}）不允许执行${action}操作。`,
    });
  }

  async function guardAction(action: 'comment' | 'message' | 'post' | 'recommend'): Promise<boolean> {
    const userId = userStore.getUserInfo?.id as string | undefined;
    if (userId && !userStatusStore.currentStatus) {
      await userStatusStore.fetchCurrentStatus(userId);
    }
    if (canPerformAction(action)) {
      return true;
    }
    showBlockModal(action);
    return false;
  }

  return { canPerformAction, showBlockModal, guardAction };
}
