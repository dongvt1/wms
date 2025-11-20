<template>
  <PageWrapper>
    <BasicTable @register="registerTable" :striped="true" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable } from '/@/components/Table';
  import { mapTableTotalSummary } from '/@/utils/common/compUtils';
  const dataSource = ref<any>([]);
  setTimeout(() => {
    dataSource.value = [
      { id: 0, name: 'Zhang San', point: 23, level: 3, updateTime: '2019-8-14' },
      { id: 1, name: 'deer', point: 33, level: 9, updateTime: '2019-8-10' },
      { id: 2, name: 'Xiao Wang', point: 6, level: 1, updateTime: '2019-8-13' },
      { id: 3, name: 'John Doe', point: 53, level: 8, updateTime: '2019-8-12' },
      { id: 4, name: 'Xiaohong', point: 44, level: 5, updateTime: '2019-8-11' },
      { id: 5, name: 'Wang Wu', point: 97, level: 10, updateTime: '2019-8-10' },
      { id: 6, name: 'Xiao Ming', point: 33, level: 2, updateTime: '2019-8-10' },
      { id: 7, name: 'Xiao Zhang', point: 33, level: 4, updateTime: '2019-8-10' },
      { id: 8, name: 'Xiaoliu', point: 33, level: 2, updateTime: '2019-8-10' },
      { id: 9, name: 'Xiaowu', point: 33, level: 7, updateTime: '2019-8-10' },
      { id: 10, name: 'Xiao Zhao', point: 33, level: 2, updateTime: '2019-8-10' },
      { id: 11, name: 'Li Hua', point: 33, level: 8, updateTime: '2019-8-10' },
      { id: 12, name: 'well-off', point: 33, level: 5, updateTime: '2019-8-10' },
    ];
  }, 1e3);
  const [registerTable] = useTable({
    rowKey: 'id',
    bordered: true,
    canResize: true,
    columns: [
      { title: 'Name', width: 500, dataIndex: 'name' },
      { title: 'Contribution points', width: 500, dataIndex: 'point' },
      { title: 'grade', width: 500, dataIndex: 'level' },
      { title: 'Update time', width: 500, dataIndex: 'updateTime' },
    ],
    dataSource: dataSource,
    // Show bottom total
    showSummary: true,
    striped: true,
    // Bottom total calculation method
    summaryFunc: onSummary,
  });

  function onSummary(tableData: Recordable[]) {
    // Available tool methods automatically calculate totals
    const totals = mapTableTotalSummary(tableData, ['point', 'level']);
    return [
      totals,
      {
        _row: 'average',
        _index: 'average',
        // 计算average值
        point: (totals.point / tableData.length).toFixed(2),
        level: (totals.level / tableData.length).toFixed(0),
      },
    ];
  }
</script>
