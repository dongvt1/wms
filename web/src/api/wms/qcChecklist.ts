import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/qc/checklist';

export interface QcChecklistModel {
  id?: string;
  templateCode?: string;
  templateName?: string;
  inspectionType?: string;
  description?: string;
  status?: string;
  createTime?: string;
}

export const wmsQcChecklistApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<QcChecklistModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<QcChecklistModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (params: any) => defHttp.post({ url: `${BASE}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE}/deleteBatch`, params }),
  getItems: (templateId: string) => defHttp.get({ url: `${BASE}/getItems`, params: { templateId } }),
  listActive: (inspectionType?: string) => defHttp.get<QcChecklistModel[]>({ url: `${BASE}/listActive`, params: { inspectionType } }),
};
