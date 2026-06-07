import { defHttp } from '/@/utils/http/axios';

enum Api {
  joinApply = '/api/v1/content/channel/member/join/apply',
  applicationPending = '/api/v1/content/channel/member/applications/pending',
  applicationApprove = '/api/v1/content/channel/member/applications/approve',
  applicationReject = '/api/v1/content/channel/member/applications/reject',
  list = '/api/v1/content/channel/member/list',
  search = '/api/v1/content/channel/member/search',
  assignRole = '/api/v1/content/channel/member/assign-role',
  governanceRemove = '/api/v1/content/channel/governance/remove',
  governanceMute = '/api/v1/content/channel/governance/mute',
  governanceUnmute = '/api/v1/content/channel/governance/unmute',
}

/** 提交加入申请 */
export const applyToJoin = (data: { channelId: string; reason: string }) =>
  defHttp.post({ url: Api.joinApply, data });

/** 待审列表 */
export const getPendingApplications = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.applicationPending, params });

/** 批准申请（支持批量） */
export const approveApplications = (data: { channelId: string; applicationIds: string[] }) =>
  defHttp.post({ url: Api.applicationApprove, data });

/** 拒绝申请（支持批量） */
export const rejectApplications = (data: { channelId: string; applicationIds: string[]; reason: string }) =>
  defHttp.post({ url: Api.applicationReject, data });

/** 成员列表 */
export const getMemberList = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.list, params });

/** 修改角色 */
export const updateMemberRole = (data: { channelId: string; memberId: string; role: string }) =>
  defHttp.post({ url: Api.assignRole, data });

/** 移除成员（支持批量） */
export const removeMembers = (data: { channelId: string; memberIds: string[]; reason: string }) =>
  defHttp.post({ url: Api.governanceRemove, data });

/** 禁言成员 */
export const muteMember = (data: { channelId: string; memberId: string; duration: string; reason: string }) =>
  defHttp.post({ url: Api.governanceMute, data });

/** 解除禁言 */
export const unmuteMember = (data: { channelId: string; memberId: string }) =>
  defHttp.post({ url: Api.governanceUnmute, data });
