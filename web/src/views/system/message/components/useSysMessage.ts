import { ref, reactive, nextTick } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { getDictItemsByCode } from '/@/utils/dict/index';
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '/@/store/modules/app';
import { useTabs } from '/@/hooks/web/useTabs';
import { useModal } from '/@/components/Modal';
import {useMessage} from "/@/hooks/web/useMessage";

/**
 * List interface
 * @param params
 */
const queryMessageList = (params) => {
  const url = '/sys/annountCement/vue3List';
  return defHttp.get({ url, params });
};

/**
 * Get message list data
 *
 * setLocaleText Set unread messages
 */
export function useSysMessage(setLocaleText) {
  const { createMessage } = useMessage();
  const rangeDateArray = getDictItemsByCode('rangeDate');
  console.log('+++++++++++++++++++++');
  console.log('rangeDateArray', rangeDateArray);
  console.log('+++++++++++++++++++++');

  const messageList = ref<any[]>([]);
  const pageNo = ref(1)
  let pageSize = 10;

  const searchParams = reactive({
    fromUser: '',
    rangeDateKey: '',
    rangeDate: [],
    starFlag: '',
    noticeType: ''
  });


  function getQueryParams() {
    let { fromUser, rangeDateKey, rangeDate, starFlag, noticeType } = searchParams;
    let params = {
      fromUser,
      starFlag,
      rangeDateKey,
      beginDate: '',
      endDate: '',
      pageNo: pageNo.value,
      pageSize,
      noticeType
    };
    if (rangeDateKey == 'zdy') {
      params.beginDate = rangeDate[0]+' 00:00:00';
      params.endDate = rangeDate[1]+' 23:59:59';
    }
    return params;
  }

  // Is the data loaded?
  const loadEndStatus = ref(false);

  //Request data
  async function loadData() {
    if(loadEndStatus.value === true){
      return;
    }
    let params = getQueryParams();
    const data = await queryMessageList(params);
    console.log('Get results', data);
    if(!data || data.length<=0){
      loadEndStatus.value = true;
      setLocaleText();
      return;
    }
    if(data.length<pageSize){
      loadEndStatus.value = true;
    }
    pageNo.value = pageNo.value+1
    let temp:any[] = messageList.value;
    temp.push(...data);
    messageList.value = temp;
    setLocaleText();
  }

  //reset
  function reset(){
    messageList.value = []
    pageNo.value = 1;
    loadEndStatus.value = false;
  }

  //mark star
  async function updateStarMessage(item){
    const url = '/sys/sysAnnouncementSend/edit';
    let starFlag = '1';
    if(item.starFlag==starFlag){
      starFlag = '0'
    }
    const params = {
      starFlag,
      id: item.sendId
    }
    //update-begin-author:taoyan date:2023-3-6 for: QQYUN-4491【application】some minor issues  4、mark star不需要提示吧
    const data:any = await defHttp.put({url, params}, {isTransformResponse: false});
    if(data.success === true){
    }else{
      createMessage.warning(data.message)
    }
    //update-end-author:taoyan date:2023-3-6 for: QQYUN-4491【application】some minor issues  4、mark star不需要提示吧
  }


  const loadingMoreStatus = ref(false);
  async function onLoadMore() {
    loadingMoreStatus.value = true;
    await loadData();
    loadingMoreStatus.value = false;
  }

  function noRead(item) {
    if (item.readFlag === '1') {
      return false;
    }
    return true;
  }

  // Message type
  function getMsgCategory(item) {
    if(item.busType=='email'){
      return 'Email reminder:';
    } else if(item.busType=='bpm'){
      return 'Process reminder:';
    } else if(item.busType=='bpm_cc'){
      return 'process copy:';
    }else if(item.busType=='bpm_task'){
      return 'process tasks:';
    } else if (item.msgCategory == '2') {
      return 'System messages:';
    } else if (item.msgCategory == '1') {
      return 'Notices and Announcements:';
    }
    return '';
  }

  // QQYUN-4472 There is a message but no reminder--View details and change to process
  function getHrefText(item) {
    if(item.busType === 'bpm'|| item.busType === 'bpm_task' || item.busType === 'tenant_invite'){
      //Determine whether to view details
      if (item.msgAbstract) {
        try {
          const json = JSON.parse(item.msgAbstract);
          if (json.taskDetail) {
            return 'check the details';
          }
        } catch (e) {
          console.error('getHrefText:msgAbstractParameters are notJSONFormat', item.msgAbstract);
        }
      }
      return 'to deal with'
    } else {
      return 'check the details'
    }
  }

  return {
    messageList,
    reset,
    loadData,
    loadEndStatus,
    searchParams,
    updateStarMessage,
    onLoadMore,
    noRead,
    getMsgCategory,
    getHrefText

  };
}

