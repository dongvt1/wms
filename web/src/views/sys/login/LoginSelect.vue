<template>
  <BasicModal v-bind="config" @register="registerModal" :title="currTitle" wrapClassName="loginSelectModal" v-model:visible="visible">
    <a-form ref="formRef" :model="formState" :rules="rules" v-bind="layout" :colon="false" class="loginSelectForm">
      <!--Multi-tenant selection-->
      <a-form-item v-if="isMultiTenant" name="tenantId" :validate-status="validate_status">
        <!--Label content-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>You belong to multiple tenants, please select login tenant</span>
            </template>
            <a-avatar style="background-color: #87d068" :size="30"> Tenant </a-avatar>
          </a-tooltip>
        </template>
        <template #extra v-if="validate_status == 'error'">
          <span style="color: #ed6f6f">Please select login tenant</span>
        </template>
        <!--Tenant dropdown content-->
        <a-select
          v-model:value="formState.tenantId"
          @change="handleTenantChange"
          placeholder="Please select login tenant"
          :class="{ 'valid-error': validate_status == 'error' }"
        >
          <template v-for="tenant in tenantList" :key="tenant.id">
            <a-select-option :value="tenant.id">{{ tenant.name }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
      <!--Multi-department selection-->
      <a-form-item v-if="isMultiDepart" :validate-status="validate_status1" :colon="false">
        <!--Label content-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>You belong to multiple departments, please select login department</span>
            </template>
            <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> Department </a-avatar>
          </a-tooltip>
        </template>
        <template #extra v-if="validate_status1 == 'error'">
          <span style="color: #ed6f6f">Please select login department</span>
        </template>
        <!--Department dropdown content-->
        <a-select
          v-model:value="formState.orgCode"
          @change="handleDepartChange"
          placeholder="Please select login department"
          :class="{ 'valid-error': validate_status1 == 'error' }"
        >
          <template v-for="depart in departList" :key="depart.orgCode">
            <a-select-option :value="depart.orgCode">{{ depart.departName }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="handleSubmit" type="primary">Confirm</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, watch, unref, reactive, UnwrapRef } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { defHttp } from '/@/utils/http/axios';
  interface FormState {
    orgCode: string | undefined;
    tenantId: number;
  }
  export default defineComponent({
    name: 'loginSelect',
    components: {
      Avatar,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const userStore = useUserStore();
      const { notification } = useMessage();
      //Tenant configuration
      const isMultiTenant = ref(false);
      const tenantList = ref([]);
      const validate_status = ref('');
      //Department configuration
      const isMultiDepart = ref(false);
      const departList = ref([]);
      const validate_status1 = ref('');
      //Modal visibility
      const visible = ref(false);
      //Login user
      const username = ref('');
      //Form
      const formRef = ref();
      //Selected tenant and department information
      const formState: UnwrapRef<FormState> = reactive({
        orgCode: undefined,
        tenantId: null,
      });

      const config = {
        maskClosable: false,
        closable: false,
        canFullscreen: false,
        width: '500px',
        minHeight: 20,
        maxHeight: 20,
      };
      //Modal operation
      const [registerModal, { closeModal }] = useModalInner();

      //Current title
      const currTitle = computed(() => {
        if (unref(isMultiDepart) && unref(isMultiTenant)) {
          return 'Please select tenant and department';
        } else if (unref(isMultiDepart) && !unref(isMultiTenant)) {
          return 'Please select department';
        } else if (!unref(isMultiDepart) && unref(isMultiTenant)) {
          return 'Please select tenant';
        }
      });

      const rules = ref({
        tenantId: [{ required: unref(isMultiTenant), type: 'number', message: 'Please select tenant', trigger: 'change' }],
        orgCode: [{ required: unref(isMultiDepart), message: 'Please select department', trigger: 'change' }],
      });

      const layout = {
        labelCol: { span: 4 },
        wrapperCol: { span: 18 },
      };
      /**
       * Handle department situation
       */
      function bizDepart(loginResult) {
        //If login interface returns user's last login tenant ID, no need to reselect
        if(loginResult.userInfo?.orgCode && loginResult.userInfo?.orgCode!==''){
          isMultiDepart.value = false;
          return;
        }
        
        let multi_depart = loginResult.multi_depart;
        //0: no department 1: one department 2: multiple departments
        if (multi_depart == 0) {
          // notification.warn({
          //   message: 'Notice',
          //   description: `You have not been assigned to a department, please confirm account information`,
          //   duration: 3,
          // });
          isMultiDepart.value = false;
        } else if (multi_depart == 2) {
          isMultiDepart.value = true;
          departList.value = loginResult.departs;
        } else {
          isMultiDepart.value = false;
        }
      }

      /**
       * Handle tenant situation
       */
      function bizTenantList(loginResult) {
        //If login interface returns user's last login tenant ID, no need to reselect
        if(loginResult.userInfo?.loginTenantId && loginResult.userInfo?.loginTenantId!==0){
          isMultiTenant.value = false;
          return;
        }
        
        let tenantArr = loginResult.tenantList;
        if (Array.isArray(tenantArr)) {
          if (tenantArr.length === 0) {
            isMultiTenant.value = false;
            userStore.setTenant(formState.tenantId);
          } else if (tenantArr.length === 1) {
            formState.tenantId = tenantArr[0].id;
            isMultiTenant.value = false;
            userStore.setTenant(formState.tenantId);
          } else {
            isMultiTenant.value = true;
            tenantList.value = tenantArr;
          }
        }
      }

      /**
       * Confirm selected tenant and department information
       */
      function handleSubmit() {
        if (unref(isMultiTenant) && !formState.tenantId) {
          validate_status.value = 'error';
          return false;
        }
        if (unref(isMultiDepart) && !formState.orgCode) {
          validate_status1.value = 'error';
          return false;
        }
        formRef.value
          .validate()
          .then(() => {
            departResolve()
              .then(() => {
                userStore.setTenant(formState.tenantId);
                emit('success');
              })
              .catch((e) => {
                console.log('Login selection issue', e);
              })
              .finally(() => {
                close();
              });
          })
          .catch((err) => {
            console.log('Form validation failed error', err);
          });
      }
      /**
       * Switch department selection
       */
      function departResolve() {
        return new Promise((resolve, reject) => {
          if (!unref(isMultiDepart) && !unref(isMultiTenant)) {
            resolve();
          } else {
            let params = { orgCode: formState.orgCode,loginTenantId: formState.tenantId, username: unref(username) };
            defHttp.put({ url: '/sys/selectDepart', params }).then((res) => {
              if (res.userInfo) {
                userStore.setUserInfo(res.userInfo);
                resolve();
              } else {
                requestFailed(res);
                userStore.logout();
                reject();
              }
            });
          }
        });
      }

      /**
       * Request failure handling
       */
      function requestFailed(err) {
        notification.error({
          message: 'Login failed',
          description: ((err.response || {}).data || {}).message || err.message || 'Request error, please try again later',
          duration: 4,
        });
      }

      /**
       * Close modal
       */
      function close() {
        closeModal();
        reset();
      }

      /**
       * Modal opening preprocessing
       */
      async function show(loginResult) {
        if (loginResult) {
          username.value = userStore.username;
          await reset();
          await bizDepart(loginResult);
          await bizTenantList(loginResult);
          if (!unref(isMultiDepart) && !unref(isMultiTenant)) {
            emit('success', userStore.getUserInfo);
          } else {
            visible.value = true;
          }
        }
        //After login modal is completed, set login identifier to false
        loginResult.isLogin = false;
        userStore.setLoginInfo(loginResult);
      }

      /**
       * Reset data
       */
      function reset() {
        tenantList.value = [];
        validate_status.value = '';

        departList.value = [];
        validate_status1.value = '';
      }
      function handleTenantChange(e) {
        validate_status.value = '';
      }

      function handleDepartChange(e) {
        validate_status1.value = '';
      }

      return {
        registerModal,
        visible,
        tenantList,
        isMultiTenant,
        validate_status,
        isMultiDepart,
        departList,
        validate_status1,
        formState,
        rules,
        layout,
        formRef,
        currTitle,
        config,
        handleTenantChange,
        handleDepartChange,
        show,
        handleSubmit,
      };
    },
  });
</script>

<style lang="less" scoped>
  .loginSelectForm {
    margin-bottom: -20px;
  }

  .loginSelectModal {
    top: 10px;
  }
</style>
