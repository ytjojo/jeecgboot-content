import { defHttp } from '/@/utils/http/axios';
import type { PageResult, CircleVO, CircleCreateReq, CircleUpdateReq, CircleJoinReq, CircleLeaveReq, CircleMemberUpdateReq, CircleMemberVO, CircleSearchResultVO, CircleGovernanceLogVO } from './model/circleModel';

enum Api {
  // 圈子 CRUD
  create = '/api/v1/content/circle/create',
  update = '/api/v1/content/circle/update',
  detail = '/api/v1/content/circle',
  join = '/api/v1/content/circle/join',
  leave = '/api/v1/content/circle/leave',
  checkName = '/api/v1/content/circle/check-name',
  myList = '/api/v1/content/circle/my-list',
  publicList = '/api/v1/content/circle/public-list',

  // 成员管理
  memberList = '/api/v1/content/circle/member/list',
  changeRole = '/api/v1/content/circle/member/change-role',
  mute = '/api/v1/content/circle/member/mute',
  unmute = '/api/v1/content/circle/member/unmute',
  remove = '/api/v1/content/circle/member/remove',

  // 搜索
  search = '/api/v1/content/circle/search',

  // 治理日志
  governanceLogList = '/api/v1/content/circle/governance-log/list',
}

// ========== 圈子 CRUD ==========

/** 创建圈子 */
export const createCircle = (data: CircleCreateReq) =>
  defHttp.post({ url: Api.create, data });

/** 更新圈子 */
export const updateCircle = (data: CircleUpdateReq) =>
  defHttp.put({ url: Api.update, data });

/** 获取圈子详情（Path 参数） */
export const getCircleDetail = (id: string) =>
  defHttp.get<CircleVO>({ url: `${Api.detail}/${id}` });

/** 加入圈子 */
export const joinCircle = (data: CircleJoinReq) =>
  defHttp.post({ url: Api.join, data });

/** 退出圈子 */
export const leaveCircle = (data: CircleLeaveReq) =>
  defHttp.post({ url: Api.leave, data });

/** 校验圈子名称唯一性 */
export const checkCircleName = (name: string) =>
  defHttp.get<boolean>({ url: Api.checkName, params: { name } });

/** 获取已加入圈子列表 */
export const getMyCircleList = (params: { pageNum: number; pageSize: number }) =>
  defHttp.get<PageResult<CircleVO>>({ url: Api.myList, params });

/** 获取公开圈子列表 */
export const getPublicCircleList = (params: { pageNum: number; pageSize: number }) =>
  defHttp.get<PageResult<CircleVO>>({ url: Api.publicList, params });

// ========== 成员管理 ==========

/** 获取成员列表 */
export const getMemberList = (params: { circleId: string; role?: string; status?: string; pageNum: number; pageSize: number }) =>
  defHttp.get<PageResult<CircleMemberVO>>({ url: Api.memberList, params });

/** 变更成员角色 */
export const changeMemberRole = (data: CircleMemberUpdateReq) =>
  defHttp.post({ url: Api.changeRole, data });

/** 禁言成员 */
export const muteMember = (data: CircleMemberUpdateReq) =>
  defHttp.post({ url: Api.mute, data });

/** 解除禁言 */
export const unmuteMember = (data: CircleMemberUpdateReq) =>
  defHttp.post({ url: Api.unmute, data });

/** 移除成员 */
export const removeMember = (data: CircleMemberUpdateReq) =>
  defHttp.post({ url: Api.remove, data });

// ========== 搜索 ==========

/** 搜索圈子 */
export const searchCircle = (params: { keyword: string; pageNum: number; pageSize: number }) =>
  defHttp.get<PageResult<CircleSearchResultVO>>({ url: Api.search, params });

// ========== 治理日志 ==========

/** 获取治理日志列表 */
export const getGovernanceLogList = (params: { circleId: string; pageNum: number; pageSize: number }) =>
  defHttp.get<PageResult<CircleGovernanceLogVO>>({ url: Api.governanceLogList, params });
