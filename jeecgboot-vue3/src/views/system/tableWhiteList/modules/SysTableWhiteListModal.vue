<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    width="40%"
    v-bind="$attrs"
    @ok="handleSubmit"
  >
    <div class="content"> 
      <BasicForm  @register="registerForm"/>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import {computed, ref, unref} from 'vue';
import {BasicModal, useModalInner} from '/@/components/Modal';
import {BasicForm, useForm} from '/@/components/Form/index';
import {formSchema} from '../SysTableWhiteList.data';
import {saveOrUpdate} from '../SysTableWhiteList.api';
// Emits declaration
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
// Form configuration
const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
  labelWidth: 120,
  wrapperCol: null,
  schemas: formSchema,
  showActionButtonGroup: false,
});
// Form assignment
const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
  // Reset form
  await resetFields();
  setModalProps({
    confirmLoading: false,
    showCancelBtn: data?.showFooter,
    showOkBtn: data?.showFooter
  });
  isUpdate.value = !!data?.isUpdate;
  if (unref(isUpdate)) {
    // Form assignment
    await setFieldsValue({
      ...data.record,
    });
  }
  setProps({ disabled: !data?.showFooter })
});
// Set title
const title = computed(() => (!unref(isUpdate) ? 'Add New' : 'Edit'));

// Form submit event
async function handleSubmit(v) {
  try {
    let values = await validate();
    setModalProps({confirmLoading: true});
    // Submit form
    await saveOrUpdate(values, isUpdate.value);
    // Close modal
    closeModal();
    // Refresh list
    emit('success', {isUpdate: isUpdate.value, values});
  } finally {
    setModalProps({confirmLoading: false});
  }
}
</script>

<style lang="less" scoped>
  .content {
    padding: 20px 8% 0 4%;
  }
</style>
