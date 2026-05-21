<template>
  <div class="template-assignment-panel">
    <a-card title="Gán Template cho Sản phẩm" size="small">
      <!-- Add Assignment Section -->
      <div class="add-assignment-section">
        <a-row :gutter="12" align="middle">
          <a-col :span="6">
            <a-select
              v-model:value="newAssignment.assignmentType"
              placeholder="Loại gán"
              style="width: 100%"
              @change="handleTypeChange"
            >
              <a-select-option value="product">Sản phẩm</a-select-option>
              <a-select-option value="product_group">Nhóm sản phẩm</a-select-option>
              <a-select-option value="default">Mặc định</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="12">
            <!-- Product search select -->
            <a-select
              v-if="newAssignment.assignmentType === 'product'"
              v-model:value="newAssignment.targetId"
              placeholder="Tìm kiếm sản phẩm..."
              style="width: 100%"
              show-search
              :filter-option="false"
              :not-found-content="productSearching ? undefined : null"
              @search="handleProductSearch"
            >
              <template #notFoundContent>
                <a-spin size="small" />
              </template>
              <a-select-option
                v-for="item in productOptions"
                :key="item.id"
                :value="item.id"
              >
                <span>{{ item.code }} - {{ item.name }}</span>
              </a-select-option>
            </a-select>

            <!-- Product group search select -->
            <a-select
              v-else-if="newAssignment.assignmentType === 'product_group'"
              v-model:value="newAssignment.targetId"
              placeholder="Tìm kiếm nhóm sản phẩm..."
              style="width: 100%"
              show-search
              :filter-option="filterCategoryOption"
            >
              <a-select-option
                v-for="item in categoryOptions"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </a-select-option>
            </a-select>

            <!-- Default type: no target needed -->
            <span v-else-if="newAssignment.assignmentType === 'default'" class="default-hint">
              Template mặc định áp dụng khi không có template riêng
            </span>
          </a-col>
          <a-col :span="6">
            <a-button
              type="primary"
              :loading="adding"
              :disabled="!canAdd"
              @click="handleAdd"
            >
              <PlusOutlined /> Gán
            </a-button>
          </a-col>
        </a-row>
      </div>

      <a-divider style="margin: 12px 0" />

      <!-- Current Assignments List -->
      <a-spin :spinning="loading">
        <a-table
          :dataSource="assignments"
          :columns="columns"
          :pagination="false"
          size="small"
          rowKey="id"
          :locale="{ emptyText: 'Chưa có sản phẩm/nhóm nào được gán' }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'assignmentType'">
              <a-tag :color="assignmentTypeColor(record.assignmentType)">
                {{ assignmentTypeLabel(record.assignmentType) }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'targetName'">
              <span v-if="record.assignmentType === 'default'" class="text-muted">
                — Mặc định —
              </span>
              <span v-else>{{ record.targetName || record.targetId }}</span>
            </template>
            <template v-if="column.dataIndex === 'action'">
              <a-popconfirm
                title="Bạn có chắc muốn gỡ gán này?"
                ok-text="Gỡ"
                cancel-text="Hủy"
                @confirm="handleRemove(record)"
              >
                <a-button type="link" danger size="small">
                  <DeleteOutlined /> Gỡ
                </a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, onMounted } from 'vue';
  import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import { wmsTemplateAssignmentApi, TemplateAssignmentModel } from '/@/api/wms/templateAssignment';
  import { wmsProductApi, ProductModel } from '/@/api/wms/product';
  import { wmsCategoryApi, CategoryModel } from '/@/api/wms/category';

  const props = defineProps<{
    templateId: string;
  }>();

  // --- State ---
  const loading = ref(false);
  const adding = ref(false);
  const productSearching = ref(false);
  const assignments = ref<TemplateAssignmentModel[]>([]);
  const productOptions = ref<ProductModel[]>([]);
  const categoryOptions = ref<CategoryModel[]>([]);

  const newAssignment = reactive({
    assignmentType: undefined as string | undefined,
    targetId: undefined as string | undefined,
  });

  // --- Table columns ---
  const columns = [
    {
      title: 'Loại gán',
      dataIndex: 'assignmentType',
      width: 140,
    },
    {
      title: 'Sản phẩm / Nhóm',
      dataIndex: 'targetName',
    },
    {
      title: 'Ngày gán',
      dataIndex: 'createTime',
      width: 160,
    },
    {
      title: 'Thao tác',
      dataIndex: 'action',
      width: 100,
      align: 'center' as const,
    },
  ];

  // --- Computed ---
  const canAdd = computed(() => {
    if (!newAssignment.assignmentType) return false;
    if (newAssignment.assignmentType === 'default') return true;
    return !!newAssignment.targetId;
  });

  // --- Load assignments ---
  async function loadAssignments() {
    if (!props.templateId) return;
    loading.value = true;
    try {
      const res = await wmsTemplateAssignmentApi.list({ templateId: props.templateId });
      assignments.value = res?.records || [];
    } catch (e: any) {
      console.error('Failed to load assignments:', e);
    } finally {
      loading.value = false;
    }
  }

  // --- Load category options ---
  async function loadCategories() {
    try {
      const res = await wmsCategoryApi.getTree();
      categoryOptions.value = res || [];
    } catch (e: any) {
      console.error('Failed to load categories:', e);
    }
  }

  // --- Product search with debounce ---
  let searchTimer: ReturnType<typeof setTimeout> | null = null;

  function handleProductSearch(value: string) {
    if (searchTimer) clearTimeout(searchTimer);
    if (!value || value.length < 1) {
      productOptions.value = [];
      return;
    }
    productSearching.value = true;
    searchTimer = setTimeout(async () => {
      try {
        const res = await wmsProductApi.search({ keyword: value });
        productOptions.value = res?.records || [];
      } catch (e: any) {
        productOptions.value = [];
      } finally {
        productSearching.value = false;
      }
    }, 300);
  }

  // --- Filter category option for local search ---
  function filterCategoryOption(input: string, option: any) {
    const label = option.children?.[0]?.children || option.label || '';
    return label.toLowerCase().includes(input.toLowerCase());
  }

  // --- Handle type change ---
  function handleTypeChange() {
    newAssignment.targetId = undefined;
    productOptions.value = [];
  }

  // --- Add assignment ---
  async function handleAdd() {
    if (!props.templateId || !newAssignment.assignmentType) return;
    adding.value = true;
    try {
      await wmsTemplateAssignmentApi.add({
        templateId: props.templateId,
        assignmentType: newAssignment.assignmentType,
        targetId: newAssignment.assignmentType === 'default' ? undefined : newAssignment.targetId,
      });
      message.success('Gán template thành công');
      // Reset form
      newAssignment.assignmentType = undefined;
      newAssignment.targetId = undefined;
      productOptions.value = [];
      // Reload list
      await loadAssignments();
    } catch (e: any) {
      message.error(e?.message || 'Gán template thất bại');
    } finally {
      adding.value = false;
    }
  }

  // --- Remove assignment ---
  async function handleRemove(record: TemplateAssignmentModel) {
    if (!record.id) return;
    try {
      await wmsTemplateAssignmentApi.delete(record.id);
      message.success('Đã gỡ gán template');
      await loadAssignments();
    } catch (e: any) {
      message.error(e?.message || 'Gỡ gán thất bại');
    }
  }

  // --- Helpers ---
  function assignmentTypeColor(type: string) {
    const map: Record<string, string> = {
      product: 'blue',
      product_group: 'green',
      default: 'orange',
    };
    return map[type] || 'default';
  }

  function assignmentTypeLabel(type: string) {
    const map: Record<string, string> = {
      product: 'Sản phẩm',
      product_group: 'Nhóm SP',
      default: 'Mặc định',
    };
    return map[type] || type;
  }

  // --- Watch templateId changes ---
  watch(
    () => props.templateId,
    (newId) => {
      if (newId) {
        loadAssignments();
      } else {
        assignments.value = [];
      }
    },
  );

  // --- Init ---
  onMounted(() => {
    if (props.templateId) {
      loadAssignments();
    }
    loadCategories();
  });

  defineExpose({ reload: loadAssignments });
</script>

<style scoped>
  .template-assignment-panel {
    margin-top: 16px;
  }

  .add-assignment-section {
    padding: 8px 0;
  }

  .default-hint {
    color: #8c8c8c;
    font-size: 13px;
    font-style: italic;
  }

  .text-muted {
    color: #8c8c8c;
  }
</style>
