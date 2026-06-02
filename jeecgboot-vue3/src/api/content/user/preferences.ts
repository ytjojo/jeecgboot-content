import { defHttp } from '/@/utils/http/axios';

enum Api {
  topics = '/api/v1/user/preferences/topics',
}

export const saveUserTopicPreferences = (topicIds: string[]) =>
  defHttp.post({ url: Api.topics, params: { topicIds } });
