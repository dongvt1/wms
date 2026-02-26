<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="700px" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { dataRuleFormSchema } from './menu.data';
  import { saveOrUpdateRule } from './menu.api';
  // Emits declaration
  const emit = defineEmits(['success', 'register']);
  const props = defineProps({ permissionId: String });
  const isUpdate = ref(true);
  // Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: dataRuleFormSchema,
    showActionButtonGroup: false,
  });
  // Form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      // Form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  // Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Rule' : 'Edit Rule'));

  // Form submit event
  async function handleSubmit() {
    try {
      const values = await validate();
      values.permissionId = props.permissionId;
      setModalProps({ confirmLoading: true });
      // Submit form
      await saveOrUpdateRule(values, isUpdate.value);
      // Close modal
      closeModal();
      // Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
