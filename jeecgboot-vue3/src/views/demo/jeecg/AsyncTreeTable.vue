<template>
  <PageWrapper>
    <a-card :bordered="false">
      <BasicTable :loading="loading" :dataSource="dataSource" @register="registerTable" @expand="onExpand" />
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable } from '/@/components/Table';

  const url = '/mock/api/asynTreeList';

  const loading = ref<boolean>(false);
  const dataSource = ref<any[]>([]);
  const [registerTable, { setLoading }] = useTable({
    rowKey: 'id',
    bordered: true,
    canResize: false,
    // tree table
    isTreeTable: true,
    showIndexColumn: true,
    columns: [
      { title: 'name', dataIndex: 'name' },
      { title: 'components', dataIndex: 'component' },
      { title: 'sort', dataIndex: 'orderNum' },
    ],
  });

  async function loadData(params) {
    loading.value = true;
    let result = await defHttp.get({ url, params });
    loading.value = false;
    return result.map((item) => {
      if (item.hasChildren) {
        return { ...item, children: [] };
      } else {
        return item;
      }
    });
  }

  async function loadRootData() {
    dataSource.value = await loadData({ id: '0' });
  }

  loadRootData();

  async function onExpand(isExpand, rowData) {
    if (isExpand && rowData.hasChildren && rowData.children.length === 0) {
      rowData.children = await loadData({ id: rowData.id });
    }
  }
</script>
