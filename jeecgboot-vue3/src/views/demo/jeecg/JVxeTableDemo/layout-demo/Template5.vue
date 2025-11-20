<template>
  <a-card :bordered="false">
    <a-row :gutter="8">
      <a-col :span="6">
        <!-- plus show-line After attributes，The expand/collapse icon automatically changes to +- style -->
        <a-tree
          class="template-5-tree"
          :tree-data="treeData"
          show-icon
          show-line
          :expandedKeys="treeExpandedKeys"
          :selectedKeys="[pagination.current]"
          @expand="handleTreeExpand"
          @select="handleTreeSelect"
        >
          <!-- Custom child node icon -->
          <a-icon slot="myIcon" type="unordered-list" style="color: #0c8fcf" />
        </a-tree>
      </a-col>
      <a-col :span="18">
        <JVxeTable
          rowNumber
          rowSelection
          :height="750"
          :loading="loading"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="pagination"
          @pageChange="handleTablePageChange"
        />
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';

  // 【Various layout templates】Tree on the left，Line editing on the right
  export default {
    name: 'Template5',
    data() {
      return {
        // Is loading
        loading: false,
        // Pager parameters
        pagination: {
          // Current page number
          current: 1,
          // Number of items per page
          pageSize: 50,
          // The number of switchable items
          pageSizeOptions: ['50'],
          // Total data（The true total is not known at this time，So fill it in first0，Check it out in the background and then assign it.）
          total: 0,
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
            // 如果plus了该属性，It means that the current cell is editable，typeIt’s the type of form，inputJust a simple input box
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
        // 树的number据，这里模拟分Page固定number据，实际情况应该是后台查出来的number据
        treeData: [
          // No.1级number据
          {
            title: '1-10Page',
            key: '1-10',
            // No.2级number据
            children: [
              { title: 'No. 1 Page', key: 1, slots: { icon: 'myIcon' } },
              { title: 'No. 2 Page', key: 2, slots: { icon: 'myIcon' } },
              {
                title: 'No. 3 Page',
                key: 3,
                slots: { icon: 'myIcon' },
                // No.3级number据
                children: [
                  { title: 'No. 333 Page', key: 333, slots: { icon: 'myIcon' } },
                  { title: 'No. 444 Page', key: 444, slots: { icon: 'myIcon' } },
                  { title: 'No. 555 Page', key: 555, slots: { icon: 'myIcon' } },
                  // No.4No.5level and so on，plus children Just attribute
                ],
              },
              { title: 'No. 4 Page', key: 4, slots: { icon: 'myIcon' } },
              { title: 'No. 5 Page', key: 5, slots: { icon: 'myIcon' } },
              { title: 'No. 6 Page', key: 6, slots: { icon: 'myIcon' } },
              { title: 'No. 7 Page', key: 7, slots: { icon: 'myIcon' } },
              { title: 'No. 8 Page', key: 8, slots: { icon: 'myIcon' } },
              { title: 'No. 9 Page', key: 9, slots: { icon: 'myIcon' } },
              { title: 'No. 10 Page', key: 10, slots: { icon: 'myIcon' } },
            ],
            slots: { icon: 'myIcon' },
          },
          {
            title: '11-20Page',
            key: '11-20',
            children: [
              { title: 'No. 11 Page', key: 11, slots: { icon: 'myIcon' } },
              { title: 'No. 12 Page', key: 12, slots: { icon: 'myIcon' } },
              { title: 'No. 13 Page', key: 13, slots: { icon: 'myIcon' } },
              { title: 'No. 14 Page', key: 14, slots: { icon: 'myIcon' } },
              { title: 'No. 15 Page', key: 15, slots: { icon: 'myIcon' } },
              { title: 'No. 16 Page', key: 16, slots: { icon: 'myIcon' } },
              { title: 'No. 17 Page', key: 17, slots: { icon: 'myIcon' } },
              { title: 'No. 18 Page', key: 18, slots: { icon: 'myIcon' } },
              { title: 'No. 19 Page', key: 19, slots: { icon: 'myIcon' } },
              { title: 'No. 20 Page', key: 20, slots: { icon: 'myIcon' } },
            ],
            slots: { icon: 'myIcon' },
          },
        ],
        // 树展开columns，default 1-10
        treeExpandedKeys: ['1-10'],
        // Queryurladdress
        url: {
          getData: '/mock/vxe/getData',
        },
      };
    },
    created() {
      this.loadData();
    },
    methods: {
      // 加载行编辑的number据
      loadData() {
        // 封装Query条件
        let formData = {
          pageNo: this.pagination.current,
          pageSize: this.pagination.pageSize,
        };
        // 调用Querynumber据接口
        this.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            // 后台Query回来的 total，Total data量
            this.pagination.total = result.total;
            // 将Query的number据赋值给 dataSource
            this.dataSource = result.records;
            // Reset selection
            this.selectedRows = [];
          })
          .finally(() => {
            // Here is the method that will be executed regardless of success or failure，close hereloading
            this.loading = false;
          });
      },

      handleTablePageChange(event) {
        // reassign
        this.pagination.current = event.current;
        this.pagination.pageSize = event.pageSize;
        // Querynumber据
        this.loadData();
        // Judgment tree expandedkey
        if (event.current <= 10) {
          this.treeExpandedKeys = ['1-10'];
        } else {
          this.treeExpandedKeys = ['11-20'];
        }
      },

      // Event triggered by tree selection
      handleTreeSelect(selectedKeys) {
        let key = selectedKeys[0];
        if (typeof key === 'string') {
          // 控制树展开为当前选择columns
          this.treeExpandedKeys = selectedKeys;
        } else {
          this.pagination.current = key;
          this.loadData();
        }
      },

      // Event triggered by tree selection
      handleTreeExpand(expandedKeys) {
        this.treeExpandedKeys = expandedKeys;
      },
    },
  };
</script>

<style lang="less">
  /** Hide file icon */
  .template-5-tree.ant-tree {
    li span.ant-tree-switcher.ant-tree-switcher-noop {
      display: none;
    }
  }
</style>
