<template>
  <BasicTable :ellipsis="true" @register="registerTable" :searchInfo="searchInfo" :columns="logColumns" :expand-column-width="16">
    <template #tableTitle>
      <div class="table-title-bar">
        <a-tabs defaultActiveKey="4" @change="tabChange" size="small">
          <a-tab-pane tab="Exception log" key="4"></a-tab-pane>
          <a-tab-pane tab="Login log" key="1"></a-tab-pane>
          <a-tab-pane tab="Operation log" key="2"></a-tab-pane>
        </a-tabs>
        <span class="export-btn" v-if="searchInfo.logType == 2">
          <a-tooltip>
            <template #title>Export</template>
            <a-button  type="text" preIcon="ant-design:download-outlined" shape="circle" @click="onExportXls" />
          </a-tooltip>
        </span>
      </div>
    </template>
    <template #expandedRowRender="{ record }">
      <div v-if="searchInfo.logType == 2">
        <div style="margin-bottom: 5px">
          <a-badge status="success" style="vertical-align: middle" />
          <span style="vertical-align: middle">Request method:{{ record.method }}</span></div
        >
        <div>
          <a-badge status="processing" style="vertical-align: middle" />
          <span style="vertical-align: middle">Request parameters:{{ record.requestParam }}</span></div
        >
      </div>
      <div v-if="searchInfo.logType == 4">
        <div style="margin-bottom: 5px">
          <a-badge status="success" style="vertical-align: middle" />
          <span class="error-box" style="vertical-align: middle">exception stack:{{ record.requestParam }}</span>
        </div>
      </div>
    </template>
  </BasicTable>
</template>
<script lang="ts" name="monitor-log" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getLogList, getExportUrl } from './log.api';
  import {
    columns,
    searchFormSchema,
    operationLogColumn,
    operationSearchFormSchema,
    exceptionColumns
  } from './log.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  const { createMessage } = useMessage();
  const checkedKeys = ref<Array<string | number>>([]);

  const logColumns = ref<any>(exceptionColumns);
  const searchSchema = ref<any>(searchFormSchema);
  const searchInfo = { logType: '4' };
  // List page public parameters、method
  const { prefixCls, tableContext, onExportXls } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: 'Log list',
      api: getLogList,
      expandRowByClick: true,
      showActionColumn: false,
      rowSelection: {
        columnWidth: 20,
      },
      formConfig: {
        schemas: searchSchema,
        fieldMapToTime: [['fieldTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
      },
    },
    exportConfig: {
      name:"Operation log",
      url: getExportUrl,
      params: searchInfo,
    },
  });

  const [registerTable, { reload }] = tableContext;

  // Log type
  function tabChange(key) {
    searchInfo.logType = key;
    //update-begin---author:wangshuai ---date:20220506  for：[VUEN-943]vue3Log management list translation is incorrect------------
    if (key == '2') {
      logColumns.value = operationLogColumn;
      searchSchema.value = operationSearchFormSchema;
    }else if(key == '4'){
      searchSchema.value = searchFormSchema;
      logColumns.value = exceptionColumns;
    } else {
      searchSchema.value = searchFormSchema;
      logColumns.value = columns;
    }
    //update-end---author:wangshuai ---date:20220506  for：[VUEN-943]vue3Log management list translation is incorrect--------------
    reload();
  }

  /**
   * Select event
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
</script>
<style lang="less" scoped>
  .error-box {
    white-space: break-spaces;
  }
  .table-title-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }
  .export-btn {
    margin-left: auto;
  }
  :deep(.jeecg-basic-table-header__toolbar){
    width:100px !important;
  }
</style>
