import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'table name',
    dataIndex: 'dataTable',
    width: 150,
    align: 'left',
  },
  {
    title: 'dataID',
    dataIndex: 'dataId',
    width: 350,
  },
  {
    title: 'version number',
    dataIndex: 'dataVersion',
    width: 100,
  },
  {
    title: 'data内容',
    dataIndex: 'dataContent',
  },
  {
    title: 'Creator',
    dataIndex: 'createBy',
    sorter: true,
    width: 200,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'dataTable',
    label: 'table name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dataId',
    label: 'dataID',
    component: 'Input',
    colProps: { span: 8 },
  },
];
