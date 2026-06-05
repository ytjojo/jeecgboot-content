import { defHttp } from '/@/utils/http/axios';

enum Api {
  createSession = '/content/user/support/customer-service/session',
  transfer = '/content/user/support/customer-service/session/{id}/transfer',
  sendMessage = '/content/user/support/customer-service/session/{id}/message',
  closeSession = '/content/user/support/customer-service/session/{id}/close',
  submitRating = '/content/user/support/customer-service/session/{id}/rating',
  sessionList = '/content/user/support/customer-service/sessions',
  sessionDetail = '/content/user/support/customer-service/session/{id}',
}

// 注意：后端 ContentServiceSessionVO 字段为 sessionType/status/rating/ratingComment/startTime/endTime/expired
// 前端使用 type/agentName/queuePosition/estimatedWaitTime 作为业务层字段
export interface ServiceSession {
  id: string;
  type: string; // 'bot' | 'human'（后端字段为 sessionType）
  status: string; // 'bot' | 'queuing' | 'human' | 'closed'
  agentName: string; // 后端无此字段，需后端补充或前端从其他来源获取
  queuePosition: number | null; // 后端无此字段，需后端补充
  estimatedWaitTime: number | null; // 秒，后端无此字段，需后端补充
  rating?: number; // 后端已有
  ratingComment?: string; // 后端已有
  startTime?: string; // 后端已有
  endTime?: string; // 后端已有
  expired?: boolean; // 后端已有
  createTime: string;
}

export interface ChatMessage {
  id: string;
  sessionId: string;
  senderType: string; // 'user' | 'bot' | 'agent' | 'system'
  content: string;
  messageType: string; // 'text' | 'image' | 'link'
  status: string; // 'sending' | 'sent' | 'failed'
  createTime: string;
}

export interface RatingParams {
  score: number; // 1-5
  comment?: string;
}

export interface SessionQueryParams {
  pageNo?: number;
  pageSize?: number;
}

/** 创建客服会话 */
export const createServiceSession = () =>
  defHttp.post({ url: Api.createSession });

/** 转人工客服 */
export const transferToHuman = (sessionId: string) =>
  defHttp.post({ url: Api.transfer.replace('{id}', sessionId) });

/** 发送消息 */
export const sendChatMessage = (sessionId: string, data: { content: string; messageType: string }) =>
  defHttp.post({ url: Api.sendMessage.replace('{id}', sessionId), data });

/** 结束会话 */
export const closeServiceSession = (sessionId: string) =>
  defHttp.post({ url: Api.closeSession.replace('{id}', sessionId) });

/** 提交服务评分 */
export const submitServiceRating = (sessionId: string, data: RatingParams) =>
  defHttp.post({ url: Api.submitRating.replace('{id}', sessionId), data });

/** 查询会话历史列表 */
export const getServiceSessionList = (params: SessionQueryParams) =>
  defHttp.get({ url: Api.sessionList, params });

/** 查询会话详情 */
export const getServiceSessionDetail = (sessionId: string) =>
  defHttp.get({ url: Api.sessionDetail.replace('{id}', sessionId) });
