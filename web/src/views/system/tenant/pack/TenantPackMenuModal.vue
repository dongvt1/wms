<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="800px" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup name="tenant-pack-menu-modal">
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { packMenuFormSchema } from '../tenant.data';
  import { addPackPermission, editPackPermission } from '../tenant.api';

  const isUpdate = ref<boolean>(false);
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
    schemas: packMenuFormSchema,
    showActionButtonGroup: false,
  });
  //tenant
  const tenantId = ref<number>();
  //Package type
  const packType = ref<number>();
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    isUpdate.value = !!data?.isUpdate;
    if(data.tenantId){
      tenantId.value = data.tenantId;
    }
    packType.value = data.packType;
    if (unref(isUpdate)) {
      //form assignment
      console.log(data.record)
      await setFieldsValue({ ...data.record });
    }
    //update-begin---author:wangshuai ---date:20230705  for：【QQYUN-5685】2 Add a view to the package package：Add whether there are buttons at the bottom and the form is disabled------------
    setModalProps({ confirmLoading: false, showCancelBtn:!!data?.showFooter, showOkBtn:!!data?.showFooter });
    // Disable entire form when hiding bottom
    setProps({ disabled: !data?.showFooter })
    //update-end---author:wangshuai ---date:20230705  for：【QQYUN-5685】2 Add a view to the package package：Add whether there are buttons at the bottom and the form is disabled------------
  });
  //Set title
  const title = computed(() => (unref(isUpdate) ? 'edit tenant套餐' : 'New tenant套餐'));
  //form submission event
  async function handleSubmit(v) {
    const values = await validate();
    
    setModalProps({ confirmLoading: true });
    values.packType = unref(packType);
    if(values.packType === 'custom'){
      values.tenantId = unref(tenantId);
    }else{
      values.tenantId = 0;
    }
    if (!unref(isUpdate)) {
      await addPackPermission(values);
    } else {
      await editPackPermission(values);
    }
    emit('success');
    setModalProps({ confirmLoading: false });
    closeModal();
  }
</script>
