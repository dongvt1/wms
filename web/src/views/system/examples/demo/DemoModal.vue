<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" :disabled="isDisabled" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './demo.data';
  import { saveOrUpdateDemo, getDemoById } from './demo.api';
  // statementEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Custom acceptance parameters
  const props = defineProps({
    //Whether to disable the page
    isDisabled: {
      type: Boolean,
      default: false,
    },
  });

  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    //labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false, showOkBtn: !props.isDisabled});
    isUpdate.value = !!data?.isUpdate;
    if(data.createBy){
      await setFieldsValue({createBy: data.createBy})
    }
    if(data.createTime){
      await setFieldsValue({createTime: data.createTime})
    }
    if (unref(isUpdate)) {
      //Get details
      data.record = await getDemoById({ id: data.record.id });
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));
  //form submission event
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateDemo(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success', values);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
