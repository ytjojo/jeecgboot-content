import { defHttp } from '/@/utils/http/axios';

/**
 * 圈子等级权益 VO
 */
export interface CircleBenefitVO {
  /** 权益名称 */
  name: string;
  /** 是否已解锁 */
  unlocked: boolean;
}

/**
 * 等级条件 VO
 */
export interface LevelConditionVO {
  /** 条件类型 */
  type: string;
  /** 条件标签 */
  label: string;
  /** 当前值 */
  current: number;
  /** 要求值 */
  required: number;
  /** 差距 */
  gap: number;
}

/**
 * 圈子等级 VO
 */
export interface CircleLevelVO {
  /** 当前等级（1-5） */
  level: number;
  /** 等级名称 */
  levelName: string;
  /** 成长分 */
  growthScore: number;
  /** 下一等级门槛分数（最高等级时为 null） */
  nextLevelThreshold: number | null;
  /** 进度百分比 */
  progressPercent: number;
  /** 成员规模得分 */
  memberScore: number;
  /** 内容贡献得分 */
  contentScore: number;
  /** 活跃互动得分 */
  activityScore: number;
  /** 已解锁权益列表 */
  benefits: CircleBenefitVO[];
  /** 下一等级条件列表 */
  nextLevelConditions: LevelConditionVO[];
}

/**
 * 成就/徽章 VO
 */
export interface AchievementVO {
  /** 徽章类型标识 */
  achievementType: string;
  /** 徽章名称 */
  name: string;
  /** 徽章描述 */
  description: string;
  /** 徽章图标 URL */
  iconUrl: string;
  /** 是否已获得 */
  earned: boolean;
  /** 获得时间 */
  earnedDate: string | null;
  /** 达成条件描述 */
  conditionDesc: string;
  /** 当前进度值 */
  currentProgress: number;
  /** 目标值 */
  targetProgress: number;
  /** 状态：EARNED / CLOSE / UNEARNED */
  status: 'EARNED' | 'CLOSE' | 'UNEARNED';
}

/**
 * 成员成长 VO
 */
export interface MemberGrowthVO {
  /** 经验值 */
  expPoints: number;
  /** 贡献值 */
  contributionPoints: number;
  /** 成员等级 */
  level: number;
  /** 等级名称 */
  levelName: string;
  /** 下一等级所需经验值（L5时为null） */
  nextLevelThreshold: number | null;
  /** 发帖数 */
  postCount: number;
  /** 连续参与天数 */
  participationDays: number;
  /** 圈内排名 */
  rank: number;
  /** 今日已获经验值 */
  todayExp: number;
  /** 每日经验上限 */
  dailyExpLimit: number;
  /** 最近获得徽章 */
  recentBadges: AchievementVO[];
  /** 等级进度百分比 */
  progressPercent: number;
}

/**
 * 排行榜条目 VO
 */
export interface LeaderboardEntryVO {
  /** 用户ID */
  userId: string;
  /** 用户名 */
  username: string;
  /** 用户头像 */
  avatar: string;
  /** 得分 */
  score: number;
  /** 排名 */
  rankNum: number;
  /** 是否当前用户 */
  highlighted: boolean;
  /** 距上一名差距 */
  gap: number;
}

/**
 * 排行榜响应（前端包装）
 */
export interface LeaderboardResponse {
  /** 排行榜条目列表 */
  entries: LeaderboardEntryVO[];
  /** 当前用户信息 */
  currentUser: LeaderboardEntryVO | null;
  /** 总人数 */
  totalCount: number;
}

enum Api {
  /** 获取圈子等级信息 */
  getCircleLevelInfo = '/api/v1/content/circle/growth/level/info',
  /** 获取成员成长信息 */
  getMemberGrowth = '/api/v1/content/circle/member_growth/info',
  /** 获取连续参与天数 */
  getParticipationDays = '/api/v1/content/circle/member_growth/participation',
  /** 获取成就徽章列表 */
  getAchievements = '/api/v1/content/circle/growth/achievement/list',
  /** 获取排行榜 */
  getLeaderboard = '/api/v1/content/circle/growth/leaderboard',
}

/**
 * 获取圈子等级信息
 * @param circleId 圈子ID
 */
export const getCircleLevelInfo = (circleId: string) =>
  defHttp.get<CircleLevelVO>({ url: Api.getCircleLevelInfo, params: { circleId } });

/**
 * 获取成员成长信息
 * @param circleId 圈子ID
 * @param userId 用户ID
 */
export const getMemberGrowth = (circleId: string, userId: string) =>
  defHttp.get<MemberGrowthVO>({ url: Api.getMemberGrowth, params: { circleId, userId } });

/**
 * 获取连续参与天数
 * @param circleId 圈子ID
 * @param userId 用户ID
 */
export const getParticipationDays = (circleId: string, userId: string) =>
  defHttp.get<{ participationDays: number; streakDetail?: boolean[] }>({
    url: Api.getParticipationDays,
    params: { circleId, userId },
  });

/**
 * 获取成就徽章列表
 * @param circleId 圈子ID
 * @param userId 用户ID
 */
export const getAchievements = (circleId: string, userId: string) =>
  defHttp.get<AchievementVO[]>({ url: Api.getAchievements, params: { circleId, userId } });

/**
 * 获取排行榜
 * @param params 查询参数
 */
export const getLeaderboard = (params: {
  circleId: string;
  dimension: string;
  period: string;
  currentUserId: string;
}) =>
  defHttp.get<LeaderboardEntryVO[]>({ url: Api.getLeaderboard, params }).then((entries) => {
    const currentUser = entries.find((item) => item.highlighted) || null;
    const response: LeaderboardResponse = {
      entries,
      currentUser,
      totalCount: entries.length,
    };
    return response;
  });
