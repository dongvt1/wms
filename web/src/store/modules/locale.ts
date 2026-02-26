import type { LocaleSetting, LocaleType } from '/#/config';

import { defineStore } from 'pinia';
import { store } from '/@/store';

import { LOCALE_KEY } from '/@/enums/cacheEnum';
import { createLocalStorage } from '/@/utils/cache';
import { localeSetting } from '/@/settings/localeSetting';

const ls = createLocalStorage();

const lsLocaleSetting = (ls.get(LOCALE_KEY) || localeSetting) as LocaleSetting;

interface LocaleState {
  localInfo: LocaleSetting;
  pathTitleMap: object;
  // myappstheme color（Low-code application list homepage）
  appIndexTheme: string
  // myapps - Routing address before jump
  appMainPth: string
}

export const useLocaleStore = defineStore({
  id: 'app-locale',
  state: (): LocaleState => ({
    localInfo: lsLocaleSetting,
    pathTitleMap: {},
    appIndexTheme: '',
    appMainPth: ''
  }),
  getters: {
    getShowPicker(): boolean {
      return !!this.localInfo?.showPicker;
    },
    getLocale(): LocaleType {
      return this.localInfo?.locale ?? 'en';
    },
    //update-begin-author:taoyan date:2022-6-1 for: VUEN-1144 online After configuring it as a menu，Open menu，Display name not shown as menu name
    getPathTitle: (state) => {
      return (path) => state.pathTitleMap[path];
    },
    //update-end-author:taoyan date:2022-6-1 for: VUEN-1144 online After configuring it as a menu，Open menu，Display name not shown as menu name
    getAppIndexTheme(): string {
      return this.appIndexTheme;
    },
    getAppMainPth(): string {
      return this.appMainPth;
    },
  },
  actions: {
    /**
     * Set up multilingual information and cache
     * @param info multilingual info
     */
    setLocaleInfo(info: Partial<LocaleSetting>) {
      this.localInfo = { ...this.localInfo, ...info };
      ls.set(LOCALE_KEY, this.localInfo);
    },
    /**
     * Initialize multilingual information and load the existing configuration from the local cache
     */
    initLocale() {
      this.setLocaleInfo({
        ...localeSetting,
        ...this.localInfo,
      });
    },
    //update-begin-author:taoyan date:2022-6-1 for: VUEN-1144 online After configuring it as a menu，Open menu，Display name not shown as menu name
    setPathTitle(path, title) {
      this.pathTitleMap[path] = title;
    },
    //update-end-author:taoyan date:2022-6-1 for: VUEN-1144 online After configuring it as a menu，Open menu，Display name not shown as menu name
    setAppIndexTheme(theme) {
      this.appIndexTheme = theme;
    },
    setAppMainPth(path) {
      this.appMainPth = path;
    },
  },
});

// Need to be used outside the setup
export function useLocaleStoreWithOut() {
  return useLocaleStore(store);
}
