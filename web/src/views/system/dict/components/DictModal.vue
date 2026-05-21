<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" width="550px" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { formSchema } from '../dict.data';
  import { saveOrUpdateDict } from '../dict.api';
  // Emits declaration
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const rowId = ref('');
  // Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  // Form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Reset form
    await resetFields();
    setModalProps({ confirmLoading: false, minHeight: 80 });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      // Form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  // Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Dictionary' : 'Edit Dictionary'));
  // Form submit event
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      // Submit form
      await saveOrUpdateDict(values, isUpdate.value);
      // Close modal
      closeModal();
      // Refresh list
      emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
