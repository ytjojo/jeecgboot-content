import { vi } from 'vitest';
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';

import { deleteComment, warnUser, listAuditLog } from '/@/api/content/governance';

describe('api/content/governance', () => {
  beforeEach(() => {
    defHttp.get.mockReset();
    defHttp.post.mockReset();
  });

  it('POSTs moderator comment delete with commentId and reason', () => {
    deleteComment('comment-1', '违规内容');
    expect(defHttp.post).toHaveBeenCalledWith({
      url: '/api/v1/content/user/governance/moderator/comment/delete',
      data: { commentId: 'comment-1', reason: '违规内容' },
    });
  });

  it('POSTs moderator user warn with userId and reason', () => {
    warnUser('user-2', '恶意评论');
    expect(defHttp.post).toHaveBeenCalledWith({
      url: '/api/v1/content/user/governance/moderator/user/warn',
      data: { userId: 'user-2', reason: '恶意评论' },
    });
  });

  it('GETs audit log with pagination and filters', () => {
    listAuditLog({ pageNo: 2, pageSize: 20, operatorUserId: 'op-1', eventType: 'DELETE_COMMENT' });
    expect(defHttp.get).toHaveBeenCalledWith({
      url: '/api/v1/content/user/governance/audit-log',
      params: { pageNo: 2, pageSize: 20, operatorUserId: 'op-1', eventType: 'DELETE_COMMENT' },
    });
  });

  it('GETs audit log with no filters', () => {
    listAuditLog();
    expect(defHttp.get).toHaveBeenCalledWith({
      url: '/api/v1/content/user/governance/audit-log',
      params: {},
    });
  });
});
