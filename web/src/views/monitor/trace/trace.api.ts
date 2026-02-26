import { defHttp } from '/@/utils/http/axios';

enum Api {
  actuatorList = '/actuator/jeecghttptrace/',
}

/**
 * Tracking information
 */
export const getActuatorList = (query: String, order: String) => {
  return defHttp.get({ url: Api.actuatorList + query + '/' + order }, { isTransformResponse: false });
};
