import { defHttp } from '/@/utils/http/axios';
import { getMenuListResultModel } from './model/menuModel';
import { useUserStoreWithOut } from '@/store/modules/user';
import { setAuthCache } from '@/utils/auth';
import { TOKEN_KEY } from '@/enums/cacheEnum';
import { router } from '@/router';
import { PageEnum } from '@/enums/pageEnum';

enum Api {
  GetMenuList = '/sys/permission/getUserPermissionByToken',
  // 【QQYUN-8487】
  // SwitchVue3Menu = '/sys/switchVue3Menu',
}

/**
 * @description: Get user menu based on id
 */

export const getMenuList = () => {
  return new Promise((resolve) => {
    //For compatibilitymockand interface data
    defHttp.get<getMenuListResultModel>({ url: Api.GetMenuList }).then((res) => {
      if (Array.isArray(res)) {
        resolve(res);
      } else {
        resolve(res['menu']);
      }
    });
  });
};

/**
 * @description: Get background menu permissions and button permissions
 */
export function getBackMenuAndPerms() {
  return defHttp.get({ url: Api.GetMenuList }).catch((e) => {
    // TokenExpired，Jump directly to the login page 2025-09-08 scott
    if (e && (e.message.includes('timeout') || e.message.includes('401'))) {
      const userStore = useUserStoreWithOut();
      userStore.setToken('');
      setAuthCache(TOKEN_KEY, null);
      router.push({
        path: PageEnum.BASE_LOGIN,
        query: {
          // Pass in the current route，After successful login, jump to the current route
          redirect: router.currentRoute.value.fullPath,
        }
      });
    }
  });
}

/**
 * switch tovue3menu
 */
 // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8487】注释掉判断menu是否vue2Version logic code
// export const switchVue3Menu = () => {
//   return new Promise((resolve) => {
//     defHttp.get({ url: Api.SwitchVue3Menu });
//   });
// };
// update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8487】注释掉判断menu是否vue2Version logic code
