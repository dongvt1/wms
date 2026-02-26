import { MenuTypeEnum, MenuModeEnum, TriggerEnum, MixSidebarTriggerEnum } from '/@/enums/menuEnum';
import {
  ContentEnum,
  PermissionModeEnum,
  ThemeEnum,
  RouterTransitionEnum,
  SettingButtonPositionEnum,
  SessionTimeoutProcessingEnum,
} from '/@/enums/appEnum';

import { CacheTypeEnum } from '/@/enums/cacheEnum';

export type LocaleType = 'en' | 'ru' | 'ja' | 'ko';

export interface MenuSetting {
  bgColor: string;
  fixed: boolean;
  collapsed: boolean;
  canDrag: boolean;
  show: boolean;
  hidden: boolean;
  split: boolean;
  menuWidth: number;
  mode: MenuModeEnum;
  type: MenuTypeEnum;
  theme: ThemeEnum;
  // update-begin--author:liaozhiyang---date:20240408---for：【QQYUN-8922】Adjust the text color of the left navigation bar to distinguish between color and dark
  isThemeBright: boolean;
  // update-end--author:liaozhiyang---date:20240408---for：【QQYUN-8922】Adjust the text color of the left navigation bar to distinguish between color and dark
  topMenuAlign: 'start' | 'center' | 'end';
  trigger: TriggerEnum;
  accordion: boolean;
  closeMixSidebarOnChange: boolean;
  collapsedShowTitle: boolean;
  mixSideTrigger: MixSidebarTriggerEnum;
  mixSideFixed: boolean;
}

export interface MultiTabsSetting {
  cache: boolean;
  show: boolean;
  showQuick: boolean;
  canDrag: boolean;
  showRedo: boolean;
  showFold: boolean;
  theme: string;
}

export interface HeaderSetting {
  bgColor: string;
  fixed: boolean;
  show: boolean;
  theme: ThemeEnum;
  // Whether to show the full screen button
  showFullScreen: boolean;
  // Whether to display the lock screen button
  useLockPage: boolean;
  // Whether to display document links
  showDoc: boolean;
  // Whether to display the message icon
  showNotice: boolean;
  // Whether to display the search button
  showSearch: boolean;
}

export interface LocaleSetting {
  // Whether to display the internationalization switch button
  showPicker: boolean;
  // Current language
  locale: LocaleType;
  // default language
  fallback: LocaleType;
  // available Locales
  availableLocales: LocaleType[];
}

export interface TransitionSetting {
  //  Whether to open the page switching animation
  enable: boolean;
  // Route basic switching animation
  basicTransition: RouterTransitionEnum;
  // Whether to open page switching loading
  openPageLoading: boolean;
  // Whether to open the top progress bar
  openNProgress: boolean;
}

export interface ProjectConfig {
  // Storage location of permission related information
  permissionCacheType: CacheTypeEnum;
  // Whether to show the configuration button
  showSettingButton: boolean;
  // Whether to show the theme switch button
  showDarkModeToggle: boolean;
  // Configure where the button is displayed
  settingButtonPosition: SettingButtonPositionEnum;
  // Permission mode
  permissionMode: PermissionModeEnum;
  // Session timeout processing
  sessionTimeoutProcessing: SessionTimeoutProcessingEnum;
  // Website gray mode, open for possible mourning dates
  grayMode: boolean;
  // Whether to turn on the color weak mode
  colorWeak: boolean;
  // Theme color
  themeColor: string;
  // Theme Mode
  themeMode: string;

  // The main interface is displayed in full screen, the menu is not displayed, and the top
  fullContent: boolean;
  // content width
  contentMode: ContentEnum;
  // Whether to display the logo
  showLogo: boolean;
  // Whether to show the global footer
  showFooter: boolean;
  // menuType: MenuTypeEnum;
  headerSetting: HeaderSetting;
  // menuSetting
  menuSetting: MenuSetting;
  // Multi-tab settings
  multiTabsSetting: MultiTabsSetting;
  // Animation configuration
  transitionSetting: TransitionSetting;
  // pageLayout whether to enable keep-alive
  openKeepAlive: boolean;
  // Lock screen time
  lockTime: number;
  // Show breadcrumbs
  showBreadCrumb: boolean;
  // Show breadcrumb icon
  showBreadCrumbIcon: boolean;
  // Use error-handler-plugin
  useErrorHandle: boolean;
  // Whether to open back to top
  useOpenBackTop: boolean;
  // Is it possible to embed iframe pages
  canEmbedIFramePage: boolean;
  // Whether to delete unclosed messages and notify when switching the interface
  closeMessageOnSwitch: boolean;
  // Whether to cancel the http request that has been sent but not responded when switching the interface.
  removeAllHttpPending: boolean;
  aiIconShow: boolean;
}

export interface GlobConfig {
  // Site title
  title: string;
  // Service interface url
  apiUrl: string;
  domainUrl: string;
  // Upload url (void)
  uploadUrl?: string;
  openSso?: string;
  openQianKun?: string;
  casBaseUrl?: string;
  // onlineview url
  viewUrl?: string;
  //  Service interface url prefix
  urlPrefix?: string;
  // Project abbreviation
  shortName: string;
  // short title
  shortTitle: string;
  // Is it currently running on electron platform
  isElectronPlatform: boolean;

  // 【JEECGAs Qiankunzi application】Whether to start in Qiankunzi application mode
  isQiankunMicro: boolean;
  // 【JEECGAs Qiankunzi application】Qiankunzi application entrance
  qiankunMicroAppEntry?: string;
}
export interface GlobEnvConfig {
  // Site title
  VITE_GLOB_APP_TITLE: string;
  // Service interface url
  VITE_GLOB_API_URL: string;
  VITE_USE_MOCK: string;
  // Service interface url prefix
  VITE_GLOB_API_URL_PREFIX?: string;
  // Project abbreviation
  VITE_GLOB_APP_SHORT_NAME: string;
  //Whether to enable single sign-on
  VITE_GLOB_APP_OPEN_SSO: string;
  //Whether to enable micro application mode
  VITE_GLOB_APP_OPEN_QIANKUN: string;
  //Single point server address
  VITE_GLOB_APP_CAS_BASE_URL: string;
  VITE_GLOB_DOMAIN_URL: string;
  // Upload url
  VITE_GLOB_UPLOAD_URL?: string;
  // view url
  VITE_GLOB_ONLINE_VIEW_URL?: string;
  // Which layouts are globally hidden，Separate multiples with commas
  VITE_GLOB_HIDE_LAYOUT_TYPES?: string;

  // 【JEECGAs Qiankunzi application】填写后将As Qiankunzi application启动，When the main application is registeredAppNameNeed to be consistent
  VITE_GLOB_QIANKUN_MICRO_APP_NAME?: string;
  // 【JEECGAs Qiankunzi application】As Qiankunzi application启动时必填，need toqiankunFilled in when the main application registers the sub-application entry Be consistent
  VITE_GLOB_QIANKUN_MICRO_APP_ENTRY?: string;
  //Online document editing version。Optional attributes：wps, onlyoffice
  VITE_GLOB_ONLINE_DOCUMENT_VERSION?: string;
  // 当前运行在什么platform
  VITE_GLOB_RUN_PLATFORM?: 'web' | 'electron';
}
