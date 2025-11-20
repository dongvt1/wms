<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Route recycle bin" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable">
      <template #status="{ record, text }">
        <a-tag color="pink" v-if="text == 0">Disable</a-tag>
        <a-tag color="#87d068" v-if="text == 1">normal</a-tag>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { columns } from '../route.data';
  import { deleteRouteList, putRecycleBin, deleteRecycleBin } from '../route.api';
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //registertabledata
  const [registerTable, { reload }] = useTable({
    rowKey: 'id',
    api: deleteRouteList,
    columns: columns,
    striped: true,
    useSearchForm: false,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 150,
      title: 'operate',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  /**
   * Undo events
   */
  async function handleRevert(record) {
    await putRecycleBin({ ids: record.id }, reload);
    emit('success');
  }
  /**
   * delete event
   */
  async function handleDelete(record) {
    await deleteRecycleBin({ ids: record.id }, reload);
  }

  //获取Action bar事件
  function getTableAction(record) {
    return [
      {
        label: 'retrieve',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: '是否确认retrieve',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Delete completely',
        icon: 'ant-design:scissor-outlined',
        color: 'error',
        popConfirm: {
          title: 'Do you confirm deletion?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
