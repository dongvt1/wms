<template>
  <div class="p-4">
    <a-card :bordered="false" style="height: 100%">
      <a-tabs v-model:activeKey="activeKey" @change="tabChange">
        <a-tab-pane :key="item.key" :tab="item.label" v-for="item in compList" />
      </a-tabs>
      <component :is="currentComponent" />
    </a-card>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import {
    AuthColumnDemo,
    BasicTableBorder,
    BasicTableDemo,
    BasicTableDemoAjax,
    CustomerCellDemo,
    EditCellTableDemo,
    EditRowTableDemo,
    ExpandTableDemo,
    ExportTableDemo,
    FixedHeaderColumn,
    InnerTableDemo,
    MergeHeaderDemo,
    MergeTableDemo,
    SelectTableDemo,
    TreeTableDemo,
  } from './index';
  export default defineComponent({
    name: 'document-table-demo',
    components: {
      AuthColumnDemo,
      BasicTableBorder,
      BasicTableDemo,
      BasicTableDemoAjax,
      CustomerCellDemo,
      EditCellTableDemo,
      EditRowTableDemo,
      ExpandTableDemo,
      ExportTableDemo,
      FixedHeaderColumn,
      InnerTableDemo,
      MergeHeaderDemo,
      MergeTableDemo,
      SelectTableDemo,
      TreeTableDemo,
    },
    setup() {
      //Currently selectedkey
      const activeKey = ref('BasicTableDemo');
      //Component collection
      const compList = ref([
        { key: 'BasicTableDemo', label: 'Basic static table' },
        { key: 'BasicTableDemoAjax', label: 'conventionalAJAXsheet' },
        { key: 'BasicTableBorder', label: '边框sheet' },
        { key: 'CustomerCellDemo', label: 'Custom column content' },
        { key: 'EditCellTableDemo', label: 'Editable cells' },
        { key: 'EditRowTableDemo', label: 'Editable lines' },
        { key: 'ExpandTableDemo', label: '可展开sheet' },
        { key: 'ExportTableDemo', label: 'Import and export' },
        { key: 'FixedHeaderColumn', label: 'Fixed header and column example' },
        { key: 'InnerTableDemo', label: '内嵌sheet' },
        { key: 'MergeHeaderDemo', label: 'Group header example' },
        { key: 'MergeTableDemo', label: 'Merge rows and columns' },
        { key: 'SelectTableDemo', label: '可选择sheet' },
        { key: 'TreeTableDemo', label: '树形sheet' },
        { key: 'AuthColumnDemo', label: 'Permission column settings' },
      ]);
      //Currently selected组件
      const currentComponent = computed(() => {
        return activeKey.value;
      });

      //usecomponentdynamic switchingtab
      function tabChange(key) {
        activeKey.value = key;
      }
      return {
        activeKey,
        currentComponent,
        tabChange,
        compList,
      };
    },
  });
</script>
