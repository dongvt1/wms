import {qiankunWindow} from 'vite-plugin-qiankun/dist/helper'

/**
 * 【JEECGAs Qiankunzi application】【Determine whether the current Qiankunzi application mode is running】
 */
export function checkIsQiankunMicro(): boolean {
  return !!qiankunWindow.__POWERED_BY_QIANKUN__;
}

export function getGlobal() {
  return (checkIsQiankunMicro() ? qiankunWindow : window) as Window
}
