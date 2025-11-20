<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Log out" @ok="handleSubmit" destroyOnClose :width="400">
    <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
      <div class="cancellation-tip">
        <p style="color: red;margin-bottom: 10px !important;">Log out后账号会保留10sky，If you need to restore, pleaseQQadministrator </p>
      </div>
      <a-form-item label="" name="phone" class="cancellation-tip">
        <div class="black font-size-13" style="margin-bottom: 6px">Verification method</div>
        <div class="pass-padding">
          <a-input placeholder="Please enter mobile phone number" v-model:value="formState.phone" />
        </div>
      </a-form-item>
      <a-form-item label="" name="smscode" class="cancellation-tip">
        <CountdownInput v-model:value="formState.smscode" placeholder="Please enter6Verification code" :sendCodeApi="sendCodeApi" />
      </a-form-item>
    </a-form>
  </BasicModal>
</template>
<script lang="ts" name="UserCancellationModal" setup>
  import { ref, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Rule } from '/@/components/Form/index';
  import { userLogOff } from '../UserSetting.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { CountdownInput } from '/@/components/CountDown';
  import { defHttp } from '@/utils/http/axios';

  const { createMessage, createErrorModal } = useMessage();
  //username
  const username = ref<string>('');
  const formRef = ref();
  const formState = reactive({
    smscode: '',
    phone: '',
  });
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    username.value = data.record.username;
    Object.assign(formState, { password: '', smscode: '', phone: '' });
  });
  const userStore = useUserStore();
  const validatorRules: Record<string, Rule[]> = {
    phone: [{ required: true, message: 'Please enter mobile phone number' }],
    smscode: [{ required: true, message: 'Please enter6Verification code' }],
  };

  //form submission event
  async function handleSubmit() {
    try {
      let values = await formRef.value.validateFields();
      setModalProps({ confirmLoading: true });
      //Submit form
      values.username = unref(username);
      await userLogOff(values).then((res) => {
        if (res.success) {
          createMessage.info({
            content: 'Log out成功！',
            duration: 1,
          });
          //Return to login page
          setTimeout(() => {
            userStore.logout(true);
          }, 1000);
          //Close pop-up window
          closeModal();
        } else {
          createMessage.warn(res.message);
        }
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Countdown function before execution
   */
  function sendCodeApi() {
    return new Promise((resolve, reject) => {
      if(!formState.phone){
        createErrorModal({ title: 'Error message', content: 'Please enter mobile phone number！' });
        reject();
        return;
      }
      let params = { phone: formState.phone, username: username.value };
      defHttp
        .post({ url: '/sys/user/sendLogOffPhoneSms', params }, { isTransformResponse: false })
        .then((res) => {
          if (res.success) {
            resolve(true);
          } else {
            createErrorModal({ title: 'Error message', content: res.message || 'unknown problem' });
            reject();
          }
        })
        .catch((res) => {
          createErrorModal({ title: 'Error message', content: res.message || 'unknown problem' });
          reject();
        });
    });
  }
</script>
<style lang="less" scoped>
  .black {
    color: @text-color;
  }
  .font-size-13 {
    font-size: 13px;
    line-height: 15px;
  }
  .cancellation-tip {
    padding: 0 10px;
  }
</style>
