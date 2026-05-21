<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="700px">
    <BasicForm @register="registerForm" />
    <!--TODO SubtableTabdata-->
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: [
      {
        field: 'orderCode',
        label: 'Order number',
        component: 'Input',
        required: true,
      },
      {
        field: 'ctype',
        label: 'Order type',
        component: 'Select',
        componentProps: {
          options: [
            { label: 'Domestic orders', value: '1' },
            { label: 'international orders', value: '2' },
          ],
        },
      },
      {
        field: 'orderDate',
        label: 'order date',
        component: 'DatePicker',
        componentProps: {
          valueFormat: 'YYYY-MM-DD hh:mm:ss',
        },
      },
      {
        field: 'orderMoney',
        label: 'Order amount',
        component: 'InputNumber',
      },
      {
        field: 'content',
        label: 'Order notes',
        component: 'Input',
      },
      {
        field: 'id',
        label: 'id',
        component: 'Input',
        show: false,
      },
    ],
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
  const title = computed(() => (!unref(isUpdate) ? 'Add new order' : 'Edit order'));
  //form submission event
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      let url = unref(isUpdate) ? '/test/order/edit' : '/test/order/add';
      defHttp.post({ url: url, params: values });
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
