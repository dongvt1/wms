export const REDIRECT_NAME = 'Redirect';

export const PARENT_LAYOUT_NAME = 'ParentLayout';

export const PAGE_NOT_FOUND_NAME = 'PageNotFound';
// update-begin--author:liaozhiyang---date:202401127---for：【issues/7500】vue-router4.5.0version routingname:PageNotFoundThe same name makes it impossible to log in.
export const PAGE_NOT_FOUND_NAME_404 = 'PageNotFound404';
// update-end--author:liaozhiyang---date:202401127---for：【issues/7500】vue-router4.5.0version routingname:PageNotFoundThe same name makes it impossible to log in.

export const EXCEPTION_COMPONENT = () => import('/@/views/sys/exception/Exception.vue');

/**
 * @description: default layout
 */
export const LAYOUT = () => import('/@/layouts/default/index.vue');

/**
 * @description: parent-layout
 */
export const getParentLayout = (_name?: string) => {
  return () =>
    new Promise((resolve) => {
      resolve({
        name: _name || PARENT_LAYOUT_NAME,
      });
    });
};
