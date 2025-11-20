<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { adjustmentFormSchema } from './inventory.data';
  import { inventoryApi } from './inventory.api';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const isAdjustment = ref(false);
  const currentInventory = ref<any>({});
  const productOptions = ref([]);

  const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 100,
    schemas: adjustmentFormSchema,
    showActionButtonGroup: false,
    baseColProps: { lg: 24, md: 24 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    isAdjustment.value = !!data?.isAdjustment;
    currentInventory.value = data?.record || {};
    productOptions.value = data?.productOptions || [];

    if (unref(isUpdate) || unref(isAdjustment)) {
      const record = data?.record;
      if (record) {
        setFieldsValue({
          productId: record.productId,
          currentQuantity: record.quantity,
          newQuantity: record.quantity,
        });
      }
    }

    // Update product options in form schema
    updateSchema([
      {
        field: 'productId',
        componentProps: {
          options: productOptions.value,
        },
      },
    ]);
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新增库存' : '调整库存'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(isAdjustment)) {
        // Handle inventory adjustment
        const adjustmentParams = {
          productId: values.productId,
          newQuantity: values.newQuantity,
          adjustmentReason: values.reason,
        };

        await inventoryApi.adjust(adjustmentParams);
      } else {
        // Handle inventory update
        const updateParams = {
          productId: values.productId,
          minStockThreshold: values.minStockThreshold || currentInventory.value?.minStockThreshold || 0,
        };

        await inventoryApi.update(updateParams);
      }

      closeModal();
      emit('success');
    } catch (error) {
      console.error('Submit failed:', error);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<script lang="ts">
export default defineComponent({
  name: 'InventoryAdjustmentModal',
});
</script>