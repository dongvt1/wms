<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="700">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema, getWarehouseAreaOptions } from './shelf.data';
  import { warehouseShelfApi } from './shelf.api';

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

    // Get a list of warehouse areas
    await fetchWarehouseAreas();

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      setFieldsValue({
        ...data.record,
      });
    }
  });

  // Get a list of warehouse areas
  async function fetchWarehouseAreas() {
    try {
      const areaOptions = await getWarehouseAreaOptions();
      
      // update formschemaregional options in
      updateSchema([
        {
          field: 'areaId',
          componentProps: {
            options: areaOptions,
          },
        },
      ]);
    } catch (error) {
      console.error('Failed to fetch warehouse area list:', error);
    }
  }

  // Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Warehouse Shelf' : 'Edit Warehouse Shelf'));

  // form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(isUpdate)) {
        await warehouseShelfApi.update({ ...values, id: rowId.value });
      } else {
        await warehouseShelfApi.save(values);
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