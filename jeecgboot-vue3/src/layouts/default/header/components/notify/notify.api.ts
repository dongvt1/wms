import { defHttp } from '/@/utils/http/axios';

enum Api {
  listCementByUser = '/sys/annountCement/listByUser',
  getUnreadMessageCount = '/sys/annountCement/getUnreadMessageCount',
  editCementSend = '/sys/sysAnnouncementSend/editByAnntIdAndUserId',
  clearAllUnReadMessage = '/sys/annountCement/clearAllUnReadMessage',
}

/**
 * Get the system notification message list
 * @param params
 */
export const listCementByUser = (params?) => defHttp.get({ url: Api.listCementByUser, params });

/**
 * Get the number of unread messages from the user in the past two months
 * @param params
 */
export const getUnreadMessageCount = (params?) => defHttp.get({ url: Api.getUnreadMessageCount, params });

export const editCementSend = (anntId, params?) => defHttp.put({ url: Api.editCementSend, params: { anntId, ...params } });

/**
 * Clear all unread messages
 */
export const clearAllUnReadMessage = () => defHttp.post({ url: Api.clearAllUnReadMessage },{ isTransformResponse: false });
