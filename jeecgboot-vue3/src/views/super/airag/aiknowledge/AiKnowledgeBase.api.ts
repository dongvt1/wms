import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  //Knowledge Base Management
  list = '/airag/knowledge/list',
  save = '/airag/knowledge/add',
  delete = '/airag/knowledge/delete',
  queryById = '/airag/knowledge/queryById',
  edit = '/airag/knowledge/edit',
  rebuild = '/airag/knowledge/rebuild',
  //Knowledge Base Documents
  knowledgeDocList = '/airag/knowledge/doc/list',
  knowledgeEditDoc = '/airag/knowledge/doc/edit',
  knowledgeDeleteBatchDoc = '/airag/knowledge/doc/deleteBatch',
  knowledgeDeleteAllDoc = '/airag/knowledge/doc/deleteAll',
  knowledgeRebuildDoc = '/airag/knowledge/doc/rebuild',
  knowledgeEmbeddingHitTest = '/airag/knowledge/embedding/hitTest',
}

/**
 * Query knowledge base
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
};

/**
 * Query knowledge base by ID
 * @param params
 */
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
};

/**
 * Add knowledge base
 * @param params
 */
export const saveKnowledge = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * Edit knowledge base
 *
 * @param params
 */
export const editKnowledge = (params) => {
  return defHttp.put({ url: Api.edit, params });
};

/**
 * Delete knowledge base
 */
export const deleteModel = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Confirm Delete',
    content: 'Are you sure you want to delete the knowledge base named '+params.name+'?',
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
 * Query knowledge base details
 * @param params
 */
export const knowledgeDocList = (params) => {
  return defHttp.get({ url: Api.knowledgeDocList, params }, { isTransformResponse: false });
};

/**
 * Knowledge base vectorization
 *
 * @param params
 */
export const rebuild = (params) => {
  return defHttp.put({ url: Api.rebuild, params,timeout: 2 * 60 * 1000 }, { joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * Add knowledge base document
 * @param params
 */
export const knowledgeSaveDoc = (params) => {
  return defHttp.post({ url: Api.knowledgeEditDoc, params });
};

/**
 * Document vectorization
 * @param params
 */
export const knowledgeRebuildDoc = (params, handleSuccess) => {
  return defHttp.put({ url: Api.knowledgeRebuildDoc, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Batch delete documents
 *
 * @param params
 * @param handleSuccess
 */
export const knowledgeDeleteBatchDoc = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.knowledgeDeleteBatchDoc, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Delete all documents
 *
 * @param params
 * @param handleSuccess
 */
export const knowledgeDeleteAllDoc = (knowId: string, handleSuccess) => {
  return defHttp.delete({ url: Api.knowledgeDeleteAllDoc, params: {knowId} }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Hit test
 * @param params
 */
export const knowledgeEmbeddingHitTest = (params) => {
  let url = Api.knowledgeEmbeddingHitTest + '/' + params.knowId;
  return defHttp.get({ url: url, params }, { isTransformResponse: false });
};
