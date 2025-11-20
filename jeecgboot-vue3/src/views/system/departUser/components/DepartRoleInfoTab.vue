<template>
  <!--Reference table-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--slot:tabletitle-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="addDepartRole" :disabled="!departId">Add department role</a-button>
      <template v-if="selectedRowKeys.length > 0">
        <a-divider type="vertical" />
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteDepartRoleBatch">
                <icon icon="ant-design:delete-outlined" />
                <span>delete</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Batch operations </span>
            <icon icon="akar-icons:chevron-down" />
          </a-button>
        </a-dropdown>
      </template>
    </template>
    <!-- slot：Inline action button -->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <!-- Add department pop-up window -->
  <DepartRoleModal :departId="departId" @register="registerFormModal" @success="onFormModalSuccess" />
  <DepartRoleAuthDrawer @register="registerAuthDrawer" />
</template>

<script lang="ts" setup>
  import { inject, ref, unref, watch, computed, onMounted } from 'vue';

  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import DepartRoleModal from './DepartRoleModal.vue';
  import DepartRoleAuthDrawer from './DepartRoleAuthDrawer.vue';
  import { deleteBatchDepartRole, departRoleList } from '../depart.user.api';
  import { departRoleColumns, departRoleSearchFormSchema } from '../depart.user.data';
  import { ColEx } from '/@/components/Form/src/types';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  defineEmits(['register']);
  // Currently selected departmentID，may be empty，Represents unselected departments
  const departId = computed(() => props.data?.id);

  // Adaptive column configuration
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 12, // ≥1200px
    xxl: 8, // ≥1600px
  };
  // List page public parameters、method
  const { tableContext, createMessage } = useListPage({
    tableProps: {
      api: departRoleList,
      columns: departRoleColumns,
      canResize: false,
      formConfig: {
        labelWidth: 100,
        schemas: departRoleSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 6,
        },
        wrapperCol: {},
        // Action button configuration
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
        showResetButton: !!departId.value,
        showSubmitButton: !!departId.value,
      },
      // 【issues/1064】Column settings cacheKey
      tableSetting: { cacheKey: 'depart_user_departInfo' },
      // Process parameters before requesting
      beforeFetch(params) {
        params.deptId = departId.value;
      },
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-53】When no department is selected，All department roles have been found out
      immediate: !!departId.value,
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-53】When no department is selected，All department roles have been found out
    },
  });

  // register ListTable
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  // registerFormPop-up window
  const [registerFormModal, formModal] = useModal();
  // registerAuthorizePop-up window抽屉
  const [registerAuthDrawer, authDrawer] = useDrawer();

  // monitor data Change，Reload data
  watch(
    () => props.data,
    () => reload()
  );
  onMounted(() => {
    // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-53】When no department is selected，All department roles have been found out
    // reload();
    // update-end--author:liaozhiyang---date:20240517---for：【TV360X-53】When no department is selected，All department roles have been found out
  });

  // Clear selected rows
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // Add department role
  function addDepartRole() {
    formModal.openModal(true, {
      isUpdate: false,
      record: {},
    });
  }

  // Editorial department roles
  function editDepartRole(record) {
    formModal.openModal(true, {
      isUpdate: true,
      record: record,
    });
  }

  // Authorized department roles
  function permissionDepartRole(record) {
    authDrawer.openDrawer(true, { record });
  }

  // 批量delete部门角色
  async function deleteDepartRole(idList, confirm) {
    if (!departId.value) {
      createMessage.warning('Please select a department first');
    } else {
      setLoading(true);
      let ids = unref(idList).join(',');
      try {
        await deleteBatchDepartRole({ ids }, confirm);
        return reload();
      } finally {
        setLoading(false);
      }
    }
    return Promise.reject();
  }

  // 批量delete部门角色事件
  async function onDeleteDepartRoleBatch() {
    try {
      await deleteDepartRole(selectedRowKeys, true);
      // 批量delete成功后清空选择
      clearSelection();
    } catch (e) {}
  }

  // 表单Pop-up window成功后的回调
  function onFormModalSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'edit', onClick: editDepartRole.bind(null, record) }];
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Authorize', onClick: permissionDepartRole.bind(null, record) },
      {
        label: 'delete',
        color: 'error',
        popConfirm: {
          title: '确认要delete吗？',
          confirm: deleteDepartRole.bind(null, [record.id], false),
        },
      },
    ];
  }
</script>
