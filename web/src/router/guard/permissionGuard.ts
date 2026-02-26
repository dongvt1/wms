import type { Router, RouteRecordRaw } from 'vue-router';

import { usePermissionStoreWithOut } from '/@/store/modules/permission';

import { PageEnum } from '/@/enums/pageEnum';
import { useUserStoreWithOut } from '/@/store/modules/user';

import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';

import { RootRoute } from '/@/router/routes';

import {isOAuth2AppEnv, isOAuth2DingAppEnv} from '/@/views/sys/login/useLogin';
import { OAUTH2_THIRD_LOGIN_TENANT_ID } from "/@/enums/cacheEnum";
import { setAuthCache } from "/@/utils/auth";
import { PAGE_NOT_FOUND_NAME_404 } from '/@/router/constant';

const LOGIN_PATH = PageEnum.BASE_LOGIN;
//auth2Login routing
const OAUTH2_LOGIN_PAGE_PATH = PageEnum.OAUTH2_LOGIN_PAGE_PATH;

//分享免Login routing
const SYS_FILES_PATH = PageEnum.SYS_FILES_PATH;

// Jump address in email,Corresponding to this route,carrytokenGo directly to the application page without logging in
const TOKEN_LOGIN = PageEnum.TOKEN_LOGIN;

const ROOT_PATH = RootRoute.path;

//update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3Not supportedauth2Log in------------
//update-begin---author:wangshuai ---date:20221111  for: [VUEN-2472]分享免Log in------------
const whitePathList: PageEnum[] = [LOGIN_PATH, OAUTH2_LOGIN_PAGE_PATH,SYS_FILES_PATH, TOKEN_LOGIN ];
//update-end---author:wangshuai ---date:20221111  for: [VUEN-2472]分享免Log in------------
//update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3Not supportedauth2Log in------------

