<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #id="{ record }"> ID: {{ record.id }} </template>
      <template #no="{ record }">
        <Tag color="green">
          {{ record.no }}
        </Tag>
      </template>
      <template #bodyCell="{ column, record }">
        <Avatar v-if="column.key === 'avatar'" :size="60" :src="record.avatar" />
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
      slots: { customRender: 'avatar' },
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
      slots: { customRender: 'no' },
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
      const [registerTable] = useTable({
        title: 'Custom column content',
        titleHelpMessage: '表格中所有avatar、The pictures aremockgenerate，Only for demonstration image placeholder',
        api: demoListApi,
        columns: columns,
        bordered: true,
        showTableSetting: true,
      });

      return {
        registerTable,
      };
    },
  });
</script>
