<template>
  <div class="p-4">
    <BasicTable @register="registerTable" @edit-change="onEditChange">
      <template #action="{ record, column }">
        <TableAction :actions="createActions(record, column)" />
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, useTable, TableAction, BasicColumn, ActionItem, EditRecordRow } from '/@/components/Table';
  import { optionsListApi } from '/@/api/demo/select';

  import { demoListApi } from '/@/api/demo/table';
  import { treeOptionsListApi } from '/@/api/demo/tree';
  import { cloneDeep } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';

  const columns: BasicColumn[] = [
    {
      title: 'Input box',
      dataIndex: 'name',
      editRow: true,
      editComponentProps: {
        prefix: '$',
      },
      width: 150,
    },
    {
      title: 'Default input state',
      dataIndex: 'name7',
      editRow: true,
      width: 150,
    },
    {
      title: 'Input boxcheck',
      dataIndex: 'name1',
      editRow: true,
      align: 'left',
      // Default required verification
      editRule: true,
      width: 150,
    },
    {
      title: 'Input box函数check',
      dataIndex: 'name2',
      editRow: true,
      align: 'right',
      editRule: async (text) => {
        if (text === '2') {
          return 'This value cannot be entered';
        }
        return '';
      },
    },
    {
      title: '数字Input box',
      dataIndex: 'id',
      editRow: true,
      editRule: true,
      editComponent: 'InputNumber',
      width: 150,
    },
    {
      title: 'drop down box',
      dataIndex: 'name3',
      editRow: true,
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
          {
            label: 'Option3',
            value: '3',
          },
        ],
      },
      width: 200,
    },
    {
      title: 'Remote drop down',
      dataIndex: 'name4',
      editRow: true,
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
      dataIndex: 'name8',
      editRow: true,
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
      editRow: true,
      editComponent: 'DatePicker',
      editComponentProps: {
        valueFormat: 'YYYY-MM-DD',
        format: 'YYYY-MM-DD',
      },
      width: 150,
    },
    {
      title: 'Time selection',
      dataIndex: 'time',
      editRow: true,
      editComponent: 'TimePicker',
      editComponentProps: {
        valueFormat: 'HH:mm',
        format: 'HH:mm',
      },
      width: 100,
    },
    {
      title: 'tick box',
      dataIndex: 'name5',
      editRow: true,

      editComponent: 'Checkbox',
      editValueMap: (value) => {
        return value ? 'yes' : 'no';
      },
      width: 100,
    },
    {
      title: 'switch',
      dataIndex: 'name6',
      editRow: true,
      editComponent: 'Switch',
      editValueMap: (value) => {
        return value ? 'open' : 'close';
      },
      width: 100,
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableAction },
    setup() {
      const { createMessage: msg } = useMessage();
      const currentEditKeyRef = ref('');
      const [registerTable] = useTable({
        title: 'Editable row example',
        titleHelpMessage: ['In this case, modify[数字Input box]In this row，same row[Remote drop down]The current editing data of the column will also be changed simultaneously.'],
        api: demoListApi,
        columns: columns,
        showIndexColumn: false,
        showTableSetting: true,
        tableSetting: { fullScreen: true },
        actionColumn: {
          width: 160,
          title: 'Action',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });

      function handleEdit(record: EditRecordRow) {
        currentEditKeyRef.value = record.key;
        record.onEdit?.(true);
      }

      function handleCancel(record: EditRecordRow) {
        currentEditKeyRef.value = '';
        record.onEdit?.(false, false);
      }

      async function handleSave(record: EditRecordRow) {
        // check
        msg.loading({ content: 'Saving...', duration: 0, key: 'saving' });
        const valid = await record.onValid?.();
        if (valid) {
          try {
            const data = cloneDeep(record.editValueRefs);
            console.log(data);
            //TODO Here the data is submitted to the server for storage
            // ...
            // Submit editing status after saving
            const pass = await record.onEdit?.(false, true);
            if (pass) {
              currentEditKeyRef.value = '';
            }
            msg.success({ content: 'Data saved', key: 'saving' });
          } catch (error) {
            msg.error({ content: 'Save failed', key: 'saving' });
          }
        } else {
          msg.error({ content: 'Please fill in the correct data', key: 'saving' });
        }
      }

      function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
        if (!record.editable) {
          return [
            {
              label: 'edit',
              disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.key : false,
              onClick: handleEdit.bind(null, record),
            },
          ];
        }
        return [
          {
            label: 'save',
            onClick: handleSave.bind(null, record, column),
          },
          {
            label: 'Cancel',
            popConfirm: {
              title: 'yesnoCanceledit',
              confirm: handleCancel.bind(null, record, column),
            },
          },
        ];
      }

      function onEditChange({ column, value, record }) {
        // This example
        if (column.dataIndex === 'id') {
          record.editValueRefs.name4.value = `${value}`;
        }
        console.log(column, value, record);
      }

      return {
        registerTable,
        handleEdit,
        createActions,
        onEditChange,
      };
    },
  });
</script>
