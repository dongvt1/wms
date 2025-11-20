<template>
  <a-card title="Example of invisible refresh" :bordered="false">
    <div style="margin-bottom: 8px">
      <span>Enable data change effects：</span>
      <a-switch v-model:checked="reloadEffect" />
    </div>

    <!--
      【Refresh the general idea without trace】：
      1. This function depends on【Instant save】Function，请先看Instant save示例
      2. Must have socket-reload property，and set to true
      3. Must have socket-key property，该property为当前表格的唯一标识，
         The system will automatically update all socket-key same form
      4. Save locally edit-closed in the event，
         Called after successful saving socketSendUpdateRow method，change current row Just pass it on （See Chapter 102 OK）
    -->
    <JVxeTable
      ref="tableRef"
      rowNumber
      rowSelection
      keepSource
      socketReload
      socketKey="demo-socket-reload"
      :reloadEffect="reloadEffect"
      :height="340"
      :loading="loading"
      :columns="columns"
      :dataSource="dataSource"
      @valueChange="onValueChange"
      @edit-closed="handleEditClosed"
    />
  </a-card>
</template>

<script lang="ts" setup>
  // Example of invisible refresh
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeColumn, JVxeTableInstance, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  const tableRef = ref<JVxeTableInstance>();
  // Whether to enable calendar refresh effect
  const reloadEffect = ref(true);
  const loading = ref(false);
  const dataSource = ref<Recordable[]>([]);
  const columns = ref<JVxeColumn[]>([
    { key: 'num', title: 'serial number', width: 80 },
    { key: 'enabled', title: 'enable', width: 80, type: JVxeTypes.checkbox },
    { key: 'ship_name', title: 'ship name', width: 180, type: JVxeTypes.input },
    { key: 'call', title: 'call', width: 80, type: JVxeTypes.input },
    { key: 'len', title: 'long', width: 80, type: JVxeTypes.input },
    { key: 'ton', title: 'ton', width: 120, type: JVxeTypes.input },
    { key: 'payer', title: 'Payer', width: 120, type: JVxeTypes.input },
    { key: 'count', title: 'number', width: 40 },
    { key: 'company', title: 'company', minWidth: 180, type: JVxeTypes.input },
    { key: 'trend', title: 'trend', width: 120, type: JVxeTypes.input },
  ]);

  // Queryurladdress
  enum Api {
    getData = '/mock/vxe/getData',
  }

  loadData();

  // 加载number据
  function loadData() {
    loading.value = true;
    defHttp
      .get({
        url: Api.getData,
        params: { pageNo: 1, pageSize: 200 },
      })
      .then((result) => {
        dataSource.value = result.records;
      })
      .finally(() => {
        loading.value = false;
      });
  }

  /** Event triggered when cell value changes */
  function onValueChange(event) {
    switch (event.type) {
      // All cannot be triggered editClosed Event components，All need to be defined here，You can install your own business requirements to complete thecase
      case JVxeTypes.radio:
      case JVxeTypes.checkbox:
        doSendUpdateRow(event);
        break;
    }
  }

  // Event triggered after cell editing is completed
  function handleEditClosed(event) {
    doSendUpdateRow(event);
  }

  // 发送变更OK请求
  function doSendUpdateRow(event) {
    let { $table, row, column } = event;
    let field = column.property;
    // Determine whether the cell value has been modified
    if ($table.isUpdateByRow(row, field)) {
      // 校验当前OK
      $table.validate(row).then((errMap) => {
        // Verification passed
        if (!errMap) {
          // 【simulate saving】（This needs to be replaced with a real request）
          let hideLoading = createMessage.loading(`Saving"${column.title}"`, 0);
          setTimeout(() => {
            hideLoading();
            createMessage.success(`"${column.title}"Saved successfully！`);
            // Partially update cells to saved state
            $table.reloadRow(row, null, field);
            // Send update message
            tableRef.value?.socketSendUpdateRow(row);
          }, 555);
        }
      });
    }
  }
</script>

<style scoped></style>
