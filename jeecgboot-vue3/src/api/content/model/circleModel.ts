/** 隐私类型 */
export type PrivacyType = 'PUBLIC' | 'PRIVATE' | 'PASSWORD';

/** 加入方式 */
export type JoinType = 'DIRECT' | 'APPROVAL' | 'INVITE' | 'PASSWORD';

/** 成员角色 */
export type MemberRole = 'CREATOR' | 'MODERATOR' | 'MEMBER';

/** 成员状态 */
export type MemberStatus = 'ACTIVE' | 'MUTED' | 'REMOVED';

/** 申请状态 */
export type ApplyStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | null;

/** 圈子 VO */
export interface CircleVO {
  id: string;
  name: string;
  description: string;
  iconUrl: string;
  coverUrl: string;
  category: string;
  privacyType: PrivacyType;
  joinType: JoinType;
  creatorId: string;
  memberCount: number;
  maxMemberCount: number;
  status: string;
  joined: boolean;
  myRole: MemberRole | null;
  applyStatus: ApplyStatus;
  isInvited: boolean;
  createTime: string;
}

/** 创建圈子请求 */
export interface CircleCreateReq {
  name: string;
  description: string;
  iconUrl?: string;
  coverUrl?: string;
  category?: string;
  privacyType: PrivacyType;
  joinType: JoinType;
  password?: string;
}

/** 更新圈子请求 */
export interface CircleUpdateReq {
  id: string;
  description: string;
  iconUrl?: string;
  coverUrl?: string;
  category?: string;
}

/** 加入圈子请求 */
export interface CircleJoinReq {
  circleId: string;
  password?: string;
}

/** 退出圈子请求 */
export interface CircleLeaveReq {
  circleId: string;
}

/** 成员操作请求（变更角色/禁言/解除禁言/移除） */
export interface CircleMemberUpdateReq {
  circleId: string;
  targetUserId: string;
  targetRole?: MemberRole;
  muteDuration?: string;
  reason?: string;
}

/** 搜索请求 */
export interface CircleSearchReq {
  keyword: string;
  pageNum: number;
  pageSize: number;
}

/** 成员 VO */
export interface CircleMemberVO {
  id: string;
  userId: string;
  role: MemberRole;
  status: MemberStatus;
  muteEndTime: string | null;
  createTime: string;
}

/** 搜索结果 VO */
export interface CircleSearchResultVO {
  id: string;
  name: string;
  iconUrl: string;
  description: string;
  category: string;
  memberCount: number;
  joined: boolean;
}

/** 治理日志 VO */
export interface CircleGovernanceLogVO {
  id: string;
  circleId: string;
  operatorId: string;
  targetUserId: string;
  operationType: string;
  detail: string;
  reason: string;
  createTime: string;
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[];
  total: number;
  pages: number;
  current: number;
  size: number;
}
