import type {App} from "vue";
import {router} from "@/router";
import {useGlobSetting} from "@/hooks/setting";
import { ElectronEnum } from '/@/enums/jeecgEnum'
const glob = useGlobSetting();

const _PRELOAD_UTILS = ElectronEnum.ELECTRON_API;

export const $electron = {
  // Is the currentElectronplatform
  isElectron: () => glob.isElectronPlatform,

  // Open link via browser
  openInBrowser: bindUtils('openInBrowser') as (url: string) => void,

  resolveRoutePath,
}

function bindUtils(n: string) {
  const fn = window[_PRELOAD_UTILS]?.[n];
  if (typeof fn?.bind === 'function') {
    return fn.bind(null);
  }
  return () => console.warn(`Electron preload util ${n} is not a function`);
}

// Parse routing path
function resolveRoutePath(path: string) {
  return window.location.origin + window.location.pathname + router.resolve(path).href;
}

/**
 * ConfigurationElectron
 */
export function setupElectron(_: App) {
  // NoElectronplatform不执行
  if (!$electron.isElectron()) {
    return;
  }
  hookWindowOpen();
  // update-begin--author:liaozhiyang---date:20250725---for：【JHHB-13】Desktop app message notifications
  hookNavigate();
  // update-end--author:liaozhiyang---date:20250725---for：【JHHB-13】Desktop app message notifications
}
function hookNavigate() {
  // @ts-ignore
  window[ElectronEnum.ELECTRON_API].onNavigate((path) => {
    router.push({ path });
  });
}
function hookWindowOpen() {
  // Save native method reference
  const originFunc = window.open;
  // rewritewindow.openmethod
  window['open'] = function (url, windowName, windowFeatures) {
    url = typeof url === 'string' ? url.trim() : '';
    if (!url) {
      throw new Error('window.open: url is required');
    }
    // Determine whether tohttporhttpsbeginning
    if (/^https?:\/\//.test(url)) {
      // Determine whether it is a local address
      if (url.startsWith(window.location.origin) || url.startsWith(window['_CONFIG']['domianURL'])) {
        // Open directly
        return originFunc(url, windowName, windowFeatures);
      }
      // callElectronOpen external browser
      return $electron.openInBrowser(url) as any;
    }
    // Custom logic
    return originFunc(url, windowName, windowFeatures)
  }
}
