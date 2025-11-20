<template>
  <div>
    <!--Reference table-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection"  @expand="handleExpand">
      <!-- Embeddedtablearea begin -->
<!--           <template #expandedRowRender="{record}">-->
<!--             <a-tabs tabPosition="top">-->
<!--               <a-tab-pane tab="Request header table" key="openApiHeader" forceRender>-->
<!--                  <openApiHeaderSubTable v-if="expandedRowKeys.includes(record.id)" :id="record.id" />-->
<!--               </a-tab-pane>-->
<!--               <a-tab-pane tab="Request parameters part" key="openApiParam" forceRender>-->
<!--                  <openApiParamSubTable v-if="expandedRowKeys.includes(record.id)" :id="record.id" />-->
<!--               </a-tab-pane>-->
<!--             </a-tabs>-->
<!--           </template>-->
     <!-- Embeddedtablearea end -->
     <!--slot:tabletitle-->
      <template #tableTitle>
          <a-button type="primary" v-auth="'openapi:open_api:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> New</a-button>
          <a-button  type="primary" v-auth="'openapi:open_api:exportXls'"  preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
          <j-upload-button  type="primary" v-auth="'openapi:open_api:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    delete
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button  v-auth="'openapi:open_api:deleteBatch'">Batch operations
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
        <!-- Advanced query -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
       <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--字段回显slot-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单area -->
    <OpenApiModal @register="registerModal" @success="handleSuccess"></OpenApiModal>
  </div>
</template>

<script lang="ts" name="openapi-openApi" setup>
  import {ref, reactive, computed, unref} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {useModal} from '/@/components/Modal';
  import OpenApiModal from './components/OpenApiModal.vue'
  import OpenApiHeaderSubTable from './subTables/OpenApiHeaderSubTable.vue'
  import OpenApiParamSubTable from './subTables/OpenApiParamSubTable.vue'
  import {columns, searchFormSchema, superQuerySchema} from './OpenApi.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './OpenApi.api';
  import {downloadFile} from '/@/utils/common/renderUtils';
  import { useUserStore } from '/@/store/modules/user';
  const queryParam = reactive<any>({});
   // Expandkey
  const expandedRowKeys = ref<any[]>([]);
  //registermodel
  const [registerModal, {openModal}] = useModal();
  const userStore = useUserStore();
   //registertabledata
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'Interface management',
           api: list,
           columns,
           canResize:false,
           formConfig: {
                //labelWidth: 120,
                schemas: searchFormSchema,
                autoSubmitOnEnter:true,
                showAdvancedButton:true,
                fieldMapToNumber: [
                ],
                fieldMapToTime: [
                ],
            },
           actionColumn: {
               width: 120,
               fixed:'right'
           },
           beforeFetch: (params) => {
             return Object.assign(params, queryParam);
           },
        },
        exportConfig: {
            name:"Interface management",
            url: getExportUrl,
            params: queryParam,
        },
        importConfig: {
            url: getImportUrl,
            success: handleSuccess
        },
    })

  const [registerTable, {reload},{ rowSelection, selectedRowKeys }] = tableContext

   // Advanced query配置
   const superQueryConfig = reactive(superQuerySchema);

   /**
   * Advanced query事件
   */
   function handleSuperQuery(params) {
     Object.keys(params).map((k) => {
       queryParam[k] = params[k];
     });
     reload();
   }

   /**
     * Expand事件
     * */
   function handleExpand(expanded, record){
        expandedRowKeys.value=[];
        if (expanded === true) {
           expandedRowKeys.value.push(record.id)
        }
    }
   /**
    * New事件
    */
  function handleAdd() {
     openModal(true, {
       isUpdate: false,
       showFooter: true,
     });
  }
   /**
    * Edit event
    */
  function handleEdit(record: Recordable) {
     openModal(true, {
       record,
       isUpdate: true,
       showFooter: true,
     });
   }
   /**
    * Details
   */
  function handleDetail(record: Recordable) {
     openModal(true, {
       record,
       isUpdate: true,
       showFooter: false,
     });
   }
   /**
    * delete事件
    */
  async function handleDelete(record) {
     await deleteOne({id: record.id}, handleSuccess);
   }
   /**
    * 批量delete事件
    */
  async function batchHandleDelete() {
     await batchDelete({ids: selectedRowKeys.value},handleSuccess);
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
  function getTableAction(record){
       return [
         {
           label: 'edit',
           onClick: handleEdit.bind(null, record),
           auth: 'openapi:open_api:edit'
         }
       ]
   }


  /**
   * 下拉Action bar
   */
  function getDropDownAction(record){
    return [
      {
        label: 'Details',
        onClick: handleDetail.bind(null, record),
      }, {
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft'
        },
        auth: 'openapi:open_api:delete'
      }
    ]
  }


</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
</style>
