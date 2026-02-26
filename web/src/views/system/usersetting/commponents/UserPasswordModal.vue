<template>
  <BasicModal v-bind="$attrs"  @register="registerModal" title="Change password" @ok="handleSubmit" destroyOnClose :width="400">
    <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
      <a-form-item name="phone">
        <div class="black font-size-13">Verify mobile number</div>
        <div class="pass-padding">
          <a-input placeholder="Please enter mobile phone number" v-model:value="formState.phone"/>
        </div>
      </a-form-item>
      <a-form-item name="smscode">
        <CountdownInput v-model:value="formState.smscode" placeholder="Please enter6Verification code" :sendCodeApi="sendCodeApi" />
      </a-form-item>
      <a-form-item name="password">
        <span class="black font-size-13">New Password</span>
        <div class="pass-padding">
          <a-input-password v-model:value="formState.password" placeholder="New Password" autocomplete="new-password"/>
        </div>
        <span class="gray-9e font-size-13">8-20Bit，Must contain letters and numbers</span>
      </a-form-item>
    </a-form>
  </BasicModal>
</template>
<script lang="ts" name="user-pass-word-modal" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Rule } from '/@/components/Form/index';
  import { updateUserPassword } from '../UserSetting.api';
  import { useMessage } from "/@/hooks/web/useMessage";
  import { useUserStore, useUserStoreWithOut } from "/@/store/modules/user";
  import { getCaptcha } from "@/api/sys/user";
  import { SmsEnum } from "@/views/sys/login/useLogin";
  import { CountdownInput } from '/@/components/CountDown';
  import { defHttp } from "@/utils/http/axios";

  const { createMessage, createErrorModal } = useMessage();
  //username
  const username = ref<string>('')
  const formRef = ref();
  const formState = reactive({
    oldpassword:'',
    password:'',
    smscode:'',
    phone:'',
  });
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    username.value = data.record.username
    Object.assign(formState, { password:'', smscode:'', phone:'',})
  });
  const userStore = useUserStore();
  const validatorRules: Record<string, Rule[]> = {
    password: [{ required: true, validator:checkPassword},{ pattern:/^(?=.*[0-9])(?=.*[a-zA-Z])(.{8,20})$/,message:'8-20Bit，Must contain letters and numbers'}],
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
      await updateUserPassword(values).then((res) =>{
        if(res.success){
          createMessage.info({
            content:'Password changed successfully，Please log in again！3sAutomatically log out after',
            duration: 3
          })
          //3sReturn to the login page
          setTimeout(()=>{
            userStore.logout(true);
          },3000)
          //Close pop-up window
          closeModal();
        }else{
          createMessage.warn(res.message);
        }
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * 验证New Password是否为空
   */
  function checkPassword(_rule: Rule, value: string) {
    if(value === ''){
      return Promise.reject('Please enterNew Password');
    }
    return Promise.resolve();
  }

  /**
   * Countdown function before execution
   */
  function sendCodeApi() {
    return new Promise((resolve, reject) => {
      let params = { mobile: formState.phone };
      defHttp.post({ url: "/sys/sendChangePwdSms", params }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
          resolve(true);
        } else {
          createErrorModal({ title: 'Error message', content: res.message || 'unknown problem' });
          reject();
        }
      }).catch((res)=>{
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
  .gray-9e {
    color: #9e9e9e;
  }
  .float-left {
    float: left;
  }
  .pass-padding {
    padding-top: 10px;
    padding-bottom: 10px;
  }
  .antd-modal-form {
    padding: 10px 24px 10px 24px;
  }
  :deep(.ant-form-item){
    margin-bottom: 10px;
  }
</style>
