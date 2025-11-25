import type { AppRouteRecordRaw, Menu } from '/@/router/types';

import { defineStore } from 'pinia';
import { store } from '/@/store';
import { useI18n } from '/@/hooks/web/useI18n';
import { useUserStore } from './user';
import { useAppStoreWithOut } from './app';
import { toRaw } from 'vue';
import { transformObjToRoute, flatMultiLevelRoutes, addSlashToRouteComponent } from '/@/router/helper/routeHelper';
import { transformRouteToMenu } from '/@/router/helper/menuHelper';

import projectSetting from '/@/settings/projectSetting';

import { PermissionModeEnum } from '/@/enums/appEnum';

import { asyncRoutes } from '/@/router/routes';
import { ERROR_LOG_ROUTE, PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { staticRoutesList } from '../../router/routes/staticRouter';

import { filter } from '/@/utils/helper/treeHelper';

import { getBackMenuAndPerms } from '/@/api/sys/menu';

import { useMessage } from '/@/hooks/web/useMessage';
import { PageEnum } from '/@/enums/pageEnum';

// System permissions
interface AuthItem {
  // Menu permission encoding，For example：“sys:schedule:list,sys:schedule:info”,Multiple commas separated
  action: string;
  // Permission policy1show2Disable
  type: string | number;
  // permission status(0invalid1efficient)
  status: string | number;
  // Permission name
  describe?: string;
  isAuth?: boolean;
}

interface PermissionState {
  // Permission code list
  permCodeList: string[] | number[];
  // Whether the route has been dynamically added
  isDynamicAddedRoute: boolean;
  // To trigger a menu update
  lastBuildMenuTime: number;
  // Backstage menu list
  backMenuList: Menu[];
  frontMenuList: Menu[];
  // Permissions the user has
  authList: AuthItem[];
  // All permissions configuration
  allAuthList: AuthItem[];
  // System safe mode
  sysSafeMode: boolean;
  // onlineSubtable button permissions
  onlineSubTableAuthMap: object;
}
export const usePermissionStore = defineStore({
  id: 'app-permission',
  state: (): PermissionState => ({
    permCodeList: [],
    // Whether the route has been dynamically added
    isDynamicAddedRoute: false,
    // To trigger a menu update
    lastBuildMenuTime: 0,
    // Backstage menu list
    backMenuList: [],
    // menu List
    frontMenuList: [],
    authList: [],
    allAuthList: [],
    sysSafeMode: false,
    onlineSubTableAuthMap: {},
  }),
  getters: {
    getPermCodeList(): string[] | number[] {
      return this.permCodeList;
    },
    getBackMenuList(): Menu[] {
      return this.backMenuList;
    },
    getFrontMenuList(): Menu[] {
      return this.frontMenuList;
    },
    getLastBuildMenuTime(): number {
      return this.lastBuildMenuTime;
    },
    getIsDynamicAddedRoute(): boolean {
      return this.isDynamicAddedRoute;
    },

    //update-begin-author:taoyan date:2022-6-1 for: VUEN-1162 The sub-watch button has no control
    getOnlineSubTableAuth: (state) => {
      return (code) => state.onlineSubTableAuthMap[code];
    },
    //update-end-author:taoyan date:2022-6-1 for: VUEN-1162 The sub-watch button has no control
  },
  actions: {
    setPermCodeList(codeList: string[]) {
      this.permCodeList = codeList;
    },

    setBackMenuList(list: Menu[]) {
      this.backMenuList = list;
      list?.length > 0 && this.setLastBuildMenuTime();
    },

    setFrontMenuList(list: Menu[]) {
      this.frontMenuList = list;
    },

    setLastBuildMenuTime() {
      this.lastBuildMenuTime = new Date().getTime();
    },

    setDynamicAddedRoute(added: boolean) {
      this.isDynamicAddedRoute = added;
    },
    resetState(): void {
      this.isDynamicAddedRoute = false;
      this.permCodeList = [];
      this.backMenuList = [];
      this.lastBuildMenuTime = 0;
    },
    async changePermissionCode() {
      const systemPermission = await getBackMenuAndPerms();
      const codeList = systemPermission.codeList;
      this.setPermCodeList(codeList);
      this.setAuthData(systemPermission);
      
      //menu routing
      const routeList = systemPermission.menu;
      return routeList;
    },
    async buildRoutesAction(): Promise<AppRouteRecordRaw[]> {
      const { t } = useI18n();
      const userStore = useUserStore();
      const appStore = useAppStoreWithOut();

      let routes: AppRouteRecordRaw[] = [];
      const roleList = toRaw(userStore.getRoleList) || [];
      const { permissionMode = projectSetting.permissionMode } = appStore.getProjectConfig;

      const routeFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { roles } = meta || {};
        if (!roles) return true;
        return roleList.some((role) => roles.includes(role));
      };

      const routeRemoveIgnoreFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { ignoreRoute } = meta || {};
        return !ignoreRoute;
      };

      /**
       * @description Home page according to settingspath，Correctionroutesinaffixmark（Fixed homepage）
       * */
      const patchHomeAffix = (routes: AppRouteRecordRaw[]) => {
        if (!routes || routes.length === 0) return;
        let homePath: string = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        function patcher(routes: AppRouteRecordRaw[], parentPath = '') {
          if (parentPath) parentPath = parentPath + '/';
          routes.forEach((route: AppRouteRecordRaw) => {
            const { path, children, redirect } = route;
            const currentPath = path.startsWith('/') ? path : parentPath + path;
            if (currentPath === homePath) {
              if (redirect) {
                homePath = route.redirect! as string;
              } else {
                route.meta = Object.assign({}, route.meta, { affix: true });
                throw new Error('end');
              }
            }
            children && children.length > 0 && patcher(children, currentPath);
          });
        }
        try {
          patcher(routes);
        } catch (e) {
          // Processed and exited the loop
        }
        return;
      };

      switch (permissionMode) {
        case PermissionModeEnum.ROLE:
          routes = filter(asyncRoutes, routeFilter);
          routes = routes.filter(routeFilter);
          //  Convert multi-level routing to level two
          routes = flatMultiLevelRoutes(routes);
          break;

        case PermissionModeEnum.ROUTE_MAPPING:
          routes = filter(asyncRoutes, routeFilter);
          routes = routes.filter(routeFilter);
          const menuList = transformRouteToMenu(routes, true);
          routes = filter(routes, routeRemoveIgnoreFilter);
          routes = routes.filter(routeRemoveIgnoreFilter);
          menuList.sort((a, b) => {
            return (a.meta?.orderNo || 0) - (b.meta?.orderNo || 0);
          });

          this.setFrontMenuList(menuList);
          // Convert multi-level routing to level two
          routes = flatMultiLevelRoutes(routes);
          break;

        // Backend menu construction
        case PermissionModeEnum.BACK:
          const { createMessage, createWarningModal } = useMessage();
          console.log(" --- Build background routing menu --- ")
          // Menu loading prompt
          // createMessage.loading({
          //   content: t('sys.app.menuLoading'),
          //   duration: 1,
          // });

          // Get the permission code from the background，
          // This function may only need to be executed once，and actual items can be placed at the right time
          let routeList: AppRouteRecordRaw[] = [];
          try {
            routeList = await this.changePermissionCode();
            //routeList = (await getMenuList()) as AppRouteRecordRaw[];
            // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8487】Comment out the judgment menu whethervue2Version logic code
            // update-begin----author:sunjianlei---date:20220315------for: Determine whether it is vue3 version menu ---
            // let hasIndex: boolean = false;
            // let hasIcon: boolean = false;
            // for (let menuItem of routeList) {
            //   // condition1：Determine whether the component is layouts/default/index
            //   if (!hasIndex) {
            //     hasIndex = menuItem.component === 'layouts/default/index';
            //   }
            //   // condition2：Determine whether the icon has colon
            //   if (!hasIcon) {
            //     hasIcon = !!menuItem.meta?.icon?.includes(':');
            //   }
            //   // 满足任何一个condition都直接跳出循环
            //   if (hasIcon || hasIndex) {
            //     break;
            //   }
            // }
            // // 两个condition都不满足，A prompt box pops up
            // if (!hasIcon && !hasIndex) {
            //   // Delay1.5The prompt will appear again after seconds，Otherwise, the prompt box will not appear.
            //   setTimeout(
            //     () =>
            //       createWarningModal({
            //         title: 'Detection tips',
            //         content:
            //           'The current menu table is <b>Vue2Version</b>，Causes menu loading exception!<br>Click to confirm，switch toVue3version menu！',
            //         onOk:function () {
            //           switchVue3Menu();
            //           location.reload();
            //         }
            //       }),
            //     100
            //   );
            // }
            // update-end----author:sunjianlei---date:20220315------for: Determine whether it is vue3 version menu ---
            // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8487】Comment out the judgment menu whethervue2Version logic code
          } catch (error) {
            console.error(error);
          }
          // Add a slash before the component address  author: lsq date:2021-09-08
          routeList = addSlashToRouteComponent(routeList);
          // Dynamically introduce components
          routeList = transformObjToRoute(routeList);

          // Build background routing menu
          const backMenuList = transformRouteToMenu(routeList);
          this.setBackMenuList(backMenuList);

          // deletemeta.ignoreRouteitem
          routeList = filter(routeList, routeRemoveIgnoreFilter);
          routeList = routeList.filter(routeRemoveIgnoreFilter);

          routeList = flatMultiLevelRoutes(routeList);
          // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-522】aiThe assistant routing is hard-coded on the front end
          routes = [PAGE_NOT_FOUND_ROUTE, ...routeList, ...staticRoutesList];
          // update-end--author:liaozhiyang---date:20240529---for：【TV360X-522】aiThe assistant routing is hard-coded on the front end
          break;
      }

      routes.push(ERROR_LOG_ROUTE);
      patchHomeAffix(routes);
      return routes;
    },
    setAuthData(systemPermission) {
      this.authList = systemPermission.auth;
      this.allAuthList = systemPermission.allAuth;
      this.sysSafeMode = systemPermission.sysSafeMode;
    },
    setAuthList(authList: AuthItem[]) {
      this.authList = authList;
    },
    setAllAuthList(authList: AuthItem[]) {
      this.allAuthList = authList;
    },

    //update-begin-author:taoyan date:2022-6-1 for: VUEN-1162 The sub-watch button has no control
    setOnlineSubTableAuth(code, hideBtnList) {
      this.onlineSubTableAuthMap[code] = hideBtnList;
    },
    //update-end-author:taoyan date:2022-6-1 for: VUEN-1162 The sub-watch button has no control
  },
});

// Need to be used outside of settings
export function usePermissionStoreWithOut() {
  return usePermissionStore(store);
}
