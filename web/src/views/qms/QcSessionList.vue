<template>
    <div class="p-4">
        <BasicTable @register="registerTable">
            <template #toolbar>
                <a-button type="primary" @click="handleAdd">+ Tạo phiên kiểm tra</a-button>
            </template>
            <template #action="{ record }">
                <TableAction :actions="getActions(record)" />
            </template>
            <template #status="{ record }">
                <a-tag :color="record.status === 'completed' ? 'green' : 'blue'">
                    {{ record.status === 'completed' ? 'Hoàn thành' : 'Nháp' }}
                </a-tag>
            </template>
        </BasicTable>
        <QcSessionModal @register="registerModal" @success="reload" />
        <QcSessionDetailModal @register="registerDetailModal" @success="reload" />
    </div>
</template>

<script lang="ts" name="qc-session-list" setup>
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { qcSessionApi } from '/@/api/warehouse/qcSession';
import QcSessionModal from './QcSessionModal.vue';
import QcSessionDetailModal from './QcSessionDetailModal.vue';

const { createMessage } = useMessage();

const [registerTable, { reload }] = useTable({
    title: 'Phiên kiểm tra công đoạn',
    api: qcSessionApi.list,
    columns: [
        { title: 'Mã phiên', dataIndex: 'sessionCode', width: 160 },
        { title: 'Công đoạn', dataIndex: 'stageName', width: 200 },
        { title: 'Mã WO', dataIndex: 'workOrderId', width: 160 },
        { title: 'Người KT', dataIndex: 'inspector', width: 130 },
        { title: 'Ngày KT', dataIndex: 'inspectionDate', width: 120 },
        { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 120 },
    ],
    formConfig: {
        labelWidth: 100,
        schemas: [
            { field: 'sessionCode', label: 'Mã phiên', component: 'Input', colProps: { span: 6 } },
            { field: 'workOrderId', label: 'Mã WO', component: 'Input', colProps: { span: 6 } },
            {
                field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
                componentProps: { options: [{ label: 'Nháp', value: 'draft' }, { label: 'Hoàn thành', value: 'completed' }] }
            },
        ],
        autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 200, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
});

const [registerModal, { openModal }] = useModal();
const [registerDetailModal, { openModal: openDetailModal }] = useModal();

function handleAdd() { openModal(true, { isUpdate: false }); }

function getActions(record: any) {
    const actions: any[] = [
        { label: 'Chi tiết', onClick: () => openDetailModal(true, { id: record.id }) },
    ];
    if (record.status === 'draft') {
        actions.push({ label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) });
        actions.push({
            label: 'Hoàn thành', type: 'primary',
            popConfirm: {
                title: 'Xác nhận hoàn thành phiên kiểm tra?',
                confirm: async () => {
                    await qcSessionApi.complete(record.id);
                    createMessage.success('Đã hoàn thành phiên kiểm tra!');
                    reload();
                },
            },
        });
        actions.push({
            label: 'Xóa', color: 'error',
            popConfirm: {
                title: 'Xác nhận xóa phiên kiểm tra?',
                confirm: async () => {
                    await qcSessionApi.delete({ id: record.id });
                    createMessage.success('Xóa thành công!');
                    reload();
                },
            },
        });
    }
    return actions;
}
</script>
