<template>
  <BasicModal @register="registerModal" title="Send test" :width="800" v-bind="$attrs" @ok="onSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { sendTestFormSchemas } from './template.data';
  import { sendMessageTest } from './template.api';

  // statement emits
  const emit = defineEmits(['register']);
  // register form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: sendTestFormSchemas,
    showActionButtonGroup: false,
  });
  // register modal
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    await setFieldsValue({ ...unref(data.record) });
  });

  //form submission event
  async function onSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Submit form
      await sendMessageTest(values);
      //Close pop-up window
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
