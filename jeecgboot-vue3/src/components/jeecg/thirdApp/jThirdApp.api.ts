import { defHttp } from '/@/utils/http/axios';
import { cloneObject } from '/@/utils/index';

export const backEndUrl = {
  // Get enabled third partiesApp
  getEnabledType: '/sys/thirdApp/getEnabledType',
  // Enterprise WeChat
  wechatEnterprise: {
    user: '/sys/thirdApp/sync/wechatEnterprise/user',
    depart: '/sys/thirdApp/sync/wechatEnterprise/depart',
  },
  // DingTalk
  dingtalk: {
    user: '/sys/thirdApp/sync/dingtalk/user',
    depart: '/sys/thirdApp/sync/dingtalk/depart',
  },
};
// Which third parties are enabledApp（Cache here）
let enabledTypes = null;

// Get enabled third partiesApp
export const getEnabledTypes = async () => {
  // Get cache
  if (enabledTypes != null) {
    return cloneObject(enabledTypes);
  } else {
    let { success, result } = await defHttp.get({ url: backEndUrl.getEnabledType }, { isTransformResponse: false });
    if (success) {
      // Cache here
      enabledTypes = cloneObject(result);
      return result;
    } else {
      console.warn('getEnabledTypeQuery failed：');
    }
  }
  return {};
};
