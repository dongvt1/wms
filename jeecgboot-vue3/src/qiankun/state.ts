// /**
//  *public data
//  */
// import { initGlobalState } from 'qiankun';
// import { store } from '/@/store';
// import { router } from '/@/router';
// import { getToken } from '/@/utils/auth';
// //Define the data passed into the child application
// export function getProps() {
//   return {
//     data: {
//       publicPath: '/',
//       token: getToken(),
//       store,
//       router,
//     },
//   };
// }
//
// /**
//  * Define global state，and returns the communication method,Used in main application，Microapp passes props Get communication method。
//  * @param state 主应用穿的public data
//  */
// export function initGlState(info = { userName: 'admin' }) {
//   // initializationstate
//   const actions = initGlobalState(info);
//   // set new value
//   actions.setGlobalState(info);
//   // register observer function - response globalState change，exist globalState Triggered when a change occurs observer function。
//   actions.onGlobalStateChange((newState, prev) => {
//     // state: Changed status; prev Status before change
//     console.info('newState', newState);
//     console.info('prev', prev);
//     for (const key in newState) {
//       console.info('onGlobalStateChange', key);
//     }
//   });
// }
