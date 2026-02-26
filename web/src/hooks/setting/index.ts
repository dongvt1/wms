import type { GlobConfig } from '/#/config';

import { getAppEnvConfig } from '/@/utils/env';

export const useGlobSetting = (): Readonly<GlobConfig> => {
  const {
    VITE_GLOB_APP_TITLE,
    VITE_GLOB_API_URL,
    VITE_GLOB_APP_SHORT_NAME,
    VITE_GLOB_API_URL_PREFIX,
    VITE_GLOB_APP_CAS_BASE_URL,
    VITE_GLOB_APP_OPEN_SSO,
    VITE_GLOB_APP_OPEN_QIANKUN,
    VITE_GLOB_DOMAIN_URL,
    VITE_GLOB_ONLINE_VIEW_URL,
    VITE_GLOB_RUN_PLATFORM,

    // 【JEECGAs Qiankunzi application】
    VITE_GLOB_QIANKUN_MICRO_APP_NAME,
    VITE_GLOB_QIANKUN_MICRO_APP_ENTRY,
  } = getAppEnvConfig();

  // if (!/[a-zA-Z\_]*/.test(VITE_GLOB_APP_SHORT_NAME)) {
  //   warn(
  //     `VITE_GLOB_APP_SHORT_NAME Variables can only be characters/underscores, please modify in the environment variables and re-running.`
  //   );
  // }

  // short title：replaceshortNameThe underscore is a space
  const shortTitle = VITE_GLOB_APP_SHORT_NAME.replace(/_/g, " ");
  // Take global configuration
  const glob: Readonly<GlobConfig> = {
    title: VITE_GLOB_APP_TITLE,
    domainUrl: VITE_GLOB_DOMAIN_URL,
    apiUrl: VITE_GLOB_API_URL,
    shortName: VITE_GLOB_APP_SHORT_NAME,
    shortTitle: shortTitle,
    openSso: VITE_GLOB_APP_OPEN_SSO,
    openQianKun: VITE_GLOB_APP_OPEN_QIANKUN,
    casBaseUrl: VITE_GLOB_APP_CAS_BASE_URL,
    urlPrefix: VITE_GLOB_API_URL_PREFIX,
    uploadUrl: VITE_GLOB_DOMAIN_URL,
    viewUrl: VITE_GLOB_ONLINE_VIEW_URL,
    // Is it currently running on electron platform
    isElectronPlatform: VITE_GLOB_RUN_PLATFORM === 'electron',

    // 【JEECGAs Qiankunzi application】Whether to start in Qiankunzi application mode
    isQiankunMicro: VITE_GLOB_QIANKUN_MICRO_APP_NAME != null && VITE_GLOB_QIANKUN_MICRO_APP_NAME !== '',
    // 【JEECGAs Qiankunzi application】Qiankunzi application entrance
    qiankunMicroAppEntry: VITE_GLOB_QIANKUN_MICRO_APP_ENTRY,
  };

  // 【JEECGAs Qiankunzi application】Qiankunzi application，need to be defined
  if (!window['_CONFIG']) {
    window['_CONFIG'] = {}
  }

  // update-begin--author:sunjianlei---date:220250115---for：【QQYUN-10956】Custom prefix configured，External connection cannot be opened，Compatibility processing is required
  let domainURL = VITE_GLOB_DOMAIN_URL;

  // If it is not based onhttp(s)beginning，也不是以域名beginning，Then it is to splice the current domain name
  if (!/^http(s)?/.test(domainURL) && !/^(\/\/)?(.*\.)?.+\..+/.test(domainURL)) {
    if (!domainURL.startsWith('/')) {
      domainURL = '/' + domainURL;
    }
    domainURL = window.location.origin + domainURL;
  }
  // update-end--author:sunjianlei---date:220250115---for：【QQYUN-10956】Custom prefix configured，External connection cannot be opened，Compatibility processing is required

  // @ts-ignore
  window._CONFIG['domianURL'] = domainURL;

  return glob as Readonly<GlobConfig>;
};
