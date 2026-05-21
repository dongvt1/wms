<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="700">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../erplist.data';
  import { saveOrUpdate } from '../erplist.api';
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
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
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));

  //form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
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
