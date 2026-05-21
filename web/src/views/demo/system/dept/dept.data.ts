import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: 'Department name',
    dataIndex: 'deptName',
    width: 160,
    align: 'left',
  },
  {
    title: 'sort',
    dataIndex: 'orderNo',
    width: 50,
  },
  {
    title: 'state',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => {
      const status = record.status;
      const enable = ~~status === 0;
      const color = enable ? 'green' : 'red';
      const text = enable ? 'enable' : 'deactivate';
      return h(Tag, { color: color }, () => text);
    },
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: 'Remark',
    dataIndex: 'remark',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'deptName',
    label: 'Department name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'state',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'enable', value: '0' },
        { label: 'deactivate', value: '1' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'deptName',
    label: 'Department name',
    component: 'Input',
    required: true,
  },
  {
    field: 'parentDept',
    label: 'superior department',
    component: 'TreeSelect',

    componentProps: {
      replaceFields: {
        title: 'deptName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
    required: true,
  },
  {
    field: 'orderNo',
    label: 'sort',
    component: 'InputNumber',
    required: true,
  },
  {
    field: 'status',
    label: 'state',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'enable', value: '0' },
        { label: 'deactivate', value: '1' },
      ],
    },
    required: true,
  },
  {
    label: 'Remark',
    field: 'remark',
    component: 'InputTextArea',
  },
];
