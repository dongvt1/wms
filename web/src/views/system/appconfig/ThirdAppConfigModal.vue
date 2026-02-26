<template>
  <BasicModal @register="registerModal" :width="800" :title="title" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useForm, BasicForm } from '/@/components/Form';
  import { thirdAppFormSchema } from './ThirdApp.data';
  import { getThirdConfigByTenantId, saveOrUpdateThirdConfig } from './ThirdApp.api';
  export default defineComponent({
    name: 'ThirdAppConfigModal',
    components: { BasicModal, BasicForm },
    setup(props, { emit }) {
      const title = ref<string>('DingTalk configuration');
      //Form configuration
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: thirdAppFormSchema,
        showActionButtonGroup: false,
        labelCol: { span: 24 },
        wrapperCol: { span: 24 },
      });
      //form assignment
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: true });
        if (data.thirdType == 'dingtalk') {
          title.value = 'DingTalk configuration';
        } else {
          title.value = 'Enterprise WeChat configuration';
        }
        //Reset form
        await resetFields();
        let values = await getThirdConfigByTenantId({ tenantId: data.tenantId, thirdType: data.thirdType });
        setModalProps({ confirmLoading: false });
        //form assignment
        if (values) {
          await setFieldsValue(values);
        } else {
          await setFieldsValue(data);
        }
      });

      /**
       * Third-party configuration click event
       */
      async function handleSubmit() {
        let values = await validate();
        let isUpdate = false;
        if (values.id) {
          isUpdate = true;
        }
        await saveOrUpdateThirdConfig(values, isUpdate);
        emit('success');
        closeModal();
      }

      return {
        title,
        registerForm,
        registerModal,
        handleSubmit,
      };
    },
  });
</script>

<style scoped></style>
