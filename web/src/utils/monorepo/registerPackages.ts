import type { App } from 'vue';
import { warn } from '/@/utils/log';
import { registerDynamicRouter } from '/@/utils/monorepo/dynamicRouter';
// Import module
import PACKAGE_JEECG_ONLINE from '@jeecg/online';
import PACKAGE_JEECG_AIFLOW from '@jeecg/aiflow';

export function registerPackages(app: App) {
  use(app, PACKAGE_JEECG_ONLINE);
  use(app, PACKAGE_JEECG_AIFLOW);
}

// noinspection JSUnusedGlobalSymbols
const installOptions = {
  baseImport,
};

/** Register module */
function use(app: App, pkg) {
  app.use(pkg, installOptions);
  registerDynamicRouter(pkg.getViews);
}

// available in the moduleimport
const importGlobs = [import.meta.glob('../../utils/**/*.{ts,js,tsx}'), import.meta.glob('../../hooks/**/*.{ts,js,tsx}')];

/**
 * Basic project guide package
 * Currently, the following imports are supported:
 * /@/utils/**
 * /@/hooks/**
 *
 * @param path file path，tsNo need to enter suffix name。like：/@/utils/common/compUtils
 */
async function baseImport(path: string) {
  if (path) {
    // Will /@/ Replace with ../../
    path = path.replace(/^\/@\//, '../../');
    for (const glob of importGlobs) {
      for (const key of Object.keys(glob)) {
        if (path === key || `${path}.ts` === key || `${path}.tsx` === key) {
          return glob[key]();
        }
      }
    }
    warn(`Introduction failed：${path} does not exist`);
  }
  return null;
}
