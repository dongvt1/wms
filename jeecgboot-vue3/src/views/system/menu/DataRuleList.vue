<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="Data Permission Rules" :width="adaptiveWidth" :rootClassName="prefixCls">
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate"> Add New</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicDrawer>
  <DataRuleModal @register="registerModal" @success="reload" :permissionId="permissionId" />
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import DataRuleModal from './DataRuleModal.vue';
  import { dataRuleColumns, dataRuleSearchFormSchema } from './menu.data';
  import { dataRuleList, deleteRule } from './menu.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';
  import { useDesign } from '/@/hooks/web/useDesign';
  const permissionId = ref('');
  const { adaptiveWidth } = useDrawerAdaptiveWidth();
  const { prefixCls } = useDesign('sys-menu-dataRulelist');
  //Permission rule modal
  const [registerModal, { openModal }] = useModal();
  const [registerDrawer] = useDrawerInner(async (data) => {
    permissionId.value = data.id;
    setProps({ searchInfo: { permissionId: unref(permissionId) } });
    reload();
  });
  // Adaptive column configuration
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 8, // ≥1200px
    xxl: 8, // ≥1600px
  };
  const [registerTable, { reload, setProps }] = useTable({
    api: dataRuleList,
    columns: dataRuleColumns,
    size: 'small',
    formConfig: {
      baseColProps: adaptiveColProps,
      labelAlign: 'right',
      labelCol: {
        offset: 1,
        xs: 24,
        sm: 24,
        md: 24,
        lg: 8,
        xl: 8,
        xxl: 8,
      },
      wrapperCol: {},
      schemas: dataRuleSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    bordered: true,
    showIndexColumn: false,
    showTableSetting: false,
    canResize: false,
    immediate: false,
    actionColumn: {
      width: 100,
      title: 'Actions',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * Add New
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Edit
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Delete
   */
  async function handleDelete(record) {
    await deleteRule({ id: record.id }, reload);
  }

  /**
   * Action column
   */
  function getTableAction(record) {
    return [
      {
        label: 'Edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Delete',
        popConfirm: {
          title: 'Are you sure to delete?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
<style lang="less">
  // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1660】Menu management-There is no gap between the query and buttons of data permissions
  @prefix-cls: ~'@{namespace}-sys-menu-dataRulelist';
  .@{prefix-cls} {
    .jeecg-basic-table {
      padding: 0;
    }
    .btnArea {
      .ant-btn {
        &:last-child {
          margin-right: 0;
        }
        &:first-child {
          margin-left: 8px;
        }
      }
    }
  }
  // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1660】Menu management-There is no gap between the query and buttons of data permissions
</style>
