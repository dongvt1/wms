import { ref } from 'vue';
import { getMenus } from '/@/router/menus';

export const useHideHomeDesign = (currentRoute) => {
  let menus: any = [];
  // Whether to hide the portal design
  const isHideHomeDesign = ref(true);
  const getHideHomeDesign = (isCurItem, path) => {
    if (/^\/portal-view\/[^/]+$/.test(path) && isCurItem) {
      if (['/portal-view/system', '/portal-view/template'].includes(path)) {
        // main portal、template portal (Need to check if design list exists,Show portal design if present,Hide portal design if not present)
        getIsHasPortalDesignList();
      } else if (['/portal-view/default'].includes(path)) {
        // The preview opened in the designer needs to hide the design mode
        isHideHomeDesign.value = true;
      } else {
        // Portal design can be displayed on personal workbench or ordinary portal
        isHideHomeDesign.value = false;
      }
    } else {
      // Non-portal page hidden portal design
      isHideHomeDesign.value = true;
    }
  };
  const getMenusContainPath = async (ptah) => {
    if (!menus.length) {
      menus = await getMenus();
    }
    const result = getMatchingRouterName(menus, ptah);
    return !!result;
  };
  const getIsHasPortalDesignList = async () => {
    if (['/portal-view/system', '/portal-view/template'].includes(currentRoute.value.path)) {
      // main portal、template portal时才需要查询菜单中是否有portalDesignList
      getMenusContainPath('/super/eoa/portalapp/portalDesignList').then((result) => {
        isHideHomeDesign.value = !result;
      });
    }
  };
  getIsHasPortalDesignList();
  return {
    getHideHomeDesign,
    isHideHomeDesign,
  };
};

/*
 * 20250701
 * liaozhiyang
 * passpathMatch items in menu
 * */
function getMatchingRouterName(menus, path) {
  for (let i = 0, len = menus.length; i < len; i++) {
    const item = menus[i];
    if (item.path === path && !item.redirect && !item.paramPath) {
      return item;
    } else if (item.children?.length) {
      const result = getMatchingRouterName(item.children, path);
      if (result) {
        return result;
      }
    }
  }
  return null;
}
