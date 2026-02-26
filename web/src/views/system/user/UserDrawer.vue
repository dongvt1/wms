<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="getTitle"
    :width="adaptiveWidth"
    @ok="handleSubmit"
    :showFooter="showFooter"
    destroyOnClose
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { defineComponent, ref, computed, unref, useAttrs } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './user.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { saveOrUpdateUser, getUserRoles, getUserDepartList, getAllRolesListNoByTenant, getAllRolesList } from './user.api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';
  import { getTenantId } from "/@/utils/auth";

  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const rowId = ref('');
  const departOptions = ref([]);
  let isFormDepartUser = false;
  //Form configuration
  const [registerForm, { setProps, resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 90,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  // TODO [VUEN-527] https://www.teambition.com/task/6239beb894b358003fe93626
  const showFooter = ref(true);
  //form assignment
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    showFooter.value = data?.showFooter ?? true;
    setDrawerProps({ confirmLoading: false, showFooter: showFooter.value });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      //Tenant information is defined as an array
   /*   if (data.record.relTenantIds && !Array.isArray(data.record.relTenantIds)) {
        data.record.relTenantIds = data.record.relTenantIds.split(',');
      } else {
        //【issues/I56C5I】In user management, I clicked twice to edit the tenant configuration and it was lost.
        //data.record.relTenantIds = [];
      }*/

      //Check role/Assignment/try catch deal with，Otherwise there will be problems with editing
      try {
        const userRoles = await getUserRoles({ userid: data.record.id });
        if (userRoles && userRoles.length > 0) {
          data.record.selectedroles = userRoles;
        }
      } catch (error) {}

      //Check the department/Assignment
      const userDepart = await getUserDepartList({ userId: data.record.id });
      if (userDepart && userDepart.length > 0) {
        data.record.selecteddeparts = userDepart;
        let selectDepartKeys = Array.from(userDepart, ({ key }) => key);
        data.record.selecteddeparts = selectDepartKeys.join(',');
        departOptions.value = userDepart.map((item) => {
          return { label: item.title, value: item.key };
        });
      }
      //Responsible department/Assignment
      data.record.departIds && !Array.isArray(data.record.departIds) && (data.record.departIds = data.record.departIds.split(','));
      //update-begin---author:zyf   Date:20211210  for：Avoid null value display exceptions------------
      //update-begin---author:liusq   Date:20231008  for：[issues/772]Avoid null value display exceptions------------
      data.record.departIds =  (!data.record.departIds || data.record.departIds == '') ? [] : data.record.departIds;
      //update-end-----author:liusq   Date:20231008  for：[issues/772]Avoid null value display exceptions------------
      //update-begin---author:zyf   Date:20211210  for：Avoid null value display exceptions------------
    }
    //deal with角色用户列表情况(Related to role list)
    data.selectedroles && (await setFieldsValue({ selectedroles: data.selectedroles }));
    // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1737】Department user editing interface，Add parametersupdateFromPage:"deptUsers"
    isFormDepartUser = data?.departDisabled === true ? true : false;
    // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1737】Department user editing interface，Add parametersupdateFromPage:"deptUsers"
    //Hide password when editing/Role list hides role information/My department is used to hide the department it belongs to.
    updateSchema([
      {
        field: 'password',
        // 【QQYUN-8324】
        ifShow: !unref(isUpdate),
      },
      {
        field: 'confirmPassword',
        ifShow: !unref(isUpdate),
      },
      {
        field: 'selectedroles',
        show: !data.isRole,
      },
      {
        field: 'departIds',
        componentProps: { options: departOptions },
      },
      {
        field: 'selecteddeparts',
        show: !data?.departDisabled,
      },
      {
        field: 'selectedroles',
        show: !data?.departDisabled,
        //update-begin---author:wangshuai ---date:20230424  for：【issues/4844】In multi-tenant mode，Add or edit users，Select the role column，Role options do not do tenant isolation------------
        //Determine whether it is multi-tenant mode
        componentProps:{
          api: data.tenantSaas?getAllRolesList:getAllRolesListNoByTenant
        }
        //update-end---author:wangshuai ---date:20230424  for：【issues/4844】In multi-tenant mode，Add or edit users，Select the role column，Role options do not do tenant isolation------------
      },
      //update-begin---author:wangshuai ---date:20230522  for：【issues/4935】The tenant drop-down box in the tenant user editing interface is not filtered，Display all tenants in the current system------------
      {
        field: 'relTenantIds',
        componentProps:{
          disabled: !!data.tenantSaas,
        },
      },
      //update-end---author:wangshuai ---date:20230522  for：【issues/4935】The tenant drop-down box in the tenant user editing interface is not filtered，Display all tenants in the current system------------
    ]);
    //update-begin---author:wangshuai ---date:20230522  for：【issues/4935】The tenant drop-down box in the tenant user editing interface is not filtered，Display all tenants in the current system------------
    if(!unref(isUpdate) && data.tenantSaas){
      await setFieldsValue({ relTenantIds: getTenantId().toString() })
    }
    //update-end---author:wangshuai ---date:20230522  for：【issues/4935】The tenant drop-down box in the tenant user editing interface is not filtered，Display all tenants in the current system------------
    // Whether adding or editing，You can set form values
    if (typeof data.record === 'object') {
      setFieldsValue({
        ...data.record,
      });
    }
    // Disable entire form when hiding bottom
    //update-begin-author:taoyan date:2022-5-24 for: VUEN-1117【issue】0523Questions about Zhou Kaiyuan
    setProps({ disabled: !showFooter.value });
    //update-end-author:taoyan date:2022-5-24 for: VUEN-1117【issue】0523Questions about Zhou Kaiyuan
    if(unref(isUpdate)){
      updateSchema([
        //Modify the parameters of main and part-time positions
        {
          field: 'mainDepPostId',
          componentProps: { params: { departIds: data.record.selecteddeparts, parentId: data.record.selecteddeparts } },
        },
        {
          field: 'otherDepPostId',
          componentProps: { params: { departIds: data.record.selecteddeparts, parentId: data.record.selecteddeparts } },
        }
      ]);
    }
    //Department management，Add new user，When adding personnel under a position, the current position is the main position by default.
    updateSchema([
      {
        field: 'mainDepPostId',
        defaultValue: data?.mainDepPostId || '',
      }
    ])
  });
  //Get title
  const getTitle = computed(() => {
    // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8389】System user details drawertitleChange
    if (!unref(isUpdate)) {
      return 'Add new user';
    } else {
      return unref(showFooter) ? 'Edit user' : 'User details';
    }
    // update-end--author:liaozhiyang---date:20240306---for：【QQYUN-8389】System user details drawertitleChange
  });
  const { adaptiveWidth } = useDrawerAdaptiveWidth();

  //Submit event
  async function handleSubmit() {
    try {
      let values = await validate();
      setDrawerProps({ confirmLoading: true });
      values.userIdentity === 1 && (values.departIds = '');
      let isUpdateVal = unref(isUpdate);
      // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1737】Department user editing interface，Add parametersupdateFromPage:"deptUsers"
      let params = values;
      if (isFormDepartUser) {
        params = { ...params, updateFromPage: 'deptUsers' };
      }
      // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1737】Department user editing interface，Add parametersupdateFromPage:"deptUsers"
      //Submit form
      await saveOrUpdateUser(params, isUpdateVal);
      //Close pop-up window
      closeDrawer();
      //Refresh list
      emit('success',{isUpdateVal ,values});
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
