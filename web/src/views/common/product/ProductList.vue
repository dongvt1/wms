<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">Thêm sản phẩm</a-button>
      </template>
    </BasicTable>

    <ProductModal @register="registerModal" @success="handleSuccess" />
    <ProductHistoryModal @register="registerHistoryModal" />
    <ProductBomModal @register="registerBomModal" />
  </div>
</template>

<script lang="ts" name="product-list" setup>
import { onMounted } from 'vue';
import { BasicTable, TableAction, useTable, ActionItem } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { columns, searchFormSchema, getCategoryOptions } from './product.data';
import { productApi } from './product.api';
import ProductModal from './ProductModal.vue';
import ProductHistoryModal from './ProductHistoryModal.vue';
import ProductBomModal from './ProductBomModal.vue';

const [registerTable, { reload, setProps }] = useTable({
  title: 'Quản lý sản phẩm',
  api: productApi.list,
  columns,
  formConfig: { labelWidth: 120, schemas: searchFormSchema, autoSubmitOnEnter: true },
  useSearchForm: true,
  showTableSetting: true,
  bordered: true,
  showIndexColumn: true,
  actionColumn: {
    width: 260,
    title: 'Thao tác',
    dataIndex: 'action',
    slots: { customRender: 'action' },
    fixed: 'right',
  },
});

const [registerModal, { openModal }] = useModal();
const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();
const [registerBomModal, { openModal: openBomModal }] = useModal();

function getTableAction(record): ActionItem[] {
  return [
    {
      label: 'Sửa',
      onClick: () => openModal(true, { record, isUpdate: true }),
    },
    {
      label: 'BOM',
      icon: 'ant-design:apartment-outlined',
      onClick: () =>
        openBomModal(true, {
          productId: record.id,
          productName: record.name,
          productCode: record.code,
        }),
    },
    {
      label: 'Lịch sử',
      onClick: () => openHistoryModal(true, { productId: record.id, productName: record.name }),
    },
    {
      label: 'Xóa',
      color: 'error',
      popConfirm: {
        title: 'Xác nhận xóa sản phẩm này?',
        confirm: () => handleDelete(record),
      },
    },
  ];
}

function handleAdd() { openModal(true, { isUpdate: false }); }

async function handleDelete(record) {
  await productApi.delete({ id: record.id });
  reload();
}

function handleSuccess() { reload(); }

onMounted(async () => {
  try {
    const categoryOptions = await getCategoryOptions();
    setProps({
      formConfig: {
        labelWidth: 120,
        schemas: searchFormSchema.map((s) =>
          s.field === 'categoryId'
            ? { ...s, componentProps: { ...s.componentProps, options: categoryOptions } }
            : s,
        ),
        autoSubmitOnEnter: true,
      },
    });
  } catch { }
});
</script>