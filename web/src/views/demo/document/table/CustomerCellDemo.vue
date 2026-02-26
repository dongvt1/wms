<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #id="{ record }"> ID: {{ record.id }} </template>
      <template #bodyCell="{ column, record }">
        <Avatar v-if="column.key === 'avatar'" :size="60" :src="record.avatar" />
        <Tag v-if="column.key === 'no'" color="green">
          {{ record.no }}
        </Tag>
      </template>
      <template #img="{ text }">
        <TableImg :size="60" :simpleShow="true" :imgList="text" />
      </template>
      <template #imgs="{ text }"> <TableImg :size="60" :imgList="text" /> </template>

      <template #category="{ record }">
        <Tag color="green">
          {{ record.no }}
        </Tag>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, BasicColumn, TableImg } from '/@/components/Table';
  import { Tag, Avatar } from 'ant-design-vue';
  import { demoListApi } from '/@/api/demo/table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      slots: { customRender: 'id' },
    },
    {
      title: 'avatar',
      dataIndex: 'avatar',
      width: 100,
    },
    {
      title: 'Classification',
      dataIndex: 'category',
      width: 80,
      align: 'center',
      defaultHidden: true,
      slots: { customRender: 'category' },
    },
    {
      title: 'Name',
      dataIndex: 'name',
      width: 120,
    },
    {
      title: 'Picture list1',
      dataIndex: 'imgArr',
      helpMessage: ['这是简单模式的Picture list', 'Only one will be displayed in the table', 'But click to preview multiple pictures'],
      width: 140,
      slots: { customRender: 'img' },
    },
    {
      title: 'Photo list2',
      dataIndex: 'imgs',
      width: 160,
      slots: { customRender: 'imgs' },
    },
    {
      title: 'address',
      dataIndex: 'address',
    },
    {
      title: 'serial number',
      dataIndex: 'no',
    },
    {
      title: 'start time',
      dataIndex: 'beginTime',
    },
    {
      title: 'end time',
      dataIndex: 'endTime',
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableImg, Tag, Avatar },
    setup() {
      const { tableContext } = useListPage({
        tableProps: {
          title: 'Custom column content',
          titleHelpMessage: '表格中所有avatar、The pictures aremockgenerate，Only for demonstration image placeholder',
          api: demoListApi,
          columns: columns,
          bordered: true,
          showTableSetting: false,
          showActionColumn: false,
          useSearchForm: false,
        },
      });
      //registertabledata
      const [registerTable] = tableContext;
      return {
        registerTable,
      };
    },
  });
</script>
