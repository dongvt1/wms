<template>
  <div class="p-4 approval-panel">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button @click="reload">
          <ReloadOutlined /> Làm mới
        </a-button>
      </template>
      <template #stageType="{ record }">
        <a-tag :color="stageTypeColor(record.stageType)">
          {{ stageTypeLabel(record.stageType) }}
        </a-tag>
      </template>
      <template #overallResult="{ record }">
        <a-tag v-if="record.overallResult === 'pass'" color="green">PASS</a-tag>
        <a-tag v-else-if="record.overallResult === 'fail'" color="red">FAIL</a-tag>
        <a-tag v-else color="default">Chưa đánh giá</a-tag>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: 'Xem chi tiết', onClick: () => handleViewDetail(record) },
          ]"
          :dropDownActions="[
            { label: 'Phê duyệt', onClick: () => handleApprove(record) },
            { label: 'Từ chối', onClick: () => handleReject(record) },
            { label: 'Kiểm tra lại', onClick: () => handleReInspect(record) },
          ]"
        />
      </template>
    </BasicTable>

    <!-- Detail Drawer -->
    <a-drawer
      v-model:open="detailVisible"
      title="Chi tiết kết quả kiểm tra"
      :width="720"
      :destroy-on-close="true"
    >
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleApprove(selectedExecution!)">
            <CheckCircleOutlined /> Phê duyệt
          </a-button>
          <a-button danger @click="handleReject(selectedExecution!)">
            <CloseCircleOutlined /> Từ chối
          </a-button>
          <a-button @click="handleReInspect(selectedExecution!)">
            <UndoOutlined /> Kiểm tra lại
          </a-button>
        </a-space>
      </template>

      <a-spin :spinning="detailLoading">
        <template v-if="selectedExecution">
          <!-- Execution info -->
          <a-descriptions :column="2" bordered size="small" class="mb-4">
            <a-descriptions-item label="Mã phiên">
              {{ selectedExecution.executionCode }}
            </a-descriptions-item>
            <a-descriptions-item label="Template">
              {{ selectedExecution.templateName }}
            </a-descriptions-item>
            <a-descriptions-item label="Loại QC">
              <a-tag :color="stageTypeColor(selectedExecution.stageType)">
                {{ stageTypeLabel(selectedExecution.stageType) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Kết quả tổng">
              <a-tag v-if="selectedExecution.overallResult === 'pass'" color="green">PASS</a-tag>
              <a-tag v-else-if="selectedExecution.overallResult === 'fail'" color="red">FAIL</a-tag>
              <a-tag v-else color="default">--</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Người kiểm tra">
              {{ selectedExecution.inspector || '--' }}
            </a-descriptions-item>
            <a-descriptions-item label="Ngày kiểm tra">
              {{ selectedExecution.inspectionDate || '--' }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- Steps results -->
          <a-collapse v-model:activeKey="activeStepKeys" class="step-results">
            <a-collapse-panel
              v-for="step in sortedSteps"
              :key="step.id"
              :header="step.stepName"
            >
              <template #extra>
                <a-space @click.stop>
                  <a-tag v-if="step.isMandatory" color="red" size="small">Bắt buộc</a-tag>
                  <a-tag
                    v-if="step.result === 'pass'"
                    color="green"
                    size="small"
                  >PASS</a-tag>
                  <a-tag
                    v-else-if="step.result === 'fail'"
                    color="red"
                    size="small"
                  >FAIL</a-tag>
                  <a-tag v-else color="default" size="small">Pending</a-tag>
                </a-space>
              </template>

              <!-- Field values (read-only) -->
              <div
                v-for="field in sortedFields(step)"
                :key="field.id"
                class="field-result-item"
              >
                <div class="field-result-header">
                  <span class="field-name">{{ field.fieldName }}</span>
                  <a-tag
                    v-if="field.result === 'pass'"
                    color="green"
                    size="small"
                  >PASS</a-tag>
                  <a-tag
                    v-else-if="field.result === 'fail'"
                    color="red"
                    size="small"
                  >FAIL</a-tag>
                  <a-tag
                    v-else-if="field.result === 'na'"
                    color="default"
                    size="small"
                  >N/A</a-tag>
                </div>
                <div class="field-result-body">
                  <span class="field-value">
                    <strong>Giá trị:</strong>
                    {{ formatFieldValue(field) }}
                  </span>
                  <span v-if="field.evalMessage" class="eval-message">
                    {{ field.evalMessage }}
                  </span>
                </div>
              </div>

              <a-empty
                v-if="!step.fields || step.fields.length === 0"
                description="Không có trường dữ liệu"
              />
            </a-collapse-panel>
          </a-collapse>
        </template>
      </a-spin>
    </a-drawer>

    <!-- Approve Modal -->
    <a-modal
      v-model:open="approveModalVisible"
      title="Phê duyệt kết quả kiểm tra"
      @ok="confirmApprove"
      :confirm-loading="approving"
      ok-text="Phê duyệt"
      cancel-text="Hủy"
    >
      <a-form layout="vertical">
        <a-form-item label="Nhận xét (tùy chọn)">
          <a-textarea
            v-model:value="approveComment"
            placeholder="Nhập nhận xét nếu có..."
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Reject Modal -->
    <a-modal
      v-model:open="rejectModalVisible"
      title="Từ chối kết quả kiểm tra"
      @ok="confirmReject"
      :confirm-loading="rejecting"
      :ok-button-props="{ danger: true, disabled: !rejectReason.trim() }"
      ok-text="Từ chối"
      cancel-text="Hủy"
    >
      <a-form layout="vertical">
        <a-form-item
          label="Lý do từ chối"
          required
          :validate-status="!rejectReason.trim() ? 'error' : ''"
          :help="!rejectReason.trim() ? 'Vui lòng nhập lý do từ chối' : ''"
        >
          <a-textarea
            v-model:value="rejectReason"
            placeholder="Nhập lý do từ chối (bắt buộc)..."
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Re-inspect Modal -->
    <a-modal
      v-model:open="reInspectModalVisible"
      title="Yêu cầu kiểm tra lại"
      @ok="confirmReInspect"
      :confirm-loading="reInspecting"
      :ok-button-props="{ disabled: !reInspectStepId || !reInspectReason.trim() }"
      ok-text="Yêu cầu kiểm tra lại"
      cancel-text="Hủy"
    >
      <a-form layout="vertical">
        <a-form-item
          label="Chọn bước cần kiểm tra lại"
          required
          :validate-status="!reInspectStepId ? 'error' : ''"
          :help="!reInspectStepId ? 'Vui lòng chọn bước kiểm tra' : ''"
        >
          <a-select
            v-model:value="reInspectStepId"
            placeholder="Chọn bước kiểm tra..."
            :options="reInspectStepOptions"
          />
        </a-form-item>
        <a-form-item
          label="Lý do yêu cầu kiểm tra lại"
          required
          :validate-status="!reInspectReason.trim() ? 'error' : ''"
          :help="!reInspectReason.trim() ? 'Vui lòng nhập lý do' : ''"
        >
          <a-textarea
            v-model:value="reInspectReason"
            placeholder="Nhập lý do yêu cầu kiểm tra lại (bắt buộc)..."
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import {
    ReloadOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    UndoOutlined,
  } from '@ant-design/icons-vue';
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    wmsApprovalApi,
    type PendingApprovalModel,
  } from '/@/api/wms/approval';
  import {
    wmsInspectionExecutionApi,
    type InspectionExecutionModel,
    type StepResultModel,
    type FieldValueModel,
  } from '/@/api/wms/inspectionExecution';

  const { createMessage } = useMessage();

  // --- Table ---
  const [registerTable, { reload }] = useTable({
    title: 'Danh sách chờ phê duyệt',
    api: wmsApprovalApi.pending,
    columns: [
      { title: 'Mã phiên', dataIndex: 'executionCode', width: 150 },
      { title: 'Template', dataIndex: 'templateName', width: 200 },
      { title: 'Loại QC', dataIndex: 'stageType', slots: { customRender: 'stageType' }, width: 100 },
      { title: 'Người kiểm tra', dataIndex: 'inspector', width: 140 },
      { title: 'Ngày kiểm tra', dataIndex: 'inspectionDate', width: 130 },
      { title: 'Kết quả', dataIndex: 'overallResult', slots: { customRender: 'overallResult' }, width: 110 },
      { title: 'Ngày nộp', dataIndex: 'updateTime', width: 160 },
    ],
    useSearchForm: false,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: {
      width: 180,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  // --- Detail Drawer ---
  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const selectedExecution = ref<InspectionExecutionModel | null>(null);
  const activeStepKeys = ref<string[]>([]);

  const sortedSteps = computed<StepResultModel[]>(() => {
    if (!selectedExecution.value) return [];
    return [...(selectedExecution.value.steps || [])].sort((a, b) => a.sortOrder - b.sortOrder);
  });

  function sortedFields(step: StepResultModel): FieldValueModel[] {
    return [...(step.fields || [])].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
  }

  function formatFieldValue(field: FieldValueModel): string {
    if (field.actualValue === null || field.actualValue === undefined || field.actualValue === '') {
      return '--';
    }
    if (field.fieldType === 'boolean') {
      const config = field.fieldConfig as { trueLabel?: string; falseLabel?: string } | null;
      if (field.actualValue === 'true' || field.actualValue === '1') {
        return config?.trueLabel || 'Đạt';
      }
      return config?.falseLabel || 'Không đạt';
    }
    return field.actualValue;
  }

  async function handleViewDetail(record: PendingApprovalModel) {
    detailVisible.value = true;
    detailLoading.value = true;
    try {
      const data = await wmsInspectionExecutionApi.queryById(record.id);
      selectedExecution.value = data;
      // Expand all steps by default
      activeStepKeys.value = (data.steps || []).map((s) => s.id);
    } catch (e: any) {
      createMessage.error('Không thể tải chi tiết phiên kiểm tra');
    } finally {
      detailLoading.value = false;
    }
  }

  // --- Approve ---
  const approveModalVisible = ref(false);
  const approveComment = ref('');
  const approving = ref(false);
  let approveTargetId = '';

  function handleApprove(record: PendingApprovalModel) {
    approveTargetId = record.id;
    approveComment.value = '';
    approveModalVisible.value = true;
  }

  async function confirmApprove() {
    approving.value = true;
    try {
      await wmsApprovalApi.approve(approveTargetId, {
        comment: approveComment.value || undefined,
      });
      createMessage.success('Phê duyệt thành công');
      approveModalVisible.value = false;
      detailVisible.value = false;
      reload();
    } catch (e: any) {
      // Global error handler
    } finally {
      approving.value = false;
    }
  }

  // --- Reject ---
  const rejectModalVisible = ref(false);
  const rejectReason = ref('');
  const rejecting = ref(false);
  let rejectTargetId = '';

  function handleReject(record: PendingApprovalModel) {
    rejectTargetId = record.id;
    rejectReason.value = '';
    rejectModalVisible.value = true;
  }

  async function confirmReject() {
    if (!rejectReason.value.trim()) {
      createMessage.warning('Vui lòng nhập lý do từ chối');
      return;
    }
    rejecting.value = true;
    try {
      await wmsApprovalApi.reject(rejectTargetId, {
        reason: rejectReason.value.trim(),
      });
      createMessage.success('Đã từ chối kết quả kiểm tra');
      rejectModalVisible.value = false;
      detailVisible.value = false;
      reload();
    } catch (e: any) {
      // Global error handler
    } finally {
      rejecting.value = false;
    }
  }

  // --- Re-inspect ---
  const reInspectModalVisible = ref(false);
  const reInspectStepId = ref<string | undefined>(undefined);
  const reInspectReason = ref('');
  const reInspecting = ref(false);
  let reInspectTargetId = '';

  const reInspectStepOptions = computed(() => {
    if (!selectedExecution.value) return [];
    return sortedSteps.value.map((step) => ({
      label: `${step.stepName}${step.result === 'fail' ? ' (FAIL)' : ''}`,
      value: step.stepId,
    }));
  });

  function handleReInspect(record: PendingApprovalModel) {
    reInspectTargetId = record.id;
    reInspectStepId.value = undefined;
    reInspectReason.value = '';
    // If detail is loaded, use it for step options; otherwise load it
    if (selectedExecution.value && selectedExecution.value.id === record.id) {
      reInspectModalVisible.value = true;
    } else {
      // Load execution detail first for step options
      loadExecutionForReInspect(record.id);
    }
  }

  async function loadExecutionForReInspect(id: string) {
    try {
      const data = await wmsInspectionExecutionApi.queryById(id);
      selectedExecution.value = data;
      reInspectModalVisible.value = true;
    } catch (e: any) {
      createMessage.error('Không thể tải dữ liệu phiên kiểm tra');
    }
  }

  async function confirmReInspect() {
    if (!reInspectStepId.value) {
      createMessage.warning('Vui lòng chọn bước cần kiểm tra lại');
      return;
    }
    if (!reInspectReason.value.trim()) {
      createMessage.warning('Vui lòng nhập lý do');
      return;
    }
    reInspecting.value = true;
    try {
      await wmsApprovalApi.reInspect(reInspectTargetId, {
        stepId: reInspectStepId.value,
        reason: reInspectReason.value.trim(),
      });
      createMessage.success('Đã yêu cầu kiểm tra lại');
      reInspectModalVisible.value = false;
      detailVisible.value = false;
      reload();
    } catch (e: any) {
      // Global error handler
    } finally {
      reInspecting.value = false;
    }
  }

  // --- Helpers ---
  function stageTypeColor(type: string) {
    const map: Record<string, string> = { iqc: 'blue', pqc: 'orange', fqc: 'green' };
    return map[type] || 'default';
  }

  function stageTypeLabel(type: string) {
    const map: Record<string, string> = { iqc: 'IQC', pqc: 'PQC', fqc: 'FQC' };
    return map[type] || type?.toUpperCase();
  }
</script>

<style scoped>
  .approval-panel {
    max-width: 1400px;
    margin: 0 auto;
  }

  .step-results {
    margin-top: 16px;
  }

  .field-result-item {
    padding: 10px 12px;
    margin-bottom: 8px;
    background: #fafafa;
    border-radius: 6px;
    border: 1px solid #f0f0f0;
  }

  .field-result-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
  }

  .field-name {
    font-weight: 500;
    color: #333;
  }

  .field-result-body {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;
    color: #666;
  }

  .field-value {
    flex-shrink: 0;
  }

  .eval-message {
    color: #999;
    font-style: italic;
  }
</style>
