import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'name',
    dataIndex: 'testName',
    width: 200,
  },
  {
    title: 'value',
    dataIndex: 'testValue',
    width: 180,
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    width: 180,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'testName',
    label: 'name',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'testName',
    label: 'name',
    required: true,
    component: 'Input',
  },
  {
    field: 'testValue',
    label: 'value',
    required: true,
    component: 'Input',
  },

  {
    label: ' ',
    field: 'menu',
    slot: 'menu',
    component: 'Input',
  },
];
