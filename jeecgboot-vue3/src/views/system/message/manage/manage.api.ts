import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

export enum Api {
  list = '/sys/message/sysMessage/list',
  delete = '/sys/message/sysMessage/delete',
  deleteBatch = '/sys/message/sysMessage/deleteBatch',
  exportXls = 'sys/message/sysMessage/exportXls',
  importXls = 'sys/message/sysMessage/importExcel',
  save = '/sys/message/sysMessage/add',
  edit = '/sys/message/sysMessage/edit',
}

export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Batch delete
 * @param params
 * @param confirm
 */
export const deleteBatch = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: 'delete',
        content: '确定要delete吗？',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * Save or change message templates
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (unref(isUpdate)) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};
