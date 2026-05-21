<template>
  <div class="p-4">
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { BasicColumn } from '/@/components/Table/src/types/table';
  import { demoListApi } from '/@/api/demo/table';

  export default defineComponent({
    components: { BasicTable },
    setup() {
      function handleSummary(tableData: Recordable[]) {
        const totalAge = tableData.reduce((prev, next) => {
          prev += next.age;
          return prev;
        }, 0);
        const totalScore = tableData.reduce((prev, next) => {
          prev += next.score;
          return prev;
        }, 0);
        return [
          {
            _row: 'total',
            _index: 'average value',
            age: Math.round(totalAge / tableData.length),
            score: Math.round(totalScore / tableData.length),
          },
          {
            _row: 'total',
            _index: 'average value',
            age: totalAge,
            score: totalScore,
          },
        ];
      }
      const getBasicColumns = (): BasicColumn[] => {
        return [
          {
            title: 'ID',
            dataIndex: 'id',
            fixed: 'left',
            width: 200,
          },
          {
            title: 'Name',
            dataIndex: 'name',
            width: 150,
            filters: [
              { text: 'Male', value: 'male' },
              { text: 'Female', value: 'female' },
            ],
          },
          {
            title: 'age',
            dataIndex: 'age',
            width: 100,
          },
          {
            title: 'Score',
            dataIndex: 'score',
            width: 100,
            resizable: true,
          },
          {
            title: 'address',
            dataIndex: 'address',
            width: 300,
          },
          {
            title: 'serial number',
            dataIndex: 'no',
            width: 150,
            sorter: true,
            defaultHidden: true,
          },
          {
            title: 'start time',
            width: 150,
            sorter: true,
            dataIndex: 'beginTime',
          },
          {
            title: 'end time',
            width: 150,
            sorter: true,
            dataIndex: 'endTime',
          },
        ];
      };
      const [registerTable] = useTable({
        title: '表尾行total示例',
        api: demoListApi,
        rowSelection: { type: 'checkbox' },
        columns: getBasicColumns(),
        // showSummary: trueused是自定义的表尾行total方式，If not set or iffalseusedantd
        showSummary: true,
        summaryFunc: handleSummary,
        scroll: { x: 1000 },
        canResize: false,
      });

      return {
        registerTable,
      };
    },
  });
</script>
