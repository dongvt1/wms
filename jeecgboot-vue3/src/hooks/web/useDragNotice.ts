import { ref, nextTick, getCurrentInstance, watch } from 'vue';
import { getToken } from '/@/utils/auth';
import md5 from 'crypto-js/md5';
import { connectWebSocket, onWebSocket } from '/@/hooks/web/useWebSocket';
import { useGlobSetting } from '/@/hooks/setting';
import { useModal } from '/@/components/Modal';
import { useUserStore } from '/@/store/modules/user';
import { isUrl } from '@/utils/is';
import { getQueryVariable, getUrlParams } from '@/utils';
import { useRouter } from 'vue-router';
import { useMessage } from '@/hooks/web/useMessage';
const { createMessage } = useMessage();
export function useDragNotice() {
  //*********************************websocketConfigurationbegin******************************************
  const glob = useGlobSetting();
  const { push, currentRoute } = useRouter();
  const userStore = useUserStore();
  const instance: any = getCurrentInstance();
  // initialization WebSocket
  function initWebSocket() {
    const token = getToken();
    //will log intokenGenerate a short identifier
    const wsClientId = md5(token);
    // WebSocketDifferent from the protocol used for ordinary requests，wsEquivalent tohttp，wssEquivalent tohttps
    const url = glob.domainUrl?.replace('https://', 'wss://').replace('http://', 'ws://') + '/dragChannelSocket/' + wsClientId;
    connectWebSocket(url);
    onWebSocket(onWebSocketMessage);
  }

  async function onWebSocketMessage(data) {
    console.log('Dashboard listens for button click eventswebsocket', data);
    if (data?.CMD === 'drag') {
      //trigger action： url：path modal：Pop-up window
      const action = data.result.action;
      //Pop-up window类型： 点击按钮打开什么Pop-up window，according totype打开不同的Pop-up window
      const type = data.result.type;
      //urladdress，Can be a route，It can also be an external link
      let url = data.result.url;
      //Pop-up windowparameter或者urlparameter
      const record = data.result.records || {};
      console.log('Dashboard listening click event typetype', type);
      console.log('Dashboard monitors click event actionsaction', action);
      console.log('仪表盘监听点击事件pathurl', url);
      console.log('仪表盘监听点击事件parameter', record);
      //1.path的话，Determine external link or internal route jump
      if (action == 'url') {
        //Special handling for common downloads
        if (url == 'fileUrl') {
          url = record[url];
        }
        const urlParamsObj = getUrlParams(url);
        if (url.startsWith('http')) {
          window.open(url, '_blank');
        } else {
          push({ path: urlParamsObj.url, query: { ...urlParamsObj.params, ...record } });
        }
      } else {
        //2.Pop-up window方式打开项目组件
        switch (type) {
          case 'email':
            //邮箱查看Pop-up window
            handleOpenType('email', { record });
            break;
          default:
            break;
        }
      }
    }
  }
  //*********************************websocketConfigurationend******************************************

  //*********************************打开Pop-up window修改，动态设置Pop-up windowbegin*******************************
  //当前表单Pop-up window
  const currentModal = ref<string | null>(null);
  //当前表单parameter
  const modalParams = ref<Recordable>({});
  //Form registration cache
  const modalRegCache = ref<Recordable>({});
  //组件绑定parameter
  const bindParams = ref<Recordable>({});
  /**
   * according to类型打开不同Pop-up window
   * @param type
   * @param params
   */
  async function handleOpenType(type, params) {
    currentModal.value = null;
    modalParams.value = { ...params };
    switch (type) {
      case 'email':
        //Email check
        currentModal.value = 'EoaMailBoxInModal';
        break;
      default:
        currentModal.value = null;
        break;
    }
    //注册表单Pop-up window
    initModalRegister();
    await nextTick(() => {
      if (modalRegCache.value[currentModal.value!]?.isRegister) {
        console.log('Registered，Go cache');
        modalRegCache.value[currentModal.value!].modalMethods.openModal(true, modalParams.value);
      }
    });
  }
  /**
   * initializationPop-up window注册
   */
  function initModalRegister() {
    //If the current selection form isnull，Don't deal with it
    if (!currentModal.value) {
      return;
    }
    //Determine whether it exists in the cache，不存在就Go cache逻辑
    if (!modalRegCache.value[currentModal.value]) {
      const [registerModal, modalMethods] = useModal();
      modalRegCache.value[currentModal.value] = {
        isRegister: false,
        register: bindRegisterModal(registerModal, modalMethods),
        modalMethods,
      };
    }
  }

  /**
   * 绑定注册Pop-up window
   * @param regFn
   * @param modalMethod
   */
  function bindRegisterModal(regFn, modalMethod) {
    return async (...args) => {
      console.log('Start registration：', currentModal.value);
      await regFn(...args);
      console.log('Registration completed：', currentModal.value);
      //打开Pop-up window
      modalMethod.openModal(true, modalParams.value);
      //Set cache flag
      modalRegCache.value[currentModal.value!].isRegister = true;
    };
  }
  //*********************************打开Pop-up window修改，动态设置Pop-up windowend******************************************
  //refresh page
  function reloadPage() {
    const iframes: any = document.getElementsByClassName('jeecg-iframe-page__main');
    // Will HTMLCollection Convert to array
    const iframeArray = Array.from(iframes);
    if (currentRoute.value?.meta?.frameSrc && currentRoute.value?.meta?.frameSrc.indexOf('/drag/view?pageId=') >= 0) {
      const targetIframe: any = iframeArray.find((iframe: any) => iframe.src == currentRoute.value?.meta?.frameSrc);
      console.log('targetIframe', targetIframe);
      if (targetIframe) {
        targetIframe.contentWindow.postMessage({ reload: true }, '*');
      }
    }
  }
  return {
    initDragWebSocket: initWebSocket,
    handleOpenType,
    currentModal,
    modalParams,
    modalRegCache,
    bindParams,
    reloadPage,
  };
}
