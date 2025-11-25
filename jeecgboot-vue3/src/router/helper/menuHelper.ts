import { AppRouteModule } from '/@/router/types';
import type { MenuModule, Menu, AppRouteRecordRaw } from '/@/router/types';
import { findPath, treeMap } from '/@/utils/helper/treeHelper';
import { cloneDeep } from 'lodash-es';
import { isUrl } from '/@/utils/is';
import { RouteParams } from 'vue-router';
import { toRaw } from 'vue';

export function getAllParentPath<T = Recordable>(treeData: T[], path: string) {
  // update-begin--author:sunjianlei---date:220230426---for：【issues/478】Fix menu expansion mergeBUG
  // Original code
  // const menuList = findPath(treeData, (n) => n.path === path) as Menu[];
  // Match paths that do not contain hidden menus first
  let menuList = findMenuPath(treeData, path, false);
  // If no match is found，Then match the path containing the hidden menu
  if(!(menuList?.length)) {
    menuList = findMenuPath(treeData, path, true)
  }
  // update-end--author:sunjianlei---date:220230426---for：【issues/478】Fix menu expansion mergeBUG
  return (menuList || []).map((item) => item.path);
}

/**
 * Find menu path
 *
 * @param treeData
 * @param path
 * @param matchHide Whether to match hidden menu
 */
function findMenuPath<T = Recordable>(treeData: T[], path: string, matchHide: boolean) {
  return findPath(treeData, (n) => {
    // Hidden menu does not participate in matching
    if(!matchHide && n.hideMenu) {
      return false;
    }
    return n.path === path
  }) as Menu[];
}

// Path handling
function joinParentPath(menus: Menu[], parentPath = '') {
  for (let index = 0; index < menus.length; index++) {
    const menu = menus[index];
    // https://next.router.vuejs.org/guide/essentials/nested-routes.html
    // Note that nested paths that start with / will be treated as a root path.
    // please note，by / Nested paths starting with will be treated as root paths。
    // This allows you to leverage the component nesting without having to use a nested URL.
    // This allows you to take advantage of component nesting，without using nested URL。
    if (!(menu.path.startsWith('/') || isUrl(menu.path))) {
      // path doesn't start with /, nor is it a url, join parent path
      // 路径不by / beginning，Neither url，Add parent path
      menu.path = `${parentPath}/${menu.path}`;
    }
    if (menu?.children?.length) {
      joinParentPath(menu.children, menu.meta?.hidePathForChildren ? parentPath : menu.path);
    }
  }
}

// Parsing the menu module
export function transformMenuModule(menuModule: MenuModule): Menu {
  const { menu } = menuModule;

  const menuList = [menu];

  joinParentPath(menuList);
  return menuList[0];
}

// Convert routes into menus
export function transformRouteToMenu(routeModList: AppRouteModule[], routerMapping = false) {
  // With the help of lodash deep copy
  const cloneRouteModList = cloneDeep(routeModList);
  const routeList: AppRouteRecordRaw[] = [];

  // Modify routing items
  cloneRouteModList.forEach((item) => {
    if (routerMapping && item.meta.hideChildrenInMenu && typeof item.redirect === 'string') {
      item.path = item.redirect;
    }

    if (item.meta?.single) {
      const realItem = item?.children?.[0];
      realItem && routeList.push(realItem);
    } else {
      routeList.push(item);
    }
  });
  // Extract the specified structure of the tree
  const list = treeMap(routeList, {
    conversion: (node: AppRouteRecordRaw) => {
      const { meta: { title, hideMenu = false } = {} } = node;

      return {
        ...(node.meta || {}),
        meta: node.meta,
        name: title,
        hideMenu,
        alwaysShow:node.alwaysShow||false,
        path: node.path,
        originComponent: node.originComponent,
        ...(node.redirect ? { redirect: node.redirect } : {}),
      };
    },
  });
  // Path handling
  joinParentPath(list);
  return cloneDeep(list);
}

/**
 * config menu with given params
 */
const menuParamRegex = /(?::)([\s\S]+?)((?=\/)|$)/g;

export function configureDynamicParamsMenu(menu: Menu, params: RouteParams) {
  const { path, paramPath } = toRaw(menu);
  let realPath = paramPath ? paramPath : path;
  const matchArr = realPath.match(menuParamRegex);

  matchArr?.forEach((it) => {
    const realIt = it.substr(1);
    if (params[realIt]) {
      realPath = realPath.replace(`:${realIt}`, params[realIt] as string);
    }
  });
  // save original param path.
  if (!paramPath && matchArr && matchArr.length > 0) {
    menu.paramPath = path;
  }
  menu.path = realPath;
  // children
  menu.children?.forEach((item) => configureDynamicParamsMenu(item, params));
}
