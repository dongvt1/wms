<template>
  <div> </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { isOAuth2AppEnv, sysOAuth2Callback, sysOAuth2Login } from '/@/views/sys/login/useLogin';
  import { useRouter } from 'vue-router';
  import { PageEnum } from '/@/enums/pageEnum';
  import { router } from '/@/router';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getAuthCache, getTenantId, getToken } from "/@/utils/auth";
  import { requestAuthCode } from 'dingtalk-jsapi';
  import { defHttp } from '/@/utils/http/axios';
  import { OAUTH2_THIRD_LOGIN_TENANT_ID } from "/@/enums/cacheEnum";

  const isOAuth = ref<boolean>(isOAuth2AppEnv());
  const env = ref<any>({ thirdApp: false, wxWork: false, dingtalk: false });
  const { currentRoute } = useRouter();
  const route = currentRoute.value;
  if (!isOAuth2AppEnv()) {
    router.replace({ path: PageEnum.BASE_LOGIN, query: route.query });
  }

  if (isOAuth.value) {
    checkEnv();
  }

  /**
   * Check the current environment
   */
  function checkEnv() {
    // Determine whether it is an enterprise WeChat environment at the time
    if (/wxwork/i.test(navigator.userAgent)) {
      env.value.thirdApp = true;
      env.value.wxWork = true;
    }
    // Determine whether it is a DingTalk environment at the time
    if (/dingtalk/i.test(navigator.userAgent)) {
      env.value.thirdApp = true;
      env.value.dingtalk = true;
    }
    doOAuth2Login();
  }

  /**
   * conductOAuth2Login operation
   */
  function doOAuth2Login() {
    if (env.value.thirdApp) {
      // Determine whether to carryToken，Yes, it means the login is successful
      if (route.query.oauth2LoginToken) {
        let token = route.query.oauth2LoginToken;
        //执行Login operation
        thirdLogin({ token, thirdType: route.query.thirdType,tenantId: getTenantId });
      } else if (env.value.wxWork) {
        sysOAuth2Login('wechat_enterprise');
      } else if (env.value.dingtalk) {
        //New version of DingTalk login
        dingdingLogin();
      }
    }
  }

  /**
   * Third party login
   * @param params
   */
  function thirdLogin(params) {
    const userStore = useUserStore();
    const { notification } = useMessage();
    const { t } = useI18n();
    userStore.ThirdLogin(params).then((res) => {
      if (res && res.userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
          duration: 3,
        });
      } else {
        notification.error({
          message: t('sys.login.errorTip'),
          description: ((res.response || {}).data || {}).message || res.message || t('sys.login.networkExceptionMsg'),
          duration: 4,
        });
      }
    });
  }

  /**
   * DingTalk login
   */
  function dingdingLogin() {
    //Companies that get DingTalk firstid，If not configured Just follow the original logic，Follow the original logic Need to determine whether it existstoken，existtokenGo directly to the homepage
    let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID) || 0;
    let url = `/sys/thirdLogin/get/corpId/clientId?tenantId=${tenantId}`;
    //update-begin---author:wangshuai---date:2024-12-09---for:Do not usegetAction onlineinside，Want to usedefHttp---
    defHttp.get({ url:url },{ isTransformResponse: false }).then((res) => {
    //update-end---author:wangshuai---date:2024-12-09---for:Do not usegetAction onlineinside，Want to usedefHttp---
        if (res.success) {
          if(res.result && res.result.corpId && res.result.clientId){
            requestAuthCode({ corpId: res.result.corpId, clientId: res.result.clientId }).then((res) => {
              let { code } = res;
              sysOAuth2Callback(code);
            });
          }else{
            toOldAuthLogin();
          }
        } else {
          toOldAuthLogin();
        }
      }).catch((err) => {
        toOldAuthLogin();
      });
  }
  
  /**
   * 旧版DingTalk login
   */
  function toOldAuthLogin() {
    let token = getToken();
    if (token) {
      router.replace({ path: PageEnum.BASE_HOME });
    } else {
      sysOAuth2Login('dingtalk');
    }
  }
</script>
