<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="800">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema, getCategoryOptions } from './product.data';
  import { productApi } from './product.api';

  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');

  // Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  // form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // Get category options
    await fetchCategories();

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      setFieldsValue({
        ...data.record,
      });
    }
  });

  // Get category options
  async function fetchCategories() {
    try {
      const categoryOptions = await getCategoryOptions();
      
      // Update category options in form schema
      updateSchema([
        {
          field: 'categoryId',
          componentProps: {
            options: categoryOptions,
          },
        },
      ]);
    } catch (error) {
      console.error('Failed to fetch category list:', error);
    }
  }

  // Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Product' : 'Edit Product'));

  // form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(isUpdate)) {
        await productApi.update({ ...values, id: rowId.value });
      } else {
        await productApi.save(values);
      }

      // Close pop-up window
      closeModal();
      // Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number){
    width: 100%;
  }
</style>