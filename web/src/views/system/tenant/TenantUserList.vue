<template>
  <div>
    <!--Reference table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">New</a-button>
        <a-button
            preIcon="ant-design:user-add-outlined"
            type="primary"
            @click="handleInvitation"
            style="margin-right: 5px">
            Invite users to join
        </a-button>
        <JThirdAppButton biz-type="user" :selected-row-keys="selectedRowKeys" syncToApp syncToLocal @sync-finally="onSyncFinally" />
        <a-button type="primary" @click="openQuitModal(true, {})" preIcon="ant-design:user-delete-outlined">Resigned personnel</a-button>
        <div style="margin-left: 10px;margin-top: 5px"> Currently logged in tenant: <span class="tenant-name">{{loginTenantName}}</span> </div>
        <a-tooltip title="More instructions for tenant users">
          <a-icon type="question-circle" style="margin-left: 8px; cursor: pointer " @click="tipShow = true"/>
        </a-tooltip>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--user drawer-->
    <TenantUserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!-- Resigned personnel列弹窗 -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
    <!--  Change owner popup  -->
    <UserSelectModal @register="registerUserModal" :excludeUserIdList="excludeUserIdList" :maxSelectCount="1" @getSelectResult="selectResult" />
    <!--  Package allocation pop-up window  -->
    <TenantPackAllotModal @register="registerPackAllotModal"></TenantPackAllotModal>
    <!--  Inviter pop-up window  -->
    <TenantInviteUserModal @register="registerSelUserModal" @inviteOk="handleInviteUserOk" />
    <a-modal v-model:open="tipShow" :footer="null" title="More instructions for tenant users" :width="800">
      <ul class="user-tenant-tip">
        <li>Remove：将用户从当前tenant中Remove</li>
        <li>delete：仅可delete当天创建的用户，delete后可在系统用户回收站恢复</li>
        <li>Resign：非tenant创建者可进行Resign操作，Resign员工可在Resigned personnel列表查看</li>
        <li>handover：tenant创建者可进行tenanthandover，handover后员工信息可在Resigned personnel列表查看</li>
      </ul>
      <div style="height: 10px"></div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="tenant-system-user" setup>
  //tsgrammar
  import { onMounted, ref, unref } from 'vue';
  import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
  import UserDrawer from '../user/UserDrawer.vue';
  import JThirdAppButton from '/@/components/jeecg/thirdApp/JThirdAppButton.vue';
  import UserQuitModal from '../user/UserQuitModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from '../user/user.data';
  import { list , deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch, getUserTenantPageList, updateUserTenantStatus } from '../user/user.api';
  // import { usePermission } from '/@/hooks/web/usePermission'
  // const { hasPermission } = usePermission();
  import { userTenantColumns, userTenantFormSchema } from '../user/user.data';
  import { useUserStore } from '/@/store/modules/user';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import { getTenantId } from "/@/utils/auth";
  import { changeOwenUserTenant } from "/@/views/system/usersetting/UserSetting.api";
  import {getLoginTenantName, invitationUserJoin, leaveTenant} from "/@/views/system/tenant/tenant.api";
  import TenantUserDrawer from './components/TenantUserDrawer.vue';
  import { sameDay, tenantSaasMessage } from "@/utils/common/compUtils";
  import TenantPackAllotModal from './components/TenantPackAllotModal.vue'
  import TenantInviteUserModal from "@/views/system/tenant/components/TenantInviteUserModal.vue";

  const { createMessage, createConfirm } = useMessage();

  //registerdrawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  //Resign代理人model
  const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
  //Resign用户列表model
  const [registerQuitModal, { openModal: openQuitModal }] = useModal();
  //Assign package pop-up window
  const [registerPackAllotModal, { openModal: openPackAllotModal }] = useModal();
  const userStore = useUserStore();
  const createBy = userStore.getUserInfo.username;
  //Pop-up prompt displays
  const tipShow = ref<boolean>(false);

  // List page public parameters、method
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: 'Tenant user list',
      api: getUserTenantPageList,
      columns: userTenantColumns,
      size: 'small',
      formConfig: {
        schemas: userTenantFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      beforeFetch: (params) => {
        params['userTenantStatus'] = '1,3,4';
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      },
    },
  });

  //registertabledata
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  /**
   * New事件
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      showFooter: true,
      tenantSaas: true,
    });
  }
  /**
   * Edit event
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
      tenantSaas: true,
    });
  }
  /**
   * Details
   */
  async function handleDetail(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
      tenantSaas: true,
    });
  }

  /**
   * successful callback
   */
  function handleSuccess() {
    reload();
  }

  /**
   *Synchronous DingTalk and WeChat callbacks
   */
  function onSyncFinally({ isToLocal }) {
    // 同步到本地时刷新下data
    if (isToLocal) {
      reload();
    }
  }

  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
        // ifShow: () => hasPermission('system:user:edit'),
      },
    ];
  }
  /**
   * 下拉Action bar
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: '查看Details',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Remove用户',
        onClick: handleLeave.bind(null, record.id),
      },
      {
        label: 'delete用户',
        popConfirm: {
          title: '是否确认delete该用户',
          confirm: handleDeleteUser.bind(null, record),
        },
        ifShow: () => record.username!== userStore.getUserInfo?.username && sameDay(record.createTime),
      },
      {
        label: 'Change owner',
        onClick: handleHandover.bind(null, record),
        ifShow: () =>{
          return record.username === record.createBy;
        }
      },
      {
        label: 'agree',
        onClick: updateStatus.bind(null, record.id, '1'),
        ifShow: () => {
          return (record.status === '3' || record.status === '4') && record.createBy === createBy;
        },
      },
      {
        label: 'reject',
        popConfirm: {
          title: '是否确认reject',
          confirm: updateStatus.bind(null, record.id, '4'),
        },
        ifShow: () => {
          return record.status === '3' && record.createBy === createBy;
        },
      },
      {
        label: 'User package',
        onClick: handleAllotPack.bind(null, record),
      }
    ];
  }

  /**
   * Update user tenant status
   * @param id
   * @param status
   */
  function updateStatus(id, status) {
    updateUserTenantStatus({ userId: id, status: status })
      .then((res) => {
        if (res.success) {
          handleSuccess();
        }
      })
      .catch((e) => {
        createMessage.warning(e.message);
      });
  }

  //============================================ tenantResignhandover  ============================================
  //tenantid
  const tenantId = ref<string>('');
  //Exclude own number set
  const excludeUserIdList = ref<any>([]);
  //Resign代理人model
  const [registerUserModal, { openModal: openUserModal }] = useModal();
  //Invite users to join弹窗
  const [registerSelUserModal, { openModal: userOpenModal }] = useModal();
  const handOverUserName = ref<string>('');
  
  /**
   * 人员handover
   */
  function handleHandover(record) {
    tenantId.value = getTenantId();
    excludeUserIdList.value = [record.id];
    //记录一下当前需要handover的用户名
    handOverUserName.value = record.createBy;
    openUserModal(true)
  }

  /**
   * User selection callback
   * @param options
   * @param values
   */
  function selectResult(options,values) {
    console.log(values)
    if(values && values.length>0){
      let userId = values[0];
      changeOwenUserTenant({ userId:userId, tenantId:unref(tenantId) }).then((res) =>{
        if(res.success){
          createMessage.success("handover成功");
          let username = userStore.getUserInfo?.username;
          if(username == handOverUserName.value){
            userStore.logout(true);
          }else{
            reload();
          }
        } else {
          createMessage.warning(res.message);
        }
      })
    }
  }
  //============================================  tenantResignhandover  ============================================


  //update-begin---author:wangshuai ---date:20230710  for：【QQYUN-5723】4、显示Currently logged in tenant------------
  const loginTenantName = ref<string>('');

  getTenantName();

  async function getTenantName(){
    loginTenantName.value = await getLoginTenantName();
  }
  //update-end---author:wangshuai ---date:20230710  for：【QQYUN-5723】4、显示Currently logged in tenant------------
  

  /**
   * Assign packages
   * 
   * @param record
   */
  function handleAllotPack(record) {
    openPackAllotModal(true,{
      record
    })
  }

  /**
   * delete用户
   */
  function handleDeleteUser(record) {
    deleteUser({ id: record.id }, reload);
  }

  /**
   * Invite users to jointenant
   */
  function handleInvitation() {
    userOpenModal(true, {});
  }
  
  /**
   * User selection callback事件
   * @param username
   * @param phone
   * @param userSelectId
   */
  async function handleInviteUserOk(phone, username) {
    let tId = getTenantId();
    if (phone) {
      await invitationUserJoin({ ids: tId, phone: phone });
      reload();
    }
    if (username) {
      await invitationUserJoin({ ids: tId, username: username });
      reload();
    }
  }

  /**
   * please leave
   * @param id
   */
  async function handleLeave(id) {
    await leaveTenant({ userIds: id, tenantId: getTenantId() }, reload)
  }
  
  
  onMounted(()=>{
    tenantSaasMessage('tenant用户')
  })
</script>

<style scoped>
  .tenant-name{
    text-decoration:underline;
    margin: 5px;
    font-size: 15px;
  }
  .user-tenant-tip{
    margin: 20px;
    background-color: #f8f9fb;
    color: #99a1a9;
    border-radius: 4px;
    padding: 12px;
  }
</style>
