import { defHttp } from '/@/utils/http/axios';

const BASE = '/warehouse/qms/stage';

export const qcStageApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE}/list`, params }),
  listActive: () => defHttp.get({ url: `${BASE}/listActive` }),
  add: (data: any) => defHttp.post({ url: `${BASE}/add`, params: data }),
  edit: (data: any) => defHttp.put({ url: `${BASE}/edit`, params: data }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE}/delete`, params }),
  queryById: (id: string) => defHttp.get({ url: `${BASE}/queryById`, params: { id } }),
  getParams: (stageId: string) => defHttp.get({ url: `${BASE}/getParams`, params: { stageId } }),
};
