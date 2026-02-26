<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './quartz.data';
  import { saveOrUpdateQuartz, getQuartzById } from './quartz.api';
  import { isJsonObjectString } from '/@/utils/is';
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    // labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
    // update-begin--author:liaozhiyang---date:20231017---for：【issues/790】The text box in the pop-up window is not centered
    labelWidth: 100,
    // update-end--author:liaozhiyang---date:20231017---for：【issues/790】The text box in the pop-up window is not centered
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Get details
      //data.record = await getQuartzById({id: data.record.id});
      try {
        data.record.paramterType = isJsonObjectString(data?.record?.parameter) ? 'json' : 'string';
      } catch (e) {
        console.log(e);
      }
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'Add new task' : 'Edit task'));
  //form submission event
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateQuartz(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
