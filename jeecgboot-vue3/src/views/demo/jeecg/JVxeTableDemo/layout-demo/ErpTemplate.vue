<template>
  <a-card :bordered="false">
    <JVxeTable
      toolbar
      :toolbarConfig="toolbarConfig"
      rowNumber
      rowSelection
      rowSelectionType="radio"
      clickSelectRow
      highlightCurrentRow
      :height="tableHeight"
      :loading="table1.loading"
      :columns="table1.columns"
      :dataSource="table1.dataSource"
      :pagination="table1.pagination"
      :expandConfig="expandConfig"
      style="margin-bottom: 8px"
      @pageChange="handleTable1PageChange"
      @selectRowChange="handleTable1SelectRowChange"
    ></JVxeTable>

    <a-tabs v-show="subTabs.show" :class="{ 'sub-tabs': true, 'un-expand': !subTabs.expand }">
      <a-tab-pane tab="Subtable1" key="1">
        <JVxeTable
          toolbar
          row-number
          row-selection
          height="auto"
          :maxHeight="350"
          :loading="table2.loading"
          :columns="table2.columns"
          :dataSource="table2.dataSource"
          :pagination="table2.pagination"
          @pageChange="handleTable2PageChange"
          @selectRowChange="handleTable2SelectRowChange"
        />
      </a-tab-pane>
      <a-tab-pane tab="Subtable2" key="2">
        <h1>这里是Subtable2</h1>
        <h1>这里是Subtable2</h1>
        <h1>这里是Subtable2</h1>
        <h1>这里是Subtable2</h1>
        <h1>这里是Subtable2</h1>
        <h1>这里是Subtable2</h1>
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script>
  import { h } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { Button, Checkbox } from 'ant-design-vue';
  import { UpOutlined, DownOutlined } from '@ant-design/icons-vue';

  export default {
    name: 'ErpTemplate',
    data() {
      return {
        toolbarConfig: {
          // prefix prefix；suffix suffix
          slot: ['prefix', 'suffix'],
          // add Add button；remove delete button；clearSelection Clear selection button
          btn: ['add', 'remove', 'clearSelection'],
        },

        expandConfig: {
          // Whether only one row can be expanded at the same time
          accordion: true,
        },

        // Subtable tabs
        subTabs: {
          show: false,
          // Whether to expand
          expand: true,
          // Whether to automatically expand
          autoExpand: true,
        },

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
            showTotal: (total, range) => {
              // Here is jsx grammar
              let text = h('span', `${range[0]}-${range[1]} common ${total} strip`);
              // 判断Subtable是否显示，If displayed, render the expand/collapse button
              if (this.subTabs.show) {
                let expand = h('span', {}, [
                  h(
                    Button,
                    {
                      type: 'link',
                      onClick: this.handleToggleTabs,
                    },
                    () => [this.subTabs.expand ? h(UpOutlined) : h(DownOutlined), h('span', {}, this.subTabs.expand ? 'close' : 'Expand')]
                  ),
                  h(
                    Checkbox,
                    {
                      // h Writing method is not supported v-model , So you need to assign values ​​manually
                      checked: this.subTabs.autoExpand,
                      'onUpdate:checked': (checked) => (this.subTabs.autoExpand = checked),
                    },
                    () => '自动Expand'
                  ),
                ]);
                // Return multipledomUse array
                return [expand, text];
              } else {
                // Return directly to a singledom
                return text;
              }
            },
          },
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
            { key: 'call', title: 'call', width: '990px', type: JVxeTypes.input },
            { key: 'len', title: 'long', width: '80px', type: JVxeTypes.inputNumber },
            { key: 'ton', title: 'ton', width: '120px', type: JVxeTypes.inputNumber },
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
        // Configuration information of child tables （The configuration is exactly the same as that of the main table，I won’t write redundant comments.）
        table2: {
          currentRowId: null,
          loading: false,
          pagination: { current: 1, pageSize: 10, pageSizeOptions: ['5', '10', '20', '30'], total: 0 },
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
        currentSubRow: null,
        // Queryurladdress
        url: {
          getData: '/mock/vxe/getData',
        },
      };
    },
    computed: {
      tableHeight() {
        let { show, expand } = this.subTabs;
        return show ? (expand ? 350 : 482) : 482;
      },
    },
    created() {
      this.loadTable1Data();
    },
    methods: {
      // loadtable1【main table】的number据
      loadTable1Data() {
        // 封装Querystrip件
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

      // QuerySubtablenumber据
      loadSubData(row) {
        if (row) {
          // There must be restrictions here，限制不能重复Query，Otherwise, an infinite loop will occur
          if (this.table2.currentRowId === row.id) {
            return true;
          }
          this.table2.currentRowId = row.id;
          this.loadTable2Data();
          return true;
        } else {
          return false;
        }
      },
      // QuerySubtablenumber据
      loadTable2Data() {
        let table2 = this.table2;
        let formData = {
          parentId: table2.currentRowId,
          pageNo: this.table2.pagination.current,
          pageSize: this.table2.pagination.pageSize,
        };
        table2.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            // 将Query的number据赋值给 dataSource
            table2.selectedRows = [];
            table2.dataSource = result.records;
            table2.pagination.total = result.total;
          })
          .finally(() => {
            // Here is the method that will be executed regardless of success or failure，close hereloading
            table2.loading = false;
          });
      },

      // table1【main table】whenselected row变化时触发的事件
      handleTable1SelectRowChange(event) {
        this.table1.selectedRows = event.selectedRows;
        this.subTabs.show = true;
        if (this.subTabs.autoExpand) {
          this.subTabs.expand = true;
        }
        this.loadSubData(event.selectedRows[0]);
      },
      // table2【Subtable】whenselected row变化时触发的事件
      handleTable2SelectRowChange(event) {
        this.table2.selectedRows = event.selectedRows;
      },

      handleTable1PageChange(event) {
        // reassign
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Querynumber据
        this.loadTable1Data();
      },
      // whentable2【Subtable】分页参number变化时触发的事件
      handleTable2PageChange(event) {
        // reassign
        this.table2.pagination.current = event.current;
        this.table2.pagination.pageSize = event.pageSize;
        // Querynumber据
        this.loadTable2Data();
      },

      // Expand或closeSubtabletabs
      handleToggleTabs() {
        this.subTabs.expand = !this.subTabs.expand;
      },
    },
  };
</script>

<style lang="less" scoped>
  .sub-tabs {
    &.un-expand {
      :deep(.ant-tabs-content) {
        height: 0 !important;
      }

      :deep(.ant-tabs-nav) {
        border-color: transparent !important;
      }

      :deep(.ant-tabs-ink-bar) {
        background-color: transparent !important;
      }

      :deep(.ant-tabs-tab) {
        display: none !important;
      }
    }
  }
</style>
