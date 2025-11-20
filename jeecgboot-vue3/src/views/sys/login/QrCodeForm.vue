<template>
  <template v-if="getShow">
    <LoginFormTitle class="enter-x" />
    <div class="enter-x min-w-64 min-h-64">
      <QrCode :value="qrCodeUrl" class="enter-x flex justify-center xl:justify-start" :width="280" />
      <Divider class="enter-x">{{ scanContent }}</Divider>
      <Button size="large" block class="mt-4 enter-x" @click="handleBackLogin">
        {{ t('sys.login.backSignIn') }}
      </Button>
    </div>
  </template>
</template>
<script lang="ts" setup>
  import { computed, onMounted, unref, ref, watch } from 'vue';
  import LoginFormTitle from './LoginFormTitle.vue';
  import { Button, Divider } from 'ant-design-vue';
  import { QrCode } from '/@/components/Qrcode/index';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useLoginState, LoginStateEnum } from './useLogin';
  import { getLoginQrcode, getQrcodeToken } from '/@/api/sys/user';
  const qrCodeUrl = ref('');
  let timer: IntervalHandle;
  const { t } = useI18n();
  const userStore = useUserStore();
  const { handleBackLogin, getLoginState } = useLoginState();
  const state = ref('0');
  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.QR_CODE);
  const scanContent = computed(() => {
    return unref(state) === '0' ? t('sys.login.scanSign') : t('sys.login.scanSuccess');
  });
  //Load QR code information
  function loadQrCode() {
    state.value = '0';
    getLoginQrcode().then((res) => {
      qrCodeUrl.value = res.qrcodeId;
      if (res.qrcodeId) {
        openTimer(res.qrcodeId);
      }
    });
  }
  //Monitor code scanning status
  function watchQrcodeToken(qrcodeId) {
    getQrcodeToken({ qrcodeId: qrcodeId }).then((res) => {
      let token = res.token;
      if (token == '-2') {
        //Re-obtain the QR code after expiration
        loadQrCode();
        clearInterval(timer);
      }
      //Scan code successfully
      if (res.success) {
        state.value = '2';
        clearInterval(timer);
        setTimeout(() => {
          userStore.qrCodeLogin(token);
        }, 500);
      }
    });
  }

  /** Start timer */
  function openTimer(qrcodeId) {
    watchQrcodeToken(qrcodeId);
    closeTimer();
    timer = setInterval(() => {
      watchQrcodeToken(qrcodeId);
    }, 1500);
  }

  /** off timer */
  function closeTimer() {
    if (timer) clearInterval(timer);
  }

  watch(getShow, (v) => {
    if (v) {
      loadQrCode();
    } else {
      closeTimer();
    }
  });
</script>
