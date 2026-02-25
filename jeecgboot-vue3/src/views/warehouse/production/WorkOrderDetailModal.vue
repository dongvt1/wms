<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Chi tiết lệnh sản xuất" width="860px" :footer="null">
    <div v-if="detail">
      <!-- Work Order Info -->
      <a-descriptions bordered :column="3" size="small" class="mb-4">
        <a-descriptions-item label="Mã lệnh">{{ detail.workOrder?.orderCode }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">
          <a-tag :color="statusColor(detail.workOrder?.status)">{{ statusLabel(detail.workOrder?.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Ưu tiên">{{ detail.workOrder?.priority }}</a-descriptions-item>
        <a-descriptions-item label="SL kế hoạch">{{ detail.workOrder?.plannedQuantity }}</a-descriptions-item>
        <a-descriptions-item label="SL thực tế">{{ detail.workOrder?.actualQuantity || '-' }}</a-descriptions-item>
        <a-descriptions-item label="Ngày BĐ KH">{{ detail.workOrder?.plannedStartDate }}</a-descriptions-item>
        <a-descriptions-item label="Ngày KT KH">{{ detail.workOrder?.plannedEndDate }}</a-descriptions-item>
        <a-descriptions-item label="Ngày BĐ TT">{{ detail.workOrder?.actualStartDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="Ngày KT TT">{{ detail.workOrder?.actualEndDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="Ghi chú" :span="3">{{ detail.workOrder?.notes || '-' }}</a-descriptions-item>
      </a-descriptions>

      <!-- Stages -->
      <a-divider>Tiến độ công đoạn</a-divider>
      <a-steps v-if="detail.stages?.length" direction="vertical" size="small">
        <a-step
          v-for="stage in detail.stages"
          :key="stage.id"
          :title="stage.stageName"
          :description="`Người PT: ${stage.assignee || '-'} | KH: ${stage.plannedDurationHours || 0}h | TT: ${stage.actualDurationHours || 0}h`"
          :status="stageStatus(stage.status)"
        />
      </a-steps>
      <a-empty v-else description="Chưa có công đoạn" />

      <!-- Logs -->
      <a-divider>Nhật ký sản xuất</a-divider>
      <a-timeline v-if="detail.logs?.length">
        <a-timeline-item
          v-for="log in detail.logs"
          :key="log.id"
          :color="logColor(log.action)"
        >
          <b>{{ log.action }}</b> – {{ log.operator || 'System' }}
          <div class="text-gray-500 text-xs">{{ log.logTime }} | {{ log.notes }}</div>
        </a-timeline-item>
      </a-timeline>
      <a-empty v-else description="Chưa có nhật ký" />
    </div>
    <a-spin v-else tip="Đang tải..." />
  </BasicModal>
</template>

<script lang="ts" name="work-order-detail-modal" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { workOrderApi } from '/@/api/warehouse/workOrder';

  const emit = defineEmits(['success', 'register']);
  const detail = ref<any>(null);

  const [registerModal] = useModalInner(async (data) => {
    detail.value = null;
    if (data?.id) {
      const result: any = await workOrderApi.queryById(data.id);
      detail.value = result;
    }
  });

  function statusColor(s: string) {
    return { draft: 'default', planned: 'blue', in_progress: 'orange', completed: 'green', cancelled: 'red' }[s] || 'default';
  }
  function statusLabel(s: string) {
    return { draft: 'Nháp', planned: 'Kế hoạch', in_progress: 'Đang SX', completed: 'Hoàn thành', cancelled: 'Đã hủy' }[s] || s;
  }
  function stageStatus(s: string) {
    return { pending: 'wait', in_progress: 'process', completed: 'finish', skipped: 'error' }[s] || 'wait';
  }
  function logColor(action: string) {
    return { CREATE: 'blue', START: 'orange', COMPLETE: 'green', CANCEL: 'red', STAGE_UPDATE: 'purple' }[action] || 'blue';
  }
</script>
