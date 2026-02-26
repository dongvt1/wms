/* JVxeTable line edit Permissions */
import { usePermissionStoreWithOut } from '/@/store/modules/permission';

/**
 * JVxe dedicated，获取Permissions
 * @param prefix
 */
export function getJVxeAuths(prefix) {
  const permissionStore = usePermissionStoreWithOut();
  prefix = getPrefix(prefix);
  let { authList, allAuthList } = permissionStore;
  let authsMap = new Map<string, typeof allAuthList[0]>();
  if (!prefix || prefix.length == 0) {
    return authsMap;
  }
  // will allvxe用到的Permissions取出来
  for (let auth of allAuthList) {
    if (auth.status == '1' && (auth.action || '').startsWith(prefix)) {
      authsMap.set(auth.action, { ...auth, isAuth: false });
    }
  }
  // Set whether the setting is authorized
  for (let auth of authList) {
    let getAuth = authsMap.get(auth.action);
    if (getAuth != null) {
      getAuth.isAuth = true;
    }
  }
  //update-begin-author:taoyan date:2022-6-1 for:  VUEN-1162 The sub-watch button has no control
  let onlineButtonAuths = permissionStore.getOnlineSubTableAuth(prefix);
  if (onlineButtonAuths && onlineButtonAuths.length > 0) {
    for (let auth of onlineButtonAuths) {
      authsMap.set(prefix + 'btn:' + auth, { action: auth, type: 1, status: 1, isAuth: false });
    }
  }
  //update-end-author:taoyan date:2022-6-1 for:  VUEN-1162 The sub-watch button has no control
  return authsMap;
}

/**
 * Get prefix
 * @param prefix
 */
export function getPrefix(prefix: string) {
  if (prefix && !prefix.endsWith(':')) {
    return prefix + ':';
  }
  return prefix;
}
