<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-dropdown v-if="showBatchBtn">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
                <Icon icon="ant-design:delete-outlined"></Icon>
                <span>delete</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Batch operations</span>
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>

      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <ManageDrawer @register="registerDrawer" />
  </div>
</template>

<script lang="ts" setup name="message-manage">
  import { unref, computed } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import ManageDrawer from './ManageDrawer.vue';
  import { Api, list, deleteBatch } from './manage.api';
  import { columns, searchFormSchema } from './manage.data';

  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'message-manage',
    tableProps: {
      title: 'Message center template list data',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: Api.exportXls,
      name: 'Message center template list',
    },
    importConfig: {
      url: Api.importXls,
      success: () => reload(),
    },
  });

  // register ListTable
  const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys }] = tableContext;
  const showBatchBtn = computed(() => selectedRowKeys.value.length > 0);

  const [registerDrawer, { openDrawer }] = useDrawer();

  function onDetail(record) {
    openDrawer(true, { record: record });
  }

  function onDelete(record) {
    if (record) {
      doDeleteDepart([record.id], false);
    }
  }

  async function onDeleteBatch() {
    try {
      await doDeleteDepart(selectedRowKeys);
      selectedRowKeys.value = [];
    } finally {
    }
  }

  /**
   * according to ids 批量delete
   * @param idListRef array
   * @param confirm Whether to display a confirmation prompt box
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        setLoading(true);
        await deleteBatch({ ids: idList.join(',') }, confirm);
        await reload();
      } finally {
        setLoading(false);
      }
    }
  }

  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'Details', onClick: onDetail.bind(null, record) }];
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: 'delete',
        color: 'error',
        popConfirm: {
          title: '确认要delete吗？',
          confirm: onDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style lang="less">
  @import 'index';
</style>
