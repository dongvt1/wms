<template>
  <a-card title="Instant save example" :bordered="false">
    <!--
      【Instantly save general ideas】：
      1. JVxeTable must be added keep-source property
      2. monitor edit-closedevent，这个event是在编辑完成后触发
      3. 在这个event里面判断number据是否更改，If changed, call the interface to save the operation.
    -->
    <JVxeTable
      toolbar
      :toolbarConfig="toolbarConfig"
      rowNumber
      rowSelection
      keepSource
      asyncRemove
      :height="340"
      :loading="loading"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="pagination"
      @save="handleTableSave"
      @removed="handleTableRemove"
      @edit-closed="handleEditClosed"
      @pageChange="handlePageChange"
      @selectRowChange="handleSelectRowChange"
    />
  </a-card>
</template>

<script lang="ts" setup>
  // Instant save example
  import { reactive, ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  // Toolbar button configuration
  const toolbarConfig = reactive({
    // add Add button；remove delete button；clearSelection Clear selection button
    btn: ['add', 'save', 'remove', 'clearSelection'],
  });
  // Is loading
  const loading = ref(false);
  // Pager parameters
  const pagination = reactive({
    // Current page number
    current: 1,
    // Number of items per page
    pageSize: 200,
    // The number of switchable items
    pageSizeOptions: ['10', '20', '30', '100', '200'],
    // Total data（The true total is not known at this time，So fill it in first0，Check it out in the background and then assign it.）
    total: 0,
  });
  // selected row
  const selectedRows = ref<Recordable[]>([]);
  // data source，Control table data
  const dataSource = ref<Recordable[]>([]);
  // column configuration，Control the columns displayed in the table
  const columns = ref<JVxeColumn[]>([
    { key: 'num', title: 'serial number', width: 80, type: JVxeTypes.normal },
    {
      // Fieldkey，跟后台number据的Field名匹配
      key: 'ship_name',
      // column title
      title: 'ship name',
      // column width
      width: 180,
      // 如果加上了该property，It means that the current cell is editable，typeIt’s the type of form，inputJust a simple input box
      type: JVxeTypes.input,
    },
    { key: 'call', title: 'call', width: 80, type: JVxeTypes.input },
    { key: 'len', title: 'long', width: 80, type: JVxeTypes.input },
    { key: 'ton', title: 'ton', width: 120, defaultValue: 233, type: JVxeTypes.input },
    { key: 'payer', title: 'Payer', width: 120, defaultValue: 'Zhang San', type: JVxeTypes.input },
    { key: 'count', title: 'number', width: 40, type: JVxeTypes.normal },
    {
      key: 'company',
      title: 'company',
      // minimum width，What is different from width is，This is not a fixed width，If the table has extra space，will be evenly distributed among the settings minWidth columns
      // 如果要做占满表格columns可以这么写
      minWidth: 180,
      type: JVxeTypes.input,
    },
    { key: 'trend', title: 'trend', width: 120, type: JVxeTypes.input },
  ]);

  // Queryurladdress
  enum Api {
    getData = '/mock/vxe/getData',
    // simulate saving单行number据（Instant save）
    saveRow = '/mock/vxe/immediateSaveRow',
    // simulate saving整个表格的number据
    saveAll = '/mock/vxe/immediateSaveAll',
  }

  loadData();

  // 加载number据
  async function loadData() {
    loading.value = true;
    // 调用Querynumber据接口
    await defHttp
      .get({
        // 请求address
        url: Api.getData,
        // 封装Query条件
        params: {
          pageNo: pagination.current,
          pageSize: pagination.pageSize,
        },
      })
      .then((result) => {
        // 后台Query回来的 total，Total data量
        pagination.total = result.total;
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

  // 【overall preservation】点击保存按钮时触发的event
  function handleTableSave({ $table, target }) {
    // Check the entire table
    $table.validate().then((errMap) => {
      // Verification passed
      if (!errMap) {
        // 获取所有number据
        let tableData = target.getTableData();
        console.log('当前保存的number据是：', tableData);
        // 获取新增的number据
        let newData = target.getNewData();
        console.log('-- 新增的number据：', newData);
        // 获取delete的number据
        let deleteData = target.getDeleteData();
        console.log('-- delete的number据：', deleteData);
        // 【simulate saving】
        loading.value = true;
        defHttp
          .post({
            url: Api.saveAll,
            params: tableData,
          })
          .then(() => {
            createMessage.success(`Saved successfully！`);
          })
          .finally(() => {
            loading.value = false;
          });
      }
    });
  }

  // 触发单元格deleteevent
  function handleTableRemove(event) {
    // Bundle event.deleteRows Pass it to the background for deletion（Notice：这里不会传递前端逻辑新增的number据，Because there is no need to request background deletion）
    console.log('待delete的number据: ', event.deleteRows);
    // You can also just sendID，Because it can be based onIDdelete
    let deleteIds = event.deleteRows.map((row) => row.id);
    console.log('待delete的number据ids: ', deleteIds);

    // 模拟请求后台delete
    loading.value = true;
    window.setTimeout(() => {
      loading.value = false;
      createMessage.success('delete成功');
      // 假设后台返回delete成功，must be called confirmRemove() method，will be actually removed from the table（会同时delete选中的逻辑新增的number据）
      event.confirmRemove();
    }, 1000);
  }

  // 单元格编辑完成之后触发的event
  function handleEditClosed(event) {
    let { $table, row, column } = event;
    let field = column.property;
    // Determine whether the cell value has been modified
    if ($table.isUpdateByRow(row, field)) {
      // Verify current line
      $table.validate(row).then((errMap) => {
        // Verification passed
        if (!errMap) {
          // 【simulate saving】
          let hideLoading = createMessage.loading(`Saving"${column.title}"`, 0);
          console.log('Instant savenumber据：', row);
          defHttp
            .put({
              url: Api.saveRow,
              params: row,
            })
            .then((res) => {
              createMessage.success(`"${column.title}"Saved successfully！`);
              // Partially update cells to saved state
              $table.reloadRow(row, null, field);
            })
            .finally(() => {
              hideLoading();
            });
        }
      });
    }
  }

  // 当分页参number变化时触发的event
  function handlePageChange(event) {
    // reassign
    pagination.current = event.current;
    pagination.pageSize = event.pageSize;
    // Querynumber据
    loadData();
  }

  // 当selected row变化时触发的event
  function handleSelectRowChange(event) {
    selectedRows.value = event.selectedRows;
  }
</script>

<style scoped></style>
