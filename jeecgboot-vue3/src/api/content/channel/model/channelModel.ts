/** 频道类型 */
export type ChannelType = 'system' | 'personal' | 'organization';

/** 频道状态 */
export type ChannelStatus = 'DRAFT' | 'PENDING_REVIEW' | 'ACTIVE' | 'REJECTED' | 'DELETE_COOLING' | 'DELETED';

/** 转让状态 */
export type TransferStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';

/** 审核操作类型 */
export type ReviewActionType = 'APPROVE' | 'REJECT' | 'RETURN';

/** 频道详情 VO */
export interface ChannelVO {
  id: string;
  name: string;
  description: string;
  iconUrl: string;
  coverUrl: string;
  channelType: ChannelType;
  status: ChannelStatus;
  categoryName: string;
  ownerId: string;
  ownerName: string;
  topWeight: number;
  orgId: string;
  orgName: string;
  createdTime: string;
  updatedTime: string;
}

/** 频道列表查询参数 */
export interface ChannelListQuery {
  current: number;
  size: number;
  channelType?: ChannelType;
  status?: ChannelStatus;
  keyword?: string;
}

/** 创建/更新频道请求 */
export interface ChannelCreateReq {
  name: string;
  description: string;
  channelType: ChannelType;
  iconUrl: string;
  coverUrl?: string;
  categoryName: string;
  orgId?: string;
}

/** 管理员创建系统频道请求 */
export interface SystemChannelCreateReq {
  name: string;
  description: string;
  iconUrl: string;
  coverUrl?: string;
  categoryName: string;
  topWeight?: number;
}

/** 频道转让 VO */
export interface ChannelTransferVO {
  transferId: string;
  channelId: string;
  fromUserId: string;
  fromUserName: string;
  toUserId: string;
  toUserName: string;
  status: TransferStatus;
  createdTime: string;
}

/** 删除检查结果 */
export interface DeleteCheckResultVO {
  canDelete: boolean;
  blockReasons: string[];
  needOrgAdminConfirm: boolean;
}

/** 名称检查结果 */
export interface NameCheckResultVO {
  available: boolean;
  message: string;
}

/** 审核 VO */
export interface ChannelReviewVO {
  id: string;
  channelId: string;
  channelName: string;
  channelType: ChannelType;
  submitterId: string;
  submitterName: string;
  submitTime: string;
  waitDuration: number;
  status: string;
  reviewNote: string;
}

/** 管理员频道列表查询参数 */
export interface AdminChannelListQuery {
  current: number;
  size: number;
  channelType?: ChannelType;
  status?: ChannelStatus;
  ownerType?: string;
  startDate?: string;
  endDate?: string;
  keyword?: string;
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

/** 审核操作请求 */
export interface ReviewActionReq {
  channelId: string;
  action: ReviewActionType;
  note?: string;
}
