import { defHttp } from '/@/utils/http/axios';

const BASE_URL = '/warehouse/bom';

export const bomApi = {
  list: (params?: any) => defHttp.get({ url: `${BASE_URL}/list`, params }),
  add: (params: any) => defHttp.post({ url: `${BASE_URL}/add`, params }),
  edit: (params: any) => defHttp.put({ url: `${BASE_URL}/edit`, params }),
  delete: (params: { id: string }) => defHttp.delete({ url: `${BASE_URL}/delete`, params }),
  deleteBatch: (params: { ids: string }) => defHttp.delete({ url: `${BASE_URL}/deleteBatch`, params }),
  queryById: (params: { id: string }) => defHttp.get({ url: `${BASE_URL}/queryById`, params }),
  getItems: (bomId: string) => defHttp.get({ url: `${BASE_URL}/getItems`, params: { bomId } }),
  getByProductId: (productId: string) => defHttp.get({ url: `${BASE_URL}/getByProductId`, params: { productId } }),
  listActive: () => defHttp.get({ url: `${BASE_URL}/listActive` }),
};