/**
 * Used for message jump
 */
export function useMessageHref(emit, props){
  //const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();
  //const [registerTaskModal, { openModal: openTaskModal }] = useModal();
  // Registration form pop-up window
  //const [registerDesignFormModal, { openModal: openDesignFormModal }] = useModal();
  const messageHrefArray: any[] = getDictItemsByCode('messageHref');
  const router = useRouter();
  const appStore = useAppStore();
  const rt = useRoute();
  const { close: closeTab, closeSameRoute } = useTabs();

  //*********************************[QQYUN-6713]System notification opens pop-up window for modification，Dynamically set pop-up windowsbegin******************************************
  //Current form pop-up window
  const currentModal = ref<string | null>(null);
  //Current form parameters
  const modalParams = ref<Recordable>({});
  //Form registration cache
  const modalRegCache = ref<Recordable>({});
  //Component binding parameters
  const bindParams = ref<Recordable>({});

  /**
   * Open different pop-up windows based on type
   * @param type
   * @param params
   */
  async function handleOpenType(type, params) {
    currentModal.value = null;
    modalParams.value = { ...params };
    switch (type) {
      case 'task':
        //Process handling
        bindParams.value = { actionType: 'todo' };
        currentModal.value = 'ProcessTaskHandleModal';
        break;
      case 'history':
        bindParams.value = {};
        //historical process
        currentModal.value = 'MyTaskHandleModal';
        break;
      case 'design':
        //form design
        currentModal.value = 'DesformViewModal';
        bindParams.value = {
          showRecordCopy: false,
          showRecordShare: false,
          showRecordSysPrint: false,
          showDesignFormBtn: false,
        };
        break;
      case 'cgform':
        //Onlineform
        currentModal.value = 'OnlineAutoModal';
        bindParams.value = {
          id: params.formId,
        }
        break;
      default:
        currentModal.value = null;
        break;
    }
    //Registration form pop-up window
    initModalRegister();
    await nextTick(() => {
      if (modalRegCache.value[currentModal.value!]?.isRegister) {
        console.log('Registered，Go cache');
        modalRegCache.value[currentModal.value!].modalMethods.openModal(true, modalParams.value);
      }
    });
  }

  /**
   * Initialize pop-up window registration
   */
  function initModalRegister() {
    //如果当前选择form为null，Don't deal with it
    if (!currentModal.value) {
      return;
    }
    //Determine whether it exists in the cache，不存exist就Go cache逻辑
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
   * Bind registration pop-up window
   * @param regFn
   * @param modalMethod
   */
  function bindRegisterModal(regFn, modalMethod) {
    return async (...args) => {
      console.log('Start registration：', currentModal.value);
      await regFn(...args);
      console.log('Registration completed：', currentModal.value);
      //Open pop-up window
      modalMethod.openModal(true, modalParams.value);
      //Set cache flag
      modalRegCache.value[currentModal.value!].isRegister = true;
    };
  }
  //*************************************[QQYUN-6713]System notification opens pop-up window for modification，Dynamically set pop-up windowsend*********************************************
  // const defaultPath = '/monitor/mynews';
  //const bpmPath = '/task/handle/'

  async function goPage(record, openModalFun?){
    if(!record.busType || record.busType == 'msg_node'){
      if(!openModalFun){
        // Jump from the message notification on the home page
        await goPageFromOuter(record);
      }else{
        // Click on the details from the message page list to view Open directlymodal
        openModalFun()
      }
      // update-begin-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，对方看不到是哪个form@of，no hyperlink
    }else if(record.busType == 'comment'){
      // de
      let msgAbstract = record.msgAbstract;
      if(msgAbstract){
        try {
          let data = JSON.parse(msgAbstract.toString());
          if(data.type == 'designForm'){
            showDesignFormModal(data);
          } else {
            showOnlineCgformModal(data);
          }
        }catch (e) {
          console.error('Open评论form，butmsgAbstractParameters are notJSONFormat', msgAbstract)
          if(openModalFun){
            openModalFun();
          }
        }
      }
      // update-end-author:taoyan date:2023-5-10 for: QQYUN-4744【System notification】6、System notification@queen of people，对方看不到是哪个form@of，no hyperlink
    }else if(record.busType == 'tenant_invite'){
      if(props.isLowApp===true){
        router.push({ name:"myapps-settings-user", query:{ page:'tenantSetting' }})
      }else{
        router.push({ name:"system-usersetting", query:{ page:'tenantSetting' }})
      }
    }else{
      if(props && props.isLowApp===true){
        openLowAppFlowModal(record)
      }else{
        await goPageWithBusType(record)
      }
    }
/*    busId: "1562035005173587970"
    busType: "email"
    openPage: "modules/eoa/email/modals/EoaEmailInForm"
    openType: "component"*/
  }

  /**
   * Openform design器 formPop-up window
   * @param data
   */
  function showDesignFormModal(data) {
    handleOpenType('design', {
      mode: 'detail',
      desformCode: data.code,
      dataId: data.dataId,
      isOnline: false,
    });
  }

  /**
   * OpenOnlineform Pop-up window
   * @param data
   */
  function showOnlineCgformModal(data) {
    handleOpenType('cgform', {
      formId: data.formId,
      isUpdate: true,
      disableSubmit: true,
      record: {
        id: data.dataId,
      },
    });
  }

  /**
   * 判断是不是formof评论消息
   * @param record
   */
  function isFormComment(record) {
    if(record.busType == 'comment'){
      let msgAbstract = record.msgAbstract;
      if(msgAbstract){
        try {
          let data = JSON.parse(msgAbstract);
          if(['cgform', 'designForm'].includes(data.type)){
            return true
          }
        }catch (e) {
          console.error('Open评论form，butmsgAbstractParameters are notJSONFormat', msgAbstract)
        }
      }
    }
    return false
  }

  /**
   * If it is a workflow task existlowAppmiddle Open directlymodal
   */
  function openLowAppFlowModal(record){
    const { busType, busId, msgAbstract } = record;
    let temp = messageHrefArray.filter(item=>item.value === busType);
    if(!temp || temp.length==0){
      console.error('The current business type is not recognized', busType);
      return;
    }
    if(busType.indexOf('bpm')<0){
      console.error('low-appJump mailbox is not supported', busType);
      return;
    }
    //Fixed parameters detailId 用于查询form数据
    let query:any = {
      detailId: busId
    };
    // Additional parameter handling
    if(msgAbstract){
      try {
        let json = JSON.parse(msgAbstract);
        Object.keys(json).map(k=>{
          query[k] = json[k]
        });
      }catch (e) {
        console.error('msgAbstractParameters are notJSONFormat', msgAbstract)
      }
    }
    console.log("busType = ", busType)
    handleOpenType('task', {
      record: {
        id: busId,
        procInsId: query.procInsId,
        processDefinitionId: query.processDefinitionId,
        isDetail: query.taskDetail || 'bpm_cc' == busType
      }
    })
  }

  /**
   * according tobusTypeDifferent jumps to different pages
   * @param record
   */
  async function goPageWithBusType(record){
    const { busType, busId, msgAbstract } = record;
    let temp = messageHrefArray.filter(item=>item.value === busType);
    if(!temp || temp.length==0){
      console.error('The current business type is not recognized', busType);
      return;
    }
    let path = temp[0].text;
    path = path.replace('{DETAIL_ID}', busId)
    //Fixed parameters detailId 用于查询form数据
    let query:any = {
      detailId: busId
    };
    // Additional parameter handling
    if(msgAbstract){
      try {
        let json = JSON.parse(msgAbstract);
        Object.keys(json).map(k=>{
          query[k] = json[k]
        });
      }catch (e) {
        console.error('msgAbstractParameters are notJSONFormat', msgAbstract)
      }
    }
    if(query.taskDetail){
      // 查看任务详情ofPop-up window
      await showHistory(query.procInsId)
    }else{
      // Jump route
      appStore.setMessageHrefParams(query);
      if(rt.path.indexOf(path)>=0){
        await closeTab();
        await router.replace({ path: path, query:{ time: new Date().getTime() } });
      }else{
        closeSameRoute(path)
        await router.push({ path: path });
      }
    }
  }

  /**
   * Jump from the message notification on the home page消息列表Openmodal
   * @param record
   */
  async function goPageFromOuter(record){
    //No business type defined 直接跳转我of消息页面
    emit('detail', record)
  }

  //===============================================================================================================
  //update-begin-author:taoyan date:2022-12-31 for:   QQYUN-3485 【View process】Make a view page，Non-processing page，Just pass the process instance parameters
  async function showHistory(processInstanceId) {
    let { formData, formUrl } = await getTaskInfoForHistory({ processInstanceId });
    formData['PROCESS_TAB_TYPE'] = 'history';
    handleOpenType('history', {
      formData,
      formUrl,
      title: 'process history',
    });
  }

  const nodeInfoUrl = '/act/process/extActProcessNode/getHisProcessNodeInfo'
  const taskNodeInfo = (params) => defHttp.get({ url: nodeInfoUrl, params });

  async function getTaskInfoForHistory(record) {
    //Query conditions
    let params = { procInstId: record.processInstanceId };
    const result = await taskNodeInfo(params);
    console.log('Get historical task information', result);
    let formData: any = {
      dataId: result.dataId,
      taskId: record.id,
      taskDefKey: record.taskId,
      procInsId: record.processInstanceId,
      tableName: result.tableName,
      vars: result.records,
    };
    let tempFormUrl = result.formUrl;
    console.log('Get流程节点formURL', tempFormUrl);
    //节点配置formURL，VUE组件类型对应of拓展参数
    if (tempFormUrl && tempFormUrl.indexOf('?') != -1 && !isURL(tempFormUrl) && tempFormUrl.indexOf('{{DOMAIN_URL}}') == -1) {
      tempFormUrl = result.formUrl.split('?')[0];
      console.log('Get流程节点formURL（Remove parameters）', tempFormUrl);
      formData.extendUrlParams = getQueryVariable(result.formUrl);
    }
    return {
      formData,
      formUrl: tempFormUrl,
    };
  }

  /**
   * GetURLupper parameters
   * @param url
   */
  function getQueryVariable(url) {
    if (!url) return;

    let t,
      n,
      r,
      i = url.split('?')[1],
      s = {};
    (t = i.split('&')), (r = null), (n = null);
    for (let o in t) {
      let u = t[o].indexOf('=');
      u !== -1 && ((r = t[o].substr(0, u)), (n = t[o].substr(u + 1)), (s[r] = n));
    }
    return s;
  }

  /**
   * URLaddress
   * @param {*} s
   */
  function isURL(s) {
    return /^http[s]?:\/\/.*/.test(s);
  }
  //update-end-author:taoyan date:2022-12-31 for:   QQYUN-3485 【View process】Make a view page，Non-processing page，Just pass the process instance parameters
  //===============================================================================================================

  return {
    goPage,
    isFormComment,
    modalRegCache,
    currentModal,
    bindParams,
  }
}
