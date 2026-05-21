import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

export enum Api {
  list = '/airag/ocr/list',
  add = '/airag/ocr/add',
  edit = '/airag/ocr/edit',
  deleteById = '/airag/ocr/deleteById',
  flowRun = '/airag/flow/run',
}

/**
 * Query OCR list
 *
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Add OCR
 * @param params
 * @param handleSuccess
 */
export const addOcr = (params) => {
  return defHttp.post({ url: Api.add, params });
};

/**
 * Edit OCR
 * @param params
 * @param handleSuccess
 */
export const editOcr = (params) => {
  return defHttp.put({ url: Api.edit, params });
};

/**
 * Delete OCR by ID
 * @param params
 * @param handleSuccess
 */
export const deleteOcrById = (params) => {
  return defHttp.delete({ url: Api.deleteById, params });
};
