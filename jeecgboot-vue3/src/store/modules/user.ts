import type { UserInfo, LoginInfo } from '/#/store';
import type { ErrorMessageMode } from '/#/axios';
import { defineStore } from 'pinia';
import { store } from '/@/store';
import { RoleEnum } from '/@/enums/roleEnum';
import { PageEnum } from '/@/enums/pageEnum';
import { ROLES_KEY, TOKEN_KEY, USER_INFO_KEY, LOGIN_INFO_KEY, DB_DICT_DATA_KEY, TENANT_ID, OAUTH2_THIRD_LOGIN_TENANT_ID } from '/@/enums/cacheEnum';
import { getAuthCache, setAuthCache, removeAuthCache } from '/@/utils/auth';
import { GetUserInfoModel, LoginParams, ThirdLoginParams } from '/@/api/sys/model/userModel';
import { doLogout, getUserInfo, loginApi, phoneLoginApi, thirdLogin } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { router } from '/@/router';
import { usePermissionStore } from '/@/store/modules/permission';
import { RouteRecordRaw } from 'vue-router';
import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { isArray } from '/@/utils/is';
import { useGlobSetting } from '/@/hooks/setting';
import { JDragConfigEnum } from '/@/enums/jeecgEnum';
import { useSso } from '/@/hooks/web/useSso';
import { isOAuth2AppEnv } from "/@/views/sys/login/useLogin";
import { getUrlParam } from "@/utils";
interface dictType {
  [key: string]: any;
}
interface UserState {
  userInfo: Nullable<UserInfo>;
  token?: string;
  roleList: RoleEnum[];
  dictItems?: dictType | null;
  sessionTimeout?: boolean;
  lastUpdateTime: number;
  tenantid?: string | number;
  shareTenantId?: Nullable<string | number>;
  loginInfo?: Nullable<LoginInfo>;
}

