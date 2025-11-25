import { defHttp } from '/@/utils/http/axios';
import { LoginParams, LoginResultModel, GetUserInfoModel } from './model/userModel';

import { ErrorMessageMode } from '/#/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { setAuthCache } from '/@/utils/auth';
import { TOKEN_KEY } from '/@/enums/cacheEnum';
import { router } from '/@/router';
import { PageEnum } from '/@/enums/pageEnum';
import { ExceptionEnum } from "@/enums/exceptionEnum";

const { createErrorModal } = useMessage();
enum Api {
  Login = '/sys/login',
  phoneLogin = '/sys/phoneLogin',
  Logout = '/sys/logout',
  GetUserInfo = '/sys/user/getUserInfo',
  // Get system permissions
  // 1、Query the buttons owned by the user/Form access
  // 2、All permissions
  // 3、System safe mode
  GetPermCode = '/sys/permission/getPermCode',
  //Newly added interface for obtaining graphic verification codes
  getInputCode = '/sys/randomImage',
  //Interface for obtaining SMS verification code
  getCaptcha = '/sys/sms',
  //Registration interface
  registerApi = '/sys/user/register',
  //Verify user interface
  checkOnlyUser = '/sys/user/checkOnlyUser',
  //SSOLogin verification
  validateCasLogin = '/sys/cas/client/validateLogin',
  //Verify mobile phone number
  phoneVerify = '/sys/user/phoneVerification',
  //Change password
  passwordChange = '/sys/user/passwordChange',
  //Third party login
  thirdLogin = '/sys/thirdLogin/getLoginUser',
  //Third party login
  getThirdCaptcha = '/sys/thirdSms',
  //Get QR code information
  getLoginQrcode = '/sys/getLoginQrcode',
  //Monitor QR code scanning status
  getQrcodeToken = '/sys/getQrcodeToken',
}

/**
 * @description: user login api
 */
export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.Login,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: user phoneLogin api
 */
export function phoneLoginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.phoneLogin,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: getUserInfo
 */
export function getUserInfo() {
  return defHttp.get<GetUserInfoModel>({ url: Api.GetUserInfo }, {}).catch((e) => {
    // update-begin--author:zyf---date:20220425---for:【VUEN-76】Capture interface timeout exception,Jump to login interface
    // TokenExpired，Jump directly to the login page
    if (e && (e.message.includes('timeout') || e.message.includes('401'))) {
      //接口不通时Jump to login interface
      const userStore = useUserStoreWithOut();
      userStore.setToken('');
      setAuthCache(TOKEN_KEY, null);
      router.push({
        path: PageEnum.BASE_LOGIN,
        query: {
          // Pass in the current route，After successful login, jump to the current route
          redirect: router.currentRoute.value.fullPath,
        }
      });
    }
    // update-end--author:zyf---date:20220425---for:【VUEN-76】Capture interface timeout exception,Jump to login interface
  });
}

export function getPermCode() {
  return defHttp.get({ url: Api.GetPermCode });
}

export function doLogout() {
  return defHttp.get({ url: Api.Logout });
}

export function getCodeInfo(currdatetime) {
  let url = Api.getInputCode + `/${currdatetime}`;
  return defHttp.get({ url: url });
}
/**
 * @description: Get SMS verification code
 */
export function getCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        //update-begin---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】same oneIP，1minutes exceed5SMS，It prompts that a verification code is required---
        if(res.code != ExceptionEnum.PHONE_SMS_FAIL_CODE){
          createErrorModal({ title: 'Error message', content: res.message || 'unknown problem' });
          reject();
        }
        reject(res);
        //update-end---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】same oneIP，1minutes exceed5SMS，It prompts that a verification code is required---
      }
    }).catch((res)=>{
      createErrorModal({ title: 'Error message', content: res.message || 'unknown problem' });
      reject();
    });
  });
}

/**
 * @description: Registration interface
 */
export function register(params) {
  return defHttp.post({ url: Api.registerApi, params }, { isReturnNativeResponse: true });
}

/**
 *Verify that the user exists
 * @param params
 */
export const checkOnlyUser = (params) => defHttp.get({ url: Api.checkOnlyUser, params }, { isTransformResponse: false });
/**
 *Verify mobile phone number码
 * @param params
 */
export const phoneVerify = (params) => defHttp.post({ url: Api.phoneVerify, params }, { isTransformResponse: false });
/**
 *Password change
 * @param params
 */
export const passwordChange = (params) => defHttp.get({ url: Api.passwordChange, params }, { isTransformResponse: false });
/**
 * @description: Third party login
 */
export function thirdLogin(params, mode: ErrorMessageMode = 'modal') {
  //==========begin Third party login/auth2Login requires passing tenantid===========
  let tenantId = "0";
  if(!params.tenantId){
    tenantId = params.tenantId;
  }
  //==========end Third party login/auth2Login requires passing tenantid===========
  return defHttp.get<LoginResultModel>(
    {
      url: `${Api.thirdLogin}/${params.token}/${params.thirdType}/${tenantId}`,
    },
    {
      errorMessageMode: mode,
    }
  );
}
/**
 * @description: Get third-party SMS verification code
 */
export function setThirdCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getThirdCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        createErrorModal({ title: 'Error message', content: res.message || 'unknown problem' });
        reject();
      }
    });
  });
}

/**
 * Get login QR code information
 */
export function getLoginQrcode() {
  let url = Api.getLoginQrcode;
  return defHttp.get({ url: url });
}

/**
 * Monitor code scanning status
 */
export function getQrcodeToken(params) {
  let url = Api.getQrcodeToken;
  return defHttp.get({ url: url, params });
}

/**
 * SSOLogin verification
 */
export async function validateCasLogin(params) {
  let url = Api.validateCasLogin;
  return defHttp.get({ url: url, params });
}
