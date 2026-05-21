<template>
    <div class="app-loading">
        <div class="app-loading-wrap">
            <img src="/resource/img/logo.png" class="app-loading-logo" alt="Logo">
            <div class="app-loading-dots">
                <span class="dot dot-spin"><i></i><i></i><i></i><i></i></span>
            </div>
            <div class="app-loading-title">JeecgBoot Enterprise-grade low-code platform</div>
        </div>
    </div>
</template>


<script lang="ts">
  /**
   * carried in the addresstoken，Jump to this page to log in
   */
  import { useRoute, useRouter } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  
  export default {
    name: "TokenLogin",
    setup(){
      const route = useRoute();
      let router = useRouter();
      const {createMessage, notification} = useMessage()
      const {t} = useI18n();
      const routeQuery:any = route.query;
      if(!routeQuery){
        createMessage.warning('Invalid parameter')
      }
     
      const token = routeQuery['loginToken'];
      if(!token){
        createMessage.warning('tokeninvalid')
      }
      const userStore = useUserStore();
      userStore.ThirdLogin({ token, thirdType:'email', goHome: false }).then(res => {
        console.log("res====>doThirdLogin",res)
        if(res && res.userInfo){
          requestSuccess(res)
        }else{
          requestFailed(res)
        }
      });

      function requestFailed (err) {
        notification.error({
          message: 'Login failed',
          description: ((err.response || {}).data || {}).message || err.message || "An error occurred with the request，Please try again later",
          duration: 4,
        });
      }
      
      function requestSuccess(res){
        let info = routeQuery.info;
        if(info){
          let query = JSON.parse(info);
          
          //update-begin-author:taoyan date:2023-4-27 for: QQYUN-4882【Simple flow】Node message notification Mail Click Apply to jump to the application homepage
          let path = '';
          if(query.isLowApp === 1){
            path = '/myapps/personalOffice/myTodo'
          }else{
            let taskId = query.taskId;
            path = '/task/handle/' + taskId
          }
          //update-end-author:taoyan date:2023-4-27 for: QQYUN-4882【Simple flow】Node message notification Mail Click Apply to jump to the application homepage
          
          router.replace({ path, query });
          notification.success({
            message: t('sys.login.loginSuccessTitle'),
            description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
            duration: 3,
          });
        }else{
          notification.error({
            message: 'Parameter invalid',
            description: "Page jump parameters are missing，Please check the log",
            duration: 4,
          });
        }
      }
    }
  }
</script>

<style scoped>

    html[data-theme='dark'] .app-loading {
        background-color: #2c344a;
    }

    html[data-theme='dark'] .app-loading .app-loading-title {
        color: rgba(255, 255, 255, 0.85);
    }

    .app-loading {
        display: flex;
        width: 100%;
        height: 100%;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        background-color: #f4f7f9;
    }

    .app-loading .app-loading-wrap {
        position: absolute;
        top: 50%;
        left: 50%;
        display: flex;
        -webkit-transform: translate3d(-50%, -50%, 0);
        transform: translate3d(-50%, -50%, 0);
        justify-content: center;
        align-items: center;
        flex-direction: column;
    }

    .app-loading .dots {
        display: flex;
        padding: 98px;
        justify-content: center;
        align-items: center;
    }

    .app-loading .app-loading-title {
        display: flex;
        margin-top: 30px;
        font-size: 30px;
        color: rgba(0, 0, 0, 0.85);
        justify-content: center;
        align-items: center;
    }

    .app-loading .app-loading-logo {
        display: block;
        width: 90px;
        margin: 0 auto;
        margin-bottom: 20px;
    }

    .dot {
        position: relative;
        display: inline-block;
        width: 48px;
        height: 48px;
        margin-top: 30px;
        font-size: 32px;
        transform: rotate(45deg);
        box-sizing: border-box;
        animation: antRotate 1.2s infinite linear;
    }

    .dot i {
        position: absolute;
        display: block;
        width: 20px;
        height: 20px;
        background-color: #0065cc;
        border-radius: 100%;
        opacity: 0.3;
        transform: scale(0.75);
        animation: antSpinMove 1s infinite linear alternate;
        transform-origin: 50% 50%;
    }

    .dot i:nth-child(1) {
        top: 0;
        left: 0;
    }

    .dot i:nth-child(2) {
        top: 0;
        right: 0;
        -webkit-animation-delay: 0.4s;
        animation-delay: 0.4s;
    }

    .dot i:nth-child(3) {
        right: 0;
        bottom: 0;
        -webkit-animation-delay: 0.8s;
        animation-delay: 0.8s;
    }

    .dot i:nth-child(4) {
        bottom: 0;
        left: 0;
        -webkit-animation-delay: 1.2s;
        animation-delay: 1.2s;
    }
    @keyframes antRotate {
        to {
            -webkit-transform: rotate(405deg);
            transform: rotate(405deg);
        }
    }
    @-webkit-keyframes antRotate {
        to {
            -webkit-transform: rotate(405deg);
            transform: rotate(405deg);
        }
    }
    @keyframes antSpinMove {
        to {
            opacity: 1;
        }
    }
    @-webkit-keyframes antSpinMove {
        to {
            opacity: 1;
        }
    }

</style>