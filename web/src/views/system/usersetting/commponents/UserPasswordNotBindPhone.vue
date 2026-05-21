<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Change password" @ok="handleSubmit" destroyOnClose :width="400">
    <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
      <a-form-item name="oldPassword">
        <div class="black font-size-13">Old Password</div>
        <div class="pass-padding">
          <a-input-password placeholder="请输入Old Password" v-model:value="formState.oldPassword" />
        </div>
      </a-form-item>
      <a-form-item name="password">
        <span class="black font-size-13">New Password</span>
        <div class="pass-padding">
          <a-input-password v-model:value="formState.password" placeholder="New Password" autocomplete="new-password" />
        </div>
        <span class="gray-9e font-size-13">8-20Bit，Must contain letters and numbers</span>
      </a-form-item>
    </a-form>
  </BasicModal>
</template>
<script lang="ts" name="user-pass-word-modal" setup>
  import { ref, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Rule } from '@/components/Form';
  import { updatePasswordNotBindPhone } from '../UserSetting.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';

  const { createMessage } = useMessage();
  //username
  const username = ref<string>('');
  const formRef = ref();
  const formState = reactive({
    oldPassword: '',
    password: '',
  });
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    username.value = data.record.username;
    Object.assign(formState, { password: '', oldPassword: '' });
  });
  const userStore = useUserStore();
  const validatorRules: Record<string, Rule[]> = {
    oldPassword: [{ required: true, message: '请输入Old Password' }],
    password: [
      { required: true, validator: checkPassword },
      { pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])(.{8,20})$/, message: '8-20Bit，Must contain letters and numbers' },
    ],
  };

  //form submission event
  async function handleSubmit() {
    try {
      let values = await formRef.value.validateFields();
      setModalProps({ confirmLoading: true });
      //Submit form
      values.username = unref(username);
      await updatePasswordNotBindPhone(values).then((res) => {
        if (res.success) {
          createMessage.info({
            content: 'Password changed successfully，Please log in again！3sAutomatically log out after',
            duration: 3,
          });
          //3sReturn to the login page
          setTimeout(() => {
            userStore.logout(true);
          }, 3000);
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
   * 验证New Password是否为空
   */
  function checkPassword(_rule: Rule, value: string) {
    if (value === '') {
      return Promise.reject('请输入New Password');
    }
    return Promise.resolve();
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
  :deep(.ant-form-item) {
    margin-bottom: 10px;
  }
</style>
