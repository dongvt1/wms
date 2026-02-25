<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? 'Sửa mẫu checklist' : 'Thêm mẫu checklist'"
    width="860px"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />

    <a-divider>Danh sách tiêu chí kiểm tra</a-divider>
    <a-table
      :dataSource="checklistItems"
      :columns="itemColumns"
      :pagination="false"
      size="small"
      bordered
      class="mb-2"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'criterionName'">
          <a-input v-model:value="record.criterionName" placeholder="Tên tiêu chí..." />
        </template>
        <template v-if="column.key === 'standardValue'">
          <a-input v-model:value="record.standardValue" placeholder="Giá trị chuẩn..." />
        </template>
        <template v-if="column.key === 'inputType'">
          <a-select v-model:value="record.inputType" style="width: 100%">
            <a-select-option value="pass_fail">Đạt/Không đạt</a-select-option>
            <a-select-option value="text">Văn bản</a-select-option>
            <a-select-option value="number">Số</a-select-option>
            <a-select-option value="select">Chọn</a-select-option>
          </a-select>
        </template>
        <template v-if="column.key === 'isRequired'">
          <a-switch v-model:checked="record.isRequired" :checked-value="1" :un-checked-value="0" />
        </template>
        <template v-if="column.key === 'action'">
          <a-button danger size="small" @click="removeItem(index)">Xóa</a-button>
        </template>
      </template>
    </a-table>
    <a-button type="dashed" block @click="addItem">+ Thêm tiêu chí</a-button>
  </BasicModal>
</template>

<script lang="ts" name="checklist-template-modal" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { qmsChecklistApi } from '/@/api/warehouse/qmsChecklist';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const recordId = ref('');
  const checklistItems = ref<any[]>([]);

  const itemColumns = [
    { title: '#', key: 'no', width: 40, customRender: ({ index }: any) => index + 1 },
    { title: 'Tên tiêu chí *', key: 'criterionName', width: '28%' },
    { title: 'Giá trị chuẩn', key: 'standardValue', width: '22%' },
    { title: 'Kiểu nhập', key: 'inputType', width: '18%' },
    { title: 'Bắt buộc', key: 'isRequired', width: '12%' },
    { title: '', key: 'action', width: '10%' },
  ];

  const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 140,
    schemas: [
      { field: 'templateCode', label: 'Mã mẫu', component: 'Input', required: true },
      { field: 'templateName', label: 'Tên mẫu', component: 'Input', required: true },
      {
        field: 'inspectionType', label: 'Loại kiểm tra', component: 'Select', required: true,
        componentProps: {
          options: [
            { label: 'IQC - Kiểm tra đầu vào', value: 'iqc' },
            { label: 'PQC - Kiểm tra sản xuất', value: 'pqc' },
          ],
        },
      },
      { field: 'productId', label: 'Sản phẩm áp dụng', component: 'Input',
        helpMessage: 'Để trống = dùng chung cho tất cả sản phẩm' },
      {
        field: 'status', label: 'Trạng thái', component: 'Select', defaultValue: 'active',
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
    checklistItems.value = [];
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate) && data.record) {
      recordId.value = data.record.id;
      setFieldsValue({ ...data.record });
      try {
        const items: any = await qmsChecklistApi.getItems(data.record.id);
        checklistItems.value = (items || []).map((i: any) => ({ ...i }));
      } catch (e) {}
    }
  });

  function addItem() {
    checklistItems.value.push({
      criterionName: '',
      standardValue: '',
      inputType: 'pass_fail',
      isRequired: 1,
    });
  }

  function removeItem(idx: number) {
    checklistItems.value.splice(idx, 1);
  }

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      const payload = {
        template: { ...values, id: unref(isUpdate) ? unref(recordId) : undefined },
        items: checklistItems.value.filter((i) => i.criterionName),
      };
      if (unref(isUpdate)) {
        await qmsChecklistApi.edit(payload);
      } else {
        await qmsChecklistApi.add(payload);
      }
      emit('success');
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
