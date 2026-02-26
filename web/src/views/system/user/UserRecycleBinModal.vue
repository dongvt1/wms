<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="User recycle bin" :showOkBtn="false" width="1000px" destroyOnClose @fullScreen="handleFullScreen">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :scroll="scroll">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Batch delete
              </a-menu-item>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Batch restore
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action bar-->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, toRaw, unref, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { recycleColumns } from './user.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin } from './user.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createConfirm } = useMessage();
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  const scroll = ref({ y: 0 });
  //registertabledata
  const [registerTable, { reload }] = useTable({
    api: getRecycleBinList,
    columns: recycleColumns,
    rowKey: 'id',
    striped: true,
    useSearchForm: false,
    showTableSetting: false,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    pagination: true,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 150,
      title: 'operate',
      dataIndex: 'action',
      // slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  // update-begin--author:liaozhiyang---date:20240704---for：【TV360X-1657】系统User recycle bin弹窗分页展示在可视区内
  const handleFullScreen = (maximize) => {
    setTableHeight(maximize);
  };
  const setTableHeight = (maximize) => {
    const clientHeight = document.documentElement.clientHeight;
    scroll.value = {
      y: clientHeight - (maximize ? 300 : 500),
    };
  };
  setTableHeight(false);
  watch(
    checkedKeys,
    (newValue, oldValue) => {
      if (checkedKeys.value.length && oldValue.length == 0) {
        scroll.value = {
          y: scroll.value.y - 50,
        };
      } else if (checkedKeys.value.length == 0 && oldValue.length) {
        scroll.value = {
          y: scroll.value.y + 50,
        };
      }
    },
    { deep: true }
  );
  // update-end--author:liaozhiyang---date:20240704---for：【TV360X-1657】系统User recycle bin弹窗分页展示在可视区内
  /**
   * Select column configuration
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };
  /**
   * Select event
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
  /**
   * Undo events
   */
  async function handleRevert(record) {
    await putRecycleBin({ userIds: record.id }, reload);
    emit('success');
  }
  /**
   * Batch restore事件
   */
  function batchHandleRevert() {
    handleRevert({ id: toRaw(unref(checkedKeys)).join(',') });
  }
  /**
   * delete event
   */
  async function handleDelete(record) {
    await deleteRecycleBin({ userIds: record.id }, reload);
  }
  /**
   * Batch delete事件
   */
  function batchHandleDelete() {
    createConfirm({
      iconType: 'warning',
      title: 'delete',
      content: '确定要永久delete吗？delete后将不可恢复！',
      onOk: () => handleDelete({ id: toRaw(unref(checkedKeys)).join(',') }),
      onCancel() {},
    });
  }
  //获取Action bar事件
  function getTableAction(record) {
    return [
      {
        label: 'retrieve',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Confirm to restore',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: '彻底delete',
        icon: 'ant-design:scissor-outlined',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
