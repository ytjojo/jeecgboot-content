import { defHttp } from '/@/utils/http/axios';

// Mock defHttp
jest.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

import {
  getCurrentStatus,
  getUserStatus,
  getStatusList,
  getTransitions,
  changeUserStatus,
  releaseUser,
  batchReleaseUsers,
  getStatusHistory,
  getAuditLogList,
  getAuditLogDetail,
  getUserAuditLogs,
  exportAuditLogs,
  verifySecurity,
  sendVerifyCode,
} from '/@/api/content/userStatus';

describe('userStatus API', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getCurrentStatus', () => {
    it('should call GET /api/content/user-status/current with userId', async () => {
      const mockResult = { code: 200, result: { userId: 'u1', status: 'NORMAL' } };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const result = await getCurrentStatus('u1');

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/current',
        params: { userId: 'u1' },
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('getUserStatus', () => {
    it('should call GET /api/content/user-status/{userId}', async () => {
      const mockResult = { code: 200, result: { userId: 'u1', status: 'MUTED' } };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const result = await getUserStatus('u1');

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/u1',
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('getStatusList', () => {
    it('should call GET /api/content/user-status/list with query params', async () => {
      const mockResult = { code: 200, result: { records: [], total: 0 } };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const params = { userId: 'u1', status: 'NORMAL', page: 1, pageSize: 10 };
      const result = await getStatusList(params);

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/list',
        params,
      });
      expect(result).toEqual(mockResult);
    });

    it('should work with empty params', async () => {
      (defHttp.get as jest.Mock).mockResolvedValue({ code: 200, result: { records: [], total: 0 } });

      await getStatusList({});

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/list',
        params: {},
      });
    });
  });

  describe('getTransitions', () => {
    it('should call GET /api/content/user-status/transitions/{currentStatus}', async () => {
      const mockResult = { code: 200, result: ['NORMAL', 'MUTED'] };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const result = await getTransitions('NORMAL');

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/transitions/NORMAL',
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('changeUserStatus', () => {
    it('should call POST /api/content/user-status/{userId}/change with payload', async () => {
      const mockResult = { code: 200, result: null };
      (defHttp.post as jest.Mock).mockResolvedValue(mockResult);

      const payload = { toStatus: 'MUTED', reason: '违规', endTime: '2026-06-10' };
      const result = await changeUserStatus('u1', payload);

      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/content/user-status/u1/change',
        params: payload,
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('releaseUser', () => {
    it('should call POST /api/content/user-status/{userId}/release', async () => {
      const mockResult = { code: 200, result: null };
      (defHttp.post as jest.Mock).mockResolvedValue(mockResult);

      const result = await releaseUser('u1', '误封解禁');

      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/content/user-status/u1/release',
        params: { reason: '误封解禁' },
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('batchReleaseUsers', () => {
    it('should call POST /api/content/user-status/batch-release', async () => {
      const mockResult = { code: 200, result: null };
      (defHttp.post as jest.Mock).mockResolvedValue(mockResult);

      const result = await batchReleaseUsers(['u1', 'u2'], '批量解禁');

      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/content/user-status/batch-release',
        params: { userIds: ['u1', 'u2'], reason: '批量解禁' },
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('getStatusHistory', () => {
    it('should call GET /api/content/user-status/{userId}/history', async () => {
      const mockResult = { code: 200, result: [] };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const result = await getStatusHistory('u1');

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/u1/history',
        params: undefined,
      });
      expect(result).toEqual(mockResult);
    });

    it('should support pagination params', async () => {
      (defHttp.get as jest.Mock).mockResolvedValue({ code: 200, result: [] });

      await getStatusHistory('u1', { page: 2, pageSize: 20 });

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/u1/history',
        params: { page: 2, pageSize: 20 },
      });
    });
  });

  describe('getAuditLogList', () => {
    it('should call GET /api/content/user-status/audit-logs with query params', async () => {
      const mockResult = { code: 200, result: { records: [], total: 0 } };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const params = { userId: 'u1', startTime: '2026-01-01', endTime: '2026-06-01', page: 1, pageSize: 10 };
      const result = await getAuditLogList(params);

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/audit-logs',
        params,
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('getAuditLogDetail', () => {
    it('should call GET /api/content/user-status/audit-logs/{logId}', async () => {
      const mockResult = { code: 200, result: { id: 'log1', action: 'CHANGE_STATUS' } };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const result = await getAuditLogDetail('log1');

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/audit-logs/log1',
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('getUserAuditLogs', () => {
    it('should call GET /api/content/user-status/users/{userId}/audit-logs', async () => {
      const mockResult = { code: 200, result: { records: [], total: 0 } };
      (defHttp.get as jest.Mock).mockResolvedValue(mockResult);

      const result = await getUserAuditLogs('u1', { page: 1, pageSize: 10 });

      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/content/user-status/users/u1/audit-logs',
        params: { page: 1, pageSize: 10 },
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('exportAuditLogs', () => {
    it('should call GET /api/content/user-status/audit-logs/export with blob response', async () => {
      const mockBlob = new Blob(['test']);
      (defHttp.get as jest.Mock).mockResolvedValue(mockBlob);

      const params = { userId: 'u1', format: 'excel' as const };
      const result = await exportAuditLogs(params);

      expect(defHttp.get).toHaveBeenCalledWith(
        {
          url: '/api/content/user-status/audit-logs/export',
          params,
          responseType: 'blob',
        },
        { isTransformResponse: false }
      );
      expect(result).toEqual(mockBlob);
    });
  });

  describe('verifySecurity', () => {
    it('should call POST /api/content/user-status/verify-security', async () => {
      const mockResult = { code: 200, result: null };
      (defHttp.post as jest.Mock).mockResolvedValue(mockResult);

      const result = await verifySecurity('13800138000', '123456');

      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/content/user-status/verify-security',
        params: { phone: '13800138000', verifyCode: '123456' },
      });
      expect(result).toEqual(mockResult);
    });
  });

  describe('sendVerifyCode', () => {
    it('should call POST /api/content/user-status/send-verify-code', async () => {
      const mockResult = { code: 200, result: null };
      (defHttp.post as jest.Mock).mockResolvedValue(mockResult);

      const result = await sendVerifyCode('13800138000');

      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/content/user-status/send-verify-code',
        params: { phone: '13800138000' },
      });
      expect(result).toEqual(mockResult);
    });
  });
});
