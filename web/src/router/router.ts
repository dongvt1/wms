/*
 * Routing instance storage file，Don't add other code easily，prevent HMR or other questions
 */
import type {Router, RouterHistory} from 'vue-router';
import {createRouter as createVueRouter, createWebHistory, createWebHashHistory, RouterOptions} from 'vue-router';

export let router: Router = null as unknown as Router;

export function setRouter(r: Router) {
  router = r
}

let webHistory: Nullable<RouterHistory> = null;

/**
 * Create route
 * @param options parameter
 * @param useHashHistory Whether to use hash routing，trueuse，false不usehashrouting
 */
export function createRouter(options: Partial<RouterOptions>, useHashHistory = false) {
  const createFn = useHashHistory ? createWebHashHistory : createWebHistory;
  webHistory = createFn(import.meta.env.VITE_PUBLIC_PATH);
  // app router
  let router = createVueRouter({
    history: webHistory,
    routes: [],
    ...options,
  });

  setRouter(router)

  return router
}

// 销毁routing
export function destroyRouter() {
  setRouter(null as unknown as Router);
  if (webHistory) {
    webHistory.destroy();
  }
  webHistory = null
}
