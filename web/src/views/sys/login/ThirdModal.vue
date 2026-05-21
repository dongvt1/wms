<template>
  <!-- Third-party login binding account and password input pop-up box -->
  <a-modal title="Please enter password" v-model:open="thirdPasswordShow" @ok="thirdLoginCheckPassword" @cancel="thirdLoginNoPassword">
    <a-input-password placeholder="Please enter password" v-model:value="thirdLoginPassword" style="margin: 15px; width: 80%" />
  </a-modal>

  <!-- Third-party login prompts whether to bind account pop-up box -->
  <a-modal :footer="null" :closable="false" v-model:open="thirdConfirmShow" :class="'ant-modal-confirm'">
    <div class="ant-modal-confirm-body-wrapper">
      <div class="ant-modal-confirm-body">
        <QuestionCircleFilled style="color: #faad14" />
        <span class="ant-modal-confirm-title">hint</span>
        <div class="ant-modal-confirm-content"> An account with the same name already exists,Please confirm whether to bind this account？ </div>
      </div>
      <div class="ant-modal-confirm-btns">
        <a-button @click="thirdLoginUserCreate" :loading="thirdCreateUserLoding">Create new account</a-button>
        <a-button @click="thirdLoginUserBind" type="primary">Confirm binding</a-button>
      </div>
    </div>
  </a-modal>

  <!-- Third-party login binds mobile phone number -->
  <a-modal title="Bind mobile phone number" v-model:open="bindingPhoneModal" :maskClosable="false">
    <Form class="p-4 enter-x" style="margin: 15px 10px">
      <FormItem class="enter-x">
        <a-input size="large" placeholder="Please enter mobile phone number" v-model:value="thirdPhone" class="fix-auto-fill">
          <template #prefix>
            <Icon icon="ant-design:mobile-outlined" :style="{ color: 'rgba(0,0,0,.25)' }"></Icon>
          </template>
        </a-input>
      </FormItem>
      <FormItem name="sms" class="enter-x">
        <CountdownInput size="large" class="fix-auto-fill" v-model:value="thirdCaptcha" placeholder="Please enter the verification code" :sendCodeApi="sendCodeApi">
          <template #prefix>
            <Icon icon="ant-design:mail-outlined" :style="{ color: 'rgba(0,0,0,.25)' }"></Icon>
          </template>
        </CountdownInput>
      </FormItem>
    </Form>
    <template #footer>
      <a-button type="primary" @click="thirdHandleOk">Sure</a-button>
    </template>
  </a-modal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Form, Input } from 'ant-design-vue';
  import { CountdownInput } from '/@/components/CountDown';
  import { useThirdLogin } from '/@/hooks/system/useThirdLogin';
  import { QuestionCircleFilled } from '@ant-design/icons-vue';

  const FormItem = Form.Item;
  const InputPassword = Input.Password;

  export default defineComponent({
    name: 'ThirdModal',
    components: { FormItem, Form, InputPassword, CountdownInput, QuestionCircleFilled },
    setup() {
      return {
        ...useThirdLogin(),
      };
    },
  });
</script>
