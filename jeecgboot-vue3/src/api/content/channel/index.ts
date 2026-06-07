import { defHttp } from '/@/utils/http/axios';
import type {
  ChannelVO,
  ChannelListQuery,
  ChannelCreateReq,
  SystemChannelCreateReq,
  ChannelTransferVO,
  DeleteCheckResultVO,
  NameCheckResultVO,
  ChannelReviewVO,
  AdminChannelListQuery,
  PageResult,
  ReviewActionReq,
} from './model/channelModel';

enum Api {
  // 用户端
  create = '/api/v1/content/channels/create',
  detail = '/api/v1/content/channels/',
  list = '/api/v1/content/channels/list',
  checkName = '/api/v1/content/channels/check-name',
  // 管理端
  adminCreateSystem = '/api/v1/content/admin/channels/create-system',
  adminReview = '/api/v1/content/admin/channels/',
  // 审核
  reviewList = '/api/v1/content/channel/review/list',
  reviewAction = '/api/v1/content/channel/review/action',
}

/** 获取频道分页列表 */
export const getChannelList = (params: ChannelListQuery) =>
  defHttp.get<PageResult<ChannelVO>>({ url: Api.list, params });

/** 获取频道详情 */
export const getChannelDetail = (id: string) =>
  defHttp.get<ChannelVO>({ url: Api.detail + id });

/** 创建频道 */
export const createChannel = (data: ChannelCreateReq) =>
  defHttp.post<ChannelVO>({ url: Api.create, data });

/** 更新频道 */
export const updateChannel = (id: string, data: ChannelCreateReq) =>
  defHttp.put<void>({ url: Api.detail + id, data });

/** 删除频道 */
export const deleteChannel = (id: string) =>
  defHttp.delete<void>({ url: Api.detail + id });

/** 撤回删除频道 */
export const cancelDeleteChannel = (id: string) =>
  defHttp.post<void>({ url: Api.detail + id + '/cancel-delete' });

/** 检查频道名称是否可用 */
export const checkNameUnique = (name: string, excludeId?: string) =>
  defHttp.get<NameCheckResultVO>({ url: Api.checkName, params: { name, excludeId } });

/** 删除前置条件检查 */
export const checkDeletePrecondition = (id: string) =>
  defHttp.get<DeleteCheckResultVO>({ url: Api.detail + id + '/delete-check' });

/** 获取转让历史 */
export const getTransferHistory = (id: string) =>
  defHttp.get<ChannelTransferVO[]>({ url: Api.detail + id + '/transfers' });

/** 获取待处理的转让请求 */
export const getPendingTransfer = (id: string) =>
  defHttp.get<ChannelTransferVO | null>({ url: Api.detail + id + '/transfer/pending' });

/** 发起频道转让 */
export const transferChannel = (id: string, toUserId: string) =>
  defHttp.post<void>({ url: Api.detail + id + '/transfer', params: { toUserId } });

/** 确认频道转让 */
export const confirmTransfer = (transferId: string) =>
  defHttp.post<void>({ url: Api.detail + 'transfer/' + transferId + '/confirm' });

/** 拒绝频道转让 */
export const rejectTransfer = (transferId: string) =>
  defHttp.post<void>({ url: Api.detail + 'transfer/' + transferId + '/reject' });

/** 管理员创建系统频道 */
export const createSystemChannel = (data: SystemChannelCreateReq) =>
  defHttp.post<ChannelVO>({ url: Api.adminCreateSystem, data });

/** 管理员审核频道 */
export const reviewChannel = (channelId: string, action: string, note?: string) =>
  defHttp.post<void>({ url: Api.adminReview + channelId + '/review', params: { action, note } });

/** 获取审核列表 */
export const getReviewList = (params: AdminChannelListQuery) =>
  defHttp.get<PageResult<ChannelReviewVO>>({ url: Api.reviewList, params });

/** 审核操作 */
export const reviewAction = (data: ReviewActionReq) =>
  defHttp.post<void>({ url: Api.reviewAction, data });
