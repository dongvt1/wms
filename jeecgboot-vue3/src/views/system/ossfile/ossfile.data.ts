import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'File name',
    dataIndex: 'fileName',
    width: 120,
  },
  {
    title: 'File address',
    dataIndex: 'url',
    width: 100,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'File name',
    field: 'fileName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'File address',
    field: 'url',
    component: 'Input',
    colProps: { span: 6 },
  },
];
