import type { RouteRecordRaw } from 'vue-router';
import type { App } from 'vue';

import { $electron } from "@/electron";
import { basicRoutes } from './routes';
import {createRouter as createVueRouter, destroyRouter, router} from './router'

// The whitelist should contain basic static routes
const WHITE_NAME_LIST: string[] = [];
const getRouteNames = (array: any[]) =>
  array.forEach((item) => {
    WHITE_NAME_LIST.push(item.name);
    getRouteNames(item.children || []);
  });
getRouteNames(basicRoutes);

/**
 * Create routing instance
 */
export function createRouter() {
  let router = createVueRouter({
      routes: basicRoutes as unknown as RouteRecordRaw[],
      strict: true,
      scrollBehavior: () => ({left: 0, top: 0}),
    },
    // in the case of Electron environment，then use hash routing
    $electron.isElectron(),
  )

  // TODO 【QQYUN-4517】【form designer】记录分享routing守卫测试
  // @ts-ignore
  router.beforeEach(async (to, from, next) => {
    //console.group('【QQYUN-4517】beforeEach');
    //console.warn('from', from);
    //console.warn('to', to);
    //console.groupEnd();
    next();
  });
}

// reset router
export function resetRouter() {
  router.getRoutes().forEach((route) => {
    const { name } = route;
    if (name && !WHITE_NAME_LIST.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name);
    }
  });
}

// config router
export function setupRouter(app: App<Element>) {
  app.use(router);
}

export {
  router,
  destroyRouter,
}
