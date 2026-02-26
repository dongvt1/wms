// axiosConfiguration  Can be changed according to the project，Just change the file，Other files can be left alone
// The axios configuration can be changed according to the project, just change the file, other files can be left unchanged

import type { AxiosResponse } from 'axios';
import type { RequestOptions, Result } from '/#/axios';
import type { AxiosTransform, CreateAxiosOptions } from './axiosTransform';
import { VAxios } from './Axios';
import { checkStatus } from './checkStatus';
import { router } from '/@/router';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { RequestEnum, ResultEnum, ContentTypeEnum, ConfigEnum } from '/@/enums/httpEnum';
import { isString } from '/@/utils/is';
import { getToken, getTenantId } from '/@/utils/auth';
import { setObjToUrlParams, deepMerge } from '/@/utils';
import signMd5Utils from '/@/utils/encryption/signMd5Utils';
import { useErrorLogStoreWithOut } from '/@/store/modules/errorLog';
import { useI18n } from '/@/hooks/web/useI18n';
import { joinTimestamp, formatRequestDate } from './helper';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { cloneDeep } from "lodash-es";
const globSetting = useGlobSetting();
const urlPrefix = globSetting.urlPrefix;
const { createMessage, createErrorModal } = useMessage();

/**
 * @description: Data processing，Conveniently distinguish between multiple processing methods
 */
