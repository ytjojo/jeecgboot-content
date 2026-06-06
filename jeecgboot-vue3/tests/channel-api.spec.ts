import { vi, describe, it, expect, beforeEach } from 'vitest';

vi.mock('@/qiankun/micro', () => ({ getGlobal: vi.fn(() => ({})) }));
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';
import {
  getChannelList,
  getChannelDetail,
  createChannel,
  updateChannel,
  deleteChannel,
  cancelDeleteChannel,
  checkNameUnique,
  checkDeletePrecondition,
  getTransferHistory,
  getPendingTransfer,
  transferChannel,
  confirmTransfer,
  rejectTransfer,
  createSystemChannel,
  reviewChannel,
  getReviewList,
  reviewAction,
} from '/@/api/content/channel';

const mockGet = defHttp.get as unknown as ReturnType<typeof vi.fn>;
const mockPost = defHttp.post as unknown as ReturnType<typeof vi.fn>;
const mockPut = defHttp.put as unknown as ReturnType<typeof vi.fn>;
const mockDelete = defHttp.delete as unknown as ReturnType<typeof vi.fn>;

describe('api/content/channel', () => {
  beforeEach(() => {
    mockGet.mockReset();
    mockPost.mockReset();
    mockPut.mockReset();
    mockDelete.mockReset();
  });

  describe('getChannelList', () => {
    it('GETs /api/v1/channels/list with query params', () => {
      getChannelList({ current: 1, size: 10, channelType: 'personal' });
      expect(mockGet).toHaveBeenCalledWith({
        url: '/api/v1/channels/list',
        params: { current: 1, size: 10, channelType: 'personal' },
      });
    });
  });

  describe('getChannelDetail', () => {
    it('GETs /api/v1/channels/{id}', () => {
      getChannelDetail('ch-1');
      expect(mockGet).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1' });
    });
  });

  describe('createChannel', () => {
    it('POSTs to /api/v1/channels/create with data', () => {
      const data = { name: 'test', description: 'desc', channelType: 'personal' as const, categoryName: 'tech' };
      createChannel(data);
      expect(mockPost).toHaveBeenCalledWith({ url: '/api/v1/channels/create', data });
    });
  });

  describe('updateChannel', () => {
    it('PUTs to /api/v1/channels/{id} with data', () => {
      const data = { name: 'updated', description: 'desc', channelType: 'personal' as const, categoryName: 'tech' };
      updateChannel('ch-1', data);
      expect(mockPut).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1', data });
    });
  });

  describe('deleteChannel', () => {
    it('DELETEs /api/v1/channels/{id}', () => {
      deleteChannel('ch-1');
      expect(mockDelete).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1' });
    });
  });

  describe('cancelDeleteChannel', () => {
    it('POSTs to /api/v1/channels/{id}/cancel-delete', () => {
      cancelDeleteChannel('ch-1');
      expect(mockPost).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1/cancel-delete' });
    });
  });

  describe('checkNameUnique', () => {
    it('GETs /api/v1/channels/check-name with name param', () => {
      checkNameUnique('test-channel');
      expect(mockGet).toHaveBeenCalledWith({
        url: '/api/v1/channels/check-name',
        params: { name: 'test-channel', excludeId: undefined },
      });
    });

    it('passes excludeId when editing', () => {
      checkNameUnique('test-channel', 'ch-1');
      expect(mockGet).toHaveBeenCalledWith({
        url: '/api/v1/channels/check-name',
        params: { name: 'test-channel', excludeId: 'ch-1' },
      });
    });
  });

  describe('checkDeletePrecondition', () => {
    it('GETs /api/v1/channels/{id}/delete-check', () => {
      checkDeletePrecondition('ch-1');
      expect(mockGet).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1/delete-check' });
    });
  });

  describe('getTransferHistory', () => {
    it('GETs /api/v1/channels/{id}/transfers', () => {
      getTransferHistory('ch-1');
      expect(mockGet).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1/transfers' });
    });
  });

  describe('getPendingTransfer', () => {
    it('GETs /api/v1/channels/{id}/transfer/pending', () => {
      getPendingTransfer('ch-1');
      expect(mockGet).toHaveBeenCalledWith({ url: '/api/v1/channels/ch-1/transfer/pending' });
    });
  });

  describe('transferChannel', () => {
    it('POSTs to /api/v1/channels/{id}/transfer with toUserId', () => {
      transferChannel('ch-1', 'user-2');
      expect(mockPost).toHaveBeenCalledWith({
        url: '/api/v1/channels/ch-1/transfer',
        params: { toUserId: 'user-2' },
      });
    });
  });

  describe('confirmTransfer', () => {
    it('POSTs to /api/v1/channels/transfer/{id}/confirm', () => {
      confirmTransfer('tr-1');
      expect(mockPost).toHaveBeenCalledWith({ url: '/api/v1/channels/transfer/tr-1/confirm' });
    });
  });

  describe('rejectTransfer', () => {
    it('POSTs to /api/v1/channels/transfer/{id}/reject', () => {
      rejectTransfer('tr-1');
      expect(mockPost).toHaveBeenCalledWith({ url: '/api/v1/channels/transfer/tr-1/reject' });
    });
  });

  describe('createSystemChannel', () => {
    it('POSTs to /api/v1/admin/channels/create-system', () => {
      const data = { name: 'sys', description: 'desc', iconUrl: '', categoryName: 'tech' };
      createSystemChannel(data);
      expect(mockPost).toHaveBeenCalledWith({ url: '/api/v1/admin/channels/create-system', data });
    });
  });

  describe('reviewChannel', () => {
    it('POSTs to /api/v1/admin/channels/{id}/review with action', () => {
      reviewChannel('ch-1', 'APPROVE');
      expect(mockPost).toHaveBeenCalledWith({
        url: '/api/v1/admin/channels/ch-1/review',
        params: { action: 'APPROVE', note: undefined },
      });
    });

    it('passes note when rejecting', () => {
      reviewChannel('ch-1', 'REJECT', 'bad content');
      expect(mockPost).toHaveBeenCalledWith({
        url: '/api/v1/admin/channels/ch-1/review',
        params: { action: 'REJECT', note: 'bad content' },
      });
    });
  });

  describe('getReviewList', () => {
    it('GETs review list with params', () => {
      getReviewList({ current: 1, size: 20 });
      expect(mockGet).toHaveBeenCalledWith({
        url: '/jeecg-boot/api/v1/content/channel/review/list',
        params: { current: 1, size: 20 },
      });
    });
  });

  describe('reviewAction', () => {
    it('POSTs review action data', () => {
      reviewAction({ channelId: 'ch-1', action: 'APPROVE' });
      expect(mockPost).toHaveBeenCalledWith({
        url: '/jeecg-boot/api/v1/content/channel/review/action',
        data: { channelId: 'ch-1', action: 'APPROVE' },
      });
    });
  });
});
