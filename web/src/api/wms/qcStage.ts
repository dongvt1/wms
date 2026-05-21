import { defHttp } from '/@/utils/http/axios';
import { PageResult, PageParams } from './types';

const BASE = '/wms/qc/stage';

export interface QcStageModel {
  id?: string;
  stageCode?: string;
  stageName?: string;
  description?: string;
  inspectionType?: string;
  status?: string;
  createTime?: string;
}

export const wmsQcStageApi = {
  list: (params?: PageParams) => defHttp.get<PageResult<QcStageModel>>({ url: `${BASE}/list`, params }),
  queryById: (id: string) => defHttp.get<QcStageModel>({ url: `${BASE}/queryById`, params: { id } }),
  add: (data: any) => defHttp.post({ url: `${BASE}/add`, params: data }),
  edit: (data: any) => defHttp.put({ url: `${BASE}/edit`, params: data }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  listActive: () => defHttp.get<QcStageModel[]>({ url: `${BASE}/listActive` }),
  getParams: (stageId: string) => defHttp.get({ url: `${BASE}/getParams`, params: { stageId } }),
};
