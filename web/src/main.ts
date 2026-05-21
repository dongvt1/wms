import type { MainAppProps } from "#/main";
import 'uno.css';
import '/@/design/index.less';
import 'ant-design-vue/dist/reset.css';
// Registration icon
import 'virtual:svg-icons-register';

import App from './App.vue';
import { createApp } from 'vue';
import { initAppConfigStore } from '/@/logics/initAppConfig';
import { setupErrorHandle } from '/@/logics/error-handle';
import { router, createRouter, setupRouter } from '/@/router';
import { setupRouterGuard } from '/@/router/guard';
import { setupStore } from '/@/store';
import { setupGlobDirectives } from '/@/directives';
import { setupI18n } from '/@/locales/setupI18n';
import { setupElectron } from "@/electron";
import { registerGlobComp } from '/@/components/registerGlobComp';
import { registerThirdComp } from '/@/settings/registerThirdComp';
import { registerSuper } from '/@/views/super/registerSuper';
import { useSso } from '/@/hooks/web/useSso';
import { checkIsQiankunMicro } from "/@/qiankun/micro";
import { autoUseQiankunMicro } from "/@/qiankun/micro/qiankunMicro";
import { useAppStoreWithOut } from "@/store/modules/app";

// registeronlinemodulelib
import { registerPackages } from '/@/utils/monorepo/registerPackages';

// Program entrance
async function main() {
  if (checkIsQiankunMicro()) {
    // 【JEECGAs Qiankunzi application】Start in Qiankunzi application mode
    // await autoUseQiankunMicro(bootstrap)
    await autoUseQiankunMicro(bootstrap)
  } else {
    // Get parameters
    const props = getMainAppProps();
    // Normal startup
    await bootstrap(props)
  }
}

main();

async function bootstrap(props?: MainAppProps) {
  // Create application instance
  const app = createApp(App);
  // 【QQYUN-6329】
  window['JAppRootInstance'] = app;

  // Create route
  createRouter();

  // Configuration storage
  setupStore(app);

  // Configuration parameters
  setupProps(props);

  // Multi-language configuration,Asynchronous situation:Language files can be obtained from the server side
  await setupI18n(app);

  // Initialize internal system configuration
  initAppConfigStore();

  // register外部module路由(registeronlinemodulelib)
  registerPackages(app);

  // register全局组件
  registerGlobComp(app);

  //CASSingle sign-on
  await useSso().ssoLogin();

  // registersuperapplication routing
  await registerSuper(app);
  
  // Configure routing
  setupRouter(app);

  // Route protection
  setupRouterGuard(router);

  // register全局指令
  setupGlobDirectives(app);

  // Configure global error handling
  setupErrorHandle(app);

  // register第三方组件
  await registerThirdComp(app);

  // Configurationelectron
  setupElectron(app)

  // Execute the mount when the route is ready( https://next.router.vuejs.org/api/#isready)
  await router.isReady();

  // Mount application
  app.mount(getMountContainer(props), true);

  console.log(" vue3 app Loading completed！")

  return app
}

// Get application mounting container
function getMountContainer(props?: MainAppProps) {
  const id = '#app';
  if (!props?.container?.querySelector) {
    return id;
  }
  return props.container.querySelector(id) ?? id;
}

// Get main application parameters
function getMainAppProps(): MainAppProps {
  // from queryString Get in
  const searchParams = new URLSearchParams(window.location.search);
  // Hide sidebar（menu）
  let hideSider = searchParams.get('hideSider') === 'true';
  // hide top
  let hideHeader = searchParams.get('hideHeader') === 'true';
  // hide manyTab switch
  let hideMultiTabs = searchParams.get('hideMultiTabs') === 'true';

  return {
    hideSider,
    hideHeader,
    hideMultiTabs
  }
}

// Configuration主应用参数
function setupProps(props?: MainAppProps) {
  if (!props) {
    return
  }
  const appStore = useAppStoreWithOut();
  appStore.setMainAppProps(props);
}
