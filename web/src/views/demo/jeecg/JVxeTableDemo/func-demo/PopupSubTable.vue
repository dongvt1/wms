<template>
  <a-card title="Pop up subtable example" :bordered="false">
    <!--
      【General idea of ​​popping up subtables】
      1. Must have clickRowShowSubForm property，如果该property设为false，Then the subtable will not pop up
      2. Must have subForm slot，Used to specify the content of the pop-up subtable
      3. highlightCurrentRow property可有可无，If there is, when clicking on a row，The background color of the guild will always be on
    -->
    <!--
      【General idea of ​​popping up detailed information】
      1. Must have clickRowShowMainForm property，如果该property设为false，Then the details will not pop up
      2. Must have mainForm slot，Used to specify pop-up content
    -->
    <JVxeTable
      toolbar
      rowNumber
      rowSelection
      highlightCurrentRow
      clickRowShowSubForm
      clickRowShowMainForm
      :height="750"
      :loading="loading"
      :columns="columns"
      :dataSource="dataSource"
      @detailsConfirm="handleDetailsConfirm"
    >
      <!-- main form -->
      <template #mainForm="{ row }">
        <template v-if="row">
          <a-form ref="form2" :model="row" :rules="rules" :label-col="labelCol" :wrapper-col="wrapperCol">
            <a-row :gutter="8">
              <a-col :span="8">
                <a-form-item label="ID" name="id">
                  <a-input v-model:value="row.id" disabled />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="serial number" name="num">
                  <a-input v-model:value="row.num" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="ship name" name="ship_name">
                  <a-input v-model:value="row.ship_name" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="call" name="call">
                  <a-input v-model:value="row.call" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="long" name="len">
                  <a-input v-model:value="row.len" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="ton" name="ton">
                  <a-input v-model:value="row.ton" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Payer" name="payer">
                  <a-input v-model:value="row.payer" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="number" name="count">
                  <a-input v-model:value="row.count" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="company" name="company">
                  <a-input v-model:value="row.company" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="trend" name="trend">
                  <a-input v-model:value="row.trend" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </template>
      </template>

      <!-- subform -->
      <template #subForm="{ row }">
        <template v-if="loadSubData(row)">
          <JVxeTable
            ref="subFormTable"
            height="auto"
            :max-height="350"
            :loading="subTable.loading"
            :columns="subTable.columns"
            :dataSource="subTable.dataSource"
          />
        </template>
      </template>
    </JVxeTable>
  </a-card>
</template>

<script lang="ts" setup>
  // Pop up subtable example
  import { reactive, ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  const loading = ref(false);
  const dataSource = ref([]);
  const columns = ref<JVxeColumn[]>([
    { key: 'num', title: 'serial number', width: '80px' },
    { key: 'ship_name', title: 'ship name', width: '180px', type: JVxeTypes.input },
    { key: 'call', title: 'call', width: '80px' },
    { key: 'len', title: 'long', width: '80px' },
    { key: 'ton', title: 'ton', width: '120px' },
    { key: 'payer', title: 'Payer', width: '120px' },
    { key: 'count', title: 'number', width: '40px' },
    {
      key: 'company',
      title: 'company',
      minWidth: '180px',
      // Whether to click to display detailed information
      // It only takes effect when the current cell cannot be edited.
      // If not set，Click to pop up only the sub-table，Details of the main table will not pop up
      showDetails: true,
    },
    { key: 'trend', title: 'trend', width: '120px' },
  ]);
  const selectedRows = ref([]);
  // Subtable information
  const subTable = reactive({
    currentRowId: null,
    loading: false,
    pagination: { current: 1, pageSize: 200, pageSizeOptions: ['100', '200'], total: 0 },
    selectedRows: [],
    dataSource: [],
    columns: [
      { key: 'dd_num', title: '调度serial number', width: '120px' },
      { key: 'tug', title: 'tugboat', width: '180px', type: JVxeTypes.input },
      { key: 'work_start_time', title: 'Job start time', width: '180px', type: JVxeTypes.input },
      { key: 'work_stop_time', title: 'Job end time', width: '180px', type: JVxeTypes.input },
      { key: 'type', title: 'Ship classification', width: '120px', type: JVxeTypes.input },
      { key: 'port_area', title: 'Port area', minWidth: '120px', type: JVxeTypes.input },
    ] as JVxeColumn[],
  });

  // formform col
  const labelCol = reactive({ span: 4 });
  const wrapperCol = reactive({ span: 20 });
  const rules = reactive({
    num: [{ required: true, message: '必须输入serial number' }],
  });

  // Queryurladdress
  enum Api {
    getData = '/mock/vxe/getData',
  }

  loadData();

  // 加载number据
  function loadData() {
    // 封装Query条件
    // 调用Querynumber据接口
    loading.value = true;
    defHttp
      .get({
        url: Api.getData,
        params: {
          pageNo: 1,
          pageSize: 30,
        },
      })
      .then((result) => {
        // 将Query的number据赋值给 dataSource
        dataSource.value = result.records;
        // Reset selection
        selectedRows.value = [];
      })
      .finally(() => {
        // Here is the method that will be executed regardless of success or failure，close hereloading
        loading.value = false;
      });
  }

  // Query子表number据
  function loadSubData(row) {
    if (row) {
      // There must be restrictions here，限制不能重复Query，Otherwise, an infinite loop will occur
      if (subTable.currentRowId === row.id) {
        return true;
      }
      subTable.currentRowId = row.id;
      subTable.loading = true;
      defHttp
        .get({
          url: Api.getData,
          params: {
            pageNo: 1,
            pageSize: 30,
            parentId: row.id,
          },
        })
        .then((result) => {
          // 将Query的number据赋值给 dataSource
          subTable.dataSource = result.records;
        })
        .finally(() => {
          // Here is the method that will be executed regardless of success or failure，close hereloading
          subTable.loading = false;
        });
      return true;
    } else {
      return false;
    }
  }

  // Click the confirm button in the details
  function handleDetailsConfirm({ row, $table, callback }) {
    console.log('保存的number据：', row);
    // Verify current line
    $table.validate(row).then((errMap) => {
      // Verification passed
      if (!errMap) {
        // syndrome table，if necessary，You can operate the following object：
        callback(true);
        loading.value = true;
        setTimeout(() => {
          loading.value = false;
          createMessage.success('Saved successfully');
        }, 1000);
      } else {
        callback(false);
        createMessage.warn('Verification failed');
      }
    });
  }
</script>

<style scoped></style>
