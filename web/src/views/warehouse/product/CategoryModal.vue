<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="700">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { categoryFormSchema } from './category.data';
  import { categoryApi } from './category.api';

  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');

  // Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: categoryFormSchema,
    showActionButtonGroup: false,
  });

  // form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // Get category tree options
    await fetchCategoryTree();

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      setFieldsValue({
        ...data.record,
      });
    }
  });

  // Get category tree options
  async function fetchCategoryTree() {
    try {
      const categoryTree = await categoryApi.getTree();
      
      // Transform tree data for TreeSelect component
      const treeData = transformToTreeSelectData(categoryTree);
      
      // Update parent category options in form schema
      updateSchema([
        {
          field: 'parentId',
          componentProps: {
            treeData: treeData,
          },
        },
      ]);
    } catch (error) {
      console.error('Failed to fetch category tree:', error);
    }
  }

  // Transform category data to TreeSelect format
  function transformToTreeSelectData(categories, excludeId = null) {
    return categories
      .filter(category => category.id !== excludeId)
      .map(category => ({
        title: category.name,
        value: category.id,
        key: category.id,
        children: category.children ? transformToTreeSelectData(category.children, excludeId) : [],
      }));
  }

  // Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Category' : 'Edit Category'));

  // form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(isUpdate)) {
        await categoryApi.update({ ...values, id: rowId.value });
      } else {
        await categoryApi.save(values);
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