// /**
//  * qiankunConfiguration
//  */
// import { registerMicroApps, setDefaultMountApp, start, runAfterFirstMounted, addGlobalUncaughtErrorHandler } from 'qiankun';
// import { apps } from './apps';
// import { getProps, initGlState } from './state';
//
// /**
//  * Refactorapps
//  */
// function filterApps() {
//   apps.forEach((item) => {
//     //Data that the main application needs to pass to the micro application。
//     item.props = getProps();
//     //Routing rules triggered by micro applications
//     // @ts-ignore
//     item.activeRule = genActiveRule('/' + item.activeRule);
//   });
//   return apps;
// }
//
// /**
//  * Route monitoring
//  * @param {*} routerPrefix prefix
//  */
// function genActiveRule(routerPrefix) {
//   return (location) => location.pathname.startsWith(routerPrefix);
// }
//
// /**
//  * Micro application registration
//  */
// function registerApps() {
//   const _apps = filterApps();
//   registerMicroApps(_apps, {
//     beforeLoad: [
//       // @ts-ignore
//       (loadApp) => {
//         console.log('before load', loadApp);
//       },
//     ],
//     beforeMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('before mount', mountApp);
//       },
//     ],
//     afterMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('before mount', mountApp);
//       },
//     ],
//     afterUnmount: [
//       // @ts-ignore
//       (unloadApp) => {
//         console.log('after unload', unloadApp);
//       },
//     ],
//   });
//   // Set default sub-app,and genActiveRuleThe parameters in the
//   // setDefaultMountApp();
//   // First micro application mount The method that needs to be called later，For example, enable some monitoring or hidden scripts。
//   runAfterFirstMounted(() => console.log('Turn on monitoring'));
//   // Add global uncaught exception handler。
//   addGlobalUncaughtErrorHandler((event) => console.log(event));
//   // Define global state
//   initGlState();
//   //start upqiankun
//   start({});
// }
//
// export default registerApps;