export function createPermissionGuard(router: Router) {
  const userStore = useUserStoreWithOut();
  const permissionStore = usePermissionStoreWithOut();

  // Customize the number of home page jumps
  let homePathJumpCount = 0;

  router.beforeEach(async (to, from, next) => {
    if (
      // 【#6861】Logic to jump to custom home page，Just jump once
      homePathJumpCount < 1 &&
      from.path === ROOT_PATH &&
      to.path === PageEnum.BASE_HOME &&
      userStore.getUserInfo.homePath &&
      userStore.getUserInfo.homePath !== PageEnum.BASE_HOME
    ) {
      homePathJumpCount++;
      next(userStore.getUserInfo.homePath);
      return;
    }

    const token = userStore.getToken;

    // Whitelist can be directly entered
    if (whitePathList.includes(to.path as PageEnum)) {
      if (to.path === LOGIN_PATH && token) {
        const isSessionTimeout = userStore.getSessionTimeout;
        
        //update-begin---author:scott ---date:2023-04-24  for：【QQYUN-4713】Log in代码调整逻辑有问题，Transformation to be seen--
        //TODO vbenDefault writing method，The purpose is unknown at the moment，If there are any problems, please comment them out for now.
        //await userStore.afterLoginAction();
        //update-end---author:scott ---date::2023-04-24  for：【QQYUN-4713】Log in代码调整逻辑有问题，Transformation to be seen--
        
        try {
          if (!isSessionTimeout) {
            next((to.query?.redirect as string) || '/');
            return;
          }
        } catch {}
        //update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3Not supportedauth2Log in------------
      } else if (to.path === LOGIN_PATH && isOAuth2AppEnv() && !token) {
        //退出Log in进入此逻辑
        //If the page entered isloginpage and is currentlyOAuth2appenvironment，andtokenis empty，Just enterOAuth2Log inpage
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        if(to.query.tenantId){
          setAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID,to.query.tenantId)
        }
        next({ path: OAUTH2_LOGIN_PAGE_PATH });
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        return;
        //update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3Not supportedauth2Log in------------
      }
      next();
      return;
    }

    // token does not exist
    if (!token) {
      // You can access without permission. You need to set the routing meta.ignoreAuth to true
      if (to.meta.ignoreAuth) {
        next();
        return;
      }

      //update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2Not implemented------------
      let path = LOGIN_PATH;
      if (whitePathList.includes(to.path as PageEnum)) {
        // 在免Log in白名单，If the page entered isloginpage and is currentlyOAuth2appenvironment，Just enterOAuth2Log inpage
        if (to.path === LOGIN_PATH && isOAuth2AppEnv()) {
          next({ path: OAUTH2_LOGIN_PAGE_PATH });
        } else {
          //在免Log in白名单，Enter directly
          next();
        }
      } else {
        //update-begin---author:wangshuai ---date:20230302  for：只有首次登陆and是Enterprise WeChat或者钉钉的情况下才会调用------------
        //----------【首次登陆and是Enterprise WeChat或者钉钉的情况下才会调用】-----------------------------------------------
        //只有首次登陆and是Enterprise WeChat或者钉钉的情况下才会调用
        let href = window.location.href;
        //Determine whether the currentauth2page，and是钉钉/Enterprise WeChat，and包含tenantIdparameter
        if(isOAuth2AppEnv() && href.indexOf("/tenantId/")!= -1){
          let params = to.params;
          if(params && params.path && params.path.length>0){
            //直接获取parameter最后一位
            setAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID,params.path[params.path.length-1])
          }
        }
        //---------【首次登陆and是Enterprise WeChat或者钉钉的情况下才会调用】------------------------------------------------
        //update-end---author:wangshuai ---date:20230302  for：只有首次登陆and是Enterprise WeChat或者钉钉的情况下才会调用------------
        // If it is currently inOAuth2APPenvironment，Just jump toOAuth2Log inpage，否则跳转到Log inpage
        path = isOAuth2AppEnv() ? OAUTH2_LOGIN_PAGE_PATH : LOGIN_PATH;
      }
      //update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2Not implemented------------
      // redirect login page
      const redirectData: { path: string; replace: boolean; query?: Recordable<string> } = {
        //update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2Not implemented------------
        path: path,
        //update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2Not implemented------------
        replace: true,
      };

      //update-begin---author:scott ---date:2023-04-24  for：【QQYUN-4713】Log in代码调整逻辑有问题，Transformation to be seen--
      if (to.fullPath) {
        console.log("to.fullPath 1",to.fullPath)
        console.log("to.path 2",to.path)
        
        let getFullPath = to.fullPath;
        if(getFullPath=='/' || getFullPath=='/500' || getFullPath=='/400' || getFullPath=='/login?redirect=/' || getFullPath=='/login?redirect=/login?redirect=/'){
          return;
        }
      //update-end---author:scott ---date:2023-04-24  for：【QQYUN-4713】Log in代码调整逻辑有问题，Transformation to be seen--
        
        redirectData.query = {
          ...redirectData.query,
          // update-begin-author:sunjianlei date:20230306 for: 修复Log in成功后，Problem with not redirecting correctly
          redirect: to.fullPath,
          // update-end-author:sunjianlei date:20230306 for: 修复Log in成功后，Problem with not redirecting correctly

        };
      }
      next(redirectData);
      return;
    }

    //==============================【首次Log inand是Enterprise WeChat或者钉钉的情况下才会调用】==================
    //判断是免Log inpage,如果page包含/tenantId/,Then go directly to the homepage
    if(isOAuth2AppEnv() && to.path.indexOf("/tenantId/") != -1){
      //update-begin---author:wangshuai---date:2024-11-08---for:【TV360X-2958】钉钉Log in后打开了敲敲云，换其他账号Log in后，When you open Knockout Cloud again, the apps that show the original account will be displayed.---
      if (isOAuth2DingAppEnv()) {
        next(OAUTH2_LOGIN_PAGE_PATH);
      } else {
        next(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
      }
      //update-end---author:wangshuai---date:2024-11-08---for:【TV360X-2958】钉钉Log in后打开了敲敲云，换其他账号Log in后，When you open Knockout Cloud again, the apps that show the original account will be displayed.---
      return;
    }
    //==============================【首次Log inand是Enterprise WeChat或者钉钉的情况下才会调用】==================
    // update-begin--author:liaozhiyang---date:202401127---for：【issues/7500】vue-router4.5.0version routingname:PageNotFound同名导致Log in进不去
    // Jump to the 404 page after processing the login
    if (from.path === LOGIN_PATH && to.name === PAGE_NOT_FOUND_NAME_404 && to.fullPath !== (userStore.getUserInfo.homePath || PageEnum.BASE_HOME)) {
      next(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
      return;
    }
    // update-end--author:liaozhiyang---date:202401127---for：【issues/7500】vue-router4.5.0version routingname:PageNotFound同名导致Log in进不去

    //update-begin---author:scott ---date:2024-02-21  for：【QQYUN-8326】Refresh home page，No need to re-obtain user information---
    // // get userinfo while last fetch time is empty
    // if (userStore.getLastUpdateTime === 0) {
    //   try {
    //     console.log("--LastUpdateTime---getUserInfoAction-----")
    //     await userStore.getUserInfoAction();
    //   } catch (err) {
    //     console.info(err);
    //     next();
    //   }
    // }
    //update-end---author:scott ---date::2024-02-21  for：【QQYUN-8326】Refresh home page，No need to re-obtain user information---
    // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8572】Table row selection stuck problem（customRendercaused by Chinese dictionary）
    if (userStore.getLastUpdateTime === 0) {
      userStore.setAllDictItemsByLocal();
    }
    // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-8572】Table row selection stuck problem（customRendercaused by Chinese dictionary）
    if (permissionStore.getIsDynamicAddedRoute) {
      next();
      return;
    }

    // Build background menu routing
    const routes = await permissionStore.buildRoutesAction();
    routes.forEach((route) => {
      router.addRoute(route as unknown as RouteRecordRaw);
    });

    router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
    permissionStore.setDynamicAddedRoute(true);
    // update-begin--author:liaozhiyang---date:202401127---for：【issues/7500】vue-router4.5.0version routingname:PageNotFound同名导致Log in进不去
    if (to.name === PAGE_NOT_FOUND_NAME_404) {
      // After dynamically adding routes，This should redirect tofullPath，Otherwise it will be loaded404page内容
      next({ path: to.fullPath, replace: true, query: to.query });
    } else {
      const redirectPath = (from.query.redirect || to.path) as string;
      const redirect = decodeURIComponent(redirectPath);
      const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect };
      next(nextData);
    }
    // update-end--author:liaozhiyang---date:202401127---for：【issues/7500】vue-router4.5.0version routingname:PageNotFound同名导致Log in进不去
  });
}
