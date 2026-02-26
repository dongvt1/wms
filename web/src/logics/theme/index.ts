import { getThemeColors, generateColors } from '../../../build/config/themeConfig';
import {
  replaceStyleVariables,
  loadDarkThemeCss,
  replaceCssColors,
  darkCssIsReady,
  linkID,
  styleTagId,
  appendCssToDom,
  getStyleDom,
} from '@rys-fe/vite-plugin-theme/es/client';
import { mixLighten, mixDarken, tinycolor } from '@rys-fe/vite-plugin-theme/es/colorUtils';
import { useAppStore } from '/@/store/modules/app';
import { defHttp } from '/@/utils/http/axios';

let cssText = '';
export async function changeTheme(color: string) {
  // update-begin--author:liaozhiyang---date:20231218---for：【QQYUN-6366】upgrade toantd4.x
  const appStore = useAppStore();
  appStore.setProjectConfig({ themeColor: color });
  // update-end--author:liaozhiyang---date:20231218---for：【QQYUN-6366】upgrade toantd4.x
  const colors = generateColors({
    mixDarken,
    mixLighten,
    tinycolor,
    color,
  });
  // update-begin--author:liaozhiyang---date:20240322---for：【QQYUN-8570】The theme color does not take effect in the dark mode of the production environment
  if (import.meta.env.PROD && appStore.getDarkMode === 'dark') {
    if (!darkCssIsReady && !cssText) {
      await loadDarkThemeCss();
    }
    const el: HTMLLinkElement = document.getElementById(linkID) as HTMLLinkElement;
    if (el?.href) {
      // cssText = await fetchCss(el.href) as string;
      !cssText && (cssText = await defHttp.get({ url: el.href }, { isTransformResponse: false }));
      const colorVariables = [...getThemeColors(color), ...colors];
      const processCss = await replaceCssColors(cssText, colorVariables);
      appendCssToDom(getStyleDom(styleTagId) as HTMLStyleElement, processCss);
    }
  } else {
    await replaceStyleVariables({
      colorVariables: [...getThemeColors(color), ...colors],
    });
    fixDark();
  }
  // update-end--author:liaozhiyang---date:20240322---for：【QQYUN-8570】The theme color does not take effect in the dark mode of the production environment
}
// 【LOWCOD-2262】Fixed the issue where switching skins was invalid in dark mode
async function fixDark() {
  // update-begin--author:liaozhiyang---date:20240322---for：【QQYUN-8570】The theme color does not take effect in the dark mode of the production environment
  const el = document.getElementById(styleTagId);
  // update-end--author:liaozhiyang---date:20240322---for：【QQYUN-8570】The theme color does not take effect in the dark mode of the production environment
  if (el) {
    el.innerHTML = el.innerHTML.replace(/\\["']dark\\["']/g, `'dark'`);
  }
}
