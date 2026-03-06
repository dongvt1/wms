<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="isUpdate ? 'Sửa BOM' : 'Thêm BOM'" width="960px"
    @ok="handleSubmit">
    <BasicForm @register="registerForm" />

    <!-- BOM Items (NVL) -->
    <a-divider>Nguyên vật liệu (NVL)</a-divider>
    <a-table :dataSource="bomItems" :columns="itemColumns" :pagination="false" size="small" bordered class="mb-2"
      :expandable="{ expandedRowRender }" row-key="_idx">
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'materialId'">
          <a-select v-model:value="record.materialId" show-search :options="materialOptions"
            :filter-option="filterOption" placeholder="Chọn vật tư" style="width: 100%" />
        </template>
        <template v-if="column.key === 'quantity'">
          <a-input-number v-model:value="record.quantity" :min="0.001" style="width:100%" />
        </template>
        <template v-if="column.key === 'unit'">
          <a-select v-model:value="record.unit" show-search :options="UNIT_OPTIONS" :filter-option="unitFilterOption"
            placeholder="Đơn vị" style="width:100%" allow-clear />
        </template>
        <template v-if="column.key === 'substituteCount'">
          <a-tag color="blue">{{ (record.substitutes || []).length }} TT</a-tag>
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
import { ref, unref, h } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { bomApi } from '/@/api/common/bom';
import { materialApi } from '/@/api/common/material';
import { Button, InputNumber, Input, Select } from 'ant-design-vue';
import { UNIT_OPTIONS, unitFilterOption } from '/@/utils/unitOptions';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);
const recordId = ref('');
const bomItems = ref<any[]>([]);
const materialOptions = ref<any[]>([]);

const itemColumns = [
  { title: 'Vật tư', key: 'materialId', width: '35%' },
  { title: 'SL', key: 'quantity', width: '12%' },
  { title: 'Đơn vị', key: 'unit', width: '12%' },
  { title: 'Thay thế', key: 'substituteCount', width: '12%' },
  { title: '', key: 'action', width: '10%' },
];

// Expanded row: inline substitute sub-table
const expandedRowRender = (record: any) => {
  const subs = record.substitutes || [];

  const subTable = h('div', { class: 'p-2 bg-gray-50' }, [
    h('div', { class: 'text-sm font-medium mb-2 text-gray-600' }, 'Linh kiện thay thế:'),
    h('div', {},
      subs.map((sub: any, idx: number) =>
        h('div', { key: idx, class: 'flex gap-2 mb-1 items-center' }, [
          h(Select, {
            value: sub.substituteMaterialId,
            options: materialOptions.value,
            showSearch: true,
            filterOption: filterOption,
            placeholder: 'Vật tư thay thế',
            style: 'width: 280px',
            onChange: (v: any) => { sub.substituteMaterialId = v; },
          }),
          h(InputNumber, {
            value: sub.priority, min: 1, max: 99,
            style: 'width: 80px',
            placeholder: 'TT',
            onChange: (v: any) => { sub.priority = v; },
          }),
          h(Input, {
            value: sub.notes, placeholder: 'Ghi chú...',
            style: 'width: 180px',
            onChange: (e: any) => { sub.notes = e.target.value; },
          }),
          h(Button, {
            danger: true, size: 'small',
            onClick: () => { subs.splice(idx, 1); },
          }, () => 'Xóa'),
        ])
      )
    ),
    h(Button, {
      type: 'dashed', size: 'small',
      onClick: () => subs.push({ substituteMaterialId: undefined, priority: 1, notes: '' }),
    }, () => '+ Thêm thay thế'),
  ]);

  return subTable;
};

const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
  labelWidth: 140,
  schemas: [
    { field: 'bomCode', label: 'Mã BOM', component: 'Input', required: true, colProps: { span: 12 } },
    { field: 'bomName', label: 'Tên BOM', component: 'Input', required: true, colProps: { span: 12 } },
    {
      field: 'productId', label: 'Thành phẩm đầu ra', component: 'Select', required: true,
      componentProps: { options: [], showSearch: true, placeholder: 'Chọn thành phẩm' }
    },
    {
      field: 'outputQuantity', label: 'SL thành phẩm ĐR', component: 'InputNumber', required: true,
      componentProps: { min: 0.001, style: 'width: 100%' }, colProps: { span: 12 }
    },
    {
      field: 'unit', label: 'Đơn vị thành phẩm', component: 'Select', colProps: { span: 12 },
      componentProps: { options: UNIT_OPTIONS, showSearch: true, filterOption: unitFilterOption, allowClear: true, placeholder: 'Chọn đơn vị...' }
    },
    { field: 'version', label: 'Phiên bản', component: 'Input', defaultValue: '1.0', colProps: { span: 12 } },
    {
      field: 'status', label: 'Trạng thái', component: 'Select', defaultValue: 'active', colProps: { span: 12 },
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

  // Load vật tư (material) cho dropdown
  try {
    const mats: any = await materialApi.listAll();
    const opts = (mats || []).map((m: any) => ({ label: `${m.code} - ${m.name}`, value: m.id }));
    materialOptions.value = opts;
  } catch (e) { }

  // Load products cho dropdown thành phẩm
  try {
    const { productApi } = await import('/@/api/warehouse/product');
    const pResult: any = await productApi.list({ pageSize: 999 });
    const prods = pResult?.records || pResult || [];
    const opts = prods.map((p: any) => ({ label: `${p.code} - ${p.name}`, value: p.id }));
    updateSchema([{ field: 'productId', componentProps: { options: opts } }]);
  } catch (e) { }

  if (unref(isUpdate) && data.record) {
    recordId.value = data.record.id;
    setFieldsValue({ ...data.record });
    try {
      const items: any = await bomApi.getItems(data.record.id);
      bomItems.value = (items || []).map((i: any, idx: number) => ({
        ...i, _idx: idx, substitutes: i.substitutes || [],
      }));
    } catch (e) { }
  }
});

function addItem() {
  bomItems.value.push({
    materialId: undefined, quantity: 1, unit: '',
    substitutes: [], _idx: Date.now(),
  });
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
      items: bomItems.value
        .filter(i => i.materialId && i.quantity)
        .map(i => ({
          ...i,
          substitutes: (i.substitutes || []).filter((s: any) => s.substituteMaterialId),
        })),
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
