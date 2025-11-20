<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="500px" :bodyStyle="{ padding: '20px 40px 20px 20px'}">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { rules } from '/@/utils/helper/validator';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import BasicForm from '/@/components/Form/src/BasicForm.vue';
  import { useForm } from '/@/components/Form/src/hooks/useForm';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { useI18n } from '/@/hooks/web/useI18n';
  const localeStore = useLocaleStore();
  const { t } = useI18n();
  // statementEmits
  const emit = defineEmits(['register']);
  const $message = useMessage();
  const formRef = ref();
  const username = ref('');
  // update-begin--author:liaozhiyang---date:20240124---for：【QQYUN-7970】internationalization
  const title = ref(t('layout.changePassword.changePassword'));
  //Form configuration
  const [registerForm, { resetFields, validate, clearValidate }] = useForm({
    schemas: [
      {
        label: t('layout.changePassword.oldPassword'),
        field: 'oldpassword',
        component: 'InputPassword',
        required: true,
      },
      {
        label: t('layout.changePassword.newPassword'),
        field: 'password',
        component: 'StrengthMeter',
        componentProps: {
          placeholder: t('layout.changePassword.pleaseEnterNewPassword'),
        },
        rules: [
          {
            required: true,
            message: t('layout.changePassword.pleaseEnterNewPassword'),
          },
        ],
      },
      {
        label: t('layout.changePassword.confirmNewPassword'),
        field: 'confirmpassword',
        component: 'InputPassword',
        dynamicRules: ({ values }) => rules.confirmPassword(values, true),
      },
    ],
    showActionButtonGroup: false,
    wrapperCol: null,
    labelWidth: localeStore.getLocale == 'zh_CN' ? 100 : 160,
  });
  // update-end--author:liaozhiyang---date:20240124---for：【QQYUN-7970】internationalization
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner();

  //form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      let params = Object.assign({ username: unref(username) }, values);
      defHttp.put({ url: '/sys/user/updatePassword', params }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
          $message.createMessage.success(res.message);
          //Close pop-up window
          closeModal();
        } else {
          $message.createMessage.warning(res.message);
        }
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  async function show(name) {
    if (!name) {
      $message.createMessage.warning('There is currently no logged-in user in the system!');
      return;
    } else {
      username.value = name;
      await setModalProps({ visible: true });
      await resetFields();
      await clearValidate();
    }
  }

  defineExpose({
    title,
    show,
  });
</script>
