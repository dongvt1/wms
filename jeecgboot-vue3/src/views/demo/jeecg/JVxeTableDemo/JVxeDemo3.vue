<template>
  <div>
    <ol style="border: 1px solid #cccccc; width: 600px; padding: 8px">
      <li>1. turn on dragSort After attributes, you can drag and drop up and down to sort.。</li>
      <li>2. use sortKey Properties can be sorted and saved in a customized manner key，Default is orderNum。</li>
      <li>3. use sortBegin Attributes can customize the starting value of sorting，Default is 0。</li>
      <li>4. sortKey The defined fields do not need to be defined in columns The value can also be obtained normally in。</li>
      <li>5. when exists fixed List time，Drag sorting will not work，Can only be sorted up and down。</li>
    </ol>

    <p> the following示例turn on了拖拽排序，The sorting value saving field is sortNum，The starting value of sorting is 3<br /> </p>

    <JVxeTable
      ref="tableRef1"
      toolbar
      dragSort
      sortKey="sortNum"
      :sortBegin="3"
      rowSelection
      dragSortFixed="none"
      rowSelectionFixed="none"
      :maxHeight="580"
      :columns="table1.columns"
      :dataSource="table1.data"
    >
      <template #toolbarSuffix>
        <a-button @click="onGetData1">Get data</a-button>
      </template>
    </JVxeTable>

    <br />
    <p>the following fixed The table does not support drag and drop sorting，Only supports clicking up and down to sort</p>

    <JVxeTable ref="tableRef2" toolbar dragSort rowSelection :maxHeight="580" :columns="table2.columns" :dataSource="table2.data">
      <template #toolbarSuffix>
        <a-button @click="onGetData2">Get data</a-button>
      </template>
    </JVxeTable>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const tableRef1 = ref<JVxeTableInstance>();
  const tableRef2 = ref<JVxeTableInstance>();
  const table1 = reactive({
    columns: [
      {
        title: 'ID',
        key: 'id',
        width: 120,
        type: JVxeTypes.normal,
      },
      {
        title: 'Name',
        key: 'name',
        width: 240,
        type: JVxeTypes.input,
        defaultValue: 'new name',
      },
      {
        title: 'field length',
        key: 'dbLength',
        width: 2400,
        type: JVxeTypes.inputNumber,
        defaultValue: 32,
      },
      {
        title: 'sortNum',
        key: 'sortNum',
        width: 120,
        type: JVxeTypes.normal,
      },
    ] as JVxeColumn[],
    data: [
      { id: 'uuid-0001', name: 'Zhang San', dbLength: 123 },
      { id: 'uuid-0002', name: 'John Doe', dbLength: 777 },
      { id: 'uuid-0003', name: 'Wang Wu', dbLength: 666 },
      { id: 'uuid-0004', name: 'Zhao Liu', dbLength: 233 },
    ],
  });

  const table2 = reactive({
    columns: [
      {
        title: 'ID',
        key: 'id',
        width: 320,
        fixed: 'left',
        type: JVxeTypes.normal,
      },
      {
        title: 'Name',
        key: 'name',
        width: 720,
        type: JVxeTypes.input,
        defaultValue: 'new name',
      },
      {
        title: 'field length',
        key: 'dbLength',
        width: 720,
        type: JVxeTypes.inputNumber,
        defaultValue: 32,
      },
    ] as JVxeColumn[],
    data: [
      { id: 'uuid-0001', name: 'Zhang San', dbLength: 123 },
      { id: 'uuid-0002', name: 'John Doe', dbLength: 777 },
      { id: 'uuid-0003', name: 'Wang Wu', dbLength: 666 },
      { id: 'uuid-0004', name: 'Zhao Liu', dbLength: 233 },
    ],
  });

  const { createMessage } = useMessage();

  function onGetData1() {
    createMessage.info('Please see the console');
    console.log(tableRef1.value!.getTableData());
  }

  function onGetData2() {
    createMessage.info('Please see the console');
    console.log(tableRef2.value!.getTableData());
  }
</script>
