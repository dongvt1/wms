import type { Menu } from '/@/router/types';
import type { Ref } from 'vue';
import { watch, unref, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { MenuSplitTyeEnum, MenuTypeEnum } from '/@/enums/menuEnum';
import { useThrottleFn } from '@vueuse/core';
import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
import { getChildrenMenus, getCurrentParentPath, getMenus, getShallowMenus } from '/@/router/menus';
import { usePermissionStore } from '/@/store/modules/permission';
import { useAppInject } from '/@/hooks/web/useAppInject';
import { PAGE_NOT_FOUND_NAME_404 } from '/@/router/constant';

export function useSplitMenu(splitType: Ref<MenuSplitTyeEnum>) {
  // Menu array
  const menusRef = ref<Menu[]>([]);
  const { currentRoute } = useRouter();
  const { getIsMobile } = useAppInject();
  const permissionStore = usePermissionStore();
  const { setMenuSetting, getIsHorizontal, getSplit, getMenuType } = useMenuSetting();

  const throttleHandleSplitLeftMenu = useThrottleFn(handleSplitLeftMenu, 50);

  const splitNotLeft = computed(() => unref(splitType) !== MenuSplitTyeEnum.LEFT && !unref(getIsHorizontal));

  const getSplitLeft = computed(() => !unref(getSplit) || unref(splitType) !== MenuSplitTyeEnum.LEFT);

  const getSpiltTop = computed(() => unref(splitType) === MenuSplitTyeEnum.TOP);

  const normalType = computed(() => {
    return unref(splitType) === MenuSplitTyeEnum.NONE || !unref(getSplit);
  });

  watch(
    [() => unref(currentRoute).path, () => unref(splitType)],
    async ([path]: [string, MenuSplitTyeEnum]) => {
      if (unref(splitNotLeft) || unref(getIsMobile)) return;
      const { meta } = unref(currentRoute);
      const currentActiveMenu = meta.currentActiveMenu as string;
      // update-begin--author:liaozhiyang---date:20250908---for：【QQYUN-13718】The first-level menu redirects to the submenu by default，But the submenu is not authorized，As a result, clicking the first-level menu cannot load the submenu.
      // When top blending mode and top left combo menu start
      if (unref(getMenuType) === MenuTypeEnum.MIX && unref(getSplit)) { 
        // 404page，Jump to the redirected path
        if (unref(currentRoute).name === PAGE_NOT_FOUND_NAME_404 && unref(currentRoute)?.redirectedFrom?.path) {
          const menus = await getMenus();
          const findItem = menus.find((item:any) => item.redirect === unref(currentRoute).path);
          if (findItem) {
            // The description is redirected from the first-level menu
            path = findItem.path;
          }
        }
      }
      // update-end--author:liaozhiyang---date:20250908---for：【QQYUN-13718】The first-level menu redirects to the submenu by default，But the submenu is not authorized，As a result, clicking the first-level menu cannot load the submenu.
      let parentPath = await getCurrentParentPath(path);
      if (!parentPath) {
        parentPath = await getCurrentParentPath(currentActiveMenu);
      }
      parentPath && throttleHandleSplitLeftMenu(parentPath);
    },
    {
      immediate: true,
    }
  );

  // Menu changes
  watch(
    [() => permissionStore.getLastBuildMenuTime, () => permissionStore.getBackMenuList],
    () => {
      genMenus();
    },
    {
      immediate: true,
    }
  );

  // split Menu changes
  watch(
    () => getSplit.value,
    () => {
      // update-begin--author:liaozhiyang---date:20240919---for：【issues/7209】The left navigation is not restored after the top left combination menu is closed.
      // if (unref(splitNotLeft)) return;
      // update-end--author:liaozhiyang---date:20240919---for：【issues/7209】The left navigation is not restored after the top left combination menu is closed.
      genMenus();
    }
  );

  // Handle left menu split
  async function handleSplitLeftMenu(parentPath: string) {
    if (unref(getSplitLeft) || unref(getIsMobile)) return;

    // spilt mode left
    const children = await getChildrenMenus(parentPath);

    if (!children || !children.length) {
      setMenuSetting({ hidden: true });
      menusRef.value = [];
      return;
    }

    setMenuSetting({ hidden: false });
    menusRef.value = children;
  }

  // get menus
  async function genMenus() {
    // normal mode
    if (unref(normalType) || unref(getIsMobile)) {
      menusRef.value = await getMenus();
      return;
    }

    // split-top
    if (unref(getSpiltTop)) {
      const shallowMenus = await getShallowMenus();

      menusRef.value = shallowMenus;
      return;
    }
  }

  return { menusRef };
}
