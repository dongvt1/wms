import {BasicColumn, FormSchema} from '/@/components/Table';

const statusOptions = [
  {label: 'Disable', value: '0'},
  {label: 'enable', value: '1'},
]

export const columns: BasicColumn[] = [
  {
    title: 'Allowed table names',
    dataIndex: 'tableName',
  },
  {
    title: 'Allowed field names',
    dataIndex: 'fieldName',
  },
  {
    title: 'state',
    dataIndex: 'status',
    customRender({text}) {
      const find = statusOptions.find(opt => opt.value === text);
      return find?.label || 'unknown';
    }
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
  }
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Allowed table names',
    field: 'tableName',
    component: 'Input',
  },
  {
    label: 'Allowed field names',
    field: 'fieldName',
    component: 'Input',
  },
  {
    label: 'state',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: statusOptions,
    },
  },
];

export const formSchema: FormSchema[] = [
  {label: '', field: 'id', component: 'Input', show: false},
  {
    label: 'Allowed table names',
    field: 'tableName',
    component: 'Input',
    required: true,
  },
  {
    label: 'Allowed field names',
    field: 'fieldName',
    component: 'Input',
    required: true,
    helpMessage: 'Separate multiple with commas',
  },
  {
    label: 'state',
    field: 'status',
    component: 'Select',
    defaultValue: '1',
    componentProps: {
      options: statusOptions,
    },
  },
];
