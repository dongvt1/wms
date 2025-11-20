<template>
  <div :class="[`${prefixCls}`]">
    <div class="my-account">third partyAPP</div>
<!--    <div class="account-row-item">-->
<!--      <div class="account-label gray-75">Enterprise WeChat binding</div>-->
<!--      <span>-->
<!--        <icon-font :style="!bindEnterpriseData.sysUserId ? { color: '#9e9e9e' } : { color: '#0082EF' }" class="item-icon" type="icon-qiyeweixin3" />-->
<!--        <span class="gray-75" style="margin-left: 12px">Enterprise WeChat</span>-->
<!--        <span class="gray-75" style="margin-left: 8px" v-if="bindEnterpriseData.realname">{{ 'Bound：' + bindEnterpriseData.realname }}</span>-->
<!--        <span class="blue-e5 pointer" style="margin-left: 24px" @click="wechatEnterpriseBind">{{-->
<!--          !bindEnterpriseData.sysUserId ? 'binding' : 'unbundle'-->
<!--        }}</span>-->
<!--      </span>-->
<!--    </div>-->
    <div class="account-row-item">
      <div class="account-label gray-75">DingTalkbinding</div>
      <span>
        <DingtalkCircleFilled :style="!bindDingData.sysUserId ? { color: '#9e9e9e' } : { color: '#007FFF' }" class="item-icon" />
        <span class="gray-75" style="margin-left: 12px">DingTalk</span>
        <span class="gray-75" style="margin-left: 8px" v-if="bindDingData.realname">{{ 'Bound：' + bindDingData.realname }}</span>
        <span class="blue-e5 pointer" style="margin-left: 24px" @click="dingDingBind">{{ !bindDingData.sysUserId ? 'binding' : 'unbundle' }}</span>
      </span>
    </div>
    <div class="account-row-item">
      <div class="account-label gray-75">账号binding</div>
      <span>
        <WechatFilled :style="!bindWechatData.sysUserId ? { color: '#9e9e9e' } : { color: '#1ec563' }" class="item-icon" />
        <span class="gray-75" style="margin-left: 12px">WeChat</span>
        <span class="gray-75" style="margin-left: 8px" v-if="bindWechatData.realname">{{ 'Bound：' + bindWechatData.realname }}</span>
        <span class="blue-e5 pointer" style="margin-left: 24px" @click="wechatBind">{{ !bindWechatData.sysUserId ? 'binding' : 'unbundle' }}</span>
      </span>
    </div>
  </div>
