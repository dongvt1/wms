import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/test/jeecgOrderMain/list',
  delete = '/test/jeecgOrderMain/delete',
  orderCustomerList = '/test/jeecgOrderMain/queryOrderCustomerListByMainId',
  orderTicketList = '/test/jeecgOrderMain/queryOrderTicketListByMainId',
}

/**
 * List interface
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * Subform information
 * @param params
 */
export const orderTicketList = (params) => defHttp.get({ url: Api.orderTicketList, params });
/**
 * Subform information
 * @param params
 */
export const orderCustomerList = (params) => defHttp.get({ url: Api.orderCustomerList, params });
/**
 * Delete user
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
