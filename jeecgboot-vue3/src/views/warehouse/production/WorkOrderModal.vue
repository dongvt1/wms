<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? 'Sửa lệnh sản xuất' : 'Tạo lệnh sản xuất'"
    width="780px"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />

    <!-- Production Stages -->
    <a-divider>Công đoạn sản xuất (tùy chọn)</a-divider>
    <div v-for="(stage, idx) in stages" :key="idx" class="stage-row">
      <a-row :gutter="8" align="middle">
        <a-col :span="1"><span class="stage-num">{{ idx + 1 }}</span></a-col>
        <a-col :span="8">
          <a-input v-model:value="stage.stageName" placeholder="Tên công đoạn" />
        </a-col>
        <a-col :span="5">
          <a-input-number v-model:value="stage.plannedDurationHours" :min="0"
            placeholder="Giờ KH" style="width:100%" />
        </a-col>
        <a-col :span="6">
          <a-input v-model:value="stage.assignee" placeholder="Người phụ trách" />
        </a-col>
        <a-col :span="4">
          <a-button danger size="small" @click="removeStage(idx)">Xóa</a-button>
        </a-col>
      </a-row>
    </div>
    <a-button type="dashed" block @click="addStage" class="mt-2">
      + Thêm công đoạn
    </a-button>
  </BasicModal>
</template>

<script lang="ts" name="work-order-modal" setup>
  import { ref, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { workOrderApi } from '/@/api/warehouse/workOrder';
  import { bomApi } from '/@/api/warehouse/bom';
  import { productionLineApi } from '/@/api/warehouse/productionLine';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const recordId = ref('');
  const stages = ref<any[]>([]);
  const bomOptions = ref<any[]>([]);
  const lineOptions = ref<any[]>([]);

  const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 130,
    schemas: [
      { field: 'orderCode', label: 'Mã lệnh SX', component: 'Input',
        componentProps: { placeholder: 'Để trống hệ thống tự tạo' } },
      { field: 'bomId', label: 'BOM (Định mức NVL)', component: 'Select', required: true,
        componentProps: { options: [], placeholder: 'Chọn BOM' } },
      { field: 'productionLineId', label: 'Dây chuyền', component: 'Select',
        componentProps: { options: [], placeholder: 'Chọn dây chuyền' } },
      { field: 'plannedQuantity', label: 'SL kế hoạch', component: 'InputNumber', required: true,
        componentProps: { min: 0.001, style: 'width: 100%' } },
      { field: 'priority', label: 'Ưu tiên', component: 'Select', defaultValue: 'normal',
        componentProps: {
          options: [
            { label: 'Thấp', value: 'low' },
            { label: 'Bình thường', value: 'normal' },
            { label: 'Cao', value: 'high' },
            { label: 'Khẩn cấp', value: 'urgent' },
          ],
        },
      },
      { field: 'plannedStartDate', label: 'Ngày BĐ kế hoạch', component: 'DatePicker',
        componentProps: { style: 'width:100%', valueFormat: 'YYYY-MM-DD' } },
      { field: 'plannedEndDate', label: 'Ngày KT kế hoạch', component: 'DatePicker',
        componentProps: { style: 'width:100%', valueFormat: 'YYYY-MM-DD' } },
      { field: 'notes', label: 'Ghi chú', component: 'InputTextArea',
        componentProps: { rows: 2 } },
    ],
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    stages.value = [];
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // Load options
    const [bomList, lineList]: any[] = await Promise.all([
      bomApi.listActive(),
      productionLineApi.listAll(),
    ]);
    const bOpts = (bomList?.records || bomList || []).map((b: any) => ({ label: `${b.bomCode} - ${b.bomName}`, value: b.id }));
    const lOpts = (lineList?.records || lineList || []).map((l: any) => ({ label: l.lineName, value: l.id }));
    updateSchema([
      { field: 'bomId', componentProps: { options: bOpts } },
      { field: 'productionLineId', componentProps: { options: lOpts } },
    ]);

    if (unref(isUpdate) && data.record) {
      recordId.value = data.record.id;
      setFieldsValue({ ...data.record });
    }
  });

  function addStage() {
    stages.value.push({ stageName: '', plannedDurationHours: null, assignee: '' });
  }

  function removeStage(idx: number) {
    stages.value.splice(idx, 1);
  }

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      const payload = {
        workOrder: { ...values, id: unref(isUpdate) ? unref(recordId) : undefined },
        stages: stages.value.filter(s => s.stageName),
      };
      if (unref(isUpdate)) {
        await workOrderApi.edit(payload.workOrder);
      } else {
        await workOrderApi.add(payload);
      }
      emit('success');
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style scoped>
.stage-row { margin-bottom: 8px; }
.stage-num { font-weight: bold; color: #1890ff; }
</style>
