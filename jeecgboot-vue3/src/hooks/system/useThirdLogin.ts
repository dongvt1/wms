import { ref, unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStore } from '/@/store/modules/user';
import { setThirdCaptcha, getCaptcha } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';

export function useThirdLogin() {
  const { createMessage, notification } = useMessage();
  const { t } = useI18n();
  const glob = useGlobSetting();
  const userStore = useUserStore();
  //Third party type
  const thirdType = ref('');
  //Third-party login related information
  const thirdLoginInfo = ref<any>({});
  //state
  const thirdLoginState = ref(false);
  //Bind mobile phone number pop-up window
  const bindingPhoneModal = ref(false);
  //third party usersUUID
  const thirdUserUuid = ref('');
  //Prompt window
  const thirdConfirmShow = ref(false);
  //Bind password pop-up window
  const thirdPasswordShow = ref(false);
  //Bind password
  const thirdLoginPassword = ref('');
  //Bind user
  const thirdLoginUser = ref('');
  //loading
  const thirdCreateUserLoding = ref(false);
  //Bind mobile phone number
  const thirdPhone = ref('');
  //Verification code
  const thirdCaptcha = ref('');
  //Third party login
  function onThirdLogin(source) {
    let url = `${glob.uploadUrl}/sys/thirdLogin/render/${source}`;
    const openWin = window.open(
      url,
      `login ${source}`,
      'height=500, width=500, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no'
    );
    thirdType.value = source;
    thirdLoginInfo.value = {};
    thirdLoginState.value = false;
    let receiveMessage = function (event) {
      let token = event.data;
      if (typeof token === 'string') {
        //If it is a string type The explanation istokeninformation
        if (token === 'Login failed') {
          createMessage.warning(token);
        } else if (token.includes('Bind mobile phone number')) {
          bindingPhoneModal.value = true;
          let strings = token.split(',');
          thirdUserUuid.value = strings[1];
        } else {
          doThirdLogin(token);
        }
      } else if (typeof token === 'object') {
        //Object type Explain that you need to be prompted whether to bind an existing account
        if (token['isObj'] === true) {
          thirdConfirmShow.value = true;
          thirdLoginInfo.value = { ...token };
        }
      } else {
        createMessage.warning('不识别的information传递');
      }
      // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-1827】mac系统谷歌浏览器企业微信Third party login成功后没有弹出绑定手机弹窗
      if (openWin?.closed) {
        window.removeEventListener('message', receiveMessage, false);
      }
      // update-end--author:liaozhiyang---date:20240717---for：【TV360X-1827】mac系统谷歌浏览器企业微信Third party login成功后没有弹出绑定手机弹窗
    };
    // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-1827】mac系统谷歌浏览器企业微信Third party login成功后没有弹出绑定手机弹窗
    window.removeEventListener('message', receiveMessage, false);
    // update-end--author:liaozhiyang---date:20240717---for：【TV360X-1827】mac系统谷歌浏览器企业微信Third party login成功后没有弹出绑定手机弹窗
    window.addEventListener('message', receiveMessage, false);
  }
  // according totokenExecute login
  function doThirdLogin(token) {
    if (unref(thirdLoginState) === false) {
      thirdLoginState.value = true;
      userStore.ThirdLogin({ token, thirdType: unref(thirdType) }).then((res) => {
        console.log('res====>doThirdLogin', res);
        if (res && res.userInfo) {
          notification.success({
            message: t('sys.login.loginSuccessTitle'),
            description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
            duration: 3,
          });
        } else {
          requestFailed(res);
        }
      });
    }
  }

  function requestFailed(err) {
    notification.error({
      message: 'Login failed',
      description: ((err.response || {}).data || {}).message || err.message || 'An error occurred with the request，Please try again later',
      duration: 4,
    });
  }
  // Bind existing account Password required
  function thirdLoginUserBind() {
    thirdLoginPassword.value = '';
    thirdLoginUser.value = thirdLoginInfo.value.uuid;
    thirdConfirmShow.value = false;
    thirdPasswordShow.value = true;
  }
  //Create new account
  function thirdLoginUserCreate() {
    thirdCreateUserLoding.value = true;
    // Add two random numbers after the account name
    thirdLoginInfo.value.suffix = parseInt(Math.random() * 98 + 1);
    defHttp
      .post({ url: '/sys/third/user/create', params: { thirdLoginInfo: unref(thirdLoginInfo) } }, { isTransformResponse: false })
      .then((res) => {
        if (res.success) {
          let token = res.result;
          doThirdLogin(token);
          thirdConfirmShow.value = false;
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        thirdCreateUserLoding.value = false;
      });
  }
  // Verify password
  function thirdLoginCheckPassword() {
    let params = Object.assign({}, unref(thirdLoginInfo), { password: unref(thirdLoginPassword) });
    defHttp.post({ url: '/sys/third/user/checkPassword', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        thirdLoginNoPassword();
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    });
  }
  // no password Cancel operation
  function thirdLoginNoPassword() {
    thirdPasswordShow.value = false;
    thirdLoginPassword.value = '';
    thirdLoginUser.value = '';
  }

  //Countdown function before execution
  function sendCodeApi() {
    //return setThirdCaptcha({mobile:unref(thirdPhone)});
    return getCaptcha({ mobile: unref(thirdPhone), smsmode: '0' });
  }
  //Bind mobile phone number点击确定按钮
  function thirdHandleOk() {
    if (!unref(thirdPhone)) {
      cmsFailed('Please enter mobile phone number');
    }
    if (!unref(thirdCaptcha)) {
      cmsFailed('请输入Verification code');
    }
    let params = {
      mobile: unref(thirdPhone),
      captcha: unref(thirdCaptcha),
      thirdUserUuid: unref(thirdUserUuid),
    };
    defHttp.post({ url: '/sys/thirdLogin/bindingThirdPhone', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        bindingPhoneModal.value = false;
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    });
  }
  function cmsFailed(err) {
    notification.error({
      message: 'Login failed',
      description: err,
      duration: 4,
    });
    return;
  }
  //Return data and methods
  return {
    thirdPasswordShow,
    thirdLoginCheckPassword,
    thirdLoginNoPassword,
    thirdLoginPassword,
    thirdConfirmShow,
    thirdCreateUserLoding,
    thirdLoginUserCreate,
    thirdLoginUserBind,
    bindingPhoneModal,
    thirdHandleOk,
    thirdPhone,
    thirdCaptcha,
    onThirdLogin,
    sendCodeApi,
  };
}
