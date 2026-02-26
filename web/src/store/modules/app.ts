import type { MainAppProps } from "#/main";
import type { ProjectConfig, HeaderSetting, MenuSetting, TransitionSetting, MultiTabsSetting } from '/#/config';
import type { BeforeMiniState } from '/#/store';

import { defineStore } from 'pinia';
import { store } from '/@/store';

import { ThemeEnum } from '/@/enums/appEnum';
import { APP_DARK_MODE_KEY_, PROJ_CFG_KEY } from '/@/enums/cacheEnum';
import { Persistent } from '/@/utils/cache/persistent';
import { darkMode } from '/@/settings/designSetting';
import { resetRouter } from '/@/router';
import { deepMerge } from '/@/utils';
import { getHideLayoutTypes } from '/@/utils/env';
import setting from '/@/settings/projectSetting';

interface AppState {
  darkMode?: ThemeEnum;
  // Page loading status
  pageLoading: boolean;
  // project config
  projectConfig: ProjectConfig | null;
  // When the window shrinks, remember some states, and restore these states when the window is restored
  beforeMiniInfo: BeforeMiniState;
  // Page jump temporary parameter storage
  messageHrefParams: any,
  // Application parameters
  mainAppProps: MainAppProps,
}
let timeId: TimeoutHandle;
export const useAppStore = defineStore({
  id: 'app',
  state: (): AppState => ({
    darkMode: undefined,
    pageLoading: false,
    projectConfig: Persistent.getLocal(PROJ_CFG_KEY),
    beforeMiniInfo: {},
    messageHrefParams: {},
    mainAppProps: {},
  }),
  getters: {
    getPageLoading(): boolean {
      return this.pageLoading;
    },
    getDarkMode(): 'light' | 'dark' | string {
      // liaozhiyang---date:20250411---for：【QQYUN-11956】repairprojectSettingThe theme mode configured in Medium does not take effect
      const getSettingTheme = () => {
        const theme = setting.themeMode;
        if (theme) {
          if (theme == ThemeEnum.DARK) {
            // forindex.htmlpageloadingIt's dark
            localStorage.setItem(APP_DARK_MODE_KEY_, theme);
          }
          return theme;
        }
        return '';
      };
      // liaozhiyang---date:20250411---for：【QQYUN-11956】repairprojectSettingThe theme mode configured in Medium does not take effect
      return this.darkMode || localStorage.getItem(APP_DARK_MODE_KEY_) || getSettingTheme() || darkMode;
    },

    getBeforeMiniInfo(): BeforeMiniState {
      return this.beforeMiniInfo;
    },

    getProjectConfig(): ProjectConfig {
      return this.projectConfig || ({} as ProjectConfig);
    },

    getHeaderSetting(): HeaderSetting {
      return this.getProjectConfig.headerSetting;
    },
    getMenuSetting(): MenuSetting {
      return this.getProjectConfig.menuSetting;
    },
    getTransitionSetting(): TransitionSetting {
      return this.getProjectConfig.transitionSetting;
    },
    getMultiTabsSetting(): MultiTabsSetting {
      return this.getProjectConfig.multiTabsSetting;
    },
    getMessageHrefParams():any{
      return this.messageHrefParams;
    },
    getMainAppProps(): MainAppProps {
      return this.mainAppProps;
    },

    getLayoutHideSider(): boolean {
      const hideLayoutTypes = getHideLayoutTypes();
      if (hideLayoutTypes.includes('sider')) {
        return true;
      }
      return !!this.mainAppProps.hideSider;
    },
    getLayoutHideHeader(): boolean {
      const hideLayoutTypes = getHideLayoutTypes();
      if (hideLayoutTypes.includes('header')) {
        return true;
      }
      return !!this.mainAppProps.hideHeader;
    },
    getLayoutHideMultiTabs(): boolean {
      const hideLayoutTypes = getHideLayoutTypes();
      if (hideLayoutTypes.includes('multi-tabs')) {
        return true;
      }
      return !!this.mainAppProps.hideMultiTabs;
    },
  },
  actions: {
    setPageLoading(loading: boolean): void {
      this.pageLoading = loading;
    },

    setDarkMode(mode: ThemeEnum): void {
      this.darkMode = mode;
      localStorage.setItem(APP_DARK_MODE_KEY_, mode);
      this.setProjectConfig({ themeMode: mode });
    },

    setBeforeMiniInfo(state: BeforeMiniState): void {
      this.beforeMiniInfo = state;
    },

    setProjectConfig(config: DeepPartial<ProjectConfig>): void {
      this.projectConfig = deepMerge(this.projectConfig || {}, config);
      // update-begin--author:liaozhiyang---date:20240408---for：【QQYUN-8922】Setting the navigation bar mode is not saved locally，Refresh and restore
      Persistent.setLocal(PROJ_CFG_KEY, this.projectConfig, true);
      // update-end--author:liaozhiyang---date:20240408---for：【QQYUN-8922】Setting the navigation bar mode is not saved locally，Refresh and restore
    },

    async resetAllState() {
      resetRouter();
      Persistent.clearAll();
    },
    async setPageLoadingAction(loading: boolean): Promise<void> {
      if (loading) {
        clearTimeout(timeId);
        // Prevent flicker
        timeId = setTimeout(() => {
          this.setPageLoading(loading);
        }, 50);
      } else {
        this.setPageLoading(loading);
        clearTimeout(timeId);
      }
    },
    setMessageHrefParams(params: any): void {
      this.messageHrefParams = params;
    },

    // 设置主Application parameters
    setMainAppProps(args: MainAppProps)  {
      this.mainAppProps.hideHeader = args.hideHeader ?? false;
      this.mainAppProps.hideSider = args.hideSider ?? false;
      this.mainAppProps.hideMultiTabs = args.hideMultiTabs ?? false;
    },

  },
});

// Need to be used outside the setup
export function useAppStoreWithOut() {
  return useAppStore(store);
}
