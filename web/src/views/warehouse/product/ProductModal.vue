<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="800">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema, getCategoryOptions } from './product.data';
import { productApi } from './product.api';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(true);
const rowId = ref('');

const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
  schemas: formSchema,
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  await resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;

  // Load category options
  const categoryOptions = await getCategoryOptions();
  updateSchema([{ field: 'categoryId', componentProps: { options: categoryOptions } }]);

  if (unref(isUpdate)) {
    rowId.value = data.record.id;
    setFieldsValue({ ...data.record });
  }
});

const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm sản phẩm' : 'Sửa sản phẩm'));

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    if (unref(isUpdate)) {
      await productApi.update({ ...values, id: rowId.value });
    } else {
      await productApi.save(values);
    }
    closeModal();
    emit('success');
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>

<style lang="less" scoped>
:deep(.ant-input-number) {
  width: 100%;
}
</style>