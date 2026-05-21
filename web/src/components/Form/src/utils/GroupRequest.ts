import { getAuthCache, setAuthCache } from '/@/utils/auth';
/**
 * Group a request
 *
 * @param getPromise Pass in one to getPromiseobject methods
 * @param groupId GroupID，如果不传或者为空则不Group
 * @param expire Expiration time，default half a minute
 */
export function httpGroupRequest(getPromise, groupId, expire = 1000 * 30) {
  if (groupId == null || groupId === '') {
    console.log('--------popup----------getFrom  DB-------with---no--groupId ');
    return getPromise();
  }

  if (getAuthCache(groupId)) {
    console.log('---------popup--------getFrom  Cache--------groupId = ' + groupId);
    return Promise.resolve(getAuthCache(groupId));
  } else {
    console.log('--------popup----------getFrom  DB---------groupId = ' + groupId);
  }

  // No request has been made yet，Just make the first request
  return getPromise().then((res) => {
    setAuthCache(groupId, res);
    return Promise.resolve(res);
  });
}
