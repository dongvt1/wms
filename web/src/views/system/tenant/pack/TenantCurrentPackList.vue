<!-- Current tenant’s package -->
<template>
  <div style="display: flex; width: 100%">
    <div :style="leftStyle">
      <BasicTable @register="registerTable">
        <template #tableTitle>
          <div style="margin-left: 10px; margin-top: 5px"
            >Currently logged in tenant: <span class="tenant-name">{{ loginTenantName }}</span>
          </div>
        </template>
        <!--Action bar-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </BasicTable>
    </div>
    <div v-if="showRight" style="width: 49%; transition: width 0.3s">
      <TenantUserRightList ref="rightListRef" @cancel="handleCancel"></TenantUserRightList>
    </div>
  </div>
  <TenantPackMenuModal @register="registerPackMenu" @success="success" />
</template>
<script lang="ts">
  import { ref, unref, defineComponent, defineEmits } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { packFormSchema, tenantPackColumns } from '../tenant.data';
  import { getLoginTenantName, packList } from '../tenant.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicTable, TableAction } from '/@/components/Table';
  import TenantPackMenuModal from './TenantPackMenuModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getTenantId } from '/@/utils/auth';
  import TenantUserRightList from '../components/TenantUserRightList.vue';

  export default defineComponent({
    name: 'TenantCurrentPackList',
    components: {
      TenantUserRightList,
      BasicTable,
      TableAction,
      TenantPackMenuModal,
    },
    setup() {
      const [registerPackMenu, { openModal }] = useModal();
      const [registerPackUser, { openModal: packUserOpenModal }] = useModal();

      const tenantId = ref<number>();
      const rightListRef = ref<any>();
      //Whether to display the user list on the right
      const showRight = ref<any>();
      //left style
      const leftStyle = ref<any>({
        width: '100%',
        transition: 'width 0.3s',
      });

      // List page public parameters、method
      const { prefixCls, tableContext } = useListPage({
        designScope: 'tenant-template',
        tableProps: {
          api: packList,
          columns: tenantPackColumns,
          immediate: false,
          formConfig: {
            schemas: packFormSchema,
            labelCol: {
              xxl: 8,
            },
            actionColOptions: {
              xs: 24,
              sm: 8,
              md: 8,
              lg: 8,
              xl: 8,
              xxl: 8,
            },
          },
          beforeFetch: (params) => {
            return Object.assign(params, { tenantId: tenantId.value, status: '1', packType: 'custom' });
          },
        },
      });
      const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
      //Currently logged in tenant name
      const loginTenantName = ref<string>('');
      const { createMessage } = useMessage();

      /**
       * Get tenant name
       */
      getTenantName();

      async function getTenantName() {
        let id = getTenantId();
        if (id) {
          loginTenantName.value = await getLoginTenantName();
          tenantId.value = Number(id);
          await reload();
        }
      }

      /**
       * Table operations
       * @param record
       */
      function getTableAction(record) {
        return [
          {
            label: 'user',
            onClick: seeTenantPackUser.bind(null, record),
          },
          {
            label: 'Permission details',
            onClick: handleDetail.bind(null, record),
          },
        ];
      }

      /**
       * success
       */
      function success() {
        (selectedRowKeys.value = []) && reload();
      }

      /**
       * 套餐包下面的user
       * @param record
       */
      function seeTenantPackUser(record) {
        showRight.value = true;
        leftStyle.value = {
          width: '50%',
          transition: 'width 0.3s',
        };
        setTimeout(() => {
          rightListRef.value.initData(record);
        }, 300);
      }

      /**
       * Details
       * @param record
       */
      function handleDetail(record) {
        openModal(true, {
          isUpdate: true,
          record: record,
          tenantId: unref(tenantId),
          packType: 'custom',
          showFooter: false,
        });
      }

      /**
       * Close callback
       */
      function handleCancel() {
        showRight.value = false;
        leftStyle.value = {
          width: '100%',
          transition: 'width 0.3s',
        };
      }

      return {
        registerPackMenu,
        success,
        getTableAction,
        registerTable,
        rowSelection,
        rightListRef,
        showRight,
        registerPackUser,
        loginTenantName,
        leftStyle,
        handleCancel,
      };
    },
  });
</script>
<style lang="less" scoped>
  .tenant-name {
    text-decoration: underline;
    margin: 5px;
    font-size: 15px;
  }
</style>
