<template>
  <div class="p-4">
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, h } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { demoListApi } from '/@/api/demo/table';
  import { BasicColumn } from '/@/components/Table/src/types/table';
  /** 
    Advantages compared to the original：
    1、Table The total row will not be misaligned when the column header is dragged;
    2、Total row usageTableSummaryway to render；
    3、supportslotCustomize total row display；
    4、Column addedcustomSummaryRenderCustom rendering function
    5、Can be solved in non-configurationapicase，When filtering by column，The problem that the total row statistics value is not updated
  */
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
            _index: 'total',
            age: totalAge,
            score: totalScore,
          },
        ];
      }
      const getBasicColumns = () :BasicColumn[] => {
        return [
          {
            title: 'ID',
            dataIndex: 'id',
            fixed: 'left',
            width: 200,
            resizable: true,
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
            resizable: true,
            customSummaryRender: ({ text }) => {
              // return <span style="color: red;">{text}</span>;
              return h('span', { style: { color: 'red' } }, [text]);
            },
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
        title: 'Native summary column（表尾total）Example',
        api: demoListApi,
        rowSelection: { type: 'checkbox' },
        columns: getBasicColumns(),
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
