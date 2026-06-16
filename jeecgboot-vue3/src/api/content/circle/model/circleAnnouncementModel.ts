/** 圈子公告视图对象 */
export interface CircleAnnouncementVO {
  /** 公告ID */
  id: string;
  /** 圈子ID */
  circleId: string;
  /** 公告内容 */
  content: string;
  /** 过期时间，为空则永不过期 */
  expireAt?: string;
  /** 创建时间 */
  createTime?: string;
}
