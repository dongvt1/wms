<template>
    <div class="p-4">
        <BasicTable @register="registerTable">
            <template #toolbar>
                <a-button type="primary" @click="handleAdd">+ Tạo công đoạn</a-button>
            </template>
            <template #action="{ record }">
                <TableAction :actions="getActions(record)" />
            </template>
            <template #status="{ record }">
                <a-tag :color="record.status === 'active' ? 'green' : 'default'">
                    {{ record.status === 'active' ? 'Đang dùng' : 'Ngừng' }}
                </a-tag>
            </template>
        </BasicTable>
        <QcStageModal @register="registerModal" @success="reload" />
    </div>
</template>

<script lang="ts" name="qc-stage-list" setup>
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { qcStageApi } from '/@/api/warehouse/qcStage';
import QcStageModal from './QcStageModal.vue';

const { createMessage } = useMessage();

const [registerTable, { reload }] = useTable({
    title: 'Danh sách công đoạn kiểm tra',
    api: qcStageApi.list,
    columns: [
        { title: 'Mã công đoạn', dataIndex: 'stageCode', width: 150 },
        { title: 'Tên công đoạn', dataIndex: 'stageName', width: 220 },
        { title: 'Mô tả', dataIndex: 'description', width: 280 },
        { title: 'Thứ tự', dataIndex: 'sortOrder', width: 80 },
        { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 110 },
    ],
    formConfig: {
        labelWidth: 100,
        schemas: [
            { field: 'stageCode', label: 'Mã', component: 'Input', colProps: { span: 6 } },
            { field: 'stageName', label: 'Tên', component: 'Input', colProps: { span: 6 } },
            {
                field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
                componentProps: { options: [{ label: 'Đang dùng', value: 'active' }, { label: 'Ngừng', value: 'inactive' }] }
            },
        ],
        autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 160, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
});

const [registerModal, { openModal }] = useModal();

function handleAdd() { openModal(true, { isUpdate: false }); }

function getActions(record: any) {
    return [
        { label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) },
        {
            label: 'Xóa', color: 'error',
            popConfirm: {
                title: 'Xác nhận xóa công đoạn?',
                confirm: async () => {
                    await qcStageApi.delete({ id: record.id });
                    createMessage.success('Xóa thành công!');
                    reload();
                },
            },
        },
    ];
}
</script>
