import type { UserConfig, ConfigEnv } from 'vite';
import pkg from './package.json';
import dayjs from 'dayjs';
import { loadEnv } from 'vite';
import { resolve } from 'path';
import { generateModifyVars } from './build/generate/generateModifyVars';
import { createProxy } from './build/vite/proxy';
import { wrapperEnv } from './build/utils';
import { createVitePlugins } from './build/vite/plugin';
import { OUTPUT_DIR } from './build/constant';

function pathResolve(dir: string) {
  return resolve(process.cwd(), '.', dir);
}

const { dependencies, devDependencies, name, version } = pkg;
const __APP_INFO__ = {
  pkg: { dependencies, devDependencies, name, version },
  lastBuildTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
};

export default ({ command, mode }: ConfigEnv): UserConfig => {
  const root = process.cwd();

  const env = loadEnv(mode, root);

  // The boolean type read by loadEnv is a string. This function can be converted to boolean type
  const viteEnv = wrapperEnv(env);

  const { VITE_PORT, VITE_PUBLIC_PATH, VITE_PROXY } = viteEnv;

  const isBuild = command === 'build';

  const serverOptions: Recordable = {}

  // ----- [begin] [JEECG as Qiankun sub-application] -----
  const {VITE_GLOB_QIANKUN_MICRO_APP_NAME, VITE_GLOB_QIANKUN_MICRO_APP_ENTRY} = viteEnv;
  const isQiankunMicro = VITE_GLOB_QIANKUN_MICRO_APP_NAME != null && VITE_GLOB_QIANKUN_MICRO_APP_NAME !== '';
  if (isQiankunMicro && !isBuild) {
    serverOptions.cors = true;
    serverOptions.origin = VITE_GLOB_QIANKUN_MICRO_APP_ENTRY!.split('/').slice(0, 3).join('/');
  }
  // ----- [end] [JEECG as Qiankun sub-application] -----
  
  console.log('[init] Start Port: ', VITE_PORT);
  console.debug('[init] Vite Proxy Config: ', VITE_PROXY);
  
  
  return {
    base: isQiankunMicro ? VITE_GLOB_QIANKUN_MICRO_APP_ENTRY : VITE_PUBLIC_PATH,
    root,
    resolve: {
      alias: [
        {
          find: 'vue-i18n',
          replacement: 'vue-i18n/dist/vue-i18n.cjs.js',
        },
        // /@/xxxx => src/xxxx
        {
          find: /\/@\//,
          replacement: pathResolve('src') + '/',
        },
        // /#/xxxx => types/xxxx
        {
          find: /\/#\//,
          replacement: pathResolve('types') + '/',
        },
        {
          find: /@\//,
          replacement: pathResolve('src') + '/',
        },
        // /#/xxxx => types/xxxx
        {
          find: /#\//,
          replacement: pathResolve('types') + '/',
        },
      ],
    },
    server: {
      // Listening on all local IPs
      host: true,
      // @ts-ignore
      https: false,
      port: VITE_PORT,
      // Load proxy configuration from .env
      proxy: createProxy(VITE_PROXY),
      // Merge server configuration
      ...serverOptions,
    },
    build: {
      minify: 'esbuild',
      target: 'es2015',
      cssTarget: 'chrome80',
      outDir: OUTPUT_DIR,
      rollupOptions: {
        // Turn off tree-shaking optimization to prevent deletion of important code, which may cause functional exceptions after packaging
        treeshake: false,
        output: {
          chunkFileNames: 'js/[name]-[hash].js', // Name of imported files
          entryFileNames: 'js/[name]-[hash].js', // Package entry file name
          // manualChunks configuration (dependency packages arranged from large to small)
          manualChunks: {
            // vue vue-router merge packaging
            'vue-vendor': ['vue', 'vue-router'],
            'antd-vue-vendor': ['ant-design-vue','@ant-design/icons-vue','@ant-design/colors'],
            'vxe-table-vendor': ['vxe-table','vxe-table-plugin-antd','xe-utils'],
            'emoji-mart-vue-fast': ['emoji-mart-vue-fast'],
            'china-area-data-vendor': ['china-area-data']
          },
        },
      },
      // Turn off brotliSize display to slightly reduce packaging time
      reportCompressedSize: false,
      // Increase warning size for oversized static resources
      chunkSizeWarningLimit: 2000,
    },
    esbuild: {
      // Clear global console.log and debug
      drop: isBuild ? ['console', 'debugger'] : [],
    },
    define: {
      // setting vue-i18-next
      // Suppress warning
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(__APP_INFO__),
    },
    css: {
      preprocessorOptions: {
        less: {
          modifyVars: generateModifyVars(),
          javascriptEnabled: true,
        },
      },
    },

    // The vite plugin used by the project. The quantity is large, so it is separately extracted and managed
    // Preload build configuration (first screen performance)
    plugins: createVitePlugins(viteEnv, isBuild, isQiankunMicro),
    optimizeDeps: {
      esbuildOptions: {
        target: 'es2020',
      },
      exclude: [
        // After upgrading to vite4, need to exclude online dependencies
        '@jeecg/online',
        '@jeecg/aiflow',
      ],
    },
  };
};
