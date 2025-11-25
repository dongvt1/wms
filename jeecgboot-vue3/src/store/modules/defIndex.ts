import {store} from '/@/store';
import {defineStore} from 'pinia';
import {defHttp} from "@/utils/http/axios";

interface DefIndexState {
  // front pageurl
  url: string,
  // front page组件
  component: string
}

export const useDefIndexStore = defineStore({
  id: 'defIndex',
  state: (): DefIndexState => ({
    url: '',
    component: '',
  }),
  getters: {},
  actions: {
    /**
     * Query the default homepage configuration
     */
    async query() {
      const config = await defIndexApi.query();
      this.url = config.url;
      this.component = config.component;
    },
    /**
     * Update default home page configuration
     * @param url front pageurl
     * @param component front page组件
     * @param isRoute Is it a route?
     */
    async update(url: string, component: string, isRoute: boolean) {
      await defIndexApi.update(url, component, isRoute);
      await this.query()
    },

    check(url: string) {
      return url === this.url;
    }
  }
});

// Need to be used outside the setup
export function useDefIndexStoreWithOut() {
  return useDefIndexStore(store);
}

/**
 * 默认front page配置API
 */
export const defIndexApi = {
  /**
   * 查询默认front page配置
   */
  async query() {
    const url = '/sys/sysRoleIndex/queryDefIndex'
    return await defHttp.get({url});
  },
  /**
   * 更新默认front page配置
   * @param url front pageurl
   * @param component front page组件
   * @param isRoute Is it a route?
   */
  async update(url: string, component: string, isRoute: boolean) {
    let apiUrl = '/sys/sysRoleIndex/updateDefIndex'
    apiUrl += '?url=' + url
    //update-begin-author:liusq---date:2025-07-04--for: 设置默认front page接口传参修改,IncreaseencodeURIComponent，prevent{{ window._CONFIG['domianURL'] }}/**Can't save
    apiUrl += '&component=' + encodeURIComponent(component)
    //update-end-author:liusq---date:2025-07-04--for: 设置默认front page接口传参修改,IncreaseencodeURIComponent，prevent{{ window._CONFIG['domianURL'] }}/**Can't save
    apiUrl += '&isRoute=' + isRoute
    return await defHttp.put({url: apiUrl});
  },

}
