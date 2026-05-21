import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';
import { LAYOUT } from '/@/router/constant';

const modules = import.meta.glob('./modules/**/*.ts', { eager: true });

const routeModuleList: AppRouteModule[] = [];

// Add to route collection
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  //New version backend login，If you want to use the old version to log in, just let go
  // component: () => import('/@/views/sys/login/Login.vue'),
  component: () => import('/@/views/system/loginmini/MiniLogin.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

//update-begin---author:wangshuai ---date:20220629  for：auth2Login page routing------------
export const Oauth2LoginRoute: AppRouteRecordRaw = {
  path: '/oauth2-app/login',
  name: 'oauth2-app-login',
  //The new version of DingTalk requires no login，If you want to use the old version, just let it go
  // component: () => import('/@/views/sys/login/OAuth2Login.vue'),
  component: () => import('/@/views/system/loginmini/OAuth2Login.vue'),
  meta: {
    title: t('routes.oauth2.login'),
  },
};
//update-end---author:wangshuai ---date:20220629  for：auth2Login page routing------------

/**
 * 【passtokenDirect and silent login】Process login page transit jump
 */
export const TokenLoginRoute: AppRouteRecordRaw = {
  path: '/tokenLogin',
  name: 'TokenLoginRoute',
  component: () => import('/@/views/sys/login/TokenLoginPage.vue'),
  meta: {
    title: 'bringtokenLogin page',
    ignoreAuth: true,
  },
};
// Basic routing without permission
export const basicRoutes = [LoginRoute, RootRoute, ...mainOutRoutes, REDIRECT_ROUTE, PAGE_NOT_FOUND_ROUTE, TokenLoginRoute, Oauth2LoginRoute];
