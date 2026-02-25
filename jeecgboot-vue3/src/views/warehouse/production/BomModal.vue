<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? 'Sửa BOM' : 'Thêm BOM'"
    width="820px"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />

    <!-- BOM Items (NVL) -->
    <a-divider>Nguyên vật liệu (NVL)</a-divider>
    <a-table
      :dataSource="bomItems"
      :columns="itemColumns"
      :pagination="false"
      size="small"
      bordered
      class="mb-2"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'materialId'">
          <a-select
            v-model:value="record.materialId"
            show-search
            :options="productOptions"
            :filter-option="filterOption"
            placeholder="Chọn NVL"
            style="width: 100%"
          />
        </template>
        <template v-if="column.key === 'quantity'">
          <a-input-number v-model:value="record.quantity" :min="0.001" style="width:100%" />
        </template>
        <template v-if="column.key === 'unit'">
          <a-input v-model:value="record.unit" placeholder="cái, kg..." />
        </template>
        <template v-if="column.key === 'action'">
          <a-button danger size="small" @click="removeItem(index)">Xóa</a-button>
        </template>
      </template>
    </a-table>
    <a-button type="dashed" block @click="addItem">+ Thêm nguyên vật liệu</a-button>
  </BasicModal>
</template>

<script lang="ts" name="bom-modal" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { bomApi } from '/@/api/warehouse/bom';
  import { productApi } from '/@/api/warehouse/product';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const recordId = ref('');
  const bomItems = ref<any[]>([]);
  const productOptions = ref<any[]>([]);

  const itemColumns = [
    { title: 'Nguyên vật liệu', key: 'materialId', width: '45%' },
    { title: 'Số lượng', key: 'quantity', width: '20%' },
    { title: 'Đơn vị', key: 'unit', width: '20%' },
    { title: '', key: 'action', width: '15%' },
  ];

  const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 140,
    schemas: [
      { field: 'bomCode', label: 'Mã BOM', component: 'Input', required: true },
      { field: 'bomName', label: 'Tên BOM', component: 'Input', required: true },
      { field: 'productId', label: 'Thành phẩm đầu ra', component: 'Select', required: true,
        componentProps: { options: [], showSearch: true, placeholder: 'Chọn thành phẩm' } },
      { field: 'outputQuantity', label: 'SL thành phẩm ĐR', component: 'InputNumber', required: true,
        componentProps: { min: 0.001, style: 'width: 100%' } },
      { field: 'unit', label: 'Đơn vị thành phẩm', component: 'Input',
        componentProps: { placeholder: 'cái, kg, hộp...' } },
      { field: 'version', label: 'Phiên bản', component: 'Input', defaultValue: '1.0' },
      { field: 'status', label: 'Trạng thái', component: 'Select', defaultValue: 'active',
        componentProps: {
          options: [
            { label: 'Đang dùng', value: 'active' },
            { label: 'Ngừng', value: 'inactive' },
          ],
        },
      },
      { field: 'notes', label: 'Ghi chú', component: 'InputTextArea', componentProps: { rows: 2 } },
    ],
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    bomItems.value = [];
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // Load all products for selection
    try {
      const pResult: any = await productApi.list({ pageSize: 999 });
      const prods = pResult?.records || pResult || [];
      const opts = prods.map((p: any) => ({ label: `${p.code} - ${p.name}`, value: p.id }));
      productOptions.value = opts;
      updateSchema([{ field: 'productId', componentProps: { options: opts } }]);
    } catch (e) {}

    if (unref(isUpdate) && data.record) {
      recordId.value = data.record.id;
      setFieldsValue({ ...data.record });
      // Load existing items
      try {
        const items: any = await bomApi.getItems(data.record.id);
        bomItems.value = (items || []).map((i: any) => ({ ...i }));
      } catch (e) {}
    }
  });

  function addItem() {
    bomItems.value.push({ materialId: undefined, quantity: 1, unit: '' });
  }

  function removeItem(idx: number) {
    bomItems.value.splice(idx, 1);
  }

  function filterOption(input: string, option: any) {
    return option.label.toLowerCase().includes(input.toLowerCase());
  }

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      const payload = {
        bom: { ...values, id: unref(isUpdate) ? unref(recordId) : undefined },
        items: bomItems.value.filter(i => i.materialId && i.quantity),
      };
      if (unref(isUpdate)) {
        await bomApi.edit(payload);
      } else {
        await bomApi.add(payload);
      }
      emit('success');
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
