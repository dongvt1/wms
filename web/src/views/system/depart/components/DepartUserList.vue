<template>
  <!--Reference table-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--slot:tabletitle-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="createUser" :disabled="!orgCode">Create new user</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="selectAddUser" :disabled="!orgCode || props.data?.orgCategory === '3'"
        >Add existing user</a-button
      >
    </template>
    <!-- slot：Inline action button -->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <UserDrawer @register="registerDrawer" @success="onUserDrawerSuccess" />
  <UserSelectModal ref="userSelectModalRef" rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectUserOk" />
</template>

<script lang="ts" setup>
  import { computed, inject, ref, watch } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import UserDrawer from '/@/views/system/user/UserDrawer.vue';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import { queryByOrgCodeForAddressList } from '../depart.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { userColumns } from '@/views/system/depart/depart.data';
  import { linkDepartUserBatch } from '@/views/system/departUser/depart.user.api';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  const userSelectModalRef: any = ref(null);
  // Currently selected departmentcode，may be empty，Represents unselected departments
  const orgCode = computed(() => props.data?.orgCode);
  // current departmentid
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
      api: queryByOrgCodeForAddressList,
      columns: userColumns,
      canResize: false,
      rowKey: 'id',
      formConfig: {
        // schemas: userInfoSearchFormSchema,
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
        // Action button configuration
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
      },
      tableSetting: { cacheKey: 'depart_user_userInfo' },
      // Process parameters before requesting
      beforeFetch(params) {
        return Object.assign(params, { orgCode: orgCode.value });
      },
      immediate: !!orgCode.value,
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

  // Create user
  async function createUser() {
    if (!departId.value) {
      createMessage.warning('Please select a department first');
    } else {
      let mainDepPostId = '';
      let selecteddeparts = departId.value;
      if (props.data?.orgCategory === '3') {
        mainDepPostId = departId.value;
        selecteddeparts = props.data.parentId;
      }
      openDrawer(true, {
        isUpdate: false,
        // Initialization responsible department
        nextDepartOptions: { value: props.data?.key, label: props.data?.title },
        //Initialization position
        record: {
          mainDepPostId: mainDepPostId,
          activitiSync: 1,
          userIdentity: 1,
          selecteddeparts: selecteddeparts,
        },
      });
    }
  }

  // View user details
  function showUserDetail(record) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  // Edit user information
  function editUserInfo(record) {
    openDrawer(true, { isUpdate: true, record, departDisabled: true, departPostDisabled: true });
  }

  // 选择Add existing user
  function selectAddUser() {
    userSelectModalRef.value.rowSelection.selectedRowKeys = [];
    selUserModal.openModal();
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
    return [
      { label: 'edit', onClick: editUserInfo.bind(null, record) },
      { label: 'Details', onClick: showUserDetail.bind(null, record) },
    ];
  }
</script>