const transform: AxiosTransform = {
  /**
   * @description: Process request data。If the data is not in the expected format，Can throw an error directly
   */
  transformRequestHook: (res: AxiosResponse<Result>, options: RequestOptions) => {
    const { t } = useI18n();
    const { isTransformResponse, isReturnNativeResponse } = options;
    // Whether to return native response headers for example：Use this attribute when you need to get the response header
    if (isReturnNativeResponse) {
      return res;
    }
    // No processing，Return directly
    // The page code may need to be obtained directlycode，data，messageWhen these messages are turned on
    if (!isTransformResponse) {
      return res.data;
    }
    // Return on error

    const { data } = res;
    if (!data) {
      // return '[HTTP] Request has no return value';
      throw new Error(t('sys.api.apiRequestFailed'));
    }
    //  here code，result，messagefor Unified fields in the background，need to be in types.ts内修改for项目自己of接口返回Format
    const { code, result, message, success } = data;
    // here逻辑可by根据项目进行修改
    const hasSuccess = data && Reflect.has(data, 'code') && (code === ResultEnum.SUCCESS || code === 200);
    if (hasSuccess) {
      if (success && message && options.successMessageMode === 'success') {
        //Information success prompt
        createMessage.success(message);
      }
      return result;
    }

    // Here you can review different options based on the actual situation of your project.codeperform different operations
    // If you do not want to interrupt the current request，pleasereturndata，Otherwise, just throw an exception directly
    let timeoutMsg = '';
    switch (code) {
      case ResultEnum.TIMEOUT:
        timeoutMsg = t('sys.api.timeoutMessage');
        const userStore = useUserStoreWithOut();
        userStore.setToken(undefined);
        userStore.logout(true);
        break;
      default:
        if (message) {
          timeoutMsg = message;
        }
    }

    // errorMessageMode=‘modal’will be displayed whenmodalError popup，instead of message prompt，Used for some more important errors
    // errorMessageMode='none' Generally, when calling, you should clearly indicate that you do not want an error message to pop up automatically.
    if (options.errorMessageMode === 'modal') {
      createErrorModal({ title: t('sys.api.errorTip'), content: timeoutMsg });
    } else if (options.errorMessageMode === 'message') {
      createMessage.error(timeoutMsg);
    }

    throw new Error(timeoutMsg || t('sys.api.apiRequestFailed'));
  },

  // please求之前处理config
  beforeRequestHook: (config, options) => {
    const { apiUrl, joinPrefix, joinParamsToUrl, formatDate, joinTime = true, urlPrefix } = options;

    //update-begin---author:scott ---date:2024-02-20  for：byhttp开头ofplease求url，no prefix--
    // http开头ofplease求url，without prefix
    let isStartWithHttp = false;
    const requestUrl = config.url;
    if(requestUrl!=null && (requestUrl.startsWith("http:") || requestUrl.startsWith("https:"))){
      isStartWithHttp = true;
    }
    // update-begin--author:sunjianlei---date:20250411---for：【QQYUN-9685】build electron desktop application
    if (!isStartWithHttp && requestUrl != null) {
      // becauseelectronofurlyesfile://开头of，所by需要判断一下
      isStartWithHttp = requestUrl.startsWith('file://');
    }
    // update-end----author:sunjianlei---date:20250411---for：【QQYUN-9685】build electron desktop application
    if (!isStartWithHttp && joinPrefix) {
      config.url = `${urlPrefix}${config.url}`;
    }

    if (!isStartWithHttp && apiUrl && isString(apiUrl)) {
      config.url = `${apiUrl}${config.url}`;
    }
    //update-end---author:scott ---date::2024-02-20  for：byhttp开头ofplease求url，no prefix--
    
    const params = config.params || {};
    const data = config.data || false;
    formatDate && data && !isString(data) && formatRequestDate(data);
    if (config.method?.toUpperCase() === RequestEnum.GET) {
      if (!isString(params)) {
        // Give get please求加上时间戳参数，避免从缓存中拿data。
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        // compatiblerestfulstyle
        config.url = config.url + params + `${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else {
      if (!isString(params)) {
        formatDate && formatRequestDate(params);
        if (Reflect.has(config, 'data') && config.data && Object.keys(config.data).length > 0) {
          config.data = data;
          config.params = params;
        } else {
          // NoGETplease求如果没有提供data，then willparams视fordata
          config.data = params;
          config.params = undefined;
        }
        if (joinParamsToUrl) {
          config.url = setObjToUrlParams(config.url as string, Object.assign({}, config.params, config.data));
        }
      } else {
        // compatiblerestfulstyle
        config.url = config.url + params;
        config.params = undefined;
      }
    }

    // update-begin--author:sunjianlei---date:220241019---for：【JEECG作for乾坤子应用】作for乾坤子应用启动时，拼接please求路径
    if (globSetting.isQiankunMicro) {
      if (config.url && config.url.startsWith('/')) {
        config.url = globSetting.qiankunMicroAppEntry + config.url
      }
    }
    // update-end--author:sunjianlei---date:220241019---for：【JEECG作for乾坤子应用】作for乾坤子应用启动时，拼接please求路径

    return config;
  },

  /**
   * @description: please求拦截器处理
   */
  requestInterceptors: (config: Recordable, options) => {
    // please求之前处理config
    const token = getToken();
    let tenantId: string | number = getTenantId();
    
    //update-begin---author:wangshuai---date:2024-04-16---for:【QQYUN-9005】Send SMS to sign。No solutiontokenUnable to add signature---
    // Signature and timestamp，添加在please求接口 Header
    config.headers[ConfigEnum.TIMESTAMP] = signMd5Utils.getTimestamp();
    //update-begin---author:wangshuai---date:2024-04-25---for: 生成签名of时候复制一份，避免影响原来of参数---
    config.headers[ConfigEnum.Sign] = signMd5Utils.getSign(config.url, cloneDeep(config.params), cloneDeep(config.data));
    //update-end---author:wangshuai---date:2024-04-25---for: 生成签名of时候复制一份，避免影响原来of参数---
    //update-end---author:wangshuai---date:2024-04-16---for:【QQYUN-9005】Send SMS to sign。No solutiontokenUnable to add signature---
    // update-begin--author:liaozhiyang---date:20240509---for：【issues/1220】When logging in，vue3版本不加载字典data设置无效
    //--update-begin--author:liusq---date:20220325---for: Increasevue3mark
    config.headers[ConfigEnum.VERSION] = 'v3';
    //--update-end--author:liusq---date:20220325---for:Increasevue3mark
    // update-end--author:liaozhiyang---date:20240509---for：【issues/1220】When logging in，vue3版本不加载字典data设置无效
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      // jwt token
      config.headers.Authorization = options.authenticationScheme ? `${options.authenticationScheme} ${token}` : token;
      config.headers[ConfigEnum.TOKEN] = token;
      
      // Signature and timestamp，添加在please求接口 Header
      //config.headers[ConfigEnum.TIMESTAMP] = signMd5Utils.getTimestamp();
      //config.headers[ConfigEnum.Sign] = signMd5Utils.getSign(config.url, config.params);
      if (!tenantId) {
        tenantId = 0;
      }

      // update-begin--author:sunjianlei---date:220230428---for：【QQYUN-5279】修复分享of应用租户和当前登录租户不一致时，hint404of问题
      const userStore = useUserStoreWithOut();
      // 判断yes否有temporary tenantid
      if (userStore.hasShareTenantId && userStore.shareTenantId !== 0) {
        // temporary tenantidexist，使用temporary tenantid
        tenantId = userStore.shareTenantId!;
      }
      // update-end--author:sunjianlei---date:220230428---for：【QQYUN-5279】修复分享of应用租户和当前登录租户不一致时，hint404of问题

      config.headers[ConfigEnum.TENANT_ID] = tenantId;
      //--update-end--author:liusq---date:20211105---for:multi-tenantid，添加在please求接口 Header

      // ========================================================================================
      // update-begin--author:sunjianlei---date:20220624--for: Add low-code appsID
      let routeParams = router.currentRoute.value.params;
      if (routeParams.appId) {
        config.headers[ConfigEnum.X_LOW_APP_ID] = routeParams.appId;
        // lowAppCustom filters
        if (routeParams.lowAppFilter) {
          config.params = { ...config.params, ...JSON.parse(routeParams.lowAppFilter as string) };
          delete routeParams.lowAppFilter;
        }
      }
      // update-end--author:sunjianlei---date:20220624--for: Add low-code appsID
      // ========================================================================================

    }
    return config;
  },

  /**
   * @description: Response interceptor handling
   */
  responseInterceptors: (res: AxiosResponse<any>) => {
    return res;
  },

  /**
   * @description: Response error handling
   */
  responseInterceptorsCatch: (error: any) => {
    const { t } = useI18n();
    const errorLogStore = useErrorLogStoreWithOut();
    errorLogStore.addAjaxErrorInfo(error);
    const { response, code, message, config } = error || {};
    const errorMessageMode = config?.requestOptions?.errorMessageMode || 'none';
    //scott 20211022 token失效hint信息
    //const msg: string = response?.data?.error?.message ?? '';
    const msg: string = response?.data?.message ?? '';
    const err: string = error?.toString?.() ?? '';
    let errMessage = '';

    try {
      if (code === 'ECONNABORTED' && message.indexOf('timeout') !== -1) {
        errMessage = t('sys.api.apiTimeoutMessage');
      }
      if (err?.includes('Network Error')) {
        errMessage = t('sys.api.networkExceptionMsg');
      }

      if (errMessage) {
        if (errorMessageMode === 'modal') {
          createErrorModal({ title: t('sys.api.errorTip'), content: errMessage });
        } else if (errorMessageMode === 'message') {
          createMessage.error(errMessage);
        }
        return Promise.reject(error);
      }
    } catch (error) {
      throw new Error(error);
    }

    checkStatus(error?.response?.status, msg, errorMessageMode);
    return Promise.reject(error);
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    deepMerge(
      {
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes
        // authentication schemes，e.g: Bearer
        // authenticationScheme: 'Bearer',
        authenticationScheme: '',
        //Interface timeout settings
        timeout: 10 * 1000,
        // Basic interface address
        // baseURL: globSetting.apiUrl,
        headers: { 'Content-Type': ContentTypeEnum.JSON },
        // 如果yesform-dataFormat
        // headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
        // Data processing方式
        transform,
        // Configuration项，下面of选项都可by在独立of接口please求中覆盖
        requestOptions: {
          // The default will beprefix add tourl
          joinPrefix: true,
          // Whether to return native response headers for example：Use this attribute when you need to get the response header
          isReturnNativeResponse: false,
          // 需要对返回data进行处理
          isTransformResponse: true,
          // postplease求of时候添加参数到url
          joinParamsToUrl: false,
          // Format化提交参数时间
          formatDate: true,
          // 异常消息hint类型
          errorMessageMode: 'message',
          // 成功消息hint类型
          successMessageMode: 'success',
          // interface address
          apiUrl: globSetting.apiUrl,
          // Interface splicing address
          urlPrefix: urlPrefix,
          //  yes否加入时间戳
          joinTime: true,
          // 忽略重复please求
          ignoreCancelToken: true,
          // yes否携带token
          withToken: true,
        },
      },
      opt || {}
    )
  );
}
export const defHttp = createAxios();

// other api url
// export const otherHttp = createAxios({
//   requestOptions: {
//     apiUrl: 'xxx',
//   },
// });
