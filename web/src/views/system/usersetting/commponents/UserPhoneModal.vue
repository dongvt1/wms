<template>
<BasicModal v-bind="$attrs" @register="registerModal" width="500px"  :title="title" :showCancelBtn="false" :showOkBtn="false">
  <a-form v-if="type==='updatePhone'" class="antd-modal-form" ref="updateFormRef" :model="updateFormState"
          :rules="updateValidatorRules">
    <div v-if="current === 0">
      <a-form-item name="phoneText">
        <span class="black font-size-13">Original mobile phone number</span>
        <div class="phone-padding">
          <span>{{ updateFormState.phoneText }}</span>
        </div>
      </a-form-item>
      <a-form-item name="smscode">
        <span class="black font-size-13">Verification code</span>
        <CountdownInput class="phone-padding" size="large" v-model:value="updateFormState.smscode" placeholder="enter6位Verification code"
                        :sendCodeApi="()=>updateSendCodeApi('verifyOriginalPhone')"/>
      </a-form-item>
      <a-form-item>
        <a-button size="large" type="primary" block @click="nextStepClick">
          Next step
        </a-button>
      </a-form-item>
    </div>
    <div v-else-if="current === 1">
      <a-form-item name="newPhone">
        <span class="black font-size-13">New mobile number</span>
        <div class="phone-padding">
          <a-input v-model:value="updateFormState.newPhone" placeholder="请enterNew mobile number"/>
        </div>
      </a-form-item>
      <a-form-item name="smscode">
        <span class="black font-size-13">Verification code</span>
        <CountdownInput class="phone-padding" size="large" v-model:value="updateFormState.smscode" placeholder="enter6位Verification code"
                        :sendCodeApi="()=>updateSendCodeApi('updatePhone')"/>
      </a-form-item>
      <a-form-item>
        <a-button size="large" type="primary" block @click="finishedClick">
          Finish
        </a-button>
      </a-form-item>
    </div>
  </a-form>

  <a-form v-else-if="type==='bindPhone'" class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
    <a-form-item  name="phone">
      <a-input size="large" v-model:value="formState.phone" placeholder="请enterPhone number" />
    </a-form-item>
    <a-form-item name="smscode">
      <CountdownInput size="large" v-model:value="formState.smscode" placeholder="enter6位Verification code" :sendCodeApi="sendCodeApi" />
    </a-form-item>
    <a-form-item>
      <a-button size="large" type="primary" block @click="updatePhone">
        confirm
      </a-button>
    </a-form-item>
  </a-form>
</BasicModal>
</template>

<script lang="ts" setup name="user-replace-phone-modal">
import BasicModal from "/@/components/Modal/src/BasicModal.vue";
import { CountdownInput } from '/@/components/CountDown';
import { useUserStore } from "/@/store/modules/user";
import { useMessage } from "/@/hooks/web/useMessage";
import { defineEmits, ref, reactive, toRaw } from "vue";
import { useModalInner } from "/@/components/Modal";
import { getCaptcha } from "/@/api/sys/user";
import { SmsEnum } from "/@/views/sys/login/useLogin";
import { Rule } from "/@/components/Form";
import { rules } from "/@/utils/helper/validator";
import { Form } from "ant-design-vue";
import { updateMobile, changePhone } from "../UserSetting.api";
import { duplicateCheck } from "/@/views/system/user/user.api";
import {defHttp} from "@/utils/http/axios";
import { ExceptionEnum } from "@/enums/exceptionEnum";

const userStore = useUserStore();
const { createMessage } = useMessage();
const formState = reactive<Record<string, any>>({
  phone:'',
  smscode:''
});

//Modify mobile phone number
const updateFormState = reactive<Record<string, any>>({
  phone:'',
  smscode:'',
  newPhone:'',
  phoneText:'',
  newSmsCode:''
});

const formRef = ref();
const userData = ref<any>({})

const validatorRules: Record<string, Rule[]> = {
  phone: [{...rules.duplicateCheckRule("sys_user",'phone',formState,{ label:'Phone number' })[0]},{ pattern: /^1[3456789]\d{9}$/, message: 'Phone number码格式有误' }],
  smscode: [{ required: true,message:'请enterVerification code' }],
};

