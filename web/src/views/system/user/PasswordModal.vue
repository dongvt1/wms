<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Change password" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" name="PassWordModal" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formPasswordSchema } from './user.data';
  import { changePassword } from './user.api';
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formPasswordSchema,
    showActionButtonGroup: false,
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    //form assignment
    await setFieldsValue({ ...data });
  });
  //form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await changePassword(values);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
