<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Home page configuration" @ok="handleSubmit" :width="600">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../home.data';
  import { saveOrUpdate } from '../home.api';
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(false);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    baseRowStyle: { marginTop: '10px' },
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      const record = {...data.values}
      //form assignment
      if (record.relationType == 'USER') {
        record.userCode = record.roleCode;
      }
      //form assignment
      if (record.relationType == 'DEFAULT') {
        record.roleCode = '';
      }
      await setFieldsValue({
        ...record,
      });
    }
  });

  //form submission event
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      if(values.relationType == 'USER'){
        values.roleCode = values.userCode;
      }
      if(values.relationType == 'DEFAULT'){
        values.roleCode = 'DEF_INDEX_ALL';
      }
      await saveOrUpdate(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped></style>
