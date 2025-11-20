import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

export enum Api {
  //Knowledge base management
  list = '/airag/app/list',
  save = '/airag/app/edit',
  release = '/airag/app/release',
  delete = '/airag/app/delete',
  queryById = '/airag/app/queryById',
  queryBathById = '/airag/knowledge/query/batch/byId',
  queryFlowById = '/airag/flow/queryById',
  promptGenerate = '/airag/app/prompt/generate',
}

/**
 * Query application
 * @param params
 */
export const appList = (params) => {
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
};

/**
 * Query knowledge base
 * @param params
 */
export const queryKnowledgeBathById = (params) => {
  return defHttp.get({ url: Api.queryBathById, params }, { isTransformResponse: false });
};

/**
 * Query application by application ID
 * @param params
 */
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
};

/**
 * Add application
 * @param params
 */
export const saveApp = (params) => {
  return defHttp.put({ url: Api.save, params });
};

// Publish application
export function releaseApp(appId: string, release = false) {
  return defHttp.post({
    url: Api.release,
    params: {
      id: appId,
      release: release,
    }
  }, {joinParamsToUrl: true});
}

/**
 * Delete application
 * @param params
 * @param handleSuccess
 */
export const deleteApp = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Confirm Delete',
    content: 'Are you sure to delete the application named '+params.name+'?',
    okText: 'Confirm',
    cancelText: 'Cancel',
    onOk: () => {
      return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};


/**
 * Query flow by application ID
 * @param params
 */
export const queryFlowById = (params) => {
  return defHttp.get({ url: Api.queryFlowById, params }, { isTransformResponse: false });
};

/**
 * Application orchestration
 * @param params
 */
export const promptGenerate = (params) => {
  return defHttp.post(
    {
      url: Api.promptGenerate+'?prompt='+ params.prompt,
      adapter: 'fetch',
      responseType: 'stream',
      timeout: 5 * 60 * 1000,
    },
    {
      isTransformResponse: false,
    }
  );
};
