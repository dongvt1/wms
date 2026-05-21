<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="isUpdate ? 'Sửa vật tư' : 'Thêm vật tư'" width="90%"
    @ok="handleSubmit">
    <BasicForm @register="registerForm" />

    <!-- Linh kiện thay thế -->
    <a-divider>Linh kiện có thể thay thế</a-divider>
    <a-table :dataSource="substitutes" :columns="subColumns" :pagination="false" size="small" bordered class="mb-2">
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'substituteMaterialId'">
          <a-select v-model:value="record.substituteMaterialId" show-search :options="materialOptions"
            :filter-option="filterOption" placeholder="Chọn vật tư thay thế" style="width:100%" />
        </template>
        <template v-if="column.key === 'priority'">
          <a-input-number v-model:value="record.priority" :min="1" :max="99" style="width:100%" />
        </template>
        <template v-if="column.key === 'notes'">
          <a-input v-model:value="record.notes" placeholder="Ghi chú..." />
        </template>
        <template v-if="column.key === 'action'">
          <a-button danger size="small" @click="removeSub(index)">Xóa</a-button>
        </template>
      </template>
    </a-table>
    <a-button type="dashed" block @click="addSub">+ Thêm linh kiện thay thế</a-button>
  </BasicModal>
</template>

<script lang="ts" name="material-modal" setup>
import { ref, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { materialApi, type MaterialSubstituteModel } from '/@/api/common/material';
import { UNIT_OPTIONS, unitFilterOption } from '/@/utils/unitOptions';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);
const recordId = ref('');
const substitutes = ref<MaterialSubstituteModel[]>([]);
const materialOptions = ref<any[]>([]);

const subColumns = [
  { title: 'Vật tư thay thế', key: 'substituteMaterialId', width: '45%' },
  { title: 'Ưu tiên', key: 'priority', width: '15%' },
  { title: 'Ghi chú', key: 'notes', width: '30%' },
  { title: '', key: 'action', width: '10%' },
];

const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
  labelWidth: 130,
  schemas: [
    { field: 'code', label: 'Mã vật tư', component: 'Input', required: true, colProps: { span: 12 } },
    { field: 'name', label: 'Tên vật tư', component: 'Input', required: true, colProps: { span: 12 } },
    {
      field: 'unit', label: 'Đơn vị tính', component: 'Select', colProps: { span: 12 },
      componentProps: { options: UNIT_OPTIONS, showSearch: true, filterOption: unitFilterOption, allowClear: true, placeholder: 'Chọn đơn vị...' }
    },
    {
      field: 'price', label: 'Giá tham khảo', component: 'InputNumber', colProps: { span: 12 },
      componentProps: { min: 0, style: 'width:100%', addonAfter: 'VNĐ' }
    },

    // ── Tồn kho & trạng thái ──
    {
      field: 'minStockLevel', label: 'Tồn kho tối thiểu', component: 'InputNumber', colProps: { span: 12 },
      componentProps: { min: 0, style: 'width:100%' }
    },
    {
      field: 'status', label: 'Trạng thái', component: 'Select', defaultValue: 1, colProps: { span: 12 },
      componentProps: {
        options: [
          { label: 'Đang dùng', value: 1 },
          { label: 'Ngừng', value: 0 },
        ],
      },
    },
    // ── Ảnh ──
    {
      field: 'image', label: 'Ảnh vật tư', component: 'JImageUpload',
      componentProps: { fileMax: 1 },
      colProps: { span: 24 },
    },
    // ── Kích thước & cân nặng ──

    {
      field: 'length', label: 'Chiều dài', component: 'InputNumber', colProps: { span: 12 },
      componentProps: { min: 0, precision: 2, style: 'width:100%', addonAfter: 'mm' }
    },
    {
      field: 'width', label: 'Chiều rộng', component: 'InputNumber', colProps: { span: 12 },
      componentProps: { min: 0, precision: 2, style: 'width:100%', addonAfter: 'mm' }
    },
    {
      field: 'height', label: 'Chiều cao', component: 'InputNumber', colProps: { span: 12 },
      componentProps: { min: 0, precision: 2, style: 'width:100%', addonAfter: 'mm' }
    },
    {
      field: 'weight', label: 'Cân nặng', component: 'InputNumber', colProps: { span: 12 },
      componentProps: { min: 0, precision: 3, style: 'width:100%', addonAfter: 'g' }
    },
    {
      field: 'description', label: 'Mô tả', component: 'InputTextArea',
      componentProps: { rows: 2, style: 'width:100%' }, colProps: { span: 24 }
    },
  ],
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  resetFields();
  substitutes.value = [];
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;

  // Load tất cả vật tư cho dropdown chọn substitute
  try {
    const mats: any = await materialApi.listAll();
    materialOptions.value = (mats || []).map((m: any) => ({
      label: `${m.code} - ${m.name}`,
      value: m.id,
    }));
  } catch (e) { }

  if (unref(isUpdate) && data.record) {
    recordId.value = data.record.id;
    setFieldsValue({ ...data.record });
    try {
      const subs: any = await materialApi.getSubstitutes(data.record.id);
      substitutes.value = (subs || []).map((s: any) => ({ ...s }));
    } catch (e) { }
  }
});

function addSub() {
  substitutes.value.push({ substituteMaterialId: undefined, priority: 1, notes: '' });
}

function removeSub(idx: number) {
  substitutes.value.splice(idx, 1);
}

function filterOption(input: string, option: any) {
  return option.label.toLowerCase().includes(input.toLowerCase());
}

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    const payload = {
      material: { ...values, id: unref(isUpdate) ? unref(recordId) : undefined },
      substitutes: substitutes.value.filter(s => s.substituteMaterialId),
    };
    if (unref(isUpdate)) {
      await materialApi.edit(payload);
    } else {
      await materialApi.add(payload);
    }
    emit('success');
    closeModal();
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
