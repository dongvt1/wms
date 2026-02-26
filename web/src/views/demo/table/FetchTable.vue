<template>
  <PageWrapper contentBackground contentClass="flex" dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleReloadCurrent"> Refresh current page </a-button>
        <a-button type="primary" @click="handleReload"> Refresh and return to the first page </a-button>
      </template>
    </BasicTable>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getBasicColumns } from './tableData';
  import { PageWrapper } from '/@/components/Page';

  import { demoListApi } from '/@/api/demo/table';
  export default defineComponent({
    components: { BasicTable, PageWrapper },
    setup() {
      const [registerTable, { reload }] = useTable({
        title: 'Remote loading example',
        api: demoListApi,
        columns: getBasicColumns(),
        pagination: { pageSize: 10 },
      });
      function handleReloadCurrent() {
        reload();
      }

      function handleReload() {
        reload({
          page: 1,
        });
      }
      return {
        registerTable,
        handleReloadCurrent,
        handleReload,
      };
    },
  });
</script>