</template>
<script lang="ts" setup name="we-chat-ding-setting">
  import { onMounted, ref, reactive, unref } from 'vue';
  import { CollapseContainer } from '/@/components/Container';
  import { bindThirdAppAccount, deleteThirdAccount, getThirdAccountByUserId } from './UserSetting.api';
  import { useUserStore } from '/@/store/modules/user';
  import { useModal } from '/@/components/Modal';
  import { DingtalkCircleFilled, createFromIconfontCN, WechatFilled } from '@ant-design/icons-vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Modal } from 'ant-design-vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-user-tenant-setting-container');

  const IconFont = createFromIconfontCN({
    scriptUrl: '//at.alicdn.com/t/font_2316098_umqusozousr.js',
  });
  const userStore = useUserStore();

  //bindingWeChat的数据
  const bindWechatData = ref<any>({});
  //bindingDingTalk的数据
  const bindDingData = ref<any>({});
  //bindingEnterprise WeChat的数据
  const bindEnterpriseData = ref<any>({});

  const glob = useGlobSetting();
  //third party类型
  const thirdType = ref('');
  //third party用户UUID
  const thirdUserUuid = ref('');
  //third party详情
  const thirdDetail = ref<any>({});
  const { createMessage } = useMessage();
  //windowsobject，Used to close window event
  const windowsIndex = ref<any>('');
  //Window listening events
  const receiveMessage = ref<any>('');
  
  /**
   * 初始化DingTalk和Enterprise WeChat数据
   */
  async function initUserDetail() {
    let values = await getThirdAccountByUserId({ thirdType: 'wechat_open,dingtalk,wechat_enterprise' });
    bindWechatData.value = "";
    bindDingData.value = "";
    bindEnterpriseData.value = "";
    if (values && values.result) {
      let result = values.result;
      for (let i = 0; i < result.length; i++) {
        setThirdDetail(result[i]);
      }
    }
  }

  /**
   * Enterprise WeChat bindingunbundle事件
   */
  function wechatEnterpriseBind() {
    console.log('Enterprise WeChat bindingunbundle事件');
    let data = unref(bindEnterpriseData);
    if (!data.sysUserId) {
      onThirdLogin('wechat_enterprise');
    }else{
      deleteAccount({ sysUserId: data.sysUserId, id: data.id }, 'Enterprise WeChat');
    }
  }

  /**
   * DingTalkbindingunbundle事件
   */
  function dingDingBind() {
    let data = unref(bindDingData);
    if (!data.sysUserId) {
      onThirdLogin('dingtalk');
    } else {
      deleteAccount({ sysUserId: data.sysUserId, id: data.id }, 'DingTalk');
    }
    console.log('DingTalkbindingunbundle事件');
  }

  /**
   * WeChatbinding
   */
  function wechatBind() {
    let data = unref(bindWechatData);
    if (!data.sysUserId) {
      onThirdLogin('wechat_open');
    }else{
      deleteAccount({ sysUserId: data.sysUserId, id: data.id }, 'WeChat');
    }
  }

  /**
   * third party登录
   * @param source
   */
  function onThirdLogin(source) {
    let url = `${glob.uploadUrl}/sys/thirdLogin/render/${source}`;
    //The window is not empty and closed
    console.log("unref(windowsIndex) ::",unref(windowsIndex))
    if(unref(windowsIndex)){
      //Make sure there is only one window
      windowsIndex.value.close();
      //Make sure there is only one listener
      window.removeEventListener('message', unref(receiveMessage),false);
    }
    
    windowsIndex.value = window.open(
      url,
      `login ${source}`,
      'height=500, width=500, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no'
    );
    thirdType.value = source;
    receiveMessage.value = async function (event) {
      let token = event.data;
      if (typeof token === 'string') {
        //If it is a string type The explanation istokeninformation
        if (token === 'Login failed') {
          cmsFailed();
        } else if (token.includes('binding手机号')) {
          let strings = token.split(',');
          thirdUserUuid.value = strings[1];
          await bindThirdAccount();
        }else{
          if(token){
            createMessage.warning('该敲敲云账号已被其它third party账号binding,请unbundle或binding其它敲敲云账号');
          }
        }
      } else {
        cmsFailed();
      }
      window.removeEventListener('message', unref(receiveMessage),false);
      windowsIndex.value = "";
    };
    window.addEventListener('message', unref(receiveMessage), false);
  }

  /**
   * binding当前用户
   */
  async function bindThirdAccount() {
    if (!unref(thirdUserUuid)) {
      cmsFailed();
      return;
    }
    let params = { thirdUserUuid: unref(thirdUserUuid), thirdType: unref(thirdType) };
    await bindThirdAppAccount(params)
      .then((res) => {
        if (res.success) {
          if (res.result) {
            setThirdDetail(res.result);
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .catch((res) => {
        createMessage.warning(res.message);
      });
  }

  /**
   * 失败提示information
   */
  function cmsFailed() {
    createMessage.warning('third party账号binding异常');
    return;
  }

  /**
   * 设置third party数据
   * @param value
   */
  function setThirdDetail(value) {
    let type = value.thirdType;
    if (type == 'wechat_open') {
      bindWechatData.value = value;
    } else if (type == 'dingtalk') {
      bindDingData.value = value;
    } else if (type == 'wechat_enterprise') {
      bindEnterpriseData.value = value;
    }
  }

  /**
   * 删除third partyinformation表
   * @param params
   */
  async function deleteAccount(params, text) {
    Modal.confirm({
      title: 'unbundle' + text,
      content: '确定要unbundle吗',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: async () => {
        await deleteThirdAccount(params).then((res) =>{
          if(res.success){
            initUserDetail();
            createMessage.success(res.message)
          }else{
            createMessage.warning(res.message)
          }
        });
      },
    });
  }

  onMounted(() => {
    initUserDetail();
  });
</script>
<style lang="less">
// update-begin-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
@prefix-cls: ~'@{namespace}-j-user-tenant-setting-container';
.@{prefix-cls} {
   padding: 30px 40px 0 20px;
  .account-row-item {
    align-items: center;
    /*begin Compatible with dark night mode*/
    border-bottom: 1px solid @border-color-base;
    /*end Compatible with dark night mode*/
    box-sizing: border-box;
    display: flex;
    height: 71px;
    position: relative;
  }

  .account-label {
    text-align: left;
    width: 160px;
  }

  .gray-75 {
    /*begin Compatible with dark night mode*/
    color: @text-color !important;
    /*end Compatible with dark night mode*/
  }

  .pointer {
    cursor: pointer;
  }

  .blue-e5 {
    color: #1e88e5;
  }

  .phone-margin {
    margin-left: 24px;
    margin-right: 24px;
  }

  .clearfix:after {
    clear: both;
  }

  .clearfix:before {
    content: '';
    display: table;
  }

  .my-account {
    font-size: 17px;
    font-weight: 700 !important;
    /*begin Compatible with dark night mode*/
    color: @text-color !important;
    /*end Compatible with dark night mode*/
    margin-bottom: 20px;
  }
  .item-icon {
    font-size: 16px !important;
  }
}
// update-end-author:liusq date:20230625 for: [issues/563]The dark theme is partially disabled
</style>
