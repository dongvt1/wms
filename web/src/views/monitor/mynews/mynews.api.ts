import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/sysAnnouncementSend/getMyAnnouncementSend',
  editCementSend = '/sys/sysAnnouncementSend/editByAnntIdAndUserId',
  readAllMsg = '/sys/sysAnnouncementSend/readAll',
  syncNotic = '/sys/annountCement/syncNotic',
  getOne = '/sys/sysAnnouncementSend/getOne',
}

/**
 * Query message list
 * @param params
 */
export const getMyNewsList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Update user system message reading status
 * @param params
 */
export const editCementSend = (params) => {
  return defHttp.put({ url: Api.editCementSend, params });
};

/**
 * Read with one click
 * @param params
 */
export const readAllMsg = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Confirm action',
    content: 'Whether to mark all as read?',
    okText: 'confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.put({ url: Api.readAllMsg, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * Sync messages
 * @param params
 */
export const syncNotic = (params) => {
  return defHttp.get({ url: Api.syncNotic, params });
};

/**
 * Send records based on messagesID获Cancel息内容
 * @param sendId
 */
export const getOne = (sendId) => {
  return defHttp.get({ url: Api.getOne, params:{sendId} });
};

