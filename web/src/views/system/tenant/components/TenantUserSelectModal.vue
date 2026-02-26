<!--User selection box used in packages-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      width="900px"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      destroyOnClose
    >
      <BasicTable ref="tableRef" @register="registerTable" :rowSelection="rowSelection">
        <template #tableTitle></template>
      </BasicTable>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getTenantUserList } from '../tenant.api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { userColumns, userSearchFormSchema } from '../tenant.data';

  export default defineComponent({
    name: 'TenantUserSelectModal',
    components: {
      //Asynchronous loading is required hereBasicTable
      BasicModal,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      //Select box title
      modalTitle: {
        type: String,
        default: 'Select user',
      },
      //query parameters
      tenantId: {
        type: Number,
        default: 0,
      },
      //Exclude usersidcollection of
      excludeUserIdList: {
        type: Array,
        default: [],
      },
    },
    emits: ['register', 'on-select'],
    setup(props, { emit, refs }) {
      const tableScroll = ref<any>({ x: false });
      const tableRef = ref();
      //Registration pop-up box
      const [register, { closeModal }] = useModalInner(() => {
        if (window.innerWidth < 900) {
          tableScroll.value = { x: 900 };
        } else {
          tableScroll.value = { x: false };
        }
        setTimeout(() => {
          if (tableRef.value) {
            tableRef.value.setSelectedRowKeys([]);
          }
        }, 800);
      });

      //Define table columns
      const columns = [
        {
          title: 'account',
          dataIndex: 'username',
          width: 40,
          align: 'left',
        },
        {
          title: 'Name',
          dataIndex: 'realname',
          width: 40,
        },
        {
          title: 'gender',
          dataIndex: 'sex_dictText',
          width: 20,
        },
        {
          title: 'phone number',
          dataIndex: 'phone',
          width: 30,
        },
        {
          title: 'Mail',
          dataIndex: 'email',
          width: 40,
        },
        {
          title: 'state',
          dataIndex: 'status_dictText',
          width: 20,
        },
      ];

      // List page public parameters、method
      const { prefixCls, tableContext } = useListPage({
        designScope: 'tenant-template',
        tableProps: {
          api: getTenantUserList,
          columns: userColumns,
          scroll: { y: 390 },
          rowKey: 'id',
          showActionColumn: false,
          formConfig: {
            schemas: userSearchFormSchema,
            labelWidth: 60,
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
            return Object.assign(params, { userTenantId: props.tenantId });
          },
        },
      });
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

      /**
       * Confirm selection
       */
      function handleOk() {
        //Return selected event
        emit('on-select', rowSelection.selectedRows, rowSelection.selectedRowKeys);
      }

      return {
        handleOk,
        register,
        columns,
        rowSelection,
        tableScroll,
        tableRef,
        registerTable,
      };
    },
  });
</script>

<style scoped>
  :deep(.ant-input-suffix) {
    display: none;
  }
</style>
