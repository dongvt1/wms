// /**
//  *microappapps
//  * @name: microapp名称 - unique
//  * @entry: microapp入口.Required - 通过该地址加载microapp，
//  * @container: microapp挂载节点 - microapp加载完成后将挂载在该节点上
//  * @activeRule: microapp触发的路由规则 - 触发路由规则后将加载该microapp
//  */
// //Sub-application list
// const _apps: object[] = [];
// for (const key in import.meta.env) {
//   if (key.includes('VITE_APP_SUB_')) {
//     const name = key.split('VITE_APP_SUB_')[1];
//     const obj = {
//       name,
//       entry: import.meta.env[key],
//       container: '#content',
//       activeRule: name,
//     };
//     _apps.push(obj);
//   }
// }
// export const apps = _apps;
