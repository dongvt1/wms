<template>
  <div class="p-4">
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicColumn, BasicTable, useTable } from '/@/components/Table';

  import { demoListApi } from '/@/api/demo/table';
  //Calculate merge header

  export default defineComponent({
    components: { BasicTable },
    setup() {
      const [registerTable] = useTable({
        title: 'Group header example',
        api: demoListApi,
        columns: getMergeHeaderColumns(),
        bordered: true,
        useSearchForm: false,
      });

      function getMergeHeaderColumns(): BasicColumn[] {
        return [
          {
            title: 'ID',
            dataIndex: 'id',
            width: 300,
          },
          {
            title: 'Name',
            dataIndex: 'name',
            width: 300,
          },
          {
            title: 'address',
            width: 120,
            children: [
              {
                title: 'address',
                dataIndex: 'address',
                key: 'address',
                width: 200,
              },
              {
                title: 'serial number',
                dataIndex: 'no',
                key: 'no',
              },
            ],
          },
          {
            title: 'start time',
            dataIndex: 'beginTime',
            width: 200,
          },
          {
            title: 'end time',
            dataIndex: 'endTime',
            width: 200,
          },
        ];
      }
      return {
        registerTable,
      };
    },
  });
</script>
