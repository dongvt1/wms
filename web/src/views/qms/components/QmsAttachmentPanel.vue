<template>
  <div class="qms-attachment-panel">
    <a-divider>
      <span>Tệp đính kèm ({{ fileList.length }}/10)</span>
    </a-divider>

    <a-upload-dragger
      v-model:file-list="uploadFileList"
      name="file"
      :multiple="true"
      :action="uploadAction"
      :headers="headers"
      :data="uploadData"
      :accept="acceptFormats"
      :disabled="isMaxReached || !entityId"
      :before-upload="beforeUpload"
      :show-upload-list="false"
      @change="handleUploadChange"
    >
      <p class="ant-upload-drag-icon">
        <InboxOutlined />
      </p>
      <p class="ant-upload-text">Kéo thả hoặc nhấn để tải lên</p>
      <p class="ant-upload-hint">
        Hỗ trợ: JPG, PNG, PDF, DOCX, XLSX — Tối đa 10MB/tệp
      </p>
      <p v-if="isMaxReached" class="ant-upload-hint" style="color: #ff4d4f">
        Đã đạt giới hạn 10 tệp đính kèm
      </p>
    </a-upload-dragger>

    <a-table
      v-if="fileList.length > 0"
      :data-source="fileList"
      :columns="columns"
      :pagination="false"
      size="small"
      bordered
      class="mt-3"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'fileSize'">
          {{ formatFileSize(record.fileSize) }}
        </template>
        <template v-if="column.key === 'fileType'">
          <a-tag>{{ (record.fileType || '').toUpperCase() }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-popconfirm title="Xóa tệp này?" @confirm="handleDelete(record.id)">
            <a-button type="link" danger size="small">Xóa</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>

    <a-empty v-if="fileList.length === 0 && !loading" description="Chưa có tệp đính kèm" class="mt-3" />
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, reactive } from 'vue';
import { InboxOutlined } from '@ant-design/icons-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { getToken } from '/@/utils/auth';
import { getTenantId } from '/@/utils/auth';
import { useGlobSetting } from '/@/hooks/setting';
import { wmsAttachmentApi, qmsAttachmentUploadUrl } from '/@/api/wms/qmsAttachment';

const props = defineProps<{
  entityType: 'iqc' | 'pqc' | 'fqc' | 'ncr';
  entityId: string;
}>();

const { createMessage } = useMessage();
const globSetting = useGlobSetting();

// Upload configuration
const uploadAction = computed(() => `${globSetting.uploadUrl}${qmsAttachmentUploadUrl}`);
const headers = reactive({
  'X-Access-Token': getToken() as string,
  'X-Tenant-Id': getTenantId() || '0',
});
const uploadData = computed(() => ({
  entityType: props.entityType,
  entityId: props.entityId,
}));

// Accepted file formats
const acceptFormats = '.jpg,.jpeg,.png,.pdf,.docx,.xlsx';
const allowedExtensions = ['jpg', 'jpeg', 'png', 'pdf', 'docx', 'xlsx'];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
const MAX_FILE_COUNT = 10;

// State
const fileList = ref<any[]>([]);
const uploadFileList = ref<any[]>([]);
const loading = ref(false);

const isMaxReached = computed(() => fileList.value.length >= MAX_FILE_COUNT);

// Table columns
const columns = [
  { title: 'Tên tệp', dataIndex: 'fileName', key: 'fileName', ellipsis: true },
  { title: 'Loại', key: 'fileType', width: 80, align: 'center' as const },
  { title: 'Dung lượng', key: 'fileSize', width: 100, align: 'right' as const },
  { title: '', key: 'action', width: 60, align: 'center' as const },
];

// Load attachments when entityId changes
watch(
  () => props.entityId,
  async (newId) => {
    if (newId) {
      await loadAttachments();
    } else {
      fileList.value = [];
    }
  },
  { immediate: true }
);

async function loadAttachments() {
  if (!props.entityId) return;
  loading.value = true;
  try {
    const res = await wmsAttachmentApi.list(props.entityType, props.entityId);
    fileList.value = res || [];
  } catch (e) {
    console.error('Failed to load attachments', e);
    fileList.value = [];
  } finally {
    loading.value = false;
  }
}

function beforeUpload(file: File): boolean | Promise<boolean> {
  // Check file count
  if (fileList.value.length >= MAX_FILE_COUNT) {
    createMessage.error('Đã đạt giới hạn 10 tệp đính kèm');
    return false;
  }

  // Check file format
  const ext = file.name.split('.').pop()?.toLowerCase() || '';
  if (!allowedExtensions.includes(ext)) {
    createMessage.error('Định dạng tệp không được hỗ trợ. Chấp nhận: JPG, PNG, PDF, DOCX, XLSX');
    return false;
  }

  // Check file size
  if (file.size > MAX_FILE_SIZE) {
    createMessage.error('Dung lượng tệp vượt quá 10MB');
    return false;
  }

  return true;
}

function handleUploadChange(info: any) {
  if (info.file.status === 'done') {
    if (info.file.response?.success) {
      createMessage.success(`${info.file.name} tải lên thành công`);
      loadAttachments();
    } else {
      createMessage.error(info.file.response?.message || `${info.file.name} tải lên thất bại`);
    }
    // Clear the upload list after processing
    uploadFileList.value = [];
  } else if (info.file.status === 'error') {
    createMessage.error(`${info.file.name} tải lên thất bại`);
    uploadFileList.value = [];
  }
}

async function handleDelete(id: string) {
  try {
    await wmsAttachmentApi.delete(id);
    createMessage.success('Đã xóa tệp đính kèm');
    await loadAttachments();
  } catch (e) {
    createMessage.error('Xóa tệp thất bại');
  }
}

function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
</script>

<style scoped>
.qms-attachment-panel {
  margin-top: 8px;
}
</style>
