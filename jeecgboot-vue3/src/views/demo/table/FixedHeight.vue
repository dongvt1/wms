<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #customTitle>
        <span>
          Name
          <BasicHelp class="ml-2" text="Name" />
        </span>
      </template>
      <template #customAddress>
        address
        <FormOutlined class="ml-2" />
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getCustomHeaderColumns } from './tableData';
  import { FormOutlined } from '@ant-design/icons-vue';
  import { demoListApi } from '/@/api/demo/table';
  import { BasicHelp } from '/@/components/Basic';

  export default defineComponent({
    components: { BasicTable, FormOutlined, BasicHelp },
    setup() {
      const [registerTable] = useTable({
        title: 'Set height/Head customization',
        api: demoListApi,
        columns: getCustomHeaderColumns(),
        canResize: false,
        scroll: { y: 100 },
      });

      return {
        registerTable,
      };
    },
  });
</script>
