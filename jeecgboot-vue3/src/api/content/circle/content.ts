import { defHttp } from '/@/utils/http/axios';

enum Api {
  content = '/api/v1/content/circle/content',
}

/** 切换置顶状态（toggle：已置顶→取消，未置顶→置顶） */
export const togglePin = (contentId: string, circleId: string) =>
  defHttp.put({ url: `${Api.content}/${contentId}/pin`, params: { circleId } });

/** 切换精华状态（toggle：已精华→取消，未精华→精华） */
export const toggleFeatured = (contentId: string, circleId: string) =>
  defHttp.put({ url: `${Api.content}/${contentId}/featured`, params: { circleId } });
