import type { GlobEnvConfig } from '/#/config';

import { warn } from '/@/utils/log';
import pkg from '../../package.json';
import { getConfigFileName } from '../../build/getConfigFileName';
import { getGlobal } from "@/qiankun/micro";

export function getCommonStoragePrefix() {
  const { VITE_GLOB_APP_SHORT_NAME } = getAppEnvConfig();
  return `${VITE_GLOB_APP_SHORT_NAME}__${getEnv()}`.toUpperCase();
}

// Generate cache key according to version
export function getStorageShortName() {
  return `${getCommonStoragePrefix()}${`__${pkg.version}`}__`.toUpperCase();
}

export function getAppEnvConfig() {
  const ENV_NAME = getConfigFileName(import.meta.env);

  const global = getGlobal();

  const ENV = (import.meta.env.DEV
    ? // Get the global configuration (the configuration will be extracted independently when packaging)
      (import.meta.env as unknown as GlobEnvConfig)
    : global[ENV_NAME as any]) as unknown as GlobEnvConfig;

  const {
    VITE_GLOB_APP_TITLE,
    VITE_GLOB_API_URL,
    VITE_USE_MOCK,
    VITE_GLOB_APP_SHORT_NAME,
    VITE_GLOB_API_URL_PREFIX,
    VITE_GLOB_APP_OPEN_SSO,
    VITE_GLOB_APP_OPEN_QIANKUN,
    VITE_GLOB_APP_CAS_BASE_URL,
    VITE_GLOB_DOMAIN_URL,
    VITE_GLOB_ONLINE_VIEW_URL,
    // Which layouts are globally hidden，Separate multiples with commas
    VITE_GLOB_HIDE_LAYOUT_TYPES,
    // What platform is it currently running on?
    VITE_GLOB_RUN_PLATFORM,

    // 【JEECGAs Qiankunzi application】
    VITE_GLOB_QIANKUN_MICRO_APP_NAME,
    VITE_GLOB_QIANKUN_MICRO_APP_ENTRY,
    
    //Online document editing version。Optional attributes：wps, onlyoffice
    VITE_GLOB_ONLINE_DOCUMENT_VERSION,
  } = ENV;

  // if (!/^[a-zA-Z\_]*$/.test(VITE_GLOB_APP_SHORT_NAME)) {
  //   warn(
  //     `VITE_GLOB_APP_SHORT_NAME Variables can only be characters/Underline，Please modify the environment variables and rerun.`
  //   );
  // }

  return {
    VITE_GLOB_APP_TITLE,
    VITE_GLOB_API_URL,
    VITE_USE_MOCK,
    VITE_GLOB_APP_SHORT_NAME,
    VITE_GLOB_API_URL_PREFIX,
    VITE_GLOB_APP_OPEN_SSO,
    VITE_GLOB_APP_OPEN_QIANKUN,
    VITE_GLOB_APP_CAS_BASE_URL,
    VITE_GLOB_DOMAIN_URL,
    VITE_GLOB_ONLINE_VIEW_URL,
    VITE_GLOB_HIDE_LAYOUT_TYPES,
    VITE_GLOB_RUN_PLATFORM,

    // 【JEECGAs Qiankunzi application】
    VITE_GLOB_QIANKUN_MICRO_APP_NAME,
    VITE_GLOB_QIANKUN_MICRO_APP_ENTRY,

    //Online document editing version。Optional attributes：wps, onlyoffice
    VITE_GLOB_ONLINE_DOCUMENT_VERSION
  };
}

/**
 * @description: Development mode
 */
export const devMode = 'development';

/**
 * @description: Production mode
 */
export const prodMode = 'production';

/**
 * @description: Get environment variables
 * @returns:
 * @example:
 */
export function getEnv(): string {
  return import.meta.env.MODE;
}

/**
 * @description: Is it a development mode
 * @returns:
 * @example:
 */
export function isDevMode(): boolean {
  return import.meta.env.DEV;
}

/**
 * @description: Is it a production mode
 * @returns:
 * @example:
 */
export function isProdMode(): boolean {
  return import.meta.env.PROD;
}

export function getHideLayoutTypes(): string[] {
  const {VITE_GLOB_HIDE_LAYOUT_TYPES} = getAppEnvConfig();
  if (typeof VITE_GLOB_HIDE_LAYOUT_TYPES !== 'string') {
    return [];
  }
  return VITE_GLOB_HIDE_LAYOUT_TYPES.split(',');
}

/**
 * Get the online document version number
 */
export function getOnlineDocumentVersion(): string {
  const { VITE_GLOB_ONLINE_DOCUMENT_VERSION } = getAppEnvConfig();
  if (typeof VITE_GLOB_ONLINE_DOCUMENT_VERSION !== 'string') {
    return 'wps';
  }
  return VITE_GLOB_ONLINE_DOCUMENT_VERSION;
}
