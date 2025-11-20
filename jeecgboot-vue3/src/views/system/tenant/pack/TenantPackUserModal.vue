<template>
  <BasicModal @register="registerModal" destroyOnClose :title="title" :width="1000" :footer="null">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #departNames="{ text, record }">
        <template v-if="text && text.length > 0">
          {{ getName(text) }}
        </template>
      </template>
      <template #positionNames="{ text, record }">
        <template v-if="text && text.length > 0">
          {{ getName(text) }}
        </template>
      </template>
      <template #tableTitle>
        <a-button preIcon="ant-design:usergroup-add-outlined" type="primary" @click="addUser">Invite members</a-button>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <tenant-user-select-modal :multi="true" @register="registerUserModal" @on-select="onSelected" :tenantId="getTenantId"></tenant-user-select-modal>
  </BasicModal>
</template>

<script lang="ts">
  import { computed, defineComponent, reactive, ref } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { tenantPackUserColumns } from '../tenant.data';
  import { queryTenantPackUserList, deleteTenantPackUser, addTenantPackUser } from '../tenant.api';
  import TenantUserSelectModal from '../components/TenantUserSelectModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: 'TenantPackUserModal',
    components: { BasicModal, BasicTable, TableAction, TenantUserSelectModal },
    setup() {
      //Get tenantsid
      const getTenantId = computed(()=>{
        return tenantPackData.tenantId;
      })
      
      //Package information
      const tenantPackData = reactive<any>({});
      //form assignment
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, showCancelBtn: true, showOkBtn: false });
        Object.assign(tenantPackData, data.record);
        await reload();
      });
      const { createMessage } = useMessage();
      //Set title
      const title = ref<string>('user');
      //registertabledata
      const { tableContext } = useListPage({
        tableProps: {
          api: queryTenantPackUserList,
          immediate: false,
          columns: tenantPackUserColumns,
          canResize: false,
          useSearchForm: false,
          beforeFetch: (params) => {
            params.tenantId = tenantPackData.tenantId;
            params.packId = tenantPackData.id;
            params.status = 1;
            return params;
          },
          actionColumn: {
            width: 120,
            fixed: 'right',
          },
        },
      });
      const [registerUserModal, { openModal: openUserModal, closeModal: closeUserModal }] = useModal();
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

      /**
       * Get department/Job title
       * @param value
       */
      function getName(value) {
        return value.join(',');
      }

      /**
       * Table operation column
       * @param record
       */
      function getTableAction(record) {
        return [
          {
            label: 'Remove',
            popConfirm: {
              title: '是否确认Remove',
              confirm: handleDelete.bind(null, record),
            }
          },
        ];
      }

      /**
       * delete
       */
      async function handleDelete(record) {
        let params = {
          packId: record.packId,
          packName: record.packName,
          tenantId: tenantPackData.tenantId,
          userId: record.id,
          realname: record.realname,
        };
        await deleteTenantPackUser(params);
        await reload();
      }

      /**
       * 添加user弹窗
       */
      function addUser() {
        openUserModal(true, {
          list: [],
        });
      }

      /**
       * Inviter callback event
       * @param arr
       */
      async function onSelected(arr) {
        if (arr && arr.length > 0) {
          let names: any[] = [];
          let ids: any[] = [];
          for (let u of arr) {
            names.push(u.realname);
            ids.push(u.id);
          }
          console.log(tenantPackData);
          let params = {
            packId: tenantPackData.id,
            packName: tenantPackData.packName,
            tenantId: tenantPackData.tenantId,
            userId: ids.join(','),
            realname: names.join(','),
          };
          await addTenantPackUser(params);
          await reload();
        }
        closeUserModal();
      }

      return {
        title,
        registerModal,
        registerTable,
        rowSelection,
        getName,
        getTableAction,
        registerUserModal,
        addUser,
        onSelected,
        getTenantId,
      };
    },
  });
</script>

<style lang="less" scoped></style>
