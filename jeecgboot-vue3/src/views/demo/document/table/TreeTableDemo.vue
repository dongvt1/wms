<template>
  <div class="p-4">
    <BasicTable @register="register">
      <template #toolbar>
        <a-button type="primary" @click="expandAll">Expand all</a-button>
        <a-button type="primary" @click="collapseAll">Collapse all</a-button>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicColumn, BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
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
  export default defineComponent({
    components: { BasicTable },
    setup() {
      const { tableContext } = useListPage({
        tableProps: {
          title: 'tree table',
          isTreeTable: true,
          rowSelection: {
            type: 'checkbox',
            getCheckboxProps(record: Recordable) {
              // Demo: first line（idfor0）The select box is disabled
              if (record.id === '0') {
                return { disabled: true };
              } else {
                return { disabled: false };
              }
            },
          },
          columns: columns,
          dataSource: getTreeTableData(),
          rowKey: 'id',
          useSearchForm: false,
        },
      });
      //registertabledata
      const [register, { expandAll, collapseAll }] = tableContext;
      function getTreeTableData() {
        const data: any = (() => {
          const arr: any = [];
          for (let index = 0; index < 40; index++) {
            arr.push({
              id: `${index}`,
              name: 'John Brown',
              age: `1${index}`,
              no: `${index + 10}`,
              address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
              beginTime: new Date().toLocaleString(),
              endTime: new Date().toLocaleString(),
              children: [
                {
                  id: `l2-${index}`,
                  name: 'John Brown',
                  age: `1${index}`,
                  no: `${index + 10}`,
                  address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
                  beginTime: new Date().toLocaleString(),
                  endTime: new Date().toLocaleString(),
                },
                {
                  id: `l3-${index}`,
                  name: 'John Mary',
                  age: `1${index}`,
                  no: `${index + 10}`,
                  address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
                  beginTime: new Date().toLocaleString(),
                  endTime: new Date().toLocaleString(),
                },
              ],
            });
          }
          return arr;
        })();

        return data;
      }
      return { register, expandAll, collapseAll };
    },
  });
</script>
