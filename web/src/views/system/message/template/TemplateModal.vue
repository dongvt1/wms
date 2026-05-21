<template>
  <BasicModal @register="registerModal" :title="title" :width="600" v-bind="$attrs" @ok="onSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { formSchemas } from './template.data';
  import { saveOrUpdate } from './template.api';

  // statement emits
  const emit = defineEmits(['success', 'register']);
  const title = ref<string>('');
  const isUpdate = ref<boolean>(false);
  // register form
  //update-begin---author:wangshuai ---date:20221123  for：[VUEN-2807]Add a viewing function to the message template------------
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema, setProps }] = useForm({
  //update-end---author:wangshuai ---date:20221123  for：[VUEN-2807]Add a viewing function to the message template--------------z
    schemas: formSchemas,
    showActionButtonGroup: false,
    baseRowStyle: {
      marginTop: '10px',
    },
    labelCol: {
      span: 5,
    },
    wrapperCol: {
      span: 17,
    },
  });
  // register modal
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
    isUpdate.value = unref(data.isUpdate);
    title.value = unref(data.title);
    await resetFields();
    await setFieldsValue({ ...data.record });
    // Disable entire form when hiding bottom
    setProps({ disabled: !data?.showFooter })
  });

  //form submission event
  async function onSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Submit form
      await saveOrUpdate(values, isUpdate);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
