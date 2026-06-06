import { vi } from 'vitest';

vi.mock('/@/store/modules/userStatus', () => ({
  useUserStatusStore: vi.fn(),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: vi.fn(),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: vi.fn(),
}));

import { useStatusGuard } from '/@/composables/useStatusGuard';
import { useUserStatusStore } from '/@/store/modules/userStatus';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';

const mockCreateWarningModal = vi.fn();
const mockFetchCurrentStatus = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
  (useMessage as any).mockReturnValue({ createWarningModal: mockCreateWarningModal });
  (useUserStatusStore as any).mockReturnValue({
    currentStatus: null,
    fetchCurrentStatus: mockFetchCurrentStatus,
  });
  (useUserStore as any).mockReturnValue({
    getUserInfo: { id: 'u1' },
  });
});

describe('useStatusGuard', () => {
  describe('canPerformAction', () => {
    it('should return true when status is null', () => {
      const { canPerformAction } = useStatusGuard();
      expect(canPerformAction('comment')).toBe(true);
    });

    it('should return true when status is NORMAL', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'NORMAL',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { canPerformAction } = useStatusGuard();
      expect(canPerformAction('comment')).toBe(true);
      expect(canPerformAction('message')).toBe(true);
      expect(canPerformAction('post')).toBe(true);
      expect(canPerformAction('recommend')).toBe(true);
    });

    it('should block comment/message/post for MUTED status', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'MUTED',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { canPerformAction } = useStatusGuard();
      expect(canPerformAction('comment')).toBe(false);
      expect(canPerformAction('message')).toBe(false);
      expect(canPerformAction('post')).toBe(false);
      expect(canPerformAction('recommend')).toBe(true);
    });

    it('should block recommend for RESTRICTED_RECOMMEND status', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'RESTRICTED_RECOMMEND',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { canPerformAction } = useStatusGuard();
      expect(canPerformAction('recommend')).toBe(false);
      expect(canPerformAction('comment')).toBe(true);
    });

    it('should block all actions for FROZEN status', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'FROZEN',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { canPerformAction } = useStatusGuard();
      expect(canPerformAction('comment')).toBe(false);
      expect(canPerformAction('message')).toBe(false);
      expect(canPerformAction('post')).toBe(false);
      expect(canPerformAction('recommend')).toBe(false);
    });

    it('should block all actions for BANNED status', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'BANNED',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { canPerformAction } = useStatusGuard();
      expect(canPerformAction('comment')).toBe(false);
      expect(canPerformAction('message')).toBe(false);
      expect(canPerformAction('post')).toBe(false);
      expect(canPerformAction('recommend')).toBe(false);
    });
  });

  describe('showBlockModal', () => {
    it('should show MUTED message', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'MUTED',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { showBlockModal } = useStatusGuard();
      showBlockModal('comment');
      expect(mockCreateWarningModal).toHaveBeenCalledWith({
        title: '操作受限',
        content: '您当前处于禁言状态，无法执行此操作。',
      });
    });

    it('should show FROZEN message', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'FROZEN',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { showBlockModal } = useStatusGuard();
      showBlockModal('comment');
      expect(mockCreateWarningModal).toHaveBeenCalledWith({
        title: '操作受限',
        content: '您的账号已被冻结，请先完成安全核验。',
      });
    });

    it('should show BANNED message', () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'BANNED',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { showBlockModal } = useStatusGuard();
      showBlockModal('comment');
      expect(mockCreateWarningModal).toHaveBeenCalledWith({
        title: '操作受限',
        content: '您的账号已被封禁，无法执行此操作。',
      });
    });
  });

  describe('guardAction', () => {
    it('should return true for allowed action', async () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'NORMAL',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { guardAction } = useStatusGuard();
      const result = await guardAction('comment');
      expect(result).toBe(true);
    });

    it('should fetch status if not loaded and return true for allowed', async () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: null,
        fetchCurrentStatus: mockFetchCurrentStatus.mockResolvedValue(undefined),
      });
      const { guardAction } = useStatusGuard();
      const result = await guardAction('comment');
      expect(mockFetchCurrentStatus).toHaveBeenCalledWith('u1');
      expect(result).toBe(true);
    });

    it('should return false and show modal for blocked action', async () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'MUTED',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { guardAction } = useStatusGuard();
      const result = await guardAction('comment');
      expect(result).toBe(false);
      expect(mockCreateWarningModal).toHaveBeenCalled();
    });

    it('should not fetch status if already loaded', async () => {
      (useUserStatusStore as any).mockReturnValue({
        currentStatus: 'NORMAL',
        fetchCurrentStatus: mockFetchCurrentStatus,
      });
      const { guardAction } = useStatusGuard();
      await guardAction('comment');
      expect(mockFetchCurrentStatus).not.toHaveBeenCalled();
    });
  });
});
