<template>
  <div class="p-4">
    <BasicTable @register="registerTable" @edit-end="handleEditEnd" @edit-cancel="handleEditCancel" :beforeEditSubmit="beforeEditSubmit" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, BasicColumn } from '/@/components/Table';
  import { optionsListApi } from '/@/api/demo/select';

  import { demoListApi } from '/@/api/demo/table';
  import { treeOptionsListApi } from '/@/api/demo/tree';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { mapTableTotalSummary } from "@/utils/common/compUtils";
  const columns: BasicColumn[] = [
    {
      title: 'Input box',
      dataIndex: 'name',
      edit: true,
      editComponentProps: {
        prefix: '$',
      },
      width: 200,
    },
    {
      title: 'Default input state',
      dataIndex: 'name7',
      edit: true,
      editable: true,
      width: 200,
    },
    {
      title: 'Input box校验',
      dataIndex: 'name1',
      edit: true,
      // Default required verification
      editRule: true,
      width: 200,
    },
    {
      title: 'Input box函数校验',
      dataIndex: 'name2',
      edit: true,
      editRule: async (text) => {
        if (text === '2') {
          return 'This value cannot be entered';
        }
        return '';
      },
      width: 200,
    },
    {
      title: '数字Input box',
      dataIndex: 'id',
      edit: true,
      editRule: true,
      editComponent: 'InputNumber',
      width: 200,
    },
    {
      title: 'drop down box',
      dataIndex: 'name3',
      edit: true,
      editComponent: 'Select',
      editComponentProps: {
        options: [
          {
            label: 'Option1',
            value: '1',
          },
          {
            label: 'Option2',
            value: '2',
          },
        ],
      },
      width: 200,
    },
    {
      title: 'Remote drop down',
      dataIndex: 'name4',
      edit: true,
      editComponent: 'ApiSelect',
      editComponentProps: {
        api: optionsListApi,
        resultField: 'list',
        labelField: 'name',
        valueField: 'id',
      },
      width: 200,
    },
    {
      title: 'Remote drop down树',
      dataIndex: 'name71',
      edit: true,
      editComponent: 'ApiTreeSelect',
      editRule: false,
      editComponentProps: {
        api: treeOptionsListApi,
        resultField: 'list',
      },
      width: 200,
    },
    {
      title: 'date selection',
      dataIndex: 'date',
      edit: true,
      editComponent: 'DatePicker',
      editComponentProps: {
        valueFormat: 'YYYY-MM-DD',
        format: 'YYYY-MM-DD',
      },
      width: 200,
    },
    {
      title: 'Time selection',
      dataIndex: 'time',
      edit: true,
      editComponent: 'TimePicker',
      editComponentProps: {
        valueFormat: 'HH:mm',
        format: 'HH:mm',
      },
      width: 200,
    },
    {
      title: 'tick box',
      dataIndex: 'name5',
      edit: true,
      editComponent: 'Checkbox',
      editValueMap: (value) => {
        return value ? 'yes' : 'no';
      },
      width: 200,
    },
    {
      title: 'switch',
      dataIndex: 'name6',
      edit: true,
      editComponent: 'Switch',
      editValueMap: (value) => {
        return value ? 'open' : 'close';
      },
      width: 200,
    },
  ];
  export default defineComponent({
    components: { BasicTable },
    setup() {
      const [registerTable] = useTable({
        title: 'Editable cell example',
        api: demoListApi,
        columns: columns,
        showIndexColumn: false,
        bordered: true,
        showSummary: true,
        summaryFunc: onSummary
      }); 

      const { createMessage } = useMessage();

      function handleEditEnd({ record, index, key, value }: Recordable) {
        console.log(record, index, key, value);
        return false;
      }

      // Simulate saving the specified data
      function feakSave({ value, key, id }) {
        createMessage.loading({
          content: `Simulating save${key}`,
          key: '_save_fake_data',
          duration: 0,
        });
        return new Promise((resolve) => {
          setTimeout(() => {
            if (value === '') {
              createMessage.error({
                content: 'Save failed：cannot be empty',
                key: '_save_fake_data',
                duration: 2,
              });
              resolve(false);
            } else {
              createMessage.success({
                content: `Record${id}of${key}saved`,
                key: '_save_fake_data',
                duration: 2,
              });
              resolve(true);
            }
          }, 2000);
        });
      }

      async function beforeEditSubmit({ record, index, key, value }) {
        console.log('Cell data is being prepared for submission', { record, index, key, value });
        return await feakSave({ id: record.id, key, value });
      }

      function handleEditCancel() {
        console.log('cancel');
      }

      function onSummary(tableData: Recordable[]) {
        // Available tool methods automatically calculate totals
        const totals = mapTableTotalSummary(tableData, ['id']);
        return [
          totals
        ];
      }
      return {
        registerTable,
        handleEditEnd,
        handleEditCancel,
        beforeEditSubmit,
      };
    },
  });
</script>