//Modify mobile phone number验证规则
const updateValidatorRules: Record<string, Rule[]> = {
  newPhone: [{...rules.duplicateCheckRule("sys_user",'phone',formState,{ label:'Phone number' })[0]},{ pattern: /^1[3456789]\d{9}$/, message: 'Phone number码格式有误' }],
  smscode: [{ required: true,message:'请enterVerification code' }],
  newSmsCode: [{ required: true,message:'请enterVerification code' }],
};
const useForm = Form.useForm;
const title = ref<string>('');
const emit = defineEmits(['register','success']);
//Modify mobile phone number还是绑定Phone number
const type = ref<string>('updatePhone');
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  setModalProps({ confirmLoading: false });
  if(data.record.phone){
    updateFormState.phone = "";
    updateFormState.smscode = "";
    current.value = 0;
    title.value = 'Modify mobile phone number';
    type.value = "updatePhone";
    Object.assign(updateFormState, data.record);
  }else{
    title.value = '绑定Phone number';
    type.value = "bindPhone"
    //Assignment
    data.record.smscode = '';
    Object.assign(formState, data.record);
    setTimeout(()=>{
      formRef.value.resetFields();
      formRef.value.clearValidate();
    },300)
  }
  userData.value = data.record;
});

/**
 * Countdown function before execution
 */
function sendCodeApi() {
  return getCaptcha({ mobile: formState.phone, smsmode: SmsEnum.REGISTER });
}

/**
 * Countdown function before execution
 * 
 * @param type type verifyOriginalPhone 验证员Phone number updatePhone Modify mobile phone number
 */
function updateSendCodeApi(type) {
  let phone = ""
  if(current.value === 0){
    phone = updateFormState.phone;
  }else{
    phone = updateFormState.newPhone;
  }
  let params = { phone: phone,  type: type };
  return new Promise((resolve, reject) => {
    defHttp.post({ url: "/sys/user/sendChangePhoneSms", params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        //update-begin---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】same oneIP，1minutes exceed5SMS，则提示需要Verification code---
        if(res.code != ExceptionEnum.PHONE_SMS_FAIL_CODE){
          createMessage.error(res.message || 'unknown problem');
          reject();
        }
        reject(res);
        //update-end---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】same oneIP，1minutes exceed5SMS，则提示需要Verification code---
      }
    }).catch((res)=>{
      createMessage.error(res.message || 'unknown problem');
      reject();
    });
  });
}

/**
 * 更New mobile number
 */
async function updatePhone() {
  await formRef.value.validateFields();
  updateMobile(formState).then((res) =>{
    if(res.success){
      createMessage.success(type.value === "updatePhone"?"Modify mobile phone number成功":"绑定Phone number成功")
      emit("success")
      closeModal();
    }else{
      createMessage.warning(res.message)
    }
  })
}
  //What steps have you gone to?
  const current = ref<number>(0);
  const updateFormRef = ref();

  /**
   * Next step点击事件
   */
  async function nextStepClick() {
    let params = { phone: updateFormState.phone, smscode: updateFormState.smscode, type: 'verifyOriginalPhone' };
    changeAndVerifyPhone(params,1)
  }

  /**
   * Finish
   */
  function finishedClick() {
    changeAndVerifyPhone({ phone: updateFormState.phone, newPhone: updateFormState.newPhone, smscode: updateFormState.smscode, type: 'updatePhone' },0);
  }

  /**
   * 修改并验证Phone number
   * @param params
   * @param index
   */
  async function changeAndVerifyPhone(params, index) {
    await updateFormRef.value.validateFields();
    changePhone(params).then((res)=>{
      if(res.success){
        current.value = index;
        if(index == 0){
          createMessage.success(res.message);
          emit("success");
          closeModal();
        }
        updateFormState.smscode = "";
      }else{
        createMessage.warn(res.message);
      }
    }).catch((res) =>{
      createMessage.warn(res.message);
    })
  }
</script>
<style lang="less" scoped>
  .antd-modal-form {
    padding: 10px 24px 10px 24px;
  }
 .black {
   color: #000000;
 }
 .font-size-13 {
   font-size: 13px;
   line-height: 15px;
 }
  .phone-padding {
    padding-top: 10px;
    padding-bottom: 10px;
  }
  :deep(.ant-form-item){
    margin-bottom: 10px;
  }
</style>