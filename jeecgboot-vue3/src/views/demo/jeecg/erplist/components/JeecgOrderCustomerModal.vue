<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="700">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, inject } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { customerFormSchema } from '../erplist.data';
  import { saveOrUpdateCustomer } from '../erplist.api';
  //Receive master tableid
  const orderId = inject('orderId') || '';
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: customerFormSchema,
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
      if (unref(orderId)) {
        values.orderId = unref(orderId);
      }
      //Submit form
      await saveOrUpdateCustomer(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
