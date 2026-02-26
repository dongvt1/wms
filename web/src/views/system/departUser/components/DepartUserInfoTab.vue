<template>
  <!--Reference table-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--slot:tabletitle-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="selectAddUser" :disabled="!departId">Add existing user</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="createUser" :disabled="!departId">Create new user</a-button>
      <template v-if="selectedRowKeys.length > 0">
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onUnlinkDepartUserBatch">
                <icon icon="bx:bx-unlink" />
                <span>Disassociate</span>
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
  <UserDrawer @register="registerDrawer" @success="onUserDrawerSuccess" />
  <DepartRoleUserAuthDrawer @register="registerUserAuthDrawer" />
  <UserSelectModal ref="userSelectModalRef" rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectUserOk" />
</template>

<script lang="ts" setup>
  import { computed, inject, ref, unref, watch } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import UserDrawer from '/@/views/system/user/UserDrawer.vue';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import DepartRoleUserAuthDrawer from './DepartRoleUserAuthDrawer.vue';
  import { departUserList, linkDepartUserBatch, unlinkDepartUserBatch } from '../depart.user.api';
  import { userInfoColumns, userInfoSearchFormSchema } from '../depart.user.data';
  import { ColEx } from '/@/components/Form/src/types';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  const userSelectModalRef: any = ref(null);
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
      api: departUserList,
      columns: userInfoColumns,
      canResize: false,
      formConfig: {
        schemas: userInfoSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 5,
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
      tableSetting: { cacheKey: 'depart_user_userInfo' },
      // Process parameters before requesting
      beforeFetch(params) {
        params.depId = departId.value;
      },
      // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-1861】User information is not loaded when there is no department
      immediate: !!departId.value,
      // update-end--author:liaozhiyang---date:20240717---for：【TV360X-1861】User information is not loaded when there is no department
    },
  });

  // register ListTable
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  watch(
    () => props.data,
    () => reload()
  );
  //registerdrawer
  const [registerDrawer, { openDrawer, setDrawerProps }] = useDrawer();
  const [registerUserAuthDrawer, userAuthDrawer] = useDrawer();
  // register用户选择 modal
  const [registerSelUserModal, selUserModal] = useModal();

  // Clear selected rows
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // View department roles
  function showDepartRole(record) {
    userAuthDrawer.openDrawer(true, { userId: record.id, departId });
  }

  // Create user
  function createUser() {
    if (!departId.value) {
      createMessage.warning('Please select a department first');
    } else {
      openDrawer(true, {
        isUpdate: false,
        departDisabled: true,
        // Initialization responsible department
        nextDepartOptions: { value: props.data?.key, label: props.data?.title },
        record: {
          activitiSync: 1,
          userIdentity: 1,
          selecteddeparts: departId.value,
        },
      });
    }
  }

  // View user details
  function showUserDetail(record) {
    openDrawer(true, {
      record,
      isUpdate: true,
      departDisabled: true,
      showFooter: false,
    });
  }

  // Edit user information
  function editUserInfo(record) {
    openDrawer(true, { isUpdate: true, record, departDisabled: true });
  }

  // 选择Add existing user
  function selectAddUser() {
    // update-begin--author:liaozhiyang---date:20240308---for：【TV360X-1613】Opening again will still be the last selected user.，Not left blank
    userSelectModalRef.value.rowSelection.selectedRowKeys = [];
    // update-end--author:liaozhiyang---date:20240308---for：【TV360X-1613】Opening again will still be the last selected user.，Not left blank
    selUserModal.openModal();
  }

  // 批量Disassociate部门和用户之间的关系
  async function unlinkDepartUser(idList, confirm) {
    if (!departId.value) {
      createMessage.warning('Please select a department first');
    } else {
      setLoading(true);
      let userIds = unref(idList).join(',');
      try {
        await unlinkDepartUserBatch({ depId: departId.value, userIds }, confirm);
        return reload();
      } finally {
        setLoading(false);
      }
    }
    return Promise.reject();
  }

  // 批量Disassociate事件
  async function onUnlinkDepartUserBatch() {
    try {
      await unlinkDepartUser(selectedRowKeys, true);
      // Clear selection after successful batch deletion
      clearSelection();
    } catch (e) {}
  }

  // Select user successfully
  async function onSelectUserOk(options, userIdList) {
    if (userIdList.length > 0) {
      try {
        setLoading(true);
        await linkDepartUserBatch(departId.value, userIdList);
        reload();
      } finally {
        setLoading(false);
      }
    }
  }

  /**
   * User drawer form successful callback
   */
  function onUserDrawerSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'edit', onClick: editUserInfo.bind(null, record) }];
  }

  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Department role', onClick: showDepartRole.bind(null, record) },
      { label: 'User details', onClick: showUserDetail.bind(null, record) },
      {
        label: 'Disassociate',
        color: 'error',
        popConfirm: {
          title: '确认Disassociate吗？',
          confirm: unlinkDepartUser.bind(null, [record.id], false),
        },
      },
    ];
  }
</script>
