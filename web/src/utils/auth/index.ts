import { Persistent, BasicKeys } from '/@/utils/cache/persistent';
import { CacheTypeEnum } from '/@/enums/cacheEnum';
import projectSetting from '/@/settings/projectSetting';
import { TOKEN_KEY, LOGIN_INFO_KEY, TENANT_ID } from '/@/enums/cacheEnum';

const { permissionCacheType } = projectSetting;
const isLocal = permissionCacheType === CacheTypeEnum.LOCAL;

/**
 * Gettoken
 */
export function getToken() {
  return getAuthCache<string>(TOKEN_KEY);
}
/**
 * Get登录信息
 */
export function getLoginBackInfo() {
  return getAuthCache(LOGIN_INFO_KEY);
}
/**
 * Get租户id
 */
export function getTenantId() {
  return getAuthCache<string>(TENANT_ID);
}

export function getAuthCache<T>(key: BasicKeys) {
  const fn = isLocal ? Persistent.getLocal : Persistent.getSession;
  return fn(key) as T;
}

export function setAuthCache(key: BasicKeys, value) {
  const fn = isLocal ? Persistent.setLocal : Persistent.setSession;
  return fn(key, value, true);
}

/**
 * Set dynamicskey
 * @param key
 * @param value
 */
export function setCacheByDynKey(key, value) {
  const fn = isLocal ? Persistent.setLocal : Persistent.setSession;
  return fn(key, value, true);
}

/**
 * Get动态key
 * @param key
 */
export function getCacheByDynKey<T>(key) {
  const fn = isLocal ? Persistent.getLocal : Persistent.getSession;
  return fn(key) as T;
}

/**
 * Remove activitykey
 * @param key
 */
export function removeCacheByDynKey<T>(key) {
  const fn = isLocal ? Persistent.removeLocal : Persistent.removeSession;
  return fn(key) as T;
}
/**
 * Remove an attribute from cache
 * @param key
 * @update:Remove an attribute from cache
 * @updateBy:lsq
 * @updateDate:2021-09-07
 */
export function removeAuthCache<T>(key: BasicKeys) {
  const fn = isLocal ? Persistent.removeLocal : Persistent.removeSession;
  return fn(key) as T;
}

export function clearAuthCache(immediate = true) {
  const fn = isLocal ? Persistent.clearLocal : Persistent.clearSession;
  return fn(immediate);
}
