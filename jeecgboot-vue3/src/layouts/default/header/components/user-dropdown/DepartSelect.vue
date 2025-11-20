<template>
  <BasicModal v-bind="config" :maxHeight="500" :title="currTitle" v-model:visible="visible" wrapClassName="loginSelectModal">
    <a-form ref="formRef" v-bind="layout" :colon="false" class="loginSelectForm">
      <a-form-item v-if="isMultiTenant" :validate-status="validate_status">
        <!--labelcontent-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>You are part of a multi-tenant，Please select your current tenant</span>
            </template>
            <a-avatar style="background-color: #87d068" :size="30"> tenant </a-avatar>
          </a-tooltip>
        </template>
        <!--department下拉content-->
        <a-select v-model:value="tenantSelected" placeholder="Please select the login department" :class="{ 'valid-error': validate_status == 'error' }">
          <template #suffixIcon>
            <Icon icon="ant-design:gold-outline" />
          </template>
          <template v-for="tenant in tenantList" :key="tenant.id">
            <a-select-option :value="tenant.id">{{ tenant.name }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
      <a-form-item v-if="isMultiDepart" :validate-status="validate_status1">
        <!--labelcontent-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>You belong to multiple departments，Please select your current department</span>
            </template>
            <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> department </a-avatar>
          </a-tooltip>
        </template>
        <!--department下拉content-->
        <a-select v-model:value="departSelected" placeholder="Please select the login department" :class="{ 'valid-error': validate_status1 == 'error' }">
          <template #suffixIcon>
            <Icon icon="ant-design:gold-outline" />
          </template>
          <template v-for="depart in departList" :key="depart.orgCode">
            <a-select-option :value="depart.orgCode">{{ depart.departName }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="close">closure</a-button>
      <a-button @click="handleSubmit" type="primary">confirm</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, watch, unref } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { BasicModal } from '/@/components/Modal';
  import { getUserDeparts, selectDepart } from '/@/views/system/depart/depart.api';
  import { getUserTenants } from '/@/views/system/tenant/tenant.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';

  const userStore = useUserStore();
  const { createMessage, notification } = useMessage();
  const props = defineProps({
    title: { type: String, default: 'department选择' },
    closable: { type: Boolean, default: false },
    username: { type: String, default: '' },
  });

  const layout = {
    labelCol: { span: 4 },
    wrapperCol: { span: 18 },
  };

  const config = {
    maskClosable: false,
    closable: false,
    canFullscreen: false,
    width: '500px',
    minHeight: 20,
    maxHeight: 20,
  };
  const currTitle = ref('');

  const isMultiTenant = ref(false);
  const currentTenantName = ref('');
  const tenantSelected = ref();
  const tenantList = ref([]);
  const validate_status = ref('');

  const isMultiDepart = ref(false);
  const currentDepartName = ref('');
  const departSelected = ref('');
  const departList = ref([]);
  const validate_status1 = ref('');
  //Show or hide the pop-up window
  const visible = ref(false);
  /**
   * Processing before opening the pop-up window
   */
  async function show() {
    //加载department
    await loadDepartList();
    //加载tenant
    await loadTenantList();
    //Title configuration
    if (unref(isMultiTenant) && unref(isMultiDepart)) {
      currTitle.value = '切换tenant和department';
    } else if (unref(isMultiTenant)) {
      currTitle.value =
        unref(currentTenantName) && unref(currentTenantName).length > 0 ? `tenant切换（当前tenant :${unref(currentTenantName)}）` : props.title;
    } else if (unref(isMultiDepart)) {
      currTitle.value =
        unref(currentDepartName) && unref(currentDepartName).length > 0 ? `department切换（当前department :${unref(currentDepartName)}）` : props.title;
    }
    //modelReveal
    if (unref(isMultiTenant) || unref(isMultiDepart)) {
      visible.value = true;
    }
  }
  /**
   *加载department信息
   */
  async function loadDepartList() {
    const result = await getUserDeparts();
    if (!result.list || result.list.length == 0) {
      return;
    }
    let currentDepart = result.list.filter((item) => item.orgCode == result.orgCode);
    departList.value = result.list;
    departSelected.value = currentDepart && currentDepart.length > 0 ? result.orgCode : '';
    currentDepartName.value = currentDepart && currentDepart.length > 0 ? currentDepart[0].departName : '';
    isMultiDepart.value = true;
  }
  /**
   *加载tenant信息
   */
  async function loadTenantList() {
    const result = await getUserTenants();
    if (!result.list || result.list.length == 0) {
      return;
    }
    let tenantId = userStore.getTenant;
    let currentTenant = result.list.filter((item) => item.id == tenantId);
    currentTenantName.value = currentTenant && currentTenant.length > 0 ? currentTenant[0].name : '';
    tenantList.value = result.list;
    tenantSelected.value = tenantId;
    isMultiTenant.value = true;
  }

  /**
   * Submit data
   */
  async function handleSubmit() {
    if (unref(isMultiTenant) && unref(tenantSelected)==null) {
      validate_status.value = 'error';
      return false;
    }
    if (unref(isMultiDepart) && !unref(departSelected)) {
      validate_status1.value = 'error';
      return false;
    }
    departResolve()
      .then(() => {
        if (unref(isMultiTenant)) {
          userStore.setTenant(unref(tenantSelected));
        }
        createMessage.success('Switch successful');
        
        //切换tenant后要刷新首页
        window.location.reload();
      })
      .catch((e) => {
        console.log('Problem with login selection', e);
      })
      .finally(() => {
        if (unref(isMultiTenant)) {
          userStore.setTenant(unref(tenantSelected));
        }
        close();
      });
  }
  /**
   *切换选择department
   */
  function departResolve() {
    return new Promise(async (resolve, reject) => {
      if (!unref(isMultiDepart)) {
        resolve();
      } else {
        const result = await selectDepart({
          username: userStore.getUserInfo.username,
          orgCode: unref(departSelected),
          loginTenantId: unref(tenantSelected),
        });
        if (result.userInfo) {
          const userInfo = result.userInfo;
          userStore.setUserInfo(userInfo);
          resolve();
        } else {
          requestFailed(result);
          userStore.logout();
          reject();
        }
      }
    });
  }
  /**
   * Request failure handling
   */
  function requestFailed(err) {
    notification.error({
      message: 'Login failed',
      description: ((err.response || {}).data || {}).message || err.message || 'An error occurred with the request，Please try again later',
      duration: 4,
    });
  }
  /**
   * closuremodel
   */
  function close() {
    departClear();
  }

  /**
   *initialization data
   */
  function departClear() {
    currTitle.value = '';

    isMultiTenant.value = false;
    currentTenantName.value = '';
    tenantSelected.value = '';
    tenantList.value = [];
    validate_status.value = '';

    isMultiDepart.value = false;
    currentDepartName.value = '';
    departSelected.value = '';
    departList.value = [];
    validate_status1.value = '';

    visible.value = false;
  }

  /**
   * monitorusername
   */
  watch(
    () => props.username,
    (value) => {
      value && loadDepartList();
    }
  );

  defineExpose({
    show,
  });
</script>
<style lang="less" scoped>
  .loginSelectForm {
    margin-bottom: -20px;
  }

  .loginSelectModal {
    top: 20px;
  }

  .valid-error .ant-select-selection__placeholder {
    color: #f5222d;
  }
</style>
