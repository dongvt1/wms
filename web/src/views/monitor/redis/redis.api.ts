import { defHttp } from '/@/utils/http/axios';

enum Api {
  keysSize = '/sys/actuator/redis/keysSize',
  memoryInfo = '/sys/actuator/redis/memoryInfo',
  info = '/sys/actuator/redis/info',
  metricsHistory = '/sys/actuator/redis/metrics/history',
}

/**
 * keynumber
 */
export const getKeysSize = () => {
  return defHttp.get({ url: Api.keysSize }, { isTransformResponse: false });
};

/**
 * memory information
 */
export const getMemoryInfo = () => {
  return defHttp.get({ url: Api.memoryInfo }, { isTransformResponse: false });
};

/**
 * Details
 */
export const getInfo = () => {
  return defHttp.get({ url: Api.info });
};

/**
 * Historical monitoring records
 */
export const getMetricsHistory = () => {
  return defHttp.get({ url: Api.metricsHistory });
};

export const getRedisInfo = () => {
  return Promise.all([getKeysSize(), getMemoryInfo()]);
};
