<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Home page configuration" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { roleIndexFormSchema } from '../role.data';
  import { saveOrUpdateRoleIndex, queryIndexByCode } from '../role.api';
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: roleIndexFormSchema,
    showActionButtonGroup: false,
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    setFieldsValue({ roleCode: data.roleCode });
    let res = await queryIndexByCode({ roleCode: data.roleCode });
    isUpdate.value = !!res.result?.id;
    if (unref(isUpdate)) {
      //form assignment
      await setFieldsValue({
        ...res.result,
      });
    }
  });

  //form submission event
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateRoleIndex(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success', { isUpdate: isUpdate.value, values });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped></style>
