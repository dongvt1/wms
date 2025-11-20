<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="700px">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../tenant.data';
  import { saveOrUpdateTenant, getTenantById } from '../tenant.api';
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    // labelWidth: 150,
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
      // Disabled in edit modeidField
      updateSchema({ field: 'id', dynamicDisabled: true });
      //Get details
      data.record = await getTenantById({ id: data.record.id });
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    } else {
      updateSchema({ field: 'id', dynamicDisabled: false });
    }
  });
  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'Add new tenant' : 'Edit tenant'));
  //form submission event
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateTenant(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