export const useUserStore = defineStore({
  id: 'app-user',
  state: (): UserState => ({
    // User information
    userInfo: null,
    // token
    token: undefined,
    // role list
    roleList: [],
    // dictionary
    dictItems: null,
    // sessionExpiration time
    sessionTimeout: false,
    // Last fetch time
    lastUpdateTime: 0,
    //tenantid
    tenantid: '',
    // 分享tenantID
    // 用于分享页面所属tenant与当前用户登录tenant不一致的情况
    shareTenantId: null,
    //Login return information
    loginInfo: null,
  }),
  getters: {
    getUserInfo(): UserInfo {
      if(this.userInfo == null){
        this.userInfo = getAuthCache<UserInfo>(USER_INFO_KEY)!=null ? getAuthCache<UserInfo>(USER_INFO_KEY) : null;
      }
      return this.userInfo || getAuthCache<UserInfo>(USER_INFO_KEY) || {};
    },
    getLoginInfo(): LoginInfo {
      return this.loginInfo || getAuthCache<LoginInfo>(LOGIN_INFO_KEY) || {};
    },
    getToken(): string {
      return this.token || getAuthCache<string>(TOKEN_KEY);
    },
    getAllDictItems(): [] {
      return this.dictItems || getAuthCache(DB_DICT_DATA_KEY);
    },
    getRoleList(): RoleEnum[] {
      return this.roleList.length > 0 ? this.roleList : getAuthCache<RoleEnum[]>(ROLES_KEY);
    },
    getSessionTimeout(): boolean {
      return !!this.sessionTimeout;
    },
    getLastUpdateTime(): number {
      return this.lastUpdateTime;
    },
    getTenant(): string | number {
      return this.tenantid || getAuthCache<string | number>(TENANT_ID);
    },
    // 是否有分享tenantid
    hasShareTenantId(): boolean {
      return this.shareTenantId != null && this.shareTenantId !== '';
    },
  },
  actions: {
    setToken(info: string | undefined) {
      this.token = info ? info : ''; // for null or undefined value
      setAuthCache(TOKEN_KEY, info);
    },
    setRoleList(roleList: RoleEnum[]) {
      this.roleList = roleList;
      setAuthCache(ROLES_KEY, roleList);
    },
    setUserInfo(info: UserInfo | null) {
      this.userInfo = info;
      this.lastUpdateTime = new Date().getTime();
      setAuthCache(USER_INFO_KEY, info);
    },
    setLoginInfo(info: LoginInfo | null) {
      this.loginInfo = info;
      setAuthCache(LOGIN_INFO_KEY, info);
    },
    setAllDictItems(dictItems) {
      this.dictItems = dictItems;
      setAuthCache(DB_DICT_DATA_KEY, dictItems);
    },
    setAllDictItemsByLocal() {
      // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8572】Table row selection stuck problem（customRender中dictionary引起的）
      if (!this.dictItems) {
        const allDictItems = getAuthCache(DB_DICT_DATA_KEY);
        if (allDictItems) {
          this.dictItems = allDictItems;
        }
      }
      // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-8572】Table row selection stuck problem（customRender中dictionary引起的）
    },
    setTenant(id) {
      this.tenantid = id;
      setAuthCache(TENANT_ID, id);
    },
    setShareTenantId(id: NonNullable<typeof this.shareTenantId>) {
      this.shareTenantId = id;
    },
    setSessionTimeout(flag: boolean) {
      this.sessionTimeout = flag;
    },
    resetState() {
      this.userInfo = null;
      this.dictItems = null;
      this.token = '';
      this.roleList = [];
      this.sessionTimeout = false;
    },
    /**
     * Login event
     */
    async login(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await loginApi(loginParams, mode);
        const { token, userInfo } = data;
        // save token
        this.setToken(token);
        this.setTenant(userInfo.loginTenantId);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * 扫码Login event
     */
    async qrCodeLogin(token): Promise<GetUserInfoModel | null> {
      try {
        // save token
        this.setToken(token);
        return this.afterLoginAction(true, {});
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * Login complete processing
     * @param goHome
     */
    async afterLoginAction(goHome?: boolean, data?: any): Promise<any | null> {
      if (!this.getToken) return null;
      //GetUser information
      const userInfo = await this.getUserInfoAction();
      const sessionTimeout = this.sessionTimeout;
      if (sessionTimeout) {
        this.setSessionTimeout(false);
      } else {
        //update-begin---author:scott ---date::2024-02-21  for：【QQYUN-8326】Login does not require building routes，Enter the home page and there is construction---
        // // Build background menu routing
        // const permissionStore = usePermissionStore();
        // if (!permissionStore.isDynamicAddedRoute) {
        //   const routes = await permissionStore.buildRoutesAction();
        //   routes.forEach((route) => {
        //     router.addRoute(route as unknown as RouteRecordRaw);
        //   });
        //   router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
        //   permissionStore.setDynamicAddedRoute(true);
        // }
        //update-end---author:scott ---date::2024-02-21  for：【QQYUN-8326】Login does not require building routes，Enter the home page and there is construction---
        
        await this.setLoginInfo({ ...data, isLogin: true });
        //update-begin-author:liusq date:2022-5-5 for:After successful login, cache the interface prefix of the drag and drop module.
        localStorage.setItem(JDragConfigEnum.DRAG_BASE_URL, useGlobSetting().domainUrl);
        //update-end-author:liusq date:2022-5-5 for: After successful login, cache the interface prefix of the drag and drop module.

        // update-begin-author:sunjianlei date:20230306 for: After successful login repair，Problem with not redirecting correctly
        let redirect = router.currentRoute.value?.query?.redirect as string;
        // Determine whether there is redirect redirect address
        //update-begin---author:wangshuai ---date:20230424  for：【QQYUN-5195】Refreshing the page directly after logging in results in not entering the organization creation page.------------
        if (redirect && goHome) {
        //update-end---author:wangshuai ---date:20230424  for：【QQYUN-5195】Refreshing the page directly after logging in results in not entering the organization creation page.------------
          // update-begin--author:liaozhiyang---date:20250407---for：【issues/8034】hashThe default jump address is abnormal when logging out and logging back in mode.
          // router.options.history.baseCan replace the previous onepublicPath
          // The current page is open
          window.open(`${router.options.history.base}${redirect}`, '_self');
          // update-end--author:liaozhiyang---date:20250407---for：【issues/8034】hashThe default jump address is abnormal when logging out and logging back in mode.
          return data;
        }
        // update-end-author:sunjianlei date:20230306 for: After successful login repair，Problem with not redirecting correctly

        //update-begin---author:wangshuai---date:2024-04-03---for:【issues/1102】Set up single sign-on post page，Tips for entering the homepage404，Also the sidebar is not drawn #1102---
        let ticket = getUrlParam('ticket');
        if(ticket){
          goHome && (window.location.replace((userInfo && userInfo.homePath) || PageEnum.BASE_HOME));
        }else{
          goHome && (await router.replace((userInfo && userInfo.homePath) || PageEnum.BASE_HOME));
        }
        //update-end---author:wangshuai---date:2024-04-03---for:【issues/1102】Set up single sign-on post page，Tips for entering the homepage404，Also the sidebar is not drawn #1102---
      }
      return data;
    },
    /**
     * Mobile phone number login
     * @param params
     */
    async phoneLogin(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await phoneLoginApi(loginParams, mode);
        //update-begin---author:wangshuai---date:2024-11-25---for:【issues/7488】Mobile phone number login，在请求头中无法Gettenantid---
        const { token , userInfo } = data;
        this.setTenant(userInfo!.loginTenantId);
        //update-end---author:wangshuai---date:2024-11-25---for:【issues/7488】Mobile phone number login，在请求头中无法Gettenantid---
        // save token
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * GetUser information
     */
    async getUserInfoAction(): Promise<UserInfo | null> {
      if (!this.getToken) {
        return null;
      }
      const { userInfo, sysAllDictItems } = await getUserInfo();
      if (userInfo) {
        const { roles = [] } = userInfo;
        if (isArray(roles)) {
          const roleList = roles.map((item) => item.value) as RoleEnum[];
          this.setRoleList(roleList);
        } else {
          userInfo.roles = [];
          this.setRoleList([]);
        }
        this.setUserInfo(userInfo);
      }
      /**
       * 添加dictionary信息到缓存
       * @updateBy:lsq
       * @updateDate:2021-09-08
       */
      if (sysAllDictItems) {
        this.setAllDictItems(sysAllDictItems);
      }
      return userInfo;
    },
    /**
     * Log out
     */
    async logout(goLogin = false) {
      if (this.getToken) {
        try {
          await doLogout();
        } catch {
          console.log('Log outTokenfail');
        }
      }

      // //update-begin-author:taoyan date:2022-5-5 for: src/layouts/default/header/index.vue showLoginSelectmethod GettenantId Log out后再次登录依然能Get到值，Not cleared
      // let username:any = this.userInfo && this.userInfo.username;
      // if(username){
      //   removeAuthCache(username)
      // }
      // //update-end-author:taoyan date:2022-5-5 for: src/layouts/default/header/index.vue showLoginSelectmethod GettenantId Log out后再次登录依然能Get到值，Not cleared

      this.setToken('');
      setAuthCache(TOKEN_KEY, null);
      this.setSessionTimeout(false);
      this.setUserInfo(null);
      this.setLoginInfo(null);
      this.setTenant(null);
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-23】Log out后会提示「Tokenaging，Please log in again」
      setTimeout(() => {
        this.setAllDictItems(null);
      }, 1e3);
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-23】Log out后会提示「Tokenaging，Please log in again」
      //update-begin-author:liusq date:2022-5-5 for:Log out后清除拖拽模块的接口前缀
      localStorage.removeItem(JDragConfigEnum.DRAG_BASE_URL);
      //update-end-author:liusq date:2022-5-5 for: Log out后清除拖拽模块的接口前缀

      //If single sign-on is enabled,then jump to the single unified login center
      const openSso = useGlobSetting().openSso;
      if (openSso == 'true') {
        await useSso().ssoLoginOut();
      }
      //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，通过tenant模式隔离------------
      //Log out的时候需要用的应用id
      if(isOAuth2AppEnv()){
        let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID);
        removeAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID);
        goLogin && await router.push({ name:"Login",query:{ tenantId:tenantId }})
      }else{
        // update-begin-author:sunjianlei date:20230306 for: After successful login repair，Problem with not redirecting correctly
        goLogin && (await router.push({
          path: PageEnum.BASE_LOGIN,
          query: {
            // Pass in the current route，After successful login, jump to the current route
            redirect: router.currentRoute.value.fullPath,
          }
        }));
        // update-end-author:sunjianlei date:20230306 for: After successful login repair，Problem with not redirecting correctly

      }
      //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，通过tenant模式隔离------------
    },
    /**
     * Login event
     */
    async ThirdLogin(
      params: ThirdLoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<any | null> {
      try {
        const { goHome = true, mode, ...ThirdLoginParams } = params;
        const data = await thirdLogin(ThirdLoginParams, mode);
        //update-begin---author:wangshuai---date:2024-07-01---for:【issues/6652】开启tenant数据隔离，接入钉钉后登录默认tenant为0Got it---
        const { token, userInfo } = data;
        this.setTenant(userInfo?.loginTenantId);
        //update-end---author:wangshuai---date:2024-07-01---for:【issues/6652】开启tenant数据隔离，接入钉钉后登录默认tenant为0Got it---
        // save token
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * Exit query
     */
    confirmLoginOut() {
      const { createConfirm } = useMessage();
      const { t } = useI18n();
      createConfirm({
        iconType: 'warning',
        title: t('sys.app.logoutTip'),
        content: t('sys.app.logoutMessage'),
        onOk: async () => {
          await this.logout(true);
        },
      });
    },
  },
});

// Need to be used outside the setup
export function useUserStoreWithOut() {
  return useUserStore(store);
}
