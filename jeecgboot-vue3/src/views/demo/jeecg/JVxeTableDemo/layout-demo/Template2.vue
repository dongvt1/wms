<template>
  <a-card :bordered="false">
    <a-row :gutter="8">
      <!-- left parent -->
      <a-col :span="12">
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          clickSelectRow
          highlightCurrentRow
          :radioConfig="{ highlight: false }"
          :checkboxConfig="{ highlight: false }"
          :height="790"
          :loading="table1.loading"
          :columns="table1.columns"
          :dataSource="table1.dataSource"
          :pagination="table1.pagination"
          @pageChange="handleTable1PageChange"
          @selectRowChange="handleTable1SelectRowChange"
        />
      </a-col>
      <a-col :span="12">
        <!-- The data selected on the left is displayed here -->
        <JVxeTable rowNumber :height="375" :columns="table1.columns" :dataSource="table1.selectedRows" style="margin: 52px 0 8px" />

        <!-- lower right -->
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          clickSelectRow
          :height="355"
          :loading="table2.loading"
          :columns="table2.columns"
          :dataSource="table2.dataSource"
          :pagination="table2.pagination"
          @pageChange="handleTable2PageChange"
          @selectRowChange="handleTable2SelectRowChange"
        />
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';

  // 【Various layout templates】 After selecting on the left，Record selected to the right，On the right is the father、son
  export default {
    name: 'Template2',
    data() {
      return {
        table1: {
          // Is loading
          loading: false,
          // Pager parameters
          pagination: {
            // Current page number
            current: 1,
            // Number of items per page
            pageSize: 200,
            // The number of switchable items
            pageSizeOptions: ['10', '20', '30', '100', '200'],
            // Total data（The true total is not known at this time，So fill it in first0，Check it out in the background and then assign it.）
            total: 0,
          },
          // Last selected row
          lastRow: null,
          // selected row
          selectedRows: [],
          // data source，Control table data
          dataSource: [],
          // column configuration，Control the columns displayed in the table
          columns: [
            { key: 'num', title: 'serial number', width: '80px' },
            {
              // Fieldkey，跟后台number据的Field名匹配
              key: 'ship_name',
              // column title
              title: 'ship name',
              // column width
              width: '180px',
              // If this attribute is added，It means that the current cell is editable，typeIt’s the type of form，inputJust a simple input box
              type: JVxeTypes.input,
            },
            { key: 'call', title: 'call', width: '80px', type: JVxeTypes.input },
            { key: 'len', title: 'long', width: '80px', type: JVxeTypes.input },
            { key: 'ton', title: 'ton', width: '120px', type: JVxeTypes.input },
            { key: 'payer', title: 'Payer', width: '120px', type: JVxeTypes.input },
            { key: 'count', title: 'number', width: '40px' },
            {
              key: 'company',
              title: 'company',
              // minimum width，What is different from width is，This is not a fixed width，If the table has extra space，will be evenly distributed among the settings minWidth columns
              // 如果要做占满表格columns可以这么写
              minWidth: '180px',
              type: JVxeTypes.input,
            },
            { key: 'trend', title: 'trend', width: '120px', type: JVxeTypes.input },
          ],
        },
        // son级表的配置信息 （The configuration is exactly the same as that of the main table，I won’t write redundant comments.）
        table2: {
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
            { key: 'port_area', title: 'Port area', width: '120px', type: JVxeTypes.input },
          ],
        },
        // Queryurladdress
        url: {
          getData: '/mock/vxe/getData',
        },
      };
    },
    // listener
    watch: {
      // monitortable1 【main table】选择的number据发生了变化
      ['table1.lastRow']() {
        this.loadTable2Data();
      },
    },
    created() {
      this.loadTable1Data();
    },
    methods: {
      // loadtable1【main table】的number据
      loadTable1Data() {
        // 封装Query条件
        let formData = {
          pageNo: this.table1.pagination.current,
          pageSize: this.table1.pagination.pageSize,
        };
        // 调用Querynumber据接口
        this.table1.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            // 后台Query回来的 total，Total data量
            this.table1.pagination.total = result.total;
            // 将Query的number据赋值给 dataSource
            this.table1.dataSource = result.records;
            // Reset selection
            this.table1.selectedRows = [];
          })
          .finally(() => {
            // Here is the method that will be executed regardless of success or failure，close hereloading
            this.table1.loading = false;
          });
      },

      // loadtable2【son表】的number据，根据main table的id进行Query
      loadTable2Data() {
        // 如果main table没有选择，则不Query
        let selectedRows = this.table1.selectedRows;
        if (!selectedRows || selectedRows.length === 0) {
          this.table2.pagination.total = 0;
          this.table2.dataSource = [];
          this.table2.selectedRows = [];
          return;
        } else if (this.table1.lastRow == null) {
          this.table1.lastRow = selectedRows[selectedRows.length - 1];
        }
        let formData = {
          parentId: this.table1.lastRow.id,
          pageNo: this.table2.pagination.current,
          pageSize: this.table2.pagination.pageSize,
        };
        this.table2.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            this.table2.pagination.total = result.total;
            this.table2.dataSource = result.records;
            this.table2.selectedRows = [];
          })
          .finally(() => {
            this.table2.loading = false;
          });
      },

      // table1【main table】whenselected row变化时触发的事件
      handleTable1SelectRowChange(event) {
        this.handleTableSelectRowChange(this.table1, event);
      },

      // table2【son表】whenselected row变化时触发的事件
      handleTable2SelectRowChange(event) {
        this.table2.selectedRows = event.selectedRows;
      },

      // whentable1【main table】分页参number变化时触发的事件
      handleTable1PageChange(event) {
        // reassign
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Querynumber据
        this.loadTable1Data();
      },

      // whentable2【son表】分页参number变化时触发的事件
      handleTable2PageChange(event) {
        // reassign
        this.table2.pagination.current = event.current;
        this.table2.pagination.pageSize = event.pageSize;
        // Querynumber据
        this.loadTable2Data();
      },

      /** public method：Handle table selection change events */
      handleTableSelectRowChange(table, event) {
        let { row, action, selectedRows, $table } = event;
        // Get the last selected
        let lastSelected = selectedRows[selectedRows.length - 1];
        if (action === 'selected') {
          table.lastRow = row;
        } else if (action === 'selected-all') {
          // Deselect all
          if (selectedRows.length === 0) {
            table.lastRow = null;
          } else if (!table.lastRow) {
            table.lastRow = lastSelected;
          }
        } else if (action === 'unselected' && row === table.lastRow) {
          table.lastRow = lastSelected;
        }
        $table.setCurrentRow(table.lastRow);
        table.selectedRows = selectedRows;
      },
    },
  };
</script>

<style scoped></style>
