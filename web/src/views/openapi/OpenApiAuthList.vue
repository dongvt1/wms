<template>
  <div class="p-2">
    <!--Query area-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :lg="6">
            <a-form-item name="name">
              <template #label><span title="Authorization name">Authorization name</span></template>
              <a-input placeholder="请输入Authorization name" v-model:value="queryParam.name" allow-clear ></a-input>
            </a-form-item>
          </a-col>
          <a-col :lg="6">
            <a-form-item name="createBy">
              <template #label><span title="Associated system user name">Associated system user name</span></template>
              <JSearchSelect dict="sys_user,username,username" v-model:value="queryParam.createBy" placeholder="请输入Associated system user name"  allow-clear ></JSearchSelect>
<!--              <a-input placeholder="请输入Associated system user name" v-model:value="queryParam.systemUserId" allow-clear ></a-input>-->
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Query</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">reset</a-button>
                <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                  {{ toggleSearchStatus ? 'close' : 'Expand' }}
                  <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                </a>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--Reference table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'openapi:open_api_auth:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> New</a-button>
        <a-button  type="primary" v-auth="'openapi:open_api_auth:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button  type="primary" v-auth="'openapi:open_api_auth:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'openapi:open_api_auth:deleteBatch'">Batch operations
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
        <!-- 高级Query -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>

    <!-- form area -->
    <OpenApiAuthModal ref="registerModal" @success="handleSuccess"></OpenApiAuthModal>
    <AuthModal ref="authModal" @success="handleSuccess"></AuthModal>
  </div>
</template>

<script lang="ts" name="openapi-openApiAuth" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './OpenApiAuth.data';
  import {
    list,
    deleteOne,
    batchDelete,
    getImportUrl,
    getExportUrl,
    getGenAKSK, saveOrUpdate
  } from "./OpenApiAuth.api";
  import OpenApiAuthModal from './components/OpenApiAuthModal.vue'
  import AuthModal from './components/AuthModal.vue'
  import { useUserStore } from '/@/store/modules/user';
  import JSearchSelect from "../../components/Form/src/jeecg/components/JSearchSelect.vue";

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const authModal = ref();
  const userStore = useUserStore();
  //registertabledata
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'Authorization management',
      api: list,
      columns,
      canResize:false,
      useSearchForm: false,
      actionColumn: {
        width: 200,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "Authorization management",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload, updateTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:24,
    sm:10,
    xl:6,
    xxl:10
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  // 高级Query配置
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * 高级Query事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    searchQuery();
  }

  /**
   * New事件
   */
  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }
  
  /**
   * Edit event
   */
  function handleAuth(record: Recordable) {
    authModal.value.disableSubmit = false;
    authModal.value.edit(record);
  }

  /**
   * Edit event
   */
  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.authDrawerOpen = true;
    registerModal.value.edit(record);
  }

  /**
   * reset事件
   * @param record
   */
  async function handleReset(record: Recordable) {
    const AKSKObj = await getGenAKSK({});
    record.ak = AKSKObj[0];
    record.sk = AKSKObj[1];
    saveOrUpdate(record,true);
    // handleSuccess;

  }
   
  /**
   * Details
   */
  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
  }
   
  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }
   
  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }
   
  /**
   * successful callback
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
   
  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'Authorize',
        onClick: handleAuth.bind(null, record),
        auth: 'openapi:open_api_auth:edit'
      },
      {
        label: 'reset',
        popConfirm: {
          title: '是否resetAK,SK',
          confirm: handleReset.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_auth:edit'
      },
    ];
  }
   
  /**
   * 下拉Action bar
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Details',
        onClick: handleDetail.bind(null, record),
      }, {
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'openapi:open_api_auth:delete'
      }
    ]
  }

  /**
   * Query
   */
  function searchQuery() {
    reload();
  }
  
  /**
   * reset
   */
  function searchReset() {
    formRef.value.resetFields();
    selectedRowKeys.value = [];
    //刷新data
    reload();
  }
  




</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .query-group-cust{
      min-width: 100px !important;
    }
    .query-group-split-cust{
      width: 30px;
      display: inline-block;
      text-align: center
    }
    .ant-form-item:not(.ant-form-item-with-help){
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }
</style>
